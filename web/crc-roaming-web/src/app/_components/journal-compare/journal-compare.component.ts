import { Component, OnInit } from '@angular/core';
import { config } from '@env/config';
import { JournalModel } from '@models/journal.model';
import { JournalService } from '@services/journal.service';
import { first } from 'rxjs/operators';

@Component({
    selector: 'app-journal-compare',
    templateUrl: './journal-compare.component.html'
})
export class JournalCompareComponent implements OnInit {
    id: number;
    element: JournalModel;
    formatTS: string;

    constructor(
        private journalService: JournalService
    ) {
        this.formatTS = config.formatTS;
    }

    ngOnInit() {
        this.journalService.getJournalRecord(this.id).pipe(first()).subscribe(r => {
            this.element = r;
            this.element.objType = 'journal.obj.' + r.objType;
            this.element.operationType = 'journal.operation.' + r.operationType;
        });
    }

}
