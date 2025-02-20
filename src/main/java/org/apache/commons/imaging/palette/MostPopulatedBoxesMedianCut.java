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

import org.apache.CoverageTester;
import org.apache.commons.imaging.ImagingException;

public class MostPopulatedBoxesMedianCut implements MedianCut {

    @Override
    public boolean performNextMedianCut(final List<ColorGroup> colorGroups, final boolean ignoreAlpha) throws ImagingException {
        CoverageTester.increaseTotalRuns();
        int maxPoints = 0;
        ColorGroup colorGroup = null;
        for (final ColorGroup group : colorGroups) {
            CoverageTester.addBranchTaken(0);
            if (group.maxDiff > 0 && group.totalPoints > maxPoints) {
                CoverageTester.addBranchTaken(1);
                colorGroup = group;
                maxPoints = group.totalPoints;
            } else{
                CoverageTester.addBranchTaken(2);
            }
        }
        if (colorGroup == null) {
            CoverageTester.addBranchTaken(3);
            return false;
        }

        final List<ColorCount> colorCounts = colorGroup.getColorCounts();

        double bestScore = Double.MAX_VALUE;
        ColorComponent bestColorComponent = null;
        int bestMedianIndex = -1;
        for (final ColorComponent colorComponent : ColorComponent.values()) {
            CoverageTester.addBranchTaken(4);
            if (ignoreAlpha && colorComponent == ColorComponent.ALPHA) {
                CoverageTester.addBranchTaken(5);
                continue;
            }
            else {
                CoverageTester.addBranchTaken(6);
            }
            colorCounts.sort(new ColorCountComparator(colorComponent));
            final int countHalf = (int) Math.round((double) colorGroup.totalPoints / 2);
            int oldCount = 0;
            int newCount = 0;
            int medianIndex;
            for (medianIndex = 0; medianIndex < colorCounts.size(); medianIndex++) {
                CoverageTester.addBranchTaken(7);
                final ColorCount colorCount = colorCounts.get(medianIndex);

                newCount += colorCount.count;

                if (newCount >= countHalf) {
                    CoverageTester.addBranchTaken(8);
                    break;
                }
                else{
                    CoverageTester.addBranchTaken(9);
                }
                oldCount = newCount;
            }
            if (medianIndex == colorCounts.size() - 1) {
                CoverageTester.addBranchTaken(10);
                medianIndex--;
            } else if (medianIndex > 0) {
                CoverageTester.addBranchTaken(11);
                final int newDiff = Math.abs(newCount - countHalf);
                final int oldDiff = Math.abs(countHalf - oldCount);
                if (oldDiff < newDiff) {
                    CoverageTester.addBranchTaken(12);
                    medianIndex--;
                }
                else{
                    CoverageTester.addBranchTaken(13);
                }
            }
            else{
                CoverageTester.addBranchTaken(14);
            }

            final List<ColorCount> lowerColors = new ArrayList<>(colorCounts.subList(0, medianIndex + 1));
            final List<ColorCount> upperColors = new ArrayList<>(colorCounts.subList(medianIndex + 1, colorCounts.size()));
            if (lowerColors.isEmpty() || upperColors.isEmpty()) {
                CoverageTester.addBranchTaken(15);
                continue;
            }
            else{
                CoverageTester.addBranchTaken(16);
            }
            final ColorGroup lowerGroup = new ColorGroup(lowerColors, ignoreAlpha);
            final ColorGroup upperGroup = new ColorGroup(upperColors, ignoreAlpha);
            final int diff = Math.abs(lowerGroup.totalPoints - upperGroup.totalPoints);
            final double score = diff / (double) Math.max(lowerGroup.totalPoints, upperGroup.totalPoints);
            if (score < bestScore) {
                CoverageTester.addBranchTaken(17);
                bestScore = score;
                bestColorComponent = colorComponent;
                bestMedianIndex = medianIndex;
            }
            else{
                CoverageTester.addBranchTaken(18);
            }
        }

        if (bestColorComponent == null) {
            CoverageTester.addBranchTaken(19);
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
                CoverageTester.addBranchTaken(20);
                limit = medianValue.alpha;
                break;
            case RED:
                CoverageTester.addBranchTaken(21);
                limit = medianValue.red;
                break;
            case GREEN:
                CoverageTester.addBranchTaken(22);
                limit = medianValue.green;
                break;
            case BLUE:
                CoverageTester.addBranchTaken(23);
                limit = medianValue.blue;
                break;
            default:
                CoverageTester.addBranchTaken(24);
                throw new IllegalArgumentException("Bad mode: " + bestColorComponent);
        }
        colorGroup.cut = new ColorGroupCut(lowerGroup, upperGroup, bestColorComponent, limit);
        return true;
    }
}

