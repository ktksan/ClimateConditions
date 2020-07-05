// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.Event;

public class BodyTemperatureValueChangedEvent implements Event {
    private float oldValue;
    private float newValue;


    public BodyTemperatureValueChangedEvent(float oldValue, float newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public float getNewBodyTemperatureValue(){
        return  newValue;
    }

    public float getOldBodyTemperatureValue(){
        return  oldValue;
    }
}
