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

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for ImmutableBitSet.
 */
public final class ImmutableBitSetTest extends AbstractBitSetTest {
    private static final long N = 1024L;
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

        m.set((N / 2L), N);
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
        AbstractBitSet emptyIntersection = empty.intersect(empty);
        assertNotNull(emptyIntersection);
        assertNotSame(empty, emptyIntersection);
        assertTrue(emptyIntersection.isEmpty());

        assertTrue(empty.intersect(partial).isEmpty());
        assertTrue(empty.intersect(full).isEmpty());

        AbstractBitSet partialIntersection = partial.intersect(partial);
        assertNotNull(partialIntersection);
        assertNotSame(partial, partialIntersection);
        assertEquals((N / 2L), partialIntersection.cardinality());

        assertTrue(partial.intersect(empty).isEmpty());
        assertEquals((N / 2L), partial.intersect(full).cardinality());

        AbstractBitSet fullIntersection = full.intersect(full);
        assertNotNull(fullIntersection);
        assertNotSame(full, fullIntersection);
        assertEquals(N, fullIntersection.cardinality());

        assertTrue(full.intersect(empty).isEmpty());
        assertEquals((N / 2L), full.intersect(partial).cardinality());
    }

    @Test
    public void testUnion() {
        AbstractBitSet emptyUnion = empty.union(empty);
        assertNotNull(emptyUnion);
        assertNotSame(empty, emptyUnion);
        assertTrue(emptyUnion.isEmpty());

        assertEquals((N / 2L), empty.union(partial).cardinality());
        assertEquals(N, empty.union(full).cardinality());

        AbstractBitSet partialUnion = partial.union(partial);
        assertNotNull(partialUnion);
        assertNotSame(partial, partialUnion);
        assertEquals((N / 2L), partial.union(partial).cardinality());

        assertEquals((N / 2L), partial.union(empty).cardinality());
        assertEquals(N, partial.union(full).cardinality());

        MutableBitSet m = new MutableBitSet(N);
        m.set(0L, (N / 2L));
        assertEquals(N, partial.union(m).cardinality());

        AbstractBitSet fullUnion = full.union(full);
        assertNotNull(fullUnion);
        assertNotSame(full, fullUnion);
        assertEquals(N, fullUnion.cardinality());

        assertEquals(N, full.union(empty).cardinality());
        assertEquals(N, full.union(partial).cardinality());
    }

    @Test
    public void testRemove() {
        AbstractBitSet emptyRemove = empty.remove(empty);
        assertNotNull(emptyRemove);
        assertNotSame(empty, emptyRemove);
        assertTrue(emptyRemove.isEmpty());

        assertTrue(empty.remove(partial).isEmpty());
        assertTrue(empty.remove(full).isEmpty());

        AbstractBitSet partialRemove = partial.remove(partial);
        assertNotNull(partialRemove);
        assertNotSame(partial, partialRemove);
        assertTrue(partialRemove.isEmpty());

        assertEquals((N / 2L), partial.remove(empty).cardinality());
        assertTrue(partial.remove(full).isEmpty());

        AbstractBitSet fullRemove = full.remove(full);
        assertNotNull(fullRemove);
        assertNotSame(full, fullRemove);
        assertTrue(fullRemove.isEmpty());

        assertEquals(N, full.remove(empty).cardinality());
        assertEquals((N / 2L), full.remove(partial).cardinality());
    }

    @Test
    public void testXor() {
        AbstractBitSet emptyXor = empty.xor(empty);
        assertNotNull(emptyXor);
        assertNotSame(empty, emptyXor);
        assertTrue(emptyXor.isEmpty());

        assertEquals((N / 2L), empty.xor(partial).cardinality());
        assertEquals(N, empty.xor(full).cardinality());

        AbstractBitSet partialXor = partial.xor(partial);
        assertNotNull(partialXor);
        assertNotSame(partial, partialXor);
        assertTrue(partialXor.isEmpty());

        assertEquals((N / 2L), partial.xor(empty).cardinality());
        assertEquals((N / 2L), partial.xor(full).cardinality());

        AbstractBitSet fullXor = full.xor(full);
        assertNotNull(fullXor);
        assertNotSame(full, fullXor);
        assertTrue(fullXor.isEmpty());

        assertEquals(N, full.xor(empty).cardinality());
        assertEquals((N / 2L), full.xor(partial).cardinality());
    }

    @Test
    public void testAnd() {
        // just forwards to intersect
        AbstractBitSet emptyAnd = empty.and(empty);
        assertNotNull(emptyAnd);
        assertNotSame(empty, emptyAnd);
    }

    @Test
    public void testOr() {
        // just forwards to union
        AbstractBitSet emptyOr = empty.or(empty);
        assertNotNull(emptyOr);
        assertNotSame(empty, emptyOr);
    }

    @Test
    public void testAndNot() {
        // just forwards to remove
        AbstractBitSet emptyAndNot = empty.andNot(empty);
        assertNotNull(emptyAndNot);
        assertNotSame(empty, emptyAndNot);
    }

    @Test
    public void testMutableCopy() {
        MutableBitSet mutableEmpty = empty.mutableCopy();
        assertNotNull(mutableEmpty);
        assertTrue(mutableEmpty.isEmpty());

        MutableBitSet mutablePartial = partial.mutableCopy();
        assertNotNull(mutablePartial);
        assertEquals((N / 2L), mutablePartial.cardinality());

        MutableBitSet mutableFull = full.mutableCopy();
        assertNotNull(mutableFull);
        assertTrue(mutableFull.get(0L));
        assertTrue(mutableFull.get(N - 1L));
    }

    @Test
    public void testUnsafeCopy() {
        UnsafeBitSet unsafeEmpty = empty.unsafeCopy();
        assertNotNull(unsafeEmpty);
        assertTrue(unsafeEmpty.isEmpty());

        UnsafeBitSet unsafePartial = partial.unsafeCopy();
        assertNotNull(unsafePartial);
        assertEquals((N / 2L), unsafePartial.cardinality());

        UnsafeBitSet unsafeFull = full.unsafeCopy();
        assertNotNull(unsafeFull);
        assertTrue(unsafeFull.get(0L));
        assertTrue(unsafeFull.get(N - 1L));
    }

    @Test
    public void testAndNotCount() {
        assertEquals(0L, ImmutableBitSet.andNotCount(empty, empty));
        assertEquals(0L, ImmutableBitSet.andNotCount(empty, partial));
        assertEquals(0L, ImmutableBitSet.andNotCount(empty, full));
        assertEquals((N / 2L), ImmutableBitSet.andNotCount(partial, empty));
        assertEquals(0L, ImmutableBitSet.andNotCount(partial, partial));
        assertEquals(0L, ImmutableBitSet.andNotCount(partial, full));
        assertEquals(N, ImmutableBitSet.andNotCount(full, empty));
        assertEquals((N / 2L), ImmutableBitSet.andNotCount(full, partial));
        assertEquals(0L, ImmutableBitSet.andNotCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testAndNotCountNullA() {
        ImmutableBitSet.andNotCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testAndNotCountNullB() {
        ImmutableBitSet.andNotCount(empty, null);
    }

    @Test
    public void testIntersectionCount() {
        assertEquals(0L, ImmutableBitSet.intersectionCount(empty, empty));
        assertEquals(0L, ImmutableBitSet.intersectionCount(empty, partial));
        assertEquals(0L, ImmutableBitSet.intersectionCount(empty, full));
        assertEquals(0L, ImmutableBitSet.intersectionCount(partial, empty));
        assertEquals((N / 2L), ImmutableBitSet.intersectionCount(partial, partial));
        assertEquals((N / 2L), ImmutableBitSet.intersectionCount(partial, full));
        assertEquals(0L, ImmutableBitSet.intersectionCount(full, empty));
        assertEquals((N / 2L), ImmutableBitSet.intersectionCount(full, partial));
        assertEquals(N, ImmutableBitSet.intersectionCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testIntersectionCountNullA() {
        ImmutableBitSet.intersectionCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testIntersectionCountNullB() {
        ImmutableBitSet.intersectionCount(empty, null);
    }

    @Test
    public void testUnionCount() {
        assertEquals(0L, ImmutableBitSet.unionCount(empty, empty));
        assertEquals((N / 2L), ImmutableBitSet.unionCount(empty, partial));
        assertEquals(N, ImmutableBitSet.unionCount(empty, full));
        assertEquals((N / 2L), ImmutableBitSet.unionCount(partial, empty));
        assertEquals((N / 2L), ImmutableBitSet.unionCount(partial, partial));
        assertEquals(N, ImmutableBitSet.unionCount(partial, full));
        assertEquals(N, ImmutableBitSet.unionCount(full, empty));
        assertEquals(N, ImmutableBitSet.unionCount(full, partial));
        assertEquals(N, ImmutableBitSet.unionCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testUnionCountNullA() {
        ImmutableBitSet.unionCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testUnionCountNullB() {
        ImmutableBitSet.unionCount(empty, null);
    }

    @Test
    public void testXorCount() {
        assertEquals(0L, ImmutableBitSet.xorCount(empty, empty));
        assertEquals((N / 2L), ImmutableBitSet.xorCount(empty, partial));
        assertEquals(N, ImmutableBitSet.xorCount(empty, full));
        assertEquals((N / 2L), ImmutableBitSet.xorCount(partial, empty));
        assertEquals(0L, ImmutableBitSet.xorCount(partial, partial));
        assertEquals((N / 2L), ImmutableBitSet.xorCount(partial, full));
        assertEquals(N, ImmutableBitSet.xorCount(full, empty));
        assertEquals((N / 2L), ImmutableBitSet.xorCount(full, partial));
        assertEquals(0L, ImmutableBitSet.xorCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testXorCountNullA() {
        ImmutableBitSet.xorCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testXorCountNullB() {
        ImmutableBitSet.xorCount(empty, null);
    }
}