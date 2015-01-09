
define(['../restc'], function (restClient, require) {

    var restc = new restClient()

    function controllerBase(id) {
        this.id = id;
    }

    controllerBase.prototype = {

        fetchUser: function (handle, debug) {
            restc.fetchUsername(handle, debug)
        },
        fetchParticipant: function (handle, debug) {
            restc.fetchParticipant(handle, debug)
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
        doMarkTrialAsSeen: function (trialId, fail) {
            restc.doMarkTrialAsSeen(trialId, fail)
        },
        setModel: function (model) {
            this.model = model;
        },
        render: function (bodyDom) {
            bodyDom.prepend('<h1>Controller ' + this.id + ' says "' +
                      this.model.getTitle() + '"</h1>');
        },
        restc: restc 
        
    };

    return controllerBase;
});
