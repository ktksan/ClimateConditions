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

import org.terasology.biomesAPI.Biome;
import org.terasology.biomesAPI.OnBiomeChangedEvent;
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
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.health.RegenComponent;
import org.terasology.logic.health.event.ActivateRegenEvent;
import org.terasology.logic.health.event.DeactivateRegenEvent;
import org.terasology.logic.health.event.DoDamageEvent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.thirst.ThirstUtils;
import org.terasology.thirst.component.ThirstComponent;
import org.terasology.utilities.random.Random;

import static org.terasology.logic.health.RegenAuthoritySystem.BASE_REGEN;
import static org.terasology.thirst.ThirstAuthoritySystem.THIRST_DAMAGE_ACTION_ID;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class ExtremeClimateConditionSystem extends BaseComponentSystem{
    public static final String FROSTBITE_DAMAGE_ACTION_ID = "Frostbite Damage";

    @In
    private EntityManager entityManager;
    @In
    private DelayManager delayManager;
    @In
    private PrefabManager prefabManager;
    @In
    private Time time;

    private int healthDecreaseInterval = 20000;
    private int healthDecreaseAmount= 10;
    private float thresholdHeight= (float)60;
    private float defaultRunFactor;
    private float defaultSpeedMultiplier;
    private float defaultJumpSpeed;
    private float reducedRunFactorMultiplier = (float)0.6;
    private float reducedSpeedMultiplierMultiplier = (float)0.7;
    private float reducedJumpSpeedMultiplier = (float)0.8;
    private Random random;

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

    //The following part is for the Snow effects.
    @ReceiveEvent(components = {PlayerCharacterComponent.class, ThirstComponent.class, CharacterMovementComponent.class})
    public void ChangeSnowEffect(MovedEvent event, EntityRef player, LocationComponent location, ThirstComponent thirst, CharacterMovementComponent movement) {
        float height = event.getPosition().getY();
        float deltaHeight = event.getDelta().getY();
        float lastHeight = height - deltaHeight;
        if(height > thresholdHeight && lastHeight <= thresholdHeight) {
                delayManager.addPeriodicAction(player, FROSTBITE_DAMAGE_ACTION_ID, 0, healthDecreaseInterval);
                player.send(new ReduceSpeedEvent());
                //The following part adds snowParticleEffect
                if (!player.hasComponent(SnowParticleComponent.class)) {
                    EntityRef particleEntity = entityManager.create("climateConditions:snowParticleEffect");
                    LocationComponent targetLoc = player.getComponent(LocationComponent.class);
                    LocationComponent childLoc = particleEntity.getComponent(LocationComponent.class);
                    childLoc.setWorldPosition(targetLoc.getWorldPosition());
                    Location.attachChild(player, particleEntity);
                    particleEntity.setOwner(player);
                    player.addComponent(new SnowParticleComponent());
                    player.getComponent(SnowParticleComponent.class).particleEntity = particleEntity;
                }

            }

        if(height < thresholdHeight && lastHeight >= thresholdHeight) {
                delayManager.cancelPeriodicAction(player, FROSTBITE_DAMAGE_ACTION_ID);
                player.send(new ChangeSpeedToDefaultEvent());

                if (player.hasComponent(SnowParticleComponent.class)) {
                    EntityRef particleEntity = player.getComponent(SnowParticleComponent.class).particleEntity;
                    if (particleEntity != EntityRef.NULL) {
                        particleEntity.destroy();
                    }
                    player.removeComponent(SnowParticleComponent.class);
                }
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onPeriodicActionTriggered(PeriodicActionTriggeredEvent event, EntityRef unusedEntity) {
        if (event.getActionId().equals(FROSTBITE_DAMAGE_ACTION_ID)) {
            for (EntityRef entity : entityManager.getEntitiesWith(LocationComponent.class, AliveCharacterComponent.class)) {
                    // Check to see if health should be decreased
                    LocationComponent location = entity.getComponent(LocationComponent.class);
                    float height = location.getLocalPosition().getY();
                    //   float deltaHeight = location.getLastPosition().getY();
                    // float lastHeight = height - deltaHeight;
                    if (height > thresholdHeight) {
                        Prefab frostbiteDamagePrefab = prefabManager.getPrefab("ClimateConditions:FrostbiteDamage");
                        entity.send(new DoDamageEvent(healthDecreaseAmount, frostbiteDamagePrefab));
                    }
            }
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_NORMAL + 10 )
    public void onHealthRegen(ActivateRegenEvent event, EntityRef player, LocationComponent location) {
        float height = location.getLocalPosition().getY();
        if(height > thresholdHeight){
            player.send(new DeactivateRegenEvent());
        }
    }
    @ReceiveEvent
    public void OnPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, CharacterMovementComponent movement) {
        defaultRunFactor = movement.runFactor;;
        defaultSpeedMultiplier = movement.speedMultiplier;
        defaultJumpSpeed = movement.jumpSpeed;
    }

    @ReceiveEvent
    public void OnSpeedReduce(ReduceSpeedEvent event, EntityRef player, CharacterMovementComponent movement){
        movement.runFactor *= reducedRunFactorMultiplier;
        movement.speedMultiplier *= reducedSpeedMultiplierMultiplier;
        movement.jumpSpeed *= reducedJumpSpeedMultiplier;
    }

    @ReceiveEvent
    public void OnChangeSpeedToDefault(ChangeSpeedToDefaultEvent event, EntityRef player, CharacterMovementComponent movement){
        movement.runFactor /= reducedRunFactorMultiplier;
        movement.speedMultiplier /= reducedSpeedMultiplierMultiplier;
        movement.jumpSpeed /= reducedJumpSpeedMultiplier;
    }

}
