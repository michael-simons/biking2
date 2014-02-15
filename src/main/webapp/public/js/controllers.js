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

'use strict';

/* Controllers */

var biking2Controllers = angular
	.module('biking2Controllers', ['highcharts-ng'])
	.constant('emptyChart', {
	    options: {credits: {enabled: false}},
	    title: {text: ''},
	    loading: true
	});

biking2Controllers.controller('IndexCtrl', function($scope, $http) {
    $http.get('/api/summary').success(function(data) {
	$scope.summary = data;
    });
});

biking2Controllers.controller('MilagesCtrl', function($scope, $http, emptyChart) {
    $scope.currentYearConfig = $scope.historyConfig = emptyChart;

    $http.get('/api/charts/currentYear').success(function(data) {
	$scope.currentYearConfig = data;
    });

    $http.get('/api/charts/history').success(function(data) {
	$scope.historyConfig = data;
    });
});

biking2Controllers.controller('TracksCtrl', function($scope, $http) {
    $http.get('/api/tracks').success(function(data) {
	$scope.tracks = data;
    });
});

biking2Controllers.controller('TrackCtrl', function($scope, $http, $routeParams) {
    var map = new OpenLayers.Map("track-map", {
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

    var layerMarkers = new OpenLayers.Layer.Markers("Markers");
    map.addLayer(layerMarkers);
    layerMarkers.addMarker(new OpenLayers.Marker(
	    new OpenLayers.LonLat(6.179489185520004, 50.75144902272457).transform(map.displayProjection, map.getProjectionObject()), 
	    new OpenLayers.Icon('http://simons.ac/images/favicon.png', new OpenLayers.Size(16, 16), new OpenLayers.Pixel(-(16 / 2), -16)))
    );

    $http.get('/api/tracks/' + $routeParams.id).success(function(track) {
	$scope.track = track;
	var gpx = new OpenLayers.Layer.GML(track.name, "/tracks/" + track.id + ".gpx", {
	    format: OpenLayers.Format.GPX,
	    style: {strokeColor: "red", strokeWidth: 5, strokeOpacity: 1.0},
	    projection: new OpenLayers.Projection("EPSG:4326")
	});
	map.addLayer(gpx);
	var bounds = new OpenLayers.Bounds();

	bounds.extend(new OpenLayers.LonLat(track.minlon, track.minlat));
	bounds.extend(new OpenLayers.LonLat(track.maxlon, track.maxlat));

	map.zoomToExtent(bounds.transform(map.displayProjection, map.getProjectionObject()));
    });
});