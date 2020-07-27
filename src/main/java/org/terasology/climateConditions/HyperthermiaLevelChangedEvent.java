// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.BeforeAfterEvent;

/**
 * An event sent out when the level attribute of {@link HyperthermiaComponent} is changed.
 */
public class HyperthermiaLevelChangedEvent extends BeforeAfterEvent<Integer> {
    public HyperthermiaLevelChangedEvent(int oldLevel, int newLevel) {
        super(oldLevel, newLevel);
    }
}
