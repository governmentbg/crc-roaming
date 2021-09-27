import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
    selector: 'app-message-dialog',
    templateUrl: './message-dialog.component.html'
})
export class MessageDialog {
    textKey: string;
    textParams: any;

    constructor(@Inject(MAT_DIALOG_DATA) public data: {titleKey: string, titleParams: any, textKey: string, textParams: any}) {
        this.textKey     = data.textKey;
        this.textParams  = data.textParams;
    }

}
