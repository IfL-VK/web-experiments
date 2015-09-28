
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

 * Get stared with the corresponding [PluginDevelopmentGuide](https://trac.deepamehta.de/wiki/PluginDevelopmentGuide)
 * Start DeepaMehta with both, the CSV and the Web-Experiments plugin (find [this page](https://github.com/mukil/web-experiments/tree/master/help) for more details on this) 
 * Visit [localhost:8080/web-exp/](http://localhost:8080/web-exp/)
 * Copy some PNG files as choices for _markers_ into the `web-experiments/symbols` folder (available in the filerepo)
 * Start editing the `.js` files under `web-experiments/scripts` (directly from within your filerepo) with your

### Code Overview

The **server side** is written in Java (1000 loc) and is easily extendible:
 * routes the AJAX multi page app per user and per configuration and
 * exposes parts of the the storage layer via JAX-RS, HTTP and JSON.

The **client side** architecture is the [multipage webapp](https://github.com/requirejs/example-multipage) example of @requirejs and overall there are _three_ pages/screens:

 * *welcomePage*: Handles all trials of type _intro_, _start_, _pause_, _welcome_ and _finish_<br/>
   Does this always in respect to the _Trial condition_, because, e.g. the _intro_ differs for "Pinning" and "No Pinning" and the _start_ for "Pinning" serves a marker selection dialog
 * *pinningPage*: Handles the first part of every _Trial_ (also a pinning trial in so called "Practice Mode") in both "Conditions" (=Pinning/No Pinning),<br/>
   * memorizing a _Map_ with the pre-configured condition: "Pinning" or "No Pinning"
   * performing intermediary tasks: curerntly users need to solve random multiplication task for 30 secs
 * *estimationPage*: Handles the second part of every _Trial_ with no difference regarding the "Trial Condition" (but also estimations in the so called "Pracice Mode"). Especially this page provides <br/>
   * five _estimations_ for **direction** and **distance** between the two configured places
   * followed by an input dialog for a _confidence_ rating<br/>
   with which users are asked to express a numeric score concerning their confidence in their latest estimation

There are two basic configuration files (CSV) to set up the base for any experiment, _Places Config_ and _Map File Config_.

There is one configuraton file (CSV) setting up the combination of Places and Maps per user/participant in a so called _Trial Config_.

Get stared with the corresponding [PluginDevelopmentGuide](https://trac.deepamehta.de/wiki/PluginDevelopmentGuide)

 
#### Author

Malte Reißig, 2014-2015
Leibniz-Institut für L&auml;nderkunde e. V.

