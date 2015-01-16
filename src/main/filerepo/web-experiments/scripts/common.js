

// Require config (common) to all ..

require.config({
    baseUrl: "/filerepo/web-experiments/scripts/",
    paths: {
      d3: "/de.akmiraketen.web-experiments/script/vendor/d3/d3.min",
      leaflet: "/de.akmiraketen.web-experiments/script/vendor/leaflet/leaflet-0.7.3/leaflet",
      leaflet_label: "/de.akmiraketen.web-experiments/script/vendor/leaflet/leaflet-label/dist/leaflet.iconlabel"
    },
    shim: {
        'leaflet': {
            exports: 'L'
        },
        'leaflet_label': {
            deps: ['leaflet'],
        }
    },
    waitSeconds: 15
});

define(function () {
    
    return {
        debug: false,
        verbose: true,
        parse_trial_id_from_resource_location: function () {
            var url = window.document.location.href
            var id = ""
            var start = url.indexOf("trial/")
            if (start === -1) start = url.indexOf("pract/")
            if (start === -1) start = url.indexOf("intro/")
            var end = url.indexOf("/pinning")
            if (end === -1) end = url.indexOf("/estimation")
            if (end === -1) {
                id = url.substring(start + 6)
            } else {
                id = url.substring(start + 6, end)
            }
            if (this.debug) console.log("Trial ID is ", id)
            return parseInt(id)
        },
        parse_view_state_from_page: function () {
            var url = window.document.location.href
            var start = url.indexOf("web-exp/")
            var screen_name = url.substring(start + 8)
            if (this.debug) console.log("View state is ", screen_name)
            return screen_name
        }
    }
    
})

