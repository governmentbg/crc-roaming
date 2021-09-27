export class Polygon {
    id: number;
    leafletId: number;
    type: string;
    name: string;
    coordinates: number[][];

    constructor() {
        this.type = 'Polygon';
    }
}