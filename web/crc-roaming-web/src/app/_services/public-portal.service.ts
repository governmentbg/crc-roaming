import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { Util } from '@helpers/util';
import { Translation } from '@models/translation';
import { ReportedRoaming } from '@models/reported-roaming.model';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { UIService } from '@services/ui.service';

const portalUrl                 = environment.webApiUrl + '/portal';
const requestDeleteUrl          = portalUrl + '/request-delete';
const mapReportedRoamingsUrl    = portalUrl + '/reported-roamings';
const aboutTextUrl              = portalUrl + '/about/{lang}';
const privacyPolicyUrl          = portalUrl + '/privacy-policy/{lang}';

@Injectable({ providedIn: 'root' })
export class PublicPortalService {
    constructor(
        private http: HttpClient,
        private ui: UIService
    ) {}

    public deleteClientProfile(email: string) {
        let body = { 'email': email }
        return this.http.post<any>(requestDeleteUrl, body);
    }

    public getMapReportedRoamings(from: NgbDateStruct, to: NgbDateStruct, countryId: number, operatorId: number, regionId: number) {
        let params = new HttpParams()
            .set("from", Util.dateToIsoString(from))
            .set("to", Util.dateToIsoString(to))
            .set("countryId",   countryId   > 0 ? countryId.toString() : "")
            .set("operatorId",  operatorId  > 0 ? operatorId.toString() : "")
            .set("regionId",    regionId    > 0 ? regionId.toString() : "");
        return this.http.get<ReportedRoaming[]>(mapReportedRoamingsUrl, {params: params});
    }

    public getAboutText(): Observable<Translation> {
        let lang = this.ui.getCurrentLang().toLocaleUpperCase();
        return this.http.get<Translation>(aboutTextUrl.replace('{lang}', lang));
    }

    public getPrivacyPolicyText(): Observable<Translation> {
        let lang = this.ui.getCurrentLang().toLocaleUpperCase();
        return this.http.get<Translation>(privacyPolicyUrl.replace('{lang}', lang));
    }

}