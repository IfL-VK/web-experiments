
# Welcome to Web Experiments

This page describes how to setup the 'web-experiments' plugin as used by the IWM Tübingen in the first experiment of the DvEW-Project (March 2015). This web-experiment aimed to gather **five distance and direction estimations** from about 30 specific maps. Many participants were asked to **memorize** the map through either **personalizing** or **not personalizing** with their favourite symbol.

Here you can [download some slides](/examples/20150703-DvEW-Web-Experiments-Kurzvorstellung-Files-German.zip) presenting the basics of the application model and (in some graphics) the coherences between a _Trial configuration_ and an experiments _Timeline/Flow_.

It's core functionality regarding a **trial/participant** is to provide (in the following order):<br/>

* load arbitrary web-maps/web-cartographies (bitmap, tile map service)
* engage users to either _personalize_ or _not personalize_ a certain place during *memorization* of each map
* interpose mathematical filler task (*multiplication*)
* record one-click *distance* and *direction estimations*
* allow submission of a users *confidence* about each estimation


It's feature set from the **experiment designer** perspective includes:

* Loading and customizing all map media items through writing one _Map_ and one _Place_ configuration
* Configuring various composition of trials and trial blocks *per-user* through writing a number of _Trial_ configurations
* Collecting all estimation-data at server-side (centrally), being a multi-user system, generating a *.csv* file as report
* Collecting timing values (seconds_to_decision), adjust the seconds per a participant at maximum has to complete a _Trial_

To customize, adapt and further develop this plugin to any other experiment/usage scenario you will need to follow the [page for developers](/development).

This web-application realizes a client-server architecture with all database-operations wrapped into RESTful API Endpoints written in Java (JAX-RS, JSON) and a JavaScript based RequireJS based AJAX Multi Page HTML-Client.

### System Requirements

* Ubuntu, Macintosh or Windows PC 
* Java 6 (Java Runtime Environment) while Java 7 is untested but should also work
* A "modern" web-browser like Firefox, Google Chrome or Safari 5 (or newer) but no IE.

### Installation

Please see this [standard installation guide](https://github.com/jri/deepamehta#3-install-deepamehta) for details on how to 
* unzip and start the platform and 
* install the two bundles/plugins.

### Usage

After installation and startup of the application one finds the following setup to use:

Please log in to the [webclient](http://localhost:8080/de.deepamehta.webclient) as "admin" to be able to interactively configure your Database and/or upload map and/or other configuration files.

1. Map material & viewports
2. Place names
3. Trials


### Configuration

Open the [standard webclient](http://localhost:8080/de.deepamehta.webclient) as the administrative GUI for the database and do _Login_ as `admin` (no password needed).

To start either upload (or copy) the two basic configuration files (Maps, Places) along with each participant-specific (Trial config) to your server.

All configuration files uses the "|"-character to seperate the values in each line. One line in the file equals one configuration entry.

Important to note is that the values in the very first column across all lines must be _unique_ across this very configuration file at hand.

During installation this plugin creates three folders in the file-repository (folder) you configured to be used by DeepaMehta 4.

* `maps` - Used to place/upload all map files to.
* `symbols` - Used to place/upload all icons/marker (used for personalization) files to.
* `scripts` - Empty folder (often used to copy the javascript sources into to get a save/reload workflow during development)

#### Symbol Configuration

To setup the available collection of symbols/markers (your participant are allowd to choose from during your experiment) you need to upload all of them into the `symbols`-folder of your DM4 file-repository.

You can do so interactively via creating a "New File Browser", then navigate into the folder `web-experiments` and into the `symbols`-folder. Within there you call the "Upload File" to choose and upload your icons.

#### Map Configuration

A complete example of a valid map configuration file can be found [here](/examples/mapfile-config-4.txt).

The declaration of a filename (3rd column) supersedes any declaration of zoom level (4) and latitude/longitude values (5,6 column). Vice versa a standard OpenStreetMap base map layer is loaded if the filename value is empty but the other three columns are set.

To load the map configuration file into the database, complete the following actions in the webclient:<br/>

* Click in the *Toolbar*: "By Type" > "Topic Type" > "Search" to reveal all so called "Topic Types"
* Find and click in the *Page Panel* (showing your _Search Results_): "Map File Config"
* Select "Map File Config" on the left side of your screen, execute the *Import CSV* upon it, choose your "Map Configuration"-CSV from your hard-disk and press "Upload".

After a successfull command all _maps_ appear in the "Page Panel" (right side) listed with their IDs. Clicking on one let's you inspect the entry.

The value in the second column identifies your map configuration entry and will be re-used to reference/use this map/viewport in a _Trial_.

#### Places configuration

A complete example of a valid places configuration file can be found [here](/examples/places-config-4.txt).

The `de.akmiraketen.webexp.trial_map_id` is of no value and thus should be left empty. The background to this is: One configured place can by now theoretically be used in many trial configurations. Which place is loaded for which map is expressed in each line of a _Trial configuration_ where a  "#MapID" is corresponding with five "Place IDs".

Note: The (per trial) configured places must be within the viewport of the map with the given #MapID otherwise an error is thrown to your browsers.

To load the places configuration file into the database, complete the same actions in the webclient:<br/>

* Click in the *Toolbar*: "By Type" > "Topic Type" > "Search" to reveal all so called "Topic Types"
* Find and click in the *Page Panel* (showing your _Search Results_): "Place Config"
* Select "Map File Config" on the left side of your screen, execute the *Import CSV* upon it, choose your "Map File Configuration"-CSV from your hard-disk and press "Upload".

After a successfull command all _places_ appear in the "Page Panel" (right side) listed with their IDs. Clicking on one let's you inspect the entry.

The value in the second column identifies your place and will be re-used to reference/use this name/location in a _Trial_.

#### Trial configuration

A complete example of a valid trial configuration file can be found [here](/examples/trial-config1.txt).

A trial configuration is currently either of `de.akmiraketen.webexp.trial_condition` (_Condition_): "Pinning" or "No Pinning"

Additionally it connects your map material (via MapID) with places (via PlaceID) and allows you to deliberately make use of the five places involved in a trial (relating to pinning and distance or direction estimations).

Trial configurations are configured per user and this web-application is realized as a multi-user application allowing many users/participant do your experiment at the same time.

To be able to configure the `Welcome`, `Introduction`, `Practice` and (potentially) also other type of screens the first five letters in the first column identify a special type of screen. The number behind those five letters is an _ordinal number_.

* `intro` - Long intro (depending on the _condition_)
* `pract` - Full Trial (Pinning, Math, Estimating, Rating) with special visual Feedback about the estimations not included in the reporting
* `start` - Short intro (depending on the _condition_)
* `trial` - Counting Trial (Pinning, Esimating) without feedback to the estimations included in the reporting
* `break` - Pause screen


### Reporting

Open the following resource on your server (e.g. localhost:8080) with your web browser and Save the result as '.csv'.

* `/web-exp/report/generate`

All data generated will be sorted per-user and is written out in TAB-separated values and UTF-8 encoding.


