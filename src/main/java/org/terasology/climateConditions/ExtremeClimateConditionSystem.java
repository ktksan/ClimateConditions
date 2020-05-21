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
import org.terasology.biomesAPI.Biome;
import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.context.Context;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterSoundComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.health.RegenComponent;
import org.terasology.logic.health.event.ActivateRegenEvent;
import org.terasology.logic.health.event.DeactivateRegenEvent;
import org.terasology.logic.health.event.DoDamageEvent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.particles.components.generators.VelocityRangeGeneratorComponent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/*
import org.terasology.thirst.ThirstUtils;
import org.terasology.thirst.component.ThirstComponent;
import org.terasology.utilities.random.Random;

import static org.terasology.logic.health.RegenAuthoritySystem.BASE_REGEN;
import static org.terasology.thirst.ThirstAuthoritySystem.THIRST_DAMAGE_ACTION_ID;
*/
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class ExtremeClimateConditionSystem extends BaseComponentSystem {
    public static final String FROSTBITE_DAMAGE_ACTION_ID = "Frostbite Damage";
    public static final String VISIBLE_BREATH_ACTION_ID = "Visible Breath";

    @In
    private EntityManager entityManager;
    @In
    private DelayManager delayManager;
    @In
    private PrefabManager prefabManager;
    @In
    private Time time;
    @In
    private Context context;

    private int healthDecreaseInterval = 20000;
    private int initialDelay = 5000;
    private int healthDecreaseAmount = 15;
    private int breathInterval = 7000;
    private float thresholdHeight = 60f;
    private float defaultRunFactor;
    private float defaultSpeedMultiplier;
    private float defaultJumpSpeed;
    private float reducedRunFactorMultiplier = 0.6f;
    private float reducedSpeedMultiplierMultiplier = 0.7f;
    private float reducedJumpSpeedMultiplier = 0.8f;
    private Random random = new FastRandom();


    /*
    NOTE: This part is for the desert effects and is incomplete.

    @ReceiveEvent(components = {ThirstComponent.class, CharacterMovementComponent.class})
    public void EnterDesert(OnPlayerSpawnedEvent event, EntityRef character, ThirstComponent thirst, CharacterMovementComponent movement) {

        /*
        Biome newBiome= event.getNewBiome();
        if(newBiome.getName() == "Desert")
        {
            thirst.sprintDecayPerSecond*=10;
            thirst.normalDecayPerSecond*=10;
        }

        thirst.sprintDecayPerSecond*=10;
        thirst.normalDecayPerSecond*=10;
        movement.runFactor = 1;
        movement.speedMultiplier = (float)0.8;
        movement.jumpSpeed = (float)8;
    }
*/

    //The following part is for the Exreme Climate Effects (Snow biome).
    @ReceiveEvent(components = {PlayerCharacterComponent.class, CharacterMovementComponent.class})
    public void extremeSnowEffect(MovedEvent event, EntityRef player, LocationComponent location, CharacterMovementComponent movement) {
        float height = event.getPosition().getY();
        float deltaHeight = event.getDelta().getY();
        float lastHeight = height - deltaHeight;
        if (height > thresholdHeight && lastHeight <= thresholdHeight) {
            delayManager.addPeriodicAction(player, FROSTBITE_DAMAGE_ACTION_ID, initialDelay, healthDecreaseInterval);
            delayManager.addPeriodicAction(player, VISIBLE_BREATH_ACTION_ID, initialDelay, breathInterval);
            player.send(new ReduceSpeedEvent());
        }
        if (height < thresholdHeight && lastHeight >= thresholdHeight) {
            delayManager.cancelPeriodicAction(player, FROSTBITE_DAMAGE_ACTION_ID);
            delayManager.cancelPeriodicAction(player, VISIBLE_BREATH_ACTION_ID);
            player.send(new ChangeSpeedToDefaultEvent());
            if (player.hasComponent(VisibleBreathComponent.class)) {
                EntityRef particleEntity = player.getComponent(VisibleBreathComponent.class).particleEntity;
                if (particleEntity != EntityRef.NULL) {
                    particleEntity.destroy();
                }
                player.removeComponent(VisibleBreathComponent.class);
            }
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onPeriodicFrostbite(PeriodicActionTriggeredEvent event, EntityRef player) {
        if (event.getActionId().equals(FROSTBITE_DAMAGE_ACTION_ID)) {
            // Check to see if health should be decreased
            LocationComponent location = player.getComponent(LocationComponent.class);
            float height = location.getLocalPosition().getY();
            //   float deltaHeight = location.getLastPosition().getY();
            // float lastHeight = height - deltaHeight;
            if (height > thresholdHeight) {
                applyFrostbiteDamagePlayer(player);
                // Stun the player for 500 ms
                applyStunEffect(player, 1000);
                playFrostbiteSound(player);
            }
        }
    }
    @ReceiveEvent(components = {LocationComponent.class})
    public void onPeriodicBreath(PeriodicActionTriggeredEvent event, EntityRef player) {
        if (event.getActionId().equals(VISIBLE_BREATH_ACTION_ID)) {
            // Check to see if breath should be visible
            LocationComponent location = player.getComponent(LocationComponent.class);
            float height = location.getLocalPosition().getY();
            if (height > thresholdHeight) {
               updateVisibleBreathEffect(player);

            }
        }
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_NORMAL + 10)
    public void onHealthRegen(ActivateRegenEvent event, EntityRef player, LocationComponent location) {
        float height = location.getLocalPosition().getY();
        if (height > thresholdHeight) {
            player.send(new DeactivateRegenEvent());
        }
    }

    public void getDefaultMovement(EntityRef player, CharacterMovementComponent movement) {
        Prefab parentPrefab = player.getParentPrefab() ;
        CharacterMovementComponent defaultMovement = parentPrefab.getComponent(CharacterMovementComponent.class);
        defaultRunFactor = defaultMovement.runFactor;
        defaultSpeedMultiplier = defaultMovement.speedMultiplier;
        defaultJumpSpeed = defaultMovement.jumpSpeed;
    }


    @ReceiveEvent
    public void onSpeedReduce(ReduceSpeedEvent event, EntityRef player, CharacterMovementComponent movement) {
        movement.runFactor *= reducedRunFactorMultiplier;
        movement.speedMultiplier *= reducedSpeedMultiplierMultiplier;
        movement.jumpSpeed *= reducedJumpSpeedMultiplier;
    }

    @ReceiveEvent
    public void onChangeSpeedToDefault(ChangeSpeedToDefaultEvent event, EntityRef player, CharacterMovementComponent movement) {
        movement.runFactor /= reducedRunFactorMultiplier;
        movement.speedMultiplier /= reducedSpeedMultiplierMultiplier;
        movement.jumpSpeed /= reducedJumpSpeedMultiplier;
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

    private void updateVisibleBreathEffect(EntityRef player) {
        EntityRef particleEntity = entityManager.create("climateConditions:VisibleBreathEffect");
        LocationComponent targetLoc = player.getComponent(LocationComponent.class);
        LocationComponent childLoc = particleEntity.getComponent(LocationComponent.class);
        childLoc.setWorldPosition(targetLoc.getWorldPosition());
        Location.attachChild(player, particleEntity);
        particleEntity.setOwner(player);
        Vector3f direction = targetLoc.getLocalDirection();
        direction.normalize();

        //NOTE: initializing velocity using the constructor wasn't working hence the long method.
        //particleEntity.addComponent(new VelocityRangeGeneratorComponent(direction,direction.scale(2)));
        VelocityRangeGeneratorComponent velocity = particleEntity.getComponent(VelocityRangeGeneratorComponent.class);
        direction.scale((float) 0.5);
        direction.addY((float) 0.5);
        velocity.minVelocity = direction;
        direction.scale((float) 1.5);
        velocity.maxVelocity = direction;
        particleEntity.addOrSaveComponent(velocity);
        if (!player.hasComponent(VisibleBreathComponent.class)) {
            player.addComponent(new VisibleBreathComponent());
        }
        player.getComponent(VisibleBreathComponent.class).particleEntity = particleEntity;
    }

}