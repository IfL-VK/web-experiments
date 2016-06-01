package de.akmiraketen.plugins.experiments.migrations;

import de.akmiraketen.plugins.experiments.WebExperimentsPlugin;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.accesscontrol.Credentials;
import de.deepamehta.files.FilesPlugin;
import de.deepamehta.files.FilesService;
import de.deepamehta.files.ItemKind;
import de.deepamehta.files.ResourceInfo;
import de.deepamehta.workspaces.WorkspacesService;
import java.io.File;
import java.util.logging.Logger;

/**
 * Creates an initial set of user accounts as needed.
 * */

public class Migration4 extends Migration {
    
    private Logger log = Logger.getLogger(getClass().getName());

    private static final String USERNAME_TYPE_URI           = "dm4.accesscontrol.username";
    
    // --- Installation Default Settings

    public static final int NR_OF_USERS = 300;
    public static final String TEMPLATE_FOLDER = "web-experiments/templates";
    public static final String SYMBOL_FOLDER = "web-experiments/symbols";
    
    // --- Consumed Plugin Services
    
    @Inject private AccessControlService acService = null;
    @Inject private WorkspacesService workspaceService = null;
    @Inject private FilesService fileService = null;
    
    @Override
    public void run() {
        log.info("#### Generating " + NR_OF_USERS + " user account for your web-experiment " + WebExperimentsPlugin.WEB_EXPERIMENTS_VERSION);
        generateNumberOfUsers();
        String rootRepoPath = "/";
        String parentFolderName = "web-experiments";
        if (FilesPlugin.FILE_REPOSITORY_PER_WORKSPACE) {
            Topic deepaMehtaWs = workspaceService.getWorkspace(WebExperimentsPlugin.WEBEXP_WORKSPACE_URI);
            rootRepoPath = fileService.pathPrefix(deepaMehtaWs.getId());
            log.info("#### Creating the \"/web-experiments/templates\" folder for screen templates in the \"Web Experiments\" workspace");
        }
        createFolderWithName(parentFolderName, rootRepoPath);
        createFolderWithName("symbols", rootRepoPath + parentFolderName);
        createFolderWithName("templates", rootRepoPath + parentFolderName);
    }

    private void generateNumberOfUsers() {
        // for 1000 do acService.createUser()
        log.info("### Setting up new users for Web Experiments");
        Topic deepaMehtaWs = workspaceService.getWorkspace(WebExperimentsPlugin.WEBEXP_WORKSPACE_URI);
        for (int i=1; i<=NR_OF_USERS; i++) {
            String username = "VP "+ i;
            if (isUsernameAvailable(username)) {
                Credentials cred = new Credentials(username, "");
                acService.createUserAccount(cred);
                acService.createMembership(username, deepaMehtaWs.getId());
            } else {
                log.info("DEBUG: Username is already taken ..");
            }
        }
    }

    /** Caller must make sure that folderPath has set the right prefix(). */
    private void createFolderWithName(String folderName, String folderPath) {
        String parentPath = "/";
        if (folderPath != null) parentPath = folderPath;
        String fullFolderPath = parentPath + File.separator + folderName;
        try {
            // check for full folder path
            if (fileService.fileExists(fullFolderPath)) {
                log.info("Web Experiments Folder exists in Repo at \"" + parentPath + "\" - Doing nothing.");
                ResourceInfo resourceInfo = fileService.getResourceInfo(fullFolderPath);
                if (resourceInfo.getItemKind() != ItemKind.DIRECTORY) {
                    String message = "Web Experiments Plugin: \""+folderName+"\" storage directory in repo path " + parentPath + " can not be used";
                    throw new IllegalStateException(message);
                }
            } else {
                if (!fileService.fileExists(parentPath)) {
                    log.warning("Parent folder in Filerepo" + parentPath + " - DOES NOT EXIST");
                    fileService.createFolder(parentPath, "/"); // Creating WS Folder
                }
                log.info("Creating the \"" + folderName + "\" subfolder for filerepo at " + parentPath + "!");
                fileService.createFolder(folderName, parentPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isUsernameAvailable(String username) {
        Topic userName = dm4.getTopicByValue(USERNAME_TYPE_URI, new SimpleValue(username));
        return (userName == null);
    }

}
