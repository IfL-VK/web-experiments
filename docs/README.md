
## Get started using `Web Experiments` ###

If you carefully follow and check the docs and infos behind the following items you should be running your own webserver with the web experiments software stack in less then 5 minutes (at least if you already have a current version of Java installed).

This software aims to support web or frontend developers and it is not a product designed to support users who have no programming or scripting experience. Once setup, the configuration and customization however can be considered user friendly as its complete configuration is supported through a graphical user interface.

### 1. Download the three files ###

- Download [DeepaMehta 4](http://download.deepamehta.de/deepamehta-4.8.zip) (requires Java)
- Download its [CSV]() and our [Web Experiments]() plugin

### 2. Installing & Launching ###

- Unzip the `deepamehta-4` archive to a folder on your hard disk
- Copy the two plugins downloaded earliy (CSV, Web Experiments) into the `bundle-deploy` folder (within the folder you just created)
- Double click the DeepaMehta start script for your OS and confirm any extra popup dialog:<br/>
  `deepamehta-windows.bat`<br/>
  `deepamehta-macosx.command`<br/>
  `deepamehta-linux.sh`<br/>
- Keep the terminal window open to use DeepaMehta 4
- Your Webbrowser will automatically open the [DeepaMehta 4 Webclient](http:/localhost:8080/de.deepamehta.webclient)

You are now ready to setup for running your first web experiment.

### 3. Configuration of a Demo Experiment ###

To configure a new experiment you must find the login button in Webclient (link above) and authenticate with the Username `admin` and an empty password. Press `OK` to log in.

#### 3.1 Loading up the pool of possible _Screen Configurations_ ####

`Screen Configurations` are the central items of any Web Experiment. They basically represent a simple HTML file realizing for example, a *task*, a custom *introduction* or *training task*, *pause* or *start* screen. Which screen relates to which template is stored in the so called screen configuration file with one screen per line. For setting up our demo experiment we will load an exemplary screen configuration.

If you are already familiar with the DeepaMehta 4 Webclient, to load a new experiment configuration you first need to find the _Topic Type_ named `Screen Configuration` (URI: `de.akmiraketen.screen_configuration`
), select it, fire its `Import CSV` command and upload for example the `screen_configuration_example.txt` from [here](https://raw.githubusercontent.com/mukil/web-experiments/master/help/screen_configuration_example.txt).

If you have never used the DeepaMehta 4 Webclient before do the following steps and you will be fine.

There are many different ways in DeepaMehta 4 to find and select a specific `Topic Type`, the easiest being 1) via a "By Type" -> "Topic Type" Search (in the upper Toolbar).

The other way to select a specific item 2) is via revealing the `Workspace` the type is associated with:

- In the Toolbar (at the top of the Webclient), far left `Workspace` select the `Web Experiments` workspace and press the `i` button next to it.
- Scroll down in the right side of your screen ("Page Panel") until you see the `Screen Configuration` type in the _Topic Type_ section of the listing

#### 3.2 Assigning a sequence of specific _Screen Configurations_ to a single #### _Participant_

After the successfull import of all your _Screen Configurations_, you need to reveal one in the map (Webclient) through selecting one and associate it (e.g. via Right Click > Associate) with the _Username_ of your choice via an _Active Configuration_ association.

The easiest way to reveal all _Usernames_ in a map is to fire a "By Type" > "Username" Search.

#### 3.3 Assigning different screen configuration files per _Participant_ ####

See [this pragraph for your information](https://github.com/mukil/web-experiments/tree/master/help#load-a-configuration-file-containing-many-screens-on-a-per-user-base).

### 4 Start a session on the demo experiment ###

As a final step, according to your screen configuration file, you must copy the contents of the `templates` folder (part of the [web-experiments github repository]()) and copy them into your DeepaMehta 4 file repository, specifically in the `web-experiments/templates` folder of it. To adjust the location of your DeepaMehta 4 file repository you can enter any path on your hard disk as the value for the `dm4.filerepo.path` value (in the `conf/config.properties` file).

Open [http://localhost:8080/experiment/](http://localhost:8080/experiment/) and enter the Username you just configured `VP <NR>`, e.g. `VP 10` and click `OK`.

### 5 Generate a report of actions across all screens and all sessions ###

The syetem simply generates a report which aggregates all actions of all users reported in the lifetime of your datbase/installation.

Please note: To do so you must be logged in.

Open [http://localhost:8080/experiment/report/generate](http://localhost:8080/experiment/report/generate) in your browser and do "Save as" to write the contents of this page into some .CSV file
