
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
        // return if initted
        if (screen.participant) return screen.participant
        // init
        $.getJSON('/experiment/participant', function(participant, status, xhr) {
            // ..
            if (status != "nocontent") {
                screen.participant = participant
                console.log("  Screen Participant", screen.participant)
            } else {
                console.log("Not logged in - REDIRECTING to Welcome page")
                window.document.location.assign("/experiment/")
            }
        })
    }

    this.getConfiguration = function() {
        // return if initted
        if (screen.configuration) return screen.configuration
        // init
        $.getJSON('/experiment/screen/' + screen.id, function(config) {
            // ..
            screen.configuration = config
            console.log("  Screen Configuration", screen.configuration)
            screen.init()
        })
    }

    this.setReport = function(obj) {
        this.report = obj
    }

    this.sendReport = function() {
        //..
    }

    this.loadNext = function() {
        window.document.location.assign("/experiment/screen/next")
    }

    this.setScreenAsSeen = function() {
        $.get('/experiment/screen/' + screen.id + '/seen', function(e) {
            console.log("Marked screen configuration " + screen.id + " as SEEN by Participant ", screen.participant)
        })
    }

    // 4)
    this.init = function() { console.log("You can override the .init() method of the screen object.")}

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
        // 3) listen to popState event (handles clicks back and forth browser buttons)
        $(window).bind('popstate', function(event) {
            // if the event has our history data on it, load the page fragment with AJAX
            console.log(event)
            window.document.location.assign('/experiment/screen/' + this.id)
            var state = event.originalEvent.state
            if (state) {
                console.log("popState", state);
            }
            return false;
        })

    })

 }

