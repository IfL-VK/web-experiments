
// The main module called by/for the "new" page.

define(function (require) {

    var newCtrl     = require('./controller/newCtrl')
    var newModel    = require('./model/newModel')
    var d3          = require('d3')
    var common      = require('common')



    // ------ Initialization of client-side data

    function init_page () {

        newCtrl.fetchParticipant(function (data) {
            
            var username = data.value

            // 1 load marker selection
            init_marker_selection_view()

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

    function init_marker_selection_view () {
        
        newCtrl.fetchAllMarker(function (data) {
            
            if (data.length === 0) d3.select('.content').html('<p class="warning">To enable personalization, ' 
                    + 'please copy some icons into the symbols folder of the file repository.</p>')
            // 
            var iconPaths = data
            // GUI
            var $list = d3.select('.content')
                .append('ul').attr('class', 'marker-selection').attr('id', 'icons')
            for (idx in iconPaths) {
                var iconPath = '/filerepo/' + iconPaths[idx].path
                $list.append('li').attr('class', 'symbol').append('img').attr('src', iconPath)
            }
            // OK 
            newModel.setIcons(data)
                
        }, false)
    }
    
    function render_start_session_dialog() {
        var hello = d3.select('.title .username').text('!')
        var trials = d3.select('.trials').remove()
        var content = d3.select('.content')
            content.html('<p>Zum starten des Experiments bitte eine ID f&uuml;r die Versuchsperson ' 
                + 'eintragen und mit <b>OK</b> die Sitzung starten:<br/><input class="vp-id" type="text" '
                + 'name="username" placeholder="VP <Nr>"><input type="button" value="OK" class="login-btn" '
                + 'name="submit"></p>')
        content.select('.login-btn').on('click', function (){
            var element = d3.select('input.vp-id')[0][0]
            newCtrl.startSession(element.value, function (){
               window.location.reload() 
            }, common.debug)
        })
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
    
    // --- Run this script when it is called/loaded

    init_page()

});

