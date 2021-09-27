import * as turf from '@turf/turf';
import * as L from 'leaflet';
import 'leaflet.markercluster';
import 'leaflet/dist/images/marker-shadow.png';
import 'leaflet/dist/images/marker-icon.png';
import 'leaflet/dist/images/marker-icon-2x.png';

import { AfterViewInit, Component, OnInit } from '@angular/core';
import { PublicPortalService } from '@services/public-portal.service';
import { first } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { config } from '@env/config';
import { NgbDate, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Region } from '@models/region.model';
import { ReportingService } from '@services/reporting.service';
import { Country } from '@models/country.model';
import { Operator } from '@models/operator.model';
import { Util } from '@helpers/util';
import { Router } from '@angular/router';
import { ZoneService } from '@services/zone.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UIService } from '@services/ui.service';
import { Polygon } from '@models/polygon.model';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '@components/confirm-dialog/confirm-dialog.component';
import { ReportedRoaming } from '@models/reported-roaming.model';
import { PermissionService } from '@services/permission.service';

@Component({
    selector: 'app-roaming-map',
    templateUrl: './roaming-map.component.html'
})
export class RoamingMapComponent implements OnInit, AfterViewInit {
    // Map crap
    mapId: number;
    map: L.Map;
    clusterLayer: L.MarkerClusterGroup;
    polygonLayer: L.FeatureGroup;
    drawer: L.Draw.Polygon;
    editor: L.Edit.Polyline;
    reportedRoamings: ReportedRoaming[];
    
    // Home page or Zones page
    addEditForm: FormGroup;
    polygonList: Polygon[];
    managePolygonsPage: boolean;
    addEditPolygonMode: boolean;
    polygonInEditMode: Polygon;
    preEditPolygon: Polygon;
    submitted: boolean;
    loading: boolean;
    editPerm: boolean;

    // Filter
    fromDate: NgbDateStruct;
    toDate: NgbDateStruct;
    regions: Region[];
    countries: Country[];
    allOperators: Operator[];
    operators: Operator[];
    selectedRegion: number;
    selectedCountry: number;
    selectedOperator: number;
    disableOperator: boolean;

    constructor(
        private publicService: PublicPortalService,
        private reportingService: ReportingService,
        public translate: TranslateService,
        private datepipe: DatePipe,
        private router: Router,
        private zoneServie: ZoneService,
        private ui: UIService,
        private formBuilder: FormBuilder,
        private dialog: MatDialog,
        private perms: PermissionService
    ) {
        this.managePolygonsPage = this.router.url === '/zones';
        this.editPerm = this.perms.hasAccess(this.perms.all.editZones);
        if (this.managePolygonsPage && !this.perms.hasAccess(this.perms.all.viewZones)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }

        this.mapId = new Date().getTime();
        this.regions = [];
        this.countries = [];
        this.allOperators = [];
        this.operators = [];
        this.selectedRegion = 0;
        this.selectedCountry = 0;
        this.selectedOperator = 0;
        this.disableOperator = true;
        this.polygonList = [];
        this.addEditPolygonMode = false;
    }

    get form() { return this.addEditForm.controls; }

    ngOnInit() {
        let d = new Date();
        this.toDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
        d.setMonth(d.getMonth() - 1);
        this.fromDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());

        this.reportingService.fillRegions(this.regions);
        this.reportingService.fillCountries(this.countries);
        this.reportingService.fillOperators(this.allOperators);
        this.translate.currentLang

        this.translate.onLangChange.subscribe(event => {
            this.fillReportedRoamings(this.reportedRoamings, event.translations['map.markerPopup']);
        });
    }

    ngAfterViewInit() {
        delete (L.Icon.Default.prototype as any)._getIconUrl;

        L.Icon.Default.mergeOptions({
            iconRetinaUrl:  'assets/img/marker-icon-2x.png',
            iconUrl:        'assets/img/marker-icon.png',
            shadowUrl:      'assets/img/marker-shadow.png',
        });
        
        this.clusterLayer = L.markerClusterGroup();
        let tiles = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", { minZoom: 7, maxZoom: 19 });
        
        this.map = new L.Map("map_" + this.mapId);
        this.map.setView(new L.LatLng(42.75, 25.50), 8);
        this.map.addLayer(tiles);
        this.map.addLayer(this.clusterLayer);

        if (this.managePolygonsPage) {
            // Validator
            this.addEditForm = this.formBuilder.group({
                name: ['', [Validators.required, Validators.maxLength(128)]],
            });

            // Map / Layer config
            this.translate.get("map.polyDrawStart").subscribe(tr => {
                L.drawLocal.draw.handlers.polygon.tooltip.start = tr;
                L.drawLocal.draw.handlers.polygon.tooltip.cont = this.translate.instant("map.polyDrawCont");
                L.drawLocal.draw.handlers.polygon.tooltip.end = this.translate.instant("map.polyDrawEnd");
            });

            this.drawer = new L.Draw.Polygon(this.map as L.DrawMap, {shapeOptions: {color: config.polygonEditColor}});
            this.polygonLayer = new L.FeatureGroup();

            this.map.addLayer(this.polygonLayer);

            let _this = this;
            this.map.on(L.Draw.Event.CREATED, function(event) {
                let polygon = event.layer as L.Polygon;
                _this.polygonLayer.addLayer(polygon);
                _this.polygonInEditMode.leafletId = _this.polygonLayer.getLayerId(polygon);
                _this.polygonInEditMode.coordinates = Util.latLngsToCoordinates(polygon.getLatLngs() as L.LatLng[]);

                _this.drawer.disable();
                (polygon as any).editing.enable();
            });

            this.zoneServie.getZones().pipe(first()).subscribe(r => {
                r.forEach(z => {
                    _this.addPolygonToMap(z)
                    _this.polygonList.push(z);
                });
            });
        }

        this.reloadReportedRoamings();
    }

    onFilterChange() {
        if (this.selectedCountry != 0) {
            this.disableOperator = false;
            this.operators = Util.filterOperatorsByCountry(this.allOperators, this.selectedCountry);
        } else {
            this.selectedOperator = 0;
            this.disableOperator = true;
        }

        this.reloadReportedRoamings();
    }

    reloadReportedRoamings() {
        this.translate.get("map.markerPopup").subscribe(template => {
            this.publicService.getMapReportedRoamings(this.fromDate, this.toDate, this.selectedCountry, this.selectedOperator, this.selectedRegion)
                .pipe(first()).subscribe(roamings => {
                    this.reportedRoamings = roamings;
                    this.fillReportedRoamings(this.reportedRoamings, template);
                });
        });
    }

    fillReportedRoamings(roamings: ReportedRoaming[], template: string) {
        this.clusterLayer.clearLayers();
        roamings && roamings.forEach(roaming => {
            let text = template
                .replace("{operator}", roaming.operator.name)
                .replace("{country}", roaming.operator.country.nameBg)
                .replace("{region}", roaming.region.name)
                .replace("{mcc}", roaming.operator.country.mcc + '')
                .replace("{mnc}", roaming.operator.mnc + '')
                .replace("{ts}", this.datepipe.transform(roaming.eventTs, config.formatTS))
                .replace("{coord}", roaming.latitude + ',' + roaming.longitude);

            let marker = new L.Marker(new L.LatLng(roaming.latitude, roaming.longitude));
            marker.bindPopup(text);

            this.clusterLayer.addLayer(marker);
        });
    }

    getPolygon(poly: Polygon): L.Polygon {
        return this.polygonLayer.getLayer(poly.leafletId) as L.Polygon;
    }

    private addPolygonToMap(poly: Polygon) {
        let polygon = new L.Polygon(Util.coordinatesToLatLngs(poly.coordinates));
        polygon.setStyle({color: config.polygonColor});
        polygon.bindTooltip(poly.name);
        
        this.polygonLayer.addLayer(polygon);
        poly.leafletId = this.polygonLayer.getLayerId(polygon);
    }

    mouseOver(poly: Polygon) {
        !this.addEditPolygonMode && this.getPolygon(poly).setStyle({color: config.polygonEditColor});
    }

    mouseOut(poly: Polygon) {
        !this.addEditPolygonMode && this.getPolygon(poly).setStyle({color: config.polygonColor});
    }

    zoomTo(poly: Polygon) {
        const polyLayer = this.getPolygon(poly);
        this.map.fitBounds(polyLayer.getBounds());
    }

    newPolygon() {
        if (!this.addEditPolygonMode) {
            this.addEditPolygonMode = true;
            this.drawer.enable();

            this.polygonInEditMode = new Polygon();
            this.polygonList.unshift(this.polygonInEditMode);
        }
    }

    editPolygon(poly: Polygon) {
        if (this.addEditPolygonMode) {
            return;
        }

        this.addEditPolygonMode = true;
        this.polygonInEditMode = poly;
        this.preEditPolygon = Object.assign({}, poly);
        this.addEditForm.patchValue(poly);
        
        const polyLayer = this.getPolygon(poly);
        (polyLayer as any).editing.enable();
        this.map.fitBounds(polyLayer.getBounds());
    }

    savePolygon() {
        this.submitted = true;
        this.loading = true;
        if (this.addEditForm.invalid) {
            this.loading = false;
            return;
        }

        let overlap: boolean = false;
        let editedPolygon = this.getPolygon(this.polygonInEditMode);
        let p1Feature = editedPolygon.toGeoJSON();
        this.polygonLayer.eachLayer(pl => {
            if (pl != editedPolygon) {
                let p2Feature = (pl as L.Polygon).toGeoJSON();
                if (turf.booleanOverlap(p1Feature, p2Feature)
                 || turf.booleanContains(p1Feature, p2Feature)
                 || turf.booleanWithin(p1Feature, p2Feature)) {
                    overlap = true;
                    return;
                }
            }
        });

        if (overlap) {
            this.ui.showError("msg.polygonOverlaps");
            this.submitted = false;
            this.loading = false;
            return;
        }
        
        this.polygonInEditMode.name = this.form.name.value;
        this.polygonInEditMode.coordinates = Util.latLngsToCoordinates(editedPolygon.getLatLngs() as L.LatLng[]);
        this.zoneServie.addEditZone(this.polygonInEditMode).subscribe(r => {
            this.submitted = false;
            this.loading = false;
            this.ui.showSuccess(this.preEditPolygon ? 'editSuccessful' : 'addSuccessful');
            this.cancelAddEdit(true);
        }, error => {
            this.loading = false;
        });
    }

    cancelAddEdit(saved?: boolean) {
        let editedPolygon = this.getPolygon(this.polygonInEditMode);
        editedPolygon && this.polygonLayer.removeLayer(editedPolygon);

        if (saved) {
            this.addPolygonToMap(this.polygonInEditMode);
        } else {
            if (this.preEditPolygon) {
                // Cancel editing of existing record
                this.polygonInEditMode.name = this.preEditPolygon.name;
                this.addPolygonToMap(this.polygonInEditMode);
            } else {
                // Cancel editing of a new record
                this.polygonList.shift();
            }
        }

        this.drawer.disable();
        this.addEditPolygonMode = false;
        this.polygonInEditMode = null;
        this.addEditForm.reset();
        this.preEditPolygon = null;
    }

    deletePolygon(poly: Polygon) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(ConfirmDialogComponent, {data: {
                titleKey: "zones.confirmDeleteTitle",
                textKey: "zones.confirmDeleteText",
                textParams: {name: poly.name}}});
            ref.afterClosed().subscribe(confirmed => {
                confirmed && this.zoneServie.deleteZone(poly.id).subscribe(data => {
                    Util.removeElement(this.polygonList, poly);
                    this.polygonLayer.removeLayer(this.getPolygon(poly));
                    this.ui.showSuccess("deleteSuccessful");
                });
            });
        }
    }

    deleteLastVertex() {
        this.drawer.deleteLastVertex();
    }

}
