import { Injectable } from "@angular/core";
import { NgbDatepickerI18n, NgbDateStruct } from "@ng-bootstrap/ng-bootstrap";
import { TranslateService } from "@ngx-translate/core";

@Injectable({providedIn: 'root'})
export class CustomDatepickerI18n extends NgbDatepickerI18n {

    constructor(private translate: TranslateService) {
        super();
    }

    getWeekdayShortName(weekday: number): string {
        return this.translate.instant('wd.' + weekday);
    }
    getMonthShortName(month: number, year?: number): string {
        return this.translate.instant('month.' + month);
    }
    getMonthFullName(month: number, year?: number): string {
        return 'c' + month;
    }
    getDayAriaLabel(date: NgbDateStruct): string {
        return 'd' + date;
    }

}