
/*
 *
 * DvEW Web Experiments DM 4 JavaScript Plugin
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>)
 * @website https://github.com/mukil/web-experiments
 * @version 0.0.4-SNAPSHOT
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
    function loadScreenConfiguration() {

        // Start to import the content of the just uploaded file
        var status = dm4c.restc.request('GET', '/experiment/screen/config/import/' + dm4c.selected_object.value,
            undefined, function (response) {
                console.log(status)
                dm4c.show_topic(dm4c.selected_object, "show")
            })
        showSpinningWheel()

        function showSpinningWheel() {
            $('#page-content').html('<img src="/de.akmiraketen.web-experiments/images/ajax-loader.gif" '
                + ' class="webexp-loading" style="margin-top: 35%; margin-left: 45%;" />')
        }

    }

    dm4c.add_plugin('de.akmiraketen.web-experiments', function () {

        // var language_menu

        // === Webclient Listeners ===

        dm4c.add_listener("init", function() {
            console.log("Hello World! Says our web-experiments plugin!")
        })

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
                label : 'Load Screen Configuration',
                handler : loadScreenConfiguration,
                context : [ 'context-menu', 'detail-panel-show' ]
            })
        }
        return commands
    })

}(jQuery, dm4c))
