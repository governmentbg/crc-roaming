import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators, AbstractControl, ValidatorFn, ValidationErrors } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { UIService } from '@services/ui.service';
import { AuthenticationService } from '@services/authentication.service';
import { config } from '@env/config';
import { first } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
    selector: 'app-change-password',
    templateUrl: './change-password.component.html'
})
export class ChangePassword {
    changePassForm: FormGroup;
    resetPasswordToken: string;
    resetPassword = false;
    submitted = false;
    loading = false;
    error = '';

    constructor(
        private ui: UIService,
        private formBuilder: FormBuilder,
        private auth: AuthenticationService,
        private router: Router,
        public dialogRef: MatDialogRef<ChangePassword>
    ) { }

    ngOnInit() {
        this.changePassForm = this.formBuilder.group({
            currentPasswd: (this.resetPassword ? undefined : ['', Validators.required]),
            newPasswd: ['', [Validators.required, Validators.pattern(config.regexValidPass)]],
            confirmPasswd: ['', []]
        });
        this.changePassForm.setValidators(this.checkPasswords())
    }

    get form() { return this.changePassForm.controls; }

    onSubmit() {
        this.error = null;
        this.submitted = true;
        if (this.changePassForm.invalid) {
            return;
        }
        this.loading = true;
        if (this.resetPassword) {
            this.auth.resetPassword(this.resetPasswordToken, this.form.newPasswd.value).pipe(first()).subscribe(success => {
                this.dialogRef.close();
                this.ui.showSuccess("auth.passChangedOK");
                this.router.navigate(['']);
            }, error => {
                this.loading = false;
                if (error.status === 410) {
                    this.ui.showError("auth.expiredPassResetLink");
                } else if (error.status === 400) {
                    this.ui.showErrorNoTranslate(error.error.errorMessage);
                }
            });
        } else {
            this.auth.changePassword(this.form.currentPasswd.value, this.form.newPasswd.value).pipe(first()).subscribe(
                success => {
                    this.dialogRef.close();
                    this.ui.showSuccess("auth.passChangedOK");
                }, error => {
                    this.loading = false;
                    if (error.status === 409) {
                        this.ui.showError("auth.wrongOldPass");
                    }
                });
        }
    }

    public checkPasswords() : ValidatorFn {
        return (group: FormGroup): ValidationErrors => {
            const passwd = group.controls['newPasswd'];
            const passwdConfirm = group.controls['confirmPasswd'];

            if (passwd.value !== passwdConfirm.value) {
                passwdConfirm.setErrors({notEq: true});
            }
            return;
        }
    }

}