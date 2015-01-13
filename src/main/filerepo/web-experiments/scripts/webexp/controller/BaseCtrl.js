
define(['../restc'], function (restClient, require) {

    var restc = new restClient()

    function controllerBase(id) {
        this.id = id;
    }

    controllerBase.prototype = {

        fetchUser: function (handle, debug) {
            restc.fetchUsername(handle, debug)
        },
        fetchParticipant: function (handle, fail, debug) {
            restc.fetchParticipant(handle, fail, debug)
        },
        fetchAllUnseenTrials: function (conditionUri, handle, debug) {
            restc.fetchAllUnseenTrials(conditionUri, handle, debug)
        },
        fetchAllTrials: function (handle, debug) {
            restc.fetchAllTrials(handle, debug)
        },
        fetchTrialConfig: function (trialId, handle, debug) {
            restc.fetchTrialConfig(trialId, handle, debug)
        },
        doMarkTrialAsSeen: function (trialId, handle, fail) {
            restc.doMarkTrialAsSeen(trialId, handle, fail)
        },
        doMarkIconPreference: function (fileTopicId, handle, fail) {
            restc.doMarkIconPreference(fileTopicId, handle, fail)
        },
        setModel: function (model) {
            this.model = model;
        },
        render: function (bodyDom) {
            bodyDom.prepend('<h1>Controller ' + this.id + ' says "' +
                      this.model.getTitle() + '"</h1>');
        },
        startSession: function (vpId, handle, debug) {
            restc.startSession(vpId, handle, debug)
        },
        restc: restc 
        
    };

    return controllerBase;
});
