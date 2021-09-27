import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { SortingPaging } from '@helpers/sorting-paging';
import { config } from '@env/config';
import { first } from 'rxjs/operators';
import { Country } from '@models/country.model';
import { OperatorService } from '@services/operator.service';
import { Operator } from '@models/operator.model';
import { SortEvent } from '@interfaces/sort-event';
import { NgbdSortableHeader } from '@helpers/ngbd-sortable-header';
import { Util } from '@helpers/util';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap';
import { ReportingService } from '@services/reporting.service';
import { ExportService } from '@services/export.service';
import { DatePipe } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { ExportDialogComponent } from '@components/export-dialog/export-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ReportedBlocking } from '@models/reported-blocking.model';
import { UIService } from '@services/ui.service';
import { PermissionService } from '@services/permission.service';

@Component({
  selector: 'app-reported-blockings-events',
  templateUrl: './reported-blockings-events.component.html'
})
export class ReportedBlockingsEventsComponent implements OnInit {
    report: ReportedBlocking[];
    pageSizeOptions: number[];
    sortingPaging!: SortingPaging;
    fromDate: any; 
    toDate: any;
    formatTS: string;
    countries: Country[];
    operators: Operator[];
    countryId: number;
    operatorId: number;
    disableOperator: boolean;
   
    @ViewChildren(NgbdSortableHeader) headers: QueryList<NgbdSortableHeader>;

    constructor(
        private reportingService: ReportingService,
        private operatorService: OperatorService,
        private exportService: ExportService,
        private datepipe: DatePipe,
        private dialog: MatDialog,
        private translate: TranslateService,
        private ui: UIService,
        private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.viewReports)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
        this.report = [];
        this.countries = [];
        this.operators = [];
        this.disableOperator = false;
        this.formatTS = config.formatTS;
        this.pageSizeOptions = config.pageSizeOptions;
        this.sortingPaging = new SortingPaging(config.pageSize);
    }

    ngOnInit() {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.loadDates();
      this.loadReport();
      this.loadReportTotalElements();
      this.reportingService.fillCountries(this.countries);
    }

    loadReport(): void {
      this.reportingService.getReportedBlockings(this.sortingPaging, Util.dateToIsoString(this.fromDate), Util.dateToIsoString(this.toDate), this.countryId, this.operatorId, false).pipe(first()).subscribe(resp => {
          this.report = resp.content;
          this.sortingPaging.fromRow = resp.fromRow;
          this.sortingPaging.toRow = resp.toRow;
      });
    }

    loadReportTotalElements(): void {
        this.reportingService.getTotalReportedBlockingElements(Util.dateToIsoString(this.fromDate), Util.dateToIsoString(this.toDate), this.countryId, this.operatorId).subscribe(resp => {
            this.sortingPaging.totalElements = resp;
        });
    }

    loadOperatorsByCountry(selectedCountryId: number): void {
      this.operatorService.getOperatorsByCountry(selectedCountryId.toString())
          .subscribe(o => this.operators = o);
    }

    loadDates() {
        let d = new Date();
        this.toDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
        d.setMonth(d.getMonth() - 1);
        this.fromDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
    }

    pageChanged(page: number) {
        this.sortingPaging.pageNumber = page;
        this.loadReport(); 
    }

    filter() {
      this.loadReport();
      this.loadReportTotalElements();
    }

    clearFilters() {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.loadDates();
      this.loadReport();
      this.loadReportTotalElements();
    }

    onCountrySelect(e) {
      this.countryId = e.target.value;
      this.disableOperatorIfNeeded(this.countryId);
      this.nullifyCountriesAndOperators(true);
      this.loadOperatorsByCountry(this.countryId);
    }

    onSort({column, direction}: SortEvent) {
      // clear other sort
      this.headers.forEach(header => {
        if (header.sortable !== column) {
          header.direction = '';
        }
      });

      this.sortingPaging.setSortBy(column);
      this.sortingPaging.setSortDirection(direction);

      this.loadReport();
    }

    disableOperatorIfNeeded(countryId: number): void {
      if(this.countryId === 0) {
        this.disableOperator = true;
      } else {
        this.disableOperator = false
      }
    }

    nullifyCountriesAndOperators(onlyOperator: boolean) : void {
      if(onlyOperator) {
        this.operatorId = 0;
        this.operators = [];
      } else {
        this.countryId = 0;
        this.operatorId = 0;
        this.operators = [];
      }
    }

    openExportDialog(exportType: String) {
      if (this.dialog.openDialogs.length == 0) {
          let ref = this.dialog.open(ExportDialogComponent, {data: {
              titleKey: "export.chooseExportPageTitle",
              textKey: "export.chooseExportPageText"}});
          ref.afterClosed().subscribe(confirmed => {
              if(confirmed) {
                if(exportType === 'pdf') {
                  this.exportToPdf(this.report);
                } else {
                  this.exportToExcel(this.report);
                }
              } else {
                this.reportingService.getReportedBlockings(this.sortingPaging, Util.dateToIsoString(this.fromDate), Util.dateToIsoString(this.toDate), this.countryId, this.operatorId, true).pipe(first()).subscribe(resp => {
                  if(exportType === 'pdf') {
                    this.exportToPdf(resp.content);
                  } else {
                    this.exportToExcel(resp.content);
                  }
                });
              }
          });
      }
    }

    exportToPdf(reportedBlockings: ReportedBlocking[]): void {
        this.exportService.exportAsPdfFile(
            this.prepareDataToExport(reportedBlockings), this.getHeading(),
            this.getFilterBody(), this.getFilterHeading(),
            this.translate.instant("menu.reports.blockingEvents"));
    }

    exportToExcel(reportedBlockings: ReportedBlocking[]): void {
      this.exportService.exportAsExcelFile(
            this.prepareDataToExport(reportedBlockings), this.getHeading(),
            this.getFilterBody(), this.getFilterHeading(),
            this.translate.instant("menu.reports.blockingEvents"));
    }
    
    private getHeading() : string[][] {
      return [[this.translate.instant("report.blockingId"), 
               this.translate.instant("report.userId"),
               this.translate.instant("report.eventTs"),
               this.translate.instant("report.countryName"),
               this.translate.instant("report.mccCode"),
               this.translate.instant("report.operatorName"),
               this.translate.instant("report.mncCode")]]
    }

    private prepareDataToExport(reportedBlockings: ReportedBlocking[]): any[] {
      let exportArray = [];
      reportedBlockings.forEach(e => {
          let rep = [e.blockingId, e.userId, 
            this.datepipe.transform(e.eventTs, config.formatTS), 
            e.operator.country.nameBg, 
            e.operator.country.mcc,
            e.operator.name, 
            e.operator.mnc];

          exportArray.push(rep);
      });

      return exportArray;
    }

    private getFilterHeading(): any[][] {
        return [
            [
                {content: this.translate.instant('report.filterHeader'), colSpan: 4, styles: {halign: 'center'}}],
            [
                this.translate.instant('filter.fromDate'),
                this.translate.instant('filter.toDate'),
                this.translate.instant('filter.country'),
                this.translate.instant('filter.operator')]
        ];
    }

    private getFilterBody(): any[] {
        let all = this.translate.instant('filter.all');
        let country = Util.filter(this.countries, this.countryId);
        let operator = Util.filter(this.operators, this.operatorId);

        return [[
            Util.formatDate(this.fromDate),
            Util.formatDate(this.toDate),
            country ? country.nameBg : all,
            operator ? operator.name : all
        ]];
    }

}
