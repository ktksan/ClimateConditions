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

import org.terasology.entitySystem.Component;

/**
 * Increases the game difficulty in locations with extreme cold climate.
 * Is added/removed by the {@link HypothermiaSystem} when the player enters/leaves a "danger zone".
 */
public class HypothermiaComponent implements Component {
    public float walkSpeedMultiplier = 0.6f;
    public float jumpSpeedMultiplier = 0.7f;
    //The level of Hypothermia represents the degree of challenges faced due to Hypothermia.
    // As of now only levels - 1,2,3 are supported 1 being the least challenging and 3 the most.
    //Level 0 represents no Hypothermia i.e. HypothermiaComponent should be removed when set to 0.
    public int level;

    HypothermiaComponent() {
        level = 1;
    }

    HypothermiaComponent(int level) {
        this.level = level;
    }
}
