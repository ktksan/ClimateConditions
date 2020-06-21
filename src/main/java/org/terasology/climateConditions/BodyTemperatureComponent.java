// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

public class BodyTemperatureComponent implements Component {

    @Replicate
    public float current = 0.4f;

    BodyTemperatureLevel currentLevel;
}
