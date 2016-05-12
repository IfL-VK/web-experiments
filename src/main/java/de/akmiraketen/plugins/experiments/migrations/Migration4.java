package de.akmiraketen.plugins.experiments.migrations;

import de.akmiraketen.plugins.experiments.WebExperimentsPlugin;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.accesscontrol.Credentials;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.files.FilesService;
import de.deepamehta.workspaces.WorkspacesService;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
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
        log.info(" ### Generating "+NR_OF_USERS+" user account for your web-experiment " + WebExperimentsPlugin.WEB_EXPERIMENTS_VERSION);
        generateNumberOfUsers();
        log.info(" ### Creating the \"/web-experiments/templates\" folder in your filerepo for screen templates " +
                "web-experiments " + WebExperimentsPlugin.WEB_EXPERIMENTS_VERSION);
        String parentFolderName = "web-experiments";
        createFolderWithName(parentFolderName, null);
        createFolderWithName("symbols", parentFolderName);
        createFolderWithName("templates", parentFolderName);
    }

    private void generateNumberOfUsers() {
        // for 1000 do acService.createUser()
        log.info("### Setting up new users for Web Experiments");
        DeepaMehtaTransaction tx = dm4.beginTx();
        try {
            for (int i=1; i<=NR_OF_USERS; i++) {
                String username = "VP "+ i;
                if (isUsernameAvailable(username)) {
                    Credentials cred = new Credentials(username, "");
                    acService.createUserAccount(cred);
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
        DeepaMehtaTransaction tx = dm4.beginTx();
        String parent = "/";
        if (parentFolderName != null) parent = parentFolderName;
        // ### 1. make use of FilesPlugin.FILE_REPOSITORY_PER_WORKSPACE
        // ### 2. make use of filesService.fileExists("/path")
        try {
            File item = fileService.getFile(parent + "/" + folderName); // throws RuntimeException if no result
            if (!item.isDirectory()) { // folder does not exist
                fileService.createFolder(folderName, parent);
            } else  {
                log.info("OK - Folder already exists");
            }
            tx.success();
        } catch (RuntimeException fe) { // file or folder does not exist
            log.warning("Cause: " + fe.getCause().toString());
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
                tx.success();
            }
        } finally {
            tx.finish();
        }
    }

    private boolean isUsernameAvailable(String username) {
        Topic userName = dm4.getTopicByValue(USERNAME_TYPE_URI, new SimpleValue(username));
        return (userName == null);
    }

}
