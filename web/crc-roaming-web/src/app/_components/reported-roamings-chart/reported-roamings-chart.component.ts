import { Component, enableProdMode, OnInit } from '@angular/core';
import { ChartType, ChartOptions } from "chart.js";
import { SingleDataSet, Label, Color } from "ng2-charts";
import { ReportingService } from '@services/reporting.service';
import { first } from 'rxjs/operators';
import { Util } from '@helpers/util';
import { CountryChartData } from '@models/country-chart-data.model';
import { OperatorChartData } from '@models/operator-chart-data.model';
import { RegionChartData } from '@models/region-chart-data.model';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap';
import { ExportService } from '@services/export.service';
import { TranslateService } from '@ngx-translate/core';
import { UIService } from '@services/ui.service';
import { PermissionService } from '@services/permission.service';
import { pieChartOptions } from '@env/config';

@Component({
  selector: 'app-reported-roamings-chart',
  templateUrl: './reported-roamings-chart.component.html'
})
export class ReportedRoamingsChartComponent implements OnInit {
  fromDate: any; 
  toDate: any;
  
  countryChartLabels: Label[];
  countryChartData: SingleDataSet;
  operatorChartLabels: Label[];
  operatorChartData: SingleDataSet;
  regionChartLabels: Label[];
  regionChartData: SingleDataSet;
  
  chartOptions: ChartOptions;
  chartColors: Color[];

  constructor(
    private reportingService: ReportingService,
    private exportService: ExportService,
    private translate: TranslateService,
    private ui: UIService,
    private perms: PermissionService
  ) {
    if (!this.perms.hasAccess(this.perms.all.viewReports)) {
        this.ui.showError("msg.permissionDenied");
        throw new Error("Permission denied!");
    }

    this.countryChartLabels = [];
    this.countryChartData = [];
    this.operatorChartLabels = [];
    this.operatorChartData = [];
    this.regionChartLabels = [];
    this.regionChartData = [];
    
    this.chartColors = [{
        backgroundColor: [
            'rgb(93, 173, 226)',
            'rgb(244, 208, 63)',
            'rgb(130, 224, 170 )',
            'rgb(241, 148, 138 )',
            'rgb(235, 152, 78)',
            'rgb(162, 217, 206 )',
            'rgb(205, 92, 92)',
            'rgb(187, 143, 206 )'
        ],
    }];
    this.chartOptions = pieChartOptions;
  }

  ngOnInit() {
    this.loadDates();
    this.loadReporetedRoamingCharts();
  }

  loadDates() {
    let d = new Date();
    this.toDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
    d.setMonth(d.getMonth() - 1);
    this.fromDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
  }

  exportChart() {
    this.exportService.exportChartsAsPDF(
        ['countryChart', 'operatorChart', 'regionChart'],
        this.getFilterHeading(), this.getFilterBody(),
        this.translate.instant("report.reportedRoamingsChart"))
  }

    private getFilterHeading(): any[][] {
        return [
            [{content: this.translate.instant('report.filterHeader'), colSpan: 2, styles: {halign: 'center'}}],
            [
                this.translate.instant('fromDate'),
                this.translate.instant('toDate')]
        ];
    }

    private getFilterBody(): any[] {
        return [[
            Util.formatDate(this.fromDate),
            Util.formatDate(this.toDate)
        ]];
    }

  loadReporetedRoamingCharts() : void {
    this.reportingService.getReportedRoamingDistributionChartsData(Util.dateToIsoString(this.fromDate), Util.dateToIsoString(this.toDate)).pipe(first()).subscribe(result => {
        result.countryChart.forEach((c: CountryChartData)  => {
            this.countryChartLabels.push(c.country.nameBg);
            this.countryChartData.push(c.count);
        })

        result.operatorChart.forEach((o: OperatorChartData) => {
            this.operatorChartLabels.push(o.operator.name);
            this.operatorChartData.push(o.count);
        })

        result.regionChart.forEach((r: RegionChartData) => {
            this.regionChartLabels.push(r.region.name);
            this.regionChartData.push(r.count);
        })
      });
  }

  filter() : void {
    this.nullifyCharts();
    this.loadReporetedRoamingCharts();
  }

  clearFilters() : void {
    this.nullifyCharts();
    this.loadDates();
    this.loadReporetedRoamingCharts();
  }

  nullifyCharts() {
    this.countryChartLabels = [];
    this.countryChartData = [];
    this.operatorChartLabels = [];
    this.operatorChartData = [];
    this.regionChartLabels = [];
    this.regionChartData = [];
  }

}
