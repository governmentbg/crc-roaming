import { Injectable, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { skip } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class UIService implements OnInit {

    constructor(
        private toastr: ToastrService,
        private translate: TranslateService,
        private router: Router,
        private route: ActivatedRoute
    ) {
        translate.addLangs(['bg', 'en']);
        
        this.route.queryParams.pipe(skip(1)).subscribe(params => {
            let lang = params['lang'];
            if (lang && lang.match('bg|en')) {
                this.useLanguage(lang);
            } else {
                this.useLanguage('bg');
            }
        });
    }

    ngOnInit() {
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.activateMenu(event.url);
            }
        });
    }

    public useLanguage(language: string) {
        this.translate.use(language);
    }

    public getCurrentLang(): string {
        let lang = this.translate.currentLang;
        if (lang) return lang;

        return this.translate.defaultLang;
    }

    private activateMenu(routeString: string) {
        const navBar = document.getElementById('nav-bar');
        if (!navBar) return;

        const menuItems: any = navBar.getElementsByClassName('mkbl');
        for (let mi of menuItems) {
            mi.classList.remove('active');
            if (mi.getAttribute('routerLink') === routeString) {
                mi.classList.add('active');
            }
        }

        const ssIndex = routeString.indexOf('/', 1);
        const routeSubStr = routeString.substr(0, ssIndex);
        const mkblDD: any = navBar.getElementsByClassName('mkbldd');
        for (let dd of mkblDD) {
            dd.classList.remove('active');
            if (ssIndex > 0 && dd.getAttribute('mkbl-dd') === routeSubStr) {
                dd.classList.add('active');
            }
        }
    }

    public clearAllMessages() {
        this.toastr.clear();
    }

    public showSuccess(msgKey: string, params?: Object) {
        this.toastr.success(this.translate.instant(msgKey, params));
    }

    public showError(msgKey: string, params?: Object) {
        this.toastr.error(this.translate.instant(msgKey, params));
    }

    public showErrorNoTimeout(msgKey: string, params?: Object) {
        this.toastr.error(this.translate.instant(msgKey, params), null, { timeOut: 0 });
    }

    public showErrorNoTranslate(msg: string) {
        this.toastr.error(msg);
    }

    public showInfo(msgKey: string, params?: Object) {
        this.toastr.info(this.translate.instant(msgKey, params));
    }

    public showWarning(msgKey: string, params?: Object) {
        this.toastr.warning(this.translate.instant(msgKey, params));
    }

    public showWarningNoTimeout(msgKey: string, params?: Object) {
        this.toastr.warning(this.translate.instant(msgKey, params), null, { timeOut: 0 });
    }

}