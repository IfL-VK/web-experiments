
var map
var place = {}
var memorize = {time: 15000}

var state = ""

function visit_frontpage () {
    window.document.location.href = "/pages/"
}

function visit_webserver_index () {
    window.document.location.href = "/"
}

// --
// ---- Mapping Screen ----
// --

function initialize_map(imageName) {

    // ------- Map Setup -----

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

function initialize_pinning(url) {
    state = "pinning"
    if (url.hash === "#with") {
        initialize_map()
        initialize_pinning_features()   
    } else {
        initialize_map()
        runMemorizationTimer()
    }
}

function set_task_title (text) {
    if (state === "pinning" || state === "estimating") {
        text += '<span class="statusbox">'
            + '<img class="btn" onclick="javascript:visit_frontpage()"'
            + 'src="images/1417207267_arrow-back-outline-32.png" title="Start again"/>'
            + '<span>Stand: Mittwoch, 17. Dezember</span>'
        + '</span>'
    } else {
        text += '<span class="statusbox">'
            + '<img class="btn" onclick="javascript:visit_webserver_index()"'
            + 'src="images/1417207267_arrow-back-outline-32.png" title="Start again"/>'
            + '<span>Stand: Mittwoch, 17. Dezember</span>'
        + '</span>'
    }
    // 
    document.getElementById("title").innerHTML = text 
}

function initialize_estimation() {
    state = "estimating"
    initialize_map("Trial1/Blank-Karte.png")
    initialize_estimation_features()
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

function isClickNearby(place, event) {
    var southWest = L.latLng(place.lat - 0.005, place.lng - 0.005)
        northEast = L.latLng(place.lat + 0.005, place.lng + 0.005)
    var activeControl = L.latLngBounds(southWest, northEast)
    return activeControl.contains(event.latlng)
}

function runMemorizationTimer() {
    set_task_title("Task: Please memorize this map in the next " + (memorize.time / 1000) + " seconds")
    setTimeout(function (e) {
        window.document.location.href = "/pages/distance.html"
    }, memorize.time)
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
    var pointB = L.latLng(centerLat, centerLng)
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
        set_task_title("Result: " + (meters / 1000).toFixed(1) + " Kilometer")
         	
    }

    polyline.addTo(featureGroup)
    startPoint.addTo(featureGroup)
    featureGroup.addTo(map)

}

