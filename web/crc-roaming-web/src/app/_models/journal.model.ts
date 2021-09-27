import { User } from "@models/user.model";

export class JournalModel {
    id: number;
    objType: string;
    operationType: string;
    editor: User;
    ts: string;
    initialState: string;
    newState: string;
}