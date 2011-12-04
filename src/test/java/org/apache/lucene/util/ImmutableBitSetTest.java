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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for ImmutableBitSet.
 */
public final class ImmutableBitSetTest extends AbstractBitSetTest {
    private static final long N = 64L;
    private static ImmutableBitSet empty;
    private static ImmutableBitSet full;
    private static ImmutableBitSet partial;

    @Override
    protected AbstractBitSet createBitSet() {
        return empty;
    }

    @BeforeClass
    public static void createBitSets() {
        MutableBitSet m = new MutableBitSet(N);
        empty = m.immutableCopy();

        m.set(0L, (N / 2L));
        partial = m.immutableCopy();

        m.set(0L, N);
        full = m.immutableCopy(); 
    }

    @Test
    public void testCapacity() {
        assertEquals(N, empty.capacity());
        assertEquals(N, partial.capacity());
        assertEquals(N, full.capacity());
    }

    @Test
    public void testCardinality() {
        assertEquals(0L, empty.cardinality());
        assertEquals(N / 2L, partial.cardinality());
        assertEquals(N, full.cardinality());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(empty.isEmpty());
        assertFalse(partial.isEmpty());
        assertFalse(full.isEmpty());
    }

    @Test
    public void testGet() {
        for (long i = 0; i < N; i++) {
            assertFalse(empty.get(i));
            if (i < (N / 2L)) {
                assertFalse(partial.get(i));
            }
            else {
                assertTrue(partial.get(i));
            }
            assertTrue(full.get(i));
        }
    }

    @Test
    public void testGetQuick() {
        for (long i = 0; i < N; i++) {
            assertFalse(empty.getQuick(i));
            if (i < (N / 2L)) {
                assertFalse(partial.getQuick(i));
            }
            else {
                assertTrue(partial.getQuick(i));
            }
            assertTrue(full.getQuick(i));
        }
    }

    @Test
    public void testNextSetBit() {
        assertEquals(-1L, empty.nextSetBit(0L));
    }

    @Test
    public void testPrevSetBit() {
        assertEquals(-1L, empty.prevSetBit(0L));
    }

    @Test
    public void testIntersects() {
        assertFalse(empty.intersects(empty));
        assertFalse(empty.intersects(partial));
        assertFalse(empty.intersects(full));

        assertFalse(partial.intersects(empty));
        assertTrue(partial.intersects(partial));
        assertTrue(partial.intersects(full));

        assertFalse(full.intersects(empty));
        assertTrue(full.intersects(partial));
        assertTrue(full.intersects(full));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSetThrowsUnsupportedOperationException() {
        bitset.set(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSetRangeThrowsUnsupportedOperationException() {
        bitset.set(0L, 1L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSetQuickThrowsUnsupportedOperationException() {
        bitset.setQuick(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testClearThrowsUnsupportedOperationException() {
        bitset.clear(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testClearRangeThrowsUnsupportedOperationException() {
        bitset.clear(0L, 1L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testClearQuickThrowsUnsupportedOperationException() {
        bitset.clearQuick(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFlipThrowsUnsupportedOperationException() {
        bitset.flip(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFlipRangeThrowsUnsupportedOperationException() {
        bitset.flip(0L, 1L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFlipQuickThrowsUnsupportedOperationException() {
        bitset.flipQuick(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFlipAndGetThrowsUnsupportedOperationException() {
        bitset.flipAndGet(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testEnsureCapacityThrowsUnsupportedOperationException() {
        bitset.ensureCapacity(0L);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testTrimTrailingZerosThrowsUnsupportedOperationException() {
        bitset.trimTrailingZeros();
    }

    @Test
    public void testIntersect() {
    }

    @Test
    public void testUnion() {
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testXor() {
    }

    @Test
    public void testAnd() {
    }

    @Test
    public void testOr() {
    }

    @Test
    public void testAndNot() {
    }
}