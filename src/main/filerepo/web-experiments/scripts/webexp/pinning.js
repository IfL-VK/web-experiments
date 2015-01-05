
// The main module called by/for the "new" page.

define(function (require) {

    var d3          = require('d3')
    var leaflet     = require('leaflet')

    var pinningCtrl = require('./controller/pinningCtrl')
    var pinningModel    = require('./model/pinningModel')

    var map
    var place = {}
    var memorize = {time: 15000}

    var state = ""


    // ------ Initialization of client-side data

    function init_pinning_page () {

        // 1 load user session
        init_user_view()

        // 2 load trial config and then initialize pinning for this trial
        pinningCtrl.fetchTrialConfig(5208, function (response) {
            
            pinningModel.setTrialConfig(response)
            pinningModel.setMapConfig(response.map_config)
            pinningModel.setPlaces(response.place_config.items)
            pinningModel.setPlaceToPinId(response['trial_config']['place_to_pin'])
            
            initialize_map()

            var pinning_condition = pinningModel.getTrialConfig()['trial_config']['trial_condition']
            console.log(" trial condition:", pinning_condition)

            if (pinning_condition === "webexp.config.no_pinning") {
                runMemorizationTimer()
            } else if (pinning_condition === "webexp.config.pinning") {
                initialize_pinning_features()
                runMemorizationTimer() // ### what if user has not pinned after timer ran out
                // my suggestion would be: run timer just after pinning was done 
            }

        }, false)

    }

    // --
    // ---- Mapping Screen ----
    // --

    function initialize_map(imageName) {

        // ------- Map Setup -----

        var mapConfig = pinningModel.getMapConfig().childs
        var mapId = mapConfig['de.akmiraketen.webexp.trial_map_id'].value

        console.log(" pinning: loaded map config", mapId)

        // create a map in the "map" div, set the view to a given place and zoom
        map = L.map('map',  {
            dragging: false,
            touchZooom: false,
            scrollWheelZoom: false,
            doubleClickZoom: false,
            boxZoom: false,
            zoomControl: false,
            keyboard: false
        })
        map.setView([52.955304, 8.326077], 12)

        // add an OpenStreetMap tile layer
        var tileLayer = L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            })
            tileLayer.addTo(map)
        // 
        if (imageName) {
            var northEast = map.getBounds().getNorthEast()
            var southWest = map.getBounds().getSouthWest()
                northEast.lat += 0.001
                northEast.lng += 0.001
                southWest.lat -= 0.001
            var imageUrl = 'images/' + imageName,
                // imageBounds = [[map.getBounds().getNorth(), map.getBounds().getEast()], [map.getBounds().getSouth(), map.getBounds().getEast()]] // ###
                imageBounds = L.latLngBounds(northEast, southWest)
            L.imageOverlay(imageUrl, imageBounds).addTo(map)
        }

    }

    function init_user_view () {
        pinningCtrl.fetchUser(function (data) {
            var username = data
            // GUI
            d3.select('.username').text(username)
            // OK
            pinningModel.setUsername(username)
        }, false)
    }

    function initialize_pinning_features () {

        var places = pinningModel.getPlaces()
        console.log(" pinning: loaded places", pinningModel.getPlaces())
        console.log(" pinning: initializing place to pin", pinningModel.getPlaceToPinId())
        var coordinates_to_pin = pinningModel.getCoordinatesOfPlaceToPin()
        console.log("   init geo coordinates: ", coordinates_to_pin)

        // ### fix configuration.. 

        place.lat = coordinates_to_pin['latitude']
        place.lng = coordinates_to_pin['longitude']

        var featureGroup = L.featureGroup()

        var marker = L.marker([place.lat, place.lng])
            marker.addTo(featureGroup)
        
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

    function isClickNearby(place, event) {
        var southWest = L.latLng(place.lat - 0.005, place.lng - 0.005)
            northEast = L.latLng(place.lat + 0.005, place.lng + 0.005)
        var activeControl = L.latLngBounds(southWest, northEast)
        return activeControl.contains(event.latlng)
    }

    function runMemorizationTimer() {
        // set_task_title("Task: Please memorize this map in the next " + (memorize.time / 1000) + " seconds")
        setTimeout(function (e) {
            window.document.location.href = "/pages/distance.html"
        }, memorize.time)

        console.log(" pinning: started memorization timer")
    }

    // ------------ Some rendering methods

    init_pinning_page()

});

