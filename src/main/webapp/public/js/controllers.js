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

biking2Controllers.controller('IndexCtrl', function($scope, $http, $interval) {
    $http.get('/api/summary').success(function(data) {
	$scope.summary = data;
    });
    $scope.currentBikingPicture = '/img/default-biking-picture.jpg';

    $http.get('/api/bikingPictures').success(function(data) {
	var bikingPictures = data.randomize();

	var timer = $interval(function(count) {
	    var thePicture = bikingPictures[count % bikingPictures.length];
	    $scope.currentBikingPicture = '/api/bikingPictures/' + thePicture.id + '.jpg';
	    $scope.currentLinkToBikingPicture = thePicture.link;
	}, 5000);
	$scope.$on("$destroy", function() {
	    $interval.cancel(timer);
	});
    });
});

biking2Controllers.controller('MilagesCtrl', function($scope, $http, $modal, emptyChart) {
    $scope.currentYearConfig = $scope.historyConfig = emptyChart;
    $scope.alerts = [];

    $http.get('/api/charts/currentYear').success(function(data) {
	$scope.currentYearConfig = data;
    });

    $http.get('/api/charts/history').success(function(data) {
	$scope.historyConfig = data;
    });

    $http.get('/api/bikes').success(function(data) {
	$scope.bikes = data;
    });
    
    $scope.closeAlert = function(index) {
	$scope.alerts.splice(index, 1);
    };

    $scope.open = function() {
	if ($scope.bikes.length === 0)
	    $scope.alerts.push({type: 'danger', msg: 'Please define at least one bike'});
	else {
	    var modalInstance = $modal.open({
		templateUrl: '/partials/_new_milage.html',
		controller: 'AddNewMilageCtrl',
		scope: $scope
	    });

	    modalInstance.result.then(
		    function() {
			$http.get('/api/charts/currentYear').success(function(data) {
			    $scope.currentYearConfig.series = data.series;
			});
		    },
		    function() {
		    }
	    );
	}
    };
});

biking2Controllers.controller('TracksCtrl', function($scope, $http) {
    $http.get('/api/tracks').success(function(data) {
	$scope.tracks = data;
    });
});

biking2Controllers.controller('TrackCtrl', function($scope, $http, $q, $routeParams) {
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

    $q.all([$http.get('/api/tracks/' + $routeParams.id), $http.get('/api/home')]).then(function(values) {
	var track = values[0].data;
	var home = values[1].data;

	layerMarkers.addMarker(new OpenLayers.Marker(
		new OpenLayers.LonLat(home.longitude, home.latitude).transform(map.displayProjection, map.getProjectionObject()),
		new OpenLayers.Icon('http://simons.ac/images/favicon.png', new OpenLayers.Size(16, 16), new OpenLayers.Pixel(-(16 / 2), -16)))
		);

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

biking2Controllers.controller('AddNewMilageCtrl', function($scope, $modalInstance, $http) {

    $scope.milage = {
	bikeId: $scope.bikes[0].id,
	recordedOn: null,
	amount: null
    };

    $scope.milage.recordedOn = new Date();
    
    $scope.recordedOnOptions = {
	'year-format': "'yyyy'",
	'starting-day': 1,
	open: false,
	minimum: $scope.milage.recordedOn
    };

    $scope.openRecordedOn = function($event) {
	$event.preventDefault();
	$event.stopPropagation();
	$scope.recordedOnOptions.open = true;
    };

    $scope.cancel = function() {
	$modalInstance.dismiss('cancel');
    };

    $scope.submit = function() {
	$http({
	    method: 'POST',
	    url: '/api/bikes/' + $scope.milage.bikeId + '/milages',
	    data: $scope.milage
	}).success(function(data) {
	    $modalInstance.close(data);
	}).error(function(data, status) {
	    if (status === 400)
		$scope.badRequest = data;
	    else if(status === 404)
		$scope.badRequest = 'Please do not temper with this form.';
	});
    };
});