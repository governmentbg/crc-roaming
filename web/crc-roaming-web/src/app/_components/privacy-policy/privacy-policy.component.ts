import { Component, OnInit } from '@angular/core';
import { PublicPortalService } from '@services/public-portal.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-privacy-policy',
  templateUrl: './privacy-policy.component.html'
})
export class PrivacyPolicyComponent implements OnInit {
    text: string;

    constructor(
        private publicService: PublicPortalService,
    ) { }

    ngOnInit() {
        this.publicService.getPrivacyPolicyText().pipe(first()).subscribe(t => {
            this.text = t.value;
        });
    }

}