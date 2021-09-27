import { Component } from '@angular/core';
import { NgbCalendar} from '@ng-bootstrap/ng-bootstrap';
import { ReportingService } from '@services/reporting.service';
import { OperatorService } from '@services/operator.service';
import { ChartOptions, ChartType } from 'chart.js';
import { Operator } from '@models/operator.model';
import { Country } from '@models/country.model';
import { Region } from '@models/region.model';
import { Color, Label, SingleDataSet } from 'ng2-charts';
import { first } from 'rxjs/operators';
import { Util } from '@helpers/util';
import { TranslateService } from '@ngx-translate/core';
import { ExportService } from '@services/export.service';
import { UIService } from '@services/ui.service';
import { PermissionService } from '@services/permission.service';
import * as Chart from 'chart.js';
import { barChartOptions } from '@env/config';

@Component({
  selector: 'app-reported-roamings-comparison-chart',
  templateUrl: './reported-roamings-comparison-chart.component.html'
})
export class ReportedRoamingsComparisonChartComponent  {
    forMonthFirstPeriod: number;
    forYearFirstPeriod: number;
    forMonthSecondPeriod: number;
    forYearSecondPeriod: number;  
    months: number[];
    years: number[];
    countries: Country[];
    operators: Operator[];
    regions: Region[];
    countryId: number;
    operatorId: number;
    regionId: number;
    disableOperator: boolean;

    chartLabels: Label[];
    chartData: SingleDataSet;
    chartOptions: ChartOptions;
    chartColors: Color[];

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
        this.regions = [];
        this.chartLabels = [];
        this.chartData = [];
        this.months = Util.getMonths();
        this.years = Util.getYears();
        
        this.chartColors = this.pieChartColorsSettings;
        this.chartOptions = barChartOptions;
    }

    ngOnInit() {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.loadDates();
      this.reportingService.fillRegions(this.regions);
      this.reportingService.fillCountries(this.countries);
      this.loadComparisonChartData();
    }

    loadComparisonChartData() : void {
        this.reportingService.getReportedRoamingComparisonChartData(
            this.forMonthFirstPeriod, this.forYearFirstPeriod,
            this.forMonthSecondPeriod, this.forYearSecondPeriod,
            this.countryId, this.operatorId, this.regionId)
            .pipe(first()).subscribe(result => {
                this.chartData = result.counts;
                console.log(this.chartData);
                result.labels.forEach(element => {
                    if (element == 'First period') {
                        this.chartLabels.push(this.translate.instant("filter.firstPeriod"));
                    }
                    if (element == 'Second period') {
                        this.chartLabels.push(this.translate.instant("filter.secondPeriod"));
                    }
                });
        });
    }

    exportChart() {
      this.exportService.exportSingleLineChartAsPDF(
        'comparisonChart',
        this.getFilterHeading(), this.getFilterBody(),
        this.translate.instant("menu.reports.reportedEventsPeriods"))
    }

    private getFilterHeading(): any[][] {
        return [
            [{content: this.translate.instant('report.filterHeader'), colSpan: 7, styles: {halign: 'center'}}],
            [
                this.translate.instant('filter.forMonthFirstPeriod'),
                this.translate.instant('filter.forYearFirstPeriod'),
                this.translate.instant('filter.forMonthSecondPeriod'),
                this.translate.instant('filter.forYearSecondPeriod'),
                this.translate.instant('filter.country'),
                this.translate.instant('filter.operator'),
                this.translate.instant('filter.region'),
            ]
        ];
    }

    private getFilterBody(): any[] {
        let all = this.translate.instant('filter.all');
        let country = Util.filter(this.countries, this.countryId);
        let operator = Util.filter(this.operators, this.operatorId);
        let region = Util.filter(this.regions, this.regionId);

        return [[
            this.translate.instant('month.' + this.forMonthFirstPeriod),
            this.forYearFirstPeriod,
            this.translate.instant('month.' + this.forMonthSecondPeriod),
            this.forYearSecondPeriod,
            country ? country.nameBg : all,
            operator ? operator.name : all,
            region ? region.name : all
        ]];
    }
    
    filter() : void {
      this.nullifyCharts();
      this.loadComparisonChartData();
    }
  
    clearFilters() : void {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.nullifyCharts();
      this.loadDates();
      this.loadComparisonChartData();
    }
  
    nullifyCharts() {
      this.chartData = [];
      this.chartLabels = [];
    }
    
    loadDates() {
      let today = this.ngbCalendar.getToday();
      this.forMonthFirstPeriod = today.month - 1;
      this.forYearFirstPeriod = today.year;
      this.forMonthSecondPeriod = today.month;
      this.forYearSecondPeriod = today.year;
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
        this.regionId = 0;
        this.operators = [];
      }
    }

    public pieChartColorsSettings: Array < any > = [{
      backgroundColor: ['rgb(93, 173, 226)', 'rgb(244, 208, 63)'],
      borderColor:     ['rgb(93, 173, 226)', 'rgb(244, 208, 63)']
    }];
  
}
