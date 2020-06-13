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
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;


@RegisterSystem(value = RegisterMode.AUTHORITY)
public class BodyTemperatureSystem extends BaseComponentSystem {
    public static final String BODY_TEMPERATURE_UPDATE_ACTION_ID = "Body Temperature Update";

    private static final Logger logger = LoggerFactory.getLogger(BodyTemperatureSystem.class);

    @In
    private EntityManager entityManager;
    @In
    ClimateConditionsSystem climateConditionsSystem;
    @In
    DelayManager delayManager;

    private static final int CHECK_INTERVAL = 1000;
    private float deltaTemp;

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
                BodyTemperatureComponent btc = entity.getComponent(BodyTemperatureComponent.class);
                float envTemperature = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),
                        location.getLocalPosition().getY(), location.getLocalPosition().getZ());
                float envHumidity = climateConditionsSystem.getHumidity(location.getLocalPosition().getX(),
                        location.getLocalPosition().getY(), location.getLocalPosition().getZ());
                deltaTemp = ((((envTemperature - (envHumidity / 10)) - btc.bodyTemperature) / 100000) * CHECK_INTERVAL);
                //Send event for other systems to modify change in body temperature.
                AffectBodyTemperatureEvent affectBodyTemperatureEvent = new AffectBodyTemperatureEvent(deltaTemp);
                entity.send(affectBodyTemperatureEvent);
                deltaTemp = affectBodyTemperatureEvent.getResultValue();
                btc.bodyTemperature = btc.bodyTemperature + deltaTemp;
                //Check for change in body temperature levels.
                if ((btc.bodyTemperature < 0.3) && (btc.bodyTemperature - deltaTemp > 0.3)) {
                    entity.send(new BodyTemperatureChangedEvent(BodyTemperatureChangedEvent.BodyTemperatureLevel.LOW));
                }
                if ((btc.bodyTemperature > 0.3) && (btc.bodyTemperature - deltaTemp < 0.3)) {
                    entity.send(new BodyTemperatureChangedEvent(BodyTemperatureChangedEvent.BodyTemperatureLevel.NORMAL));
                }
                if ((btc.bodyTemperature > 0.6) && (btc.bodyTemperature - deltaTemp < 0.6)) {
                    entity.send(new BodyTemperatureChangedEvent(BodyTemperatureChangedEvent.BodyTemperatureLevel.LOW));
                }
                if ((btc.bodyTemperature < 0.6) && (btc.bodyTemperature - deltaTemp > 0.6)) {
                    entity.send(new BodyTemperatureChangedEvent(BodyTemperatureChangedEvent.BodyTemperatureLevel.NORMAL));
                }
                entity.getOwner().send(new ChatMessageEvent("Body Temperature: " + btc.bodyTemperature,
                        entity.getOwner()));
                entity.getOwner().send(new ChatMessageEvent("Env Temperature: " + envTemperature, entity.getOwner()));
                entity.saveComponent(btc);
            }
        }
    }

    @ReceiveEvent
    public void onTemperatureChangedToHigh(BodyTemperatureChangedEvent event, EntityRef player) {
        if(event.getBodyTemperatureLevel() == BodyTemperatureChangedEvent.BodyTemperatureLevel.HIGH) {
            player.addOrSaveComponent(new HyperthermiaComponent());
        }
    }

    @ReceiveEvent
    public void onTemperatureChangedToLow(BodyTemperatureChangedEvent event, EntityRef player) {
        if(event.getBodyTemperatureLevel() == BodyTemperatureChangedEvent.BodyTemperatureLevel.LOW) {
            player.addOrSaveComponent(new HypothermiaComponent());
        }
    }

    @ReceiveEvent
    public void onTemperatureChangedToNormal(BodyTemperatureChangedEvent event, EntityRef player) {
        if(event.getBodyTemperatureLevel() == BodyTemperatureChangedEvent.BodyTemperatureLevel.NORMAL) {
            if(player.hasComponent(HyperthermiaComponent.class)) {
                player.removeComponent(HyperthermiaComponent.class);
            }
            if(player.hasComponent(HypothermiaComponent.class)) {
                player.removeComponent(HypothermiaComponent.class);
            }
        }
    }
}
