
define(['./BaseCtrl'], function (BaseCtrl) {

    var customCtrl = new BaseCtrl('Controller: Welcome Page')

    // Extending BaseCtrl module for new page/app specific controls

    customCtrl.fetchAllMarker = function (handle, debug) {
        customCtrl.restc.fetchAllIcons(handle, debug)
    }

    customCtrl.fetchAllUnseenPinningTrials = function (handle, debug) {
        customCtrl.restc.fetchAllUnseenPinningTrials(handle, debug)
    }

    return customCtrl

});

