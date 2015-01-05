
define(['./BaseModel'], function (Base) {

    var m2 = new Base('This is the data for Pinning Page');

    var map = {}
    var trial = {}
    var places = []
    var placeToPin = undefined

    m2.setMapConfig = function (mapConfig) {
		this.map = mapConfig
	}

  	m2.getMapConfig = function () {
		return this.map
	}

   	m2.setTrialConfig = function (trialConfig) {
		this.trial = trialConfig
	}

   	m2.getTrialConfig = function () {
		return this.trial
	}

    m2.setPlaces = function (places) {
		this.places = places
	}

   	m2.getPlaces = function () {
		return this.places
	}

	m2.setPlaceToPinId = function (placeToPinId) {
		this.placeToPin = placeToPinId
	}

    m2.getPlaceToPinId = function () {
		return this.placeToPin
	}

   	m2.getCoordinatesOfPlaceToPin = function () {
   		if (!this.trial.hasOwnProperty('trial_config')) throw Error ("Misusage: No trial config loaded.")
   		if (typeof this.getPlaceToPinId() === 'undefined') throw Error ("Error: No place to pin set.")
   		for (var idx in this.places) {
   			var place = this.places[idx]
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
   		if (!this.trial.hasOwnProperty('trial_config')) throw Error ("Misusage: No trial config loaded.")
   		if (typeof this.getPlaceToPinId() === 'undefined') throw Error ("Error: No place to pin set.")
   		for (var idx in this.places) {
   			var place = this.places[idx]
   			var currentPlaceId = place.childs['de.akmiraketen.webexp.place_id'].value
   			if (currentPlaceId === this.getPlaceToPinId()) {
   				// get name of this place
   				return place.childs['de.akmiraketen.webexp.place_name'].value
   			}   			
   		}
   	}

    return m2;

});

