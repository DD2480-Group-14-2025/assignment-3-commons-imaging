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
     * According to lizard, the original CCN is 18. This version of refactoring decreases it to 3.
     * @param colorGroups
     * @param ignoreAlpha
     * @return if next median cut is performed successfully
     * @throws ImagingException
     */
    @Override
    public boolean performNextMedianCut(final List<ColorGroup> colorGroups, final boolean ignoreAlpha) throws ImagingException {
        ColorGroup colorGroup = findMaxPointsColorGroup(colorGroups);
        if (colorGroup == null) {
            return false;
        }

        BestCutResult bestCutResult = calculateBestMedianCut(colorGroup, ignoreAlpha);
        if (bestCutResult.bestColorComponent == null) {
            return false;
        }

        applyBestCut(colorGroups, colorGroup, bestCutResult, ignoreAlpha);
        return true;
    }

    private ColorGroup findMaxPointsColorGroup(final List<ColorGroup> colorGroups) {
        int maxPoints = 0;
        ColorGroup colorGroup = null;
        for (final ColorGroup group : colorGroups) {
            if (group.maxDiff > 0 && group.totalPoints > maxPoints) {
                colorGroup = group;
                maxPoints = group.totalPoints;
            }
        }
        return colorGroup;
    }

    private BestCutResult calculateBestMedianCut(final ColorGroup colorGroup, final boolean ignoreAlpha) throws ImagingException {
        double bestScore = Double.MAX_VALUE;
        ColorComponent bestColorComponent = null;
        int bestMedianIndex = -1;
        List<ColorCount> colorCounts = colorGroup.getColorCounts();

        for (final ColorComponent colorComponent : ColorComponent.values()) {
            if (ignoreAlpha && colorComponent == ColorComponent.ALPHA) {
                continue;
            }
            colorCounts.sort(new ColorCountComparator(colorComponent));
            int medianIndex = findMedianIndex(colorCounts, colorGroup.totalPoints);

            List<ColorCount> lowerColors = colorCounts.subList(0, medianIndex + 1);
            List<ColorCount> upperColors = colorCounts.subList(medianIndex + 1, colorCounts.size());
            if (lowerColors.isEmpty() || upperColors.isEmpty()) {
                continue;
            }

            double score = calculateScore(lowerColors, upperColors, ignoreAlpha);
            if (score < bestScore) {
                bestScore = score;
                bestColorComponent = colorComponent;
                bestMedianIndex = medianIndex;
            }
        }
        return new BestCutResult(bestScore, bestColorComponent, bestMedianIndex);
    }

    private int findMedianIndex(final List<ColorCount> colorCounts, final int totalPoints) {
        final int countHalf = (int) Math.round((double) totalPoints / 2);
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
        return medianIndex;
    }

    private double calculateScore(final List<ColorCount> lowerColors, final List<ColorCount> upperColors, boolean ignoreAlpha) throws ImagingException {
        ColorGroup lowerGroup = new ColorGroup(lowerColors, ignoreAlpha);
        ColorGroup upperGroup = new ColorGroup(upperColors, ignoreAlpha);
        int diff = Math.abs(lowerGroup.totalPoints - upperGroup.totalPoints);
        return diff / (double) Math.max(lowerGroup.totalPoints, upperGroup.totalPoints);
    }

    private void applyBestCut(final List<ColorGroup> colorGroups, final ColorGroup colorGroup, final BestCutResult bestCutResult, final boolean ignoreAlpha) throws ImagingException {
        List<ColorCount> colorCounts = colorGroup.getColorCounts();
        colorCounts.sort(new ColorCountComparator(bestCutResult.bestColorComponent));
        List<ColorCount> lowerColors = new ArrayList<>(colorCounts.subList(0, bestCutResult.bestMedianIndex + 1));
        List<ColorCount> upperColors = new ArrayList<>(colorCounts.subList(bestCutResult.bestMedianIndex + 1, colorCounts.size()));
        ColorGroup lowerGroup = new ColorGroup(lowerColors, ignoreAlpha);
        ColorGroup upperGroup = new ColorGroup(upperColors, ignoreAlpha);
        colorGroups.remove(colorGroup);
        colorGroups.add(lowerGroup);
        colorGroups.add(upperGroup);

        ColorCount medianValue = colorCounts.get(bestCutResult.bestMedianIndex);
        int limit = determineLimit(bestCutResult.bestColorComponent, medianValue);
        colorGroup.cut = new ColorGroupCut(lowerGroup, upperGroup, bestCutResult.bestColorComponent, limit);
    }

    private int determineLimit(ColorComponent bestColorComponent, ColorCount medianValue) {
        switch (bestColorComponent) {
            case ALPHA:
                return medianValue.alpha;
            case RED:
                return medianValue.red;
            case GREEN:
                return medianValue.green;
            case BLUE:
                return medianValue.blue;
            default:
                throw new IllegalArgumentException("Bad mode: " + bestColorComponent);
        }
    }

    private static class BestCutResult {
        double bestScore;
        ColorComponent bestColorComponent;
        int bestMedianIndex;

        BestCutResult(double bestScore, ColorComponent bestColorComponent, int bestMedianIndex) {
            this.bestScore = bestScore;
            this.bestColorComponent = bestColorComponent;
            this.bestMedianIndex = bestMedianIndex;
        }
    }
}
