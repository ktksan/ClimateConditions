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

import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.chat.ChatMessageEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.generation.GeneratingRegion;


@RegisterSystem(value = RegisterMode.AUTHORITY)
public class TemperatureSystem extends BaseComponentSystem {
    @In
    BiomeRegistry biomeRegistry;
    @In
    CoreRegistry coreRegistry;
    @In
    GeneratingRegion generatingRegion;
    @In
    private Time time;
    @In
    private EntityManager entityManager;
    @In
    ClimateConditionsSystem climateConditionsSystem;
    @In
    DelayManager delayManager;

    private static final int CHECK_INTERVAL = 1000;

    private static final int DISPLAY_INTERVAL = 10000;


    private float bodyTemperature;

    private float lastBodyTemperature;

    public static final String BODY_TEMPERATURE_UPDATE_ACTION_ID = "Body Temperature Update";

    public static final String BODY_TEMPERATURE_DISPLAY_ACTION_ID = "Body Temperature Display";

    @ReceiveEvent
    public void onSpawn(OnPlayerSpawnedEvent event, EntityRef player) {
        bodyTemperature = 0.4f;
        lastBodyTemperature = 0.4f;
        delayManager.addPeriodicAction(player, BODY_TEMPERATURE_UPDATE_ACTION_ID, 0, CHECK_INTERVAL);
        delayManager.addPeriodicAction(player, BODY_TEMPERATURE_DISPLAY_ACTION_ID, 0, DISPLAY_INTERVAL);

    }

    @ReceiveEvent
    public void onTemperatureUpdate(PeriodicActionTriggeredEvent event, EntityRef player, LocationComponent location) {
        if (event.getActionId().equals(BODY_TEMPERATURE_UPDATE_ACTION_ID)) {
            float envTemperature = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),
                    location.getLocalPosition().getY(), location.getLocalPosition().getZ());
            float envHumidity = climateConditionsSystem.getHumidity(location.getLocalPosition().getX(),
                    location.getLocalPosition().getY(), location.getLocalPosition().getZ());
            bodyTemperature =
                    lastBodyTemperature + ((((envTemperature - (envHumidity / 10)) - lastBodyTemperature) / 100000) * CHECK_INTERVAL);
            lastBodyTemperature = bodyTemperature;
        }
    }

    @ReceiveEvent
    public void onTemperatureDisplay(PeriodicActionTriggeredEvent event, EntityRef player, LocationComponent location) {
        if (event.getActionId().equals(BODY_TEMPERATURE_DISPLAY_ACTION_ID)) {
            float envTemperature = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),
                    location.getLocalPosition().getY(), location.getLocalPosition().getZ());
            player.getOwner().send(new ChatMessageEvent("Body Temperature: " + bodyTemperature, player.getOwner()));
            player.getOwner().send(new ChatMessageEvent("Env Temperature: " + envTemperature, player.getOwner()));
        }

    }
}
