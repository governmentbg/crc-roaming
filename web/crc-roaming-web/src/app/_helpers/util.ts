
import { Operator } from '@models/operator.model';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import * as L from 'leaflet';

export class Util {

    // Prepare dates for backend
    static dateToIsoString(date: NgbDateStruct): string {
      return date ? date.year + "-" + ('0' + date.month).slice(-2) + "-" + ('0' + date.day).slice(-2) : null;
    }

    static formatDate(date: NgbDateStruct): string {
      return date ? ('0' + date.day).slice(-2) + '.' + ('0' + date.month).slice(-2) + "." + date.year : null;
    }

    static filter(arr: any[], id: number) {
        for (let i = 0; i < arr.length; i++) {
            if (arr[i].id == id) {
                return arr[i];
            }
        }

        return null;
    }

    static filterOperatorsByCountry(operators: Operator[], countryId: number): Operator[] {
        let filtered: Operator[] = [];

        operators && operators.forEach(o => {
            if (o.country.id == countryId) {
                filtered.push(o);
            }
        })

        return filtered;
    }

    static buildMapDrawControl(editLayer: L.FeatureGroup): L.Control.Draw {
        let drawControl = new L.Control.Draw({
            draw: {
                polygon: false,
                polyline: false,
                rectangle: false,
                circle: false,
                marker: false,
                circlemarker: false,
            },
            edit: {
                featureGroup: editLayer,
                edit: false,
                remove: false,
            },
        });

        return drawControl;
    }

    static latLngsToCoordinates(latLngs: L.LatLng[]): number[][] {
        return L.GeoJSON.latLngsToCoords(latLngs, 1)[0];
    }

    static coordinatesToLatLngs(coordinates: number[][]): L.LatLng[] {
        return L.GeoJSON.coordsToLatLngs(coordinates);
    }

    static removeElement(array: any[], element: any) {
        const index = array.indexOf(element, 0);
        if (index > -1) {
            array.splice(index, 1);
        }
    }

    static getMonths(): number[] {
        let months: number[] = [];
        for (let i = 1; i <= 12; i++) {
            months.push(i);
        }
 
        return months;
    }
 
    static getYears(): number[] {
        let years: number[] = [];
        let thisYear = new Date().getFullYear();
        for (let i = 2020; i <= thisYear; i++) {
            years.push(i);
        }
 
        return years;
    }

}
