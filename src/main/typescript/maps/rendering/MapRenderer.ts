import * as L from 'leaflet';
import { Facility, MapData } from "../types/dataTypes";


export class MapRenderer {
    private map: L.Map | undefined;

    public renderMap(mapData: MapData): void {
        this.initMap();
        if (this.map === undefined) {
            console.error("Map not initialized");
            return;
        }

        this.map.eachLayer((layer) => {
            if (layer instanceof L.Marker) {
                this.map.removeLayer(layer);
            }
        });

        mapData.facilities.forEach((facility) => {
            if (facility.latitude && facility.longitude) {
                const marker = L.marker([facility.latitude, facility.longitude]).addTo(this.map);
                marker.bindPopup(this.generatePopupContent(facility));
            }
        });
    }

    private initMap(): void {
        this.map = L.map("map").setView([0, 0], 2);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: 'Map data © <a href="https://openstreetmap.org">OpenStreetMap</a> contributors',
            maxZoom: 18,
        }).addTo(this.map);
    }
    
    private generatePopupContent(facility: Facility): string {
        return `<b>${facility.name}</b><br>Type: ${facility.type}<br>`;
    }

}