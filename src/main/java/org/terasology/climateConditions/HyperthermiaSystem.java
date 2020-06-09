/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.climateConditions;

import org.terasology.biomesAPI.Biome;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AffectJumpForceEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.thirst.event.AffectThirstEvent;

import java.util.Optional;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HyperthermiaSystem extends BaseComponentSystem {
    private float walkSpeedMultiplier = 0.7f;
    private float jumpSpeedMultiplier = 0.85f;
    private final Name DesertId = new Name("CoreWorlds:Desert");

    @In
    BiomeRegistry biomeRegistry;

    @ReceiveEvent(components = {PlayerCharacterComponent.class, CharacterMovementComponent.class})
    public void onBiomeChange(OnBiomeChangedEvent event, EntityRef player) {
        if (event.getNewBiome().getId().equals(DesertId)) {
            player.addOrSaveComponent(new HyperthermiaComponent());
        } else {
            if (player.hasComponent(HyperthermiaComponent.class)) {
                player.removeComponent(HyperthermiaComponent.class);
            }
        }
    }

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef player) {
        event.multiply(walkSpeedMultiplier);
    }

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void modifyJumpSpeed(AffectJumpForceEvent event, EntityRef player) {
        event.multiply(jumpSpeedMultiplier);
    }

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void modifyThirst(AffectThirstEvent event, EntityRef player) {
        event.multiply(2f);
    }

    @ReceiveEvent
    public void onSpawn(OnPlayerSpawnedEvent event, EntityRef player, LocationComponent location) {
        final Optional<Biome> biome = biomeRegistry.getBiome(new Vector3i(location.getLocalPosition()));
        if (biome.get().getId().equals(DesertId)) {
            player.addOrSaveComponent(new HyperthermiaComponent());
        }
    }

}
