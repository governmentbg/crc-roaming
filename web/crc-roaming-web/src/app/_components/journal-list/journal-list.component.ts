import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { JournalCompareComponent } from '@components/journal-compare/journal-compare.component';
import { config, modalMinWidth } from '@env/config';
import { SortingPaging } from '@helpers/sorting-paging';
import { JournalType } from '@models/journal-type.model';
import { JournalModel } from '@models/journal.model';
import { NgbDate, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { JournalService } from '@services/journal.service';
import { PermissionService } from '@services/permission.service';
import { UIService } from '@services/ui.service';
import { first } from 'rxjs/operators';

@Component({
    selector: 'app-journal-list',
    templateUrl: './journal-list.component.html',
})
export class JournalListComponent implements OnInit {
    formatTS: string;
    recordTypes: JournalType[];
    journalList: JournalModel[];
    sortingPaging!: SortingPaging;
    pageSizeOptions: number[];

    fromDate: NgbDateStruct;
    toDate: NgbDateStruct;
    selectedType: JournalType

    constructor(
        private journalService: JournalService,
        private dialog: MatDialog,
        private ui: UIService,
        private perms: PermissionService
    ) {
        if (!this.perms.hasAccess(this.perms.all.viewJournal)) {
            this.ui.showError("msg.permissionDenied");
            throw new Error("Permission denied!");
        }
        this.formatTS = config.formatTS;
        this.sortingPaging = new SortingPaging(config.pageSize);
        this.pageSizeOptions = config.pageSizeOptions;
    }

    ngOnInit() {
        this.clearFilter(true);

        this.recordTypes = [];
        this.journalService.fillRecordTypes(this.recordTypes);
        this.loadJournalData();
        // this.loadTotalCount();
    }

    clearFilter(doNotLoad?: boolean) {
        let d = new Date();
        this.toDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());
        d.setMonth(d.getMonth() - 1);
        this.fromDate = new NgbDate(d.getFullYear(), d.getMonth() + 1, d.getDate());

        this.selectedType = null;

        if (!doNotLoad) {
            this.loadJournalData();
            // this.loadTotalCount();
        }
    }

    loadJournalData() {
        this.loadTotalCount();
        // this.journalList = [];
        this.journalService.getJournalRecords(this.sortingPaging, this.fromDate, this.toDate, this.selectedType).pipe(first()).subscribe(resp => {
            this.journalList = resp.content;
            this.sortingPaging.fromRow = resp.fromRow;
            this.sortingPaging.toRow = resp.toRow;
        });
    }

    loadTotalCount() {
        this.journalService.getTotalJournalRecordsCount(this.fromDate, this.toDate, this.selectedType).pipe(first()).subscribe(resp => {
            this.sortingPaging.totalElements = resp;
        });
    }

    show(journalElement: JournalModel) {
        if (this.dialog.openDialogs.length == 0) {
            let ref = this.dialog.open(JournalCompareComponent, modalMinWidth);
            ref.componentInstance.id = journalElement.id;
        }
    }

    pageChanged(page: number) {
        this.sortingPaging.pageNumber = page;
        this.loadJournalData();
    }

}
