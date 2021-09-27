import { Component, OnInit } from '@angular/core';
import { PublicPortalService } from '@services/public-portal.service';
import { first } from 'rxjs/operators';

@Component({
    selector: 'about',
    templateUrl: './about.component.html'
})
export class AboutComponent implements OnInit {
    text: string;

    constructor(
        private publicService: PublicPortalService,
    ) { }

    ngOnInit() {
        this.publicService.getAboutText().pipe(first()).subscribe(t => {
            this.text = t.value;
        });
    }

}
