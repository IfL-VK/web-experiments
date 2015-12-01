package de.akmiraketen.plugins.experiments.model;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.DeepaMehtaService;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * The view model class every client holds for the currently loaded trial.
 *
 * @author Malte Reißig (<m_reissig@ifl-leipzig.de>), 2014-2015
 * @website https://github.com/mukil/web-experiments
 * @version 0.34SNAPSHOT
 */
public class ScreenConfigViewModel implements JSONEnabled {
    
    Logger log = Logger.getLogger(getClass().getName());
    
    Topic screenConfig = null;
    
    private final String SCREEN_TEMPLATE_NAME = "de.akmiraketen.screen_template";
    private final String SCREEN_CONDITION =     "de.akmiraketen.screen_condition";
    private final String SCREEN_OPTIONS =       "de.akmiraketen.screen_options";
    private final String SCREEN_TIMEOUT =       "de.akmiraketen.screen_timeout";

    public ScreenConfigViewModel(Topic screenConfig) {
        this.screenConfig = screenConfig.loadChildTopics();
    }

    public String getScreenTemplateName() {
        return screenConfig.getChildTopics().getString(SCREEN_TEMPLATE_NAME);
    }

    public String getScreenOptionsValue() {
        return screenConfig.getChildTopics().getString(SCREEN_OPTIONS);
    }

    public String getScreenConditionValue() {
        return screenConfig.getChildTopics().getString(SCREEN_CONDITION);
    }
    
    public String getTrialName() {
        return screenConfig.getSimpleValue().toString();
    }

    public int getScreenTimeoutValue() {
        return screenConfig.getChildTopics().getInt(SCREEN_TIMEOUT);
    }
    
    public JSONObject toJSON() {
        try {
            JSONObject response = new JSONObject();
            response.put("id", this.screenConfig.getId());
            response.put("options", getScreenOptionsValue());
            response.put("condition", getScreenConditionValue());
            response.put("timeout", getScreenTimeoutValue());
            return response;
        } catch (JSONException ex) {
            Logger.getLogger(ScreenConfigViewModel.class.getName()).log(Level.SEVERE, null, ex);
            return new JSONObject();
        }
    }
    
}
