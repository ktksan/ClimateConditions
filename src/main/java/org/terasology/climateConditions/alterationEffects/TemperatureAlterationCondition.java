// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions.alterationEffects;

/**
 * Stores information regarding the type/conditions for BodyTemperatureAlterationEffect.
 */
public enum TemperatureAlterationCondition {
    ON_DECREASE,    //When Body Temperature is decreased
    ON_INCREASE,    //When Body Temperature is increased
    ALWAYS;
}
