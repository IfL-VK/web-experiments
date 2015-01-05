
// The main module called by/for the "new" page.

define(function (require) {

    var newCtrl     = require('./controller/newCtrl')
    var newModel    = require('./model/newModel')
    var d3          = require('d3')



    // ------ Initialization of client-side data

    function init_page () {

        init_user_view()
        init_marker_selection_view()
        get_all_trial_configs()

    }

    function init_user_view () {
        newCtrl.fetchUser(function (data) {
            // 
            var username = data
            // GUI
            d3.select('.username').text(username)
            // OK
            newModel.setUsername(username)

        }, false)
    }

    function init_marker_selection_view () {
        
        newCtrl.fetchAllMarker(function (data) {
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

    function get_all_trial_configs () {

        newCtrl.fetchAllTrials(function (trials) {
            console.log(trials)
            var links = d3.select('.trials').selectAll('a').data(trials.items)
                .attr('href', function (d) { 
                    return '/web-exp/trial/' + d.id + '/pinning' 
                })
                .attr('class', 'trial-links')
                .text(function (d) {
                    console.log(d.value)
                    return d.value 
                })

                links.enter()
                    .append('a')
                    .attr('href', function (d) { 
                        return '/web-exp/trial/' + d.id + '/pinning' 
                    })
                    .text(function (d) {
                        console.log(d.value)
                        return d.value 
                    })

                links.exit().remove()

        }, false)
    }

    // ------------ Some rendering methods


    /* $(function () {
        controller.render(lib.getBody());
    }); **/

    init_page()

});

