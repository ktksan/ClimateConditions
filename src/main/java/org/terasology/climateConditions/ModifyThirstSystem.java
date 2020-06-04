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
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.thirst.component.ThirstComponent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class ModifyThirstSystem extends BaseComponentSystem {
    private float defaultNormalDecayPerSecond;
    private float defaultSprintDecayPerSecond;

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void onAddedHypothermia(OnAddedComponent event, EntityRef player, ThirstComponent thirst) {
        scaleThrist(player, thirst, 2f);
    }

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void beforeRemoveHyperthermia(BeforeRemoveComponent event, EntityRef player, ThirstComponent thirst) {
        getDefaultThirst(player);
        thirst.normalDecayPerSecond = defaultNormalDecayPerSecond;
        thirst.sprintDecayPerSecond = defaultSprintDecayPerSecond;
        player.saveComponent(thirst);
    }


    public void scaleThrist(EntityRef player, ThirstComponent thirst, float magnitude) {
        thirst.normalDecayPerSecond *= magnitude;
        thirst.sprintDecayPerSecond *= magnitude;
        player.saveComponent(thirst);
    }

    public void getDefaultThirst(EntityRef player) {
        ThirstComponent thirst = player.getParentPrefab().getComponent(ThirstComponent.class);
        defaultNormalDecayPerSecond = thirst.normalDecayPerSecond;
        defaultSprintDecayPerSecond = thirst.sprintDecayPerSecond;
    }
}
