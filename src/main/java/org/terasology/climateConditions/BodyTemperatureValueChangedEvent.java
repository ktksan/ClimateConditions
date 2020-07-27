// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.BeforeAfterEvent;

public class BodyTemperatureValueChangedEvent extends BeforeAfterEvent<Float> {
    public BodyTemperatureValueChangedEvent(float oldValue, float newValue) {
        super(oldValue, newValue);
    }
}
