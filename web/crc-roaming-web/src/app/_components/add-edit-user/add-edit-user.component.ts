import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { User } from '@models/user.model';
import { Role } from '@models/role.model';
import { config } from '@env/config';
import { UserService } from '@services/user.service';
import { UIService } from '@services/ui.service';
import { RoleService } from '@services/role.service';
import { PermissionService } from '@services/permission.service';

@Component({
  selector: 'app-add-edit-user',
  templateUrl: './add-edit-user.component.html'
})
export class AddEditUserComponent implements OnInit {
    editMode: boolean;
    user: User;
    roles: Role[];
    loading = false;
    submitted = false;
    addEditForm: FormGroup;

    constructor(
        private ui: UIService,
        private formBuilder: FormBuilder,
        private userService: UserService,
        private roleService: RoleService,
        private dialogRef: MatDialogRef<AddEditUserComponent>,
        private perms: PermissionService,
    ) {
        if (!this.perms.hasAccess(this.perms.all.editUsers)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.getRoles();

        this.addEditForm = this.formBuilder.group({
            fullName: ['', [Validators.required, Validators.maxLength(128)]],
            email:    ['', [Validators.required, Validators.maxLength(64), Validators.pattern(config.regexValidEmail)]],
            enabled:  ['', [Validators.required]],
            roleId:   ['', [Validators.required]]
        });

        if (this.editMode) {
            this.addEditForm.patchValue(this.user);
            this.addEditForm.patchValue({roleId: this.user.role.id});
        }
    }

    get form() { return this.addEditForm.controls; }

    onSubmit() {
        this.submitted = true;
        if (this.addEditForm.invalid) {
            return;
        }

        this.loading = true;
        this.userService.addOrEditUser(this.editMode, this.user == null ? null : this.user.id, 
          this.form.fullName.value, this.form.email.value, this.form.enabled.value, this.form.roleId.value,
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

    getRoles(): void {
      this.roleService.getRoles(true)
          .subscribe(roles => this.roles = roles);
    }

}
