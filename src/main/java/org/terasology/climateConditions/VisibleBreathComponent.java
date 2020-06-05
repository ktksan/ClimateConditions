package org.terasology.climateConditions;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * Augments the world with a visible breath particle effect. Has a desired effect only if the entity has a {@link
 * org.terasology.logic.location.LocationComponent} which determines the location and direction of the effect.
 * Is added/updated by the {@link VisibleBreathingSystem} periodically.
 */
public class VisibleBreathComponent implements Component {
    public EntityRef particleEntity = EntityRef.NULL;
}
