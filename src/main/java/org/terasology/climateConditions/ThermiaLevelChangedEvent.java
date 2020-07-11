// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.Event;

public class ThermiaLevelChangedEvent implements Event {
    private int oldLevel;
    private int newLevel;

    ThermiaLevelChangedEvent(int oldLevel, int newLevel) {
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public int getNewLevel() {
        return this.newLevel;
    }

    public int getOldLevel() {
        return this.oldLevel;
    }
}
