
package de.akmiraketen.plugins.experiments;

import de.akmiraketen.plugins.experiments.model.ParticipantViewModel;
import de.akmiraketen.plugins.experiments.model.TrialConfigViewModel;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;



/**
 * A simple but flexible browser application to conduct experiments on the 
 * perception and the processing of media items.
 *
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>), 2014
 * @website https://github.com/ifl-vk/web-exp
 * @version 0.2-SNAPSHOT
 */

@Path("/web-exp")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebExperimentsPlugin extends PluginActivator {

    private Logger log = Logger.getLogger(getClass().getName());

    private final String DEEPAMEHTA_VERSION = "DeepaMehta 4.4";
    private final String WEB_EXPERIMENTS_VERSION = "0.0.1-SNAPSHOT";
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
    
    private static final String TRIAL_CONFIG_TYPE = "de.akmiraketen.webexp.trial_config";
    private static final String TRIAL_CONDITION_TYPE = "de.akmiraketen.webexp.trial_condition";
    private static final String TRIAL_CONDITION_BLOCKS_SIZE_TYPE = "de.akmiraketen.webexp.trial_condition_block_size";
    private static final String TRIAL_SEEN_EDGE_TYPE = "de.akmiraketen.webexp.trial_seen_edge";
    private static final String MARKER_CONFIG_EDGE_TYPE = "de.akmiraketen.webexp.config_marker_symbol";
    
    // -- Trial Report & Config 
    
    private static final String TRIAL_REPORT_URI = "de.akmiraketen.webexp.trial_report";
    
    private static final String TRIAL_CONFIG_MAP_ID = "de.akmiraketen.webexp.trial_map_id";
    private static final String TRIAL_CONFIG_PLACE_TO_PIN = "de.akmiraketen.webexp.trial_place_to_pin";
    private static final String TRIAL_CONFIG_PLACE_FROM1 = "de.akmiraketen.webexp.trial_place_from_1";
    private static final String TRIAL_CONFIG_PLACE_TO1 = "de.akmiraketen.webexp.trial_place_to_1";
    
    // ----- Place Config
    
    private static final String PLACE_CONFIG = "de.akmiraketen.webexp.place_config";
    private static final String PLACE_CONFIG_ID = "de.akmiraketen.webexp.place_id";
    private static final String PLACE_CONFIG_NAME = "de.akmiraketen.webexp.place_name";
    private static final String PLACE_CONFIG_LAT = "de.akmiraketen.webexp.place_latitude";
    private static final String PLACE_CONFIG_LNG = "de.akmiraketen.webexp.place_longitude";
    
    // ----- Trial Pinning Report URIs
    
    private static final String COORDINATES_PINNED_URI = "de.akmiraketen.webexp.report_pinned_coordinates";
    private static final String REACTION_TIME_URI = "de.akmiraketen.webexp.report_pinning_rt";
    private static final String COUNT_OUTSIDE_URI = "de.akmiraketen.webexp.report_pinning_count_outside";
    
    // ----- Trial Estimation Report URIs
    
    private static final String ESTIMATION_REPORT_URI = "de.akmiraketen.webexp.trial_estimation_report";
    private static final String ESTIMATION_NR_URI = "de.akmiraketen.webexp.report_estimation_nr";
    private static final String ESTIMATED_COORDINATES_URI = "de.akmiraketen.webexp.report_estimated_coordinates";
    private static final String ESTIMATED_DISTANCE_URI = "de.akmiraketen.webexp.report_estimated_distance";
    private static final String ESTIMATION_CONFIDENCE = "de.akmiraketen.webexp.report_estimation_confidence";
    private static final String ESTIMATION_TO_START_TIME_URI = "de.akmiraketen.webexp.report_estimated_to_start_time";
    private static final String ESTIMATION_TIME_URI = "de.akmiraketen.webexp.report_estimation_time";
    private static final String ESTIMATION_FROM_PLACE_URI = "de.akmiraketen.webexp.report_from_place_id";
    private static final String ESTIMATION_TO_PLACE_URI = "de.akmiraketen.webexp.report_to_place_id";
   
    
    private static final String TRIAL_CONDITION_A = "webexp.config.pinning";
    private static final String TRIAL_CONDITION_B = "webexp.config.no_pinning";
    private static final int MAX_ESTIMATION_COUNT = 6;
    private static final int OK_NR = 1;
    private static final int FAIL_NR = -1;
    
    
    private static final String SYMBOL_FOLDER = "web-experiments/symbols";

    // ###
    
    private Random random = null;
    
    // --- Plugin Services
    
    @Inject
    private AccessControlService acService = null;
    
    @Inject
    private FilesService fileService = null;
    
    @Inject
    private WorkspacesService workspaceService = null;
    
    @Override
    public void init() {
        log.info("INIT: Thanks for deploying Web Experiments " + WEB_EXPERIMENTS_VERSION);
        random = new Random();
    }
    
    @Override
    public void postInstall() {
        log.info("POST-INSTALL: Initially creating some users for experiments " + WEB_EXPERIMENTS_VERSION);
        generateSomeUsers();
        log.info("POST-INSTALL: Initially creating some folders for web-app " + WEB_EXPERIMENTS_VERSION);
        String parentFolderName = "web-experiments";
        createFolderWithName(parentFolderName, null);
        createFolderWithName("symbols", parentFolderName);
        createFolderWithName("maps", parentFolderName);
        createFolderWithName("sessions", parentFolderName);
        createFolderWithName("scripts", parentFolderName);
    }
    
    // --- All available routes to single pages
    
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getNewScreen() {
        return getStaticResource("web/new.html");
    }
    
    @GET
    @Path("/icon")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getNewIconView() {
        return getStaticResource("web/new.html");
    }
    
    @GET
    @Path("/welcome")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getNewWelcomeView() {
        return getStaticResource("web/new.html");
    }
    
    @GET
    @Path("/finish")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getNewFinishView() {
        return getStaticResource("web/new.html");
    }
    
    @GET
    @Path("/start")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getWelcomeScreen() {
        // ###
        return getStaticResource("web/index.html");
    }
    
    @GET    
    @Path("/trial/{trialId}/pinning")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getTrialPinningScreen(@PathParam("trialId") String trialId) {
        // ### use templates to preset:
        // setViewParameter(trialId)
        return getStaticResource("web/pinning.html");
    }
    
    @GET
    @Path("/trial/{trialId}/estimation")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getTrialEstimationScreen(@PathParam("trialId") String trialId) {
        // ### use templates to preset:
        // setViewParameter(trialId)
        return getStaticResource("web/estimation.html");
    }

    @GET
    @Path("/pract/{trialId}/pinning")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getPracticeTrialPinningScreen(@PathParam("trialId") String trialId) {
        // ### use templates to preset:
        // setViewParameter(trialId)
        return getStaticResource("web/pinning.html");
    }

    @GET
    @Path("/pract/{trialId}/estimation")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getPracticeTrialEstimationScreen(@PathParam("trialId") String trialId) {
        // ### use templates to preset:
        // setViewParameter(trialId)
        return getStaticResource("web/estimation.html");
    }

    @GET
    @Path("/intro/{trialId}")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getIntroductionScreen(@PathParam("trialId") String trialId) {
        // ### use templates to preset:
        // setViewParameter(trialId)
        return getStaticResource("web/introduction.html");
    }

    @GET
    @Path("/pause/{trialId}")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getPauseScreen() {
        // ### use templates to preset:
        // setViewParameter(trialId)
        return getStaticResource("web/pause.html");
    }

    @GET
    @Path("/start/{trialId}")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getStartScreen() {
        // ### use templates to preset:
        // setViewParameter(trialId)
        return getStaticResource("web/start.html");
    }

    
    // --- REST Resources / API Endpoints
    
    
    @GET
    @Path("/report/generate")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String doGenerateCompleteReport() {
        StringBuilder report = new StringBuilder();
        ResultList<RelatedTopic> propositi = dms.getTopics("dm4.accesscontrol.user_account", 0);
        report.append("VP ID\tTrial Condition\tMap ID\tTopin\tTopinname\tPinned\tPinRT\tPinInactive\t");
        report.append("Estfrom.1\tEstfromname.1\tEstto.1\tEsttoname.1\tEstimation.1\tEststart.1\tEstend.1\tEstconfidence.1\t");
        report.append("Estfrom.2\tEstfromname.2\tEstto.2\tEsttoname.2\tEstimation.2\tEststart.2\tEstend.2\tEstconfidence.2\t");
        report.append("Estfrom.3\tEstfromname.3\tEstto.3\tEsttoname.3\tEstimation.3\tEststart.3\tEstend.3\tEstconfidence.3\t");
        report.append("Estfrom.4\tEstfromname.4\tEstto.4\tEsttoname.4\tEstimation.4\tEststart.4\tEstend.4\tEstconfidence.4\t");
        report.append("Estfrom.5\tEstfromname.5\tEstto.5\tEsttoname.5\tEstimation.5\tEststart.5\tEstend.5\tEstconfidence.5");
        report.append("\n");
        for (RelatedTopic vp : propositi.getItems()) {
            Topic username = vp.loadChildTopics(USERNAME_TYPE_URI).getChildTopics().getTopic(USERNAME_TYPE_URI);
            String vpId = username.getSimpleValue().toString();
            ResultList<RelatedTopic> trialReports = username.getRelatedTopics("dm4.core.association", "dm4.core.parent", 
                    "dm4.core.child", TRIAL_REPORT_URI, 0);
            if (trialReports.getTotalCount() > 0) {
                log.info("  Fetched " + trialReports.getTotalCount() + " written to DB for " + vpId);
                for (RelatedTopic trialReport : trialReports.getItems()) {
                    trialReport.loadChildTopics();
                    String trialConfigId = trialReport.loadChildTopics("de.akmiraketen.webexp.report_trial_config_id")
                            .getChildTopics().getString("de.akmiraketen.webexp.report_trial_config_id");
                    if (trialConfigId.contains("trial") || trialConfigId.contains("pract")) {
                        Topic trialConfig = dms.getTopic("uri", new SimpleValue(trialConfigId));
                        if (trialConfig != null) { // trial configuration could be loaded
                            // General Info
                            String trialCondition = trialConfig.loadChildTopics(TRIAL_CONDITION_TYPE)
                                    .getChildTopics().getString(TRIAL_CONDITION_TYPE);
                            String mapId = trialConfig.loadChildTopics(TRIAL_CONFIG_MAP_ID)
                                    .getChildTopics().getString(TRIAL_CONFIG_MAP_ID);
                            // Pinning Data
                            String placeToPinId = trialConfig.loadChildTopics(TRIAL_CONFIG_PLACE_TO_PIN)
                                .getChildTopics().getString(TRIAL_CONFIG_PLACE_TO_PIN);
                            Topic placeConfig = getConfiguredPlace(placeToPinId);
                            String placeToPinName = getConfiguredPlaceName(placeConfig);
                            String placeCoordinates = getConfiguredPlaceCoordinates(placeConfig);
                            String pinnedCoordinates = "-1;-1";
                            int pinningRT = -1, pinInactive = -1;
                            try {
                                pinnedCoordinates = trialReport.loadChildTopics(COORDINATES_PINNED_URI)
                                    .getChildTopics().getString(COORDINATES_PINNED_URI);
                                pinningRT = trialReport.loadChildTopics(REACTION_TIME_URI)
                                        .getChildTopics().getInt(REACTION_TIME_URI);
                                pinInactive = trialReport.loadChildTopics(COUNT_OUTSIDE_URI)
                                        .getChildTopics().getInt(COUNT_OUTSIDE_URI);
                            } catch (Exception e) {
                                log.warning("No pinning data was recorded during trial: " + trialConfigId + " for " + username.getSimpleValue());
                            }
                            // Estimation Report Data
                            String estimation1 = "", estFrom1 = "", estFromName1 = "", estTo1 = "", estToName1 = "";
                            int estStart1 = -1, estEnd1 = -1, estConfidence1 = -1;
                            String estimation2 = "", estFrom2 = "", estFromName2 = "", estTo2 = "", estToName2 = "";
                            int estStart2 = -1, estEnd2 = -1, estConfidence2 = -1;
                            String estimation3 = "", estFrom3 = "", estFromName3 = "", estTo3 = "", estToName3 = "";
                            int estStart3 = -1, estEnd3 = -1, estConfidence3 = -1;
                            String estimation4 = "", estFrom4 = "", estFromName4 = "", estTo4 = "", estToName4 = "";
                            int estStart4 = -1, estEnd4 = -1, estConfidence4 = -4;
                            String estimation5 = "", estFrom5 = "", estFromName5 = "", estTo5 = "", estToName5 = "";
                            int estStart5 = -1, estEnd5 = -1, estConfidence5 = -1;
                            try {
                                List<Topic> estimationReports = trialReport.getChildTopics().getTopics(ESTIMATION_REPORT_URI);
                                for (Topic estimationReport : estimationReports) {
                                    int estimationNr = -1;
                                    estimationReport.loadChildTopics();
                                    estimationNr = estimationReport.getChildTopics().getInt(ESTIMATION_NR_URI);
                                    Topic fromPlace = getConfiguredPlace(estimationReport.getChildTopics().getString(ESTIMATION_FROM_PLACE_URI));
                                    Topic toPlace = getConfiguredPlace(estimationReport.getChildTopics().getString(ESTIMATION_TO_PLACE_URI));
                                    switch (estimationNr) {
                                        case 1:
                                            estFrom1 = getConfiguredPlaceCoordinates(fromPlace);
                                            estFromName1 = getConfiguredPlaceName(fromPlace);
                                            estTo1 = getConfiguredPlaceCoordinates(toPlace);;
                                            estToName1 = getConfiguredPlaceName(toPlace);
                                            estimation1 = estimationReport.getChildTopics().getString(ESTIMATED_COORDINATES_URI);
                                            estStart1 = estimationReport.getChildTopics().getInt(ESTIMATION_TO_START_TIME_URI);
                                            estEnd1 = estimationReport.getChildTopics().getInt(ESTIMATION_TIME_URI);
                                            estConfidence1 = estimationReport.getChildTopics().getInt(ESTIMATION_CONFIDENCE);
                                        case 2:
                                            estFrom2 = getConfiguredPlaceCoordinates(fromPlace);
                                            estFromName2 = getConfiguredPlaceName(fromPlace);
                                            estTo2 = getConfiguredPlaceCoordinates(toPlace);;
                                            estToName2 = getConfiguredPlaceName(toPlace);
                                            estimation2 = estimationReport.getChildTopics().getString(ESTIMATED_COORDINATES_URI);
                                            estStart2 = estimationReport.getChildTopics().getInt(ESTIMATION_TO_START_TIME_URI);
                                            estEnd2 = estimationReport.getChildTopics().getInt(ESTIMATION_TIME_URI);
                                            estConfidence2 = estimationReport.getChildTopics().getInt(ESTIMATION_CONFIDENCE);
                                        case 3:
                                            estFrom3 = getConfiguredPlaceCoordinates(fromPlace);
                                            estFromName3 = getConfiguredPlaceName(fromPlace);
                                            estTo3 = getConfiguredPlaceCoordinates(toPlace);;
                                            estToName3 = getConfiguredPlaceName(toPlace);
                                            estimation3 = estimationReport.getChildTopics().getString(ESTIMATED_COORDINATES_URI);
                                            estStart3 = estimationReport.getChildTopics().getInt(ESTIMATION_TO_START_TIME_URI);
                                            estEnd3 = estimationReport.getChildTopics().getInt(ESTIMATION_TIME_URI);
                                            estConfidence3 = estimationReport.getChildTopics().getInt(ESTIMATION_CONFIDENCE);
                                        case 4:
                                            estFrom4 = getConfiguredPlaceCoordinates(fromPlace);
                                            estFromName4 = getConfiguredPlaceName(fromPlace);
                                            estTo4 = getConfiguredPlaceCoordinates(toPlace);;
                                            estToName4 = getConfiguredPlaceName(toPlace);
                                            estimation4 = estimationReport.getChildTopics().getString(ESTIMATED_COORDINATES_URI);
                                            estStart4 = estimationReport.getChildTopics().getInt(ESTIMATION_TO_START_TIME_URI);
                                            estEnd4 = estimationReport.getChildTopics().getInt(ESTIMATION_TIME_URI);
                                            estConfidence4 = estimationReport.getChildTopics().getInt(ESTIMATION_CONFIDENCE);
                                        case 5:
                                            estFrom5 = getConfiguredPlaceCoordinates(fromPlace);
                                            estFromName5 = getConfiguredPlaceName(fromPlace);
                                            estTo5 = getConfiguredPlaceCoordinates(toPlace);;
                                            estToName5 = getConfiguredPlaceName(toPlace);
                                            estimation5 = estimationReport.getChildTopics().getString(ESTIMATED_COORDINATES_URI);
                                            estStart5 = estimationReport.getChildTopics().getInt(ESTIMATION_TO_START_TIME_URI);
                                            estEnd5 = estimationReport.getChildTopics().getInt(ESTIMATION_TIME_URI);
                                            estConfidence5 = estimationReport.getChildTopics().getInt(ESTIMATION_CONFIDENCE);
                                    }
                                }
                            } catch (Exception e) {
                                log.warning("No estimation data was recorded during trial " + trialConfigId + " for " + username.getSimpleValue());
                            }
                            // Write line
                            report.append(vpId + "\t" + trialCondition + "\t" + mapId + "\t" + placeCoordinates + "\t" + placeToPinName
                                    + "\t" + pinnedCoordinates + "\t" + pinningRT + "\t" + pinInactive
                                    + "\t" + estFrom1 + "\t" + estFromName1 + "\t" + estTo1 + "\t" + estToName1 + "\t" + estimation1 + "\t" + estStart1 + "\t" + estEnd1 + "\t" + estConfidence1
                                    + "\t" + estFrom2 + "\t" + estFromName2 + "\t" + estTo2 + "\t" + estToName2 + "\t" + estimation2 + "\t" + estStart2 + "\t" + estEnd2 + "\t" + estConfidence2
                                    + "\t" + estFrom3 + "\t" + estFromName3 + "\t" + estTo3 + "\t" + estToName3 + "\t" + estimation3 + "\t" + estStart3 + "\t" + estEnd3 + "\t" + estConfidence3
                                    + "\t" + estFrom4 + "\t" + estFromName4 + "\t" + estTo4 + "\t" + estToName4 + "\t" + estimation4 + "\t" + estStart4 + "\t" + estEnd4 + "\t" + estConfidence4
                                    + "\t" + estFrom5 + "\t" + estFromName5 + "\t" + estTo5 + "\t" + estToName5 + "\t" + estimation5 + "\t" + estStart5 + "\t" + estEnd5 + "\t" + estConfidence5);
                            report.append("\n");
                        } else { // trial configuration could not be loaded..
                            log.warning("System Trial Configuration changed"
                                + " - Fetching Trial Config with URI: " + trialConfigId + " failed --- SKIPPED");
                        }
                    }
                }
            }
        }
        return report.toString();
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
     * 
     * @param topicId   long value id of file topic
     * @return  related File topic
     */
    
    @GET
    @Path("/symbol/choose/{topicId}")
    @Transactional
    public Topic chooseSymbolFile(@PathParam("topicId") long topicId) {
        log.info("Setting Marker for User: " + acService.getUsername() + " topicId: " + topicId);
        Topic relatedIconTopic = dms.getTopic(topicId);
        Topic username = getRequestingUser();
        try {
            log.info("Icon topic to be related is " + relatedIconTopic.getId());
            assignNewMarkerSymbolToUsername(relatedIconTopic, username);
        } catch(RuntimeException e) {
            throw new RuntimeException(e);
        }
        return relatedIconTopic.loadChildTopics(FILE_PATH_TYPE);
    }
    
    /** 
     * Fetches all Trial Configs currently in database.
     * @return 
     */
    
    @GET
    @Path("/trial/all")
    public ResultList<RelatedTopic> getAllTrialConfigs() {
        return dms.getTopics(TRIAL_CONFIG_TYPE, 0);
    }

    @GET
    @Path("/trial/{trialId}")
    public TrialConfigViewModel getTrialConfigViewModel(@PathParam("trialId") long id) {
        Topic trial = dms.getTopic(id);
        return new TrialConfigViewModel(trial.loadChildTopics(), dms);
    }
    
    @GET
    @Path("/participant")
    public ParticipantViewModel getParticipantViewModel() {
        Topic username = getRequestingUser();
        return new ParticipantViewModel(username, dms);
    }
    
    /** 
     * Fetches all Trial Configs of a certain condition currently in database.
     * @parameter   condition   String containing an URI of an instance of "de.akmiraketen.webexp.trial_condition"
     * @return 
     */
    
    @GET
    @Path("/trial/by_condition/{instanceUri}")
    public ResultList<RelatedTopic> getAllTrialConfigsByCondition(@PathParam("instanceUri") String givenUri) {
        ResultList<RelatedTopic> trials = dms.getTopics(TRIAL_CONFIG_TYPE, 0);
        // 
        Iterator<RelatedTopic> i = trials.iterator();
        while (i.hasNext()) {
            RelatedTopic trial = i.next();
            Topic trial_condition = trial.getChildTopics().getTopic(TRIAL_CONDITION_TYPE);
            if (!trial_condition.getUri().equals(givenUri)) {
                i.remove();
            }
        }
        return trials;
    }
    
    /** 
     * Fetches all unseen Trial Configs of a certain condition for a certain user.
     * @parameter   condition   String containing an URI of an instance of "de.akmiraketen.webexp.trial_condition"
     * @return 
     */
    
    @GET
    @Path("/trial/unseen/{instanceUri}")
    public ResultList<RelatedTopic> getUnseenTrialConfigsByCondition(@PathParam("instanceUri") String givenUri) {
        Topic user = getRequestingUser();
        ResultList<RelatedTopic> trials = getAllTrialConfigsByCondition(givenUri);
        // Check if trial_seen_edge exists between trial and user
        Iterator<RelatedTopic> k = trials.iterator();
        while (k.hasNext()) {
            if (hasSeenTrial(user, k.next())) k.remove();
        }
        return trials;
    }
    
    @GET
    @Path("/trial/unseen/random/next")
    public long getNextUnseenRandomTrialId() {
        Topic user = getRequestingUser();
        ParticipantViewModel vp = new ParticipantViewModel(user, dms);
        ResultList<RelatedTopic> unseen_trials = getUnseenTrialConfigsByCondition(vp.getFirstTrialConditionURI());
        // if no more trials for requesting user under her default condition
        if (unseen_trials.getSize() == 0) { // > check the other condition for unseen trials
            // now we should redirect to a "Pause"-screen but just once for each VP!
            if (vp.getFirstTrialConditionURI().equals(TRIAL_CONDITION_A)) {
                unseen_trials = getUnseenTrialConfigsByCondition(TRIAL_CONDITION_B);
            } else if (vp.getFirstTrialConditionURI().equals(TRIAL_CONDITION_B)) {
                unseen_trials = getUnseenTrialConfigsByCondition(TRIAL_CONDITION_A);
            }
        }
        if (unseen_trials.getItems().isEmpty()) return FAIL_NR; // experiment finished > no unseen trial left
        int index = random.nextInt(unseen_trials.getItems().size());
        return unseen_trials.getItems().get(index).getId();
    }
    
    @GET
    @Path("/nextpage")
    public Response getNextPage() throws URISyntaxException {
        Topic user = getRequestingUser();
        long trialId = getNextUnseenTrialId();
        if (trialId == FAIL_NR) {
            log.info("Experiment finished, no configured trial left for requesting user");
            return Response.seeOther(new URI("/web-exp/finish")).build();
        }
        Topic trialConfig = dms.getTopic(trialId);
        if (!hasSeenTrial(user, trialConfig)) {
            if (trialConfig.getUri().contains("intro")) {
                // route to intro page
                log.info("REDIRECT to INTRO pages " + trialConfig.getUri());
                URI location = new URI("/web-exp/intro/" + trialConfig.getId());
                return Response.seeOther(location).build();
            } else if (trialConfig.getUri().contains("break")) {
                log.info("REDIRECT to PAUSE page \"" + trialConfig.getUri() + "\"");
                URI location = new URI("/web-exp/pause/" + trialConfig.getId());
                return Response.seeOther(location).build();
            } else if (trialConfig.getUri().contains("start")) {
                log.info("REDIRECT to START page \"" + trialConfig.getUri() + "\"");
                URI location = new URI("/web-exp/start/" + trialConfig.getId());
                return Response.seeOther(location).build();
            } else if (trialConfig.getUri().contains("pract")) {
                // start practice session
                log.info("REDIRECT to PRACTICE trial \"" + trialConfig.getUri() + "\"");
                URI location = new URI("/web-exp/pract/" + trialConfig.getId() + "/pinning");
                return Response.seeOther(location).build();
            } else {
                log.info("LOADING next TRIAL .. \"" + trialConfig.getUri() + "\"");
                URI location = new URI("/web-exp/trial/" + trialConfig.getId() + "/pinning");
                return Response.seeOther(location).build();
            }
        }
        return Response.ok(FAIL_NR).build(); // experiment finished > no unseen trial left
    }
    
    @GET
    @Path("/trial/{trialId}/seen")
    @Transactional
    public Response doMarkTrialAsSeen(@PathParam("trialId") long trialId) {
        Topic user = getRequestingUser();
        Association trial_seen = user.getAssociation(TRIAL_SEEN_EDGE_TYPE, 
                ROLE_DEFAULT, ROLE_DEFAULT, trialId);
        if (trial_seen != null) {
            long nextTrialId = getNextUnseenTrialId();
            log.warning("This trial was already seen by our VP - Please load next => " + nextTrialId);
            // throw new WebApplicationException(new InvalidParameterException(), Status.BAD_REQUEST);
            return Response.ok(nextTrialId).build();
        }
        dms.createAssociation(new AssociationModel(TRIAL_SEEN_EDGE_TYPE, 
                new TopicRoleModel(user.getId(), "dm4.core.default"), 
                new TopicRoleModel(trialId, "dm4.core.default")));
        return Response.ok(OK_NR).build();
    }
    
    @GET
    @Path("/estimation/next/{trialId}")
    public Response getNextTrialEstimationNr(@PathParam("trialId") long id) {
        Topic user = getRequestingUser();
        String trialConfigUri = dms.getTopic(id).getUri();
        log.info("Fetching Trial Report for Trial: " + trialConfigUri + " and " + user.getSimpleValue());
        ResultList<RelatedTopic> trialReports = user.getRelatedTopics("dm4.core.association", 
                "dm4.core.parent", "dm4.core.child", "de.akmiraketen.webexp.trial_report", 0);
        long count = 1; // default estimation is first
        for (RelatedTopic trialReport : trialReports) {
            String trial = trialReport.getChildTopics().getString("de.akmiraketen.webexp.report_trial_config_id");
            if (trialConfigUri.equals(trial)) {
                log.fine("Re-using Trial Report for Trial: " + trialConfigUri + " and " + user.getSimpleValue());
                trialReport.loadChildTopics(ESTIMATION_REPORT_URI);
                if (trialReport.getChildTopics().has(ESTIMATION_REPORT_URI)) {
                    List<Topic> estimations = trialReport.getChildTopics().getTopics(ESTIMATION_REPORT_URI);
                    log.fine("Trial report has " + estimations.size()+ " estimation reports associated.. ");
                    count = estimations.size() + 1; // number of next estimation for this user and this trial
                }
                // check if we are now over the maximum number of estimations
                if (count == MAX_ESTIMATION_COUNT) {
                    count = getNextUnseenTrialId(); // return ID of next unseen trial instead of estimationNR
                }
                return Response.ok(count).build();
            } else {
                log.fine("> Next Estimation: " + trialConfigUri + " === " + trial);
            }
        }
        return Response.ok(count).build();
    }
    
    @POST
    @Path("/estimation/{trialId}/{estimationNr}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void storeEstimationReport(String payload, @PathParam("trialId") long trialId, 
            @PathParam("estimationNr") int estimation) {
        Topic user = getRequestingUser();
        try {
            // 1 Parse POST Request
            log.info("POST Estimation: " + payload);
            JSONObject data = new JSONObject(payload);
            Topic trialConfig = getTrialConfigTopic(trialId);
            JSONObject coordinates = data.getJSONObject("geo_coordinates");
            String latitude = coordinates.getString("latitude");
            String longitude = coordinates.getString("longitude");
            String fromPlaceId = data.getString("from_place_id");
            String toPlaceId = data.getString("to_place_id");
            int estimatedDistance = data.getInt("estimated_distance");
            int confidenceValue = data.getInt("certainty");
            int toStartTime = data.getInt("to_start_time");
            int estimationTime = data.getInt("estimation_time");
            log.fine("ESTIMATED Coordinates for " + estimation + " are \"" + latitude + "," 
                    + longitude + "\" - by " + user.getSimpleValue() + " on " + trialConfig.getSimpleValue());
            // 2 Check consistency of this request for reporting
            Topic report = getOrCreateTrialPinningReportTopic(trialId, user);
            List<Topic> estimations = null;
            report.loadChildTopics(ESTIMATION_REPORT_URI);
            if (report.getChildTopics().has(ESTIMATION_REPORT_URI)) {
                estimations = report.getChildTopics().getTopics(ESTIMATION_REPORT_URI);
                log.fine("Trial report has " + estimations.size()+ " estimation reports associated.. ");
                for (Topic estimationReport : estimations) {
                    estimationReport.loadChildTopics(ESTIMATION_NR_URI);
                    int eNr = estimationReport.getChildTopics().getInt(ESTIMATION_NR_URI);
                    if (eNr == estimation) throw new InvalidParameterException("A trial estimation report "
                            + "already exists for this user, trial and estimation nr!");
                }
            } else {
                log.info("Trial report has NO  estimation reports associated.. ");
            }
            // 3 Start to build up new trial estimation report
            ChildTopicsModel values = new ChildTopicsModel()
                .put(ESTIMATED_COORDINATES_URI, latitude + ";" + longitude)
                .put(ESTIMATION_FROM_PLACE_URI, fromPlaceId)
                .put(ESTIMATION_TO_PLACE_URI, toPlaceId)
                .put(ESTIMATION_TO_START_TIME_URI, toStartTime)
                .put(ESTIMATION_TIME_URI, estimationTime)
                .put(ESTIMATED_DISTANCE_URI, "" + estimatedDistance) // stored as dm4.core.text
                .put(ESTIMATION_NR_URI, estimation)
                .put(ESTIMATION_CONFIDENCE, confidenceValue);
            TopicModel estimationModel = new TopicModel(ESTIMATION_REPORT_URI, values);
            // 4 assign new trial estimation report to trial report
            report.setChildTopics(new ChildTopicsModel()
                .add(ESTIMATION_REPORT_URI, estimationModel));
            // sanity check for the log s
            estimations = report.getChildTopics().getTopics(ESTIMATION_REPORT_URI);
            log.info("NOW Trial report has " + estimations.size()+ " estimation reports associated.. ");
            /** ChildTopicsModel reportModel = report.getChildTopics().getModel();
                reportModel.add(ESTIMATION_REPORT_URI, estimationModel);
            report.setChildTopics(reportModel); **/
        } catch (JSONException e) {
            // ### store estimation in trial report for user
            log.warning("Failed to parse estimation data: " +  e.getClass().toString() + ", " + e.getMessage());
            throw new WebApplicationException(new RuntimeException("Parsing " + payload + " failed"), 
               500);
        } catch (InvalidParameterException ipe) {
            throw new WebApplicationException(ipe, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            // ### store estimation in trial report for user
            log.warning("Failed to store estimation data: " +  e.getClass().toString() + ", " + e.getMessage());
            throw new WebApplicationException(e, 500);
        }
    }
    
    @POST
    @Path("/pinning/{trialId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void storePinningData(String payload, @PathParam("trialId") long trialId) {
        Topic user = getRequestingUser();
        try {
            // 1 Parse data of POST request
            log.fine("POST Pinning: " + payload);
            JSONObject data = new JSONObject(payload);
            Topic trialConfig = getTrialConfigTopic(trialId);
            JSONObject coordinates = data.getJSONObject("geo_coordinates");
            int countClickOutside = data.getInt("count_click_outside");
            int reactionTime = data.getInt("reaction_time");
            String latitude = coordinates.getString("latitude");
            String longitude = coordinates.getString("longitude");
            log.fine("Pinned Coordinates are \"" + latitude + "," 
                    + longitude + "\" - by " + user.getSimpleValue() + " on " + trialConfig.getSimpleValue());
            // 2 Start new trial report
            Topic report = getOrCreateTrialPinningReportTopic(trialId, user);
            ChildTopicsModel values = new ChildTopicsModel()
                .put(COORDINATES_PINNED_URI, latitude + ";" + longitude)
                .put(REACTION_TIME_URI, reactionTime)
                .put(COUNT_OUTSIDE_URI, countClickOutside);
            report.setChildTopics(values);
        } catch (JSONException e) {
            // ### store estimation in trial report for user
            log.warning("Failed to parse pinning data: " +  e.getClass().toString() + ", " + e.getMessage());
            throw new WebApplicationException(new RuntimeException("Parsing " + payload + " failed"), 
               500);
        } catch (Exception e) {
            // ### store estimation in trial report for user
            log.warning("Failed to store pinning data: " +  e.getClass().toString() + ", " + e.getMessage());
            throw new WebApplicationException(e, 500);
        }
    }
    
    // --- Helper Methods
    
    private ArrayList<RelatedTopic> getAllTrialsSortedByURI(ResultList<RelatedTopic> all) {
        // build up sortable collection of all result-items
        ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
        for (RelatedTopic obj : all) {
            in_memory.add(obj);
        }
        // sort all result-items
        Collections.sort(in_memory, new Comparator<RelatedTopic>() {
            public int compare(RelatedTopic t1, RelatedTopic t2) {
                try { // ### webexp.config.intro + webexp.config.pract
                    int index = "webexp.config.trial".length();
                    String one = t1.getUri().substring(index);
                    String two = t2.getUri().substring(index);
                    if ( Long.parseLong(one.toString()) < Long.parseLong(two.toString()) ) return -1;
                    if ( Long.parseLong(one.toString()) > Long.parseLong(two.toString()) ) return 1;
                } catch (Exception nfe) {
                    log.warning("Error while accessing URI of Topic 1: " + t1.getId() + " Topic2: "
                            + t2.getId() + " nfe: " + nfe.getMessage());
                    return 0;
                }
                return 0;
            }
        });
        return in_memory;
    }
    
    private long getNextUnseenTrialId() {
        Topic user = getRequestingUser();
        ResultList<RelatedTopic> all_trials = dms.getTopics(TRIAL_CONFIG_TYPE, 0);
        ArrayList<RelatedTopic> sorted_trial_config_lines = getAllTrialsSortedByURI(all_trials);
        Iterator<RelatedTopic> iterator = sorted_trial_config_lines.iterator();
        while (iterator.hasNext()) {
            RelatedTopic trialConfig = iterator.next();
            if (!hasSeenTrial(user, trialConfig)) {
                return trialConfig.getId();
            }
        }
        return FAIL_NR; // experiment finished > no unseen trial left
    }

    private boolean hasSeenTrial(Topic user, Topic trial) {
        Association trial_seen = trial.getAssociation(TRIAL_SEEN_EDGE_TYPE,
            ROLE_DEFAULT, ROLE_DEFAULT, user.getId());
        return trial_seen != null;
    }

    private Topic getOrCreateTrialPinningReportTopic (long trialId, Topic user) {
        DeepaMehtaTransaction tx = dms.beginTx();
        Topic report = null;
        String trialConfigUri = dms.getTopic(trialId).getUri();
        ResultList<RelatedTopic> trialReports = user.getRelatedTopics("dm4.core.association", 
                "dm4.core.parent", "dm4.core.child", "de.akmiraketen.webexp.trial_report", 0);
        for (RelatedTopic trialReport : trialReports) {
            String trial = trialReport.getChildTopics().getString("de.akmiraketen.webexp.report_trial_config_id");
            if (trialConfigUri.equals(trial)) {
                log.info("Re-using Trial Report for Trial: " + trialConfigUri + " and VP " + user.getSimpleValue());
                return trialReport;
            }
        }
        try {
            log.info("Creating new Trial Report for user " + user.getSimpleValue() + " and Trial " + trialConfigUri);
            ChildTopicsModel child = new ChildTopicsModel(new JSONObject()
                    .put("de.akmiraketen.webexp.report_trial_config_id", trialConfigUri));
            TopicModel model = new TopicModel("de.akmiraketen.webexp.trial_report", child);
            report = dms.createTopic(model);
            dms.createAssociation(new AssociationModel("dm4.core.association", 
                    new TopicRoleModel(user.getId(), "dm4.core.parent"), 
                    new TopicRoleModel(report.getId(), "dm4.core.child")));
            tx.success();
        } catch (JSONException ex) {
            log.severe("Could not create a Trial Report for user ..");
        } finally {
            tx.finish();
        }
        return report;
    }
    
    private Topic getConfiguredPlace(String id) {
        Topic placeConfigIdTopic = dms.getTopic(PLACE_CONFIG_ID, new SimpleValue(id));
        return placeConfigIdTopic.getRelatedTopic("dm4.core.composition", "dm4.core.child", 
                "dm4.core.parent", PLACE_CONFIG);
    }
    
    private String getConfiguredPlaceName(Topic configTopic) {
        configTopic.loadChildTopics(PLACE_CONFIG_NAME);
        return configTopic.getChildTopics().getString(PLACE_CONFIG_NAME);
    }
    
    private String getConfiguredPlaceCoordinates(Topic configTopic) {
        String lat = configTopic.loadChildTopics(PLACE_CONFIG_LAT).getChildTopics().getString(PLACE_CONFIG_LAT);
        String lng = configTopic.loadChildTopics(PLACE_CONFIG_LNG).getChildTopics().getString(PLACE_CONFIG_LNG);
        return lat + ";" + lng;
    }
    
    public Topic getTrialConfigTopic(@PathParam("trialId") long id) {
        return dms.getTopic(id).loadChildTopics();
    }
    
    private Topic getRequestingUser() {
        String username = acService.getUsername();
        if (username == null || username.isEmpty()) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        return dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username));
    }
    
    private void generateSomeUsers() {
        // for 1000 do acService.createUser()
        log.info("Setting up some new users for Web Experiments");
        DeepaMehtaTransaction tx = dms.beginTx();
        String conditionValue = TRIAL_CONDITION_A;
        try {
            for (int i=1; i<=200; i++) {
                String username = "VP "+ i;
                if (isUsernameAvailable(username)) {
                    if (i > 100) conditionValue = TRIAL_CONDITION_B;
                    Credentials cred = new Credentials(username, "");
                    ChildTopicsModel userAccount = new ChildTopicsModel()
                        .put(USERNAME_TYPE_URI, cred.username)
                        .put(USER_PASSWORD_TYPE_URI, cred.password)
                        .putRef(TRIAL_CONDITION_TYPE, conditionValue)
                        .put(TRIAL_CONDITION_BLOCKS_SIZE_TYPE, 15);
                    // ### set user account to "Blocked" until verified (introduce this in a new migration)
                    TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
                    Topic vpAccount = dms.createTopic(userModel);
                    Topic usernameTopic = vpAccount.loadChildTopics(USERNAME_TYPE_URI)
                            .getChildTopics().getTopic(USERNAME_TYPE_URI);
                    workspaceService.assignToWorkspace(usernameTopic, workspaceService.getDefaultWorkspace().getId());
                    setDefaultAdminACLEntries(vpAccount);
                    log.info("Created user \"" + username + "\" for web-experiments.");
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
        // fixme: framework should also allow us to query case insensitve for a username
        Topic userName = dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username));
        return (userName == null);
    }
        
    private void assignNewMarkerSymbolToUsername(Topic relatedIconTopic, Topic username) {
        // deleting former marker config for given username
        Topic account = username.getRelatedTopic("dm4.core.composition", "dm4.core.child", 
                "dm4.core.parent", "dm4.accesscontrol.user_account");
        List<Association> assignments = account.getAssociations();
        Iterator<Association> i = assignments.iterator();
        while (i.hasNext()) {
            Association assoc = i.next();
            if (assoc.getTypeUri().equals(MARKER_CONFIG_EDGE_TYPE)) {
                log.info("Deleting former Marker Config Assignment..");
                i.remove();
                assoc.delete();
            }
        }
        // before setting a new one (and make sure there is always just one)
        createSymbolUserAssignment(relatedIconTopic, account);
    }
    
    private Association createSymbolUserAssignment(Topic relatedIconTopic, Topic account) {
        return dms.createAssociation(new AssociationModel(MARKER_CONFIG_EDGE_TYPE, 
            new TopicRoleModel(account.getId(), ROLE_DEFAULT), 
            new TopicRoleModel(relatedIconTopic.getId(), ROLE_DEFAULT)));
    }
    
}
