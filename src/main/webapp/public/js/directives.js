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

/* Directives */

/**
 * highcharts-ng
 * @version v0.0.4 - 2014-01-25
 * @link https://github.com/pablojim/highcharts-ng
 * @author Barry Fitzgerald <>
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
angular.module('highcharts-ng', []).directive('ngHighchart', function () {
  var indexOf = function (arr, find, i) {
    if (i === undefined)
      i = 0;
    if (i < 0)
      i += arr.length;
    if (i < 0)
      i = 0;
    for (var n = arr.length; i < n; i++)
      if (i in arr && arr[i] === find)
        return i;
    return -1;
  };
  function prependMethod(obj, method, func) {
    var original = obj[method];
    obj[method] = function () {
      var args = Array.prototype.slice.call(arguments);
      func.apply(this, args);
      if (original) {
        return original.apply(this, args);
      } else {
        return;
      }
    };
  }
  function deepExtend(destination, source) {
    for (var property in source) {
      if (source[property] && source[property].constructor && source[property].constructor === Object) {
        destination[property] = destination[property] || {};
        deepExtend(destination[property], source[property]);
      } else {
        destination[property] = source[property];
      }
    }
    return destination;
  }
  var seriesId = 0;
  var ensureIds = function (series) {
    angular.forEach(series, function (s) {
      if (!angular.isDefined(s.id)) {
        s.id = 'series-' + seriesId++;
      }
    });
  };
  var axisNames = [
      'xAxis',
      'yAxis'
    ];
  var getMergedOptions = function (scope, element, config) {
    var mergedOptions = {};
    var defaultOptions = {
        chart: { events: {} },
        title: {},
        subtitle: {},
        series: [],
        credits: {},
        plotOptions: {},
        navigator: { enabled: false }
      };
    if (config.options) {
      mergedOptions = deepExtend(defaultOptions, config.options);
    } else {
      mergedOptions = defaultOptions;
    }
    mergedOptions.chart.renderTo = element[0];
    angular.forEach(axisNames, function (axisName) {
      if (config[axisName]) {
        prependMethod(mergedOptions.chart.events, 'selection', function (e) {
          var thisChart = this;
          if (e[axisName]) {
            scope.$apply(function () {
              scope.config[axisName].currentMin = e[axisName][0].min;
              scope.config[axisName].currentMax = e[axisName][0].max;
            });
          } else {
            scope.$apply(function () {
              scope.config[axisName].currentMin = thisChart[axisName][0].dataMin;
              scope.config[axisName].currentMax = thisChart[axisName][0].dataMax;
            });
          }
        });
        prependMethod(mergedOptions.chart.events, 'addSeries', function (e) {
          scope.config[axisName].currentMin = this[axisName][0].min || scope.config[axisName].currentMin;
          scope.config[axisName].currentMax = this[axisName][0].max || scope.config[axisName].currentMax;
        });
        mergedOptions[axisName] = angular.copy(config[axisName]);
      }
    });
    if (config.title) {
      mergedOptions.title = config.title;
    }
    if (config.subtitle) {
      mergedOptions.subtitle = config.subtitle;
    }
    if (config.credits) {
      mergedOptions.credits = config.credits;
    }
    return mergedOptions;
  };
  var updateZoom = function (axis, modelAxis) {
    var extremes = axis.getExtremes();
    if (modelAxis.currentMin !== extremes.dataMin || modelAxis.currentMax !== extremes.dataMax) {
      axis.setExtremes(modelAxis.currentMin, modelAxis.currentMax, false);
    }
  };
  var processExtremes = function (chart, axis, axisName) {
    if (axis.currentMin || axis.currentMax) {
      chart[axisName][0].setExtremes(axis.currentMin, axis.currentMax, true);
    }
  };
  var chartOptionsWithoutEasyOptions = function (options) {
    return angular.extend({}, options, {
      data: null,
      visible: null
    });
  };
  var prevOptions = {};
  var processSeries = function (chart, series) {
    var ids = [];
    if (series) {
      ensureIds(series);
      angular.forEach(series, function (s) {
        ids.push(s.id);
        var chartSeries = chart.get(s.id);
        if (chartSeries) {
          if (!angular.equals(prevOptions[s.id], chartOptionsWithoutEasyOptions(s))) {
            chartSeries.update(angular.copy(s), false);
          } else {
            if (s.visible !== undefined && chartSeries.visible !== s.visible) {
              chartSeries.setVisible(s.visible, false);
            }
            if (chartSeries.options.data !== s.data) {
              chartSeries.setData(angular.copy(s.data), false);
            }
          }
        } else {
          chart.addSeries(angular.copy(s), false);
        }
        prevOptions[s.id] = chartOptionsWithoutEasyOptions(s);
      });
    }
    for (var i = chart.series.length - 1; i >= 0; i--) {
      var s = chart.series[i];
      if (indexOf(ids, s.options.id) < 0) {
        s.remove(false);
      }
    }
  };
  var initialiseChart = function (scope, element, config) {
    config = config || {};
    var mergedOptions = getMergedOptions(scope, element, config);
    var chart = config.useHighStocks ? new Highcharts.StockChart(mergedOptions) : new Highcharts.Chart(mergedOptions);
    for (var i = 0; i < axisNames.length; i++) {
      if (config[axisNames[i]]) {
        processExtremes(chart, config[axisNames[i]], axisNames[i]);
      }
    }
    processSeries(chart, config.series);
    if (config.loading) {
      chart.showLoading();
    }
    chart.redraw();
    return chart;
  };
  return {
    restrict: 'EAC',
    replace: true,
    template: '<div></div>',
    scope: { config: '=' },
    link: function (scope, element, attrs) {
      var chart = false;
      function initChart() {
        if (chart)
          chart.destroy();
        chart = initialiseChart(scope, element, scope.config);
      }
      initChart();
      scope.$watch('config.series', function (newSeries, oldSeries) {
        if (newSeries === oldSeries)
          return;
        processSeries(chart, newSeries);
        chart.redraw();
      }, true);
      scope.$watch('config.title', function (newTitle) {
        chart.setTitle(newTitle, true);
      }, true);
      scope.$watch('config.subtitle', function (newSubtitle) {
        chart.setTitle(true, newSubtitle);
      }, true);
      scope.$watch('config.loading', function (loading) {
        if (loading) {
          chart.showLoading();
        } else {
          chart.hideLoading();
        }
      });
      scope.$watch('config.credits.enabled', function (enabled) {
        if (enabled) {
          chart.credits.show();
        } else if (chart.credits) {
          chart.credits.hide();
        }
      });
      scope.$watch('config.useHighStocks', function (useHighStocks) {
        initChart();
      });
      angular.forEach(axisNames, function (axisName) {
        scope.$watch('config.' + axisName, function (newAxes, oldAxes) {
          if (newAxes === oldAxes)
            return;
          if (newAxes) {
            chart[axisName][0].update(newAxes, false);
            updateZoom(chart[axisName][0], angular.copy(newAxes));
            chart.redraw();
          }
        }, true);
      });
      scope.$watch('config.options', function (newOptions, oldOptions, scope) {
        if (newOptions === oldOptions)
          return;
        initChart();
      }, true);
      scope.$on('$destroy', function () {
        if (chart)
          chart.destroy();
        element.remove();
      });
    }
  };
});

angular
	.module('track-map-ng', [])
	.directive('ngTrackMap', [function () {
		return {
		    restrict: 'EA',
		    priority: -10,
		    link: function(scope, elem, attrs) {
			var projection = new OpenLayers.Projection("EPSG:3857");		
			var displayProjection = new OpenLayers.Projection("EPSG:4326");		
			var map = new OpenLayers.Map(elem[0], {
			    controls: [
				new OpenLayers.Control.Navigation(),
				new OpenLayers.Control.PanZoomBar(),
				new OpenLayers.Control.LayerSwitcher(),
				new OpenLayers.Control.Attribution()
			    ],
			    projection: projection,
			    displayProjection: displayProjection,
			    units: 'm',
			    center: new OpenLayers.LonLat([10.447683, 51.163375]).transform(displayProjection, projection),
			    layers: [
				new OpenLayers.Layer.OSM.Mapnik("Mapnik"),
				new OpenLayers.Layer.OSM.CycleMap("CycleMap"),
				new OpenLayers.Layer.Markers("Markers")
			    ],
			    zoom: 6
			});
			
			scope.$watch(attrs.home, function (value) {
			    if(value === undefined)
				return;
			    var home = value;
			    map.getLayersByName('Markers')[0].addMarker(
				    new OpenLayers.Marker(
					new OpenLayers.LonLat(home.longitude, home.latitude).transform(map.displayProjection, map.getProjectionObject()),
					new OpenLayers.Icon('http://simons.ac/images/favicon.png', new OpenLayers.Size(16, 16), new OpenLayers.Pixel(-(16 / 2), -16))
				    )
			    );
			});			
			
			scope.$watch(attrs.track, function (value) {
			    if(value === undefined)
				return;
			    var track = value;
			    
			    var oldLayer = map.getLayersByName(track.name);
			    if(oldLayer.length > 0) {
				map.removeLayer(oldLayer[0]);
			    }
			    var newLayer = new OpenLayers.Layer.GML(track.name, "/tracks/" + track.id + ".gpx", {
				format: OpenLayers.Format.GPX,
				style: {strokeColor: "red", strokeWidth: 5, strokeOpacity: 1.0},
				projection: new OpenLayers.Projection("EPSG:4326")
			    });
			    map.addLayer(newLayer);
			    map.raiseLayer(newLayer, -1);
			    
			    var bounds = new OpenLayers.Bounds();
			    bounds.extend(new OpenLayers.LonLat(track.minlon, track.minlat));
			    bounds.extend(new OpenLayers.LonLat(track.maxlon, track.maxlat));

			    map.zoomToExtent(bounds.transform(map.displayProjection, map.getProjectionObject()));			   			    
			});			
		    }
		};
	}]);