import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
    selector: 'app-confirm-dialog',
    templateUrl: './confirm-dialog.component.html'
})
export class ConfirmDialogComponent {
    titleKey: string;
    titleParams: any;
    textKey: string;
    textParams: any;

    constructor(@Inject(MAT_DIALOG_DATA) public data: {titleKey: string, titleParams: any, textKey: string, textParams: any}) {
        this.titleKey    = data.titleKey;
        this.titleParams = data.titleParams;
        this.textKey     = data.textKey;
        this.textParams  = data.textParams;
    }

}
