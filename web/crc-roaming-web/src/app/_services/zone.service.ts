import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Polygon } from '@models/polygon.model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

const portalUrl     = environment.webApiUrl + '/portal';
const getZonesUrl   = portalUrl + '/roaming-zones';
const zonesUrl      = environment.webApiUrl + '/zones';
const addZoneUrl    = zonesUrl + '/add';
const editZoneUrl   = zonesUrl + '/edit/{id}';
const deleteZoneUrl = zonesUrl + '/delete/{id}';

@Injectable({ providedIn: 'root' })
export class ZoneService {

    constructor(private http: HttpClient) { }

    public addEditZone(poly: Polygon): Observable<any> {
        if (poly.id) {
            return this.http.post<any>(editZoneUrl.replace("{id}", poly.id.toString()), poly);
        } else {
            return this.http.post<number>(addZoneUrl, poly).pipe(map(savedId => {
                poly.id = savedId;
            }));
        }
    }

    public getZones(): Observable<Polygon[]> {
        return this.http.get<Polygon[]>(getZonesUrl);
    }

    public deleteZone(id: number) {
        return this.http.delete<any>(deleteZoneUrl.replace("{id}", id.toString()));
    }

}
