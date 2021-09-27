import { Role } from "@models/role.model";

export class User {
    id: number;
    fullName: string;
    email: string;
    enabled: boolean;
    role: Role;
}