

// Require setup for loading other JS modules

require.config({
    baseUrl: "/filerepo//web-experiments/scripts",
    paths: {
      "d3": "/de.akmiraketen.web-experiments/script/vendor/d3/d3.min.js"
      "leaflet": "/de.akmiraketen.web-experiments/script/vendor/leaflet/leaflet-0.7.3/leaflet.js"
    },
    waitSeconds: 15
});


// Main

require(["d3"], function(d3) {
    console.log("loaded d3")
});


