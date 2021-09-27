import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Notification } from '@models/notification.model';
import { NotificationService } from '@services/notification.service';
import { UIService } from '@services/ui.service';

@Component({
    selector: 'app-add-notification',
    templateUrl: './add-notification.component.html',
})
export class AddNotificationComponent implements OnInit {
    loading = false;
    submitted = false;
    addEditForm: FormGroup;
    oldNotification: Notification;

    constructor(
        private ui: UIService,
        private formBuilder: FormBuilder,
        private notificationService: NotificationService,
        private dialogRef: MatDialogRef<AddNotificationComponent>
    ) { }

    ngOnInit() {
        this.addEditForm = this.formBuilder.group({
            subject: ['', [Validators.required, Validators.maxLength(32)]],
            body:    ['', [Validators.required, Validators.maxLength(512)]],
        });

        this.oldNotification && this.addEditForm.patchValue(this.oldNotification);
    }

    get form() { return this.addEditForm.controls; }

    onSubmit() {
        this.submitted = true;
        if (this.addEditForm.invalid) {
            return;
        }

        this.loading = true;
        this.notificationService.addNotification(this.form.subject.value, this.form.body.value).subscribe(() => {
            this.dialogRef.close(true);
            this.ui.showSuccess("msg.addNotifSuccessful");
        },() => {
            this.loading = false;
            this.ui.showError("msg.errorServer");
        });
    }

}
