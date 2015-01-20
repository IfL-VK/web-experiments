
// The main module called by/for the "new" page.

define(function (require) {

    var newCtrl     = require('./controller/newCtrl')
    var newModel    = require('./model/newModel')
    // var d3          = require('d3')
    var common      = require('common')

    var view_state  = "" // values may be "", "finish", "welcome", "pause" or "icon", "intro"

    // ------ Initialization of page (according to view_state)

    function init_page () {

        view_state = common.parse_view_state_from_page()
        console.log(view_state)
        // if "intro", "break" or "pract" mark trial as seen, too

        if (view_state.indexOf("intro") !== -1 ||
            view_state.indexOf("pause") !== -1 ||
            view_state.indexOf("start") !== -1) {

            var trialId = common.parse_trial_id_from_resource_location()
            if (common.verbose) console.log(trialId)

            if (view_state.indexOf("intro") !== -1) {
                // --- Render introduction views
                render_pinning_intro()
            } else if (view_state.indexOf("pause") !== -1 ||
                       view_state.indexOf("start") !== -1) {
                //
            }
            newCtrl.doMarkTrialAsSeen(trialId) // ###
            
        } else { // --- Render icon or welcome View
            
            newCtrl.fetchParticipant(function (data) {
            
                var marker_id = data.selected_marker_id
                var username = data.value

                if (view_state === "icon") {
                    // 2 load marker selection
                    init_marker_selection_view(marker_id)
                } else if (view_state === "welcome" || view_state === "") {
                    // 1 show welcome message
                    init_welcome_view()
                    console.log("Render welcome message")
                } else if (view_state === "finish") {
                    // ###
                    set_task_description('Done')
                    set_page_content('<p class="textblock">Thank you!</p>')
                    console.log("Render finish message")
                } else if (view_state === "pause") {
                    // ###
                    console.log("Render pause view")
                }

                /** 2 load trials
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
                newModel.setUsername(username) **/
            }, function (error) {
                render_start_session_dialog()
                throw Error("No session to start the experiment - Please log in as \"VP <Nr>\"")
            }, false)
        }

    }

    // --- Intro 1 View

    function render_pinning_intro() {
        var content = 'In den folgenden &Uuml;bungen wirst du eine Karte sehen und gebeten werden, <b>einen Ort zu markieren</b>.'
            + 'Klicke dazu mit der Maus in den kleinen Kreis beim Ortsnamen.<br/>'
            + 'Welchen der dargestellten Orte du markieren sollst, wird dir oberhalb der Karte angezeigt.</p><p>Nach dem Markieren hast '
            + 'du noch etwas Zeit dir die <b>Karte so genau wie m&ouml;glich einzupr&auml;gen</b>.'
            + 'Stelle dir dazu vor, du wirst am markierten Ort ausgesetzt und sollst nun die Strecken zu den anderen Orten auswendig '
            + 'wiederfinden. Du kannst dabei den direkten Weg querfeldein gehen und bist nicht auf Wege angewiesen.'
            + '<br/><br/><img src="/de.akmiraketen.web-experiments/images/web_exp_pinning_badingen.png">'
        set_page_content(content)
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_filler_intro() })
    }

    function render_filler_intro() {
        set_page_content('<p class="textblock">Nachdem du dir die Karte eingepr&auml;gt hast, erh&auml;ltst du Rechenaufgaben, die du bitte so schnell '
            + 'und so genau wie m&ouml;glich beantwortest, indem du die Ergebniszahl eintippst.</p>'
            + '<p class="textblock">...</p>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_estimation_intro() })
    }

    function render_estimation_intro() {
        set_page_content('<p>Nach den Rechenaufgaben werden dir zwei Orte genannt, die du bereits auf der Karte zuvor kennen gelernt hast.</p>'
            + '<p class="wichtig">Bitte sch&auml;tze, wie weit der eine vom anderen genannten Ort entfernt liegt.</p>'
            + '<p>Dazu klickst du bitte in den Kreis in der Mitte des Bildschirms und ziehst die entstehende Linie so lange, '
            + 'bis die L&auml;nge mit der Distanz zwischen den beiden Orten &uuml;bereinstimmt. Au&szlig;erdem drehst du die Linie so, dass auch die '
            + 'Richtung zwischen den beiden Orten deiner Erinnerung nach stimmt. Sobald du die Linie losl&auml;sst, ist deine Antwort gespeichert.</p>'
            + '<br/><br/><img src="/de.akmiraketen.web-experiments/images/web_exp_estimation_screen_blank.png"><br/><br/>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_certainty_intro() })
    }

    function render_certainty_intro() {
        set_page_content('<p class="textblock">Anschlie&szlig;end erscheint eine Seite, auf der du angeben sollst, wie sicher du dir in der Sch&auml;tzung warst.</p>'
            + '<br/><img src="/de.akmiraketen.web-experiments/images/web_exp_confidence.png"><br/><br/>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '#').text('weiter')
            next.on('click', function (e) { render_practice_intro() })
    }

    function render_practice_intro() {
        set_page_content('<p class="textblock">Du f&uuml;hrst f&uuml;nf Sch&auml;tzungen zu jeder Karte durch, bevor du die n&auml;chste Karte pr&auml;sentiert bekommst.</p>'
            + '<p class="textblock">Insgesamt bekommst du zwei Bl&ouml;cke mit je 15 Karten pr&auml;sentiert.<br/>Zum Abschluss bitten wir dich, noch einen Fragebogen auszuf&uuml;llen.</p>'
            + '<p class="textblock">Um mit den &Uuml;bungen beginnen zu k&ouml;nnen, klicke auf weiter</p>'
            + '<br/><br/><br/><br/>')
        var next = d3.select('.content').append('a').attr('class', 'button').attr('href', '/web-exp/nextpage').text('weiter')
            // next.on('click', function (e) { render_practice_intro() })
    }

    // --- Welcome View

    function init_welcome_view () {
        set_task_description("Willkommen zu unserem Experiment")
        var content = '<p class="textblock">Das Experiment dauert ca. 2 Stunden und gliedert sich in drei Bl&ouml;cke. '
            + 'Im ersten und zweite Block wirst du verschiedene Landkarten sehen und dir diese einpr&auml;gen, '
            + 'um zur abgebildeten Landschaft Fragen zu beantworten. In einem letzten, k&uuml;rzeren Block erh&auml;ltst du'
            + ' einige Frageb&ouml;gen.</p><br/>'
            + '<p class="wichtig">Bitte f&uuml;hre den Versuch konzentriert und gewissenhaft durch!<p><br/>'
	    + '<p class="welcome"><a href="/web-exp/icon" class="button">Los geht\'s</a></p>'
        set_page_content(content)
    }

    // --- Icon View

    function init_marker_selection_view (selected_marker_id) {
        set_task_description('')
        var message = "<p>In einem der beiden Bl&ouml;cke, in denen du dir Karten einpr&auml;gen sollst, wirst du "
            + "bei jeder neuen Karte aufgefordert einen Ort auf der jeweiligen Karte zu markieren. Dazu wird dir ein Pin zur "
            + "Verf&uuml;gung stehen.</p>"
            message += "<p>Wie dieser Pin aussehen soll, kannst du unter den folgenden Alternativen ausw&auml;hlen. "
                + " Deine Auswahl legt das Aussehen des Pins f&uum;r das gesamte Experiment fest.</p>"
            message += "<p>Klicke dazu bitte den Pin an, mit dem du die Markierungen kennzeichnen m&ouml;chtest.</p>"
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
            d3.select('.content').append('a').attr('href', '#goto').attr('class', 'button').text('Ok!')
                    .on('click', function (e){ render_go_to_intro() })
            d3.selectAll('li.symbol img').on('click', function () {
                // Implement OK Button Next Handler
                var iconId = this.id
                newCtrl.doMarkIconPreference(iconId, function () {
                    if (common.debug) console.log("OK - Icon set", iconId) // ### render icon as silected
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
    }
    
    function render_go_to_intro() {
        set_page_content('<p class="textblock">Bevor du den ersten Block startest, zeigen wir dir auf den folgenden Seiten, '
            + 'welche Aufgaben du bekommst und wie du diese l&ouml;sen kannst. Bitte lese diese Anleitung '
            + 'aufmerksam durch!</p><a href="/web-exp/nextpage" class="button">zur Anleitung</a>')
    }

    // Login View

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
                window.location.href = '/web-exp/welcome'
            }, common.debug)
        }
    }

    // --- GUI Helper

    function set_task_description (message) {
        d3.select("#title").html(message)
    }

    function set_page_content (message) {
        d3.select("div.content").html(message)
    }
    
    // --- Run this script when it is called/loaded

    init_page()

});

