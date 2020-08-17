// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions.igloo;

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Requires({
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = Igloo.SIZE * 2)),
        @Facet(value = BiomeFacet.class, border = @FacetBorder(sides = Igloo.SIZE * 2)),
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = Igloo.SIZE * 2))
})
@Produces(IglooFacet.class)
public class IglooProvider implements FacetProviderPlugin {
    private Noise noise;
    private static final int ARBITRARY_OVERLAP_OFFSET = 3;
    private static final int SNOW_BIOME_THRESHOLD = 96;
    //TODO: Get the snow biome threshold from the SolidRasterizer in CoreWorlds.

    @Override
    public void setSeed(long seed) {
        noise = new WhiteNoise(seed + ARBITRARY_OVERLAP_OFFSET);
    }

    /**
     * Places the Igloo Structure in the Snow Biome with a very low probability of spawn.
     */
    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(IglooFacet.class).extendBy(0, Igloo.SIZE * 2,
                Igloo.SIZE * 2);

        IglooFacet facet = new IglooFacet(region.getRegion(), border);
        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        BiomeFacet biomeFacet = region.getRegionFacet(BiomeFacet.class);

        Rect2i worldRegion = surfaceHeightFacet.getWorldRegion();

        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(surfaceHeightFacet.getWorld(wx, wz));
                int seaLevel = seaLevelFacet.getSeaLevel();
                // check if height is within this region
                if (surfaceHeight >= facet.getWorldRegion().minY() &&
                        surfaceHeight <= facet.getWorldRegion().maxY()) {
                    // Sea Level + 96 is the height at which snow blocks are placed in all biomes.
                    if (noise.noise(wx, wz) > 0.9999f && (surfaceHeight >= seaLevel + SNOW_BIOME_THRESHOLD ||
                            biomeFacet.getWorld(wx, wz).getId().equals(CoreBiome.SNOW))) {
                        int lowestY = getLowestY(surfaceHeightFacet, new Vector2i(wx, wz),
                                Igloo.SIZE, Igloo.SIZE);
                        if (lowestY >= facet.getWorldRegion().minY()
                                && lowestY <= facet.getWorldRegion().maxY()) {
                            facet.setWorld(wx, lowestY, wz, new Igloo());
                        }
                    }
                }
            }
        }
        region.setRegionFacet(IglooFacet.class, facet);
    }

    /**
     * Calculates the lowest y in the rectangular area where the igloo has to be placed. This is then used to clear out
     * the area so as to have a leveled ground to place the igloo template.
     */
    private int getLowestY(BaseFieldFacet2D facet, Vector2i basePosition, int sizeX, int sizeY) {
        //The basePosition is the cornerPosition for the Igloo Structure Template.
        Vector2i stepX = new Vector2i(1, 0);
        Vector2i stepY = new Vector2i(0, 1);
        Vector2i start = new Vector2i(basePosition);
        Vector2i end = new Vector2i(start).add(sizeX, sizeY);
        int lowestY = Integer.MAX_VALUE;
        // Iterates on various positions in the area between the start and end point to obtain the minimum Y
        // coordinate for the area.
        for (Vector2i pos = new Vector2i(start); pos.x <= end.x; pos.add(stepX)) {
            for (pos.setY(start.y); pos.y <= end.y; pos.add(stepY)) {
                if (facet.getWorldRegion().contains(pos)) {
                    int y = (int) facet.getWorld(pos);
                    lowestY = Math.min(y, lowestY);
                }
            }
        }
        return lowestY;
    }
}
