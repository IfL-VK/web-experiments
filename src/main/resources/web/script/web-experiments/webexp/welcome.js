
// The main module called by/for the "welcome" page.

define(function (require) {

    var newCtrl     = require('./controller/welcomeCtrl')
    var newModel    = require('./model/welcomeModel')
    var common      = require('common')

    var view_state  = "" // values may be "", "finish", "welcome", "pause" or "icon", "intro"

    // ------ Initialization of page (according to view_state)

    function init_page () {

        view_state = common.parse_view_state_from_page()
        if (common.verbose) console.log(" Page initialization => ", view_state)

        if (view_state.indexOf("intro") !== -1 || view_state.indexOf("start") !== -1) {

        // --- Render intro and start pages per condition

            var trialId = common.parse_trial_id_from_resource_location()
            newCtrl.fetchTrialConfig(trialId, function(data) {

                var page_condition = data['trial_config']['trial_condition']

                if (view_state.indexOf("intro") !== -1) { // intro page per condition

                    if (page_condition === "webexp.config.pinning") {
                        render_pinning_intro()

                    } else {
                        render_no_pinning_intro()
                    }

                } else if (view_state.indexOf("start") !== -1) { // start page per condition

                    // do nothing here

                }

                newCtrl.doMarkTrialAsSeen(trialId)

            }, false)
            
        } else {
        // --- Render welcome View or static page (namely finish + pause)

            newCtrl.fetchParticipant(function (data) {

                if (data.status == 204) { // API change: as of 0.4-SNAPSHOT
                    // re-implemented response for anunauthenticated request
                    render_start_session_dialog()
                    throw Error("No session to start the experiment - Please log in as \"VP <Nr>\"")

                } else if (view_state === "welcome" || view_state === "" || view_state === "#") {

                    init_welcome_view()
                    
                } else if (view_state === "finish") {

                    set_task_description('Das war\'s am PC')
                    set_page_content('<p class="textblock">Die Aufgaben am PC hast du nun erfolgreich beendet.<br/>'
                        + 'Bitte f&uuml;lle nun den Fragebogen auf deinem Tisch aus.<br/>'
                        + 'Wende dich danach bitte leise an die Versuchsleitung.</p>')

                } else if (view_state.indexOf("pause") !== -1) {
                    // is modellled as a trial config, too, so we need to mark it as seen
                    var trialId = common.parse_trial_id_from_resource_location()
                    newCtrl.doMarkTrialAsSeen(trialId)

                }

            }, function (error) { // this should never be thrown as of 0.4-SNAPSHOT
                render_start_session_dialog()
                throw Error("No session to start the experiment - Please log in as \"VP <Nr>\"")
            }, false)
        }

    }

    // -- Login View

    function render_start_session_dialog() {
        d3.select('.title .username').text('')
        d3.select('.title').text('')
        var content = d3.select('.content')
            content.html('<p class="textblock">Zum Starten des Experiments bitte eine ID f&uuml;r die Versuchsperson '
                + 'eintragen und mit <b>OK</b> die Sitzung starten:<br/><br/><input class="vp-id" type="text" '
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
                window.location.reload()
            }, common.debug)
        }
    }

    // --- Welcome View

    function init_welcome_view() {
        set_task_description("Willkommen zu unserem Experiment")
        var content = '<p class="textblock"></p>'
        set_page_content(content)
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text("Ok, los geht's")
            next.on('click', function (e) { window.document.location.assign("/experiment/screen/next") })
    }

    // --- General Intro View

    function render_go_to_intro() {
        set_page_content('<p class="textblock">Bevor du den ersten Block startest, zeigen wir dir auf den folgenden Seiten, '
            + 'welche Aufgaben du bekommst und wie du diese l&ouml;sen kannst. Bitte lese diese Anleitung '
            + 'aufmerksam durch!</p>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_overview_intro() })
    }

    function render_overview_intro() {
        d3.select('p.logo img').remove()
        set_task_description('')
        set_page_content('<p class="textblock">Im folgenden Experiment wirst du in jedem Durchgang eine Karte sehen. '
                + 'Du wirst entweder darum gebeten, dir die Karte nur einzupr&auml;gen oder &ndash; zus&auml;tzlich zum Einpr&auml;gen &ndash; auch noch einen Ort zu markieren.<br/><br/>'
                + 'Du hast immer eine ca. Minute Zeit, dir die Karte so genau wie m&ouml;glich einzupr&auml;gen.</p>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_filler_intro() })
        // d3.select('.content').append('a').attr('class', 'button').attr('href', '/web-exp/nextpage').text('weiter')
    }

    function render_filler_intro() {
        set_page_content('<p class="textblock">Nachdem du dir die Karte eingepr&auml;gt hast, erh&auml;ltst du Rechenaufgaben, die du bitte so schnell '
            + 'und so korrekt wie m&ouml;glich beantwortest, indem du die Ergebniszahl eintippst.</p>'
            + '<br/><h4 class="image">Abbildung 2:</h4><img src="/de.akmiraketen.web-experiments/images/web_exp_filler_task_screen.png"><br/><br/>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_estimation_intro() })
    }

    function render_estimation_intro() {
        set_page_content('<p class="textblock">Nach den Rechenaufgaben werden dir zwei Orte genannt, die du bereits auf der Karte zuvor kennen gelernt hast.</p>'
            + '<p class="wichtig"><b>Bitte sch&auml;tze, wie weit der eine vom anderen genannten Ort entfernt liegt.</b></p>'
            + '<p class="textblock">Dazu klickst du bitte in den Kreis in der Mitte des Bildschirms und ziehst die entstehende Linie so lange, '
            + 'bis die L&auml;nge mit der Distanz zwischen den beiden Orten &uuml;bereinstimmt. Au&szlig;erdem drehst du die Linie so, dass auch die '
            + 'Richtung zwischen den beiden Orten deiner Erinnerung nach stimmt. Sobald du die Maustaste losl&auml;sst, ist deine Antwort gespeichert.</p>'
			+ '<p class="textblock">Um ein Gef&uuml;hl f&uuml;r die Distanzen zu bekommen, haben wird dir die H&ouml;he der vorher gezeigten Karte in der Ecke unten links abgebildet.</p>'
            + '<br/><h4 class="image">Abbildung 3:</h4><img src="/de.akmiraketen.web-experiments/images/web_exp_estimation_screen_blank.png"><br/><br/>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_certainty_intro() })
    }

    function render_certainty_intro() {
        set_page_content('<p class="textblock">Anschlie&szlig;end erscheint eine Seite, auf der du angeben sollst, wie sicher du dir bei deiner Sch&auml;tzung warst.</p>'
            + '<br/><h4 class="image">Abbildung 4:</h4><img src="/de.akmiraketen.web-experiments/images/web_exp_confidence.png"><br/><br/>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_practice_intro() })
    }

    function render_practice_intro() {
        set_page_content('<p class="textblock">Du f&uuml;hrst zu jeder Karte f&uuml;nf Sch&auml;tzungen durch, bevor du die n&auml;chste Karte pr&auml;sentiert bekommst.</p>'
            + '<p class="textblock">Insgesamt bekommst du zwei Bl&ouml;cke mit je 15 Karten pr&auml;sentiert.<br/>Zum Abschluss bitten wir dich, noch einen Fragebogen auszuf&uuml;llen.</p>'
            + '<p class="textblock">Bevor das eigentliche Experiment startet, bekommst du zun&auml;chst zwei &Uuml;bungsdurchg&auml;nge pr&auml;sentiert. '
            + 'In den zwei &Uuml;bungsdurchg&auml;ngen vor den beiden Bl&ouml;cken, erh&auml;lst du R&uuml;ckmeldung dar&uuml;ber, wie genau deine Sch&auml;tzung war.</p>'
            + '<br/><br/>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_feedback_intro() })
    }

    function render_feedback_intro() {
        set_page_content('<p>In den zwei &Uuml;bungsdurchg&auml;ngen vor den beiden Bl&ouml;cken, erh&auml;ltst du R&uuml;ckmeldung dar&uuml;ber, '
            + 'in welche Richtung und wie weit der eine vom anderen Ort tats&auml;chlich entfernt liegt. Dabei zeigt dir die graue Linie deine eigene '
            + '&ndash; zuvor abgegebene &ndash; Sch&auml;tzung an, die orangefarbene Linie zeigt die Richtung und Entfernung, die tats&auml;chlich zwischen den beiden Orten liegt.<br/>'
            + '<img src="/de.akmiraketen.web-experiments/images/web_exp_feedback_lines.png">' 
            + '</p><br/>')
        d3.select('.content').append('a').attr('class', 'button').attr('href', '/web-exp/nextpage').text('Ok')
    }

    // --- Pinning / No Pinning Explanation View

    function render_pinning_intro() {
        var content = '<p class="textblock">In dem folgenden Block wirst du in jedem Durchgang eine Karte sehen und gebeten werden, einen Ort zu markieren. '
            + 'Klicke dazu mit der Maus in den kleinen Kreis beim Ortsnamen. '
            + 'Welchen der dargestellten Orte du markieren sollst, wird dir oberhalb der Karte angezeigt.</p>'
            + '<p class="textblock">Nach dem Markieren hast du ca. eine Minute Zeit, dir die Karte so genau wie m&ouml;glich einzupr&auml;gen. '
            + 'Stelle dir dazu vor, du wirst am markierten Ort ausgesetzt und sollst nun die Strecken zu den anderen Orten auswendig '
            + 'wiederfinden. Du kannst dabei den direkten Weg querfeldein gehen und bist nicht auf Wege angewiesen.'
            + '<h4 class="image">Abbildung 1:</h4><img src="/de.akmiraketen.web-experiments/images/web_exp_pinning_badingen.png"></p>'
        set_page_content(content)
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { init_marker_selection_view() })
    }

    function render_no_pinning_intro() {
        set_task_description('Anleitung')
        var content = '<p class="textblock">In diesem Block wirst du in jedem Durchgang eine Karte sehen.<br/>'
            + 'Du hast ca. eine Minute Zeit, dir die Karte so genau wie m&ouml;glich einzupr&auml;gen.<br/>'
            + 'Stelle dir dazu bitte vor, du wirst in der abgebildeten Region ausgesetzt und sollst nun die Strecken zwischen den Orten auswendig wiederfinden. Du kannst dabei den direkten Weg '
            + 'querfeldein gehen und bist nicht auf Wege angewiesen.</p>'
            + '<p class="textblock">Danach wirst du Rechenaufgaben gestellt bekommen und Sch&auml;tzungen hinsichtlich Richtung und Entfernung der Orte machen. '
            + 'Dazu wirst du gefragt, wie sicher du dir bei der Sch&auml;tzung warst.</p>'
        set_page_content(content)
        d3.select('.content').append('a').attr('class', 'button').attr('href', '/web-exp/nextpage').text('Ok')
    }

    // --- Icon View

    function init_marker_selection_view () {

        newCtrl.fetchParticipant(function (data) {

            var selected_marker_id = data.selected_marker_id

            set_task_description('')
            var message = '<p class="textblock">F&uuml;r das Pinnen wird dir ein Pin zur Verf&uuml;gung stehen.</p>'
                message += '<p class="textblock">Wie dieser Pin aussehen soll, kannst du unter den folgenden Alternativen ausw&auml;hlen. '
                    + ' Deine Auswahl legt das Aussehen des Pins f&uuml;r das gesamte Experiment fest.</p>'
                message += '<p class="textblock">Klicke dazu bitte den Pin an, mit dem du die Markierungen kennzeichnen m&ouml;chtest.</p>'
            set_page_content(message)
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

                // Render OK GUI
                d3.select('.content').append('br')
                d3.select('.content').append('a').attr('href', '#goto').attr('class', 'button disabled').html('weiter')

                d3.selectAll('li.symbol img').on('click', function () {
                    // Implement OK Button Next Handler
                    var iconId = this.id
                    newCtrl.doMarkIconPreference(iconId, function () {
                        if (common.debug) console.log("OK - Icon set", iconId) // ### render icon as silected
                        d3.select('.content a.button').attr('class', 'button')
                            .on('click', function (e) {
                               window.location.href = '/web-exp/nextpage'
                            })
                    }, function (error) {
                        console.warn("Fail - Icon preference could not be set!")
                    })
                    // GUI
                    d3.selectAll('img').attr('class', '')
                    this.className = "selected"
                })
                // Cache in app-model
                newModel.setIcons(data)
            }, false)
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

