
// Load common code that includes config, then load the app logic for this page.
require(['./common'], function (common) {

    require(['webexp/welcome'], function (main_page){
        main_page.init_page()
    })
    
});

