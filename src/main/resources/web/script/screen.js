
/**
 * DvEW Web Experiments - Screen Implementation
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>)
 * <a href="https://github.com/mukil/web-experiments">Source Code</a>
 * @version 0.0.4-SNAPSHOT
 *
 * Dependencies: jQuery 2.1.4 resp. d3.js
 */

 var screen = new function() {

    this.id             = undefined
    this.participant    = undefined
    this.configuration  = undefined
    this.report         = undefined

    this.getParticipant = function() {
        $.getJSON('/experiment/participant', function(participant) {
            // ..
            console.log("  Participant", participant)
        })
    }

    this.getConfiguration = function() {
        $.getJSON('/experiment/screen/' + screen.id, function(config) {
            // ..
            console.log("  Configuration", config)
        })
    }

    this.sendReport = function() {
        //..
    }

    this.loadNext = function() {
        window.document.location.assign("/experiment/screen/next")
    }

    // ------------------------------------------------------------------------------------------------ Constructor Code

    $(function() {

        // 0) parsing request
        var url = window.document.location.href
        screen.id = url.slice(url.lastIndexOf("/") + 1)
        console.log("Loading screen", screen.id)
        // 1) Load participant
        screen.getParticipant()
        // 2) Load screen configuration
        screen.getConfiguration()

    })

 }

