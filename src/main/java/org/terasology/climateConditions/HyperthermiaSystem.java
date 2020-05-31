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

import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AffectJumpForceEvent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.thirst.component.ThirstComponent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HyperthermiaSystem extends BaseComponentSystem {
    private float walkSpeedMultiplier = 0.6f;
    private float jumpSpeedMultiplier = 0.7f;

    @ReceiveEvent
    public void modifySpeed2(GetMaxSpeedEvent event, EntityRef player, ThirstComponent thirst) {
        if (event.getMovementMode() == MovementMode.WALKING) {
            event.multiply(1.2f);
            thirst.normalDecayPerSecond = 3f;
        }
    }


    @ReceiveEvent
    public void onBiomeChange(OnBiomeChangedEvent event, EntityRef player, ThirstComponent thirst) {
            if(event.getNewBiome().getDisplayName().equals("DESERT")) {
                thirst.normalDecayPerSecond = 3f;
            }
//            String A = "a";
    }

    @ReceiveEvent
    public void modifyJumpSpeed2(AffectJumpForceEvent event, EntityRef player) {
        event.multiply(0.8f);
    }
}
