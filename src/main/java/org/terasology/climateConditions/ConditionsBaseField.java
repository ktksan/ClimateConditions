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

import com.google.common.base.Function;
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.Noise2D;
import org.terasology.utilities.procedural.SimplexNoise;

public class ConditionsBaseField {
    private Noise2D noiseTable;
    private int seaLevel;
    private int maxLevel;
    private float noiseMultiplier;
    private Function<Float, Float> function;
    private float minimumValue;
    private float maximumValue;

    public ConditionsBaseField(int seaLevel, int maxLevel, float noiseMultiplier,
                               Function<Float, Float> function, long conditionSeed,
                               float minimumValue, float maximumValue) {
        this.seaLevel = seaLevel;
        this.maxLevel = maxLevel;
        this.noiseMultiplier = noiseMultiplier;
        this.function = function;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        noiseTable = new SimplexNoise(conditionSeed);
    }

    public float get(float x, float y, float z) {
        float conditionAlpha = TeraMath.clamp(getConditionAlpha(x, y, z), 0, 1);
        return minimumValue + (conditionAlpha * (maximumValue - minimumValue));
    }

    private float getConditionAlpha(float x, float y, float z) {
        float result = noiseTable.noise(x * noiseMultiplier, z * noiseMultiplier);
        float temperatureBase = function.apply(TeraMath.clamp((result + 1.0f) / 2.0f));
        if (y <= seaLevel) {
            return temperatureBase;
        } else if (y >= maxLevel) {
            return 0;
        } else {
            // The higher above see level - the colder
            return temperatureBase * (1f * (maxLevel - y) / (maxLevel - seaLevel));
        }
    }
}
