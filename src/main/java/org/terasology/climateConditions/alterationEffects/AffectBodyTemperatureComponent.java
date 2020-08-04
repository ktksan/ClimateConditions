// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions.alterationEffects;

import org.terasology.entitySystem.Component;

/**
 * This is the component added to entities with the body temperature alteration effect.
 */
public class AffectBodyTemperatureComponent implements Component {

    public float multiplier;
    public float modifier;
    public float postMultiplier;
    /**
     * Stores information on type of body temperature change alteration -
     * currently 3 ids have been implemented -
     * 1. "activateWhenEnvironmentColder" - when change in temperature is negative.
     * 2. "activateWhenEnvironmentHotter" - when change in temperature is positive.
     * 3. "" - modifies the change irrespective of whether it is positive or negative.
     */
    String id = "";
}
