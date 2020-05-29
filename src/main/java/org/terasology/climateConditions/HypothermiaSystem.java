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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AffectJumpForceEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.physics.events.MovedEvent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HypothermiaSystem extends BaseComponentSystem {
    private float thresholdHeight = 60f;
    private float walkSpeedMultiplier = 0.6f;
    private float jumpSpeedMultiplier = 0.7f;

    @ReceiveEvent(components = {PlayerCharacterComponent.class, CharacterMovementComponent.class})
    public void observeDangerZone(MovedEvent event, EntityRef player) {
        //TODO: react on OnBiomeChangedEvent to handle the danger zone
        float height = event.getPosition().getY();
        float lastHeight = height - event.getDelta().getY();
        if (height > thresholdHeight && lastHeight <= thresholdHeight) {
            player.addOrSaveComponent(new HypothermiaComponent());
        }
        if (height < thresholdHeight && lastHeight >= thresholdHeight) {
            if (player.hasComponent(HypothermiaComponent.class)) {
                player.removeComponent(HypothermiaComponent.class);
            }
        }
    }

    @ReceiveEvent(components = {HypothermiaComponent.class})
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef player) {
        if (event.getMovementMode() == MovementMode.WALKING) {
            event.multiply(walkSpeedMultiplier);
        }
    }

    @ReceiveEvent(components = {HypothermiaComponent.class})
    public void modifyJumpSpeed(AffectJumpForceEvent event, EntityRef player) {
        event.multiply(jumpSpeedMultiplier);
    }
}
