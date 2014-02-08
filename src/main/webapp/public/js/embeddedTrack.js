/* 
 * Copyright 2014 Michael J. Simons.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var map;
$(document).ready(function() {
    var _map = $('#map');
    if ($('#map').height() === 0)
	_map.css('height', _map.width() * 2 / 3);

    var home = {"longitude": $('#map').data('homeLongitude'), "latitude": $('#map').data('homeLatitude'), "description": "Home"};

    map = new OpenLayers.Map("map", {
	controls: [
	    new OpenLayers.Control.Navigation(),
	    new OpenLayers.Control.PanZoomBar(),
	    new OpenLayers.Control.LayerSwitcher(),
	    new OpenLayers.Control.Attribution()
	],
	projection: new OpenLayers.Projection("EPSG:900913"),
	displayProjection: new OpenLayers.Projection("EPSG:4326"),
	units: 'm'
    });

    map.addLayer(new OpenLayers.Layer.OSM.Mapnik("Mapnik"));
    map.addLayer(new OpenLayers.Layer.OSM.CycleMap("CycleMap"));
    var gpx = new OpenLayers.Layer.GML($('#map').data('trackName'), $('#map').data('trackUrl'), {
	format: OpenLayers.Format.GPX,
	style: {strokeColor: "red", strokeWidth: 5, strokeOpacity: 1.0},
	projection: new OpenLayers.Projection("EPSG:4326")
    });
    map.addLayer(gpx);
    var size = new OpenLayers.Size(16, 16);
    var offset = new OpenLayers.Pixel(-(size.w / 2), -size.h);
    var icon = new OpenLayers.Icon('http://simons.ac/images/favicon.png', size, offset);
    layerMarkers = new OpenLayers.Layer.Markers("Markers");
    map.addLayer(layerMarkers);
    layerMarkers.addMarker(new OpenLayers.Marker(new OpenLayers.LonLat(home.longitude, home.latitude).transform(map.displayProjection, map.getProjectionObject()), icon));

    bounds = new OpenLayers.Bounds();
    bounds.extend(new OpenLayers.LonLat(_map.data('trackMinlon'), _map.data('trackMinlat')));
    bounds.extend(new OpenLayers.LonLat(_map.data('trackMaxlon'), _map.data('trackMaxlat')));
    map.zoomToExtent(bounds.transform(map.displayProjection, map.getProjectionObject()));
});
