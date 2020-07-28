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
package org.terasology.climateConditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AffectJumpForceEvent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.event.ActivateRegenEvent;
import org.terasology.logic.health.event.ChangeMaxHealthEvent;
import org.terasology.thirst.event.AffectThirstEvent;

/**
 * Handles effects related to Hyperthermia.
 * Hyperthermia occurs in case of extremely high body temperatures and, e.g., slows the player's movements.
 * For adding new effects in existing or new Hyperthermia Levels, {@link HyperthermiaLevelChangedEvent} should be
 * reacted to either in this or a separate authority system for eg. {@link FrostbiteSystem}, a hypothermia effect.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HyperthermiaSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(HyperthermiaSystem.class);

    /**
     * Reduces the walking/running speed of the player.
     * Is only active iff the player has a {@link HyperthermiaComponent}.
     */
    @ReceiveEvent
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef player, HyperthermiaComponent hyperthermia) {
        event.multiply(hyperthermia.walkSpeedMultiplier);
    }

    /**
     * Reduces the jump speed of the player.
     * Is only active iff the player has a {@link HyperthermiaComponent}.
     */
    @ReceiveEvent
    public void modifyJumpSpeed(AffectJumpForceEvent event, EntityRef player, HyperthermiaComponent hyperthermia) {
        event.multiply(hyperthermia.jumpSpeedMultiplier);
    }

    /**
     * Increases the thirst decay per second of the player.
     * Is only active iff the player has a {@link HyperthermiaComponent}.
     */
    @ReceiveEvent
    public void modifyThirst(AffectThirstEvent event, EntityRef player, HyperthermiaComponent hyperthermia) {
        event.multiply(hyperthermia.thirstMultiplier);
    }

    /**
     * Weakens the player by reducing the maxHealth and regeneration of the player.
     */
    private void applyWeakening(EntityRef player, HealthComponent health, HyperthermiaComponent hyperthermia) {
        player.send(new ChangeMaxHealthEvent(hyperthermia.maxHealthMultiplier * health.maxHealth));
        health.currentHealth = Math.min(health.currentHealth, health.maxHealth);
        health.regenRate *= hyperthermia.regenMultiplier;
        player.saveComponent(health);
    }


    /**
     * Reverts the player weakening by restoring the maxHealth and regeneration of the player to the original value.
     */
    private void revertWeakening(EntityRef player, HealthComponent health, HyperthermiaComponent hyperthermia) {
        player.send(new ChangeMaxHealthEvent(player.getParentPrefab().getComponent(HealthComponent.class).maxHealth));
        player.send(new ActivateRegenEvent());
        health.regenRate /= hyperthermia.regenMultiplier;
        player.saveComponent(health);
    }

    @ReceiveEvent
    public void hyperthermiaLevelChanged(HyperthermiaLevelChangedEvent event, EntityRef player,
                                         HyperthermiaComponent hyperthermia, HealthComponent health) {
        int oldLevel = event.getOldValue();
        int newLevel = event.getNewValue();
        player.saveComponent(modifyHyperthermiaMultipliers(hyperthermia, newLevel));
        //Weakening effect remains active for Hyperthermia levels 3 and greater.
        if (newLevel == 3 && oldLevel < newLevel) {
            applyWeakening(player, health, hyperthermia);
        } else if (oldLevel == 3 && oldLevel > newLevel) {
            revertWeakening(player, health, hyperthermia);
        }
    }

    private HyperthermiaComponent modifyHyperthermiaMultipliers(HyperthermiaComponent hyperthermia, int level) {
        switch (level) {
            case 1:
                hyperthermia.walkSpeedMultiplier = 1;
                hyperthermia.jumpSpeedMultiplier = 1;
                hyperthermia.thirstMultiplier = 1.5f;
                break;
            case 2:
                hyperthermia.walkSpeedMultiplier = 0.7f;
                hyperthermia.jumpSpeedMultiplier = 0.85f;
                hyperthermia.thirstMultiplier = 2f;
                break;
            case 3:
                hyperthermia.walkSpeedMultiplier = 0.6f;
                hyperthermia.jumpSpeedMultiplier = 0.7f;
                hyperthermia.thirstMultiplier = 2.25f;
                break;
            default:
                logger.warn("Unexpected Hyperthermia Level.");
        }
        return hyperthermia;
    }
}
