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

public class HyperthermiaComponent implements Component {
    public float walkSpeedMultiplier = 0.7f;
    public float jumpSpeedMultiplier = 0.85f;
    public float regenMultiplier = 0.8f;
    public float maxHealthMultiplier = 0.8f;
    public float thirstMultiplier = 2f;
    //The level of Hyperthermia represents the degree of challenges faced due to Hyperthermia.
    // As of now only levels - 1,2,3 are supported 1 being the least challenging and 3 the most.
    //Level 0 represents no Hyperthermia i.e. HyperthermiaComponent should be removed when set to 0.
    public int level;

    HyperthermiaComponent() {
        level = 1;
    }

    HyperthermiaComponent(int level) {
        this.level = level;
    }
}
