
## Web Experiments Help

For installation, please follow the instructions to:

* Install DeepaMehta **4.4.3** from source like<br/>
  <pre>git clone https://github.com/jri/deepamehta.git
  cd deepamehta
  git checkout 4.4.3
  mvn clean install -P all</pre>
  Or[use this (linux-based) guide](https://trac.deepamehta.de/wiki/PluginDevelopmentGuide) to build dm4 from source.
* Download a release of the `dm44-deepamehta-csv` [from here](http://download.deepamehta.de/dm44-deepamehta-csv-0.0.4.jar), place it on your hard-disk and register its  folder in the main deepamehta `pom.xml` (the one in your DeepaMehta home directory)
* Clone this `web-experiments` repository (which is a dm4-plugin) onto your computer
* Bild the `web-experiments` plugin through using `cd web-experiments` and `mvn clean package`<br/>
* Register the `web-experiments` plugin:<br/>
  Enter the path to your newly created `target` folder (inside of your web-experiments folder) into the main deepamehta `pom.xml`, too (this time just append `/target` to your web-experiments path)
* Further change in the main deepamehta `pom.xml` the option/line called `dm4.filerepo.path` so that it points to our javascript source folder under `web-experiments/src/main/filerepo`
* Now you are ready to start the platform with these two plugins

## Config

* Ensure to set installation properties in `<DeepaMehta4-Directory>/conf/conf.properties`:
  `dm4.filerepo.path` to a writeable folder destination
   optional: `dm4.security.subnet_filter` resp. to your networked audience, e.g. 0.0.0.0/0 for ALL
  
* Switch to file-logging in as desrcibed in `<DeepaMehta4-Directory>/conf/logging.properties`

* Make FilesPlugin less verbose through adding the following line to `<DeepaMehta4-Directory>/conf/logging.properties`
  de.deepamehta.plugins.files.FilesPlugin.level=WARNING

### Load Basic Configuration

Open http://localhost:8080/de.deepamehta.webclient as the administrative GUI and do _Login_ as `admin` (no password needed).

To _upload_ the three basic configuration files describing your experiment complete the following steps:

* Click in the *Toolbar*: "By Type" > "Topic Type" > "Search" to reveal all so called "Topic Types"
* Find and click in the *Page Panel* (showing your _Search Results_): "Map File Config", "Place Config" and "Trial Config"
* Select each of these three _Topic Types_ and execute the *Import CSV* upon them

If no error dialog is shown your experiment configuration was loaded successfully. You should now give this configuration a test run.

### Start experiments

* Open http://localhost:8080/web-exp/ and enter an unused `VP <NR>`, e.g. `VP 10`

## Generate a full report

* Open http://localhost:8080/web-exp/report/generate in your browser and do "Save as" to write the contents of this page into some .CSV file

## Note

At any given time there can be just one specific set of configuration files loaded into the application and the report depends on the settings configured. This means: Always generate a report before loading a new set of configuration files into the system.


Author: Malte Reißig, 2014-2015
Leibniz-Institut für L&auml;nderkunde e. V.


