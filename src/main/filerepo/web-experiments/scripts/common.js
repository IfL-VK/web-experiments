

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
            var id = url.substring(start + 6, end)
            if (this.debug) console.log("Trial ID is ", id)
            return parseInt(id)
        }
    }
    
})

