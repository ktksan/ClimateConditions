// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.BeforeAfterEvent;

/**
 * Event sent out by the {@link BodyTemperatureSystem} to notify other systems about the change in the Body Temperature
 * Value. Changes in Body Temperature Value are also used to check if Body Temperature Level should be changed.
 * Body Temperature Levels corresponding to the Body Temperature Value ranges can be found in
 * {@link BodyTemperatureComponent}.
 */
public class BodyTemperatureValueChangedEvent extends BeforeAfterEvent<Float> {
    public BodyTemperatureValueChangedEvent(float oldValue, float newValue) {
        super(oldValue, newValue);
    }
}
