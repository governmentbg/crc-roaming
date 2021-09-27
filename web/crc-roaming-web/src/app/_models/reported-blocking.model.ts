import { Operator } from "./operator.model";

export class ReportedBlocking {
    blockingId: number;
    userId: number;
    eventTs: string;
    operator: Operator;
}