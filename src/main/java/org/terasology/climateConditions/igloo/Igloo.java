// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.climateConditions.igloo;

/**
 * Igloos are placed only in the Snow Biome.
 * They are very rare and represent remaining traces of abandoned settlements in the Snow Biome.
 */
public class Igloo {
    /**
     * Used by {@link IglooProvider} to reserve borders for the Igloo Facet.
     * NOTE: The SIZE attribute has directly been obtained by measuring the extents of the Igloo Structure Template
     * in-game and must be carefully measured for every structure template. Not doing so may result in improper
     * formation of the structure or may even lead to loads of NPE/ Out of Bounds exceptions during world generation.
     */
    public static final int SIZE = 13;
}
