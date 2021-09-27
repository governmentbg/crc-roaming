import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Role } from '../_models/role.model';
import { Permission } from '../_models/permission.model';
import { environment } from 'src/environments/environment';

const rolesUrl          = environment.webApiUrl + '/roles';
const allRolesUrl       = rolesUrl + '/all';
const allPermissionsUrl = rolesUrl + '/permissions/get-all';
const addRoleUrl        = rolesUrl + '/add';
const getRoleUrl        = rolesUrl + '/get/';
const updateRoleUrl     = rolesUrl + '/edit/';
const deleteRoleUrl     = rolesUrl + '/delete/';

@Injectable({ providedIn: 'root' })
export class RoleService {

    constructor(
        private http: HttpClient
    ) { }

    public getRoles(onlyValid: boolean): Observable<Role[]> {
        let requestParams = new HttpParams();
        requestParams = requestParams.set("onlyValid", onlyValid.toString());
                        
        return this.http.get<any>(allRolesUrl, { params: requestParams});
    }

    public getRoleById(id: number) {
        return this.http.get<any>(getRoleUrl + id);
    }

    public getPermissions(): Observable<Permission[]> {
        return this.http.get<Permission[]>(allPermissionsUrl);
    }

    public addOrEditRole(edit: boolean, id: number, name: string, description: string, permissionIds: Array<any>, toSingleUser: boolean, enabled: boolean) {
        const data = { 'name': name, 'description': description, 'permissionIds': permissionIds, 'toSingleUser': toSingleUser, 'enabled': enabled};
        if (edit) {
            return this.http.post<any>(updateRoleUrl + id, data);
        } else {
            return this.http.post<any>(addRoleUrl, data);
        }
    }

    public deleteRole(id: number) {
        return this.http.delete<any>(deleteRoleUrl + id);
    }

}
