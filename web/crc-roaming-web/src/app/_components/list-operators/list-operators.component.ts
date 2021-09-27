import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AddEditOperatorComponent } from '@components/add-edit-operator/add-edit-operator.component';
import { ConfirmDialogComponent } from '@components/confirm-dialog/confirm-dialog.component';
import { modalMinWidth, config } from '@env/config';
import { NgbdSortableHeader } from '@helpers/ngbd-sortable-header';
import { SortEvent } from '@interfaces/sort-event';
import { Operator } from '@models/operator.model';
import { OperatorService } from '@services/operator.service';
import { PermissionService } from '@services/permission.service';
import { UIService } from '@services/ui.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-list-operators',
  templateUrl: './list-operators.component.html'
})
export class ListOperatorsComponent implements OnInit {
    @ViewChildren(NgbdSortableHeader) headers: QueryList<NgbdSortableHeader>;
    operators: Operator[];
    formatTS: string;
    editPerm: boolean;

    constructor(
        private operatorService: OperatorService,
        private dialog: MatDialog,
        private ui: UIService,
        private perms: PermissionService
    ) {
        this.formatTS = config.formatTS;
        this.editPerm = this.perms.hasAccess(this.perms.all.editOperators);
        if (!this.perms.hasAccess(this.perms.all.viewOperators)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.loadOperators();
    }

    loadOperators(sortBy?: string, direction?: string): void {
        this.operatorService.getOperators(sortBy, direction).pipe(first()).subscribe(data => {
            this.operators = data;
        });
    }

    add() {
        this.openAddEdit(false, null);
    }

    edit(operator: Operator) {
        this.openAddEdit(true, operator);
    }

    private openAddEdit(editMode: boolean, operator: Operator) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(AddEditOperatorComponent, modalMinWidth);
            ref.componentInstance.editMode = editMode;
            ref.componentInstance.operator = operator;
            ref.afterClosed().subscribe(reload => {
                reload && this.loadOperators();
            });
        }
    }

    delete(operator: Operator) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(ConfirmDialogComponent, {data: {
                titleKey: "operators.confirmDeleteTitle",
                textKey: "operators.confirmDeleteText",
                textParams: {name: operator.name}}});
            ref.afterClosed().subscribe(confirmed => {
                confirmed && this.operatorService.deleteOperator(operator.id).subscribe(data => {
                    this.loadOperators();
                    this.ui.showSuccess("deleteSuccessful");
                })
            });
        }
    }

    onSort({ column, direction }: SortEvent) {
        // clear other sort
        this.headers.forEach(header => {
            if (header.sortable !== column) {
                header.direction = '';
            }
        });
        this.loadOperators(column, direction);
    }

}