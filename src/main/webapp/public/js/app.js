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

/* App Module */

Array.prototype.randomize = function() {
    var i = this.length, j, temp;
    if(i===0)
	return this;
    while (--i) {
	j = Math.floor(Math.random() * (i - 1));
	temp = this[i];
	this[i] = this[j];
	this[j] = temp;
    }
    return this;
};

var biking2 = angular
	.module('biking2', [
	    'ngRoute', 
	    'angularFileUpload', 
	    'ui.bootstrap', 
	    'track-map-ng',	    
	    'biking2Controllers'
	])
	.directive('ngActiveTab', ['$location', function($location) {
	    return {
		link: function postLink(scope, element, attrs) {
		    scope.$on("$routeChangeSuccess", function(event, current, previous) {
			// this var grabs the tab-level off the attribute, or defaults to 1
			var pathLevel = attrs.activeTab || 1,
				// this var finds what the path is at the level specified
				pathToCheck = $location.path().split('/')[pathLevel],
				// this var finds grabs the same level of the href attribute
				tabLink = attrs.href.split('/')[pathLevel];

			if (pathToCheck === tabLink) {
			    element.parent().addClass("active");
			} else {
			    element.parent().removeClass("active");
			}
		    });
		}
	    };
	}])
	.config(['$routeProvider', '$locationProvider', 
		function($routeProvider, $locationProvider) {
		    $locationProvider.html5Mode(true);

		    $routeProvider.
			    when('/', {
				templateUrl: '/partials/_index.html',
				controller: 'IndexCtrl'
			    }).
			    when('/bikes', {
				templateUrl: '/partials/_bikes.html',
				controller: 'BikesCtrl'
			    }).
			    when('/milages', {
				templateUrl: '/partials/_milages.html',
				controller: 'MilagesCtrl'
			    }).
			    when('/gallery', {
				templateUrl: '/partials/_gallery.html',
				controller: 'GalleryCtrl'
			    }).
			    when('/tracks', {
				templateUrl: '/partials/_tracks.html',
				controller: 'TracksCtrl'
			    }).
			    when('/tracks/:id', {
				templateUrl: '/partials/_track.html',
				controller: 'TrackCtrl'
			    }).
			    when('/location', {
				templateUrl: '/partials/_location.html',
				controller: 'LocationCtrl'
			    }).
			    when('/about', {
				templateUrl: '/partials/_about.html',
				controller: 'AboutCtrl'
			    }).
			    otherwise({
				redirectTo: '/'
			    });
		}
	])
	.filter('bytes', function() {
	    return function(bytes, precision) {
		if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
		if (bytes === 0) return '0 bytes';
		if (typeof precision === 'undefined') precision = 1;
		var units = ['bytes', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB'],
			number = Math.floor(Math.log(bytes) / Math.log(1024));
		return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) +  '' + units[number];
	    };
	})
	.filter('kibibytes', function() {
	    return function(bytes, precision) {
		if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
		if (bytes === 0) return '0 bytes';
		if (typeof precision === 'undefined') precision = 1;
		var units = ['KiB', 'MiB', 'GiB', 'TiB', 'PiB'],
			number = Math.floor(Math.log(bytes) / Math.log(1024));
		return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) +  '' + units[number];
	    };
	})
	.run(['$rootScope', function($rootScope) {
	    $rootScope.currentYear = new Date().getFullYear();
	}]);    
