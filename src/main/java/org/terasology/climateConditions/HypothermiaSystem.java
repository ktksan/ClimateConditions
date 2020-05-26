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

import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.physics.events.MovedEvent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HypothermiaSystem extends BaseComponentSystem {
    private float thresholdHeight = 60f;

    @ReceiveEvent(components = {PlayerCharacterComponent.class, CharacterMovementComponent.class})
    public void addHypothermia(MovedEvent event, EntityRef player, LocationComponent location, CharacterMovementComponent movement) {
        float height = event.getPosition().getY();
        float deltaHeight = event.getDelta().getY();
        float lastHeight = height - deltaHeight;
        if (height > thresholdHeight && lastHeight <= thresholdHeight) {
                if(!player.hasComponent(HypothermiaComponent.class)){
                    player.addOrSaveComponent(new HypothermiaComponent());
                }
                player.send(new HypothermiaTriggeredEvent());
        }
        if (height < thresholdHeight && lastHeight >= thresholdHeight) {
            if(player.hasComponent(HypothermiaComponent.class)){
                player.removeComponent(HypothermiaComponent.class);
            }
        }
    }
    /*
    //Definition of danger zone undecided.
    @ReceiveEvent
    public void OnSnowBiomeEntered(OnBiomeChangedEvent event, EntityRef player){
            if(event.getNewBiome().getDisplayName()=="Snow"){
                if(!player.hasComponent(HypothermiaComponent.class)){
                    player.addOrSaveComponent(new HypothermiaComponent());
                }
        }
    }

     */
    @ReceiveEvent
    public void onHypothermiaTriggered(HypothermiaTriggeredEvent event, EntityRef player){
        if(player.hasComponent(HypothermiaComponent.class)){

        }
    }
}
