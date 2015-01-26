
## Web Experiments Help

For installation, please follow the instructions to:

* Install DeepaMehta 4.4.x from source
* Download and unzip the binary release of DeepaMehta 4.4.x
* Download and place a binary release of `dm44-deepamehta-csv` in the `bundle` directory
* Download and install a binary release of the `web-experiments` in the `bundle` directory
* Choosd and run the corresponding `deepamehta-start` script for your operating system

## Download

wget http://download.deepamehta.de/deepamehta-4.4.2.zip
wget http://download.deepamehta.de/dm44-deepamehta-csv-0.0.4.jar

If these fail, probably due to a new dm release, the following addresses should resolve:

wget http://download.deepamehta.de/archive/4.4/deepamehta-4.4.2.zip
wget http://download.deepamehta.de/archive/4.4/dm44-deepamehta-csv-0.0.4.jar

## Config

* Ensure to set installation properties in `<DeepaMehta4-Directory>/conf/conf.properties`:
  `dm4.filerepo.path` to a writeable folder destination
  `dm4.security.subnet_filter` resp. to your networked audience, e.g. 0.0.0.0/0 for ALL
  
* Switch to file-logging in `<DeepaMehta4-Directory>/conf/logging.properties`

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


Author: Malte Reißig, 2014

