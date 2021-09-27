import { Component } from '@angular/core';
import { NgbCalendar} from '@ng-bootstrap/ng-bootstrap';
import { ReportingService } from '@services/reporting.service';
import { OperatorService } from '@services/operator.service';
import { ChartOptions, ChartType } from 'chart.js';
import { Operator } from '@models/operator.model';
import { Country } from '@models/country.model';
import { Color, Label, SingleDataSet } from 'ng2-charts';
import { first } from 'rxjs/operators';
import { Util } from '@helpers/util';
import { TranslateService } from '@ngx-translate/core';
import { ExportService } from '@services/export.service';
import * as Chart from 'chart.js';
import { barChartOptions } from '@env/config';

@Component({
  selector: 'app-reported-blockings-comparison',
  templateUrl: './reported-blockings-comparison.component.html'
})
export class ReportedBlockingsComparisonComponent  {
    forMonthFirstPeriod: number;
    forYearFirstPeriod: number;
    forMonthSecondPeriod: number;
    forYearSecondPeriod: number;  
    months: number[];
    years: number[];
    countries: Country[];
    operators: Operator[];
    countryId: number;
    operatorId: number;
    disableOperator: boolean;
    pieTitle: string[];

    chartLabels: Label[];
    chartData: SingleDataSet;
    chartOptions: ChartOptions;
    chartColors: Color[];
    chartType: ChartType;
    chartLegend: boolean;
    chartPlugins = [];

    constructor(
      private reportingService: ReportingService,
      private operatorService: OperatorService,
      private ngbCalendar: NgbCalendar,
      private translate: TranslateService,
      private exportService: ExportService,
    ) {
      this.countries = [];
      this.operators = [];
      this.pieTitle = [];
      this.chartLabels = [];
      this.chartData = [];
      this.months = Util.getMonths();
      this.years = Util.getYears();
      this.chartColors = [{
        backgroundColor: [
            'rgb(93, 173, 226)',
            'rgb(244, 208, 63)'],
      }];
      this.chartOptions = barChartOptions;
    }

    ngOnInit() {
      this.nullifyCountriesAndOperators(false);
      this.disableOperatorIfNeeded(this.countryId);
      this.loadDates();
      this.reportingService.fillCountries(this.countries);
      this.loadComparisonChartData();
    }

    loadComparisonChartData() : void {
      this.reportingService.getReportedBlockingComparisonChartData(this.forMonthFirstPeriod, this.forYearFirstPeriod, this.forMonthSecondPeriod, this.forYearSecondPeriod, this.countryId, this.operatorId).pipe(first()).subscribe(result => {
            this.chartData = result.counts;
            this.loadPieTitle(result.labels);
            this.chartLabels = this.pieTitle;
        });
    }

    exportChart() {
        this.exportService.exportSingleLineChartAsPDF(
          'blockingsComparisonChart',
          this.getFilterHeading(), this.getFilterBody(),
          this.translate.instant("menu.reports.blockingEventsPeriods"));
      }
  
      private getFilterHeading(): any[][] {
          return [
              [{content: this.translate.instant('report.filterHeader'), colSpan: 6, styles: {halign: 'center'}}],
              [
                  this.translate.instant('filter.forMonthFirstPeriod'),
                  this.translate.instant('filter.forYearFirstPeriod'),
                  this.translate.instant('filter.forMonthSecondPeriod'),
                  this.translate.instant('filter.forYearSecondPeriod'),
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
              this.translate.instant('month.' + this.forMonthFirstPeriod),
              this.forYearFirstPeriod,
              this.translate.instant('month.' + this.forMonthSecondPeriod),
              this.forYearSecondPeriod,
              country ? country.nameBg : all,
              operator ? operator.name : all
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
        this.operators = [];
      }
    }

    loadPieTitle(labels: string[]): void {
      this.pieTitle = [];
      labels.forEach(element => {
          if(element === 'First period') {
            this.pieTitle.push(this.translate.instant("filter.firstPeriod"));
          }
          if(element === 'Second period') {
            this.pieTitle.push(this.translate.instant("filter.secondPeriod"));
          }
       });
    }

}
