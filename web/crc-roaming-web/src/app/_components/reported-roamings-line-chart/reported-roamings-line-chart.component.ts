import { Component } from '@angular/core';
import { NgbCalendar } from '@ng-bootstrap/ng-bootstrap';
import { ReportingService } from '@services/reporting.service';
import { OperatorService } from '@services/operator.service';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { Operator } from '@models/operator.model';
import { Country } from '@models/country.model';
import { Region } from '@models/region.model';
import { Color, Label } from 'ng2-charts';
import { first } from 'rxjs/operators';
import { Util } from '@helpers/util';
import { TranslateService } from '@ngx-translate/core';
import { ExportService } from '@services/export.service';
import { UIService } from '@services/ui.service';
import { PermissionService } from '@services/permission.service';
import { lineChartOptions } from '@env/config';

@Component({
  selector: 'app-reported-roamings-line-chart',
  templateUrl: './reported-roamings-line-chart.component.html'
})
export class ReportedRoamingsLineChartComponent  {
    fromMonth: number;
    toMonth: number;
    fromYear: number;
    toYear: number;    
    months: number[];
    years: number[];
    countries: Country[];
    operators: Operator[];
    regions: Region[];
    countryId: number;
    operatorId: number;
    regionId: number;
    disableOperator: boolean;

    lineChartData: ChartDataSets[];
    lineChartOptions: (ChartOptions & { annotation ?: any });
    lineChartLabels: Label[];
    lineChartColors: Color[];
    lineChartLegend: boolean;
    lineChartType: string;

    constructor(
      private reportingService: ReportingService,
      private operatorService: OperatorService,
      private ngbCalendar: NgbCalendar,
      private translate: TranslateService,
      private exportService: ExportService,
      private ui: UIService,
      private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.viewReports)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
        this.countries = [];
        this.operators = [];
        this.regions = [];
        this.lineChartData = [];
        this.lineChartColors = [];
        this.months = Util.getMonths();
        this.years = Util.getYears();
        this.loadCommonChartSettings();
    }

    ngOnInit() {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.loadDates();
      this.reportingService.fillRegions(this.regions);
      this.reportingService.fillCountries(this.countries);

      this.loadReporetLineChart();
    }

    loadReporetLineChart() : void {
      this.reportingService.getReportedRoamingEvolutionChartsData(this.fromMonth, this.toMonth, this.fromYear, this.toYear, this.countryId, this.operatorId, this.regionId).pipe(first()).subscribe(result => {
            this.lineChartData = [{ data: result.counts, label: this.translate.instant("menu.reports.reportedEventsLinear")}];
            this.lineChartLabels = result.labels;
        });
    }

    exportChart() {
      this.exportService.exportSingleLineChartAsPDF(
        'reportedRoamingLineChart',
        this.getFilterHeading(), this.getFilterBody(),
        this.translate.instant("menu.reports.reportedEventsLinear"))
    }

    private getFilterHeading(): any[][] {
        return [
            [{content: this.translate.instant('report.filterHeader'), colSpan: 7, styles: {halign: 'center'}}],
            [
                this.translate.instant('filter.fromMonth'),
                this.translate.instant('filter.fromYear'),
                this.translate.instant('filter.toMonth'),
                this.translate.instant('filter.toYear'),
                this.translate.instant('filter.country'),
                this.translate.instant('filter.operator'),
                this.translate.instant('filter.region'),
            ]
        ];
    }

    private getFilterBody(): any[] {
        let all = this.translate.instant('filter.all');
        let country = Util.filter(this.countries, this.countryId);
        let operator = Util.filter(this.operators, this.operatorId);
        let region = Util.filter(this.regions, this.regionId);

        return [[
            this.translate.instant('month.' + this.fromMonth),
            this.fromYear,
            this.translate.instant('month.' + this.toMonth),
            this.toYear,
            country ? country.nameBg : all,
            operator ? operator.name : all,
            region ? region.name : all
        ]];
    }

    filter() : void {
      this.nullifyCharts();
      this.loadReporetLineChart();
    }
  
    clearFilters() : void {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.nullifyCharts();
      this.loadDates();
      this.loadReporetLineChart();
    }
  
    nullifyCharts() {
      this.lineChartData = [];
      this.lineChartLabels = [];
    }
    
    loadDates() {
      let today = this.ngbCalendar.getToday();
      this.fromMonth = today.month - 1;
      this.toMonth = today.month;
      this.fromYear = today.year;
      this.toYear = today.year;
    }

    onCountrySelect(e) {
      this.countryId = e.target.value;
      this.disableOperatorIfNeeded(this.countryId);
      this.nullifyCountriesAndOperators(true);
      this.loadOperatorsByCountry(this.countryId);
    }

    loadOperatorsByCountry(selectedCountryId: number): void {
      this.operatorService.getOperatorsByCountry(selectedCountryId.toString())
          .subscribe(o => this.operators = o);
    }

    disableOperatorIfNeeded(countryId: number): void {
      if(this.countryId === 0) {
        this.disableOperator = true;
      } else {
        this.disableOperator = false
      }
    }

    nullifyCountriesAndOperators(onlyOperator: boolean): void {
      if(onlyOperator) {
        this.operatorId = 0;
        this.operators = [];
      } else {
        this.countryId = 0;
        this.operatorId = 0;
        this.regionId = 0;
        this.operators = [];
      }
    }

    loadCommonChartSettings(): void {
        this.lineChartColors = this.lineChartColorsSettings;
        this.lineChartLegend = true;
        this.lineChartType = 'line';
        this.lineChartOptions = lineChartOptions;
    }

    public lineChartColorsSettings: Array<any> = [{
      borderColor: 'black',
      backgroundColor: 'rgba(255, 0, 0, 0.3)',
    }];

}
