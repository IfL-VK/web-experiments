
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
        return m2.getCoordinatesOfPlace(m2.getPlaceToPinId())
    }

    m2.getNameOfPlaceToPin = function () {
        return m2.getNameOfPlace(m2.getPlaceToPinId())
    }

    return m2;

});

