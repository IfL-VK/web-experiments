package de.akmiraketen.plugins.experiments.migrations;

import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.accesscontrol.SharingMode;
import de.deepamehta.workspaces.WorkspacesService;

/**
 * Introduces the Public "Web Experiments" Workspace (as of DeepaMehta 4.7).
 * Assigns all our topic types to the "Web Exderiments" workspace so members can edit these type definitions.
 * Home to all web experiments related Topic Types.
 * */

public class Migration3 extends Migration {

    static final String WEBEXP_WORKSPACE_NAME = "Web Experiments";
    static final String WEBEXP_WORKSPACE_URI = "de.akmiraketen.web_experiments";
    static final SharingMode WEBEXP_WORKSPACE_SHARING_MODE = SharingMode.PUBLIC;

    @Inject
    private WorkspacesService workspaceService;

    @Inject
    private AccessControlService accessControlService;

    @Override
    public void run() {
        Topic webExperiments = workspaceService.createWorkspace(WEBEXP_WORKSPACE_NAME, WEBEXP_WORKSPACE_URI,
                WEBEXP_WORKSPACE_SHARING_MODE);
        accessControlService.setWorkspaceOwner(webExperiments, "admin");
        TopicType screenConfiguration = dm4.getTopicType("de.akmiraketen.screen_configuration");
        TopicType screenReport = dm4.getTopicType("de.akmiraketen.screen_report");
        TopicType screenAction = dm4.getTopicType("de.akmiraketen.screen_action");
        workspaceService.assignTypeToWorkspace(screenConfiguration, webExperiments.getId());
        workspaceService.assignTypeToWorkspace(screenReport, webExperiments.getId());
        workspaceService.assignTypeToWorkspace(screenAction, webExperiments.getId());
    }
}
