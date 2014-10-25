/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class HumidityGeneratorSystem extends BaseComponentSystem {
    @In
    private ClimateConditionsSystem environmentSystem;

    @Override
    public void preBegin() {
        environmentSystem.addHumidityModifier(1000,
                new ConditionModifier() {
                    @Override
                    public float getCondition(float value, float x, float y, float z) {
                        return getValue(value, x, y, z);
                    }
                });
    }

    private Map<Vector3i, HumidityGeneratorComponent> activeComponents = new HashMap<>();

    @ReceiveEvent
    public void componentActivated(OnActivatedComponent event, HumidityGeneratorComponent generator, BlockComponent block) {
        activeComponents.put(block.getPosition(), generator);
    }

    @ReceiveEvent
    public void componentUpdated(OnChangedComponent event, HumidityGeneratorComponent generator, BlockComponent block) {
        activeComponents.put(block.getPosition(), generator);
    }

    @ReceiveEvent
    public void componentDeactivated(BeforeDeactivateComponent event, HumidityGeneratorComponent generator, BlockComponent block) {
        activeComponents.remove(block.getPosition());
    }

    private float getValue(float value, float x, float y, float z) {
        for (Map.Entry<Vector3i, HumidityGeneratorComponent> entry : activeComponents.entrySet()) {
            Vector3i location = entry.getKey();
            HumidityGeneratorComponent generator = entry.getValue();

            if ((generator.humidity > value && generator.humidifier)
                    || (generator.humidity < value && !generator.humidifier)) {
                float distance = getDistance(x, y, z, location);
                if (distance <= generator.flatRange) {
                    value = generator.humidity;
                } else if (distance < generator.maxRange) {
                    float distanceFactor = 1f - (distance - generator.flatRange) / (generator.maxRange - generator.flatRange);
                    value = value + (float) ((generator.humidity - value) * Math.pow(distanceFactor, 1 / 3f));
                }
            }
        }

        return value;
    }

    private float getDistance(float x, float y, float z, Vector3i location) {
        return (float) Math.sqrt((location.x - x) * (location.x - x)
                + (location.y - y) * (location.y - y) + (location.z - z) * (location.z - z));
    }
}
