
define(function () {

    var username = undefined;
    var marker = undefined
    var icons = undefined
    var title = undefined
    
    var map = {}
    var trial = {}
    var places = []

    function modelBase(title) {
        this.title = title;
    }
    
    modelBase.prototype = {
        
        setMapConfig: function (mapConfig) {
            map = mapConfig
        },
        getMapConfig: function () {
            return map
        },
        getMapConfigId: function () {
            return map.childs['de.akmiraketen.webexp.trial_map_id'].value
        },
        setTrialConfig: function (trialConfig) {
            trial = trialConfig
        },
        getTrialConfig: function () {
            return trial
        },
        setPlaces: function (placeConfig) {
            places = placeConfig
        },
        getPlaces: function () {
            return places
        },
        getNameOfPlace: function (placeId) {
            if (!trial.hasOwnProperty('trial_config')) throw Error ("Misusage: No trial config loaded.")
            if (places.length === 0) throw Error ("Error: No places loaded.")
            for (var idx in places) {
                var place = places[idx]
                var currentPlaceId = place.childs['de.akmiraketen.webexp.place_id'].value
                if (currentPlaceId === placeId) {
                    // get name of this place
                    return place.childs['de.akmiraketen.webexp.place_name'].value
                }
            }
        },
        getTitle: function () {
            return title
        },
        setUsername: function (user) {
            username = user
        },
        getUsername: function () {
            return username
        },
        setMarker: function (iconPath) {
            marker = iconPath
        },
        getMarker: function () {
            return marker
        },
        setIcons: function (cons) {
            icons = cons
        },
        getIcons: function () {
            return icons
        }
    };

    return modelBase;
});
