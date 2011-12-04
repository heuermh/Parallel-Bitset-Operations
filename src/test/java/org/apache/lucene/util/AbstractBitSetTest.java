/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Abstract unit test for subclasses of AbstractBitSet.
 */
public abstract class AbstractBitSetTest {
    protected AbstractBitSet bitset;

    /**
     * Create and return a new instance of a subclass of AbstractBitSet to test.
     *
     * @return a new instance of a subclass of AbstractBitSet to test
     */
    protected abstract AbstractBitSet createBitSet();

    @Before
    public void setUp() {
        bitset = createBitSet();
    }

    @Test
    public void testCreateBitSet() {
        assertNotNull(bitset);
    }

    @Test(expected=NullPointerException.class)
    public void testIntersectsNullOther() {
        bitset.intersects(null);
    }

    @Test(expected=NullPointerException.class)
    public void testIntersectNullOther() {
        bitset.intersect(null);
    }

    @Test(expected=NullPointerException.class)
    public void testUnionNullOther() {
        bitset.union(null);
    }

    @Test(expected=NullPointerException.class)
    public void testRemoveNullOther() {
        bitset.remove(null);
    }

    @Test(expected=NullPointerException.class)
    public void testXorNullOther() {
        bitset.xor(null);
    }

    @Test(expected=NullPointerException.class)
    public void testAndNullOther() {
        bitset.and(null);
    }

    @Test(expected=NullPointerException.class)
    public void testOrNullOther() {
        bitset.or(null);
    }

    @Test(expected=NullPointerException.class)
    public void testAndNotNullOther() {
        bitset.andNot(null);
    }
}