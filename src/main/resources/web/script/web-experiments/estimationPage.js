
// Load common code that includes config, then load the app logic for this page.
require(['./common'], function (common) {

    require(['webexp/estimation'], function (estimation_page) {
        estimation_page.init_page()
    })

})
