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

import com.google.common.collect.Maps;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.ImmutableBlockLocation;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TemperatureGeneratorSystem extends BaseComponentSystem {
    @In
    private ClimateConditionsSystem environmentSystem;

    @Override
    public void preBegin() {
        environmentSystem.addTemperatureModifier(1000,
                new ConditionModifier() {
                    @Override
                    public float getCondition(float value, float x, float y, float z) {
                        return getValue(value, x, y, z);
                    }
                });
    }

    private Map<ImmutableBlockLocation, TemperatureGeneratorComponent> activeComponents = Maps.newHashMap();

    @ReceiveEvent
    public void componentActivated(OnActivatedComponent event, TemperatureGeneratorComponent generator, BlockComponent block) {
        activeComponents.put(new ImmutableBlockLocation(block.getPosition()), generator);
    }

    @ReceiveEvent
    public void componentUpdated(OnChangedComponent event, TemperatureGeneratorComponent generator, BlockComponent block) {
        activeComponents.put(new ImmutableBlockLocation(block.getPosition()), generator);
    }

    @ReceiveEvent
    public void componentDeactivated(BeforeDeactivateComponent event, TemperatureGeneratorComponent generator, BlockComponent block) {
        activeComponents.remove(new ImmutableBlockLocation(block.getPosition()));
    }

    private float getValue(float value, float x, float y, float z) {
        for (Map.Entry<ImmutableBlockLocation, TemperatureGeneratorComponent> entry : activeComponents.entrySet()) {
            ImmutableBlockLocation location = entry.getKey();
            TemperatureGeneratorComponent generator = entry.getValue();

            if ((generator.temperature > value && generator.heater)
                    || (generator.temperature < value && !generator.heater)) {
                float distance = getDistance(x, y, z, location);
                if (distance <= generator.flatRange) {
                    value = generator.temperature;
                } else if (distance < generator.maxRange) {
                    float distanceFactor = 1f - (distance - generator.flatRange) / (generator.maxRange - generator.flatRange);
                    value = value + (float) ((generator.temperature - value) * Math.pow(distanceFactor, 1 / 3f));
                }
            }
        }

        return value;
    }

    private float getDistance(float x, float y, float z, ImmutableBlockLocation location) {
        return (float) Math.sqrt((location.x - x) * (location.x - x)
                + (location.y - y) * (location.y - y) + (location.z - z) * (location.z - z));
    }
}
