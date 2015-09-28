
## dm4-ice: Interactive cartographic experiments with DeepaMehta 4

A seemingly complicated web application to generate and conduct interactive cartographic experiments in the web-browser.

This plugin provides web application developers and researchers a _structure_ for designing and running behavioral experiments online. Compared to jsPsych this web-application is more of a complete setup for developers who are familiar with writing Java based REST APIs and developing JavaScript Multi-Page applications.

[DeepaMehta 4](http://www.github.com/jri/deepamehta) is a plugin development framework and a [microservice architecture](http://martinfowler.com/articles/microservices.html) (shipping with e.g. neo4j and Jetty) making it easy to deploy this application either on your desktop machine or on a web server.

_dm4-ice_ basically handles things like user and session management, enables you to load and edit three types of configuration files and it determines which trial runs next for whom and when.

The differences to jsPsych on the technology side are:
* a persistent semantic network in the backend (with neo4j-storage),
* configuration files can be managed and edited by end-users (using the standard dm4-webclient)
* writing a custom report (output of the experiment) is not part of the storage mechanisms but a distinct operation and thus better customizable and/or extendable
* server side Java/JAX-RS facility to easily write and extend this applications REST API,
* a simple Multi-Page AJAX architecture on client-side (require.js)

The current applications features include:
 * Varying _Trial Configurations_ per _Participant_
 * Mapping Component: Leaflet is set up for serving either (A) static bitmap files or generated bitmaps of your (B) Tile Map Server, depending on your _Map configuration_-file
 * Place Label: Each location and label on a map are editable via a _Place Configuration_-file
 * Pinning Task: Memorization of maps based on a _Trial Condition_
   ("Pinning" and "No Pinning" are currently implemented)
 * Estimation Task: Submitting values for direction and distance in between two configured _Places_
 * Introduction and Training Mode (per _Trial Condition_), Marker Selection, Filler tasks (Multiplication), Timing
 * The applications architecture allows for _resuming_ once started trials at a later point in time (participants just going throug the introductions again)
 * Reporting: Custom CSV Export operations implement to collect the usage data over all participants

## Installation & Configuration

Please find help on this at the [help/README.md](https://github.com/mukil/web-experiments/tree/master/help) page in this repo.

## Development

If you are not familiar with DM 4 developer you will get kickstarted on [our docs page for new developers](http://mukil.github.io/web-experiments/development/).
 
#### Author

Malte Reißig, 2014-2015
Leibniz-Institut für L&auml;nderkunde e. V.

