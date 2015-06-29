
define(['./BaseCtrl'], function (BaseCtrl) {

    var customCtrl = new BaseCtrl('Controller: Pinning Page')

    // Extending BaseCtrl module for new page/app specific controls

    customCtrl.postEstimationReport = function (trialId, estimationNr, data, handle, fail, debug) {
        customCtrl.restc.postEstimationReport(trialId, estimationNr, data, handle, fail, debug)
    }
    
    customCtrl.fetchNextEstimationNr = function (trialId, handle, fail, debug) {
        customCtrl.restc.fetchNextEstimationNr(trialId, handle, fail, debug)
    }
    
    return customCtrl

});
