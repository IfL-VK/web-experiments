package de.akmiraketen.plugins.experiments;

import de.akmiraketen.plugins.experiments.migrations.Migration4;
import de.akmiraketen.plugins.experiments.model.ParticipantViewModel;
import de.akmiraketen.plugins.experiments.model.ScreenConfigViewModel;
import de.deepamehta.core.Association;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.files.DirectoryListing;
import de.deepamehta.files.DirectoryListing.FileItem;
import de.deepamehta.files.ItemKind;
import de.deepamehta.files.FilesService;
import de.deepamehta.workspaces.WorkspacesService;

import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
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
import java.util.regex.Pattern;

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
 * @version 0.5-SNAPSHOT
 */
@Path("/experiment")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebExperimentsPlugin extends PluginActivator {

    private Logger log = Logger.getLogger(getClass().getName());

    public static final String DEEPAMEHTA_VERSION = "DeepaMehta 4.8";
    public static final String WEB_EXPERIMENTS_VERSION = "0.5-SNAPSHOT";
    public static final String CHARSET = "UTF-8";

    // --- DeepaMehta 4 URIs
    
    private static final String USER_ACCOUNT_TYPE_URI       = "dm4.accesscontrol.user_account";
    private static final String USERNAME_TYPE_URI           = "dm4.accesscontrol.username";
    private static final String USER_PASSWORD_TYPE_URI      = "dm4.accesscontrol.password";
    
    private static final String ROLE_PARENT                 = "dm4.core.child";
    private static final String ROLE_CHILD                  = "dm4.core.parent";
    private static final String ROLE_DEFAULT                = "dm4.core.default";
    
    private static final String FILE_TYPE                   = "dm4.files.file";
    private static final String FILE_PATH_TYPE              = "dm4.files.path";

    // --- Web Experiment URIs

    public static final String WEBEXP_WORKSPACE_NAME = "Web Experiments";
    public static final String WEBEXP_WORKSPACE_URI = "de.akmiraketen.web_experiments";

    private static final String SCREEN_CONFIG_TYPE          = "de.akmiraketen.screen_configuration";
    private static final String SCREEN_CONDITION_NAME       = "de.akmiraketen.screen_condition";
    private static final String SCREEN_TEMPLATE_NAME        = "de.akmiraketen.screen_template";
    private static final String SCREEN_TIMEOUT_VALUE        = "de.akmiraketen.screen_timeout";
    private static final String SCREEN_OPTIONS_BLOB         = "de.akmiraketen.screen_options";

    // -- Per User Config

    private static final String SCREEN_SEEN_EDGE            = "de.akmiraketen.screen_seen";
    private static final String ACTIVE_CONFIGURATION_EDGE   = "de.akmiraketen.active_configuration";

    // -- Screen Report URIs

    private static final String SCREEN_REPORT_TYPE          = "de.akmiraketen.screen_report";
    private static final String SCREEN_ACTION_TYPE          = "de.akmiraketen.screen_action";
    private static final String SCREEN_ACTION_NAME_TYPE     = "de.akmiraketen.action_name";
    private static final String SCREEN_ACTION_VALUE_TYPE    = "de.akmiraketen.action_value";

    // -- Definitions

    private static final int OK_NR = 1;
    private static final int FAIL_NR = -1;
    private static final String SCREEN_CONFIG_URI_PREFIX    = "webexp.config.";

    // -- Settings

    public static final String CONFIG_SEPERATOR = "|";

    // --- Consumed Plugin Services
    
    @Inject private AccessControlService acService = null;
    @Inject private WorkspacesService workspaceService = null;
    @Inject private FilesService fileService = null;
    
    @Override
    public void init() {
        log.info("### Thank you for deploying Web Experiments " + WEB_EXPERIMENTS_VERSION);
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
            Topic configuration = dm4.getTopic(screenTopicId);
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
            Topic screenTopic = dm4.getTopic(id);
            // 3) check if unseen by user
            if (hasSeenScreen(user, screenTopic.getId())) {
                throw new WebApplicationException(Response.seeOther(new URI("/experiment/screen/next")).build());
            }
            ScreenConfigViewModel screenConfig = new ScreenConfigViewModel(screenTopic);
            String templateFileName = screenConfig.getScreenTemplateName();
            File screenTemplate = fileService.getFile(Migration4.TEMPLATE_FOLDER + "/" + templateFileName);
            fileInput = new FileInputStream(screenTemplate);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("A file for the Screen Configuration Topic with ID was NOT FOUND, please use" +
                    " the \"/web-experiments/templates\" folder in your configured DM 4 File Repository.", e);
        }
        return fileInput;
    }



    // ----------------------------------------------------------------------------------------------- API Resources

    @GET
    @Path("/workspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Topic getWorkpsace() {
        return dm4.getTopicByValue("uri", new SimpleValue(WEBEXP_WORKSPACE_URI));
    }

    /** 
     * 
     * @return  List of FileItems in JSON
     */
    @GET
    @Path("/symbol/all")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllSymbolFileTopics() {
        DirectoryListing items = fileService.getDirectoryListing(Migration4.SYMBOL_FOLDER);
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
            try {
                FileItem file = icons.next();
                Topic fileTopic = fileService.getFileTopic(file.getPath()); // creates topic if not-existing
                JSONObject responseObject = new JSONObject();
                responseObject.put("path", file.getPath()).put("topic_id", fileTopic.getId());
                symbolFiles.add(responseObject);
            } catch (JSONException ex) {
                Logger.getLogger(WebExperimentsPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            Topic screenTopic = dm4.getTopic(id);
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
            dm4.createAssociation(mf.newAssociationModel(SCREEN_SEEN_EDGE,
                mf.newTopicRoleModel(username.getId(), "dm4.core.default"),
                mf.newTopicRoleModel(screenId, "dm4.core.default")));
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
        Topic screenConfigTopic = dm4.getTopic(screenId);
        Topic existingReport = getScreenReportTopic(username, screenConfigTopic);
        if (existingReport == null) {
            //
            ChildTopicsModel reportModel = mf.newChildTopicsModel().putRef(SCREEN_CONFIG_TYPE, screenId);
            Topic screenReport = dm4.createTopic(mf.newTopicModel(SCREEN_REPORT_TYPE, reportModel));
            dm4.createAssociation(mf.newAssociationModel("dm4.core.association",
                    mf.newTopicRoleModel(username.getId(), "dm4.core.default"),
                    mf.newTopicRoleModel(screenReport.getId(), "dm4.core.default")));
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
        Topic screenConfigTopic = dm4.getTopic(screenConfigId);
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
            ChildTopicsModel actionReportChilds = mf.newChildTopicsModel()
                    .putRef(SCREEN_ACTION_NAME_TYPE, actionNameUri)
                    .put(SCREEN_ACTION_VALUE_TYPE, actionValue);
            ChildTopicsModel actionReportTopic = mf.newChildTopicsModel()
                    .add(SCREEN_ACTION_TYPE, mf.newTopicModel(SCREEN_ACTION_TYPE, actionReportChilds));
            TopicModel actionReportModel = mf.newTopicModel(SCREEN_REPORT_TYPE, actionReportTopic);
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
        List<RelatedTopic> reports = username.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", SCREEN_REPORT_TYPE);
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
    public List<Topic> getReportEventTypes() {
        return dm4.getTopicsByType("de.akmiraketen.action_name");
    }

    /** Custom Configuration Loading Mechanism, available as a Topic Command for the selected username Topic... */
    @GET
    @Path("/screen/config/import/{username}")
    @Transactional
    public Topic doImportScreenConfigurationForUsername(@PathParam("username") String name) {
        try {
            // 1) fetch related file topic
            Topic username = acService.getUsernameTopic(name);
            Topic fileTopic = null;
            try {
                fileTopic = username.getRelatedTopic(ACTIVE_CONFIGURATION_EDGE, ROLE_DEFAULT,
                        ROLE_DEFAULT, FILE_TYPE);
            } catch(Exception e) {
                log.severe("Screen Configuration could not be loaded. Reason: There can only be 1 screen " +
                        "configuration file related to a Username via an \"Active Configuration\" assocation at a time.");
                throw new WebApplicationException(500);
            }
            log.info("Importing new Screen Configuration File: " + fileTopic.getId()
                    + " fileName=" + fileTopic.getSimpleValue() + " for \"" + username + "\"");
            File screenConfigurationFileTopic = fileService.getFile(fileTopic.getId());
            // 2) delete potential former trial config topic
            List<RelatedTopic> usersTrialConfigs = getActiveScreenConfigs(username);
            Iterator<RelatedTopic> i = usersTrialConfigs.iterator();
            while (i.hasNext()) {
                Topic topic = i.next();
                if (topic.getTypeUri().equals(SCREEN_CONFIG_TYPE)) {
                    log.fine("Deleting former Screen Configuration Topic " + topic.getUri() + " for user " + name);
                    i.remove();
                    topic.delete();
                }
            }
            // 3) read in file topic's lines and create new trial configs
            int nr = 1;
            BufferedReader br = new BufferedReader(new FileReader(screenConfigurationFileTopic.getAbsolutePath()));
            try {
                String line = br.readLine();
                while (line != null) {
                    if (!line.startsWith("webexp.order_id")) {
                        log.fine("Line: " + line);
                        createNewTrialConfig(line, username);
                        nr++;
                    }
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
            return username;
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not import Screen Configuration for user " + name, ex);
            return null;
        }
    }
    
    private void createNewTrialConfig(String config_line, Topic username) {
        // 1) split csv-config file line and read in config value for this screen
        // webexp.order_id | de.akmiraketen.screen_template | de.akmiraketen.screen_condition | de.akmiraketen.screen_timeout | de.akmiraketen.screen_options
        String[] values = Pattern.compile(CONFIG_SEPERATOR, Pattern.LITERAL).split(config_line);
        String ordinalNumber = values[0].trim();
        String screenTemplateName = values[1].trim();
        String screenConditionName = values[2].trim();
        String screenTimeout = values[3].trim();
        String screenJsonOptions = "";
        if (values.length > 4) {
            screenJsonOptions = values[4].trim();
        }
        // 2) Check sanity, template Name and ordinal Number can't be empty values
        if (screenTemplateName.isEmpty()) {
            throw new RuntimeException("Screen Configuration misses Template Name!");
        }
        if (ordinalNumber.isEmpty()) {
            throw new RuntimeException("Screen Configuration misses Ordinal Number!");
        }
        // 3) Build up Screen Configuration Topic model
        String configUri = SCREEN_CONFIG_URI_PREFIX + username.getSimpleValue() + "_" + ordinalNumber;
        TopicModel screenConfiguration = mf.newTopicModel(configUri, SCREEN_CONFIG_TYPE, mf.newChildTopicsModel()
                .put(SCREEN_TEMPLATE_NAME, screenTemplateName)
                .put(SCREEN_CONDITION_NAME, screenConditionName)
                .put(SCREEN_TIMEOUT_VALUE, screenTimeout)
                .put(SCREEN_TIMEOUT_VALUE, screenTimeout)
                .put(SCREEN_OPTIONS_BLOB, screenJsonOptions));
        // 4) Create Screen Configuraton Topic
        Topic screenConfigTopic = dm4.createTopic(screenConfiguration);
        log.info("Created Screen Configuration with URI=\"" + configUri + " " + screenConfigTopic.getId() + "\" " +
                "Condition: " + screenConditionName + ", " +  "Template: \""
                + screenTemplateName + "\"and Ordinal Nr" + ". " + ordinalNumber);
        // 5) Assign Scren Configuration Topic to Username via "Active Configuration" edge
        createTrialConfigUserAssignment(screenConfigTopic, username);
    }

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
        List<Topic> participants = dm4.getTopicsByType(USERNAME_TYPE_URI);
        log.info("Gathering reporting for overall " + participants.size() + " user accounts");
        report.append("VP ID\tScreen Template\tScreen Condition\tScreen Timeout\tScreen Options\tAction Type\tAction Value");
        report.append("\n");
        for (Topic username : participants) {
            String usernameValue = username.getSimpleValue().toString();
            List<RelatedTopic> screenReports = username.getRelatedTopics("dm4.core.association",ROLE_DEFAULT,
                    ROLE_DEFAULT, SCREEN_REPORT_TYPE);
            if (screenReports.size() > 0) {
                log.info("Fetched " + screenReports.size() + " Screen Reports for \"" + usernameValue + "\"");
                for (RelatedTopic screenReport : screenReports) {
                    // load full report
                    screenReport.loadChildTopics();
                    // load corresponding screen configuration topic
                    Topic screenConfigurationTopic = null;
                    String templateName = "", conditionValue = "", options = "", timeout = "";
                    if (screenReport.getChildTopics().getTopicOrNull(SCREEN_CONFIG_TYPE) != null) {
                        screenConfigurationTopic = screenReport.getChildTopics().getTopic(SCREEN_CONFIG_TYPE);
                        screenConfigurationTopic.loadChildTopics();
                        templateName = screenConfigurationTopic.getChildTopics().getString(SCREEN_TEMPLATE_NAME);
                        conditionValue = screenConfigurationTopic.getChildTopics().getString(SCREEN_CONDITION_NAME);
                        options = screenConfigurationTopic.getChildTopics().getString(SCREEN_OPTIONS_BLOB);
                        timeout = screenConfigurationTopic.getChildTopics().getString(SCREEN_TIMEOUT_VALUE);
                    }
                    // load all actions reported for that screen
                    List<RelatedTopic> reportedActions = screenReport.getChildTopics().getTopicsOrNull(SCREEN_ACTION_TYPE);
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
        List<RelatedTopic> screenConfigs = getActiveScreenConfigs(username);
        log.info("Found " + screenConfigs.size() + " active configurations for " + username.getSimpleValue());
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
    private List<RelatedTopic> getActiveScreenConfigs(Topic username) {
        return username.getRelatedTopics(ACTIVE_CONFIGURATION_EDGE, ROLE_DEFAULT, ROLE_DEFAULT, SCREEN_CONFIG_TYPE);
    }

    private void fullActionReport(String usernameValue, String templateName, String conditionValue, String options,
            String timeout, List<Topic> reportedActions, StringBuilder report) {
        for (Topic action : reportedActions) {
            String actionType = "", actionValue = "";
            if (action.getChildTopics().getTopicOrNull(SCREEN_ACTION_NAME_TYPE) != null) {
                actionType = action.getChildTopics().getString(SCREEN_ACTION_NAME_TYPE);
            }
            if (action.getChildTopics().getTopicOrNull(SCREEN_ACTION_VALUE_TYPE) != null) {
                actionValue = action.getChildTopics().getString(SCREEN_ACTION_VALUE_TYPE);
            }
            report.append(usernameValue).append("\t").append(templateName).append("\t").append(conditionValue).
                append("\t").append(timeout).append("\t").append(options).append("\t").append(actionType)
                .append("\t").append(actionValue).append("\n");
        }
    }

    private void customSelectionReport(String usernameValue, String templateName, String conditionValue, String options,
            String timeout, List<RelatedTopic> reportedActions, StringBuilder report) {
        // Sum of selections by "name" in actionValue Object
        HashMap sum = new HashMap();
        for (Topic action : reportedActions) {
            String actionType = "", actionValue = "";
            if (action.getChildTopics().getTopicOrNull(SCREEN_ACTION_NAME_TYPE) != null) {
                actionType = action.getChildTopics().getString(SCREEN_ACTION_NAME_TYPE);
                if (actionType.equals("Select")) {
                    if (action.getChildTopics().getTopicOrNull(SCREEN_ACTION_VALUE_TYPE) != null) {
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
    
    private ArrayList<RelatedTopic> getScreenTopicsSortedByURI(List<RelatedTopic> all) {
        // build up sortable collection of all result-items
        ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
        for (RelatedTopic obj : all) {
            in_memory.add(obj);
        }
        // 1) sort all result-items
        Collections.sort(in_memory, new Comparator<RelatedTopic>() {
            public int compare(RelatedTopic t1, RelatedTopic t2) {
                try { // URI is, either global "webexp.config.10" or per participant "webexp.config.VP X_10"
                    // 1.1) Compare global screen config topics
                    if (t1.getUri().contains(".") && t2.getUri().contains(".")) {
                        // 1.1) If screens were configured manually
                        // throws NotFoundException if our URI was constructed automatically via doImportScreenConfig
                        String one = t1.getUri().substring(t1.getUri().lastIndexOf(".") + 1);
                        String two = t2.getUri().substring(t2.getUri().lastIndexOf(".") + 1);
                        if ( Long.parseLong(one) < Long.parseLong(two)) return -1;
                        if ( Long.parseLong(one) > Long.parseLong(two)) return 1;
                    } else {
                        log.warning("We could not sort the active screen configurations for this users due to a "
                            + "misconfigured URI in 1.\"" + t1.getUri() + "\" OR 2. \"" + t2.getUri());
                    }
                } catch (Exception nfe) {
                    // 1.2) If screen configs were imported via our per-user function
                    try {
                        if (t1.getUri().contains("_") && t2.getUri().contains("_")) {
                            // 1.1) If screens were configured manually
                            String one = t1.getUri().substring(t1.getUri().lastIndexOf("_") + 1);
                            String two = t2.getUri().substring(t2.getUri().lastIndexOf("_") + 1);
                            if ( Long.parseLong(one) < Long.parseLong(two)) return -1;
                            if ( Long.parseLong(one) > Long.parseLong(two)) return 1;
                        } else {
                            log.warning("We could not sort the active screen configurations for this users due to a "
                                    + "misconfigured URI in 1.\"" + t1.getUri() + "\" OR 2. \"" + t2.getUri());
                        }
                        return 0;
                    } catch(Exception nf) {
                        log.warning("Error while accessing URI of Topic 1: " + t1.getUri() + " Topic2: "
                                + t2.getUri() + " Ordinal Number Seperator Not Found " + nfe.getMessage());
                    }
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
        return dm4.getTopicByValue(USERNAME_TYPE_URI, new SimpleValue(username));
    }

    private Association createTrialConfigUserAssignment(Topic trialConfig, Topic username) {
        return dm4.createAssociation(mf.newAssociationModel(ACTIVE_CONFIGURATION_EDGE,
            mf.newTopicRoleModel(username.getId(), ROLE_DEFAULT),
            mf.newTopicRoleModel(trialConfig.getId(), ROLE_DEFAULT)));
    }

}
