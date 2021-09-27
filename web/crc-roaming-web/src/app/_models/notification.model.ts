import { User } from "@models/user.model";

export class Notification {
    id: number;
    createdAt: string;
    sentAt: string;
    sent: boolean;
    subject: string;
    body: string;
    createdBy: User;
}