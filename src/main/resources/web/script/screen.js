
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
    this.event_types    = undefined

    this.getParticipant = function() {
        // return if initted
        if (screen.participant) return screen.participant
        // init
        $.getJSON('/experiment/participant', function(participant, status, xhr) {
            // ..
            if (status !== "nocontent") {
                screen.participant = participant
                console.log("  Cached Screen Participant", screen.participant)
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
        $.getJSON('/experiment/screen/' + screen.id, function(config, status) {
            // ..
            screen.configuration = config
            console.log("  Cached Screen Configuration", screen.configuration, "HTTP Status", status)
            if (status === "success") {
                screen.init()
            } else {
                throw new Error("Could not load screen configuration, please check the server logs or the URL!")
            }
        })
    }

    this.initEventTypes = function() {
        // return if initted
        if (screen.event_types) return screen.event_types
        // init
        $.getJSON('/experiment/report/action', function(all) {
            // ..
            screen.event_types = all
            console.log("  Cached Action Name Topics for Reporting", screen.event_types)
        })
    }

    this.setReport = function(obj) {
        this.report = obj
    }

    this.startReport = function(event) {
        $.get('/experiment/report/start/' + screen.id, function(e, status) {
            // console.log("  initReport HTTP Status", status, "Response", e)
            if (e != -1) {
                console.log("  Screen Initiated Reporting for Screen Config=" + screen.id + " and Participant=",
                    screen.participant)
            } else {
                console.warn("Could NOT INIT Reporting for Screen configuration " + screen.id +  " and Participant=",
                    screen.participant)
            }
        })
    }

    this.postActionReport = function(actionObject) {
        $.ajax({
            type: "POST",
            url: '/experiment/report/action/' + screen.id,
            contentType: "application/json",
            data: JSON.stringify(actionObject),
            dataType: "json",
            processData: false,
            async: true
        })
        .done(function(data, text_status, jq_xhr) {
            // console.log("  Saving action report=" + text_status)
        })
        .fail(function(jq_xhr, text_status, error_thrown) {
            // Note: since at least jQuery 2.0.3 an exception thrown from the "error" callback (as registered in the
            // $.ajax() settings object) does not reach the calling plugin. (In jQuery 1.7.2 it did.) Apparently the
            // exception is catched by jQuery. That's why we use the Promise style to register our callbacks (done(),
            // fail(), always()). An exception thrown from fail() does reach the calling plugin.
            throw "Screen ReportError: POST request failed (" + text_status + ": " + error_thrown + ")"
        })
    }

    this.loadNext = function() {
        window.document.location.assign("/experiment/screen/next")
    }

    this.setScreenAsSeen = function() {
        console.log("Marking Screen as Seen")
        $.get('/experiment/screen/' + screen.id + '/seen', function(e) {
            if (e !== -1) {
                console.log("  Screen configuration SET " + screen.id + " as SEEN by Participant ", screen.participant)
            } else {
                console.warn("Could NOT SET screen as SEEN by " + screen.participant)
            }
        })
    }

    this.restartExperiment = function() {
        $.post('/accesscontrol/logout', function(e) {
            console.log("  Logged out", screen.participant)
            window.document.location.assign('/experiment/')
        })
    }

    // 4) Setup Hook "init" - is called when screen configuration was loaded
    this.init = function() {
        console.log("  Hint: You can use the screen.init() hook to execute code after screen configuration was loaded")
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
        // 4) init event types for reporting
        screen.initEventTypes()
        // 5) invalidate configured screen for the current participant
        // screen.setScreenAsSeen()
        // $('.loader').remove()

    })

 }

