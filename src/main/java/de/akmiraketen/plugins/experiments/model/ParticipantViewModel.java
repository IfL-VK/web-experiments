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
    
    Topic topic = null;

    public ParticipantViewModel(Topic username) {
        this.topic = username; /* .getRelatedTopic("dm4.core.composition", "dm4.core.child",
                "dm4.core.parent", "dm4.accesscontrol.user_account").loadChildTopics(); */
    }

    public JSONObject toJSON() {
        try {
            JSONObject object = new JSONObject();
            object.put("username", this.topic.getSimpleValue());
            object.put("username_id", this.topic.getId());
            return object;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
}
