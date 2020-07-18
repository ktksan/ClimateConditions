// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

/**
 * An event sent out when the level attribute of {@link HypothermiaComponent} is changed.
 */
public class HypothermiaLevelChangedEvent extends AbstractThermiaLevelChangedEvent{
    public HypothermiaLevelChangedEvent(int oldLevel, int newLevel) {
        super(oldLevel,newLevel);
    }
}
