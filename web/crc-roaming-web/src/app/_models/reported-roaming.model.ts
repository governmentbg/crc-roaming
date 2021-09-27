import { Region } from "@models/region.model";
import { Operator } from "./operator.model";

export class ReportedRoaming {
    roamingId: number;
    userId: number;
    eventTs: string;
    coordinates: string;
    latitude: number;
    longitude: number;
    operator: Operator;
    region: Region;
    os: string;
}