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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for MutableBitSet.
 */
public class MutableBitSetTest extends AbstractBitSetTest {
    private static final long N = 800L;
    private MutableBitSet empty;
    private MutableBitSet full;
    private MutableBitSet partial;
    private MutableBitSet half;

    @Override
    protected AbstractBitSet createBitSet() {
        return new MutableBitSet(N);
    }

    @Before
    public void setUp() {
        super.setUp();
        empty = new MutableBitSet(N);
        partial = new MutableBitSet(N);
        partial.set((N / 2L), N);
        full = new MutableBitSet(N);
        full.set(0L, N);
        half = new MutableBitSet((N / 2L));
        half.set((N / 4L), (N / 2L));
    }

    @Test
    public void testCapacity() {
        assertTrue(empty.capacity() >= N);
        assertTrue(partial.capacity() >= N);
        assertTrue(full.capacity() >= N);
        assertTrue(half.capacity() >= (N / 2L));
    }

    @Test
    public void testCardinality() {
        assertEquals(0L, empty.cardinality());
        assertEquals(N / 2L, partial.cardinality());
        assertEquals(N, full.cardinality());
        assertEquals(N / 4L, half.cardinality());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(empty.isEmpty());
        assertFalse(partial.isEmpty());
        assertFalse(full.isEmpty());
        assertFalse(half.isEmpty());

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
        assertEquals((N / 2L), partial.nextSetBit(0L));
        assertEquals(0L, full.nextSetBit(0L));

        assertEquals(-1L, empty.nextSetBit(N - 1L));
        assertEquals(N - 1L, partial.nextSetBit(N - 1L));
        assertEquals(N - 1L, full.nextSetBit(N - 1L));
    }

    @Test
    public void testPrevSetBit() {
        assertEquals(-1L, empty.prevSetBit(0L));
        assertEquals(-1L, partial.prevSetBit(0L));
        assertEquals(0L, full.prevSetBit(0L));

        assertEquals(-1L, empty.prevSetBit(N - 1L));
        assertEquals(N - 1L, partial.prevSetBit(N - 1L));
        assertEquals(N - 1L, full.prevSetBit(N - 1L));
    }

    @Test
    public void testIntersects() {
        assertFalse(empty.intersects(empty));
        assertFalse(empty.intersects(partial));
        assertFalse(empty.intersects(full));
        assertFalse(empty.intersects(half));

        assertFalse(partial.intersects(empty));
        assertTrue(partial.intersects(partial));
        assertTrue(partial.intersects(full));
        assertFalse(partial.intersects(half));

        assertFalse(full.intersects(empty));
        assertTrue(full.intersects(partial));
        assertTrue(full.intersects(full));
        assertTrue(full.intersects(half));

        assertFalse(half.intersects(empty));
        assertFalse(half.intersects(partial));
        assertTrue(half.intersects(full));
        assertTrue(half.intersects(half));
    }

    @Test
    public void testAndNotCount() {
        assertEquals(0L, MutableBitSet.andNotCount(empty, empty));
        assertEquals(0L, MutableBitSet.andNotCount(empty, partial));
        assertEquals(0L, MutableBitSet.andNotCount(empty, full));
        assertEquals((N / 2L), MutableBitSet.andNotCount(partial, empty));
        assertEquals(0L, MutableBitSet.andNotCount(partial, partial));
        assertEquals(0L, MutableBitSet.andNotCount(partial, full));
        assertEquals(N, MutableBitSet.andNotCount(full, empty));
        assertEquals((N / 2L), MutableBitSet.andNotCount(full, partial));
        assertEquals(0L, MutableBitSet.andNotCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testAndNotCountNullA() {
        MutableBitSet.andNotCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testAndNotCountNullB() {
        MutableBitSet.andNotCount(empty, null);
    }

    @Test
    public void testIntersectionCount() {
        assertEquals(0L, MutableBitSet.intersectionCount(empty, empty));
        assertEquals(0L, MutableBitSet.intersectionCount(empty, partial));
        assertEquals(0L, MutableBitSet.intersectionCount(empty, full));
        assertEquals(0L, MutableBitSet.intersectionCount(partial, empty));
        assertEquals((N / 2L), MutableBitSet.intersectionCount(partial, partial));
        assertEquals((N / 2L), MutableBitSet.intersectionCount(partial, full));
        assertEquals(0L, MutableBitSet.intersectionCount(full, empty));
        assertEquals((N / 2L), MutableBitSet.intersectionCount(full, partial));
        assertEquals(N, MutableBitSet.intersectionCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testIntersectionCountNullA() {
        MutableBitSet.intersectionCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testIntersectionCountNullB() {
        MutableBitSet.intersectionCount(empty, null);
    }

    @Test
    public void testUnionCount() {
        assertEquals(0L, MutableBitSet.unionCount(empty, empty));
        assertEquals((N / 2L), MutableBitSet.unionCount(empty, partial));
        assertEquals(N, MutableBitSet.unionCount(empty, full));
        assertEquals((N / 2L), MutableBitSet.unionCount(partial, empty));
        assertEquals((N / 2L), MutableBitSet.unionCount(partial, partial));
        assertEquals(N, MutableBitSet.unionCount(partial, full));
        assertEquals(N, MutableBitSet.unionCount(full, empty));
        assertEquals(N, MutableBitSet.unionCount(full, partial));
        assertEquals(N, MutableBitSet.unionCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testUnionCountNullA() {
        MutableBitSet.unionCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testUnionCountNullB() {
        MutableBitSet.unionCount(empty, null);
    }

    @Test
    public void testXorCount() {
        assertEquals(0L, MutableBitSet.xorCount(empty, empty));
        assertEquals((N / 2L), MutableBitSet.xorCount(empty, partial));
        assertEquals(N, MutableBitSet.xorCount(empty, full));
        assertEquals((N / 2L), MutableBitSet.xorCount(partial, empty));
        assertEquals(0L, MutableBitSet.xorCount(partial, partial));
        assertEquals((N / 2L), MutableBitSet.xorCount(partial, full));
        assertEquals(N, MutableBitSet.xorCount(full, empty));
        assertEquals((N / 2L), MutableBitSet.xorCount(full, partial));
        assertEquals(0L, MutableBitSet.xorCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testXorCountNullA() {
        MutableBitSet.xorCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testXorCountNullB() {
        MutableBitSet.xorCount(empty, null);
    }
}