
define(['./BaseModel'], function (Base) {

    var m2 = new Base('This is the data for Pinning Page');

    var fromPlaceOne    = -1
    var toPlaceOne      = -1
    
    var fromPlaceTwo    = -1
    var toPlaceTwo      = -1
    
    var fromPlaceThree  = -1
    var toPlaceThree    = -1

    var fromPlaceFour   = -1
    var toPlaceFour     = -1
    
    var fromPlaceFive   = -1
    var toPlaceFive     = -1
    
    m2.setEstimations = function (trialConfig) {
        
        this.setFromPlaceOne(trialConfig['from_place1'])
        this.setToPlaceOne(trialConfig['to_place1'])
        
        this.setFromPlaceTwo(trialConfig['from_place2'])
        this.setToPlaceTwo(trialConfig['to_place2'])
        
        this.setFromPlaceThree(trialConfig['from_place3'])
        this.setToPlaceThree(trialConfig['to_place3'])
        
        this.setFromPlaceFour(trialConfig['from_place4'])
        this.setToPlaceFour(trialConfig['to_place4'])
        
        this.setFromPlaceFive(trialConfig['from_place5'])
        this.setToPlaceFive(trialConfig['to_place5'])
        
    }
    
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

