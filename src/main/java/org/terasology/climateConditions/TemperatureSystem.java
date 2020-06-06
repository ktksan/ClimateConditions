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

import org.terasology.alterationEffects.damageOverTime.DamageOverTimeComponent;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.chat.ChatMessageEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.generation.GeneratingRegion;


@RegisterSystem(value = RegisterMode.AUTHORITY)
public class TemperatureSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
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

    /**
     * Integer storing when to check each effect.
     */
    private static final int CHECK_INTERVAL = 10000;

    /**
     * Integer storing when to apply DOT damage
     */
    private static final int DAMAGE_TICK = 10000;

    /**
     * Last time the list of DOT effects were checked.
     */
    private long lastUpdated;

    @Override
    public void update(float delta) {
        final long currentTime = time.getGameTimeInMs();

        // If the current time passes the CHECK_INTERVAL threshold, continue.
        if (currentTime >= lastUpdated + CHECK_INTERVAL) {
            // Set the lastUpdated time to be the currentTime.
            lastUpdated = currentTime;

            // For every entity with the health and DOT components, check to see if they have passed a DAMAGE_TICK. If
            // so, apply a heal event to the applicable entities with the given damageAmount.
            for (EntityRef entity : entityManager.getEntitiesWith(PlayerCharacterComponent.class)) {
                final DamageOverTimeComponent component = entity.getComponent(DamageOverTimeComponent.class);
                final LocationComponent location = entity.getComponent(LocationComponent.class);
                float temp = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),
                        location.getLocalPosition().getY(), location.getLocalPosition().getZ());
                entity.getOwner().send(new ChatMessageEvent("temp: " + temp, entity.getOwner()));

            }
        }
    }
/*
    @ReceiveEvent(components = {PlayerCharacterComponent.class, CharacterMovementComponent.class})
    public void changeBiome(OnBiomeChangedEvent event, EntityRef player, LocationComponent location) {
        float temp = climateConditionsSystem.getTemperature(location.getLocalPosition().getX(),location
        .getLocalPosition().getY(),location.getLocalPosition().getZ());
        player.getOwner().send(new ChatMessageEvent("temp: " + temp, player.getOwner()));

    }


 */

}
