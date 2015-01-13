

// Require config (common) to all ..

require.config({
    baseUrl: "/filerepo/web-experiments/scripts/",
    paths: {
      d3: "/de.akmiraketen.web-experiments/script/vendor/d3/d3.min",
      leaflet: "/de.akmiraketen.web-experiments/script/vendor/leaflet/leaflet-0.7.3/leaflet"
    },
    waitSeconds: 15
});

define(function () {
    
    return {
        debug: false,
        verbose: true,
        parse_trial_id_from_resource_location: function () {
            var url = window.document.location.href
            var start = url.indexOf("trial/")
            var end = url.indexOf("/pinning")
            if (end === -1) end = url.indexOf("/estimation")
            var id = url.substring(start + 6, end)
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

