// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

public class HyperthermiaLevelChangedEvent extends ThermiaLevelChangedEvent{
    public HyperthermiaLevelChangedEvent(int oldLevel, int newLevel) {
        super(oldLevel,newLevel);
    }
}
