/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.imaging.palette;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.imaging.ImagingException;

public class MostPopulatedBoxesMedianCut implements MedianCut {

    /**
     * Requirements to be tested:
     * (✅-Covered, ❌-Missed, 👍-Appended)
     * ✅ Ensure the function successfully performs a median cut when given a valid list of ColorGroup objects.
     * ✅ Verify that the function returns true when a cut is performed and false when no cut is possible.
     * ✅ Test when the most populated color group is found and successfully split.
     * 👍 Test when no valid color group is found, leading to an early return of false.
     * 👍 Test when only single color group is found, leading to an early return of false.
     * ✅ A single-color image (whiteImage) ensures that no unnecessary cuts are made.
     * ✅ A two-color image (whiteAndBlackImage) verifies a simple split scenario.
     * ✅ A multi-color image (rainbowImage) tests how colors are grouped when more than two colors exist.
     * ❌ Handling of extreme cases (e.g., all pixels being the same, forcing edge case sorting logic).
     * ❌ Verify sorting behavior by ensuring different ColorComponent values (RED, GREEN, BLUE, ALPHA) are correctly evaluated.
     * 👍 Test cases where the median index lands at the end of the list to ensure proper adjustments.
     * ❌ Check cases that could cause an IllegalArgumentException or an ImagingException, such as an empty colorGroups list or an invalid ColorComponent
     * ✅ Simulate multiple consecutive median cuts to ensure stability and correctness over repeated function calls.
     * ❌ The function iterates over color component ALPHA.
     * ✅ The function iterates over color component RED.
     * 👍 The function iterates over color component GREEN.
     * ❌ The function iterates over color component BLUE.
     * @param colorGroups
     * @param ignoreAlpha
     * @return if the next median cut can be performed
     * @throws ImagingException
     */
    @Override
    public boolean performNextMedianCut(final List<ColorGroup> colorGroups, final boolean ignoreAlpha) throws ImagingException {
        int maxPoints = 0;
        ColorGroup colorGroup = null;
        for (final ColorGroup group : colorGroups) {
            if (group.maxDiff > 0 && group.totalPoints > maxPoints) {
                colorGroup = group;
                maxPoints = group.totalPoints;
            }
        }
        if (colorGroup == null) {
            return false;
        }

        final List<ColorCount> colorCounts = colorGroup.getColorCounts();

        double bestScore = Double.MAX_VALUE;
        ColorComponent bestColorComponent = null;
        int bestMedianIndex = -1;
        for (final ColorComponent colorComponent : ColorComponent.values()) {
            if (ignoreAlpha && colorComponent == ColorComponent.ALPHA) {
                continue;
            }
            colorCounts.sort(new ColorCountComparator(colorComponent));
            final int countHalf = (int) Math.round((double) colorGroup.totalPoints / 2);
            int oldCount = 0;
            int newCount = 0;
            int medianIndex;
            for (medianIndex = 0; medianIndex < colorCounts.size(); medianIndex++) {
                final ColorCount colorCount = colorCounts.get(medianIndex);

                newCount += colorCount.count;

                if (newCount >= countHalf) {
                    break;
                }
                oldCount = newCount;
            }
            if (medianIndex == colorCounts.size() - 1) {
                medianIndex--;
            } else if (medianIndex > 0) {
                final int newDiff = Math.abs(newCount - countHalf);
                final int oldDiff = Math.abs(countHalf - oldCount);
                if (oldDiff < newDiff) {
                    medianIndex--;
                }
            }

            final List<ColorCount> lowerColors = new ArrayList<>(colorCounts.subList(0, medianIndex + 1));
            final List<ColorCount> upperColors = new ArrayList<>(colorCounts.subList(medianIndex + 1, colorCounts.size()));
            if (lowerColors.isEmpty() || upperColors.isEmpty()) {
                continue;
            }
            final ColorGroup lowerGroup = new ColorGroup(lowerColors, ignoreAlpha);
            final ColorGroup upperGroup = new ColorGroup(upperColors, ignoreAlpha);
            final int diff = Math.abs(lowerGroup.totalPoints - upperGroup.totalPoints);
            final double score = diff / (double) Math.max(lowerGroup.totalPoints, upperGroup.totalPoints);
            if (score < bestScore) {
                bestScore = score;
                bestColorComponent = colorComponent;
                bestMedianIndex = medianIndex;
            }
        }

        if (bestColorComponent == null) {
            return false;
        }

        colorCounts.sort(new ColorCountComparator(bestColorComponent));
        final List<ColorCount> lowerColors = new ArrayList<>(colorCounts.subList(0, bestMedianIndex + 1));
        final List<ColorCount> upperColors = new ArrayList<>(colorCounts.subList(bestMedianIndex + 1, colorCounts.size()));
        final ColorGroup lowerGroup = new ColorGroup(lowerColors, ignoreAlpha);
        final ColorGroup upperGroup = new ColorGroup(upperColors, ignoreAlpha);
        colorGroups.remove(colorGroup);
        colorGroups.add(lowerGroup);
        colorGroups.add(upperGroup);

        final ColorCount medianValue = colorCounts.get(bestMedianIndex);
        final int limit;
        switch (bestColorComponent) {
            case ALPHA:
                limit = medianValue.alpha;
                break;
            case RED:
                limit = medianValue.red;
                break;
            case GREEN:
                limit = medianValue.green;
                break;
            case BLUE:
                limit = medianValue.blue;
                break;
            default:
                throw new IllegalArgumentException("Bad mode: " + bestColorComponent);
        }
        colorGroup.cut = new ColorGroupCut(lowerGroup, upperGroup, bestColorComponent, limit);
        return true;
    }
}