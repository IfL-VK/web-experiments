
define(['./BaseCtrl'], function (BaseCtrl) {

    var customCtrl = new BaseCtrl('Controller: New Page')

    // Extending BaseCtrl module for new page/app specific controls

    customCtrl.foo = function (info) {
        console.log("FOO called: " + info)
    }

    // Allways expect and pass on handle and debug-flag

    customCtrl.fetchAllMarker = function (handle, debug) {
        customCtrl.restc.fetchAllIcons(handle, debug)
    }

    customCtrl.renderIconSelection = function () {
        console.log(customCtrl.getIcons())
    }

    customCtrl.fetchAllUnseenPinningTrials = function (handle, debug) {
        customCtrl.restc.fetchAllUnseenPinningTrials(handle, debug)
    }
    
    // 

    return customCtrl

});

