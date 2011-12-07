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

/**
 * Abstract bit set.
 */
public abstract class AbstractBitSet {
    protected abstract long[] bits();
    protected abstract long numBits();
    protected abstract int wlen();

    public abstract long capacity(); // 1 greater than the index of the last (set?) bit
    public abstract long cardinality(); // number of bits set to true
    public abstract boolean isEmpty(); // cardinality == 0
    public abstract boolean get(long index);
    public abstract boolean getQuick(long index);
    public abstract long nextSetBit(long index);
    public abstract long prevSetBit(long index);
    public abstract boolean intersects(AbstractBitSet other);
    //public abstract void forEach(Procedure<Long> procedure);

    // optional
    public abstract void set(long index);
    public abstract void set(long startIndex, long endIndex);
    public abstract void setQuick(long index);
    public abstract void clear(long index);
    public abstract void clear(long startIndex, long endIndex);
    public abstract void clearQuick(long index);
    public abstract boolean getAndSet(long index);
    public abstract void flip(long index);
    public abstract void flip(long startIndex, long endIndex);
    public abstract void flipQuick(long index);
    public abstract boolean flipAndGet(long index);
    public abstract void ensureCapacity(long numBits);
    public abstract void trimTrailingZeros();

    // may return this or a copy of this
    public abstract AbstractBitSet intersect(AbstractBitSet other); // just forwards to and
    public abstract AbstractBitSet union(AbstractBitSet other); // just forwards to or
    public abstract AbstractBitSet remove(AbstractBitSet other);
    public abstract AbstractBitSet xor(AbstractBitSet other);
    public abstract AbstractBitSet and(AbstractBitSet other);
    public abstract AbstractBitSet or(AbstractBitSet other);
    public abstract AbstractBitSet andNot(AbstractBitSet other); // just forwards to remove

    // from RamUsageEstimator.java
    private final static int NUM_BYTES_LONG = 8;

    // from ArrayUtil.java
    protected static long[] grow(final long[] array, final int minSize) {
        assert minSize >= 0 : "size must be positive (got " + minSize + "): likely integer overflow?";
        if (array.length < minSize) {
            long[] newArray = new long[oversize(minSize, NUM_BYTES_LONG)];
            System.arraycopy(array, 0, newArray, 0, array.length);
            return newArray;
        }
        else {
            return array;
        }
    }

    private static int oversize(final int minTargetSize, final int bytesPerElement) {
        if (minTargetSize < 0) {
            // catch usage that accidentally overflows int
            throw new IllegalArgumentException("invalid array size " + minTargetSize);
        }

        if (minTargetSize == 0) {
            // wait until at least one element is requested
            return 0;
        }

        // asymptotic exponential growth by 1/8th, favors
        // spending a bit more CPU to not tie up too much wasted
        // RAM:
        int extra = minTargetSize >> 3;

        if (extra < 3) {
            // for very small arrays, where constant overhead of
            // realloc is presumably relatively high, we grow
            // faster
            extra = 3;
        }

        int newSize = minTargetSize + extra;

        // add 7 to allow for worst case byte alignment addition below:
        if (newSize + 7 < 0) {
            // int overflowed -- return max allowed array size
            return Integer.MAX_VALUE;
        }

        if (JRE_IS_64BIT) {
            // round up to 8 byte alignment in 64bit env
            switch (bytesPerElement) {
            case 4:
                // round up to multiple of 2
                return (newSize + 1) & 0x7ffffffe;
            case 2:
                // round up to multiple of 4
                return (newSize + 3) & 0x7ffffffc;
            case 1:
                // round up to multiple of 8
                return (newSize + 7) & 0x7ffffff8;
            case 8:
                // no rounding
            default:
                // odd (invalid?) size
                return newSize;
            }
        }
        else {
            // round up to 4 byte alignment in 64bit env
            switch (bytesPerElement) {
            case 2:
                // round up to multiple of 2
                return (newSize + 1) & 0x7ffffffe;
            case 1:
                // round up to multiple of 4
                return (newSize + 3) & 0x7ffffffc;
            case 4:
            case 8:
                // no rounding
            default:
                // odd (invalid?) size
                return newSize;
            }
        }
    }

    // from Constants.java
    private static final String OS_ARCH = System.getProperty("os.arch");
    private static final boolean JRE_IS_64BIT;

    static {
        // NOTE: this logic may not be correct; if you know of a
        // more reliable approach please raise it on java-dev!
        final String x = System.getProperty("sun.arch.data.model");
        if (x != null) {
            JRE_IS_64BIT = x.indexOf("64") != -1;
        } else {
            if (OS_ARCH != null && OS_ARCH.indexOf("64") != -1) {
                JRE_IS_64BIT = true;
            } else {
                JRE_IS_64BIT = false;
            }
        }
    }
}