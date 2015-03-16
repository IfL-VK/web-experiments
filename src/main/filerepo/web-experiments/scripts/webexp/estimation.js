
// The main module called by/for the "new" page.

define(function (require) {
    
    var L               = require('leaflet'),
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
        "estimated_screen_coordinates": {
            "x": -1, "y" : -1
        },
        "real_screen_coordinates": {
            "x": -1, "y" : -1
        },
        "geo_coordinates": {
            "latitude": -1, "longitude" : -1
        }
    }
    
    var featureGroup = L.featureGroup()
    var startPoint = undefined
    var polyline = undefined

    var view_state  = "" // values may be "" or "pract"
    var estimationNr = 1

    // ------ Initialization of client-side data for estimation

    function init_estimation_page () {
        
        // 0 get trial id out of url
        trialId = common.parse_trial_id_from_resource_location()
        view_state = common.parse_view_state_from_page()

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

                // .. make sure all place details are configured (and thus loaded) for this map
                var fromPlaceCoordinates = model.getCoordinatesOfPlace(report.from_place_id)
                var toPlaceName = model.getNameOfPlace(report.to_place_id)
                check_places_for_estimation_configuration(fromPlaceCoordinates, toPlaceName)

                // 2.1.4 initialize leaflet container and task description according to map configuration
                initialize_map("Trial1/Blank-Karte.png", fromPlaceCoordinates)
                init_task_description(report.from_place_id, report.to_place_id)

                // 2.1.5 init pinning according to configured trial condition
                var memo_seconds = model.getTrialConfig()['trial_config']['memo_seconds']
                    // ... override default memo time if configured for trial
                    if (typeof memo_seconds !== "undefined") memorize.time = (memo_seconds * 1000)

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
        var centerLat, centerLng, zoomLevel, fileName;
        try {
            centerLat = placeToStartFrom['latitude']
            centerLng = placeToStartFrom['longitude']
            zoomLevel = mapConfig['de.akmiraketen.webexp.trial_map_scale'].value
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
        /** var tileLayer = L.tileLayer('http://api.tiles.mapbox.com/v4/malle.58740102/{z}/{x}/{y}.png?'
                + 'access_token=pk.eyJ1IjoibWFsbGUiLCJhIjoiRDZkTFJOTSJ9.6tEtxWpZ_mUwVCyjWVw9MQ ', {
                attribution: '&copy; Mapbox &amp; OpenStreetMap</a> contributors'
            })
            tileLayer.addTo(map) **/
        // ..
        if (blank_image_path) {
            var northEast = map.getBounds().getNorthEast()
            var southWest = map.getBounds().getSouthWest()
                northEast.lat += 0.001
                northEast.lng += 0.001
                southWest.lat -= 0.001
            var imageUrl = '/filerepo/web-experiments/maps/' + blank_image_path,
                imageBounds = L.latLngBounds(northEast, southWest)
            L.imageOverlay(imageUrl, imageBounds).addTo(map)
        }
        if (common.debug) console.log("   init " + mapId + " config, viewport: " + map.getBounds(), mapConfig)
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
            default:
                window.location.href = '/web-exp/nextpage'
        }
    }


    // ----  Estimation Interaction Setup ----

    function initialize_estimation_features (fromPlace) {

        var centerLat = fromPlace['latitude']
        var centerLng = fromPlace['longitude']
        startPoint = L.circle([centerLat, centerLng], 100, {
            fill: true, fillColor: 'black', weight: '4px', color: 'gray', opacity: 1 })
        // create a red polyline from an arrays of LatLng points
        var pointA = L.latLng(centerLat, centerLng)
        // var pointB = L.latLng(centerLat, centerLng)
        var points = [pointA, pointA]
        polyline = L.polyline(points, {
                color: 'grey', weight: '8px', opacity: 1
            })
            // ### external plugin .. polyline.showExtremities('arrowM');
            
        // relies on global var featureGroup
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
                    // stop timer
                    stop_reaction_interval(timerId)
                    // create report
                    var start = polyline._parts[0][0]
                    var end = polyline._parts[0][1]
                    var coordinateX = (end.x - start.x)
                    var coordinateY = (end.y - start.y)
                    if (common.debug) console.log("   line dropped at X: " + coordinateX + " Y:" + coordinateY)
                    set_estimated_screen_coordinates(coordinateX, coordinateY)
                    get_correct_line(true) // ### set_real_screen_coordinates()
                    // get certainty estimation score and set them to report object
                    init_certainty_submission(function () { // certainty submission done
                        // send report to be written to database
                        control.postEstimationReport(trialId, estimationNr, report, function(done) {
                            if (common.verbose) console.log("OK - Load next estimation ")
                            // redirecting to index page for grabbing next trial
                            if (view_state.indexOf("pract") !== -1) {
                                render_feedback_view()
                            } else {
                                window.document.location.reload()
                            }
                        }, function (error) {
                            console.warn("FAIL - Estimated coordinates could not be saved!", error)
                        }, false)
                    })
                }
            })
            
        function updateEndPointOfPolyline(e) {
            polyline.spliceLatLngs(polyline.getLatLngs().length-1, 1, e.latlng)
        }

    }
    
    function render_feedback_view () {
        // 
        var ortA = model.getNameOfPlace(report.from_place_id)
        var ortB = model.getNameOfPlace(report.to_place_id)
        var title = 'Um von ' + ortA + ' nach ' + ortB + ' zu kommen, m&uuml;sstest du folgenderma&szlig;en gehen:'
        set_task_description(title)
        // ..
        d3.select('#map').attr('style', 'display:block;')
        d3.select('.certainty-scale').attr('style', 'display:none;')
        // ..
        polyline.setStyle({opacity: 0.3})
        //
        get_correct_line(true)
        // .. move on after 5secs
        run_timer(5000, function (e) { window.document.location.reload() })

    }

    function get_correct_line (render) {
        // ..
        var placeA = model.getCoordinatesOfPlace(report.from_place_id)
        var placeB = model.getCoordinatesOfPlace(report.to_place_id)
        var otherpolyline = L.polyline(
            [L.latLng(placeA.latitude, placeA.longitude), L.latLng(placeB.latitude, placeB.longitude)], {
            color: 'orange', weight: '8px', opacity: 1
        })
        if (render) otherpolyline.addTo(featureGroup)
        var start = otherpolyline._parts[0][0]
        var end = otherpolyline._parts[0][1]
        set_real_screen_coordinates((end.x - start.x), (end.y - start.y))
    }

    function init_certainty_submission (callback) {
        // hide map
        d3.select('#map').attr('style', 'display:none;')
        // change page title
        var html = 'Wie sicher warst du dir bei deiner Schätzung?'
        set_task_description(html)
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
    }

    function init_task_description (fromId, toId) {
        d3.select('i.from-place').text(model.getNameOfPlace(fromId))
        d3.select('i.to-place').text(model.getNameOfPlace(toId))
    }

    function init_user_view () {
        control.fetchUser(function (data) {
            // 
            var username = data
            // d3.select('.username').text(username)
            // OK
            model.setUsername(username)

        }, false)
    }
    
    // ------ Helper Methods

    function check_places_for_estimation_configuration (fromCoords, toName) {
        //    are not (configured) for this map
        if (typeof fromCoords === "undefined" || typeof toName === "undefined") {
            throw Error ("Could not load the place_to_start from trial config. Proabably the Place "
                + "(with ID= "+report.from_place_id+") configured for this estimation is not configured as "
                + " a place for this map (MapID:" + model.getMapConfigId()+")")
        }

    }
    
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

    function set_estimated_screen_coordinates (x, y) {
        if (common.verbose) console.log(" set estimated screen coordinate: " + x + ";" + y)
        report.estimated_screen_coordinates.x = x
        report.estimated_screen_coordinates.y = y
    }

    function set_real_screen_coordinates (x, y) {
        if (common.verbose) console.log(" set real screen coordinate: " + x + ";" + y)
        report.real_screen_coordinates.x = x
        report.real_screen_coordinates.y = y
    }
    
    function set_estimated_distance (value) {
        console.log(" estimated distance was: " + value)
        report.estimated_distance = value
    }
    
    function set_task_description (message) {
        document.getElementById("title").innerHTML = message 
    }

    function run_timer(value, action_handler) {
        var milliseconds = 10000
        if (typeof value !== "undefined") milliseconds = value
        setTimeout(function (e) {
            if (typeof action_handler !== "undefined") action_handler() // run action
        }, milliseconds)
        if (common.verbose) console.log("  running timer for " +(milliseconds/1000)+ " seconds, then do: " + typeof action_handler)
    }

    return {
        init_page: init_estimation_page
    }

});

