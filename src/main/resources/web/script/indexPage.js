
// Load common code that includes config, then load the app logic for this page.
require(['./common'], function (common) {

    require(['webexp/index'], function (main_page){
        main_page.init_page()
    })

})
