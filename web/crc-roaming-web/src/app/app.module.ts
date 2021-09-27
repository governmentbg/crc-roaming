import { HttpClient, HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { APP_BASE_HREF, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DEFAULT_OPTIONS } from '@angular/material/dialog';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ToastrModule } from 'ngx-toastr';
import { AppComponent } from '@app/app.component';
import { appRoutingModule } from '@app/app.routing';
import { ChangePassword } from '@components/change-password/change-password.component';
import { ForgottenPasswordComponent } from '@components/forgotten-password/forgotten-password.component';
import { LoginComponent } from '@components/login/login.component';
import { RoamingMapComponent } from '@components/roaming-map/roaming-map.component';
import { AuthInterceptor } from '@helpers/auth.interceptor';
import { environment } from '@env/environment';
import { ListCountriesComponent } from '@components/list-countries/list-countries.component';
import { AddEditCountryComponent } from '@components/add-edit-country/add-edit-country.component';
import { ListOperatorsComponent } from '@components/list-operators/list-operators.component';
import { AddEditOperatorComponent } from '@components/add-edit-operator/add-edit-operator.component';
import { ConfirmDialogComponent } from '@components/confirm-dialog/confirm-dialog.component';
import { ListUsersComponent } from '@components/list-users/list-users.component';
import { AddEditUserComponent } from '@components/add-edit-user/add-edit-user.component';
import { ListRolesComponent } from '@components/list-roles/list-roles.component';
import { AddEditRoleComponent } from '@components/add-edit-role/add-edit-role.component';
import { NgbDateParserFormatter, NgbDatepickerI18n, NgbInputDatepicker, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ListTextsComponent } from '@components/list-texts/list-texts.component';
import { EditTextComponent } from '@components/edit-text/edit-text.component';
import { ReportRegisteredUsersComponent } from '@components/report-registered-users/report-registered-users.component';
import { ReportedRoamingsComponent } from '@components/reported-roamings/reported-roamings.component';
import { NgbdSortableHeader } from '@helpers/ngbd-sortable-header';
import { DeleteClientProfileComponent } from '@components/delete-client-profile/delete-client-profile.component';
import { MessageDialog } from '@components/message-dialog/message-dialog.component';
import { NgxCaptchaModule } from 'ngx-captcha';
import { CustomDateParserFormatter } from '@helpers/custom-date-parser-formatter';
import { ReportedRoamingsChartComponent } from '@components/reported-roamings-chart/reported-roamings-chart.component';
import { ChartsModule } from 'ng2-charts';
import { NotificationsComponent } from '@components/notifications/notifications.component';
import { AddNotificationComponent } from '@components/add-notification/add-notification.component';
import { ExportService } from '@services/export.service';
import { ExportDialogComponent } from '@components/export-dialog/export-dialog.component';
import { ReportedRoamingsLineChartComponent } from '@components/reported-roamings-line-chart/reported-roamings-line-chart.component';
import { JournalListComponent } from '@components/journal-list/journal-list.component';
import { JournalCompareComponent } from '@components/journal-compare/journal-compare.component';
import { ReportedRoamingsComparisonChartComponent } from '@components/reported-roamings-comparison-chart/reported-roamings-comparison-chart.component';
import { AboutComponent } from '@components/about/about.component';
import { ReportedBlockingsEventsComponent } from '@components/reported-blockings-events/reported-blockings-events.component';
import { ReportedBlockingsDistributionComponent } from '@components/reported-blockings-distribution/reported-blockings-distribution.component';
import { ReportedBlockingsEvolutionComponent } from '@components/reported-blockings-evolution/reported-blockings-evolution.component';
import { ReportedBlockingsComparisonComponent } from '@components/reported-blockings-comparison/reported-blockings-comparison.component';
import { MobileApps } from '@components/mobile-apps/mobile-apps.component';
import { CancelledComponent } from '@components/cancelled/cancelled.component';
import { CustomDatepickerI18n } from '@helpers/custom-datepicker-i18n';
import { DatepickerTooltipI18n } from '@helpers/datepicker-i18n-tooltips';

@NgModule({
    declarations: [
        AppComponent,
        AboutComponent,
        AddEditCountryComponent,
        AddEditOperatorComponent,
        AddEditRoleComponent,
        AddEditUserComponent,
        AddNotificationComponent,
        CancelledComponent,
        ChangePassword,
        ConfirmDialogComponent,
        DatepickerTooltipI18n,
        DeleteClientProfileComponent,
        EditTextComponent,
        ForgottenPasswordComponent,
        JournalCompareComponent,
        JournalListComponent,
        LoginComponent,
        ListCountriesComponent,
        ListOperatorsComponent,
        ListRolesComponent,
        ListTextsComponent,
        ListUsersComponent,
        MessageDialog,
        MobileApps,
        NotificationsComponent,
        RoamingMapComponent,
        ReportRegisteredUsersComponent,
        ReportedRoamingsComponent,
        NgbdSortableHeader,
        ReportedRoamingsChartComponent,
        ExportDialogComponent,
        ReportedRoamingsLineChartComponent,
        ReportedRoamingsComparisonChartComponent,
        ReportedBlockingsEventsComponent,
        ReportedBlockingsDistributionComponent,
        ReportedBlockingsEvolutionComponent,
        ReportedBlockingsComparisonComponent,
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        MatDialogModule,
        NgbModule,
        NgxCaptchaModule,
        FormsModule,
        ChartsModule,
        ReactiveFormsModule,
        appRoutingModule,
        HttpClientModule,
        ToastrModule.forRoot({
        timeOut: 4500,
        positionClass: 'toast-bottom-center',
        preventDuplicates: true,
        // closeButton: true,
        // progressBar: true,
        // maxOpened: 1,
        autoDismiss: true,
        enableHtml: true
    }),
    TranslateModule.forRoot({
        defaultLanguage: 'bg',
        loader: {
            provide: TranslateLoader,
            useFactory: HttpLoaderFactory,
            deps: [HttpClient]
        }
    })
    ],
    providers: [
        DatePipe, ExportService, NgbInputDatepicker,
        { provide: HTTP_INTERCEPTORS,           useClass: AuthInterceptor, multi: true },
        { provide: APP_BASE_HREF,               useValue: environment.webPath },
        { provide: NgbDateParserFormatter,      useClass: CustomDateParserFormatter },
        { provide: NgbDatepickerI18n,           useClass: CustomDatepickerI18n },
        { provide: MAT_DIALOG_DEFAULT_OPTIONS,  useValue: {disableClose: true, hasBackdrop: true} },
    ],
    bootstrap: [AppComponent],
    entryComponents: [
        ForgottenPasswordComponent,
        ChangePassword,
    ]
})

export class AppModule { }

// required for AOT compilation
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, environment.webPath + 'assets/i18n/' , '.json');
}
