import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Country } from '../_models/country.model';
import { environment } from 'src/environments/environment';

const countriesUrl = environment.webApiUrl + '/countries';
const addCountryUrl = countriesUrl + '/add';
const getCountryUrl = countriesUrl + '/get/';
const updateCountryUrl = countriesUrl + '/edit/';
const deleteCountryUrl = countriesUrl + '/delete/';
const getCountriesUrl = environment.webApiUrl + '/portal/countries';

@Injectable({ providedIn: 'root' })
export class CountryService {

    constructor(
        private http: HttpClient
    ) { }

    public getCountries(orderBy?: string, direction?: string): Observable<Country[]> {
        let url = getCountriesUrl;
        if (orderBy != undefined && orderBy != null && direction != '') {
            url += '?orderBy=' + orderBy + '&asc=' + (direction == 'asc' ? true : false);
        }
        return this.http.get<Country[]>(url);
    }

    public getCountry(id: number) {
        return this.http.get<any>(getCountryUrl + id);
    }

    public addOrEditCountry(edit: boolean, id: number, nameInt: string, nameBg: string, mcc: string, phoneCode: string, euMember: boolean) {
        const data = { 'nameInt': nameInt, 'nameBg': nameBg, 'mcc': mcc, 'phoneCode': phoneCode, 'euMember': euMember };
        if (edit) {
            return this.http.post<any>(updateCountryUrl + id, data);
        } else {
            return this.http.post<any>(addCountryUrl, data);
        }
    }

    public deleteCountry(id: number) {
        return this.http.delete<any>(deleteCountryUrl + id);
    }

}
