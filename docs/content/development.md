
## Web Experiments Developers Guide

This development guide aims to support uni*x based developers (including Macintosh).

### Setting up the development environment

For setting up your development environment, please follow all the instructions linked in the following step-by-step guide.

* Install DeepaMehta **4.4.3** from source like (in short):<br/>
  <pre>
  git clone https://github.com/jri/deepamehta.git
  cd deepamehta
  git checkout 4.4.3
  mvn clean install -P all</pre>
  For a longer version see the [official guide](https://trac.deepamehta.de/wiki/PluginDevelopmentGuide) to build DeepaMehta 4 from source.


### Install CSV Plugin as dependency
* Register (after downloading) a release of the `dm44-deepamehta-csv` [from here](http://download.deepamehta.de/dm44-deepamehta-csv-0.0.4.jar), place it on your hard-disk and register its  folder in the main deepamehta `pom.xml` (the one in your DeepaMehta home directory)

### Build the web-experiments plugin from source and install it
 
* Clone this `web-experiments` repository (which is a dm4-plugin) onto your computer with git<br/>
  `git clone https://github.com/mukil/web-experiments.git`
* Build the `web-experiments` plugin through typing<br/>`cd web-experiments`and<br/>`mvn clean package` into your terminal
* Register the `web-experiments` plugin:<br/>
  Enter the path to your newly created `target` folder (inside of your web-experiments folder) into the main deepamehta `pom.xml`, too (this time just append `/target` to your web-experiments path)
* Now you are ready to start the platform with these two plugins

#### Optimization:

Additional, for a quicker JS/HTML **save/refresh** development turnaround:

* Adapt the value in the main DeepaMehta `pom.xml`-file of `dm4.filerepo.path` to a location including all the javascript sources of this project.

### Additional System Configuration

* To make the instance world-wide and publicly available you need to ensure 
  to set installation properties in `<DeepaMehta4-Directory>/conf/conf.properties`:
  `dm4.filerepo.path` to a writeable folder destination
   optional: `dm4.security.subnet_filter` resp. to your networked audience, e.g. 0.0.0.0/0 for ALL
  
* Additionally, switch to file-logging as desrcibed in `<DeepaMehta4-Directory>/conf/logging.properties`
  Especially do make the `FilesPlugin` less verbose through adding the following line to `<DeepaMehta4-Directory>/conf/logging.properties`
  `de.deepamehta.plugins.files.FilesPlugin.level=WARNING`

### Load Basic Trial Configuration

Please find all the details about how to set-up a basic experiment at [this page](/).

### Code Overview

#### Server Side Development

The server side architecture is easily extendable through writing a Java method and annotating it (like the others) using JAX-RS. Hot-deployment of your new java-code happens automatically after using the `mvn clean package` command for building the plugin.

It is written in Java (1000 loc) responsible to:
 * calculate the routes to the respective AJAX multi-page app per user and per configuration and
 * for exposing important parts of the the storage layer (managing and loading trials and sending trial reports) via JAX-RS, HTTP and JSON.

#### Client Side Development

The client's architecture is based on the [RequireJS Multi-Page](https://github.com/requirejs/example-multipage) module and has overall four pages.

 * *welcomePage*: Handles most text screens, more specifically all trials of type _intro_, _start_, _pause_, _welcome_ and _finish_<br/>
   Does this always in respect to the _Trial condition_, because, e.g. the _intro_ differs for "Pinning" and "No Pinning" and the _start_ for "Pinning" serves a marker selection dialog
 * *pinningPage*: Handles the first part of every _Trial_ (also a pinning trial in so called "Practice Mode") in both "Conditions" (=Pinning/No Pinning),<br/>
   * memorizing a _Map_ with the pre-configured condition: "Pinning" or "No Pinning"
   * performing intermediary tasks: curerntly users need to solve random multiplication task for 30 secs
 * *estimationPage*: Handles the second part of every _Trial_ with no difference regarding the "Trial Condition" (but also estimations in the so called "Pracice Mode"). Especially this page provides <br/>
   * five _estimations_ for **direction** and **distance** between the two configured places
   * followed by an input dialog for a _confidence_ rating<br/>
   with which users are asked to express a numeric score concerning their confidence in their latest estimation

The client side code comes with a rest client (written in D3) to communicate with the applications REST API. All DOM manipulations are implemented with the help of [D3JS](http://www.d3js.org) as well.

The map component is a basic [LeafleftJS](http://www.leafletjs.com) integration.

There are two _basic_ configuration files (CSV) to set up the base for any experiment, _Places Config_ and _Map File Config_.

There is one _advanced_ configuraton file (CSV) setting up the combination of Places and Maps per user/participant. It is the so called _Trial Config_ which is loaded per "Username" and helps the application to determine which page each user sees next.

To get started with adapting, extending or customizing this applicatino please start with the corresponding [PluginDevelopmentGuide](https://trac.deepamehta.de/wiki/PluginDevelopmentGuide) of DeepaMehta 4.

