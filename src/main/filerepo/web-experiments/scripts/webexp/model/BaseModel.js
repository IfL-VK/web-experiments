
define(function () {

    var username = undefined
    var marker = undefined
    var icons = undefined

    // var trials = undefined

    var view_state = undefined

    function modelBase(title) {
        this.title = title;
    }

    modelBase.prototype = {
        getTitle: function () {
            return this.title;
        },
        setUsername: function (user) {
            this.username = user
        },
        getUsername: function () {
            return this.username
        },
        setMarker: function (iconPath) {
            this.marker = iconPath
        },
        getMarker: function () {
            return this.marker
        },
        setIcons: function (cons) {
            this.icons = cons
        },
        getIcons: function () {
            return this.icons
        }
    };

    return modelBase;
});
