
// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions.alterationEffects;

import org.terasology.alterationEffects.AlterationEffect;
import org.terasology.alterationEffects.AlterationEffects;
import org.terasology.alterationEffects.OnEffectRemoveEvent;
import org.terasology.climateConditions.AffectBodyTemperatureEvent;
import org.terasology.context.Context;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.registry.In;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This system manages the effects of the Body Temperature Alteration Effect.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class BodyTemperatureAlterationSystem extends BaseComponentSystem {

    @In
    Context context;

    /**
     * This will store the mapping of the effect constants to the effect components.
     */
    private Map<String, Class<? extends Component>> effectComponents = new HashMap<>();

    /**
     * This will store the mapping of the effect constants to the alteration effects.
     */
    private Map<String, AlterationEffect> alterationEffects = new HashMap<>();

    @Override
    public void initialise() {
        effectComponents.put(BodyTemperatureAlterationEffect.BODY_TEMPERATURE, AffectBodyTemperatureComponent.class);
        alterationEffects.put(BodyTemperatureAlterationEffect.BODY_TEMPERATURE,
                new BodyTemperatureAlterationEffect(context));
    }

    /**
     * When the body temperature of an entity changes, modify the change in temperature based on what effects are being
     * applied to the entity.
     *
     * @param event Stores information on the body temperature change that is to be applied and collects
     *         potential modifiers.
     * @param entityRef The entity experiencing a change in body temperature.
     */
    @ReceiveEvent
    public void modifyTemperatureChange(AffectBodyTemperatureEvent event, EntityRef entityRef) {
        // If the entity's stunned, prevent it from moving.
        AffectBodyTemperatureComponent affectBodyTemperatureComponent =
                entityRef.getComponent(AffectBodyTemperatureComponent.class);
        if (affectBodyTemperatureComponent != null) {
            if (affectBodyTemperatureComponent.condition == TemperatureAlterationCondition.ALWAYS) {
                event.addPostMultiply(affectBodyTemperatureComponent.postMultiplier);
            } else {
                float deltaTemp = event.getResultValueWithoutCapping();
                if (affectBodyTemperatureComponent.condition == TemperatureAlterationCondition.ON_DECREASE && (deltaTemp < 0)) {
                    event.addPostMultiply(affectBodyTemperatureComponent.postMultiplier);
                } else if (affectBodyTemperatureComponent.condition == TemperatureAlterationCondition.ON_INCREASE && (deltaTemp > 0)) {
                    event.addPostMultiply(affectBodyTemperatureComponent.postMultiplier);
                }
            }
        }
    }

    /**
     * Once a basic effect's duration has expired, remove the effect from the entity that had it, send a removal event,
     * informing the other effect systems, and then re-apply the associated alteration effect.
     *
     * @param event Event with information of what particular effect expired.
     * @param entity The entity that had the expired effect.
     */
    @ReceiveEvent
    public void expireEffects(DelayedActionTriggeredEvent event, EntityRef entity) {
        final String actionId = event.getActionId();

        // First, make sure this expired event is actually part of the AlterationEffects module.
        if (actionId.startsWith(AlterationEffects.EXPIRE_TRIGGER_PREFIX)) {
            // Remove the expire trigger prefix and store the resultant String into effectNamePlusID.
            String effectNamePlusID = actionId.substring(AlterationEffects.EXPIRE_TRIGGER_PREFIX.length());

            // Split the effectNamePlusID into two parts. The first part will contain the AlterationEffect's name, and
            // the second part will contain the effectID.
            String[] parts = effectNamePlusID.split(Pattern.quote("|"), 2);

            // If there are two items in the parts, set the effectID accordingly.
            String effectID = "";
            if (parts.length == 2) {
                effectID = parts[1];
            }

            // Set the effectName using the first String in the parts array.
            String effectName = parts[0];

            // If this DelayedActionTriggeredEvent corresponds to one of the basic alteration effects.
            final Class<? extends Component> component = effectComponents.get(effectName);
            if (component != null) {
                // Remove the component corresponding to this particular effect.
                entity.removeComponent(component);

                // Send out an event alerting the other effect-related systems that this effect has been removed.
                entity.send(new OnEffectRemoveEvent(entity, entity, alterationEffects.get(effectName), effectID, "",
                        true));

                // Re-apply this effect so that if there are any modifiers still in effect, they'll be recalculated and
                // reapplied to the entity correctly.
                alterationEffects.get(effectName).applyEffect(entity, entity, 0, 0);
            }
        }
    }
}
