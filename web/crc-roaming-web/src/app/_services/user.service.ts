import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { User } from '../_models/user.model';
import { Role } from '../_models/role.model';
import { environment } from 'src/environments/environment';
import { SortingPaging } from '@helpers/sorting-paging';
import { Page } from '@interfaces/page';

const usersUrl = environment.webApiUrl + '/users';
const rolesUrl = environment.webApiUrl + '/roles';
const allUsersUrl = usersUrl + '/get-all';
const countAllUsersUrl = usersUrl + '/count-all';
const addUserUrl = usersUrl + '/add';
const getUserUrl = usersUrl + '/get/';
const updateUserUrl = usersUrl + '/edit/';

@Injectable({ providedIn: 'root' })
export class UserService {

    constructor(
        private http: HttpClient
    ) { }

    public getUsers(sortingPaging: SortingPaging): Observable<Page<User>> {
        let requestParams = new HttpParams();
        requestParams = requestParams.set("page", sortingPaging.getPageNumber());
        requestParams = requestParams.set("size", sortingPaging.getPageSize());
        
        return this.http.get<any>(allUsersUrl, {
          params: requestParams});
    }

    public getTotalElements(): Observable<number>{
        return this.http.get<any>(countAllUsersUrl);
    }

    public getUserById(id: number) {
        return this.http.get<any>(getUserUrl + id);
    }

    public addOrEditUser(edit: boolean, id: number, fullName: string, email: string, enabled: boolean, roleId: number) {
        const data = { 'fullName': fullName, 'email': email, 'enabled': enabled, 'roleId': roleId };
        if (edit) {
            return this.http.post<any>(updateUserUrl + id, data);
        } else {
            return this.http.post<any>(addUserUrl, data);
        }
    }

}