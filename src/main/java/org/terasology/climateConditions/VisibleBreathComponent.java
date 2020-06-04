package org.terasology.climateConditions;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * This component is added to the player entity when the player enters the danger zone. Gives a visible breath to the
 * player using particle effects.
 */
public class VisibleBreathComponent implements Component {
    public EntityRef particleEntity = EntityRef.NULL;
}
