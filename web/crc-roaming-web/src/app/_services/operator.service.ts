import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Operator } from '../_models/operator.model';
import { environment } from 'src/environments/environment';

const operatorsUrl      = environment.webApiUrl + '/operators';
const addOperatorUrl    = operatorsUrl + '/add';
const updateOperatorUrl = operatorsUrl + '/edit/';
const deleteOperatorUrl = operatorsUrl + '/delete/';
const getOperatorUrl    = operatorsUrl + '/get/';
const allOperatorsUrl   = environment.webApiUrl + '/portal/operators';

@Injectable({ providedIn: 'root' })
export class OperatorService {

    constructor(
        private http: HttpClient
    ) { }

    public getOperators(orderBy?: string, direction?: string): Observable<Operator[]> {
        let url = allOperatorsUrl;
        if (orderBy != undefined && orderBy != null && direction != '') {
            url += '?orderBy=' + orderBy + '&asc=' + (direction == 'asc' ? true : false);
        }

        return this.http.get<Operator[]>(url);
    }

    public getOperatorsByCountry(countryId: string): Observable<Operator[]> {
        return this.http.get<Operator[]>(getOperatorUrl + countryId);
    }

    public addOrEditOperator(edit: boolean, id: number, countryId: number, name: string, mnc: number) {
        const data = { 'countryId': countryId, 'name': name, 'mnc': mnc };
        if (edit) {
            return this.http.post<any>(updateOperatorUrl + id, data);
        } else {
            return this.http.post<any>(addOperatorUrl, data);
        }
    }

    public deleteOperator(id: number) {
        return this.http.delete<any>(deleteOperatorUrl + id);
    }

}