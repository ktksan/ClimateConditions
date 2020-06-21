// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.Event;

public class BodyTemperatureChangedEvent implements Event {
    private BodyTemperatureLevel oldLevel;
    private BodyTemperatureLevel newLevel;


    public BodyTemperatureChangedEvent(BodyTemperatureLevel oldLevel, BodyTemperatureLevel newLevel) {
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public BodyTemperatureLevel getNewBodyTemperatureLevel(){
        return  newLevel;
    }

    public BodyTemperatureLevel getOldBodyTemperatureLevel(){
        return  oldLevel;
    }
}
