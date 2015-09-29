package de.akmiraketen.plugins.experiments.model;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.DeepaMehtaService;
import java.util.HashSet;
import java.util.Set;
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
 * @version 0.3-SNAPSHOT
 */
public class TrialConfigViewModel implements JSONEnabled {
    
    Logger log = Logger.getLogger(getClass().getName());
    
    Topic trialConfig = null;
    DeepaMehtaService dms = null;
    
    private final String TRIAL_MAP_ID = "de.akmiraketen.webexp.trial_map_id";
    private final String TRIAL_CONDITION = "de.akmiraketen.webexp.trial_condition";
    private final String TRIAL_MEMO_SECONDS = "de.akmiraketen.webexp.trial_memo_sec";
    private final String TRIAL_PLACE_TO_PIN = "de.akmiraketen.webexp.trial_place_to_pin";
    
    private final String TRIAL_TO_PLACE_ONE = "de.akmiraketen.webexp.trial_place_to_1";
    private final String TRIAL_FROM_PLACE_ONE = "de.akmiraketen.webexp.trial_place_from_1";
    private final String TRIAL_TO_PLACE_TWO = "de.akmiraketen.webexp.trial_place_to_2";
    private final String TRIAL_FROM_PLACE_TWO = "de.akmiraketen.webexp.trial_place_from_2";
    private final String TRIAL_TO_PLACE_THREE = "de.akmiraketen.webexp.trial_place_to_3";
    private final String TRIAL_FROM_PLACE_THREE = "de.akmiraketen.webexp.trial_place_from_3";
    private final String TRIAL_TO_PLACE_FOUR = "de.akmiraketen.webexp.trial_place_to_4";
    private final String TRIAL_FROM_PLACE_FOUR = "de.akmiraketen.webexp.trial_place_from_4";
    private final String TRIAL_TO_PLACE_FIVE = "de.akmiraketen.webexp.trial_place_to_5";
    private final String TRIAL_FROM_PLACE_FIVE = "de.akmiraketen.webexp.trial_place_from_5";
    
    private final String TRIAL_MAP_CONFIG_ID = "de.akmiraketen.webexp.mapfile_config";
    
    // private final String PLACE_CONFIG = "de.akmiraketen.webexp.place_config";
    // private final String PLACE_ID = "de.akmiraketen.webexp.place_id";
    
    public TrialConfigViewModel (Topic trialConfig, DeepaMehtaService dms) {
        this.dms = dms;
        this.trialConfig = trialConfig;
    }
    
    /** The following topic connects every trial with its map configuration and a map with all its places*/
    
    public Topic getTrialMapIdTopic() {
        return trialConfig.getChildTopics().getTopic(TRIAL_MAP_ID);
    }

    public JSONObject getRelatedPlaceConfigs() throws JSONException {
        // collect all five place ids manually
        Set<String> placeIds = new HashSet<String>();
        if (!placeIds.contains(getFromPlaceOneId())) placeIds.add(getFromPlaceOneId());
        if (!placeIds.contains(getToPlaceOneId())) placeIds.add(getToPlaceOneId());
        if (!placeIds.contains(getFromPlaceTwoId())) placeIds.add(getFromPlaceTwoId());
        if (!placeIds.contains(getToPlaceTwoId())) placeIds.add(getToPlaceTwoId());
        if (!placeIds.contains(getFromPlaceThreeId())) placeIds.add(getFromPlaceThreeId());
        if (!placeIds.contains(getToPlaceThreeId())) placeIds.add(getToPlaceThreeId());
        if (!placeIds.contains(getFromPlaceFourId())) placeIds.add(getFromPlaceFourId());
        if (!placeIds.contains(getToPlaceFourId())) placeIds.add(getToPlaceFourId());
        if (!placeIds.contains(getFromPlaceFiveId())) placeIds.add(getFromPlaceFiveId());
        if (!placeIds.contains(getToPlaceFiveId())) placeIds.add(getToPlaceFiveId());
        JSONObject places = new JSONObject();
        JSONArray placeItems = new JSONArray();
        for (String placeId : placeIds) {
            Topic placeConfig = getPlaceConfigByPlaceId(placeId);
            if (placeConfig != null) {
                placeItems.put(placeConfig.toJSON());
            } else {
                log.warning("No place config loaded");
            }
        }
        places.put("items", placeItems);
        return places;
    }
    
    public JSONObject getRelatedMapFileConfig() throws JSONException {
        Topic mapFileTopicId = getTrialMapIdTopic();
        log.info("Loading Map File Config Id " + mapFileTopicId.getSimpleValue() + " "
                + "for Trial Config \"" + trialConfig.getUri().toString() + "\"");
        Topic mapFileConfig = mapFileTopicId.getRelatedTopic("dm4.core.aggregation", 
                "dm4.core.child", "dm4.core.parent", TRIAL_MAP_CONFIG_ID); // load map config for this map-id
        mapFileConfig.loadChildTopics();
        return mapFileConfig.toJSON();
    }
    
    public String getPlaceIdToPin() {
        return trialConfig.getChildTopics().getString(TRIAL_PLACE_TO_PIN);
    }
    
    public Topic getPlaceConfigByPlaceId(String placeId) {
        Topic placeIdTopic = dms.getTopic("de.akmiraketen.webexp.place_id", new SimpleValue(placeId));
        if (placeIdTopic != null) {
            Topic placeConfig = placeIdTopic.getRelatedTopic("dm4.core.composition", null, null, "de.akmiraketen.webexp.place_config");
            if (placeConfig != null) {
                return placeConfig.loadChildTopics();
            } else {
                log.warning("TrialConfig is missing a valid \"Place ID\" "
                    + "- system could not load a place configured with id: " + placeId);
            }
        }
        return null;
    }

    // --- Places involved in the trial
    
    public String getFromPlaceOneId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_FROM_PLACE_ONE);
    }
    
    public String getToPlaceOneId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_TO_PLACE_ONE);
    }
    
    public String getFromPlaceTwoId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_FROM_PLACE_TWO);
    }
    
    public String getToPlaceTwoId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_TO_PLACE_TWO);
    }
    
    public String getFromPlaceThreeId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_FROM_PLACE_THREE);
    }
    
    public String getToPlaceThreeId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_TO_PLACE_THREE);
    }
    
    public String getFromPlaceFourId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_FROM_PLACE_FOUR);
    }
    
    public String getToPlaceFourId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_TO_PLACE_FOUR);
    }
    
    public String getFromPlaceFiveId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_FROM_PLACE_FIVE);
    }
    
    public String getToPlaceFiveId () throws JSONException {
        return trialConfig.getChildTopics().getString(TRIAL_TO_PLACE_FIVE);
    }
    
    // --- General trial atttributes
    
    public String getTrialConditionURI() {
        return trialConfig.getChildTopics().getTopic(TRIAL_CONDITION).getUri();
    }
    
    public String getTrialName() {
        return trialConfig.getSimpleValue().toString();
    }
    
    public String getTrialMemorizationTime() {
        return trialConfig.getChildTopics().getString(TRIAL_MEMO_SECONDS);
    }
    
    public JSONObject toJSON() {
        try {
            JSONObject response = new JSONObject();
            response.put("map_config", getRelatedMapFileConfig());
            response.put("place_config", getRelatedPlaceConfigs());
            response.put("trial_config", new JSONObject()
                .put("memo_seconds", getTrialMemorizationTime())
                .put("trial_name", getTrialName())
                .put("trial_condition", getTrialConditionURI())
                .put("place_to_pin", getPlaceIdToPin()));
            response.put("from_place1", getFromPlaceOneId());
            response.put("to_place1", getToPlaceOneId());
            response.put("from_place2", getFromPlaceTwoId());
            response.put("to_place2", getToPlaceTwoId());
            response.put("from_place3", getFromPlaceThreeId());
            response.put("to_place3", getToPlaceThreeId());
            response.put("from_place4", getFromPlaceFourId());
            response.put("to_place4", getToPlaceFourId());
            response.put("from_place5", getFromPlaceFiveId());
            response.put("to_place5", getToPlaceFiveId());
            return response;
        } catch (JSONException ex) {
            Logger.getLogger(TrialConfigViewModel.class.getName()).log(Level.SEVERE, null, ex);
            return new JSONObject();
        }
    }
    
}
