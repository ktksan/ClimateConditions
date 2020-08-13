// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.characters.events.PlayerDeathEvent;
import org.terasology.logic.chat.ChatMessageEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;


@RegisterSystem(value = RegisterMode.AUTHORITY)
public class BodyTemperatureSystem extends BaseComponentSystem {
    public static final String BODY_TEMPERATURE_UPDATE_ACTION_ID = "climateConditions:bodyTemperatureUpdate";

    private static final Logger logger = LoggerFactory.getLogger(BodyTemperatureSystem.class);

    @In
    private EntityManager entityManager;
    @In
    ClimateConditionsSystem climateConditionsSystem;
    @In
    DelayManager delayManager;

    private static final int CHECK_INTERVAL = 1000;
    private float criticalLowBodyTemperatureThreshold = 0.17f;
    private float lowBodyTemperatureThreshold = 0.22f;
    private float reducedBodyTemperatureThreshold = 0.3f;
    private float raisedBodyTemperatureThreshold = 0.5f;
    private float highBodyTemperatureThreshold = 0.58f;
    private float criticalHighBodyTemperatureThreshold = 0.63f;
    //The Normal Body Temperature range is 0.3 - 0.5 as of now.

    public void postBegin() {
        boolean processedOnce = false;
        for (EntityRef entity : entityManager.getEntitiesWith(WorldComponent.class)) {
            if (!processedOnce) {
                delayManager.addPeriodicAction(entity, BODY_TEMPERATURE_UPDATE_ACTION_ID, 0, CHECK_INTERVAL);
                processedOnce = true;
            } else {
                logger.warn("More than one entity with WorldComponent found");
            }
        }
        // The world is the entity here which shall have a Body Temperature Update Periodic Action.
    }

    /**
     * Updates the Body Temperature System when the periodic action with actionId = BODY_TEMPERATURE_UPDATE_ACTION_ID is
     * triggered.
     */
    @ReceiveEvent
    public void onTemperatureUpdate(PeriodicActionTriggeredEvent event, EntityRef world) {
        if (event.getActionId().equals(BODY_TEMPERATURE_UPDATE_ACTION_ID)) {
            for (EntityRef entity : entityManager.getEntitiesWith(AliveCharacterComponent.class,
                    BodyTemperatureComponent.class, LocationComponent.class)) {
                LocationComponent location = entity.getComponent(LocationComponent.class);
                BodyTemperatureComponent bodyTemperature = entity.getComponent(BodyTemperatureComponent.class);
                float envTemperature = climateConditionsSystem.getTemperature(location.getLocalPosition());
                float envHumidity = climateConditionsSystem.getHumidity(location.getLocalPosition());
                float deltaTemp =
                        ((((envTemperature - (envHumidity / 10)) - bodyTemperature.current) / 100000) * CHECK_INTERVAL);

                //Send event for other systems to modify change in body temperature.
                AffectBodyTemperatureEvent affectBodyTemperatureEvent = new AffectBodyTemperatureEvent(deltaTemp);
                entity.send(affectBodyTemperatureEvent);
                deltaTemp = affectBodyTemperatureEvent.getResultValueWithoutCapping();

                //Check for change in body temperature levels.
                float oldValue = bodyTemperature.current;
                //Update current body temperature.
                bodyTemperature.current = bodyTemperature.current + deltaTemp;
                float newValue = bodyTemperature.current;
                entity.saveComponent(bodyTemperature);
                if (oldValue != newValue) {
                    entity.send(new BodyTemperatureValueChangedEvent(oldValue, newValue));
                }

                //only for development purposes
                if (Math.round(oldValue * 100) != Math.round(newValue * 100)) {
                    entity.getOwner().send(new ChatMessageEvent("Body Temperature: " + bodyTemperature.current,
                            entity.getOwner()));
                    entity.getOwner().send(new ChatMessageEvent("Env Temperature: " + envTemperature,
                            entity.getOwner()));
                }
            }
        }
    }

    /**
     * Reacts to {@link BodyTemperatureValueChangedEvent} and modifies the body temperature level in case it needs to be
     * changed.
     */
    @ReceiveEvent
    public void onBodyTemperatureValueChanged(BodyTemperatureValueChangedEvent event, EntityRef player,
                                              BodyTemperatureComponent bodyTemperature) {
        BodyTemperatureLevel before = checkBodyTemperatureLevel(event.getOldValue());
        BodyTemperatureLevel after = checkBodyTemperatureLevel(event.getNewValue());
        if (before != after) {
            bodyTemperature.currentLevel = after;
            player.addOrSaveComponent(bodyTemperature);
            player.send(new BodyTemperatureLevelChangedEvent(before, after));
        }
    }

    /**
     * Deals with addition and removal of Hypothermia and Hyperthermia Components.
     */
    @ReceiveEvent
    public void onBodyTemperatureLevelChanged(BodyTemperatureLevelChangedEvent event, EntityRef player) {
        int oldLevel = checkThermiaLevel(event.getOldValue());
        int newLevel = checkThermiaLevel(event.getNewValue());
        if (newLevel > 0) { // newLevel lies in the Hyperthermia range.

            if (oldLevel < 0) { // oldLevel lies in the Hypothermia range
                player.removeComponent(HypothermiaComponent.class);
                player.send(new HypothermiaLevelChangedEvent(-1 * oldLevel, 0));
            }
            player.addOrSaveComponent(new HyperthermiaComponent(newLevel));
            player.send(new HyperthermiaLevelChangedEvent(Math.max(oldLevel, 0), newLevel));

        } else if (newLevel < 0) { // newLevel lies in the Hypothermia range.

            if (oldLevel > 0) { // oldLevel lies in the Hyperthermia range.
                player.removeComponent(HyperthermiaComponent.class);
                player.send(new HyperthermiaLevelChangedEvent(oldLevel, 0));
            }
            player.addOrSaveComponent(new HypothermiaComponent(-1 * newLevel));
            player.send(new HypothermiaLevelChangedEvent(Math.max(-1 * oldLevel, 0), -1 * newLevel));

        } else { // newLevel == 0 which corresponds to normal body temperature.

            if (player.hasComponent(HyperthermiaComponent.class)) {
                player.removeComponent(HyperthermiaComponent.class);
                player.send(new HyperthermiaLevelChangedEvent(oldLevel, 0));
            }
            if (player.hasComponent(HypothermiaComponent.class)) {
                player.removeComponent(HypothermiaComponent.class);
                player.send(new HypothermiaLevelChangedEvent(-1 * oldLevel, 0));
            }

        }
    }

    /**
     * Returns the Thermia level corresponding to the BodyTemperatureLevel.
     */
    private int checkThermiaLevel(BodyTemperatureLevel level) {
        /**
         * Positive values lie in the Hyperthermia Range and the corresponding Hyperthermia level is the value returned.
         * Negative values lie in the Hypothermia Range and the corresponding Hypothermia Level is (-1 * value).
         * Zero corresponds to normal body temperature.
         */
        switch (level) {
            case CRITICAL_LOW:
                return -3;
            case LOW:
                return -2;
            case REDUCED:
                return -1;
            case RAISED:
                return 1;
            case HIGH:
                return 2;
            case CRITICAL_HIGH:
                return 3;
            default:
                return 0;                 // NORMAL
        }
    }

    /**
     * Returns the BodyTemperatureLevel corresponding to the body temperature value.
     */
    public BodyTemperatureLevel checkBodyTemperatureLevel(float temperature) {
        if (temperature <= criticalLowBodyTemperatureThreshold) {
            return BodyTemperatureLevel.CRITICAL_LOW;
        } else if (temperature <= lowBodyTemperatureThreshold) {
            return BodyTemperatureLevel.LOW;
        } else if (temperature <= reducedBodyTemperatureThreshold) {
            return BodyTemperatureLevel.REDUCED;
        } else if (temperature < raisedBodyTemperatureThreshold) {
            return BodyTemperatureLevel.NORMAL;
        } else if (temperature < highBodyTemperatureThreshold) {
            return BodyTemperatureLevel.RAISED;
        } else if (temperature < criticalHighBodyTemperatureThreshold) {
            return BodyTemperatureLevel.HIGH;
        } else {
            return BodyTemperatureLevel.CRITICAL_HIGH;
        }
    }

    /**
     * Resets body temperature when the player dies.
     */
    @ReceiveEvent
    public void temperatureReset(PlayerDeathEvent event, EntityRef player, BodyTemperatureComponent bodyTemperature) {
        float oldTemperature = bodyTemperature.current;
        bodyTemperature.current = player.getParentPrefab().getComponent(BodyTemperatureComponent.class).current;
        player.saveComponent(bodyTemperature);
        player.send(new BodyTemperatureValueChangedEvent(oldTemperature, bodyTemperature.current));
    }
}
