package de.akmiraketen.plugins.experiments;

import de.akmiraketen.plugins.experiments.model.ParticipantViewModel;
import de.akmiraketen.plugins.experiments.model.ScreenConfigViewModel;
import de.deepamehta.core.Association;
import de.deepamehta.core.DeepaMehtaObject;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Credentials;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.files.DirectoryListing;
import de.deepamehta.plugins.files.DirectoryListing.FileItem;
import de.deepamehta.plugins.files.ItemKind;
import de.deepamehta.plugins.files.service.FilesService;
import de.deepamehta.plugins.workspaces.service.WorkspacesService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


/**
 * A simple and flexible web-application to conduct experiments on the
 * perception and the processing of web-cartographies.
 *
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>), 2016
 * @website https://github.com/mukil/web-experiments
 * @version 0.4-SNAPSHOT
 */
@Path("/experiment")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebExperimentsPlugin extends PluginActivator {

    private Logger log = Logger.getLogger(getClass().getName());

    private final String DEEPAMEHTA_VERSION = "DeepaMehta 4.4.3";
    private final String WEB_EXPERIMENTS_VERSION = "0.4-SNAPSHOT";
    private final String CHARSET = "UTF-8";

    // --- DeepaMehta 4 URIs
    
    private static final String USER_ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
    private static final String USERNAME_TYPE_URI = "dm4.accesscontrol.username";
    private static final String USER_PASSWORD_TYPE_URI = "dm4.accesscontrol.password";
    
    private static final String ROLE_PARENT = "dm4.core.child";
    private static final String ROLE_CHILD = "dm4.core.parent";
    private static final String ROLE_DEFAULT = "dm4.core.default";
    
    private static final String FILE_TYPE = "dm4.files.file";
    private static final String FILE_PATH_TYPE = "dm4.files.path";
    
    // --- Web Experiment URIs

    private static final String SCREEN_CONFIG_TYPE = "de.akmiraketen.screen_configuration";
    private static final String SCREEN_CONDITION_TYPE = "de.akmiraketen.screen_condition";
    private static final String SCREEN_TEMPLATE_NAME = "de.akmiraketen.screen_template";
    private static final String SCREEN_TIMEOUT_VALUE = "de.akmiraketen.screen_timeout";
    private static final String SCREEN_OPTIONS_BLOB = "de.akmiraketen.screen_options";

    // -- Per User Config

    private static final String SCREEN_SEEN_EDGE = "de.akmiraketen.screen_seen";
    private static final String ACTIVE_CONFIGURATION_EDGE = "de.akmiraketen.active_configuration";

    // -- Screen Report URIs

    private static final String SCREEN_REPORT_TYPE = "de.akmiraketen.screen_report";
    private static final String SCREEN_ACTION_TYPE = "de.akmiraketen.screen_action";
    private static final String SCREEN_ACTION_NAME_TYPE = "de.akmiraketen.action_name";
    private static final String SCREEN_ACTION_VALUE_TYPE = "de.akmiraketen.action_value";

    // -- Definitions

    private static final int OK_NR = 1;
    private static final int FAIL_NR = -1;

    // -- Settings

    private static final int NR_OF_USERS = 300;
    private static final String TEMPLATE_FOLDER = "web-experiments/templates";
    private static final String SYMBOL_FOLDER = "web-experiments/symbols";

    // --- Consumed Plugin Services
    
    @Inject private AccessControlService acService = null;
    @Inject private WorkspacesService workspaceService = null;
    @Inject private FilesService fileService = null;
    
    @Override
    public void init() {
        log.info("### Thank you for deploying Web Experiments " + WEB_EXPERIMENTS_VERSION);
    }
    
    @Override
    public void postInstall() {
        log.info(" ### Generating "+NR_OF_USERS+" user account for your web-experiment " + WEB_EXPERIMENTS_VERSION);
        generateNumberOfUsers();
        log.info(" ### Creating the \"/web-experiments/templates\" folder in your filerepo for screen templates " +
                "web-experiments " + WEB_EXPERIMENTS_VERSION);
        String parentFolderName = "web-experiments";
        createFolderWithName(parentFolderName, null);
        createFolderWithName("symbols", parentFolderName);
        createFolderWithName("templates", parentFolderName);
    }
    
    // ---------------------------------------------------------------------------------------------------- Routes
    
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getWelcomeScreen() {
        return getStaticResource("web/index.html");
    }
    
    @GET
    @Path("/finish")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getFinishView() {
        return getStaticResource("web/index.html");
    }

    /**
     * Redirects the requesting clien to the next configured resource for her (if the request is authenticated).
     **/
    @GET
    @Path("/screen/next")
    public Response getNextScreen() throws URISyntaxException {
        // 1) get current username by http session (or throw a 401)
        Topic user = getRequestingUsername();
        // 2) get next trial config topic related to the user topic, discarding those related
        //    to the user via a "trial_seen_edge".
        long screenTopicId = getNextUnseenScreenId(user);
        if (screenTopicId == FAIL_NR) {
            // 2.1) If the particpant has seen all trials configured for her we redirect to our final screen.
            log.info("Experiment finished, no configured trial left for requesting user");
            return Response.seeOther(new URI("/experiment/finish")).build();
        } else if (screenTopicId != FAIL_NR) {
            // 2.2) If there is yet an "unseen" trial configured for the user, we load and redirect
            //      the request according to the configured template "type".
            Topic configuration = dms.getTopic(screenTopicId);
            ScreenConfigViewModel screenConfig = new ScreenConfigViewModel(configuration);
            log.info("Should REDIRECT to screen template \"" + screenConfig.getScreenTemplateName() + "\" configured at "
                + "\"/experiment/screen/" + configuration.getId() + "\"");
            URI location = new URI("/experiment/screen/" + configuration.getId());
            return Response.seeOther(location).build();
        } else {
            return Response.ok(FAIL_NR).build(); // experiment finished > no unseen trial left
        }
    }

    /**
     * Fetches the Template file for the given Screen Configuration Topic ID.
     * @return  InputStream     HTML Template File representing the "Screen" (=Task, Trial).
     */
    @GET
    @Path("/screen/{screenTopicId}")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getScreen(@PathParam("screenTopicId") long id) throws URISyntaxException {
        InputStream fileInput = null;
        try {
            // 1) get current username by http session (or throw a 401)
            Topic user = getRequestingUsername();
            // 2) fetch screen
            Topic screenTopic = dms.getTopic(id);
            // 3) check if unseen by user
            if (hasSeenScreen(user, screenTopic.getId())) {
                throw new WebApplicationException(Response.seeOther(new URI("/experiment/screen/next")).build());
            }
            ScreenConfigViewModel screenConfig = new ScreenConfigViewModel(screenTopic);
            String templateFileName = screenConfig.getScreenTemplateName();
            File screenTemplate = fileService.getFile(TEMPLATE_FOLDER + "/" + templateFileName);
            fileInput = new FileInputStream(screenTemplate);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("A file for the Screen Configuration Topic with ID was NOT FOUND, please use" +
                    " the \"/web-experiments/templates\" folder in your configured DM 4 File Repository.", e);
        }
        return fileInput;
    }



    // ----------------------------------------------------------------------------------------------- API Resources
    
    /** 
     * 
     * @return  List of FileItems in JSON
     */
    @GET
    @Path("/symbol/all")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllSymbolFileTopics() {
        DirectoryListing items = fileService.getDirectoryListing(SYMBOL_FOLDER);
        // ArrayList<Topic> symbols = new ArrayList<Topic>(); 
        ArrayList<JSONObject> symbolFiles = new ArrayList<JSONObject>();
        // 1) Gather svg-icon files from our symbols directory
        Iterator<FileItem> files = items.getFileItems().iterator();
        while (files.hasNext()) {
            FileItem fileItem = files.next();
            if (fileItem.getMediaType() == null && fileItem.getItemKind() != ItemKind.FILE) files.remove();
        }
        // 2) Create file topics (representing the files in our DB)
        Iterator<FileItem> icons = items.getFileItems().iterator();
        while (icons.hasNext()) {
            FileItem file = icons.next();
            Topic fileTopic = fileService.createFileTopic(file.getPath()); // fetches topic if existing
            JSONObject responseObject = new JSONObject();
            try {
                responseObject.put("path", file.getPath()).put("topic_id", fileTopic.getId());
            } catch (JSONException ex) {
                log.severe("Could not build up icon response list");
            }
            symbolFiles.add(responseObject);
        }
        return symbolFiles.toString();
    }

    /**
     * Fetches the web-experiments representation of a user account/participant.
     * @return ParticipantViewModel JSON String
     */
    @GET
    @Path("/participant")
    @Produces(MediaType.APPLICATION_JSON)
    public ParticipantViewModel getParticipantViewModel() {
        Topic username = getRequestingUsername();
        return new ParticipantViewModel(username);
    }

    /** 
     * Loads a Screen Configuration Topic.
     * @param id    topicId
     * @return ScreenConfigViewModel    String JSON
     */
    @GET
    @Path("/screen/{screenTopicId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ScreenConfigViewModel getScreenConfiguration(@PathParam("screenTopicId") long id) {
        try {
            Topic screenTopic = dms.getTopic(id);
            return new ScreenConfigViewModel(screenTopic);
        } catch(Exception e) {
            throw new RuntimeException("A Screen Configuration Topic with the given id was NOT FOUND", e);
        }
    }

    @GET
    @Path("/screen/{screenConfigId}/seen")
    @Transactional
    public Response setScreenAsSeen(@PathParam("screenConfigId") long screenId) {
        // 1) get current username by http session (or throw a 401)
        Topic username = getRequestingUsername();
        // 2) Check if user has seen the screen already
        if (!hasSeenScreen(username, screenId)) {
            dms.createAssociation(new AssociationModel(SCREEN_SEEN_EDGE,
                new TopicRoleModel(username.getId(), "dm4.core.default"),
                new TopicRoleModel(screenId, "dm4.core.default")));
        } else {
            log.info("### Screen Seen Edge already exists, responding with next unseen screen id - OK!");
            long screenConfigurationId = getNextUnseenScreenId(username);
            return Response.ok(screenConfigurationId).build();
        }
        log.info("### Set screen " + screenId + " as SEEN by user=" + username.getSimpleValue());
        // 3) Create Report - To avoid deadlocks we could initSceenReport always when screen is marked as seen
        // But we probably would need to remove the @Transactiononal annotation from initScreenReport()
        return Response.ok(OK_NR).build();
    }

    /**
     * Initiates a screen report for the authenticated user and the given screen configuration (topicId).
     **/
    @GET
    @Path("/report/start/{screenConfigId}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response initScreenReport(@PathParam("screenConfigId") long screenId) {
        Topic username = getRequestingUsername();
        Topic screenConfigTopic = dms.getTopic(screenId);
        Topic existingReport = getScreenReportTopic(username, screenConfigTopic);
        if (existingReport == null) {
            //
            ChildTopicsModel reportModel = new ChildTopicsModel().putRef(SCREEN_CONFIG_TYPE, screenId);
            Topic screenReport = dms.createTopic(new TopicModel(SCREEN_REPORT_TYPE, reportModel));
            dms.createAssociation(new AssociationModel("dm4.core.association",
                    new TopicRoleModel(username.getId(), "dm4.core.default"),
                    new TopicRoleModel(screenReport.getId(), "dm4.core.default")));
            log.info("Initialized Screen Report Topic for " + username.getSimpleValue() + " on " + screenConfigTopic
                    .getSimpleValue());
        } else {
            log.warning("### Screen Report already exists, do not attempt to create another! - SKIPPING");
            return Response.ok(FAIL_NR).build();
        }
        return Response.ok(OK_NR).build();
    }

    /**
     * Adds an "action" topic to the screen report for the authenticated user and the given screen config topic id.
     * Note: This write method is NOT SAFE for parallel requests (many WRITE operations may need access to the very
     * same Screen Configuration Topic).
     **/
    @POST
    @Path("/report/action/{screenConfigId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addActionReport(@PathParam("screenConfigId") long screenConfigId, String actionObject)
            throws JSONException {
        Topic username = getRequestingUsername();
        Topic screenConfigTopic = dms.getTopic(screenConfigId);
        Topic existingReport = getScreenReportTopic(username, screenConfigTopic);
        if (existingReport == null) {
            //
            log.warning("### Screen Report does NOT exist, please do initialize one before trying to report an action" +
                    " - ACTION REPORT FAILED");
        } else {
            JSONObject actionReport = new JSONObject(actionObject.toString());
            if (!actionReport.has(SCREEN_ACTION_NAME_TYPE)) {
                throw new IllegalArgumentException("addActionReport misses a \"" +
                        "" + SCREEN_ACTION_NAME_TYPE + "\" property / key (JSON POST Body)");
            }
            String actionNameUri =  actionReport.getString(SCREEN_ACTION_NAME_TYPE);
            String actionValue = actionReport.getString("value");
            log.info("POSTed Action Name URI: " + actionNameUri + ", value=" + actionValue);
            ChildTopicsModel actionReportChilds = new ChildTopicsModel()
                    .putRef(SCREEN_ACTION_NAME_TYPE, actionNameUri)
                    .put(SCREEN_ACTION_VALUE_TYPE, actionValue);
            ChildTopicsModel actionReportTopic = new ChildTopicsModel()
                    .add(SCREEN_ACTION_TYPE, new TopicModel(SCREEN_ACTION_TYPE, actionReportChilds));
            TopicModel actionReportModel = new TopicModel(SCREEN_REPORT_TYPE, actionReportTopic);
            existingReport.update(actionReportModel);
            return Response.ok(OK_NR).build();
        }
        return Response.ok(FAIL_NR).build();
    }

    /**
     * @param username
     * @param screenConfig
     * @return screenReport     Returns the given Screen Report as a RelatedTopic if existent, otherwise 'null'.
     */
    private RelatedTopic getScreenReportTopic(Topic username, Topic screenConfig) {
        ResultList<RelatedTopic> reports = username.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", SCREEN_REPORT_TYPE, 0);
        Iterator<RelatedTopic> iterator = reports.iterator();
        while (iterator.hasNext()) {
            RelatedTopic screenReportTopic = iterator.next();
            screenReportTopic.loadChildTopics();
            Topic screenConfigChildTopic = screenReportTopic.getChildTopics().getTopic(SCREEN_CONFIG_TYPE);
            if (screenConfigChildTopic.getId() == screenConfig.getId()) {
                log.info("Fetched existing screen report with topicId=" + screenConfigChildTopic.getId()
                        + " for " + username.getSimpleValue());
                return screenReportTopic;
            }
        }
        return null;
    }

    /**
     * Utility method to construct a valid \"Screen Action\" report entry.
     * @return String   All action name topics current in the DB.
     */
    @GET
    @Path("/report/action")
    @Produces(MediaType.APPLICATION_JSON)
    public ResultList<RelatedTopic> getReportEventTypes() {
        return dms.getTopics("de.akmiraketen.action_name", 0);
    }

    /** @GET
    @Path("/screen/config/import")
    @Transactional
    public Topic doImportUserTrialConfig() {
        return doImportUserTrialConfig(acService.getUsername());
    }

    @GET
    @Path("/screen/config/import/{username}")
    @Transactional
    public Topic doImportUserTrialConfig(@PathParam("username") String name) {
        try {
            // 1) fetch related file topic
            Topic username = acService.getUsername(name);
            Topic fileTopic = username.getRelatedTopic(ACTIVE_CONFIGURATION_EDGE, "dm4.core.default",
                    "dm4.core.default", "dm4.files.file");
            log.info("Loading Trial Config File Topic: " + fileTopic.getId()
                    + " fileName=" + fileTopic.getSimpleValue() + " for \"" + username);
            File trialConfig = fileService.getFile(fileTopic.getId());
            // 2) delete and create trial config topic
            ResultList<RelatedTopic> usersTrialConfigs = getUsersScreens(username);
            Iterator<RelatedTopic> i = usersTrialConfigs.iterator();
            while (i.hasNext()) {
                Topic topic = i.next();
                if (topic.getTypeUri().equals(SCREEN_CONFIG_TYPE)) {
                    log.fine(">>> Deleting former Trial Config Topic " + topic.getUri());
                    i.remove();
                    topic.delete();
                }
            }
            // 3) read in file topic's lines
            int nr = 1;
            BufferedReader br = new BufferedReader(new FileReader(trialConfig.getAbsolutePath()));
            try {
                String line = br.readLine();
                while (line != null) {
                    if (!line.startsWith("webexp.config")) {
                        log.fine("Line: " + line);
                        createNewTrialConfig(nr, line, username);
                        nr++;
                    }
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
            return username;
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not import trial configuration fo user " + name, ex);
            return null;
        }
    } **/
    
    /** private void createNewTrialConfig(int lineNr, String config_line, Topic username) {
        // split csv-config file line
        String[] values = Pattern.compile("|", Pattern.LITERAL).split(config_line);
        String conditionUri = (values[3].trim().equals("Pinning")) ? TRIAL_CONDITION_A : ((values[3].trim().equals("No Pinning")) ? TRIAL_CONDITION_B : "");
        String trialMapId = values[2].trim();
        Topic map = dms.getTopic(TRIAL_CONFIG_MAP_ID, new SimpleValue(trialMapId));
        if (conditionUri.isEmpty()) log.severe("Trial Conditition URI could not be detected...!");
        if (map == null) log.severe("Map ID could not be detected...!");
        // build up topic model
        String configUri = "webexp.config." + username.getSimpleValue() + "_" + lineNr + "_" + values[0].trim();
        TopicModel trialConfig = new TopicModel(configUri, TRIAL_CONFIG_TYPE, new ChildTopicsModel()
                .put(TRIAL_CONFIG_NAME, values[1].trim())
                .putRef(TRIAL_CONFIG_MAP_ID, map.getId())
                .putRef(TRIAL_CONDITION_TYPE, conditionUri)
                .put(TRIAL_CONFIG_PLACE_TO_PIN, values[4].trim())
                .put(TRIAL_CONFIG_PLACE_FROM1, values[5].trim())
                .put(TRIAL_CONFIG_PLACE_TO1, values[6].trim())
                .put(TRIAL_CONFIG_PLACE_FROM2, values[7].trim())
                .put(TRIAL_CONFIG_PLACE_TO2, values[8].trim())
                .put(TRIAL_CONFIG_PLACE_FROM3, values[9].trim())
                .put(TRIAL_CONFIG_PLACE_TO3, values[10].trim())
                .put(TRIAL_CONFIG_PLACE_FROM4, values[11].trim())
                .put(TRIAL_CONFIG_PLACE_TO4, values[12].trim())
                .put(TRIAL_CONFIG_PLACE_FROM5, values[13].trim())
                .put(TRIAL_CONFIG_PLACE_TO5, values[14].trim())
                .put(TRIAL_CONFIG_MEMO_SEC, values[15].trim()));
        // create topic
        Topic trialConfigTopic = dms.createTopic(trialConfig);
        log.info(">>> Created new Trial Configuration: " + configUri + " (" + trialConfigTopic.getId() + ") for \"" + trialMapId + "\" (Topic: "+map.getId()+")");
        createTrialConfigUserAssignment(trialConfigTopic, username);
    } **/

    /**
     * An implementation to generate a CSV report containing all trial report (usage data) for all participants
     * which have completed some trials data as a response to a HTTP GET request.
     * @return String   The full contents of a CSV usage report.
     */
    @GET
    @Path("/report/generate")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String doGenerateCompleteReport() {
        // 1) check for logged in user (or throw a 401)
        getRequestingUsername();
        // 2) gather all reports from the db
        StringBuilder report = new StringBuilder();
        ResultList<RelatedTopic> participants = dms.getTopics("dm4.accesscontrol.username", 0);
        log.info("Gathering reporting for overall " + participants.getSize() + " user accounts");
        report.append("VP ID\tScreen Template\tScreen Condition\tScreen Timeout\tScreen Options\tAction Type\tAction Value");
        report.append("\n");
        for (RelatedTopic username : participants.getItems()) {
            String usernameValue = username.getSimpleValue().toString();
            ResultList<RelatedTopic> screenReports = username.getRelatedTopics("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", SCREEN_REPORT_TYPE, 0);
            if (screenReports.getSize()> 0) {
                log.info("Fetched " + screenReports.getSize() + " Screen Reports for \"" + usernameValue + "\"");
                for (RelatedTopic screenReport : screenReports) {
                    // load full report
                    screenReport.loadChildTopics();
                    // load corresponding screen configuration topic
                    Topic screenConfigurationTopic = null;
                    String templateName = "", conditionValue = "", options = "", timeout = "";
                    if (screenReport.getChildTopics().has(SCREEN_CONFIG_TYPE)) {
                        screenConfigurationTopic = screenReport.getChildTopics().getTopic(SCREEN_CONFIG_TYPE);
                        screenConfigurationTopic.loadChildTopics();
                        templateName = screenConfigurationTopic.getChildTopics().getString(SCREEN_TEMPLATE_NAME);
                        conditionValue = screenConfigurationTopic.getChildTopics().getString(SCREEN_CONDITION_TYPE);
                        options = screenConfigurationTopic.getChildTopics().getString(SCREEN_OPTIONS_BLOB);
                        timeout = screenConfigurationTopic.getChildTopics().getString(SCREEN_TIMEOUT_VALUE);
                    }
                    // load all actions reported for that screen
                    List<Topic> reportedActions = null;
                    if (screenReport.getChildTopics().has(SCREEN_ACTION_TYPE)) {
                        reportedActions = screenReport.getChildTopics().getTopics(SCREEN_ACTION_TYPE);
                    }
                    if (screenConfigurationTopic != null && reportedActions != null && reportedActions.size() > 0) {
                        // aggregate reported line
                        // fullActionReport(usernameValue, templateName, conditionValue, options, timeout, reportedActions, report);
                        customSelectionReport(usernameValue, templateName, conditionValue, options, timeout, reportedActions, report);
                    } else {
                        log.info("NO action REPORTED for \"" + usernameValue + "\" on screen "
                            + screenReport.getSimpleValue().toString());
                    }
                }
            }
        }
        return report.toString();
    }



    // ------------------------------------------------------------------------------------------------- Helper Methods
    
    /**
     * Iterates all Screen Configuration Topics dirctly related to a specific username.
     * @return ID   long Topic id of the next sceen configuration to load.
     *
     * Note: Screen Configuration Topics are ordered by an ordinal number (the last part of the topics URI).
     * Note: The screen configuration topic must be related to the username topic directly via an association of
     * type "de.akmiraketen.webexp.active_configuration".
     * Note: A screen is regarded as "Unseen" if there is no relation (typed="de.akmiraketen.webexp.screen_seen_edge")
     * between the username and the screen configuration topic.
     */
    private long getNextUnseenScreenId(Topic username) {
        ResultList<RelatedTopic> screenConfigs = getActiveScreenConfigs(username);
        log.info("Found " + screenConfigs.getSize() + " active configurations for " + username.getSimpleValue());
        ArrayList<RelatedTopic> orderedScreenConfigs = getScreenTopicsSortedByURI(screenConfigs);
        Iterator<RelatedTopic> iterator = orderedScreenConfigs.iterator();
        while (iterator.hasNext()) {
            RelatedTopic screenConfig = iterator.next();
            if (!hasSeenScreen(username, screenConfig.getId())) {
                return screenConfig.getId();
            }
        }
        return FAIL_NR; // experiment finished > no unseen trial left
    }

    /**
     * Checks if a screen config topic with the given id is already related
     * (type="de.akmiraketen.webexp.screen_seen_edge") to the given username topic.
     */
    private boolean hasSeenScreen(Topic username, long screenConfigId) {
        Association trialSeen = username.getAssociation(SCREEN_SEEN_EDGE, ROLE_DEFAULT, ROLE_DEFAULT, screenConfigId);
        return trialSeen != null;
    }

   /**
    * Loads all active screen configuration for the given username topic.
    **/
    private ResultList<RelatedTopic> getActiveScreenConfigs(Topic user) {
        return user.getRelatedTopics(ACTIVE_CONFIGURATION_EDGE, ROLE_DEFAULT, ROLE_DEFAULT, SCREEN_CONFIG_TYPE, 0);
    }

    private void fullActionReport(String usernameValue, String templateName, String conditionValue, String options,
            String timeout, List<Topic> reportedActions, StringBuilder report) {
        for (Topic action : reportedActions) {
            String actionType = "", actionValue = "";
            if (action.getChildTopics().has(SCREEN_ACTION_NAME_TYPE)) {
                actionType = action.getChildTopics().getString(SCREEN_ACTION_NAME_TYPE);
            }
            if (action.getChildTopics().has(SCREEN_ACTION_VALUE_TYPE)) {
                actionValue = action.getChildTopics().getString(SCREEN_ACTION_VALUE_TYPE);
            }
            report.append(usernameValue).append("\t").append(templateName).append("\t").append(conditionValue).
                append("\t").append(timeout).append("\t").append(options).append("\t").append(actionType)
                .append("\t").append(actionValue).append("\n");
        }
    }

    private void customSelectionReport(String usernameValue, String templateName, String conditionValue, String options,
            String timeout, List<Topic> reportedActions, StringBuilder report) {
        // Sum of selections by "name" in actionValue Object
        HashMap sum = new HashMap();
        for (Topic action : reportedActions) {
            String actionType = "", actionValue = "";
            if (action.getChildTopics().has(SCREEN_ACTION_NAME_TYPE)) {
                actionType = action.getChildTopics().getString(SCREEN_ACTION_NAME_TYPE);
                if (actionType.equals("Select")) {
                    if (action.getChildTopics().has(SCREEN_ACTION_VALUE_TYPE)) {
                        try {
                            actionValue = action.getChildTopics().getString(SCREEN_ACTION_VALUE_TYPE);
                            JSONObject valueObject = new JSONObject(actionValue);
                            String name = valueObject.getString("name") + " (" + valueObject.getString("type") + ")";
                            int count = 1;
                            if (sum.containsKey(name)) {
                                count = (Integer) sum.get(name) + 1;
                            }
                            sum.put(name, count);
                        } catch (JSONException ex) {
                            log.log(Level.SEVERE, "Could not transform \"Action Value\" string to JSONObject", ex);
                        }
                    }
                }
            }
        }
        //
        Set<String> nameKeys = sum.keySet();
        Iterator<String> namesIterator = nameKeys.iterator();
        while(namesIterator.hasNext()) {
            String name = namesIterator.next();
            report.append(usernameValue).append("\t").append(templateName).append("\t").append(conditionValue)
                .append("\t").append(timeout).append("\t").append(options).append("\t").append(name)
                .append("\t").append(sum.get(name)).append("\n");
        }
    }
    
    private ArrayList<RelatedTopic> getScreenTopicsSortedByURI(ResultList<RelatedTopic> all) {
        // build up sortable collection of all result-items
        ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
        for (RelatedTopic obj : all) {
            in_memory.add(obj);
        }
        // sort all result-items
        Collections.sort(in_memory, new Comparator<RelatedTopic>() {
            public int compare(RelatedTopic t1, RelatedTopic t2) {
                try { // ### webexp.config.intro + webexp.config.pract
                    if (t1.getUri().contains(".") && t2.getUri().contains(".")) {
                        String one = t1.getUri().substring(t1.getUri().lastIndexOf(".") + 1);
                        String two = t2.getUri().substring(t2.getUri().lastIndexOf(".") + 1);
                        if ( Long.parseLong(one) < Long.parseLong(two)) return -1;
                        if ( Long.parseLong(one) > Long.parseLong(two)) return 1;
                    } else {
                        log.warning("We could not sort the active screen configurations for this users due to a "
                            + "misconfigured URI in 1.\"" + t1.getUri() + "\" OR 2. \"" + t2.getUri());
                    }
                } catch (Exception nfe) {
                    log.warning("Error while accessing URI of Topic 1: " + t1.getUri() + " Topic2: "
                            + t2.getUri() + " nfe: " + nfe.getMessage());
                    return 0;
                }
                return 0;
            }
        });
        return in_memory;
    }

    private Topic getRequestingUsername() {
        String username = acService.getUsername();
        if (username == null || username.isEmpty()) {
            throw new WebApplicationException(204);
        }
        return dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username));
    }
    
    private void generateNumberOfUsers() {
        // for 1000 do acService.createUser()
        log.info("### Setting up new users for Web Experiments");
        DeepaMehtaTransaction tx = dms.beginTx();
        try {
            for (int i=1; i<=NR_OF_USERS; i++) {
                String username = "VP "+ i;
                if (isUsernameAvailable(username)) {
                    Credentials cred = new Credentials(username, "");
                    ChildTopicsModel userAccount = new ChildTopicsModel()
                        .put(USERNAME_TYPE_URI, cred.username)
                        .put(USER_PASSWORD_TYPE_URI, cred.password);
                    TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
                    Topic vpAccount = dms.createTopic(userModel);
                    Topic usernameTopic = vpAccount.loadChildTopics(USERNAME_TYPE_URI)
                            .getChildTopics().getTopic(USERNAME_TYPE_URI);
                    if (usernameTopic != null && workspaceService.getDefaultWorkspace() != null) {
                        workspaceService.assignToWorkspace(usernameTopic, workspaceService.getDefaultWorkspace().getId());
                        setDefaultAdminACLEntries(vpAccount);
                        log.info("Created user \"" + username + "\" for web-experiments.");
                    } else {
                        log.info("Could not create new user, topic username: " + username+ ", workspace:" +
                                workspaceService.getDefaultWorkspace());
                    }
                } else {
                    log.info("DEBUG: Username is already taken ..");
                }
            }
            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    private DeepaMehtaObject setDefaultAdminACLEntries(DeepaMehtaObject item) {
        // Let's repair broken/missing ACL-Entries
        ACLEntry writeEntry = new ACLEntry(Operation.WRITE, UserRole.CREATOR, UserRole.OWNER);
        acService.setACL(item, new AccessControlList(writeEntry));
        acService.setCreator(item, "admin");
        acService.setOwner(item, "admin");
        return item;
    }
    
    private void createFolderWithName(String folderName, String parentFolderName) {
        DeepaMehtaTransaction tx = dms.beginTx();
        String parent = "/";
        if (parentFolderName != null) parent = parentFolderName;
        try {
            File item = fileService.getFile(parent + "/" + folderName); // throws RuntimeException if no result
            if (!item.isDirectory()) { // folder does not exist
                fileService.createFolder(folderName, parent);
            } else  {
                log.info("OK - Folder already exists");
            }
            tx.success();
        } catch (RuntimeException fe) { // file or folder does not exist
            log.info("Cause: " + fe.getCause().toString());
            if (fe.getCause().toString().contains("does not exist")) {
                try {
                    log.info("Folder does not exist!..");
                    fileService.createFolder(folderName, parent); // might throw permission denied error
                    log.info("CREATED new Folder " + folderName);
                    tx.success();
                } catch (RuntimeException re) {
                    log.severe("Most probably DeepaMehta cant write to your filerepo, "
                            + "please check the filrepo configuration in the pom.xml");
                    throw new RuntimeException(re);
                }
            } else {
                // throw new RuntimeException(fe);
                log.info("OK - Folder already exists");
            }
        } finally {
            tx.finish();
        }
    }
    
    private boolean isUsernameAvailable(String username) {
        Topic userName = dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username));
        return (userName == null);
    }
    
    private Association createTrialConfigUserAssignment(Topic trialConfig, Topic username) {
        return dms.createAssociation(new AssociationModel(ACTIVE_CONFIGURATION_EDGE,
            new TopicRoleModel(username.getId(), ROLE_DEFAULT),
            new TopicRoleModel(trialConfig.getId(), ROLE_DEFAULT)));
    }

}
