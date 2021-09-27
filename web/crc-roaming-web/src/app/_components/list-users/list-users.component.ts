import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AddEditUserComponent } from '@components/add-edit-user/add-edit-user.component';
import { config, modalMinWidth } from '@env/config';
import { SortingPaging } from '@helpers/sorting-paging';
import { User } from '@models/user.model';
import { PermissionService } from '@services/permission.service';
import { UIService } from '@services/ui.service';
import { UserService } from '@services/user.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-list-users',
  templateUrl: './list-users.component.html'
})
export class ListUsersComponent implements OnInit {
    editPerm: boolean;
    users: User[];
    pageSizeOptions: number[];
    sortingPaging!: SortingPaging;
    formatTS: string;

    constructor(
        private userService: UserService,
        private dialog: MatDialog,
        private perms: PermissionService,
        private ui: UIService
    ) { 
        this.formatTS = config.formatTS;
        this.editPerm = this.perms.hasAccess(this.perms.all.editUsers);
        if (!this.perms.hasAccess(this.perms.all.viewUsers)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
        this.users = [];
        this.pageSizeOptions = config.pageSizeOptions;
        this.sortingPaging = new SortingPaging(config.pageSize);
    }

    ngOnInit() {
        this.loadUsers();
        this.loadTotalElements();
    }

    loadUsers(): void {
        this.userService.getUsers(this.sortingPaging).pipe(first()).subscribe(resp => {
            this.users = resp.content;
            this.sortingPaging.fromRow = resp.fromRow;
            this.sortingPaging.toRow = resp.toRow;
        });
    }

    loadTotalElements(): void {
        this.userService.getTotalElements().subscribe(resp => {
            this.sortingPaging.totalElements = resp;
        });
    }

    pageChanged(page: number) {
        this.sortingPaging.pageNumber = page;
        this.loadUsers(); 
    }

    add() {
        this.openAddEdit(false, null);
    }

    edit(user: User) {
        this.openAddEdit(true, user);
    }

    private openAddEdit(editMode: boolean, user: User) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(AddEditUserComponent, modalMinWidth);
            ref.componentInstance.editMode = editMode;
            ref.componentInstance.user = user;
            ref.afterClosed().subscribe(reload => {
                reload && this.loadUsers();
            });
        }
    }
}