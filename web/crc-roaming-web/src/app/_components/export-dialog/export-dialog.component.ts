import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
    selector: 'app-export-dialog',
    templateUrl: './export-dialog.component.html'
})
export class ExportDialogComponent {
    titleKey: string;
    titleParams: any;
    textKey: string;

    constructor(@Inject(MAT_DIALOG_DATA) public data: {titleKey: string, titleParams: any, textKey: string}) {
        this.titleKey    = data.titleKey;
        this.titleParams = data.titleParams;
        this.textKey     = data.textKey;
    }

}
