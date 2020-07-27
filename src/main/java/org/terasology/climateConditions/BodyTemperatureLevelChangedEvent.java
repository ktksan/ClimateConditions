// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.BeforeAfterEvent;

public class BodyTemperatureLevelChangedEvent extends BeforeAfterEvent<BodyTemperatureLevel> {
    public BodyTemperatureLevelChangedEvent(BodyTemperatureLevel oldLevel, BodyTemperatureLevel newLevel) {
        super(oldLevel, newLevel);
    }
}
