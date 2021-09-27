import { HttpClient, HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { BehaviorSubject, EMPTY, Observable, throwError } from "rxjs";
import { catchError, switchMap } from "rxjs/operators";
import { AuthenticationService } from "../_services/authentication.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    private isRefreshing = false;
    private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

    constructor(
        private http: HttpClient,
        private router: Router
    ) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (AuthenticationService.getAccessToken()) {
            request = this.addToken(request, AuthenticationService.getAccessToken());
        }

        return next.handle(request).pipe(catchError(error => {
            if (request.url.endsWith("/login")) {
                return next.handle(request);
            }

            if (error instanceof HttpErrorResponse) {
                if (error.status === 401) {
                    return this.handle401Error(request, next);
                } else if (error.status === 0) {
                    sessionStorage.clear();
                    this.router.navigate(['']);
                    return EMPTY;
                }
            }

            return throwError(error);
        }));
    }

    private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
        if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.refreshTokenSubject.next(null);
            return AuthenticationService.refreshToken(this.http).pipe(switchMap((token: any) => {
                this.isRefreshing = false;
                if (token) {
                    this.refreshTokenSubject.next(AuthenticationService.getAccessToken());
                    return next.handle(this.addToken(request, AuthenticationService.getAccessToken()));
                }
            }));

        } else {
            sessionStorage.clear();
            this.router.navigate(['']);
            return EMPTY;
        }
    }

    private addToken(request: HttpRequest<any>, token: string) {
        return request.clone({
            setHeaders: {
                'Authorization': `Bearer ${token}`
            }
        });
    }

}