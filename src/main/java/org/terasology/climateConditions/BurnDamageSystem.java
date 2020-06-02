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

import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.health.event.ActivateRegenEvent;
import org.terasology.logic.health.event.DeactivateRegenEvent;
import org.terasology.logic.health.event.DoDamageEvent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BurnDamageSystem extends BaseComponentSystem {
    public static final String BURN_DAMAGE_ACTION_ID = "Burn Damage";

    @In
    private DelayManager delayManager;
    @In
    private PrefabManager prefabManager;
    @In
    private Context context;

    private static final int healthDecreaseInterval = 20000;
    private static final int initialDelay = 60000;
    private static final int healthDecreaseAmount = 10;

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void onHyperthermia(OnAddedComponent event, EntityRef player) {
        delayManager.addPeriodicAction(player, BURN_DAMAGE_ACTION_ID, initialDelay, healthDecreaseInterval);
    }

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void beforeRemoveHyperthermia(BeforeRemoveComponent event, EntityRef player) {
        delayManager.cancelPeriodicAction(player, BURN_DAMAGE_ACTION_ID);
    }

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void onPeriodicBurn(PeriodicActionTriggeredEvent event, EntityRef player) {
        if (event.getActionId().equals(BURN_DAMAGE_ACTION_ID)) {
            applyBurnDamagePlayer(player);
        }
    }

    @ReceiveEvent(components = {HyperthermiaComponent.class})
    public void onHealthRegen(ActivateRegenEvent event, EntityRef entity) {
        entity.send(new DeactivateRegenEvent());
    }

    private void applyBurnDamagePlayer(EntityRef player) {
        Prefab burnDamagePrefab = prefabManager.getPrefab("ClimateConditions:BurnDamage");
        player.send(new DoDamageEvent(healthDecreaseAmount, burnDamagePrefab));
    }
}
