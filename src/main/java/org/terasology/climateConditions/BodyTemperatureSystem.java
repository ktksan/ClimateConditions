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
                float envTemperature = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),
                        location.getLocalPosition().getY(), location.getLocalPosition().getZ());
                float envHumidity = climateConditionsSystem.getHumidity(location.getLocalPosition().getX(),
                        location.getLocalPosition().getY(), location.getLocalPosition().getZ());
                float deltaTemp =
                        ((((envTemperature - (envHumidity / 10)) - bodyTemperature.current) / 100000) * CHECK_INTERVAL);
                /*
                //Send event for other systems to modify change in body temperature.
                AffectBodyTemperatureEvent affectBodyTemperatureEvent = new AffectBodyTemperatureEvent(deltaTemp);
                entity.send(affectBodyTemperatureEvent);
                deltaTemp = affectBodyTemperatureEvent.getResultValue();
                 */
                //Check for change in body temperature levels.
                BodyTemperatureLevel before = lookupLevel(bodyTemperature.current);
                //Update current body temperature.
                bodyTemperature.current = bodyTemperature.current + deltaTemp;
                entity.saveComponent(bodyTemperature);
                BodyTemperatureLevel after = lookupLevel(bodyTemperature.current);

                if (before != after) {
                    entity.send(new BodyTemperatureChangedEvent(before, after));
                    bodyTemperature.currentLevel = after;
                }
                //only for development purposes
                entity.getOwner().send(new ChatMessageEvent("Body Temperature: " + bodyTemperature.current,
                        entity.getOwner()));
                entity.getOwner().send(new ChatMessageEvent("Env Temperature: " + envTemperature, entity.getOwner()));
            }
        }
    }

    @ReceiveEvent
    public void onTemperatureChangedToHigh(BodyTemperatureChangedEvent event, EntityRef player) {
        if (event.getNewBodyTemperatureLevel() == BodyTemperatureLevel.HIGH) {
            player.addOrSaveComponent(new HyperthermiaComponent());
        }
    }

    @ReceiveEvent
    public void onTemperatureChangedToLow(BodyTemperatureChangedEvent event, EntityRef player) {
        if (event.getNewBodyTemperatureLevel() == BodyTemperatureLevel.LOW) {
            player.addOrSaveComponent(new HypothermiaComponent());
        }
    }

    @ReceiveEvent
    public void onTemperatureChangedToNormal(BodyTemperatureChangedEvent event, EntityRef player) {
        if (event.getNewBodyTemperatureLevel() == BodyTemperatureLevel.NORMAL) {
            if (player.hasComponent(HyperthermiaComponent.class)) {
                player.removeComponent(HyperthermiaComponent.class);
            }
            if (player.hasComponent(HypothermiaComponent.class)) {
                player.removeComponent(HypothermiaComponent.class);
            }
        }
    }

    public BodyTemperatureLevel lookupLevel(float temperature) {
        if (temperature <= 0.3) {
            return BodyTemperatureLevel.LOW;
        } else if (temperature <= 0.6) {
            return BodyTemperatureLevel.NORMAL;
        } else
        {
            return BodyTemperatureLevel.HIGH;
        }
    }
}
