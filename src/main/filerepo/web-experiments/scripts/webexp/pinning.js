
// The main module called by/for the "new" page.

define(function (require) {

    var d3              = require('d3'),
        some_L          = require('leaflet'),
        some_leaflet    = require('leaflet_label'),
        common          = require('common'),
        control         = require('./controller/pinningCtrl'),
        model           = require('./model/pinningModel')

    var map                         // map leaflet reference
    var place_to_pin = {}           // configured place to pin geo-coordinates
    var memorize = { time: 15000 }  // configured time for memorization (trial)
    var trialId = -1
    
    var timerId = setInterval(count_reaction_time, 500)
    var report = {
        "count_click_outside": 0,
        "reaction_time": 0,
        "geo_coordinates": {
            "latitude": -1, "longitude" : -1
        }
    }

    var pinned_already = false

    // ------ Initialization of client-side data for pinning

    function init_pinning_page () {
        
        // 0 get trial id out of url
        trialId = common.parse_trial_id_from_resource_location()
        if (trialId === -1) throw Error("No more trials available for this VP - You're done.")

        // 1 load user session
        init_user_view() // ### todo: load a users preferenced marker

        // 2 load trial config and then initialize pinning for this trial
        control.fetchTrialConfig(trialId, function (response) {
            
            // 2.1 initialize pinning page view model
            model.setTrialConfig(response)
            model.setMapConfig(response.map_config)
            model.setPlaces(response.place_config.items)
            model.setPlaceToPinId(response['trial_config']['place_to_pin'])
            
            init_task_description()
            
            // 2.2 init pinning according to configured trial condition
            // ... override default memo time with time for memo configured in trial
            var memo_seconds = model.getTrialConfig()['trial_config']['memo_seconds']
                memorize.time = (memo_seconds * 1000)

            // 3 .. load participant data
            control.fetchParticipant(function (data) {
                var icon_path = data['selected_marker_path']
                // 2.2 initialize leaflet container according to map configuration
                initialize_map()
                // ... switch per trial condition
                var pinning_condition = model.getTrialConfig()['trial_config']['trial_condition']
                if (common.debug) console.log(" trial condition:", pinning_condition)
                if (pinning_condition === "webexp.config.no_pinning") {
                    if(common.verbose) console.log(" ... no pinning (" + trialId + ")")
                    run_timer()
                } else if (pinning_condition === "webexp.config.pinning") {
                    if(common.verbose) console.log(" ... pinning active - no timer (" + trialId + ")")
                    initialize_pinning_features(icon_path)
                    // run_timer() // ### what if user has not pinned after timer ran out
                    // my suggestion would be: run timer just after pinning was done
                } else {
                    throw Error("Unknown trial condition for pinning (\""+pinning_condition+"\"), Trial: " + trialId)
                }

                // 4 show place name
                init_place_labels()

            }, function (error) {
                console.warn("Error loading participant ..")
            }, common.debug)

        }, common.debug)
        
        // 3 do mark trial as seen by this VP (logged in user)
        control.doMarkTrialAsSeen(trialId, function (response) {
            // console.log("trialID:" + trialId  + "; response: ", response)
            var loadedTrialId = parseInt(response.response)
            if (loadedTrialId === 1) {
                if (common.verbose) console.log("OK - Trial marked as seen!")
            } else if (trialId !== loadedTrialId) {
                if (common.verbose) console.log(" Trial already seen because server says we should move to next-trial: " + loadedTrialId )
                window.location.href = '/web-exp/trial/' + loadedTrialId + '/pinning'
            }
        }, function (error) {
            throw Error("Trial could not be marked as seen: " + error.status)
        })

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
            console.log("Zoomlevel: " + zoomLevel)
            fileName  = mapConfig['de.akmiraketen.webexp.trial_map_filename'].value
        } catch (error) {
            throw Error ("Map File config for " + mapId + " is missing a value.")
        }
        if (common.debug) console.log(" pinning: loaded map config for MapID:", mapId)
        // .. create a map in the "map" div, set the view to a given place and zoom
        map = L.map('map',  {
            dragging: false, touchZooom: false,
            scrollWheelZoom: false, doubleClickZoom: false,
            boxZoom: false, zoomControl: false, keyboard: false,
            attributionControl: false
        })
        // .. set viewport by the corresponding map file configuration for this trial
        map.setView([centerLat, centerLng], zoomLevel)
        // ### fixme: find maptile layer 
        // .. add an OpenStreetMap tile layer
        var tileLayer = L.tileLayer('http://api.tiles.mapbox.com/v4/malle.2823bf39/{z}/{x}/{y}.png?'
                + 'access_token=pk.eyJ1IjoibWFsbGUiLCJhIjoiRDZkTFJOTSJ9.6tEtxWpZ_mUwVCyjWVw9MQ ', {
                attribution: '&copy; Mapbox &amp; OpenStreetMap</a> contributors'
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

    function init_place_labels () {
        for (var i = 0; i < model.getPlaces().length; i++) {
            var place = model.getPlaces()[i]
            var lat = place.childs['de.akmiraketen.webexp.place_latitude'].value
            var lng = place.childs['de.akmiraketen.webexp.place_longitude'].value
            var name = place.childs['de.akmiraketen.webexp.place_name'].value
            /* var blankIcon = L.icon({
                iconUrl: '/de.akmiraketen.web-experiments/images/circle20-grey.png', iconSize: [15, 15],
                labelAnchor: [3, 0] // as I want the label to appear 2px past the icon (10 + 2 - 6)
            }) **/
            
            var NewIcon = L.Icon.Label.extend({
                options: {
                    iconUrl: '/de.akmiraketen.web-experiments/images/circle20-grey.png', shadowUrl: null,
                    iconSize: new L.Point(10, 10), iconAnchor: new L.Point(10, 5),
                    labelAnchor: new L.Point(17, -4), wrapperAnchor: new L.Point(15, -0)
                }
            });
            var m = L.marker([lat, lng], { draggable: false, icon: new NewIcon({ labelText: name }) })
            // if (!m.hasOwnProperty('bindLabel')) throw Error("Laeflet label plugin was not initialized correctly..")
                // m.bindLabel(name, { noHide: true })
                m.addTo(map)
                // m.showLabel();
        }
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

    function initialize_pinning_features (icon_path) {
        
        if(common.debug) console.log(" pinning: loaded places", model.getPlaces())
        if(common.debug) console.log(" pinning: initializing place to pin", model.getPlaceToPinId())
        
        check_place_to_pin_configuration()
        
        var featureGroup = L.featureGroup()
        // var marker = L.marker([place_to_pin.lat, place_to_pin.lng])
        // if (common.debug) marker.addTo(featureGroup)

        var personalIcon = undefined
        if (icon_path) {
            /* iconAnchor:   [24, 24] *shadowUrl: 'leaf-shadow.png', shadowSize: [50, 64],
             * shadowAnchor: [4, 62], popupAnchor:  [-3, -76] **/
            personalIcon = L.icon({
                iconUrl: '/filerepo/' + icon_path, iconSize: [24, 24]
            });
        }

        map.on('click', function (e) {
            if (common.verbose) console.log("  map clicked: " + e.latlng + " (vs.) " + place_to_pin.lat + ", " + place_to_pin.lng)
            if (is_click_nearby(e)) {
                if (common.verbose) console.log(" active control clicked - pinned")
                // do set values into report
                if (!pinned_already) {
                    set_geo_coordinates(e.latlng)
                    stop_reaction_interval(timerId)
                    control.postPinningReport(trialId, report, undefined, function (error) {
                        console.warn(error)
                    }, common.debug)
                    // ### fixme: do not at marker (if already present) again
                    var marker = undefined
                    if (icon_path) {
                        // marker = L.marker([e.latlng.lat, e.latlng.lng], {icon: personalIcon})
                        marker = L.marker([place_to_pin.lat, place_to_pin.lng], {icon: personalIcon})
                        if (common.debug) console.log("using personal icon from " + icon_path)
                    } else {
                        // marker = L.marker([e.latlng.lat, e.latlng.lng])
                        marker = L.marker([place_to_pin.lat, place_to_pin.lng], {icon: personalIcon})
                    }
                    marker.addTo(featureGroup)
                    run_timer()
                }
                pinned_already = true
            } else {
                click_count_outside_increase()
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
    
    function click_count_outside_increase() {
        report.count_click_outside++
    }
    
    function count_reaction_time() {
        report.reaction_time += 500
    }
    
    function stop_reaction_interval(intervalId) {
        console.log(" reaction time was: " + report.reaction_time)
        clearInterval(intervalId)
    }
    
    function set_geo_coordinates (object) {
        console.log(" click count outside was: " + report.count_click_outside)
        report.geo_coordinates.latitude = object.lat
        report.geo_coordinates.longitude = object.lng
    }

    // --- Run this script when it is called/loaded

    init_pinning_page()

});

