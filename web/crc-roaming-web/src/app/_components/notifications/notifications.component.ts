import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AddNotificationComponent } from '@components/add-notification/add-notification.component';
import { config, modalMinWidth } from '@env/config';
import { SortingPaging } from '@helpers/sorting-paging';
import { Notification } from '@models/notification.model';
import { NotificationService } from '@services/notification.service';
import { UIService } from '@services/ui.service';
import { first } from 'rxjs/operators';

@Component({
    selector: 'app-notifications',
    templateUrl: './notifications.component.html'
})
export class NotificationsComponent implements OnInit {
    notificationList: Notification[];
    pageSizeOptions: number[];
    sortingPaging!: SortingPaging;
    formatTS: string;

    constructor(
        private notificationService: NotificationService,
        private dialog: MatDialog,
        private ui: UIService
    ) {
        this.formatTS = config.formatTS;
        this.pageSizeOptions = config.pageSizeOptions;
        this.sortingPaging = new SortingPaging(config.pageSize);
    }

    ngOnInit() {
        this.notificationList = [];
        this.loadNotifications();
        this.loadTotalElements();
    }

    loadNotifications(): void {
        this.notificationService.fillNotifications(this.sortingPaging).pipe(first()).subscribe(resp => {
            this.notificationList = resp.content;
            this.sortingPaging.fromRow = resp.fromRow;
            this.sortingPaging.toRow = resp.toRow;
        });
    }
    
    loadTotalElements(): void {
        this.notificationService.getTotalElements().subscribe(resp => {
            this.sortingPaging.totalElements = resp;
        });
    }

    pageChanged(page: number) {
        this.sortingPaging.pageNumber = page;
        this.loadNotifications(); 
    }

    openAdd(n?: Notification) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(AddNotificationComponent, modalMinWidth);
            ref.componentInstance.oldNotification = n;
            ref.afterClosed().subscribe(reload => {
                reload && this.loadNotifications();
            });
        }
    }

    send(n: Notification) {
        n.sent = true;
        this.notificationService.sendNotification(n.id).subscribe(() => {
            this.ui.showSuccess("msg.sendNotifSuccessful");
            this.loadNotifications();
        });
    }

}
