
/*
 *
 * DvEW Web Experiments DM 4 JavaScript Plugin
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>)
 * @website https://github.com/ifl-vk/web-exp
 * @version 0.0.1-SNAPSHOT
 *
 */

(function ($, dm4c) {

    function isLoggedIn() {
        var requestUri = '/accesscontrol/user'
        //
        var response = false
        $.ajax({
            type: "GET", url: requestUri,
            dataType: "text", processData: true, async: false,
            success: function(data, text_status, jq_xhr) {
                if (typeof data === "undefined") return false // this seems to match (new) response semantics
                if (data !== "") response = true
            },
            error: function(jq_xhr, text_status, error_thrown) {
                console.warn("CSV Importer Plugin says: Not authenticated.")
                response = false
            }
        })
        return response
    }

    // upload and import the file
    function loadTrialConfig() {

        // Start to import the content of the just uploaded file
        var status = dm4c.restc.request('GET', '/web-exp/trial/config/import/' + dm4c.selected_object.value)
        console.log(status)

    }

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

    // configure menu and type commands
    dm4c.add_listener('topic_commands', function(topic) {

        var commands = []

        if (isLoggedIn() && topic.type_uri === 'dm4.accesscontrol.username') {
            commands.push({
                is_separator : true,
                context : 'context-menu'
            })
            commands.push({
                label : 'Load Trial Config',
                handler : loadTrialConfig,
                context : [ 'context-menu', 'detail-panel-show' ]
            })
        }
        return commands
    })

}(jQuery, dm4c))
