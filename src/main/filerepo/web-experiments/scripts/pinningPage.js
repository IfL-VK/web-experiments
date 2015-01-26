
// Load common code that includes config, then load the app logic for this page.
require(['./common'], function (common) {

    require(['webexp/pinning'], function (pinning_page){
        pinning_page.init_page()
        // pinning_page.init_filler()
    })

});

