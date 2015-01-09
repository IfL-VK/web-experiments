
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
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.files.DirectoryListing;
import de.deepamehta.plugins.files.DirectoryListing.FileItem;
import de.deepamehta.plugins.files.ItemKind;
import de.deepamehta.plugins.files.service.FilesService;
import java.io.File;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    
    // ----- Trial Pinning Report URIs
    
    private static final String COORDINATES_PINNED_URI = "de.akmiraketen.webexp.report_pinned_coordinates";
    private static final String REACTION_TIME_URI = "de.akmiraketen.webexp.report_pinning_rt";
    private static final String COUNT_OUTSIDE_URI = "de.akmiraketen.webexp.report_pinning_count_outside";
    
    // ----- Trial Estimation Report URIs
    
    private static final String ESTIMATION_REPORT_URI = "de.akmiraketen.webexp.trial_estimation_report";
    private static final String ESTIMATION_NR_URI = "de.akmiraketen.webexp.report_estimation_nr";
    private static final String COORDINATES_URI = "de.akmiraketen.webexp.report_estimated_coordinates";
    private static final String ESTIMATED_DISTANCE_URI = "de.akmiraketen.webexp.report_estimated_distance";
    private static final String TO_START_TIME_URI = "de.akmiraketen.webexp.report_estimated_to_start_time";
    private static final String ESTIMATION_TIME_URI = "de.akmiraketen.webexp.report_estimation_time";
    private static final String FROM_PLACE_URI = "de.akmiraketen.webexp.report_from_place_id";
    private static final String TO_PLACE_URI = "de.akmiraketen.webexp.report_to_place_id";
    private static final String ESTIMATED_CONFIDENCE = "de.akmiraketen.webexp.report_estimation_confidence";
    
    private static final String SYMBOL_FOLDER = "web-experiments/symbols";

    // ###
    
    // --- Plugin Services
    
    @Inject
    private AccessControlService acService = null;
    
    @Inject
    private FilesService fileService = null;
    
    @Override
    public void init() {
        log.info("INIT: Thanks for deploying Web Experiments " + WEB_EXPERIMENTS_VERSION);
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
        // ###
        return getStaticResource("web/new.html"); // ### to be removed completely
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
    
    
    // --- REST Resources / API Endpoints
    
    /** 
     * 
     * @return  List of FileItems in JSON
     */
    
    @GET
    @Path("/symbol/all")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FileItem> getAllSymbolFileTopics() {
        DirectoryListing items = fileService.getDirectoryListing(SYMBOL_FOLDER);
        // ArrayList<Topic> symbols = new ArrayList<Topic>(); 
        ArrayList<FileItem> symbolFiles = new ArrayList<FileItem>(); 
        // 1) Gather svg-icon files from our symbols directory
        Iterator<FileItem> files = items.getFileItems().iterator();
        while (files.hasNext()) {
            FileItem fileItem = files.next();
            if (fileItem.getItemKind() != ItemKind.FILE) files.remove();
            if (fileItem.getMediaType().equals(MediaType.APPLICATION_SVG_XML)) files.remove();
        }
        // 2) Create file topics (representing the files in our DB)
        Iterator<FileItem> icons = items.getFileItems().iterator();
        while (icons.hasNext()) {
            FileItem file = icons.next();
            // Topic fileTopic = fileService.createFileTopic(file.getPath());
            symbolFiles.add(file);
        }
        return symbolFiles;
    }
    
    /** 
     * 
     * @param fileName  String to icon file uri in filerepo
     * @return  List of FileItems in JSON
     */
    
    @GET
    @Path("/symbol/choose/{fileName}")
    @Transactional
    public Topic chooseSymbolFile(@PathParam("fileName") String fileName) {
        log.info("Setting Marker for User: " + acService.getUsername() + " fileName: " + fileName);
        Topic relatedIconTopic = null;
        Topic username = getRequestingUser();
        try {
            relatedIconTopic = getSymbolFileTopic(SYMBOL_FOLDER + "/" + fileName); // throws FREs
            log.info("Icon topic to be related is " + relatedIconTopic.getId());
            assignNewMarkerSymbolToUsername(relatedIconTopic, username);
        } catch(RuntimeException e) {
            throw new RuntimeException(e);
        }
        return relatedIconTopic.loadChildTopics(FILE_PATH_TYPE);
    }
    
    private Topic getSymbolFileTopic(String filePath) {
        Topic fileTopic = null;
        try {
            fileTopic = fileService.createFileTopic(filePath); // does a fetch if existing
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return fileTopic;
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
        return new TrialConfigViewModel(dms.getTopic(id).loadChildTopics(), dms);
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
            RelatedTopic trial = k.next();
            Association trial_seen = trial.getAssociation(TRIAL_SEEN_EDGE_TYPE, 
                ROLE_DEFAULT, ROLE_DEFAULT, user.getId());
            if (trial_seen != null) {
                log.info("Removing seen trial for user \""+user.getSimpleValue()+"\" => " + trial.getSimpleValue());
                k.remove();
            } else {
                log.info("Identified unseen trial for user \""+user.getSimpleValue()+"\" => " + trial.getSimpleValue());
            }
        }
        return trials;
    }
    
    @GET
    @Path("/trial/{trialId}/seen")
    @Transactional
    public Response doMarkTrialAsSeen(@PathParam("trialId") long trialId) {
        Topic user = getRequestingUser();
        Association trial_seen = user.getAssociation(TRIAL_SEEN_EDGE_TYPE, 
                ROLE_DEFAULT, ROLE_DEFAULT, trialId);
        if (trial_seen != null) throw new WebApplicationException(new InvalidParameterException(), Status.BAD_REQUEST);
        dms.createAssociation(new AssociationModel(TRIAL_SEEN_EDGE_TYPE, 
                new TopicRoleModel(user.getId(), "dm4.core.default"), 
                new TopicRoleModel(trialId, "dm4.core.default")));
        return Response.ok().build();
    }
    
    @GET
    @Path("/estimation/next/{trialId}")
    public int getNextTrialEstimationNr(@PathParam("trialId") long id) {
        Topic user = getRequestingUser();
        String trialConfigUri = dms.getTopic(id).getUri();
        log.info("Fetching Trial Report for Trial: " + trialConfigUri + " and VP " + user.getSimpleValue());
        ResultList<RelatedTopic> trialReports = user.getRelatedTopics("dm4.core.association", 
                "dm4.core.parent", "dm4.core.child", "de.akmiraketen.webexp.trial_report", 0);
        int count = 1; // default estimation is first
        for (RelatedTopic trialReport : trialReports) {
            String trial = trialReport.getChildTopics().getString("de.akmiraketen.webexp.report_trial_config_id");
            if (trialConfigUri.equals(trial)) {
                log.info("Re-using Trial Report for Trial: " + trialConfigUri + " and VP " + user.getSimpleValue());
                trialReport.loadChildTopics(ESTIMATION_REPORT_URI);
                if (trialReport.getChildTopics().has(ESTIMATION_REPORT_URI)) {
                    List<Topic> estimations = trialReport.getChildTopics().getTopics(ESTIMATION_REPORT_URI);
                    log.info("Trial report has " + estimations.size()+ " estimation reports associated.. ");
                    count = estimations.size() + 1; // number of next estimation for this user and this trial
                }
                return count;
            } else {
                log.info("Next Estimation Debug: " + trialConfigUri + " === " + trial);
            }
        }
        return count;
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
            log.info("ESTIMATED Coordinates for " + estimation + " are \"" + latitude + "," 
                    + longitude + "\" - by " + user.getSimpleValue() + " on " + trialConfig.getSimpleValue());
            // 2 Check consistency of this request for reporting
            Topic report = getOrCreateTrialPinningReportTopic(trialId, user);
            List<Topic> estimations = null;
            report.loadChildTopics(ESTIMATION_REPORT_URI);
            if (report.getChildTopics().has(ESTIMATION_REPORT_URI)) {
                estimations = report.getChildTopics().getTopics(ESTIMATION_REPORT_URI);
                log.info("Trial report has " + estimations.size()+ " estimation reports associated.. ");
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
                .put(COORDINATES_URI, latitude + ";" + longitude)
                .put(FROM_PLACE_URI, fromPlaceId)
                .put(TO_PLACE_URI, toPlaceId)
                .put(TO_START_TIME_URI, toStartTime)
                .put(ESTIMATION_TIME_URI, estimationTime)
                .put(ESTIMATED_DISTANCE_URI, "" + estimatedDistance) // stored as dm4.core.text
                .put(ESTIMATION_NR_URI, estimation)
                .put(ESTIMATED_CONFIDENCE, confidenceValue);
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
            log.info("POST Pinning: " + payload);
            JSONObject data = new JSONObject(payload);
            Topic trialConfig = getTrialConfigTopic(trialId);
            JSONObject coordinates = data.getJSONObject("geo_coordinates");
            int countClickOutside = data.getInt("count_click_outside");
            int reactionTime = data.getInt("reaction_time");
            String latitude = coordinates.getString("latitude");
            String longitude = coordinates.getString("longitude");
            log.info("Pinned Coordinates are \"" + latitude + "," 
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
    
    private Topic getOrCreateTrialPinningReportTopic (long trialId, Topic user) {
        DeepaMehtaTransaction tx = dms.beginTx();
        Topic report = null;
        String trialConfigUri = dms.getTopic(trialId).getUri();
        log.info("Fetching Trial Report for Trial: " + trialConfigUri + " and VP " + user.getSimpleValue());
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
        String conditionA = "webexp.config.pinning";
        String conditionB = "webexp.config.no_pinning";
        String conditionValue = conditionA;
        try {
            for (int i=10; i<=30; i++) {
                String username = "VP "+ i;
                if (isUsernameAvailable(username)) {
                    if (i > 20) conditionValue = conditionB;
                    ChildTopicsModel userAccount = new ChildTopicsModel()
                        .put(USERNAME_TYPE_URI, username)
                        .put(USER_PASSWORD_TYPE_URI, "")
                        .putRef(TRIAL_CONDITION_TYPE, conditionValue)
                        .put(TRIAL_CONDITION_BLOCKS_SIZE_TYPE, 15);
                    // ### set user account to "Blocked" until verified (introduce this in a new migration)
                    TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
                    Topic vpAccount = dms.createTopic(userModel);
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
