
define(['./BaseCtrl'], function (BaseCtrl) {

    var customCtrl = new BaseCtrl('Controller: Index Page')

    // Extending BaseCtrl module for new page/app specific controls

    customCtrl.fetchAllMarker = function (handle, debug) {
        customCtrl.restc.fetchAllIcons(handle, debug)
    }

    customCtrl.fetchAllUnseenPinningTrials = function (handle, debug) {
        customCtrl.restc.fetchAllUnseenPinningTrials(handle, debug)
    }

    customCtrl.logoutParticipant = function (handle, debug) {
        customCtrl.restc.logoutParticipant(handle, debug)
    }

    return customCtrl

});

