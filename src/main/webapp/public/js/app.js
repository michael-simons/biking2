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

var biking2 = angular
	.module('biking2', ['ngRoute', 'biking2Controllers'])
	.config(
	    function($routeProvider, $locationProvider) {
		$locationProvider.html5Mode(true);

		$routeProvider.
			when('/', {
			    templateUrl: 'partials/_index.html',
			    controller: 'IndexCtrl'
			}).
			when('/current-year', {
			    templateUrl: 'partials/_current-year.html',
			    controller: 'CurrentYearCtrl'
			}).
			when('/history', {
			    templateUrl: 'partials/_history.html',
			    controller: 'HistoryCtrl'
			}).
			when('/about', {
			    templateUrl: 'partials/_about.html'
			}).
			otherwise({
			    redirectTo: '/'
			});
	    }
	)
	.run(function($rootScope) {
	    $rootScope.currentYear = new Date().getFullYear();
	});
