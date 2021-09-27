import { Component, HostListener, Injectable, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { UIService } from '@app/_services/ui.service';
import { ChangePassword } from '@components/change-password/change-password.component';
import { DeleteClientProfileComponent } from '@components/delete-client-profile/delete-client-profile.component';
import { LoginComponent } from '@components/login/login.component';
import { MessageDialog } from '@components/message-dialog/message-dialog.component';
import { config, modalMinWidth } from '@env/config';
import { base64Fonts } from '@env/fonts';
import { AuthenticationService } from '@services/authentication.service';
import { PermissionService } from '@services/permission.service';
import { filter, first } from 'rxjs/operators';
import { jsPDF } from "jspdf"
import { AboutComponent } from '@components/about/about.component';
import { MobileApps } from '@components/mobile-apps/mobile-apps.component';
import { Chart } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';

export let browserRefresh = false;

@Component({
    selector: 'app',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
@Injectable({ providedIn: 'root' })
export class AppComponent implements OnInit {
    logoutMsgTimer: any;
    logoutTimer: any;
    loggedIn: boolean;
    appVersion: string;

    constructor(
        public auth: AuthenticationService,
        public ui: UIService,
        public perms: PermissionService,
        private dialog: MatDialog,
        private activatedRoute: ActivatedRoute,
        private router: Router,
    ) {
        this.loggedIn = this.auth.isLoggedIn();
        this.appVersion = config.appVersion;
        this.initLogoutTimer();
    }

    ngOnInit() {
        this.router.events.pipe(
            filter((e): e is NavigationStart => e instanceof NavigationStart)
        ).subscribe((e: NavigationStart) => {
            this.loggedIn = this.auth.isLoggedIn();
        });

        // Show messages on landing page
        this.activatedRoute.queryParams.subscribe(params => {
            let action = params['action'];
            if (action === config.messageAction) {
                this.dialog.open(MessageDialog, {data: {textKey: 'msg.' + params['key']}});
            } else if (action === config.resetPasswdAction) {
                let token = params['token'];
                this.auth.checkResetToken(token).pipe(first()).subscribe(isValid => {
                    if (isValid) {
                        let dialogRef = this.dialog.open(ChangePassword, modalMinWidth);
                        dialogRef.componentInstance.resetPasswordToken = token;
                        dialogRef.componentInstance.resetPassword = true;
                    } else {
                        this.ui.showError("auth.expiredPassResetLink");
                        this.router.navigate(['']);
                    }
                });
            }
        });

        // Load custom fonts
        jsPDF.API.events.push(['addFonts', function () {
            this.addFileToVFS('OpenSans-Regular-normal.ttf', base64Fonts.openSansRegular);
            this.addFont('OpenSans-Regular-normal.ttf', 'OpenSans', 'normal');
        }]);

        // Load charts global settings
        Chart.plugins.register(ChartDataLabels);
        (Chart as any).Legend.prototype.afterFit = function() {
            this.height = this.height + 20;
        };
      }

    @HostListener('document:click') onMouseClick() {
        clearTimeout(this.logoutMsgTimer);
        clearTimeout(this.logoutTimer);
        this.logoutMsgTimer = null;
        this.logoutTimer = null;
        this.initLogoutTimer();
    }

    public initLogoutTimer() {
        if (this.loggedIn && this.logoutMsgTimer == null) {
            let _this = this;
            this.logoutMsgTimer = setTimeout(function () {
                _this.ui.showWarningNoTimeout("msg.noActivity10min");
                _this.logoutTimer = setTimeout(() => {
                    _this.auth.logout();
                    _this.loggedIn = false;
                    _this.ui.clearAllMessages();
                    _this.ui.showErrorNoTimeout("msg.noActivityLogout");
                }, config.timeoutLogout);
            }, config.timeoutLogoutMsg);
        }
    }

    public openLogin() {
        if (this.dialog.openDialogs.length == 0) {
            this.dialog.open(LoginComponent, modalMinWidth)
            .afterClosed().subscribe(result => {
                if (result === "OK") {
                    this.loggedIn = true;
                    this.initLogoutTimer();
                }
            })
        }
    }

    public openDialog(type: string) {
        if (this.dialog.openDialogs.length > 0) {
            return;
        }

        switch (type) {
            case 'mobileApps': {
                this.dialog.open(MobileApps, modalMinWidth);
                break;
            }

            case 'changePass': {
                this.dialog.open(ChangePassword, modalMinWidth);
                break;
            }

            case 'delProfile': {
                this.dialog.open(DeleteClientProfileComponent, modalMinWidth);
                break;
            }

            case 'about': {
                this.dialog.open(AboutComponent, modalMinWidth);
                break;
            }

            case 'login': {
                this.dialog.open(LoginComponent, modalMinWidth).afterClosed().subscribe(result => {
                    if (result === "OK") {
                        this.loggedIn = true;
                        this.initLogoutTimer();
                    }
                });
                break;
            }
        }
    }

    public logout() {
        this.auth.logout();
        this.loggedIn = false;
    }

}
