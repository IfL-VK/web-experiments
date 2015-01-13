
// The main module called by/for the "new" page.

define(function (require) {

    var newCtrl     = require('./controller/newCtrl')
    var newModel    = require('./model/newModel')
    var d3          = require('d3')
    var common      = require('common')

    var view_state  = "" // values may be "", "finish", "welcome", "pause" or "icon"

    // ------ Initialization of client-side data

    function init_page () {

        view_state = common.parse_view_state_from_page()

        newCtrl.fetchParticipant(function (data) {
            
            var marker_id = data.selected_marker_id
            var username = data.value
s
            if (view_state === "icon") {
                // 2 load marker selection
                init_marker_selection_view(marker_id)
            } else if (view_state === "welcome") {
                // 1 show welcome message
                init_welcome_view()
                console.log("Render welcome message")
            } else if (view_state === "finish") {
                // ###
                console.log("Render finish message")
            } else if (view_state === "pause") {
                // ###
                console.log("Render pause view")
            }

            // 2 load trials
            var users_condition = data['first_trial_condition']
            // fetch and then render all loaded trials
            newCtrl.fetchAllUnseenTrials(users_condition, function (trials) {
                if (common.debug) console.log(trials.items)
                if (trials.total_count === 0) {
                    d3.select('.trials').html('<p class="warning">To start testing, ' 
                        + 'please load some trial configurations.</p>')
                } else {
                    if (common.verbose) console.log("Loaded " + trials.items.length + " of type " + users_condition)
                    render_all_trial_configs(trials.items)   
                }
            }, false)
            // GUI
            d3.select('.username').text(username)
            // OK
            newModel.setUsername(username)
        }, function (error) {
            console.log("Auth response", error.status)
            console.warn("Please log in as \"VP <Nr>\"")
            render_start_session_dialog()
            throw Error("No session to start the experiment")
        }, false)
    }

    function init_welcome_view () {
        set_task_description("Willkommen zum Experiment!")
        var content = '<p>Im Folgenden werden dir in jedem Durchgang unterschiedliche Landkarten gezeigt.</p>'
            + '<p>Du bekommst entweder die Aufgabe, einen bestimmten Ort zu pinnen und dir die Karte einzupr&auml;gen, '
            + 'oder du wirst aufgefordert, dir die Karte einzupr&auml;gen ohne einen bestimmten Ort zu markieren.</p>'
            + '<p>Es wird zun&auml;chst einige &Uuml;bungsdurchg&auml;nge geben, bevor das Experiment mit insgesamt '
            + '30 Durchg&auml;ngen startet. Der Versuch dauert etwa 90 bis 120 Minuten.</p>'
            + '<p class="wichtig">Wir bitten dich, den Versuch konzentriert und gewissenhaft durchzuf&uuml;hren! <p>'
            + '<p class="welcome">Falls du noch Fragen hast, wende dich bitte an die Versuchsleitung. </p>'
	    + '<p class="welcome"><a href="/web-exp/icon" class="button">Los geht\'s</a></p>'
        set_page_content(content)
    }

    function init_marker_selection_view (selected_marker_id) {
        var title = "Du wirst nachher einige Orte in Landkarten markieren. <br/> "
            + "W&auml;hle bitte dazu einen Pin, mit dem du die Markierungen kennzeichnen m&ouml;chtest. "
            + "Diesen Pin nutzt du f&uuml;r den Rest des Experimentes zum Markieren"
        set_task_description(title)
        //
        newCtrl.fetchAllMarker(function (data) {
            if (data.length === 0) d3.select('.content').html('<p class="warning">To enable personalization, ' 
                    + 'please copy some icons into the symbols folder of the file repository.</p>')
            var iconPaths = data
            var $list = d3.select('.content')
                .append('ul').attr('class', 'marker-selection').attr('id', 'icons')
            for (var idx in iconPaths) {
                var iconPath = '/filerepo/' + iconPaths[idx]['path']
                var topicId = iconPaths[idx]['topic_id']
                if (topicId === selected_marker_id) {
                    $list.append('li').attr('class', 'symbol btn').append('img')
                            .attr('src', iconPath).attr('id', topicId).attr('class', 'selected')
                } else {
                    $list.append('li').attr('class', 'symbol btn').append('img')
                            .attr('src', iconPath).attr('id', topicId)
                }
            }
            d3.select('.content').append('a').attr('href', '/web-exp/trial').text('Los geht\'s')
            d3.selectAll('li.symbol img').on('click', function () {
                newCtrl.doMarkIconPreference(this.id, function (){
                    console.log("OK - Icon set") // ### render icon as silected
                }, function (error) {
                    console.warn("Fail - Icon preference could not be set!")
                })
            })
            // Cache in app-model
            newModel.setIcons(data)
        }, false)
    }
    
    function render_start_session_dialog() {
        d3.select('.title .username').text('!')
        d3.select('.trials').remove()
        var content = d3.select('.content')
            content.html('<p>Zum starten des Experiments bitte eine ID f&uuml;r die Versuchsperson ' 
                + 'eintragen und mit <b>OK</b> die Sitzung starten:<br/><input class="vp-id" type="text" '
                + 'name="username" placeholder="VP <Nr>"><input type="button" value="OK" class="login-btn" '
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
                window.location.href = '/web-exp/icon'
            }, common.debug)
        }
    }

    function render_all_trial_configs(items) {
        var links = d3.select('.trials').selectAll('a').data(items)
            .attr('href', function (d) { 
                return '/web-exp/trial/' + d.id + '/pinning' 
            })
            .attr('class', 'trial-links')
            .text(function (d) {
                return d.value 
            })

            links.enter()
                .append('a')
                .attr('href', function (d) { 
                    return '/web-exp/trial/' + d.id + '/pinning' 
                })
                .text(function (d) {
                    return d.value 
                })
            links.exit().remove()
    }

    function set_task_description (message) {
        d3.select("#title").html(message)
    }

    function set_page_content (message) {
        d3.select("div.content").html(message)
    }
    
    // --- Run this script when it is called/loaded

    init_page()

});

