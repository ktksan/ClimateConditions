// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

/**
 * An event sent out when the level attribute of {@link HypothermiaComponent} is changed.
 * NOTE: level 0 represents the player does not have a HypothermiaComponent.
 */
public class HypothermiaLevelChangedEvent extends AbstractThermiaLevelChangedEvent{
    public HypothermiaLevelChangedEvent(int oldLevel, int newLevel) {
        super(oldLevel,newLevel);
    }
}
