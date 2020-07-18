// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

/**
 * An event sent out when the level attribute of {@link HyperthermiaComponent} is changed.
 */
public class HyperthermiaLevelChangedEvent extends AbstractThermiaLevelChangedEvent {
    public HyperthermiaLevelChangedEvent(int oldLevel, int newLevel) {
        super(oldLevel, newLevel);
    }
}
