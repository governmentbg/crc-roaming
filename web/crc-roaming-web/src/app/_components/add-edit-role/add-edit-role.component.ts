import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Permission } from '@models/permission.model';
import { Role } from '@models/role.model';
import { PermissionService } from '@services/permission.service';
import { RoleService } from '@services/role.service';
import { UIService } from '@services/ui.service';

@Component({
  selector: 'app-add-edit-role',
  templateUrl: './add-edit-role.component.html'
})
export class AddEditRoleComponent implements OnInit {
    role: Role;
    allPermissions: Permission[];
    permissionIds: Array<any> = [];
    editMode: boolean;
    submitted = false;
    loading = false;
    addEditForm: FormGroup;
    emptyCheckPermissions: boolean = false;

    constructor(
        private ui: UIService,
        private formBuilder: FormBuilder,
        private roleService: RoleService,
        private dialogRef: MatDialogRef<AddEditRoleComponent>,
        private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.editRoles)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.loadPermissions();

        this.role && this.role.permissions.forEach(r => {
            this.permissionIds.push(r.id);
        });

        this.addEditForm = this.formBuilder.group({
            name:         ['', [Validators.required, Validators.maxLength(128)]],
            description:  ['', [Validators.required, Validators.maxLength(128)]],
            toSingleUser: ['', [Validators.required]],
            enabled:      ['', [Validators.required]]
        });

        if (this.editMode) {
            this.addEditForm.patchValue(this.role);
        }
    }

    get form() { return this.addEditForm.controls; }

    onSubmit() {
        this.submitted = true;
        this.emptyCheckPermissions = this.permissionIds.length > 0 ? false : true;
        if (this.addEditForm.invalid || this.emptyCheckPermissions) {
            return;
        }

        this.loading = true;
        this.roleService.addOrEditRole(this.editMode, this.role == null ? null : this.role.id,
            this.form.name.value, this.form.description.value, this.permissionIds, this.form.toSingleUser.value, this.form.enabled.value
        ).subscribe(success => {
            this.dialogRef.close("OK");
            this.ui.showSuccess(this.editMode ? "editSuccessful" : "addSuccessful");
        }, error => {
            this.loading = false;
            if (error.error.code === 409) {
                this.ui.showError("errors." + error.error.messageKey);
            }
        });
    }

    loadPermissions(): void {
    this.roleService.getPermissions()
        .subscribe(permissions => this.allPermissions = permissions);
    }

    onChange(id:number, isChecked:boolean): void {
        if (isChecked) {
          this.permissionIds.push(id);
        } else {
          let index = this.permissionIds.indexOf(id);
          this.permissionIds.splice(index,1);
        }
    }

    checkId(id:number): boolean {
        return this.role && this.role.permissions.some(p => p.id === id);
    }

    isDisabled(item: Permission): boolean {
        console.log(this.permissionIds);
        if (item.unavailableUnless == null) {
            return false;
        }

        return this.permissionIds.indexOf(item.unavailableUnless) == -1;
    }

}
