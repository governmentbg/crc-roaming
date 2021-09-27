import { Permission } from "./permission.model";

export class Role {
    id: number;
    name: string;
    description: string;
    permissions: Permission[];

    constructor() {
        this.id = null;
        this.name = null;
        this.description = null;
        this.permissions = null;
    }
}