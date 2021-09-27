import { Injectable } from '@angular/core';
import { permissions } from '@env/config';
import { S_PERMS } from '@services/authentication.service';

@Injectable({ providedIn: 'root' })
export class PermissionService {
    private perms: string[];
    public all: any;

    constructor() {
        this.all = permissions;
        this.perms = [];
    }

    public hasAccess(perm: string): boolean {
        if (this.perms.length == 0) {
            const sp = sessionStorage.getItem(S_PERMS);
            if (sp) {
                sp.split(',').forEach(p => {
                    this.perms.push(p);
                });
            }
        }

        for (let p of this.perms) {
            if (p == perm) {
                return true
            }
        }

        return false;
    }

    public clear(): void {
        this.perms = [];
    }

}
