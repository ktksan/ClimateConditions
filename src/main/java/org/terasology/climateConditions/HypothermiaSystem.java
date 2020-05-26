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

import org.terasology.alterationEffects.speed.StunAlterationEffect;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterSoundComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.health.event.DoDamageEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HypothermiaSystem extends BaseComponentSystem {
    public static final String FROSTBITE_DAMAGE_ACTION_ID = "Frostbite Damage";

    @In
    private DelayManager delayManager;
    @In
    private PrefabManager prefabManager;
    @In
    private Context context;

    private float thresholdHeight = 60f;
    private int healthDecreaseInterval = 20000;
    private int initialDelay = 5000;
    private int healthDecreaseAmount = 15;
    private Random random = new FastRandom();


    @ReceiveEvent(components = {PlayerCharacterComponent.class, CharacterMovementComponent.class})
    public void observeDangerZone(MovedEvent event, EntityRef player, LocationComponent location, CharacterMovementComponent movement) {
        //TODO: react on OnBiomeChangedEvent to handle the danger zone
        float height = event.getPosition().getY();
        float lastHeight = height - event.getDelta().getY();
        if (height > thresholdHeight && lastHeight <= thresholdHeight) {
            player.addOrSaveComponent(new HypothermiaComponent());
        }
        if (height < thresholdHeight && lastHeight >= thresholdHeight) {
            if(player.hasComponent(HypothermiaComponent.class)){
                player.removeComponent(HypothermiaComponent.class);
            }
        }
    }

    @ReceiveEvent
    public void onHypothermia(OnAddedComponent event, EntityRef player, HypothermiaComponent hypothermia){
        if(player.hasComponent(HypothermiaComponent.class)){
            delayManager.addPeriodicAction(player, FROSTBITE_DAMAGE_ACTION_ID, initialDelay, healthDecreaseInterval);
        }
    }
    @ReceiveEvent(components = {LocationComponent.class})
    public void onPeriodicFrostbite(PeriodicActionTriggeredEvent event, EntityRef player) {
        if (event.getActionId().equals(FROSTBITE_DAMAGE_ACTION_ID)) {
            // Check to see if health should be decreased
            LocationComponent location = player.getComponent(LocationComponent.class);
            float height = location.getLocalPosition().getY();
            if (height > thresholdHeight) {
                applyFrostbiteDamagePlayer(player);
                // Stun the player for 500 ms
                applyStunEffect(player, 1000);
                playFrostbiteSound(player);
            }
        }
    }
    private void applyFrostbiteDamagePlayer(EntityRef player) {
        Prefab frostbiteDamagePrefab = prefabManager.getPrefab("ClimateConditions:FrostbiteDamage");
        player.send(new DoDamageEvent(healthDecreaseAmount, frostbiteDamagePrefab));
    }
    private void applyStunEffect(EntityRef player, int duration) {
        StunAlterationEffect stunAlterationEffect = new StunAlterationEffect(context);
        //Both the instigator and the target is the player
        //the magnitude parameter is not used by StunAlterationEffect
        stunAlterationEffect.applyEffect(player, player, 0, duration);

    }
    public void playFrostbiteSound (EntityRef entity) {
        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if (characterSounds.deathSounds.size() > 0) {
            StaticSound sound = random.nextItem(characterSounds.deathSounds);
            entity.send(new PlaySoundEvent(entity, sound, characterSounds.deathVolume));
        }
    }

}
