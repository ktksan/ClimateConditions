// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.event.AbstractValueModifiableEvent;

/**
 * This event is sent out by the {@link BodyTemperatureSystem} to allow for other systems to modify the change in body
 * temperature.
 */
public class AffectBodyTemperatureEvent extends AbstractValueModifiableEvent {
    //The following additions are only needed because the AbstractValueModifiableEvent does not support negative base
    // values at the moment.
    private boolean isNegative = false;

    public AffectBodyTemperatureEvent(float baseValue) {
        super((baseValue > 0) ? baseValue : (-1 * baseValue));
        if (baseValue < 0) {
            isNegative = true;
        }
    }

    @Override
    public float getResultValue() {
        if (isNegative) {
            return super.getResultValue() * -1;
        }
        return super.getResultValue();
    }

    public boolean isNegative() {
        return isNegative;
    }
}
