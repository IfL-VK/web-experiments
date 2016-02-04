
// Require config (common) to all ..

require.config({
    baseUrl: "/de.akmiraketen.web-experiments/script/",
    paths: {
      d3: "/de.akmiraketen.web-experiments/script/vendor/d3/d3.min"
    },
    shim: {
        'd3': {
            exports: 'd3'
        }
    },
    waitSeconds: 15
})

define(function () {
    
    return {
        debug: false,
        verbose: true,
        parse_trial_id_from_resource_location: function () {
            var url = window.document.location.href
            var id = ""
            var start = url.indexOf("screen/")
            id = url.substring(start + 7)
            // }
            if (this.debug) console.log("Trial ID is ", id)
            return parseInt(id)
        },
        parse_view_state_from_page: function () {
            var url = window.document.location.href
            var start = url.indexOf("experiment/")
            var screen_name = url.substring(start + 11)
            if (this.debug) console.log("View state is ", screen_name)
            return screen_name
        }
    }
    
})
