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

biking2Controllers.controller('BikesCtrl', function($scope, $http, $modal) {
    $http.get('/api/bikes?all=true').success(function(data) {
	$scope.bikes = data;
    });

    $scope.openNewBikeDlg = function() {
	var modalInstance = $modal.open({
	    templateUrl: '/partials/_new_bike.html',
	    controller: 'AddNewBikeCtrl',
	    scope: $scope
	});

	modalInstance.result.then(
		function(newBike) {
		    $scope.bikes.push(newBike);
		},
		function() {
		}
	);
    };
});

biking2Controllers.controller('AddNewBikeCtrl', function($scope, $modalInstance, $http) {
    $scope.bike = {
	name: null,
	boughtOn: new Date(),
	color: null
    };

    $scope.boughtOnOptions = {
	'year-format': "'yyyy'",
	'starting-day': 1,
	open: false
    };

    $scope.openBoughtOn = function($event) {
	$event.preventDefault();
	$event.stopPropagation();
	$scope.boughtOnOptions.open = true;
    };

    $scope.cancel = function() {
	$modalInstance.dismiss('cancel');
    };

    $scope.submit = function() {
	$scope.submitting = true;
	$http({
	    method: 'POST',
	    url: '/api/bikes',
	    data: $scope.bike
	}).success(function(data) {
	    $scope.submitting = false;
	    $modalInstance.close(data);
	}).error(function(data, status) {
	    $scope.submitting = false;
	    if (status === 400)
		$scope.badRequest = data;
	    else if (status === 409)
		$scope.badRequest = 'The name is already used.';
	});
    };
});

biking2Controllers.controller('MilagesCtrl', function($scope, $http, $modal, emptyChart) {
    $scope.currentYearConfig = $scope.historyConfig = emptyChart;
    $scope.alerts = [];

    $http.get('/api/charts/currentYear').success(function(data) {
	$scope.currentYearConfig = data;
    });
    
    $http.get('/api/charts/monthlyAverage').success(function(data) {
	$scope.monthlyAverageConfig = data;
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

    $scope.openNewMilageDlg = function() {
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
			    $scope.currentYearConfig.userData = data.userData;
			});
			
			$http.get('/api/charts/monthlyAverage').success(function(data) {
			   $scope.monthlyAverageConfig = data;
			});
		    },
		    function() {
		    }
	    );
	}
    };
});

biking2Controllers.controller('AddNewMilageCtrl', function($scope, $modalInstance, $http) {

    $scope.milage = {
	bikeId: $scope.bikes[0].id,
	recordedOn: new Date(),
	amount: null
    };

    $scope.recordedOnOptions = {
	'year-format': "'yyyy'",
	'starting-day': 1,
	open: false
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
	$scope.submitting = true;
	$http({
	    method: 'POST',
	    url: '/api/bikes/' + $scope.milage.bikeId + '/milages',
	    data: $scope.milage
	}).success(function(data) {
	    $scope.submitting = false;
	    $modalInstance.close(data);
	}).error(function(data, status) {
	    $scope.submitting = false;
	    if (status === 400)
		$scope.badRequest = data;
	    else if (status === 404)
		$scope.badRequest = 'Please do not temper with this form.';
	});
    };
});

biking2Controllers.controller('GalleryCtrl', function($scope, $http, $modal) {
    $scope.imageInterval = 5000;
    $scope.allPictures = [];
    $scope.slides = [];

    $scope.reshuffle = function() {
	$scope.allPictures = $scope.allPictures.randomize();
	$scope.slides.length = 0;
	var max = Math.min(15, $scope.allPictures.length);
	for (var i = 0; i < max; ++i) {
	    $scope.slides.push({
		image: '/api/galleryPictures/' + $scope.allPictures[i].id + '.jpg',
		takenOn: $scope.allPictures[i].takenOn,
		text: $scope.allPictures[i].description
	    });
	}
    };

    $http.get('/api/galleryPictures').success(function(data) {
	$scope.allPictures = data;
	$scope.reshuffle();
    });

    $scope.openNewPictureDlg = function() {
	var modalInstance = $modal.open({
	    templateUrl: '/partials/_new_picture.html',
	    controller: 'AddNewPictureCtrl',
	    scope: $scope
	});

	modalInstance.result.then(
		function(newPicture) {
		    if ($scope.slides.length < 15) {
			$scope.slides.push({
			    image: '/api/galleryPictures/' + newPicture.id + '.jpg',
			    takenOn: newPicture.takenOn,
			    text: newPicture.description
			});
		    }
		},
		function() {
		}
	);
    };
});

biking2Controllers.controller('AddNewPictureCtrl', function($scope, $modalInstance, $http, $upload) {
    $scope.picture = {
	takenOn: null,
	description: null
    };
    $scope.imageData = null;

    $scope.onFileSelect = function($files) {
	$scope.imageData = $files[0];
    };

    $scope.takenOnOptions = {
	'year-format': "'yyyy'",
	'starting-day': 1,
	open: false
    };

    $scope.openTakenOn = function($event) {
	$event.preventDefault();
	$event.stopPropagation();
	$scope.takenOnOptions.open = true;
    };

    $scope.cancel = function() {
	$modalInstance.dismiss('cancel');
    };

    $scope.submit = function() {
	$scope.submitting = true;
	$upload.upload({
	    method: 'POST',
	    url: '/api/galleryPictures',
	    data: $scope.picture,
	    file: $scope.imageData,
	    fileFormDataName: 'imageData',
	    withCredentials: true,
	    formDataAppender: function(formData, key, val) {
		if (key !== null && key === 'takenOn')
		    formData.append(key, val.toISOString());
		else
		    formData.append(key, val);
	    }
	}).success(function(data) {
	    $scope.submitting = false;
	    $modalInstance.close(data);
	}).error(function() {
	    $scope.submitting = false;
	    $scope.badRequest = 'There\'s something wrong with your input, please check!';
	});
    };
});

biking2Controllers.controller('TracksCtrl', function($scope, $http, $modal) {
    $http.get('/api/tracks').success(function(data) {
	$scope.tracks = data;
    });

    $scope.openNewTrackDlg = function() {
	var modalInstance = $modal.open({
	    templateUrl: '/partials/_new_track.html',
	    controller: 'AddNewTrackCtrl',
	    scope: $scope
	});

	modalInstance.result.then(
		function(newTrack) {
		    $scope.tracks.push(newTrack);
		},
		function() {
		}
	);
    };
});

biking2Controllers.controller('AddNewTrackCtrl', function($scope, $modalInstance, $upload) {
    $scope.track = {
	name: null,
	coveredOn: new Date(),
	description: null,
	type: 'biking'
    };
    $scope.trackData = null;

    $scope.types = ['biking', 'running'];

    $scope.onFileSelect = function($files) {
	$scope.trackData = $files[0];
    };

    $scope.coveredOnOptions = {
	'year-format': "'yyyy'",
	'starting-day': 1,
	open: false
    };

    $scope.openCoveredOn = function($event) {
	$event.preventDefault();
	$event.stopPropagation();
	$scope.coveredOnOptions.open = true;
    };

    $scope.cancel = function() {
	$modalInstance.dismiss('cancel');
    };

    $scope.submit = function() {
	$scope.submitting = true;
	$upload.upload({
	    method: 'POST',
	    url: '/api/tracks',
	    data: $scope.track,
	    file: $scope.trackData,
	    fileFormDataName: 'trackData',
	    withCredentials: true,
	    formDataAppender: function(formData, key, val) {
		// Without that, val.toJSON() is used which adds "...
		if (key !== null && key === 'coveredOn')
		    formData.append(key, val.toISOString());
		else
		    formData.append(key, val);
	    }
	}).success(function(data) {
	    $scope.submitting = false;
	    $modalInstance.close(data);
	}).error(function(data, status) {
	    $scope.submitting = false;
	    if (status === 409)
		$scope.badRequest = 'A track with the given name on that date already exists.';
	    else
		$scope.badRequest = 'There\'s something wrong with your input, please check!';
	});
    };
});

biking2Controllers.controller('TrackCtrl', function($scope, $http, $q, $routeParams) {    
    $q.all([$http.get('/api/tracks/' + $routeParams.id), $http.get('/api/home')]).then(function(values) {
	$scope.track = values[0].data;	
	$scope.home = values[1].data;	
    });
});

biking2Controllers.controller('LocationCtrl', function($scope, $http) {
    $scope.locations = [];
    $http.get('/api/locations').success(function(data) {
	$scope.locations = data;	
	var stompClient = Stomp.over(new SockJS('/api/ws'));
	stompClient.connect({}, function() {	
	    stompClient.subscribe('/topic/currentLocation', function(greeting) {
		$scope.$apply(function() {
		    $scope.locations.push(JSON.parse(greeting.body));
		});
	    });
	});
    
	$scope.$on("$destroy", function() {	
	    stompClient.disconnect(function() {
	    });
	});
    });
});

biking2Controllers.controller('AboutCtrl', function($scope, $http, $filter, $interval) {
    $scope.refreshInterval = 30;
    $scope.memoryConfig = {
	options: {
	    chart: {
		type: 'area'
	    },
	    credits: {
		enabled: false
	    },
	    title: {
		text: 'Memory usage'
	    },
	    subtitle: {
                text: 'Refreshes every ' + $scope.refreshInterval + ' seconds'
            },
	    tooltip: {
		shared: true,
		valueSuffix: 'MiB'
	    },
	    plotOptions: {
		area: {
		    stacking: 'normal',
		    lineColor: '#666666',
		    lineWidth: 1,
		    marker: {
			lineWidth: 1,
			lineColor: '#666666'
		    }
		}
	    }
	},
	series: [{
		name: 'Free',
		data: []
	    }, {
		name: 'Used',
		data: []
	    }],
	loading: false,
	xAxis: {
	    categories: [],
	    tickmarkPlacement: 'on',
	    title: {
		enabled: false
	    }
	},
	yAxis: {
	    title: {
		text: 'MiB'
	    }
	}
    };

    $scope.refresh = function() {
	var formatBytes = function(bytes) {
	    return Math.round((bytes / Math.pow(1024, Math.floor(2))) * 10) / 10;
	};

	$http.get('/api/about').success(function(data) {
	    $scope.about = data;
	    $scope.humanizedUptime = nezasa.iso8601.Period.parseToString(data.vm.uptime, null, null, true);
	    var max = 10;
	    var cur = $scope.memoryConfig.series[0].data.length;

	    if (cur === max) {
		$scope.memoryConfig.series[0].data.splice(0, 1);
		$scope.memoryConfig.series[1].data.splice(0, 1);
		$scope.memoryConfig.xAxis.categories.splice(0, 1);
	    }
	    $scope.memoryConfig.series[0].data.push(formatBytes($scope.about.vm.freeMemory));
	    $scope.memoryConfig.series[1].data.push(formatBytes($scope.about.vm.usedMemory));
	    $scope.memoryConfig.xAxis.categories.push($filter('date')(new Date(), "HH:mm:ss"));
	});
    };

    var timer = $interval(function() {
	$scope.refresh();
    }, $scope.refreshInterval * 1000);
    $scope.$on("$destroy", function() {	
	$interval.cancel(timer);
    });
    $scope.refresh();
});