import { Component, OnInit } from '@angular/core';
import { Validators } from '@angular/forms';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { config } from '@env/config';
import { AuthenticationService } from '@services/authentication.service';
import { UIService } from '@services/ui.service';

@Component({
    selector: 'app-forgotten-password',
    templateUrl: './forgotten-password.component.html'
})
export class ForgottenPasswordComponent implements OnInit {
    forgottenPassForm: FormGroup;
    loading = false;
    submitted = false;
    siteKey = config.siteKey;

    constructor(
        public ui: UIService,
        private formBuilder: FormBuilder,
        private auth: AuthenticationService,
        public dialogRef: MatDialogRef<any>
    ) { 
    }

    ngOnInit() {
        this.forgottenPassForm = this.formBuilder.group({
            email: ['', [Validators.required, Validators.pattern(config.regexValidEmail)]],
            recaptcha: ['', Validators.required]
        });
    }

    get form() { return this.forgottenPassForm.controls; }

    onSubmit() {
        this.submitted = true;
        this.loading = true;
        if (this.forgottenPassForm.invalid) {
            this.loading = false;
            return;
        }
        this.auth.requestPasswordReset(this.form.email.value).subscribe(
            suc => {
                this.ui.showSuccess('auth.forgPassEmailSent');
                this.dialogRef.close();
            },
            err => {
                this.loading = false;
                if (err.status === 400) {
                    this.ui.showErrorNoTranslate(err.error.errorMessage);
                } else {
                    this.ui.showError('msg.errorServer');
                }
            }
        );
    }

}
