import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { first } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { SortingPaging } from '@helpers/sorting-paging';
import { Page } from '@interfaces/page';
import { Notification } from '@models/notification.model';

const notificationsUrl         = environment.webApiUrl + '/notifications';
const allNotificationsUrl      = notificationsUrl + '/all';
const countAllNotificationsUrl = notificationsUrl + '/count-all';
const addNotificationUrl       = notificationsUrl + '/add';
const sendNotificationUrl      = notificationsUrl + '/send/';

@Injectable({ providedIn: 'root' })
export class NotificationService {

    constructor(
        private http: HttpClient
    ) { }

    public fillNotifications(sortingPaging: SortingPaging): Observable<Page<Notification>> {
        let requestParams = new HttpParams();
        requestParams = requestParams.set("page", sortingPaging.getPageNumber());
        requestParams = requestParams.set("size", sortingPaging.getPageSize());
        
        return this.http.get<any>(allNotificationsUrl, {params: requestParams});
    }

    public getTotalElements(): Observable<number>{
        return this.http.get<any>(countAllNotificationsUrl);
    }

    public addNotification(subject: string, body: string) {
        const data = {"subject": subject, "body": body};
        return this.http.post<any>(addNotificationUrl, data);
    }

    public sendNotification(id: number) {
        return this.http.post<any>(sendNotificationUrl + id, null);
    }

}