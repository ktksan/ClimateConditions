// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions.alterationEffects;

import org.terasology.entitySystem.Component;

/**
 * This is the component added to entities with the body temperature alteration effect.
 */
public class AffectBodyTemperatureComponent implements Component {
    public float postMultiplier;
    /**
     * Stores information regarding condition for body temperature change alteration -
     * Currently there are 3 types of the BodyTemperatureAlterationEffect with the following conditions-
     * 1. ON_DECREASE - when the change in temperature is negative.
     * 2. ON_INCREASE - when the change in temperature is positive.
     * 3. ALWAYS - modifies the change irrespective of whether it is positive or negative.
     */
    TemperatureAlterationCondition condition;
}
