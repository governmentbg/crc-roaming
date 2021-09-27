import { Injectable } from '@angular/core';
import { ReportedBlockingDistributionChart } from '@models/reported-blocking-distribution-chart.model';
import { ReportComparisonChart } from '@models/report-comparison-chart.model';
import { ReportRegisteredUser } from '@models/report-registered-user.model';
import { ReportedRoamingChart } from '@models/reported-roaming-chart.model';
import { ReportedBlocking } from '@models/reported-blocking.model';
import { ReportLineChart } from '@models/report-line-chart.model';
import { ReportedRoaming } from '@models/reported-roaming.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { SortingPaging } from '@helpers/sorting-paging';
import { Operator } from '@models/operator.model';
import { Country } from '@models/country.model';
import { Region } from '@models/region.model';
import { Page } from '@interfaces/page';
import { first } from 'rxjs/operators';
import { Observable } from 'rxjs';

const reportsUrl                            = environment.webApiUrl + '/reports';
const publicPortalUrl                       = environment.webApiUrl + '/portal';
const registeredUsersUrl                    = reportsUrl      + '/registered-users';
const getCountriesUrl                       = publicPortalUrl + '/countries';
const getOperatorsUrl                       = publicPortalUrl + '/operators';
const getRegionsUrl                         = publicPortalUrl + '/regions';

const reportedRoamingsUrl                   = reportsUrl + '/roamings';
const countReportedRoamingsUrl              = reportsUrl + '/roamings-count';
const reportedRoamingDistributionChartsUrl  = reportsUrl + '/roamings-charts';
const reportedRoamingEvolutionChartUrl      = reportsUrl + '/roamings-linear';
const reportedRoamingComparisonChartUrl     = reportsUrl + '/roamings-comparison';

const reportedBlockingsUrl                  = reportsUrl + '/blockings';
const countReportedBlockingsUrl             = reportsUrl + '/blockings-count';
const reportedBlockingDistributionChartsUrl = reportsUrl + '/blockings-charts';
const reportedBlockingEvolutionChartUrl     = reportsUrl + '/blockings-linear';
const reportedBlockingComparisonChartUrl    = reportsUrl + '/blockings-comparison';

@Injectable({ providedIn: 'root' })
export class ReportingService {
 
    constructor(
        private http: HttpClient
    ) { }

    // Start general report section
    public fillRegions(regions: Region[]) {
        this.http.get<Region[]>(getRegionsUrl).pipe(first()).subscribe(result => {
            result.forEach(r => {
                regions.push(r);
            })
        });
    }

    public fillCountries(countries: Country[]) {
        this.http.get<Country[]>(getCountriesUrl).pipe(first()).subscribe(result => {
            result.forEach(c => {
                countries.push(c);
            })
        });
    }

    public fillOperators(operators: Operator[]) {
        this.http.get<Operator[]>(getOperatorsUrl).pipe(first()).subscribe(result => {
            result.forEach(o => {
                operators.push(o);
            })
        });
    }
    // End general report section

    // Start registered users section
    public getNumberOfRegisteredUsers(fromDate: string, toDate: string): Observable<ReportRegisteredUser> {
        let requestParams = this.createHttpReportedRequestParams(null, fromDate, toDate, null, null, null, null);
        return this.http.get<ReportRegisteredUser>(registeredUsersUrl, { params: requestParams });
    }
    // End registered users section

    // Start reported blocking section
    public getReportedBlockings(sortingPaging: SortingPaging, fromDate: string, toDate: string, countryId: number, operatorId: number, getAll: boolean): Observable<Page<ReportedBlocking>> {
        let requestParams = this.createHttpReportedRequestParams(sortingPaging, fromDate, toDate, countryId, operatorId, null,getAll);
        return this.http.get<Page<ReportedBlocking>>(reportedBlockingsUrl, { params: requestParams });
    }

    public getTotalReportedBlockingElements(fromDate: string, toDate: string, countryId: number, operatorId: number): Observable<number> {
        let requestParams = this.createHttpReportedRequestParams(null, fromDate, toDate, countryId, operatorId, null, null);
        return this.http.get<number>(countReportedBlockingsUrl, { params: requestParams });
    } 

    public getReportedBlockingDistributionChartsData(fromDate: string, toDate: string,) : Observable<ReportedBlockingDistributionChart> {
        let requestParams = this.createHttpReportedRequestParams(null, fromDate, toDate, null, null, null, null);
        return this.http.get<ReportedBlockingDistributionChart>(reportedBlockingDistributionChartsUrl, { params: requestParams });
    }

    public getReportedBlockingEvolutionChartsData(fromMonth: number, toMonth: number, fromYear: number, toYear: number, countryId: number, operatorId: number) : Observable<ReportLineChart> {
        let requestParams = this.createEvolutionChartHttpRequestParams(fromMonth, toMonth, fromYear, toYear, countryId, operatorId, null);
        return this.http.get<ReportLineChart>(reportedBlockingEvolutionChartUrl, { params: requestParams });
    }

    public getReportedBlockingComparisonChartData(forMonthFirstPeriod: number, forYearFirstPeriod: number, forMonthSecondPeriod: number, forYearSecondPeriod: number, countryId: number, operatorId: number) : Observable<ReportLineChart> {
        let requestParams = this.createComparisonChartHttpRequestParams(forMonthFirstPeriod, forYearFirstPeriod, forMonthSecondPeriod, forYearSecondPeriod, countryId, operatorId, null);
        return this.http.get<ReportComparisonChart>(reportedBlockingComparisonChartUrl, { params: requestParams });
    }
    // End reported blocking section

    // Start reported roamings section
    public getReportedRoamings(sortingPaging: SortingPaging, fromDate: string, toDate: string, countryId: number, operatorId: number, regionId: number, getAll: boolean): Observable<Page<ReportedRoaming>> {
        let requestParams = this.createHttpReportedRequestParams(sortingPaging, fromDate, toDate, countryId, operatorId, regionId, getAll);
        return this.http.get<Page<ReportedRoaming>>(reportedRoamingsUrl, { params: requestParams });
    }

    public getTotalReportedRoamgingElements(fromDate: string, toDate: string, countryId: number, operatorId: number, regionId: number): Observable<number>{
        let requestParams = this.createHttpReportedRequestParams(null, fromDate, toDate, countryId, operatorId, regionId, null);
        return this.http.get<number>(countReportedRoamingsUrl, { params: requestParams });
    } 

    public getReportedRoamingDistributionChartsData(fromDate: string, toDate: string,) : Observable<ReportedRoamingChart> {
        let requestParams = this.createHttpReportedRequestParams(null, fromDate, toDate, null, null, null, null);
        return this.http.get<ReportedRoamingChart>(reportedRoamingDistributionChartsUrl, { params: requestParams });
    }

    public getReportedRoamingEvolutionChartsData(fromMonth: number, toMonth: number, fromYear: number, toYear: number, countryId: number, operatorId: number, regionId: number) : Observable<ReportLineChart> {
        let requestParams = this.createEvolutionChartHttpRequestParams(fromMonth, toMonth, fromYear, toYear, countryId, operatorId, regionId);
        return this.http.get<ReportLineChart>(reportedRoamingEvolutionChartUrl, { params: requestParams });
    }

    public getReportedRoamingComparisonChartData(forMonthFirstPeriod: number, forYearFirstPeriod: number, forMonthSecondPeriod: number, forYearSecondPeriod: number, countryId: number, operatorId: number, regionId: number) : Observable<ReportLineChart> {
        let requestParams = this.createComparisonChartHttpRequestParams(forMonthFirstPeriod, forYearFirstPeriod, forMonthSecondPeriod, forYearSecondPeriod, countryId, operatorId, regionId);
        return this.http.get<ReportComparisonChart>(reportedRoamingComparisonChartUrl, { params: requestParams });
    }
    // End reported roamings section

    // Start general http request section
    private createHttpReportedRequestParams(sortingPaging: SortingPaging, fromDate: string, toDate: string, countryId: number, operatorId: number, regionId: number, getAll: boolean) {
        let requestParams = new HttpParams();

        if (sortingPaging != null) {
            requestParams = requestParams.set("page", sortingPaging.getPageNumber());
            requestParams = requestParams.set("size", sortingPaging.getPageSize());

            if (sortingPaging.isSortingValid()) {
                requestParams = requestParams.set("sortBy", sortingPaging.getSortBy());
                requestParams = requestParams.set("sortDirection", sortingPaging.getSortDirection());
            }
        }
        if (fromDate && fromDate.length != 0 && fromDate.trim()) {
            requestParams = requestParams.set("fromDate", fromDate);
        }
        if (toDate && toDate.length != 0 && toDate.trim()) {
            requestParams = requestParams.set("toDate", toDate);
        }
        if (countryId && countryId != 0) {
            requestParams = requestParams.set("countryId", countryId.toString());
        }
        if (operatorId && operatorId != 0) {
            requestParams = requestParams.set("operatorId", operatorId.toString());
        }
        if (regionId && regionId != 0) {
            requestParams = requestParams.set("regionId", regionId.toString());
        }
        if (getAll != null) {
            requestParams = requestParams.set("getAll", getAll.toString());
        }

        return requestParams;
    }

    private createEvolutionChartHttpRequestParams(fromMonth: number, toMonth: number, fromYear: number, toYear: number, countryId: number, operatorId: number, regionId: number) {
        let requestParams = new HttpParams();
        requestParams = requestParams.set("fromMonth", fromMonth.toString());
        requestParams = requestParams.set("toMonth", toMonth.toString());
        requestParams = requestParams.set("fromYear", fromYear.toString());
        requestParams = requestParams.set("toYear", toYear.toString());
       
        if (countryId && countryId !== 0) {
            requestParams = requestParams.set("countryId", countryId.toString());
        }
        if (operatorId && operatorId !== 0) {
            requestParams = requestParams.set("operatorId", operatorId.toString());
        }
        if (regionId && regionId !== 0) {
            requestParams = requestParams.set("regionId", regionId.toString());
        }

        return requestParams;
    }

    private createComparisonChartHttpRequestParams(forMonthFirstPeriod: number, forYearFirstPeriod: number, forMonthSecondPeriod: number, forYearSecondPeriod: number, countryId: number, operatorId: number, regionId: number) {
        let requestParams = new HttpParams();
        requestParams = requestParams.set("forMonthFirstPeriod", forMonthFirstPeriod.toString());
        requestParams = requestParams.set("forYearFirstPeriod", forYearFirstPeriod.toString());
        requestParams = requestParams.set("forMonthSecondPeriod", forMonthSecondPeriod.toString());
        requestParams = requestParams.set("forYearSecondPeriod", forYearSecondPeriod.toString());
       
        if (countryId && countryId !== 0) {
            requestParams = requestParams.set("countryId", countryId.toString());
        }
        if (operatorId && operatorId !== 0) {
            requestParams = requestParams.set("operatorId", operatorId.toString());
        }
        if (regionId && regionId !== 0) {
            requestParams = requestParams.set("regionId", regionId.toString());
        }

        return requestParams;
    }
    // End general http request section

}
