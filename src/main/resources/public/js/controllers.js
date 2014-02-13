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

var biking2Controllers = angular.module('biking2Controllers', ["highcharts-ng"]);

biking2Controllers.controller('main', function($scope, $http) {
    $scope.now = new Date();    
    $http.get('/api/summary').success(function(data) {
	$scope.summary = data;
    });
});

biking2Controllers.controller('charts', function($scope, $http) {
    $http.get('/api/charts/currentYear').success(function(data) {
	$scope.currentYearConfig = data;
    });
});