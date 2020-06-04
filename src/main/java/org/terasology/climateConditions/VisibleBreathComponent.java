package org.terasology.climateConditions;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * Augments the world with a visible breath particle effect.
 * Is added/updated by the {@link VisibleBreathingSystem} periodically.
 */
public class VisibleBreathComponent implements Component {
    public EntityRef particleEntity = EntityRef.NULL;
}
