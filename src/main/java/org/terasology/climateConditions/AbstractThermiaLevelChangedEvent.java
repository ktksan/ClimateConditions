// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.Event;

/**
 * This event (or a subtype) is sent whenever the level attribute of Hypothermia or Hyperthermia Component is changed.
 * This usually means a change in the challenges faced by the player due to abnormal body temperatures.
 */
public class AbstractThermiaLevelChangedEvent implements Event {
    private int oldLevel;
    private int newLevel;

    AbstractThermiaLevelChangedEvent(int oldLevel, int newLevel) {
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
