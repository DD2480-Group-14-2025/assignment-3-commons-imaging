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

package org.apache;

import java.util.Arrays;

public class CoverageTester {

    // Ensuring 100 capacity, the arraylist can then later be trimmed down.
    private static int[] branches;
    private static int totalRuns;

    /**
     * Initialize the branch tracker
     * @param totBranches total amount of branches in the function to test
     */
    public static void initializeBranches(int totBranches) {
        branches = new int[totBranches];
        Arrays.fill(branches, 0);
    }

    public static void increaseTotalRuns() {
        totalRuns++;
    }

    public static void addBranchTaken(int branch) {
        branches[branch]++;
    }

    public static int[] getBranches() {
        return branches;
    }

    public static int getTotalRuns() {
        return totalRuns;
    }
}
