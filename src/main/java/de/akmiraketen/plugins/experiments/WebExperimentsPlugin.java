
package de.akmiraketen.plugins.experiments;

import de.akmiraketen.plugins.experiments.model.TrialConfigViewModel;
import de.deepamehta.core.Association;
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
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.files.DirectoryListing;
import de.deepamehta.plugins.files.DirectoryListing.FileItem;
import de.deepamehta.plugins.files.ItemKind;
import de.deepamehta.plugins.files.service.FilesService;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    private static final String TRIAL_SEEN_EDGE_TYPE = "de.akmiraketen.webexp.trial_seen_edge";
    private static final String MARKER_CONFIG_EDGE_TYPE = "de.akmiraketen.webexp.config_marker_symbol";
    
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
        try {
            Topic username = getRequestingUser(); // throws RE
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
            Topic trial_condition = trial.getChildTopics().getTopic("de.akmiraketen.webexp.trial_condition");
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
    @Path("/trial/{trialId}")
    public TrialConfigViewModel getTrialConfigViewModel(@PathParam("trialId") long id) {
        return new TrialConfigViewModel(dms.getTopic(id).loadChildTopics(), dms);
    }
    
    @POST
    @Path("/estimation/{trialId}/{estimationNr}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void storeEstimationReport(String payload, @PathParam("trialId") long trialId, 
            @PathParam("estimationNr") long estimation) {
        Topic user = getRequestingUser();
        try {
            // 
            log.info("POST Estimation: " + payload);
            JSONObject data = new JSONObject(payload);
            Topic trialConfig = getTrialConfigTopic(trialId);
            // 
            String latitude = data.getString("latitude");
            String longitude = data.getString("longitude");
            log.info("ESTIMATED Coordinates for " + estimation + " are \"" + latitude + "," 
                    + longitude + "\" - by " + user.getSimpleValue() + " on " + trialConfig.getSimpleValue());
        } catch (JSONException e) {
            // ### store estimation in trial report for user
            log.warning("Failed to parse estimation data: " +  e.getClass().toString() + ", " + e.getMessage());
            throw new WebApplicationException(new RuntimeException("Parsing " + payload + " failed"), 
               500);
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
            Topic report = getOrCreateTrialPinningReportTopic(trialId);
            String coordinates_uri = "de.akmiraketen.webexp.report_pinned_coordinates";
            String rt_uri = "de.akmiraketen.webexp.report_pinning_rt";
            String count_uri = "de.akmiraketen.webexp.report_pinning_count_outside";
            ChildTopicsModel values = new ChildTopicsModel()
                .put(coordinates_uri, latitude + ";" + longitude)
                .put(rt_uri, reactionTime)
                .put(count_uri, countClickOutside);
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
    
    
    private Topic getOrCreateTrialPinningReportTopic (long trialId) {
        Topic report = null;
        String trialConfigUri = dms.getTopic(trialId).getUri();
        log.info("Processing Trial Report for Trial : " + trialConfigUri);
        Topic trialReport = dms.getTopic("de.akmiraketen.webexp.report_trial_config_id", 
                new SimpleValue(trialConfigUri));
        if (trialReport != null) {
            log.warning("Trial Report was already started ... re-sing existing one!");
            report = trialReport.getRelatedTopic("dm4.core.composition", "dm4.core.child", "dm4.core.parent", 
                    "de.akmiraketen.webexp.trial_report");
        }
        try {
            ChildTopicsModel child = new ChildTopicsModel(new JSONObject()
                    .put("de.akmiraketen.webexp.report_trial_config_id", trialConfigUri));
            TopicModel model = new TopicModel("de.akmiraketen.webexp.trial_report", child);
            report = dms.createTopic(model);
        } catch (JSONException ex) {
            Logger.getLogger(WebExperimentsPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return report;
    }
    
    // --- Helper Methods
    
    public Topic getTrialConfigTopic(@PathParam("trialId") long id) {
        return dms.getTopic(id).loadChildTopics();
    }
    
    private Topic getRequestingUser() {
        String username = acService.getUsername();
        if (username.isEmpty()) throw new RuntimeException("Not logged in.");
        return dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username));
    }
    
    private void generateSomeUsers() {
        // for 1000 do acService.createUser()
        log.info("Setting up some new users for Web Experiments");
        DeepaMehtaTransaction tx = dms.beginTx();
        try {
            for (int i=1; i<=10; i++) {
                String username = "user"+ i;
                if (isUsernameAvailable(username)) {
                    ChildTopicsModel userAccount = new ChildTopicsModel()
                        .put(USERNAME_TYPE_URI, username)
                        .put(USER_PASSWORD_TYPE_URI, "");
                    // ### set user account to "Blocked" until verified (introduce this in a new migration)
                    TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
                    Topic user = dms.createTopic(userModel);
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
        List<Association> assignments = username.getAssociations();
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
        createSymbolUserAssignment(relatedIconTopic, username);
    }
    
    private Association createSymbolUserAssignment(Topic relatedIconTopic, Topic username) {
        return dms.createAssociation(new AssociationModel(MARKER_CONFIG_EDGE_TYPE, 
            new TopicRoleModel(username.getId(), ROLE_DEFAULT), 
            new TopicRoleModel(relatedIconTopic.getId(), ROLE_DEFAULT)));
    }
    
}
