
/*
 *
 * DvEW Web Experiments DM 4 JavaScript Plugin
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>)
 * @website https://github.com/ifl-vk/web-exp
 * @version 0.0.1-SNAPSHOT
 *
 */

(function ($, dm4c) {

    dm4c.add_plugin('de.akmiraketen.web-experiments', function () {

        // var language_menu

        // === Webclient Listeners ===

        dm4c.add_listener("init", function() {
            console.log("Hello World! says our web-experiments plugin!")
        })

        function showSpinningWheel () {
            $('#page-content').html('<img src="/de.akmiraketen.web-experiments/images/ajax-loader.gif" '
                + ' class="wikidata-loading" />')
        }

    })

}(jQuery, dm4c))
