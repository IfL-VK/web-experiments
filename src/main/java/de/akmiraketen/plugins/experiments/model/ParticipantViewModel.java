package de.akmiraketen.plugins.experiments.model;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.DeepaMehtaService;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * The view model class every client holds for the currently authenticated participant.
 *
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>), 2014-2015
 * @website https://github.com/mukil/web-experiments
 * @version 0.4-SNAPSHOT
 */
public class ParticipantViewModel implements JSONEnabled {
    
    private static final String MARKER_CONFIG_EDGE_TYPE = "de.akmiraketen.webexp.config_marker_symbol";
    private static final String TRIAL_CONDITION_TYPE = "de.akmiraketen.webexp.trial_condition";
    private static final String TRIAL_CONDITION_BLOCKS_SIZE_TYPE = "de.akmiraketen.webexp.trial_condition_block_size";
    private static final int DEFAULT_TRIAL_CONDITION_BLOCK_SIZE = 15;
    
    Topic topic = null;
    DeepaMehtaService dms = null;
    
    public ParticipantViewModel(Topic username, DeepaMehtaService dms) {
        this.topic = username.getRelatedTopic("dm4.core.composition", "dm4.core.child", 
                "dm4.core.parent", "dm4.accesscontrol.user_account").loadChildTopics();
        this.dms = dms;
    }
    
    public String getSelectedMarkerPath() {
        String path = "leaflet-marker-icon.png";
        Topic relatedIconTopic = topic.getRelatedTopic(MARKER_CONFIG_EDGE_TYPE, "dm4.core.default", 
                "dm4.core.default", "dm4.files.file");
        if (relatedIconTopic != null) {
            relatedIconTopic.loadChildTopics("dm4.files.path");
            path = relatedIconTopic.getChildTopics().getString("dm4.files.path");
        }
        return path;
    }
    
    public long getSelectedMarkerId() {
        long id = -1;
        Topic relatedIconTopic = topic.getRelatedTopic(MARKER_CONFIG_EDGE_TYPE, "dm4.core.default", 
                "dm4.core.default", "dm4.files.file");
        if (relatedIconTopic != null) {
            id = relatedIconTopic.getId();
        }
        return id;
    }
    
    public String getFirstTrialConditionURI() {
        String condition = "webexp.config.pinning";  // case for logged in user "admin"
        if (topic.getChildTopics().has(TRIAL_CONDITION_TYPE)) { 
            condition = topic.getChildTopics().getTopic(TRIAL_CONDITION_TYPE).getUri();
        }
        return condition;
    }
    
    public int getSizeOfFirstConditionBlock() {
        int value = -1;
        if (!topic.getChildTopics().has(TRIAL_CONDITION_BLOCKS_SIZE_TYPE)) {
            return DEFAULT_TRIAL_CONDITION_BLOCK_SIZE; // case for logged in user "admin"
        }
        value = topic.getChildTopics().getInt(TRIAL_CONDITION_BLOCKS_SIZE_TYPE);
        return value;
    }

    public JSONObject toJSON() {
        try {
            JSONObject object = new JSONObject();
            object.put("selected_marker_id", getSelectedMarkerId());
            object.put("selected_marker_path", getSelectedMarkerPath());
            object.put("first_trial_condition", getFirstTrialConditionURI());
            object.put("blocksize_first_condition", getSizeOfFirstConditionBlock());
            return object;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
}
