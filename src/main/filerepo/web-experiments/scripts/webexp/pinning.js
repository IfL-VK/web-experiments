
// The main module called by/for the "new" page.

define(function (require) {

    var d3          = require('d3')
    var leaflet     = require('leaflet')
    var common      = require('common')

    var control     = require('./controller/pinningCtrl')
    var model    = require('./model/pinningModel')

    var map                         // map leaflet reference
    var place_to_pin = {}           // configured place to pin geo-coordinates
    var memorize = { time: 15000 }  // configured time for memorization (trial)
    var trialId = -1


    // ------ Initialization of client-side data for pinning

    function init_pinning_page () {
        
        // 0 get trial id out of url
        trialId = common.parse_trial_id_from_resource_location()

        // 1 load user session
        init_user_view() // ### todo: load a users preferenced marker

        // 2 load trial config and then initialize pinning for this trial
        control.fetchTrialConfig(trialId, function (response) {
            
            // 2.1 initialize pinning page view model
            model.setTrialConfig(response)
            model.setMapConfig(response.map_config)
            model.setPlaces(response.place_config.items)
            model.setPlaceToPinId(response['trial_config']['place_to_pin'])
            
            // 2.2 initialize leaflet container according to map configuration
            initialize_map()
            init_task_description()
            
            // 2.3 init pinning according to configured trial condition
            // ... override default memo time with time for memo configured in trial
            var memo_seconds = model.getTrialConfig()['trial_config']['memo_seconds']
                memorize.time = (memo_seconds * 1000)

            // ... switch per trial condition
            var pinning_condition = model.getTrialConfig()['trial_config']['trial_condition']
            if (common.debug) console.log(" trial condition:", pinning_condition)
            
            if (pinning_condition === "") {
                if(common.verbose) console.log(" ... no pinning (" + trialId + ")")
                run_timer()
            } else if (pinning_condition === "webexp.config.pinning") {
                if(common.verbose) console.log(" ... pinning active - no timer (" + trialId + ")")
                initialize_pinning_features()
                // run_timer() // ### what if user has not pinned after timer ran out
                // my suggestion would be: run timer just after pinning was done 
            } else {
                throw Error("Unknown trial condition for pinning (\""+pinning_condition+"\"), Trial: " + trialId)
            }

        }, common.debug)

    }

    // --
    // ---- Pinning Map View ----
    // --

    function initialize_map() {

        // ------- Leaflet Map Setup -----

        var mapConfig = model.getMapConfig().childs
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
        /** if (fileName) {
            var northEast = map.getBounds().getNorthEast()
            var southWest = map.getBounds().getSouthWest()
                northEast.lat += 0.001
                northEast.lng += 0.001
                southWest.lat -= 0.001
            var imageUrl = '/filerepo/web-experiments/maps/' + fileName,
                // imageBounds = [[map.getBounds().getNorth(), map.getBounds().getEast()], [map.getBounds().getSouth(), map.getBounds().getEast()]] // ###
                imageBounds = L.latLngBounds(northEast, southWest)
            L.imageOverlay(imageUrl, imageBounds).addTo(map)
        } **/
    }

    function init_user_view () {
        control.fetchUser(function (data) {
            var username = data
            // GUI
            d3.select('.username').text(username)
            // OK
            model.setUsername(username)
        }, common.debug)
    }
    
    function init_task_description () {
        d3.select('i.place-to-pin').text(model.getNameOfPlaceToPin())
    }

    function initialize_pinning_features () {
        
        if(common.debug) console.log(" pinning: loaded places", model.getPlaces())
        if(common.debug) console.log(" pinning: initializing place to pin", model.getPlaceToPinId())
        
        check_place_to_pin_configuration()
        
        var featureGroup = L.featureGroup()
        var marker = L.marker([place_to_pin.lat, place_to_pin.lng])

        if (common.debug) marker.addTo(featureGroup)

        map.on('click', function (e) {
            if (common.verbose) console.log("  map clicked: " + e.latlng + " (vs.) " + place_to_pin.lat + ", " + place_to_pin.lng)
            if (is_click_nearby(e)) {
                if (common.verbose) console.log("  active control clicked - pinned")
                // ### fixme: do not at marker (if already present) again
                var marker = L.marker([place_to_pin.lat, place_to_pin.lng])
                    marker.addTo(featureGroup)
                run_timer()
            }
        })
        
        featureGroup.addTo(map)
    }
    
    function check_place_to_pin_configuration () {
        
        // 1 load place config by id from all places configured for this map id 
        var coordinates_to_pin = model.getCoordinatesOfPlaceToPin() // assume that placeToPin Id is set in model
        
        // 2 store coordinate for place to pin globally
        place_to_pin.lat = coordinates_to_pin['latitude']
        place_to_pin.lng = coordinates_to_pin['longitude']
        
        // 3 logged potential configuration errors to the browser console
        if (typeof coordinates_to_pin === "undefined") {
            throw Error ("Place with ID \"" + model.getPlaceToPinId() + "\" is not configured for this Map "
                + " (check the MapId in your places config file)!")
        } else { // Configuration - OK
            if (common.verbose) console.log("Place to pin configuration - OK")
        }
        if (!map.getBounds().contains(L.latLng(place_to_pin.lat, place_to_pin.lng))) {
            throw Error ("The configured coordinates for our \"place_to_pin\" "
                + " (Place "+model.getPlaceToPinId()+") are not within the viewport of "
                + " this map configuration (" +model.getMapConfigId()+ "). Please check Map Center Coordinates "
                + " and/or all coordinates in the place configs for this map.")
        }
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

    init_pinning_page()

});

