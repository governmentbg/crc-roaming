import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Country } from '@models/country.model';
import { CountryService } from '@services/country.service';
import { PermissionService } from '@services/permission.service';
import { UIService } from '@services/ui.service';

@Component({
    selector: 'app-add-edit-country',
    templateUrl: './add-edit-country.component.html'
})
export class AddEditCountryComponent implements OnInit {
    editMode: boolean;
    country: Country;
    loading = false;
    submitted = false;
    addEditForm: FormGroup;

    constructor(
        private ui: UIService,
        private formBuilder: FormBuilder,
        private countryService: CountryService,
        private dialogRef: MatDialogRef<AddEditCountryComponent>,
        private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.editCountries)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.addEditForm = this.formBuilder.group({
            nameInt:    ['', [Validators.required, Validators.maxLength(128)]],
            nameBg:     ['', [Validators.required, Validators.maxLength(128)]],
            mcc:        ['', [Validators.required, Validators.maxLength(4), Validators.pattern('^[0-9]*$')]],
            phoneCode:  ['', [Validators.required, Validators.maxLength(4), Validators.pattern('^(\\\+?\\\d+)$')]],
            euMember:   ['', [Validators.required]]
        });

        if (this.editMode) {
            this.addEditForm.patchValue(this.country);
        }
    }

    get form() { return this.addEditForm.controls; }

    onSubmit() {
        this.submitted = true;
        if (this.addEditForm.invalid) {
            return;
        }

        this.loading = true;
        this.countryService.addOrEditCountry(this.editMode, this.country == null ? null : this.country.id,
            this.form.nameInt.value, this.form.nameBg.value,
            this.form.mcc.value, this.form.phoneCode.value,
            this.form.euMember.value
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

}
