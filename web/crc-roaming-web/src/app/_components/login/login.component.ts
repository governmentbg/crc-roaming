import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ForgottenPasswordComponent } from '@components/forgotten-password/forgotten-password.component';
import { AuthenticationService } from '@services/authentication.service';
import { UIService } from '@services/ui.service';
import { first } from 'rxjs/operators';
import { config } from '@env/config';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-login-modal',
    templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
    loginForm: FormGroup;
    submitted = false;
    loading = false;
    siteKey = config.siteKey;

    constructor(
        private formBuilder: FormBuilder,
        private auth: AuthenticationService,
        private dialog: MatDialog,
        public ui: UIService,
        public dialogRef: MatDialogRef<LoginComponent>,
        public router: Router
    ) {
    }

    ngOnInit() {
        this.loginForm = this.formBuilder.group({
            email: ['', Validators.required],
            password: ['', Validators.required],
            recaptcha: ['', Validators.required]
        });
    }

    get form() { return this.loginForm.controls; }

    onSubmit() {
        this.submitted = true;

        if (this.loginForm.invalid) {
            return;
        }

        this.loading = true;
        this.auth.login(this.form.email.value, this.form.password.value).pipe(first()).subscribe(() => {
            this.ui.useLanguage('bg');
            this.dialogRef.close("OK");
            this.router.navigate(['']);
        }, error => {
            this.loading = false;
            if (error.status === 401) {
                this.ui.showError(error.error.messageKey);
            } else if (error.status === 400) {
                this.ui.showErrorNoTranslate(error.error.errorMessage);
            } else {
                this.ui.showError('msg.errorOnLogin');
            }
        });
    }

    public openForgottenPass() {
        this.dialog.open(ForgottenPasswordComponent);
    }
}
