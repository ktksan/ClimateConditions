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
    private float lowBodyTemperatureThreshold = 0.2f;
    private float criticalLowBodyTemperatureThreshold = 0.1f;
    private float highBodyTemperatureThreshold = 0.6f;
    private float criticalHighBodyTemperatureThreshold = 0.7f;


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
                deltaTemp = affectBodyTemperatureEvent.getResultValue();
                if (affectBodyTemperatureEvent.isNegative()) {
                    deltaTemp *= -1;
                }

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
                entity.getOwner().send(new ChatMessageEvent("Body Temperature: " + bodyTemperature.current,
                        entity.getOwner()));
                entity.getOwner().send(new ChatMessageEvent("Env Temperature: " + envTemperature, entity.getOwner()));
            }
        }
    }

    @ReceiveEvent
    public void onBodyTemperatureValueChanged(BodyTemperatureValueChangedEvent event, EntityRef player) {
        BodyTemperatureLevel before = checkLevel(event.getOldBodyTemperatureValue());
        BodyTemperatureLevel after = checkLevel(event.getNewBodyTemperatureValue());
        if (before != after) {
            player.send(new BodyTemperatureLevelChangedEvent(before, after));
        }
    }

    @ReceiveEvent
    public void onBodyTemperatureLevelChanged(BodyTemperatureLevelChangedEvent event, EntityRef player) {
        switch (event.getNewBodyTemperatureLevel()) {
            case LOW:
                player.addOrSaveComponent(new HypothermiaComponent());
                break;
            case CRITICAL_LOW:
                player.addOrSaveComponent(new HypothermiaComponent(2));
                break;
            case HIGH:
                player.addOrSaveComponent(new HyperthermiaComponent());
                break;
            case CRITICAL_HIGH:
                player.addOrSaveComponent(new HyperthermiaComponent(2));
                break;
            case NORMAL:
                if (player.hasComponent(HyperthermiaComponent.class)) {
                    player.removeComponent(HyperthermiaComponent.class);
                } else if (player.hasComponent(HypothermiaComponent.class)) {
                    player.removeComponent(HypothermiaComponent.class);
                }
        }
    }

    public BodyTemperatureLevel checkLevel(float temperature) {
        if (temperature <= criticalLowBodyTemperatureThreshold) {
            return BodyTemperatureLevel.CRITICAL_LOW;
        } else if (temperature <= lowBodyTemperatureThreshold) {
            return BodyTemperatureLevel.LOW;
        } else if (temperature < highBodyTemperatureThreshold) {
            return BodyTemperatureLevel.NORMAL;
        } else if (temperature < criticalHighBodyTemperatureThreshold){
            return BodyTemperatureLevel.HIGH;
        } else {
            return BodyTemperatureLevel.CRITICAL_HIGH;
        }
    }
}
