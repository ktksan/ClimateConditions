// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.BeforeAfterEvent;

/**
 * Event sent out by the {@link BodyTemperatureSystem} to notify other systems about the change in the Body Temperature
 * Level. Body Temperature Levels corresponding to the Body Temperature Value ranges can be found in
 * {@link BodyTemperatureComponent}.
 */
public class BodyTemperatureLevelChangedEvent extends BeforeAfterEvent<BodyTemperatureLevel> {
    public BodyTemperatureLevelChangedEvent(BodyTemperatureLevel oldLevel, BodyTemperatureLevel newLevel) {
        super(oldLevel, newLevel);
    }
}
