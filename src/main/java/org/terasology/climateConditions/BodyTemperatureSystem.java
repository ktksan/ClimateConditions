/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;


@RegisterSystem(value = RegisterMode.AUTHORITY)
public class BodyTemperatureSystem extends BaseComponentSystem {
    public static final String BODY_TEMPERATURE_UPDATE_ACTION_ID = "Body Temperature Update";
    public static final String BODY_TEMPERATURE_DISPLAY_ACTION_ID = "Body Temperature Display";

    private static final Logger logger = LoggerFactory.getLogger(BodyTemperatureSystem.class);

    @In
    private EntityManager entityManager;
    @In
    ClimateConditionsSystem climateConditionsSystem;
    @In
    DelayManager delayManager;

    private static final int CHECK_INTERVAL = 1000;
    private static final int DISPLAY_INTERVAL = 10000;

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
            for (EntityRef entity : entityManager.getEntitiesWith(AliveCharacterComponent.class)) {
                LocationComponent location = entity.getComponent(LocationComponent.class);
                if (!entity.hasComponent(BodyTemperatureComponent.class)) {
                    BodyTemperatureComponent btc = new BodyTemperatureComponent();
                    entity.addComponent(btc);
                }
                BodyTemperatureComponent btc = entity.getComponent(BodyTemperatureComponent.class);
                float envTemperature = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),
                        location.getLocalPosition().getY(), location.getLocalPosition().getZ());
                float envHumidity = climateConditionsSystem.getHumidity(location.getLocalPosition().getX(),
                        location.getLocalPosition().getY(), location.getLocalPosition().getZ());
                btc.bodyTemperature =
                        btc.lastBodyTemperature + ((((envTemperature - (envHumidity / 10)) - btc.lastBodyTemperature) / 100000) * CHECK_INTERVAL);
                btc.lastBodyTemperature = btc.bodyTemperature;
                entity.saveComponent(btc);
            }
        }
    }

    @ReceiveEvent
    public void onTemperatureDisplay(PeriodicActionTriggeredEvent event, EntityRef player, LocationComponent location
            , BodyTemperatureComponent btc) {
        // temporary: for help in development
        if (event.getActionId().equals(BODY_TEMPERATURE_DISPLAY_ACTION_ID)) {
            float envTemperature = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),
                    location.getLocalPosition().getY(), location.getLocalPosition().getZ());
            player.getOwner().send(new ChatMessageEvent("Body Temperature: " + btc.bodyTemperature, player.getOwner()));
            player.getOwner().send(new ChatMessageEvent("Env Temperature: " + envTemperature, player.getOwner()));
        }

    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player) {
        delayManager.addPeriodicAction(player, BODY_TEMPERATURE_DISPLAY_ACTION_ID, 0, DISPLAY_INTERVAL);
        // temporary: for help in development
    }
}
