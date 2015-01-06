
define(['./BaseModel'], function (Base) {

    var m2 = new Base('This is the data for Pinning Page');

    var placeToPin = undefined
    
    // Extending BaseModel for new page/app

    m2.setPlaceToPinId = function (placeToPinId) {
        placeToPin = placeToPinId
    }

    m2.getPlaceToPinId = function () {
        return placeToPin
    }

    m2.getCoordinatesOfPlaceToPin = function () {
        if (!m2.getTrialConfig().hasOwnProperty('trial_config')) throw Error ("Misusage: No trial config loaded.")
        if (typeof this.getPlaceToPinId() === 'undefined') throw Error ("Error: No place to pin set.")
        var places = m2.getPlaces()
        for (var idx in places) {
            var place = places[idx]
            var currentPlaceId = place.childs['de.akmiraketen.webexp.place_id'].value
            if (currentPlaceId === this.getPlaceToPinId()) {
                // get geo coordinates of this place
                return {
                    "latitude" :  place.childs['de.akmiraketen.webexp.place_latitude'].value,
                    "longitude" : place.childs['de.akmiraketen.webexp.place_longitude'].value
                }
            }   			
        }
    }

    m2.getNameOfPlaceToPin = function () {
        return m2.getNameOfPlace(m2.getPlaceToPinId())
    }

    return m2;

});

