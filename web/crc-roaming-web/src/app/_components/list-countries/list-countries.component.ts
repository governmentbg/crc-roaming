import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AddEditCountryComponent } from '@components/add-edit-country/add-edit-country.component';
import { ConfirmDialogComponent } from '@components/confirm-dialog/confirm-dialog.component';
import { config, modalMinWidth } from '@env/config';
import { NgbdSortableHeader } from '@helpers/ngbd-sortable-header';
import { SortEvent } from '@interfaces/sort-event';
import { Country } from '@models/country.model';
import { CountryService } from '@services/country.service';
import { PermissionService } from '@services/permission.service';
import { UIService } from '@services/ui.service';
import { first } from 'rxjs/operators';

@Component({
    selector: 'app-list-countries',
    templateUrl: './list-countries.component.html'
})
export class ListCountriesComponent implements OnInit {
    @ViewChildren(NgbdSortableHeader) headers: QueryList<NgbdSortableHeader>;
    countries: Country[];
    formatTS: string;
    editPerm: boolean;

    constructor(
        private countryService: CountryService,
        private dialog: MatDialog,
        private ui: UIService,
        private perms: PermissionService
    ) {
        this.formatTS = config.formatTS;
        this.editPerm = this.perms.hasAccess(this.perms.all.editCountries);
        if (!this.perms.hasAccess(this.perms.all.viewCountries)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.loadCountries();
    }

    loadCountries(sortBy?: string, direction?: string): void {
        this.countryService.getCountries(sortBy, direction).pipe(first()).subscribe(data => {
            this.countries = data;
        });
    }

    add() {
        this.openAddEdit(false, null);
    }

    edit(country: Country) {
        this.openAddEdit(true, country);
    }

    private openAddEdit(editMode: boolean, country: Country) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(AddEditCountryComponent, modalMinWidth);
            ref.componentInstance.editMode = editMode;
            ref.componentInstance.country = country;
            ref.afterClosed().subscribe(reload => {
                reload && this.loadCountries();
            });
        }
    }

    delete(country: Country) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(ConfirmDialogComponent, {data: {
                titleKey: "countries.confirmDeleteTitle",
                textKey: "countries.confirmDeleteText",
                textParams: {name: country.nameBg}}});
            ref.afterClosed().subscribe(confirmed => {
                confirmed && this.countryService.deleteCountry(country.id).subscribe(data => {
                    this.loadCountries();
                    this.ui.showSuccess("deleteSuccessful");
                })
            });
        }
    }

    onSort({ column, direction }: SortEvent) {
        // clear other sort
        this.headers.forEach(header => {
            if (header.sortable !== column) {
                header.direction = '';
            }
        });
        this.loadCountries(column, direction);
    }

}
