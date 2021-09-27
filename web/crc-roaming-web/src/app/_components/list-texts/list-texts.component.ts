import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EditTextComponent } from '@components/edit-text/edit-text.component';
import { modalMinWidth } from '@env/config';
import { Translation } from '@models/translation';
import { PermissionService } from '@services/permission.service';
import { SystemService } from '@services/system.service';
import { UIService } from '@services/ui.service';

@Component({
    selector: 'app-list-texts',
    templateUrl: './list-texts.component.html'
})
export class ListTextsComponent implements OnInit {
    texts: Translation[];
    editPerm: boolean

    constructor(
        private systemService: SystemService,
        private dialog: MatDialog,
        private perms: PermissionService,
        private ui: UIService
    ) {
        this.editPerm = this.perms.hasAccess(this.perms.all.editTexts)
        if (!this.perms.hasAccess(this.perms.all.viewTexts)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.reloadTexts();
    }

    reloadTexts() {
        this.systemService.getSystemTexts().subscribe(r => {
            this.texts = r;
        });
    }

    edit(text: Translation) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(EditTextComponent, modalMinWidth);
            ref.componentInstance.text = text;
            ref.afterClosed().subscribe(reload => {
                reload && this.reloadTexts();
            });
        }
    }

}
