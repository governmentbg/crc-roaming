import { Directive } from "@angular/core";
import { NgbInputDatepicker } from "@ng-bootstrap/ng-bootstrap";
import { TranslateService } from "@ngx-translate/core";

@Directive({selector: '[ngbDatepicker]'})
export class DatepickerTooltipI18n {

    constructor(private translate: TranslateService, private datepicker: NgbInputDatepicker) {
        const previousToggle = this.datepicker.toggle;
        this.datepicker.toggle = () => {
            previousToggle.bind(this.datepicker)();
            if (this.datepicker.isOpen()) {
                this.translateTooltips();
            }
        };
    }

    private translateTooltips() {
        // Select month: tooltip over the month drop-down
        this.setAttributes('select', 'Select month', this.translate.instant("calendar.selectMonth"));

        // Select year: tooltip over the year drop-down
        this.setAttributes('select', 'Select year', this.translate.instant("calendar.selectYear"));

        // Next month: tooltip over the next month arrow
        this.setAttributes('button', 'Next month', this.translate.instant("calendar.nextMonth"));

        // Previous month: tooltip over the previous month arrow
        this.setAttributes('button', 'Previous month', this.translate.instant("calendar.prevMonth"));
    }

    private setAttributes(type: string, title: string, label: string) {
        // @ts-ignore
        const element = this.datepicker._cRef.location.nativeElement.querySelector(type + '[title="' + title + '"]');
        element.setAttribute('title', label);
        element.setAttribute('aria-label', label);
    }

}