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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AffectJumpForceEvent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.registry.In;

/**
 * Adds a {@link HypothermiaComponent} to the player.
 * Hypothermia occurs in locations with extremely cold climate and, e.g., slows the player's movements.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HypothermiaSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(HyperthermiaSystem.class);

    /**
     * Reduces the walking/running speed of the player.
     * Is only active iff the player has a {@link HypothermiaComponent}.
     */
    @ReceiveEvent
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef player, HypothermiaComponent hypothermia) {
        if (event.getMovementMode() == MovementMode.WALKING) {
            event.multiply(hypothermia.walkSpeedMultiplier);
        }
    }

    /**
     * Reduces the jump speed of the player.
     * Is only active iff the player has a {@link HypothermiaComponent}.
     */
    @ReceiveEvent
    public void modifyJumpSpeed(AffectJumpForceEvent event, EntityRef player, HypothermiaComponent hypothermia) {
        event.multiply(hypothermia.jumpSpeedMultiplier);
    }

    @ReceiveEvent
    public void hypothermiaLevelChanged(HypothermiaLevelChangedEvent event, EntityRef player,
                                        HypothermiaComponent hypothermia) {
        player.saveComponent(modifySpeedMultipliers(hypothermia, event.getNewValue()));
    }

    private HypothermiaComponent modifySpeedMultipliers(HypothermiaComponent hypothermia, int level) {
        switch (level) {
            case 1:
                hypothermia.walkSpeedMultiplier = 1;
                hypothermia.jumpSpeedMultiplier = 1;
                break;
            case 2:
                hypothermia.walkSpeedMultiplier = 0.7f;
                hypothermia.jumpSpeedMultiplier = 0.7f;
                break;
            case 3:
                hypothermia.walkSpeedMultiplier = 0.5f;
                hypothermia.jumpSpeedMultiplier = 0.6f;
                break;
            default:
                logger.warn("Unexpected Hypothermia Level.");
        }
        return hypothermia;
    }
}
