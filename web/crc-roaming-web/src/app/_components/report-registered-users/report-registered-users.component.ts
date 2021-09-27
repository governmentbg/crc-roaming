import { Component, OnInit } from '@angular/core';
import { ReportRegisteredUser } from '@models/report-registered-user.model';
import { ReportingService } from '@services/reporting.service';
import { config } from '@env/config';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap';
import { Util } from '@helpers/util';
import { ExportService } from '@services/export.service';
import { TranslateService } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { UIService } from '@services/ui.service';
import { PermissionService } from '@services/permission.service';

@Component({
    selector: 'app-report-registered-users',
    templateUrl: './report-registered-users.component.html'
})
export class ReportRegisteredUsersComponent implements OnInit {
    report: ReportRegisteredUser;
    formatTS: string;
    fromDate: any;
    toDate: any;

    constructor(
        private reportingService: ReportingService,
        private exportService: ExportService,
        private translate: TranslateService,
        private datepipe: DatePipe,
        private ui: UIService,
        private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.viewReports)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
        this.report = new ReportRegisteredUser;
        this.formatTS = config.formatTS;
    }

    ngOnInit() {
        this.loadDates();
        this.loadReport();
    }

    loadDates() {
        let d = new Date();
        this.toDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
        d.setMonth(d.getMonth() - 1);
        this.fromDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
    }

    loadReport() {
        this.reportingService.getNumberOfRegisteredUsers(Util.dateToIsoString(this.fromDate), Util.dateToIsoString(this.toDate)).subscribe(r => {
            this.report = r;
        });
    }

    filter() {
        this.loadReport();
    }

    clearFilters() {
        this.loadDates();
        this.loadReport();
    }

    export(type: string) {
        if (type == 'pdf') {
            this.exportService.exportAsPdfFile(
                this.prepareData(), this.getHeadings(),
                this.getFilterBody(), this.getFilterHeadings(),
                this.translate.instant("report.numberOfRegisteredMobileUsers"));
        } else if (type == "xlsx") {
            this.exportService.exportAsExcelFile(
                this.prepareData(), this.getHeadings(),
                this.getFilterBody(), this.getFilterHeadings(),
                this.translate.instant("report.numberOfRegisteredMobileUsers"));
        }
    }

    private getHeadings() : string[][] {
        return [[
            this.translate.instant("report.numberOfRegUsers"), 
            this.translate.instant("report.lastCreatedUserDate")]]
    }

    private prepareData(): any[] {
        return [[
            this.report.numberOfRegisteredUsers,
            this.datepipe.transform(this.report.dateOfLastCreatedUser, config.formatTS)]];
    }

    private getFilterHeadings(): any[][] {
        return [
            [
                {content: this.translate.instant('report.filterHeader'), colSpan: 2, styles: {halign: 'center'}}],
            [
                this.translate.instant('filter.fromDate'),
                this.translate.instant('filter.toDate')]
        ];
    }

    private getFilterBody(): any[] {
        return [[
            Util.formatDate(this.fromDate),
            Util.formatDate(this.toDate)
        ]];
    }

}

