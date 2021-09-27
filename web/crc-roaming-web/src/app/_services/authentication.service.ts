import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '@env/environment';
import { PermissionService } from '@services/permission.service';
import jwt_decode from "jwt-decode";
import { Observable } from 'rxjs';
import { first, map } from 'rxjs/operators';

const authUrl           = environment.webApiUrl + '/auth';
const loginUrl          = authUrl + '/o/login';
const logoutUrl         = authUrl + '/logout';
const resetRequestUrl   = authUrl + '/o/request-password-reset';
const checkPassTokenUrl = authUrl + '/o/is-passwd-token-valid?token=';
const resetPasswdUrl    = authUrl + '/o/reset-password';
const changePasswdUrl   = authUrl + '/change-password';
const refreshTokenUrl   = authUrl + '/o/refresh';

const S_TOKEN_ACCESS = 'accessToken';
const S_TOKEN_REFRESH = 'refreshToken';
export const S_PERMS = 'userPermissions';
const S_FULL_NAME = 'fullName';
const S_USER_ROLE = 'role';

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
    constructor(
        private http: HttpClient,
        private router: Router,
        private perms: PermissionService
    ) {}

    get fullName() { return sessionStorage.getItem(S_FULL_NAME); }
    get role() { return sessionStorage.getItem(S_USER_ROLE); }

    public login(email: string, password: string) {
        const req = { 'username': email, 'password': password };

        // TODO: store this in http-only cookie
        return this.http.post<any>(loginUrl, req).pipe(map(resp => {
            let responseObj = JSON.parse(JSON.stringify(resp));
            // store user details and jwt token in sessionStorage to keep user logged in between page refreshes
            AuthenticationService.resetInSession(S_TOKEN_ACCESS,  responseObj.accessToken);
            AuthenticationService.resetInSession(S_TOKEN_REFRESH, responseObj.refreshToken);
            let decoded = AuthenticationService.getDecodedAccessToken();
            AuthenticationService.resetInSession(S_PERMS,         decoded.permissions);
            AuthenticationService.resetInSession(S_FULL_NAME,     decoded.lau_fullName);
            AuthenticationService.resetInSession(S_USER_ROLE,     decoded.lau_role);
        }));
    }

    public logout() {
        this.http.post<any>(logoutUrl, {}).pipe(first()).subscribe();
        sessionStorage.clear();
        this.perms.clear();
        this.router.navigate([""]);
    }

    public static refreshToken(http: HttpClient) {
        const data = {
            'accessToken': sessionStorage.getItem(S_TOKEN_ACCESS),
            'refreshToken': sessionStorage.getItem(S_TOKEN_REFRESH)
        };

        return http.post<any>(refreshTokenUrl, data,).pipe(map(data => {
            let responseObj = JSON.parse(JSON.stringify(data));

            this.resetInSession(S_TOKEN_ACCESS, responseObj.accessToken);
            this.resetInSession(S_TOKEN_REFRESH, responseObj.refreshToken);

            return true;
        }, (err: any) => {
            return false;
        }));
    }

    public static getAccessToken(): string {
        return sessionStorage.getItem(S_TOKEN_ACCESS);
    }

    public static getDecodedAccessToken(): any {
        try {
            return jwt_decode(this.getAccessToken());
        }
        catch (Error) {
            return null;
        }
    }

    public requestPasswordReset(email: any) {
        let body = { "email": email }
        return this.http.post<any>(resetRequestUrl, body);
    }

    public checkResetToken(token: string): Observable<boolean> {
        return this.http.get<boolean>(checkPassTokenUrl + token);
    }

    public resetPassword(token: string, password: string): Observable<any> {
        let body = {
            "token": token,
            "password": password
        };
        
        return this.http.post<any>(resetPasswdUrl, body);
    }

    public changePassword(oldPassword: string, newPassword: string): Observable<any> {
        let body = {
            'oldPassword': oldPassword,
            'newPassword': newPassword
        };
        
        return this.http.post<any>(changePasswdUrl, body);
    }

    public isLoggedIn() {
        if (sessionStorage.getItem(S_TOKEN_ACCESS)) {
            return true;
        }

        return false;
    }

    private static resetInSession(key: string, val: any) {
        sessionStorage.removeItem(key);
        sessionStorage.setItem(key, val);
    }

}