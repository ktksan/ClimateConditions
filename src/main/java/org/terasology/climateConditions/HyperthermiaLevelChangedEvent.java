// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.BeforeAfterEvent;

/**
 * An event sent out when the level attribute of {@link HyperthermiaComponent} is changed.
 * <p>
 * A level of 0 represents that player is or was not affected by hyperthermia, for instance:
 * <ul>
 *     <li>{@code HyperthermiaLevelChangedEvent (0, 1)} is sent if the player gains hyperthermia in level 1</li>
 *     <li>{@code HyperthermiaLevelChangedEvent (3, 0)} is sent if the player looses all 3 levels of hyperthermia</li>
 * </ul>
 * The event is only sent if the new level differs from the old level.
 */
public class HyperthermiaLevelChangedEvent extends BeforeAfterEvent<Integer> {
    public HyperthermiaLevelChangedEvent(int oldLevel, int newLevel) {
        super(oldLevel, newLevel);
    }
}
