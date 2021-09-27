import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Translation } from '@models/translation';
import { PermissionService } from '@services/permission.service';
import { SystemService } from '@services/system.service';
import { UIService } from '@services/ui.service';

@Component({
    selector: 'app-edit-text',
    templateUrl: './edit-text.component.html'
})
export class EditTextComponent implements OnInit {
    text: Translation
    loading = false;
    submitted = false;
    editForm: FormGroup;

    constructor(
        private ui: UIService,
        private formBuilder: FormBuilder,
        private systemService: SystemService,
        private dialogRef: MatDialogRef<EditTextComponent>,
        private perms: PermissionService,
    ) {
        if (!this.perms.hasAccess(this.perms.all.editTexts)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
    }

    ngOnInit() {
        this.editForm = this.formBuilder.group({
            value:    ['', [Validators.required]],
        });

        this.editForm.patchValue(this.text);
    }

    get form() { return this.editForm.controls; }

    onSubmit() {
        this.submitted = true;
        if (this.editForm.invalid) {
            return;
        }

        this.loading = true;
        this.systemService.updateSystemText(this.text.key, this.text.language, this.form.value.value)
        .subscribe(() => {
            this.dialogRef.close(true);
            this.ui.showSuccess("editSuccessful");
        });
    }

}
