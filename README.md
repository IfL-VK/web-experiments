
## Web Experiments

A DeepaMehta 4 Plugin to bundle server and client-side components. The client side JS components themselves are structured as require.js resp. angular modules.

Software Components of the Web-Experiments Application:

 * HTTP Session Component (User generation)
 * Configuration options for each _Trial_ and each _Experiemnt_
 * HTML Mapping Component based on Leaflet
 * Filler Component
 * Report Component (CSV)
 * If possible: Configuration Dialogs (instead of LOADING CSVs)
 * If possible: Authoring/Annotating of Maps directly in the system (not in CSVs)

### Development

 * Set up your development environment through building DeepaMehta 4 from source (see [Details here](https://trac.deepamehta.de))
 * Copy the `web-experiments` folder (in this repo located at src/filerepo/web-experiments) into your dm4 filerepo path
 * Start DeepaMehta with both, the CSV and the Web-Exp plugin and then open http://localhost:8080/web-exp
 * Copy some PNG files for icon personalization into the web-experiments/symbols folder (in your filerepo)
 * Start editing the .js files under web-experiments/scripts (directly from within your filerepo)
 * Client side architecture is the [multipage webapp](https://github.com/requirejs/example-multipage) example of @requirejs
 
### 1st Milestone

* Realize a functional and clean application model
* Turn our HTML Mapping Trial into a re-usable component
* ..

Configurable options for a _Experiment_ (Session):

When the study variies *within* a propositi the following setup is _global_:

 * Welcome Text
 * Pause Text
 * Training Mode: on|off
 * Optional: Nr. of subsequent Trials under condition A
 * Optional: Nr. of subsequent Trials under condition B
 * Optional: Nr. of subsequent Trials under condition C
 * List of Trials (?)
 
When the study varies *between* propositi the configuration must be set up per _session_.

Configurable options for each _Trial_:
 
 * Condition (A, B or C)
 * Timer (Memorization Time, Animation Time)
 * Map in use (Maptype, Filename, Tile Map Server, Center, Scale, Places in Map, Marker-Symbol)
 * Place to be pinned (A, B, C or none)
 * Places involved in distance estimation (From, To)
 
Variables to be *reported* for each _Experiment_ (Session):
 * Session-ID
 * Current Trial
 * Start time of session
 * End time of session
 * Anzahl der Übungen
 * Trials
 ** Condition Z
 ** Optional: Exakte Koordinate des Pinnings
 ** Optional: Dauer bis Pinning
 ** Optional: n-Koordinaten (alle die nicht in "active control" landeten, bis Active Control) -> (Coordinate Clicked, Seconds)
 ** Optional: Nr. of Count of clicks out of "active control"
 ** Real distance to place X (in meters)
 ** Estimated distance to place X (in meters)
 ** Real direction to place X (in degrees)
 ** Estimated direction to place X (in degrees)
 ** ...
 
#### Author

Malte Reißig, 2014-2015
Leibniz-Institut für L&auml;nderkunde e. V.

