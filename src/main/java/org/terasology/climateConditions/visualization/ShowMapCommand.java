/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.climateConditions.visualization;

import com.google.common.base.Function;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.climateConditions.ConditionsBaseField;
import org.terasology.context.Context;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.world.WorldProvider;

@RegisterSystem
public class ShowMapCommand extends BaseComponentSystem {
    public static final int SIZE_OF_IMAGE = 300;

    @In
    ClimateMapDisplaySystem climateSystem;

    @In
    private ClimateConditionsSystem climateConditions;

    @In
    private NUIManager nuiManager;

    /**
     * Displays the selected map at the selected height level.
     *
     * The lighter a pixel on the map, the higher temperature/humidity/whatever else.
     * @param mapType
     */
    @Command(shortDescription = "Display condition map", helpText = "Show a given map (humidity, temperature) at a given height level.")
    public void showClimateMap(@CommandParam("map type") String mapType, @CommandParam("height to look at") int height) {
        climateSystem.setMapHeight(height);

        if (mapType.equals("temperature")) {
            climateSystem.setClimateConditionsBase(climateConditions.getTemperatureBaseField());
        } else {
            climateSystem.setClimateConditionsBase(climateConditions.getHumidityBaseField());
        }

        nuiManager.pushScreen("ClimateConditions:displayConditionScreen");
    }
}
