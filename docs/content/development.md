
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


### Server Side Development

The server side architecture is easily extendable through writing a Java method and annotating it (like the others) using JAX-RS. Hot-deployment of your new java-code happens automatically after using the `mvn clean package` command for building the plugin.

.. (to be continued).

### Client Side Development

The client's architecture is based on the [RequireJS Multi-Page](https://github.com/requirejs/example-multipage) module and has overall four pages.

* `welcome.js`: handles most text, image (all DOM) manipulations for "introduction.html", "pause.html", "start.html", "welcome.html" in _two conditions_
* `pinning.js`: handles "pinning.html" in _two conditions_, including the mathematical filler-task
* `estimation.js`: handles "estimation.html" including submission of certainty-values/ratings

The REST-Client and all DOM manipulations are implemented with the help of [D3JS](http://www.d3js.org).

The map component is a basic [LeafleftJS](http://www.leafletjs.com) integration.

