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

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;

import static org.terasology.climateConditions.visualization.ShowMapCommand.SIZE_OF_IMAGE;

public class ClimateMapWidget extends CoreWidget {
    private ClimateMapDisplaySystem climateSystem;

    /**
     * Converts the base climate condition values to a color, and draws them on the canvas.
     * @param canvas The canvas that the map is drawn on.
     */
    @Override
    public void onDraw(Canvas canvas) {
        if (climateSystem != null) {
            canvas.drawFilledRectangle(Rect2i.createFromMinAndMax(new Vector2i(0, 0), new Vector2i(SIZE_OF_IMAGE, SIZE_OF_IMAGE)), Color.WHITE);
            for (int i = 0; i < SIZE_OF_IMAGE; i++) {
                for (int j = 0; j < SIZE_OF_IMAGE; j++) {
                    Vector3f playerPosition = climateSystem.getPlayer().getPosition();
                    int height = climateSystem.getMapHeight();
                    int offsetZ =  - (SIZE_OF_IMAGE / 2) + j;
                    int offsetX = - (SIZE_OF_IMAGE / 2) + i;
                    float color = climateSystem.getClimateConditionsBase().get(playerPosition.x + offsetX, height, playerPosition.z + offsetZ);
                    canvas.drawLine(i, j, i + 1, j + 1, new Color(color, color, color));
                }
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(SIZE_OF_IMAGE, SIZE_OF_IMAGE);
    }

    public void setShowMapCommand(ClimateMapDisplaySystem command) {
        climateSystem = command;
    }
}
