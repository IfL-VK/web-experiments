
## Web-Experiments Module for DeepaMehta 4

A template based web-service module facilitating the generation and realisation of web-based user studies.

Compared to jsPsych this web-application is more of a complete setup for developers who are familiar with writing Java based REST APIs and developing JavaScript Multi-Page applications. It basically frees you of things like _session management_, handling _experiment configuration_ per participant and _creating arbitrary reports_ per `Screen`.

The app is realized as a [DeepaMehta 4](http://www.github.com/jri/deepamehta) Plugin, which is a FLOSS development framework and Java based implementation of a [microservice architecture](http://martinfowler.com/articles/microservices.html). It therefore uses Neo4j, Apache Lucene and Jetty among other technologies. You can find out more about DeepaMehta 4 on its github page. The DM 4 Standard Distribution males it easy to deploy this application on your desktop or web server.

The central concepts in this application are `Screen` and `Screen Report`.<br/>
Additionally you write a `Screen Configuration` file in TAB seperated CSV to control the flow of Screens across all your participants.<br/>
This release comes with three standard templates implementing a "Welcome", "Start" and "Pause" Screen. These should get you going when implementing your own `Screens` for a user study.

## Usage of `screen.js`

For the realisation of new `Screens` (think of as `Task Implementation` or `Introduction Page`) developers are assisted when importing `screen.js` and using the screen-Object (see [here](help/README.md) and [here](src/main/resources/web/script/screen.js) to read about all currently available utility methods).

<pre>
<!-- Import the Screen-Interface into your HTML Template -->
<script src="/de.akmiraketen.web-experiments/script/screen.js"></script>
// 0) Override this method to initialize your screen
// At this point you can easily access data about the current Participant and Screen
screen.init = function() {
    // 1) Access to this screen configuration, e.g. to read out its "condition"
    var configuration = screen.getConfiguration()
    // 2) initialize reporting for this screen and user
    screen.startReport()
    // 3) set this screen as seen so the participant does not see this screen again
    setTimeout(screen.setScreenAsSeen, 5000)
}
// 1) Example to report a "click" action (for the screen and user) when the "map" is clicked
map.on('click', function(e) {
    screen.postActionReport({
        "de.akmiraketen.action_name": "de.akmiraketen.click",
        "value": {
            "name" : e.latlng.toString(),
            "type": "map",
            "id" : "-1"
        }
    })
})
</pre>

## Installation & Configuration

Please find help on this at the [help/README.md](https://github.com/mukil/web-experiments/tree/master/help) page in this repo.

## Development

If you are not familiar with DeepaMehta 4 Plugin Development you can get kickstarted in this [PluginDevelopmentGuide](https://trac.deepamehta.de/wiki/PluginDevelopmentGuide).

### Release History

#### 0.4 Release, Upcoming

Source Code:
https://github.com/mukil/web-experiments/tree/rewrite

Download:
https://github.com/mukil/web-experiments/releases

New in this Version:
Complete Rewrite of version 0.2. Comes now with support for _Screen Templates_ and a JavaScript Interface (`screen.js`) supporting developers in implementing new Templates. Additionally it comes with a revised Screen Configuration model and reduced complexity for reporting arbitrary data.

In this version we released a simple set of three standard templates (in folder `templates/standard` in this repository) for `Web-Experiments` which hopefully get you going quick when implementing your experiment.

#### 0.3 Release, 01. Dezember 2015

Source Code:
https://github.com/mukil/web-experiments/tree/b2b483306558388d1957eade5c29612b82dee0fa

Download:
https://github.com/mukil/web-experiments/releases

New in this Version:
The source code repository now comes with an extensive documentation.

#### 0.2 Release, 15. August 2015

The very first Release, developed under heavy constraints.

Source Code:
https://github.com/mukil/web-experiments/commit/fcf95efa04440932793244ecdae90c7bee64f533

Download:
https://github.com/mukil/web-experiments/releases


#### Author

Malte Reißig, 2014-2016
Leibniz-Institut für L&auml;nderkunde e. V.

