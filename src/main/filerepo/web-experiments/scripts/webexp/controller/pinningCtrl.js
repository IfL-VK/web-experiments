
define(['./BaseCtrl'], function (BaseCtrl) {

    var customCtrl = new BaseCtrl('Controller: Pinning Page')

    // Extending BaseCtrl module for new page/app specific controls

    customCtrl.postPinningReport = function (trialId, data, handle, fail, debug) {
        customCtrl.restc.postPinningReport(trialId, data, handle, fail, debug)
    }

    return customCtrl

});

