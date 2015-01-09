
define(['d3'], function (d3, require) {

    function restClient () {}

    function fetch(resource, callback, json, debug) {
        var response_type = "text/plain"
        if (json) response_type = "application/json"
        d3.xhr(resource, response_type, function (response) {
            // process response
            var result = undefined    
            if (json) {
                result = JSON.parse(response.response)
            } else {
                result = response.response
            }
            if (debug) console.log(response.status, result)
            if (response.status !== 200 && response.status !== 204) throw Error(response.status)
            if (typeof callback !== "function") throw Error("Please always specify a response "
                + "handler when calling restClient for async HTTP")
            callback(result)
        })
    }
    
    function mark(resource, fail) {
        var xhr = d3.xhr(resource)
            xhr.get()
            xhr.on('error', function (e){
                console.warn(e.status, e.statusText)
                if (typeof fail !== "undefined") fail(e)                
            })
    }
    
    function post(resource, data, callback, fail, json, debug) {
        var response_type = "application/json"
        var xhr = d3.xhr(resource, response_type)
            xhr.header('Content-Type', "application/json")
            xhr.post(JSON.stringify(data))
            xhr.on('load', function (response) {
                if (debug) console.log(response)
                if (json && typeof callback !== "undefined") {
                    // process response
                    var result = undefined
                        result = JSON.parse(response.response)
                    if (debug) console.log(response.status, result)
                    if (response.status !== 200 && response.status !== 204) throw Error(response.status)
                    if (typeof callback !== "function") throw Error("Please always specify a response "
                        + "handler when calling restClient for async HTTP")
                    callback(result)
                } else if (typeof callback !== "undefined") {
                    callback()
                }
            })
            xhr.on('error', function (e) {
                console.warn(e.status, e.statusText)
                if (typeof fail !== "undefined") fail(e)
            })
    }

    restClient.prototype = {

        fetchUsername: function (handle, debug) {
            if (debug) console.log(" restc: requesting username ... ")
            fetch('/accesscontrol/user', handle, false, debug)
        },
        fetchParticipant: function (handle, debug) {
            if (debug) console.log(" restc: requesting participant ... ")
            fetch('/web-exp/participant', handle, true, debug)
        },
        fetchAllIcons: function (handle, debug) {
            // 
            fetch('/web-exp/symbol/all', handle, true, debug)
        },
        fetchAllTrials: function (handle, debug) {
            fetch('/web-exp/trial/all', handle, true, debug)
        },
        fetchAllUnseenTrials: function (conditionUri, handle, debug) {
            // 
            fetch('/web-exp/trial/unseen/' + conditionUri, handle, true, debug)
        },
        fetchTrialConfig: function (trialId, handle, debug) {
            //
            fetch('/web-exp/trial/' + trialId, handle, true, debug)
        },
        fetchNextEstimationNr: function (trialId, handle, debug) {
            //
            fetch('/web-exp/estimation/next/' + trialId, handle, true, debug)
        },
        postEstimationReport: function (trialId, estimationNr, payload, handle, fail, debug) {
            post('/web-exp/estimation/' + trialId + '/' + estimationNr, payload, handle, fail, false, debug)
        },
        postPinningReport: function (trialId, payload, handle, fail, debug) {
            post('/web-exp/pinning/' + trialId, payload, handle, fail, false, debug)
        },
        doMarkTrialAsSeen: function (trialId, callback) {
            mark('/web-exp/trial/' + trialId + "/seen", callback)
        },

    }

    return restClient
    
})
