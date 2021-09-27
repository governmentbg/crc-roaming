import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { SortingPaging } from '@helpers/sorting-paging';
import { JournalType as JournalObjType } from '@models/journal-type.model';
import { JournalModel } from '@models/journal.model';
import { first } from 'rxjs/operators';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Util } from '@helpers/util';
import { Page } from '@interfaces/page';
import { Observable } from 'rxjs';

const journalUrl                = environment.webApiUrl + '/journal';
const journalTypesUrl           = journalUrl + '/all-types';
const journalRecordsUrl         = journalUrl + '/all-records';
const journalRecordsCountUrl    = journalUrl + '/count-records';
const singleRecordUrl           = journalUrl + '/get/{id}';

@Injectable({ providedIn: 'root' })
export class JournalService {
 
    constructor(
        private http: HttpClient
    ) { }

    public fillRecordTypes(recordTypes: JournalObjType[]) {
        this.http.get<JournalObjType[]>(journalTypesUrl).pipe(first()).subscribe(r => {
            r.forEach(t => {
                recordTypes.push(t);
            });
        });
    }

    public getJournalRecords(sortingPaging: SortingPaging, from: NgbDateStruct, to: NgbDateStruct, type: JournalObjType): Observable<Page<JournalModel>> {
        let params = {from: Util.dateToIsoString(from), to: Util.dateToIsoString(to), page: sortingPaging.getPageNumber(), size: sortingPaging.getPageSize()};
        if (type && type != "null") {
            params["objType"] = type;
        }

        return this.http.get<Page<JournalModel>>(journalRecordsUrl, {params: params});
    }

    public getTotalJournalRecordsCount(from: NgbDateStruct, to: NgbDateStruct, type: JournalObjType): Observable<number> {
        let params = {from: Util.dateToIsoString(from), to: Util.dateToIsoString(to)};
        if (type && type != "null") {
            params["objType"] = type;
        }

        return this.http.get<number>(journalRecordsCountUrl, {params: params});
    }

    public getJournalRecord(id: number) {
        return this.http.get<JournalModel>(singleRecordUrl.replace('{id}', id + ''));
    }

}
