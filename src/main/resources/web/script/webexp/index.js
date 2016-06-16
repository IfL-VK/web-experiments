
// The main module used in the "index" template.
// Here, the very first and the very last screen of your experiment is handled.
// With that there is a utility to login and logout (which are the equivalent to
// starting and restarting an experiment.

define(function(require) {

    var newCtrl     = require('./controller/indexCtrl')
    var newModel    = require('./model/indexModel')
    var common      = require('common')

    var view_state  = "" // values may be "" or "welcome" and "finish"

    // ------ Initialization of page (according to view_state)

    function init_page() {

        view_state = common.parse_view_state_from_page()
        // correcting a call to this page without a trailing slash
        if (view_state.indexOf("experiment") !== -1) load_init_screen()

        // --- Render welcome or finish screen (including login, logout and restart)

        init_workspace_cookie()

        newCtrl.fetchParticipant(function (data) {

            // Remove Loader
            d3.select('.loader').remove()
            // Init Model
            newModel.setUsername(data.username)

            // Handler Authentication/User Status
            if (data.status === 204) { // API change: as of 0.4-SNAPSHOT
                // re-implemented response for an unauthenticated request
                render_start_session_dialog()
                throw Error("No session to start the experiment - Please log in as \"VP <Nr>\"")

            } else if (view_state === "welcome" || view_state === "" || view_state === "#") {

                init_welcome_view()

            } else if (view_state === "finish") {

                set_task_description('<br/>Danke!<br/><br/>')
                set_page_content('<a class="restart" href="#">Neustart</a>')
                d3.select('a.restart').on('click', restart_experiment)

            }

        }, function (error) { // this should never be thrown as of 0.4-SNAPSHOT
            render_start_session_dialog()
            throw Error("No session to start the experiment - Please log in as \"VP <Nr>\"")
        }, false)

    }

    function init_workspace_cookie() {
        newCtrl.fetchWorkspace(function(response) {
            var workspace = JSON.parse(response)
            remove_ws_cookie()
            set_ws_cookie(workspace.id)
            console.log("Set \"Web Experiments\" Workspace Cookie, Workspace ID", workspace.id)
        })

        function remove_ws_cookie() {
            // Note: setting the expire date to yesterday removes the cookie
            var days = -1
            var expires = new Date()
            expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000)
            document.cookie = common.workspaceCookieName + "=;path=/;expires=" + expires.toGMTString()
        }

        function set_ws_cookie(value) {
            document.cookie = common.workspaceCookieName + "=" + value + ";path=/"
        }

    }

    function load_init_screen() {
        window.document.location.assign("/experiment/")
    }

    function load_next_screen() {
        window.document.location.assign("/experiment/screen/next")
    }

    function page_refresh() {
        window.location.reload()
    }

    function restart_experiment() {
        newCtrl.logoutParticipant(function(e) {
            console.log("OK - Restart log out successful!")
            window.document.location.assign('/experiment/')
        }, true)
    }

    function logout() {
        newCtrl.logoutParticipant(function(e) {
            window.document.location.assign('/experiment/')
        }, true)
    }

    // -- Login View

    function render_start_session_dialog() {
        d3.select('.title .username').text('')
        d3.select('.title').text('')
        var content = d3.select('.content')
            content.html('<p class="textblock">Zum Starten des Experiments bitte eine ID f&uuml;r die Versuchsperson '
                + 'eintragen und mit <b>OK</b> die Sitzung starten:<br/><br/><input class="vp-id input" type="text" '
                + 'name="username" placeholder="VP <Nr>"><input type="button" value="OK" class="login-btn button" '
                + 'name="submit"></p>')
        content.select('.login-btn').on('click', do_auth)
        content.select('.vp-id').on('keyup', function () {
            if (d3.event.keyCode === 13) {
                do_auth()
            }
        })

        function do_auth() {
            var element = d3.select('input.vp-id')[0][0]
            newCtrl.startSession(element.value, function (){
                page_refresh()
            }, common.debug)
        }
    }

    // --- Welcome View

    function init_welcome_view() {
        set_task_description("Willkommen zu unserem Experiment")
        var content = '<p>Angemeldet als '+newModel.getUsername()+'</p><a class="logout" href="#">Log out</a>'
        set_page_content(content)
        d3.select('a.logout').on('click', logout)
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text("Start")
            next.on('click', function (e) {
                load_next_screen()
            })
    }
    
    // --- GUI Helper

    function set_task_description (message) {
        d3.select("#title").html(message)
    }

    function set_page_content (message) {
        d3.select("div.content").html(message)
    }
    
    return {
        init_page: init_page
    }

});

