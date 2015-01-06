
define(['./BaseModel'], function (Base) {

    var m2 = new Base('This is the data for Pinning Page');

    var fromPlaceOne    = -1
    var toPlaceOne      = -1
    
    m2.setFromPlaceOne = function (placeId) {
        fromPlaceOne = placeId
    }
    
    m2.getFromPlaceOne = function () {
        return fromPlaceOne
    }
    
    m2.setToPlaceOne = function (placeId) {
        toPlaceOne = placeId
    }
    
    m2.getToPlaceOne = function () {
        return toPlaceOne
    }
    
    var fromPlaceTwo    = -1
    var toPlaceTwo      = -1
    
    m2.setFromPlaceTwo = function (placeId) {
        fromPlaceTwo = placeId
    }
    
    m2.getFromPlaceTwo = function () {
        return fromPlaceTwo
    }
    
    m2.setToPlaceTwo = function (placeId) {
        toPlaceTwo = placeId
    }
    
    m2.getToPlaceTwo = function () {
        return toPlaceTwo
    }
    
    var fromPlaceThree  = -1
    var toPlaceThree    = -1
    
    m2.setFromPlaceThree = function (placeId) {
        fromPlaceThree = placeId
    }
    
    m2.getFromPlaceThree = function () {
        return fromPlaceThree
    }
    
    m2.setToPlaceThree = function (placeId) {
        toPlaceThree = placeId
    }
    
    m2.getToPlaceThree = function () {
        return toPlaceThree
    }
    
    var fromPlaceFour   = -1
    var toPlaceFour     = -1
    
    m2.setFromPlaceFour = function (placeId) {
        fromPlaceFour = placeId
    }
    
    m2.getFromPlaceFour = function () {
        return fromPlaceFour
    }
    
    m2.setToPlaceFour = function (placeId) {
        toPlaceFour = placeId
    }
    
    m2.getToPlaceFour = function () {
        return toPlaceFour
    }
    
    var fromPlaceFive   = -1
    var toPlaceFive     = -1
    
    m2.setFromPlaceFive = function (placeId) {
        fromPlaceFive = placeId
    }
    
    m2.getFromPlaceFive = function () {
        return fromPlaceFive
    }
    
    m2.setToPlaceFive = function (placeId) {
        toPlaceFive = placeId
    }
    
    m2.getToPlaceFive = function () {
        return toPlaceFive 
    }

    return m2;

});

