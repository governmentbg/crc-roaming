import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { config } from '@env/config';
import { Country } from '@models/country.model';
import { Operator } from '@models/operator.model';
import { CountryService } from '@services/country.service';
import { OperatorService } from '@services/operator.service';
import { PermissionService } from '@services/permission.service';
import { UIService } from '@services/ui.service';

@Component({
    selector: 'app-add-edit-operator',
    templateUrl: './add-edit-operator.component.html'
})
export class AddEditOperatorComponent implements OnInit {
    editMode: boolean;
    operator: Operator;
    countries: Country[];
    loading = false;
    submitted = false;
    addEditForm: FormGroup;

    constructor(
        private ui: UIService,
        private formBuilder: FormBuilder,
        private countryService: CountryService,
        private operatorService: OperatorService,
        private dialogRef: MatDialogRef<AddEditOperatorComponent>,
        private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.editOperators)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.getCountries();

        this.addEditForm = this.formBuilder.group({
            countryId: ['', [Validators.required]],
            name:      ['', [Validators.required, Validators.maxLength(128)]],
            mnc:       ['', [Validators.required, Validators.maxLength(4), Validators.pattern(config.regexOnlyDigits)]]
        });

        if (this.editMode) {
            this.addEditForm.patchValue(this.operator);
            this.addEditForm.patchValue({countryId: this.operator.country.id});
        }
    }

    get form() { return this.addEditForm.controls; }

    onSubmit() {
        this.submitted = true;
        if (this.addEditForm.invalid) {
            return;
        }

        this.loading = true;
        this.operatorService.addOrEditOperator(this.editMode, this.operator == null ? null : this.operator.id,
            this.form.countryId.value, this.form.name.value, this.form.mnc.value
        ).subscribe(success => {
            this.dialogRef.close(true);
            this.ui.showSuccess(this.editMode ? "editSuccessful" : "addSuccessful");
        }, error => {
            this.loading = false;
            if (error.error.code === 409) {
                this.ui.showError("errors." + error.error.messageKey);
            }
        });
    }

    getCountries(): void {
    this.countryService.getCountries()
        .subscribe(countries => this.countries = countries);
    }
}
