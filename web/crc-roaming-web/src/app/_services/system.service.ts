import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { Translation } from '@models/translation';

const systemUrl  = environment.webApiUrl + '/system';
const getTextsUrl   = systemUrl + '/get-texts';
const updateTextUrl = systemUrl + '/update-text/{key}/{lang}';

@Injectable({ providedIn: 'root' })
export class SystemService {

    constructor(
        private http: HttpClient
    ) { }

    public getSystemTexts() {
        return this.http.get<Translation[]>(getTextsUrl);
    }

    public updateSystemText(key: string, lang: string, value: string) {
        let body = {value: value};
        return this.http.post(updateTextUrl.replace('{key}', key).replace('{lang}', lang), body);
    }
}
