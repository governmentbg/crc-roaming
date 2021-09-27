import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { UIService } from '@services/ui.service';
import { first } from 'rxjs/operators';
import { config } from '@env/config';
import { PublicPortalService } from '@services/public-portal.service';

@Component({
    selector: 'app-delete-client-profile',
    templateUrl: './delete-client-profile.component.html'
})
export class DeleteClientProfileComponent implements OnInit {
    delForm: FormGroup;
    submitted = false;
    loading = false;
    siteKey = config.siteKey;

    constructor(
        private formBuilder: FormBuilder,
        public ui: UIService,
        private publicService: PublicPortalService,
        public dialogRef: MatDialogRef<DeleteClientProfileComponent>,
    ) { 
    }

    ngOnInit() {
        this.delForm = this.formBuilder.group({
            email: ['', [Validators.required, Validators.pattern(config.regexValidEmail)]],
            recaptcha: ['', [Validators.required]]
        });
    }

    get form() { return this.delForm.controls; }

    onSubmit() {
        this.loading = true;
        this.submitted = true;

        if (this.delForm.invalid) {
            this.loading = false;
            return;
        }

        this.publicService.deleteClientProfile(this.form.email.value).pipe(first()).subscribe(() => {
            this.dialogRef.close();
            this.ui.showSuccess("delProfile.requestSent");
        }, () => {
            this.loading = false;
        });
    }

}
