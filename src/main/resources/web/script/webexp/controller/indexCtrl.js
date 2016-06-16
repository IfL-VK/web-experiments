
define(['./BaseCtrl'], function (BaseCtrl) {

    var customCtrl = new BaseCtrl('Controller: Index Page')

    // Extending BaseCtrl module for new page/app specific controls

    customCtrl.fetchAllMarker = function (handle, debug) {
        customCtrl.restc.fetchAllIcons(handle, debug)
    }
    
    customCtrl.fetchWorkspace = function (handle, debug) {
        customCtrl.restc.fetchWorkspace(handle, debug)
    }

    return customCtrl

});

