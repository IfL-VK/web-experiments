
package de.akmiraketen.plugins.experiments.migrations;

import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationDefinitionModel;
import de.deepamehta.core.model.TopicTypeModel;
import de.deepamehta.core.service.Migration;
import java.util.logging.Logger;

/** 
 * Assign user accounts a trial condition used to determine which trial 
 * condition will they be working through as the first block of trials)
 */
public class Migration4 extends Migration {
    
    private Logger logger = Logger.getLogger(getClass().getName());
    
    @Override
    public void run() {
        
        TopicType sizeOfFirstConditionBlock = dms.createTopicType(new TopicTypeModel(
                "de.akmiraketen.webexp.trial_condition_block_size", "Trial Condition Block Size", 
                "dm4.core.number"));
        TopicType userAccount = dms.getTopicType("dm4.accesscontrol.user_account");
        userAccount.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def", 
                "dm4.accesscontrol.user_account", "de.akmiraketen.webexp.trial_condition", 
                "dm4.core.one", "dm4.core.one"));
        userAccount.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def", 
                "dm4.accesscontrol.user_account", "de.akmiraketen.webexp.trial_condition_block_size", 
                "dm4.core.one", "dm4.core.one"));
    }
   
}
