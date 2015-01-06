
// The main module called by/for the "new" page.

define(function (require) {
    
    var d3          = require('d3')
    var leaflet     = require('leaflet')
    var common      = require('common')

    var newCtrl     = require('./controller/estimationCtrl')
    var newModel    = require('./model/estimationModel')

    var map                         // map leaflet reference
    var placeFrom = {}              // configured place from
    var placeTo = {}                // configured place to
    var memorize = { time: 15000 }  // configured time for memorization (trial)
    var trialId = -1


    // ------ Initialization of client-side data for estimation

    function init_estimation_page () {
        
        // 0 get trial id out of url
        trialId = common.parse_trial_id_from_resource_location()

        // 1 load user session
        init_user_view() // ### todo: load a users preferenced marker
        
        // 2 load trial config and then initialize pinning for this trial
        newCtrl.fetchTrialConfig(trialId, function (response) {
            
            // 2.1 initialize estimation page view model
            newModel.setTrialConfig(response)
            newModel.setMapConfig(response.map_config)
            newModel.setPlaces(response.place_config.items)
            // 2.2. ### prepare all estimations
            console.log(response)
            
            // 2.2 initialize leaflet container according to map configuration
            initialize_map("Trial1/Blank-Karte.png")
            init_task_description()
            
            // 2.3 init pinning according to configured trial condition
            // ... override default memo time with time for memo configured in trial
            var memo_seconds = newModel.getTrialConfig()['trial_config']['memo_seconds']
                memorize.time = (memo_seconds * 1000)

            // 3 initialize estimation features
            initialize_estimation_features()
            
        }, common.debug)

    }

    // --
    // ---- Estimation Map View ----
    // --

    function initialize_map(blank_image_path) { // ### refactor to base? there is a duplicate in pinning

        // ------- Leaflet Map Setup -----

        var mapConfig = newModel.getMapConfig().childs
        var mapId = mapConfig['de.akmiraketen.webexp.trial_map_id'].value
        if (common.debug) console.log("   init "+mapId+" config", mapConfig)
        var centerLat, centerLng, zoomLevel, fileName;
        try {
            centerLat = mapConfig['de.akmiraketen.webexp.trial_map_center_lat'].value
            centerLng = mapConfig['de.akmiraketen.webexp.trial_map_center_lng'].value
            zoomLevel = mapConfig['de.akmiraketen.webexp.trial_map_scale'].value
            fileName  = mapConfig['de.akmiraketen.webexp.trial_map_filename'].value
        } catch (error) {
            throw Error ("Map File config for " + mapId + " is missing a value.")
        }
        if (common.debug) console.log(" pinning: loaded map config for MapID:", mapId)
        // .. create a map in the "map" div, set the view to a given place and zoom
        map = L.map('map',  {
            dragging: false, touchZooom: false,
            scrollWheelZoom: false, doubleClickZoom: false,
            boxZoom: false, zoomControl: false, keyboard: false
        })
        // .. set viewport by the corresponding map file configuration for this trial
        map.setView([centerLat, centerLng], zoomLevel)
        // ### fixme: find maptile layer 
        // .. add an OpenStreetMap tile layer
        var tileLayer = L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            })
            tileLayer.addTo(map)
        // uncomment the following linces to use bitmap map-files instead of tiles
        // ### use blank screen
        if (blank_image_path) {
            var northEast = map.getBounds().getNorthEast()
            var southWest = map.getBounds().getSouthWest()
                northEast.lat += 0.001
                northEast.lng += 0.001
                southWest.lat -= 0.001
            var imageUrl = '/filerepo/web-experiments/maps/' + blank_image_path,
                // imageBounds = [[map.getBounds().getNorth(), map.getBounds().getEast()], [map.getBounds().getSouth(), map.getBounds().getEast()]] // ###
                imageBounds = L.latLngBounds(northEast, southWest)
            L.imageOverlay(imageUrl, imageBounds).addTo(map)
        }
    }


    // ---
    // ---- Estimation Screen ----
    // --

    function initialize_estimation_features () {

        var centerLat = 52.955304
        var centerLng = 8.326077
        
        /** var labelMarker = L.marker([centerLat, centerLng], {
                zIndexOffset: -1
            })
            labelMarker.bindLabel("Huntlosen", { 
                noHide: true, offset: [-40,15], zIndexOffset: -1
            })
            labelMarker.addTo(map) **/
        var startPoint = L.circle([centerLat, centerLng], 140, { fill: true, fillColor: 'black', weight: 5, color: 'gray', opacity: 1 })

        // create a red polyline from an arrays of LatLng points
        var pointA = L.latLng(centerLat, centerLng)
        // var pointB = L.latLng(centerLat, centerLng)
        var points = [pointA, pointA]
        var polyline = L.polyline(points, {
                color: 'grey', weight: 8, opacity: 1
                
            })
            // polyline.showExtremities('arrowM');
            
        var isDrawing = false
        
        var featureGroup = L.featureGroup()
            
            startPoint.on('mousedown', function(e) {
                updateEndPointOfPolyline(e)
                isDrawing = true
            })
            
            map.on('mousemove', function(e) {
                if (isDrawing) updateEndPointOfPolyline(e)
            })
            
            map.on('mouseup', function(e) {
                if (isDrawing) {
                    isDrawing = false
                    calculateDistance()
                }
            })
            
        function updateEndPointOfPolyline(e) {
            polyline.spliceLatLngs(polyline.getLatLngs().length-1, 1, e.latlng)
        }
        
        function calculateDistance() {
            // 
            var els = polyline.getLatLngs()
            // Returns the distance (in meters) to the given LatLng calculated 
                // using the Haversine formula. See description on
     	    // http://en.wikipedia.org/wiki/Haversine_formula
     	    var meters = Math.round(els[0].distanceTo(els[1]))
            // ### report values as results / write values into trial report
            set_task_description("Result: " + (meters / 1000).toFixed(1) + " Kilometer")
             	
        }

        polyline.addTo(featureGroup)
        startPoint.addTo(featureGroup)
        featureGroup.addTo(map)

    }

    function initialize_pinning_features () {

        place.lat = 52.991019
        place.lng = 8.272516

        var featureGroup = L.featureGroup()
        
        map.on('click', function (e) {
            if (isClickNearby(place, e)) {
                // console.log("Active control pressed")
                var marker = L.marker([place.lat, place.lng])
                    marker.addTo(featureGroup)
                runMemorizationTimer()
            }
        })
        featureGroup.addTo(map)
    }
    
    function init_task_description () {
        // ### 
        /** var placeFrom = -1
        var placeTo = -1
        d3.select('i.from-place').text(newModel.getNameOfPlace())
        d3.select('i.to-place').text(newModel.getNameOfPlace()) **/
    }

    function init_user_view () {
        newCtrl.fetchUser(function (data) {
            // 
            var username = data
            d3.select('.username').text(username)
            // OK
            newModel.setUsername(username)

        }, false)
    }
    
    // ------ Helper Methods
    
    function is_click_nearby(event) {
        var southWest = L.latLng(place_to_pin.lat - 0.005, place_to_pin.lng - 0.005)
        var northEast = L.latLng(place_to_pin.lat + 0.005, place_to_pin.lng + 0.005)
        var activeControl = L.latLngBounds(southWest, northEast)
        return activeControl.contains(event.latlng)
    }

    function run_timer(seconds) {
        if (typeof seconds === "undefined") 
        set_task_description("Task: Please memorize this map in the next " + (memorize.time / 1000) + " seconds")
        setTimeout(function (e) {
            window.document.location.href = "/web-exp/trial/" + trialId + "/estimation"
        }, memorize.time)
    
        if (common.verbose) console.log("  running timer for " +memorize.time / 1000+ " seconds")
    }
    
    function set_task_description (message) {
        document.getElementById("title").innerHTML = message 
    }

    // --- Run this script when it is called/loaded

    init_estimation_page()

});

