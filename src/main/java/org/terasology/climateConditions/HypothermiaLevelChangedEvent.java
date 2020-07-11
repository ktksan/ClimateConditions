// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

public class HypothermiaLevelChangedEvent extends ThermiaLevelChangedEvent{
    public HypothermiaLevelChangedEvent(int oldLevel, int newLevel) {
        super(oldLevel,newLevel);
    }
}
