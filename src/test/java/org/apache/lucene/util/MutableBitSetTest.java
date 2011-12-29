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

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
    public void testAndCount() {
        assertEquals(0L, MutableBitSet.andCount(empty, empty));
        assertEquals(0L, MutableBitSet.andCount(empty, partial));
        assertEquals(0L, MutableBitSet.andCount(empty, full));
        assertEquals(0L, MutableBitSet.andCount(partial, empty));
        assertEquals((N / 2L), MutableBitSet.andCount(partial, partial));
        assertEquals((N / 2L), MutableBitSet.andCount(partial, full));
        assertEquals(0L, MutableBitSet.andCount(full, empty));
        assertEquals((N / 2L), MutableBitSet.andCount(full, partial));
        assertEquals(N, MutableBitSet.andCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testAndCountNullA() {
        MutableBitSet.andCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testAndCountNullB() {
        MutableBitSet.andCount(empty, null);
    }

    @Test
    public void testOrCount() {
        assertEquals(0L, MutableBitSet.orCount(empty, empty));
        assertEquals((N / 2L), MutableBitSet.orCount(empty, partial));
        assertEquals(N, MutableBitSet.orCount(empty, full));
        assertEquals((N / 2L), MutableBitSet.orCount(partial, empty));
        assertEquals((N / 2L), MutableBitSet.orCount(partial, partial));
        assertEquals(N, MutableBitSet.orCount(partial, full));
        assertEquals(N, MutableBitSet.orCount(full, empty));
        assertEquals(N, MutableBitSet.orCount(full, partial));
        assertEquals(N, MutableBitSet.orCount(full, full));
    }

    @Test(expected=NullPointerException.class)
    public void testOrCountNullA() {
        MutableBitSet.orCount(null, empty);
    }

    @Test(expected=NullPointerException.class)
    public void testOrCountNullB() {
        MutableBitSet.orCount(empty, null);
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

    @Test
    public void testSerializable() {
        assertTrue(bitset instanceof Serializable);
    }

    @Test
    public void testSerializeEmpty() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(empty);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Object dest = in.readObject();
        in.close();

        assertEquals(empty, (MutableBitSet) dest);
    }

    @Test
    public void testSerializeFull() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(full);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Object dest = in.readObject();
        in.close();

        assertEquals(full, (MutableBitSet) dest);
    }

    @Test
    public void testSerializePartial() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(partial);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Object dest = in.readObject();
        in.close();

        assertEquals(partial, (MutableBitSet) dest);
    }

    @Test
    public void testSerializeHalf() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(half);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Object dest = in.readObject();
        in.close();

        assertEquals(half, (MutableBitSet) dest);
    }
}