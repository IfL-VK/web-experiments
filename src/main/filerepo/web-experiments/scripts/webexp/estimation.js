
// The main module called by/for the "new" page.

define(function (require) {
    
    var d3              = require('d3'),
        leaflet         = require('leaflet'),
        leaflet_label   = require('leaflet_label'),
        common          = require('common'),
        control         = require('./controller/estimationCtrl'),
        model           = require('./model/estimationModel')

    var map                         // map leaflet reference
    var placeFrom = {}              // configured place from
    var placeTo = {}                // configured place to
    var memorize = { time: 15000 }  // configured time for memorization (trial)
    var trialId = -1

    var timerId = setInterval(count_to_start_time, 500)
    var report = {
        "from_place_id": 0,
        "to_place_id": 0,
        "to_start_time": 0,
        "estimation_time": 0,
        "estimated_distance": 0,
        "certainty": -1,
        "geo_coordinates": {
            "latitude": -1, "longitude" : -1
        }
    }
    
    var estimationNr = 1

    // ------ Initialization of client-side data for estimation

    function init_estimation_page () {
        
        // 0 get trial id out of url
        trialId = common.parse_trial_id_from_resource_location()

        // 1 load user session
        init_user_view() // ### todo: load a users preferenced marker
        
        // 2 load trial config and then initialize pinning for this trial
        control.fetchTrialConfig(trialId, function (response) {
            
            // 2.1 initialize estimation page view model
            model.setTrialConfig(response)
            model.setMapConfig(response.map_config)
            model.setPlaces(response.place_config.items)
            model.setEstimations(response)
            
            // 2.1.2 get next estimation number for this trial and user
            control.fetchNextEstimationNr(trialId, function (nr) {
                
                estimationNr = nr
                
                // 2.1.3 initialize place coordinates for current estimation
                initialize_current_place_coordinates(estimationNr)

                // .. maeke sure all place details are configured (and thus loaded) for this map
                var fromPlaceCoordinates = model.getCoordinatesOfPlace(report.from_place_id)
                var toPlaceName = model.getNameOfPlace(report.to_place_id)
                // .. catch a possible mismatch if places used in this estimation config are not assigned 
                //    are not (configured) for this map
                if (typeof fromPlaceCoordinates === "undefined" || typeof toPlaceName === "undefined") {
                    throw Error ("Could not load the place_to_start from trial config. Proabably the Place "
                        + "(with ID= "+report.from_place_id+") configured for this estimation is not configured as "
                        + " a place for this map (MapID:" + model.getMapConfigId()+")")
                }

                // 2.1.4 initialize leaflet container and task description according to map configuration
                initialize_map("Trial1/Blank-Karte.png", fromPlaceCoordinates)
                init_task_description(report.from_place_id, report.to_place_id)

                // 2.1.5 init pinning according to configured trial condition
                // ... override default memo time with time for memo configured in trial
                var memo_seconds = model.getTrialConfig()['trial_config']['memo_seconds']
                    memorize.time = (memo_seconds * 1000)

                // 2.1.6 initialize estimation features
                initialize_estimation_features(fromPlaceCoordinates)
                
                
            }, function (error) { console.warn(error) }, common.debug)
            
        }, common.debug)

    }

    // --- Initialization of Leaflets Map View

    function initialize_map(blank_image_path, placeToStartFrom) { // ### refactor to base? there is a duplicate in pinning

        // ------- Leaflet Map Setup -----
        
        var mapConfig = model.getMapConfig().childs
        var mapId = mapConfig['de.akmiraketen.webexp.trial_map_id'].value
        if (common.debug) console.log("   init "+mapId+" config", mapConfig)
        var centerLat, centerLng, zoomLevel, fileName;
        try {
            centerLat = placeToStartFrom['latitude']
            centerLng = placeToStartFrom['longitude']
            zoomLevel = mapConfig['de.akmiraketen.webexp.trial_map_scale'].value
            console.log("Zoomlevel: " + zoomLevel)
            fileName  = mapConfig['de.akmiraketen.webexp.trial_map_filename'].value
        } catch (error) {
            console.log(error)
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
        // ### use blank screen
        /** if (blank_image_path) {
            var northEast = map.getBounds().getNorthEast()
            var southWest = map.getBounds().getSouthWest()
                northEast.lat += 0.001
                northEast.lng += 0.001
                southWest.lat -= 0.001
            var imageUrl = '/filerepo/web-experiments/maps/' + blank_image_path,
                // imageBounds = [[map.getBounds().getNorth(), map.getBounds().getEast()], [map.getBounds().getSouth(), map.getBounds().getEast()]] // ###
                imageBounds = L.latLngBounds(northEast, southWest)
            L.imageOverlay(imageUrl, imageBounds).addTo(map)
        } **/
    }
    
    function initialize_current_place_coordinates(estimation_nr) {
        switch (estimation_nr) {
            case 1:
                report.from_place_id = model.getFromPlaceOne()
                report.to_place_id = model.getToPlaceOne()
                break
            case 2:
                report.from_place_id = model.getFromPlaceTwo()
                report.to_place_id = model.getToPlaceTwo()
                break
            case 3:
                report.from_place_id = model.getFromPlaceThree()
                report.to_place_id = model.getToPlaceThree()
                break
            case 4:
                report.from_place_id = model.getFromPlaceFour()
                report.to_place_id = model.getToPlaceFour()
                break
            case 5:
                report.from_place_id = model.getFromPlaceFive()
                report.to_place_id = model.getToPlaceFive()
                break
            case -1: // no unseen trial (id) left for requesting user
                window.location.href = '/web-exp/finish'
            default: 
                window.location.href = '/web-exp/nextpage'
        }
    }


    // ----  Estimation Interaction Setup ----

    function initialize_estimation_features (fromPlace) {

        var centerLat = fromPlace['latitude']
        var centerLng = fromPlace['longitude']
        /** var labelMarker = L.marker([centerLat, centerLng], {
                zIndexOffset: -1
            })
            labelMarker.bindLabel("Huntlosen", { 
                noHide: true, offset: [-40,15], zIndexOffset: -1
            })
            labelMarker.addTo(map) **/
        var startPoint = L.circle([centerLat, centerLng], 100, {
            fill: true, fillColor: 'black', weight: 4, color: 'gray', opacity: 1 })
        // create a red polyline from an arrays of LatLng points
        var pointA = L.latLng(centerLat, centerLng)
        // var pointB = L.latLng(centerLat, centerLng)
        var points = [pointA, pointA]
        var polyline = L.polyline(points, {
                color: 'grey', weight: 8, opacity: 1
                
            })
            // ### external plugin .. polyline.showExtremities('arrowM');
            
        var featureGroup = L.featureGroup()
            polyline.addTo(featureGroup)
            startPoint.addTo(featureGroup)
            featureGroup.addTo(map)
        

        // --- estimation interaction handler ---
        
        var isDrawing = false
        
            // ---- estimation starts
            
            startPoint.on('mousedown', function(e) {
                updateEndPointOfPolyline(e)
                isDrawing = true
                stop_reaction_interval(timerId)
                timerId = setInterval(count_estimation_time, 100)
            })
            
            map.on('mousemove', function(e) {
                if (isDrawing) updateEndPointOfPolyline(e)
            })
            
            // ---- estimation finishes
            
            map.on('mouseup', function(e) {
                if (isDrawing) { // estimation finished
                    isDrawing = false
                    stop_reaction_interval(timerId) // stop timer
                    var estimatedCoordinates = calculateDistance() // gets values
                        set_geo_coordinates(estimatedCoordinates) // sets values in report object
                        // get certainty estimation score and set them to report object
                        init_certainty_submission(function () { // certainty submission done
                            // save all values in report object to database
                            control.postEstimationReport(trialId, estimationNr, report, function(done) {
                                console.log("OK - Load next estimation ")
                                // redirecting to index page for grabbing next trial
                                window.document.location.reload() // ### do better
                            }, function (error) {
                                console.log("FAIL - Estimated coordinates could not be saved!")
                            }, false)
                        })
                }
            })
            
        function updateEndPointOfPolyline(e) {
            polyline.spliceLatLngs(polyline.getLatLngs().length-1, 1, e.latlng)
        }
        
        function calculateDistance() {
            // 
            var els = polyline.getLatLngs()
            var fromPlace = els[0]
            var toPlace = els[1]
            // Returns the distance (in meters) to the given LatLng calculated 
                // using the Haversine formula. See description on
     	    // http://en.wikipedia.org/wiki/Haversine_formula
     	    var meters = Math.round(fromPlace.distanceTo(toPlace))
            set_estimated_distance(meters)
            // ### report values as results / write values into trial report
            return toPlace
        }

    }
    
    function init_certainty_submission (callback) {
        // hide map
        d3.select('#map').attr('style', 'display:none;')
        // change page title
        set_task_description('Wie sicher warst du dir bei deiner Schätzung?')
        // show certainty scale
        var element = d3.select('.certainty-scale')
            element.attr('style', 'display:block;')
        // append possible values
        var header = '<div class="header"><span class="first">Sehr unsicher</span>'
            + '<span class="last">Sehr sicher</span></div>'
        var options = '<label><input class="btn" type="radio" name="rating" value="0"></input></label>'
            + '<label><input class="btn" type="radio" name="rating" value="1"></input></label>'
            + '<label><input class="btn" type="radio" name="rating" value="2"></input></label>'
            + '<label><input class="btn" type="radio" name="rating" value="3"></input></label>'
            + '<label><input class="btn" type="radio" name="rating" value="4"></input></label>'
            + '<label><input class="btn" type="radio" name="rating" value="5"></input></label>'
            + '<label><input class="btn" type="radio" name="rating" value="6"></input></label>'
        element.html(header + options)
        element.selectAll('input.btn').on('click', function () {
            set_certainty_value(parseInt(this.value))
            callback()
        })
        // 
    }

    function init_task_description (fromId, toId) {
        d3.select('i.from-place').text(model.getNameOfPlace(fromId))
        d3.select('i.to-place').text(model.getNameOfPlace(toId))
    }

    function init_user_view () {
        control.fetchUser(function (data) {
            // 
            var username = data
            d3.select('.username').text(username)
            // OK
            model.setUsername(username)

        }, false)
    }
    
    // ------ Helper Methods
    
    function count_estimation_time() {
        report.estimation_time += 100
    }
    
    function count_to_start_time() {
        report.to_start_time += 500
    }
    
    function stop_reaction_interval(intervalId) {
        if (common.verbose) console.log(" reaction time was: " + report.to_start_time 
                + " and " + report.estimation_time)
        clearInterval(intervalId)
    }
    
    function set_certainty_value(value) {
        report.certainty = value
    }
    
    function set_geo_coordinates (object) {
        if (common.verbose) console.log(" estimated coordinate was: " + object)
        report.geo_coordinates.latitude = object.lat
        report.geo_coordinates.longitude = object.lng
    }
    
    function set_estimated_distance (value) {
        console.log(" estimated distance was: " + value)
        report.estimated_distance = value
    }
    
    function set_task_description (message) {
        document.getElementById("title").innerHTML = message 
    }

    // --- Run this script when it is called/loaded

    init_estimation_page()

});

