
## Web Experiments

A [DeepaMehta 4 Plugin](http://www.github.com/jri/deepamehta) for designing and implementing experiments in the browser for subject groups using web-cartography.

The applications features include:

 * Varying _Trial Configurations_ per _Participant_
 * Mapping Component: Leaflet has support for serving static bitmap files or maps of your favourite Tile Map Server
 * Pinning: Memorization of maps based on a _Trial Condition_
   ("Pinning" and "No Pinning" are currently the only ones implemented)
 * Estimations: Submitting values for direction and distance in between two configured _Places_
 * Introduction and Training Mode (per _Trial Condition_), Marker Selection, Filler tasks (Multiplication), Timing
 * Application model allows for _resuming_ once started trials at a later point in time
 * Reporting: CSV Export of the data of all user sessions

As a web-application it is client-server application with multi-user support and a Neo4J file database under the hood.

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

