import { Component } from '@angular/core';
import { NgbCalendar } from '@ng-bootstrap/ng-bootstrap';
import { ReportingService } from '@services/reporting.service';
import { OperatorService } from '@services/operator.service';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { Operator } from '@models/operator.model';
import { Country } from '@models/country.model';
import { Color, Label } from 'ng2-charts';
import { first } from 'rxjs/operators';
import { Util } from '@helpers/util';
import { TranslateService } from '@ngx-translate/core';
import { ExportService } from '@services/export.service';
import { UIService } from '@services/ui.service';
import { PermissionService } from '@services/permission.service';
import { tick } from '@angular/core/testing';
import { lineChartOptions } from '@env/config';

@Component({
  selector: 'app-reported-blockings-evolution',
  templateUrl: './reported-blockings-evolution.component.html'
})
export class ReportedBlockingsEvolutionComponent  {
    fromMonth: number;
    toMonth: number;
    fromYear: number;
    toYear: number;    
    months: number[];
    years: number[];
    countries: Country[];
    operators: Operator[];
    countryId: number;
    operatorId: number;
    disableOperator: boolean;

    lineChartData: ChartDataSets[];
    lineChartLabels: Label[];
    lineChartOptions: (ChartOptions & { annotation ?: any });
    lineChartColors: Color[];
    lineChartLegend: boolean;
    lineChartType: string;
    lineChartPlugins: [];

    constructor(
      private reportingService: ReportingService,
      private operatorService: OperatorService,
      private ngbCalendar: NgbCalendar,
      private translate: TranslateService,
      private exportService: ExportService,
      private ui: UIService,
      private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.viewReports)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
        this.countries = [];
        this.operators = [];
        this.lineChartData = [];
        this.lineChartColors = [];
        this.months = Util.getMonths();
        this.years = Util.getYears();
        this.loadCommonChartSettings();
    }

    ngOnInit() {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.loadDates();
      this.reportingService.fillCountries(this.countries);

      this.loadReporetLineChart();
    }

    loadReporetLineChart() : void {
        this.reportingService.getReportedBlockingEvolutionChartsData(
            this.fromMonth, this.toMonth, this.fromYear, this.toYear, this.countryId, this.operatorId)
            .pipe(first()).subscribe(result => {
                this.lineChartData = [{ data: result.counts, label: this.translate.instant("menu.reports.reportedEventsLinear")}];
                this.lineChartLabels = result.labels;
            });
    }

    exportChart() {
          this.exportService.exportSingleLineChartAsPDF(
            'reportedBlockingsLineChart',
            this.getFilterHeading(), this.getFilterBody(),
            this.translate.instant("menu.reports.blockingEventsLinear"));
        }
    
        private getFilterHeading(): any[][] {
            return [
                [{content: this.translate.instant('report.filterHeader'), colSpan: 6, styles: {halign: 'center'}}],
                [
                    this.translate.instant('filter.fromMonth'),
                    this.translate.instant('filter.fromYear'),
                    this.translate.instant('filter.toMonth'),
                    this.translate.instant('filter.toYear'),
                    this.translate.instant('filter.country'),
                    this.translate.instant('filter.operator')
                ]
            ];
        }
    
        private getFilterBody(): any[] {
            let all = this.translate.instant('filter.all');
            let country = Util.filter(this.countries, this.countryId);
            let operator = Util.filter(this.operators, this.operatorId);
    
            return [[
                this.translate.instant('month.' + this.fromMonth),
                this.fromYear,
                this.translate.instant('month.' + this.toMonth),
                this.toYear,
                country ? country.nameBg : all,
                operator ? operator.name : all
            ]];
        }

    filter() : void {
      this.nullifyCharts();
      this.loadReporetLineChart();
    }
  
    clearFilters() : void {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.nullifyCharts();
      this.loadDates();
      this.loadReporetLineChart();
    }
  
    nullifyCharts() {
      this.lineChartData = [];
      this.lineChartLabels = [];
    }
    
    loadDates() {
      let today = this.ngbCalendar.getToday();
      this.fromMonth = today.month - 1;
      this.toMonth = today.month;
      this.fromYear = today.year;
      this.toYear = today.year;
    }

    onCountrySelect(e) {
      this.countryId = e.target.value;
      this.disableOperatorIfNeeded(this.countryId);
      this.nullifyCountriesAndOperators(true);
      this.loadOperatorsByCountry(this.countryId);
    }

    loadOperatorsByCountry(selectedCountryId: number): void {
      this.operatorService.getOperatorsByCountry(selectedCountryId.toString())
          .subscribe(o => this.operators = o);
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

    loadCommonChartSettings() : void {
        this.lineChartColors = this.lineChartColorsSettings;
        this.lineChartLegend = true;
        this.lineChartType = 'line';
        this.lineChartOptions = lineChartOptions;
    }

    public lineChartColorsSettings: Array<any> = [{
      borderColor: 'black',
      backgroundColor: 'rgba(255, 0, 0, 0.3)',
    }];

}

