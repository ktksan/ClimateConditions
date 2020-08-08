// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions.igloo;

import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generator.plugin.RegisterPlugin;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.Map;
import java.util.Objects;

@RegisterPlugin
public class IglooRasterizer implements WorldRasterizerPlugin {
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;

    /**
     * Stores the prefab of the Igloo Structure Template.
     */
    Prefab iglooStructure;

    @Override
    public void initialize() {
        iglooStructure = Objects.requireNonNull(CoreRegistry.get(PrefabManager.class)).getPrefab("ClimateConditions:Igloo");
    }

    /**
     * Places various blocks in required regions relative to the base position in order to recreate the Igloo Structure
     * at the time of world gen.
     */
    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        IglooFacet structureFacet = chunkRegion.getFacet(IglooFacet.class);
        SpawnBlockRegionsComponent spawnBlockRegionsComponent =
                iglooStructure.getComponent(SpawnBlockRegionsComponent.class);

        for (Map.Entry<BaseVector3i, Igloo> entry : structureFacet.getWorldEntries().entrySet()) {
            //Base Position is the corner position for the Igloo Structure Template.
            Vector3i basePosition = new Vector3i(entry.getKey());
            // Fill blocks in the required regions.
            for (SpawnBlockRegionsComponent.RegionToFill regionToFill : spawnBlockRegionsComponent.regionsToFill) {
                Block block = regionToFill.blockType;
                Region3i region = regionToFill.region;
                for (Vector3i pos : region) {
                    // pos is the position vector relative to the origin block of the Structural Template.
                    pos.add(basePosition);
                    if (chunkRegion.getRegion().encompasses(pos)) {
                        chunk.setBlock(ChunkMath.calcRelativeBlockPos(pos), block);
                    }
                }
            }
        }
    }
}
