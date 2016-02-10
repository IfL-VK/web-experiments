
## Web Experiments Developers Guide

For setting up your development environment, please follow all the instructions linked in the following step-by-step guide.

* Install DeepaMehta **4.4.3** from source like<br/>
  <pre>git clone https://github.com/jri/deepamehta.git
  cd deepamehta
  git checkout 4.4.3
  mvn clean install -P all</pre>
  Or use the [official guide](https://trac.deepamehta.de/wiki/PluginDevelopmentGuide) to build DeepaMehta 4 from source.
* Register (after downloading) a release of the `dm44-deepamehta-csv` [from here](http://download.deepamehta.de/dm44-deepamehta-csv-0.0.4.jar), place it on your hard-disk and register its  folder in the main deepamehta `pom.xml` (the one in your DeepaMehta home directory)
* Clone this `web-experiments` repository (which is a dm4-plugin) onto your computer
* Build the `web-experiments` plugin through using `cd web-experiments` and `mvn clean package`<br/>
* Register the `web-experiments` plugin:<br/>
  Enter the path to your newly created `target` folder (inside of your web-experiments folder) into the main deepamehta `pom.xml`, too (this time just append `/target` to your web-experiments path)
* Now you are ready to start the platform with these two plugins

## Platform Configuration

* To make the instance world-wide and publicly available you need to ensure
  to set installation properties in `<DeepaMehta4-Directory>/conf/conf.properties`:
  `dm4.filerepo.path` to a writeable folder destination
   optional: `dm4.security.subnet_filter` resp. to your networked audience, e.g. 0.0.0.0/0 for ALL
  
* Additionally, switch to file-logging as desrcibed in `<DeepaMehta4-Directory>/conf/logging.properties`
  Especially do make the `FilesPlugin` less verbose through adding the following line to `<DeepaMehta4-Directory>/conf/logging.properties`
  `de.deepamehta.plugins.files.FilesPlugin.level=WARNING`

### Web-Experiments

To start an experiment and see anything you must upload a "Screen Configuration" file. You can use the examplary one in this very folder named "screen_configuration_example.csv" but please adapt the "de.akmiraketen.screen_template" column to contain a valid path to a HTML Template which should be shown to the participant. The template files must be located underneath your file repository (see Platform Configuration above).

### Template Development

Use `screen.js`as explained in the main README of this repository (one level above).

The `screen` JavaScript object provides you the following functionality:

Implement your own `screen.init` method to initialze your template

**screen.getConfiguration()** - Returns the resp. "Screen Configuration" for this template

**screen.getParticipant()** - Returns the resp. "Participant" data for the session

**screen.setScreenAsSeen()** - Comment this during template development, it sets the screen as seen and will make sure that the next page-refresh will deliver the screen configured up next (for the logged in participant)

**screen.startReport()** - Initializes a "Screen Report" for this screen and user

**screen.postActionReport({})** - Adds an "Action Report" to the resp. "Screen Report"- To see which types of Actions already ship in your release please read the "migration2.json" file in "src/main/resources/migrations" which is part of this repo.

### Web-Experiments: Screen Configuration

If you are familiar with DeepaMehta 4 please have a look at the "active_configuration_used.png" image file in this folder (above). It visualizes a proper configuration. Additionally it shows that there already have been "Screen Reports" collected for this participant and the configured screens.

**To configure** open http://localhost:8080/de.deepamehta.webclient in your browser (IE is not supported) as that is the administrative GUI. Do _Login_ as `admin` (no password needed) to upload a `Screen Configuration`.

#### Manually Configure Screens per Participant

This method is OK during software development or experiments with just a few screens.

* Click in the *Toolbar*: "By Type" > "Topic Type" > "Search" to reveal all so called "Topic Types"
* Select `Screen Configuration` from the search results and
* Select `Import CSV` command on the lower right side of the screen.
* Select a `Screen Configuration` File from your Desktop and press `Upload`.

After the successfull import of all your _Screen Configurations_, reveal one in the map through selecting one and associate it with the _Username_ of your choice via an _Active Configuration_ association.

#### Load a configuration file containing many `Screens` on a per user base

_Screen_ configurations can be loaded on a per-user base, all screens get automatically associated (as _Active Configuration_) association with the _Username_ selected.

* Use the "Create"-Menu to start a "New File Browser"
* Upload all "Screen Configuration" files into a folder of your choice
* Reveal the "Screen Configuration" of your interest
* Reveal the "Username Configration" to be setup with that screen configuration
* Associate the two items via an "Active Configuration" association
* Select the _Username_ topic again and
* Select the "Load Screen Configuration" command on the lower right side of the screen

If no error dialog is shown your experiment configuration was loaded successfully and you can double check this by selecting the _Username_ topic on the map again, you will see all the screens as items configured for it.

### Start Experiment

* Open http://localhost:8080/experiment/ and enter an unused `VP <NR>`, e.g. `VP 10`

## Access Reporting

The syetem simply generates a report which aggregates all actions of all users reported in the lifetime of your datbase/installation.

Please note: To do so you must be logged in.

* Open http://localhost:8080/experiment/report/generate in your browser and 
* Do "Save as" to write the contents of this page into some .CSV file

Author: Malte Reißig, 2014-2015
Leibniz-Institut für L&auml;nderkunde e. V.


