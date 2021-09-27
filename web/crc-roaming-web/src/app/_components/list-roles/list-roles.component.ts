import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AddEditRoleComponent } from '@components/add-edit-role/add-edit-role.component';
import { ConfirmDialogComponent } from '@components/confirm-dialog/confirm-dialog.component';
import { config, modalMinWidth } from '@env/config';
import { Role } from '@models/role.model';
import { PermissionService } from '@services/permission.service';
import { RoleService } from '@services/role.service';
import { UIService } from '@services/ui.service';

@Component({
  selector: 'app-list-roles',
  templateUrl: './list-roles.component.html'
})
export class ListRolesComponent implements OnInit {
    roles: Role[];
    formatTS: string;
    editPerm: boolean;

    constructor(
        private roleService: RoleService,
        private dialog: MatDialog,
        private ui: UIService,
        private perms: PermissionService
    ) {
        this.formatTS = config.formatTS;
        this.editPerm = this.perms.hasAccess(this.perms.all.editRoles);
        if (!this.perms.hasAccess(this.perms.all.viewRoles)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.loadRoles();
    }

    loadRoles(): void {
        this.roleService.getRoles(false)
            .subscribe(roles => this.roles = roles);
      }

    add() {
        this.openAddEdit(false, null);
    }

    edit(role: Role) {
        this.openAddEdit(true, role);
    }

    private openAddEdit(editMode: boolean, role: Role) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(AddEditRoleComponent, modalMinWidth);
            ref.componentInstance.editMode = editMode;
            ref.componentInstance.role = role;
            ref.afterClosed().subscribe(d => {
                if (d === "OK") {
                    this.loadRoles();
                }
            });
        }
    }

    delete(role: Role) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(ConfirmDialogComponent, {data: {
                titleKey: "roles.confirmDeleteTitle",
                textKey: "roles.confirmDeleteText",
                textParams: {name: role.name}}});
            ref.afterClosed().subscribe(confirmed => {
                confirmed && this.roleService.deleteRole(role.id).subscribe(success => {
                    ref.close("OK");
                    this.loadRoles();
                    this.ui.showSuccess("deleteSuccessful");
                }, error => {
                    if (error.error.code === 409) {
                        this.ui.showError("errors." + error.error.messageKey);
                    }
                });

            });
        }
    }

}
