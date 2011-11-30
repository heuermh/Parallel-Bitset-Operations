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

import java.util.Arrays;

/**
 * Mutable bit set.
 */
public class MutableBitSet extends AbstractBitSet/* implements Cloneable, Serializable */ {
    /** Default number of bits, <code>64</code>. */
    public static final long DEFAULT_NUM_BITS = 64;

    protected long[] bits;
    protected long numBits;
    protected int wlen;


    /**
     * Create a new mutable bit set with the default number of bits.
     *
     * @see DEFAULT_NUM_BITS
     */
    public MutableBitSet() {
        this(DEFAULT_NUM_BITS);
    }

    /**
     * Create a new mutable bit set with the specified number of bits.
     *
     * @param numBits number of bits
     */
    public MutableBitSet(final long numBits) {
        this.numBits = numBits;
        bits = new long[bits2words(numBits)];
        wlen = bits.length;
    }

    /**
     * Create a new mutable bit set from the specified <code>long[]</code>.
     *
     * @param bits bits stored in <code>long[]</code>
     * @param wlen number of words/elements used in <code>bits</code>
     */
    public MutableBitSet(final long[] bits, final int wlen) {
        this(bits.clone(), wlen * 64, wlen);
    }

    /**
     * Create a new mutable bit set from the specified <code>long[]</code>.
     *
     * @param bits bits stored in <code>long[]</code>
     * @param numBits number of bits
     * @param wlen number of words/elements used in <code>bits</code>
     */
    protected MutableBitSet(final long[] bits, final long numBits, final int wlen) {
        this.bits = bits;
        this.wlen = wlen;
        this.numBits = numBits;
    }


    @Override
    public AbstractBitSet and(final AbstractBitSet other) {
        return intersect(other);
    }

    @Override
    public AbstractBitSet andNot(final AbstractBitSet other) {
        return remove(other);
    }

    @Override
    public long capacity() {
        return bits.length << 6;
    }

    @Override
    public long cardinality() {
        return BitUtil.pop_array(bits, 0, wlen);
    }

    @Override
    public void clear(final int startIndex, final int endIndex) {
        if (endIndex <= startIndex) {
            return;
        }

        int startWord = (startIndex >> 6);
        if (startWord >= wlen) {
            return;
        }

        // since endIndex is one past the end, this is index of the last
        // word to be changed.
        int endWord = ((endIndex - 1) >> 6);

        long startmask = -1L << startIndex;
        long endmask = -1L >>> -endIndex; // 64-(endIndex&0x3f) is the same as
                                          // -endIndex due to wrap

        // invert masks since we are clearing
        startmask = ~startmask;
        endmask = ~endmask;

        if (startWord == endWord) {
            bits[startWord] &= (startmask | endmask);
            return;
        }

        bits[startWord] &= startmask;

        int middle = Math.min(wlen, endWord);
        Arrays.fill(bits, startWord + 1, middle, 0L);
        if (endWord < wlen) {
            bits[endWord] &= endmask;
        }
    }

    @Override
    public void clear(final long index) {
        int wordNum = (int) (index >> 6); // div 64
        if (wordNum >= wlen) {
            return;
        }
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] &= ~bitmask;
    }

    @Override
    public void clear(final long startIndex, final long endIndex) {
        if (endIndex <= startIndex) {
            return;
        }

        int startWord = (int) (startIndex >> 6);
        if (startWord >= wlen) {
            return;
        }

        // since endIndex is one past the end, this is index of the last
        // word to be changed.
        int endWord = (int) ((endIndex - 1) >> 6);

        long startmask = -1L << startIndex;
        long endmask = -1L >>> -endIndex; // 64-(endIndex&0x3f) is the same as
                                          // -endIndex due to wrap

        // invert masks since we are clearing
        startmask = ~startmask;
        endmask = ~endmask;

        if (startWord == endWord) {
            bits[startWord] &= (startmask | endmask);
            return;
        }

        bits[startWord] &= startmask;

        int middle = Math.min(wlen, endWord);
        Arrays.fill(bits, startWord + 1, middle, 0L);
        if (endWord < wlen) {
            bits[endWord] &= endmask;
        }
    }

    @Override
    public void ensureCapacity(final long numBits) {
        ensureCapacityWords(bits2words(numBits));
    }

    @Override
    public void ensureCapacityWords(final int numWords) {
        if (bits.length < numWords) {
            bits = grow(bits, numWords);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MutableBitSet)) {
            return false;
        }
        MutableBitSet a;
        MutableBitSet b = (MutableBitSet) o;
        // make a the larger set.
        if (b.wlen > this.wlen) {
            a = b;
            b = this;
        }
        else {
            a = this;
        }

        // check for any set bits out of the range of b
        for (int i = a.wlen - 1; i >= b.wlen; i--) {
            if (a.bits[i] != 0) {
                return false;
            }
        }

        for (int i = b.wlen - 1; i >= 0; i--) {
            if (a.bits[i] != b.bits[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void fastClear(final int index) {
        assert index >= 0 && index < numBits;
        int wordNum = index >> 6;
        int bit = index & 0x03f;
        long bitmask = 1L << bit;
        bits[wordNum] &= ~bitmask;
        // hmmm, it takes one more instruction to clear than it does to set...
        // any way to work around this? If there were only 63 bits per word, we
        // could use a right shift of 10111111...111 in binary to position the 0 in
        // the correct place (using sign extension).
        // Could also use Long.rotateRight() or rotateLeft() *if* they were
        // converted by the JVM into a native instruction.
        // bits[word] &= Long.rotateLeft(0xfffffffe,bit);
    }

    @Override
    public void fastClear(final long index) {
        assert index >= 0 && index < numBits;
        int wordNum = (int) (index >> 6); // div 64
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] &= ~bitmask;
    }

    @Override
    public void fastFlip(final int index) {
        assert index >= 0 && index < numBits;
        int wordNum = index >> 6; // div 64
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] ^= bitmask;
    }

    @Override
    public void fastFlip(final long index) {
        assert index >= 0 && index < numBits;
        int wordNum = (int) (index >> 6); // div 64
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] ^= bitmask;
    }

    @Override
    public boolean fastGet(final int index) {
        assert index >= 0 && index < numBits;
        int i = index >> 6; // div 64
        // signed shift will keep a negative index and force an
        // array-index-out-of-bounds-exception, removing the need for an
        // explicit check.
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        return (bits[i] & bitmask) != 0;
    }

    @Override
    public boolean fastGet(final long index) {
        assert index >= 0 && index < numBits;
        int i = (int) (index >> 6); // div 64
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        return (bits[i] & bitmask) != 0;
    }

    @Override
    public void fastSet(final int index) {
        assert index >= 0 && index < numBits;
        int wordNum = index >> 6; // div 64
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] |= bitmask;
    }

    @Override
    public void fastSet(final long index) {
        assert index >= 0 && index < numBits;
        int wordNum = (int) (index >> 6);
        int bit = (int) index & 0x3f;
        long bitmask = 1L << bit;
        bits[wordNum] |= bitmask;
    }

    @Override
    public void flip(final long index) {
        int wordNum = expandingWordNum(index);
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] ^= bitmask;
    }

    @Override
    public void flip(final long startIndex, final long endIndex) {
        if (endIndex <= startIndex) {
            return;
        }
        int startWord = (int) (startIndex >> 6);

        // since endIndex is one past the end, this is index of the last
        // word to be changed.
        int endWord = expandingWordNum(endIndex - 1);

        /***
         * Grrr, java shifting wraps around so -1L>>>64 == -1
         * for that reason, make sure not to use endmask if the bits to flip
         * will be zero in the last word (redefine endWord to be the last changed...)
         * long startmask = -1L << (startIndex & 0x3f); // example: 11111...111000
         * long endmask = -1L >>> (64-(endIndex & 0x3f)); // example: 00111...111111
         ***/

        long startmask = -1L << startIndex;
        long endmask = -1L >>> -endIndex; // 64-(endIndex&0x3f) is the same as
                                          // -endIndex due to wrap

        if (startWord == endWord) {
            bits[startWord] ^= (startmask & endmask);
            return;
        }

        bits[startWord] ^= startmask;

        for (int i = startWord + 1; i < endWord; i++) {
            bits[i] = ~bits[i];
        }

        bits[endWord] ^= endmask;
    }

    @Override
    public boolean flipAndGet(final int index) {
        assert index >= 0 && index < numBits;
        int wordNum = index >> 6; // div 64
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] ^= bitmask;
        return (bits[wordNum] & bitmask) != 0;
    }

    @Override
    public boolean flipAndGet(final long index) {
        assert index >= 0 && index < numBits;
        int wordNum = (int) (index >> 6); // div 64
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        bits[wordNum] ^= bitmask;
        return (bits[wordNum] & bitmask) != 0;
    }

    @Override
    public boolean get(final int index) {
        int i = index >> 6; // div 64
        // signed shift will keep a negative index and force an
        // array-index-out-of-bounds-exception, removing the need for an
        // explicit check.
        if (i >= bits.length) {
            return false;
        }

        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        return (bits[i] & bitmask) != 0;
    }

    @Override
    public boolean get(final long index) {
        int i = (int) (index >> 6); // div 64
        if (i >= bits.length) {
            return false;
        }
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        return (bits[i] & bitmask) != 0;
    }

    @Override
    public boolean getAndSet(final int index) {
        assert index >= 0 && index < numBits;
        int wordNum = index >> 6; // div 64
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        boolean val = (bits[wordNum] & bitmask) != 0;
        bits[wordNum] |= bitmask;
        return val;
    }

    @Override
    public boolean getAndSet(final long index) {
        assert index >= 0 && index < numBits;
        int wordNum = (int) (index >> 6); // div 64
        int bit = (int) index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        boolean val = (bits[wordNum] & bitmask) != 0;
        bits[wordNum] |= bitmask;
        return val;
    }

    @Override
    public int getBit(final int index) {
        assert index >= 0 && index < numBits;
        int i = index >> 6; // div 64
        int bit = index & 0x3f; // mod 64
        return ((int) (bits[i] >>> bit)) & 0x01;
    }

    /** Expert: gets the number of longs in the array that are in use */
    public int getNumWords() {
        return wlen;
    }

    @Override
    public int hashCode() {
        // Start with a zero hash and use a mix that results in zero if the input is zero.
        // This effectively truncates trailing zeros without an explicit check.
        long h = 0;
        for (int i = bits.length; --i >= 0;) {
            h ^= bits[i];
            h = (h << 1) | (h >>> 63); // rotate left
        }
        // fold leftmost bits into right and add a constant to prevent
        // empty sets from returning 0, which is too common.
        return (int) ((h >> 32) ^ h) + 0x98761234;
    }

    /**
     * Return a new immutable copy of this mutable bit set.
     *
     * @return a new immutable copy of this mutable bit set
     */
    public ImmutableBitSet immutableCopy() {
        return new ImmutableBitSet(bits, wlen); // bits is cloned in ctr
    }

    @Override
    public AbstractBitSet intersect(final AbstractBitSet other) {
        int newLen = Math.min(this.wlen, other.wlen());
        long[] thisArr = this.bits;
        long[] otherArr = other.bits();
        // testing against zero can be more efficient
        int pos = newLen;
        while (--pos >= 0) {
            thisArr[pos] &= otherArr[pos];
        }
        if (this.wlen > newLen) {
            // fill zeros from the new shorter length to the old length
            Arrays.fill(bits, newLen, this.wlen, 0);
        }
        this.wlen = newLen;
        return this;
    }

    @Override
    public boolean intersects(final AbstractBitSet other) {
        int pos = Math.min(this.wlen, other.wlen());
        long[] thisArr = this.bits;
        long[] otherArr = other.bits();
        while (--pos >= 0) {
            if ((thisArr[pos] & otherArr[pos]) != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return cardinality() == 0;
    }

    @Override
    public int length() {
        return bits.length << 6;
    }

    @Override
    public int nextSetBit(final int index) {
        int i = index >> 6;
        if (i >= wlen) {
            return -1;
        }
        int subIndex = index & 0x3f; // index within the word
        long word = bits[i] >> subIndex; // skip all the bits to the right of index

        if (word != 0) {
            return (i << 6) + subIndex + BitUtil.ntz(word);
        }

        while (++i < wlen) {
            word = bits[i];
            if (word != 0) {
                return (i << 6) + BitUtil.ntz(word);
            }
        }
        return -1;
    }

    @Override
    public long nextSetBit(final long index) {
        int i = (int) (index >>> 6);
        if (i >= wlen) {
            return -1;
        }
        int subIndex = (int) index & 0x3f; // index within the word
        long word = bits[i] >>> subIndex; // skip all the bits to the right of index

        if (word != 0) {
            return (((long) i) << 6) + (subIndex + BitUtil.ntz(word));
        }

        while (++i < wlen) {
            word = bits[i];
            if (word != 0) {
                return (((long) i) << 6) + BitUtil.ntz(word);
            }
        }
        return -1;
    }

    @Override
    public AbstractBitSet or(final AbstractBitSet other) {
        return union(other);
    }

    @Override
    public int prevSetBit(final int index) {
        int i = index >> 6;
        final int subIndex;
        long word;
        if (i >= wlen) {
            i = wlen - 1;
            if (i < 0) {
                return -1;
            }
            subIndex = 63; // last possible bit
            word = bits[i];
        } else {
            if (i < 0) {
                return -1;
            }
            subIndex = index & 0x3f; // index within the word
            word = (bits[i] << (63 - subIndex)); // skip all the bits to the left of index
        }

        if (word != 0) {
            return (i << 6) + subIndex - Long.numberOfLeadingZeros(word); // See LUCENE-3197
        }

        while (--i >= 0) {
            word = bits[i];
            if (word != 0) {
                return (i << 6) + 63 - Long.numberOfLeadingZeros(word);
            }
        }
        return -1;
    }

    @Override
    public long prevSetBit(final long index) {
        int i = (int) (index >> 6);
        final int subIndex;
        long word;
        if (i >= wlen) {
            i = wlen - 1;
            if (i < 0) {
                return -1;
            }
            subIndex = 63; // last possible bit
            word = bits[i];
        } else {
            if (i < 0) {
                return -1;
            }
            subIndex = (int) index & 0x3f; // index within the word
            word = (bits[i] << (63 - subIndex)); // skip all the bits to the left of index
        }

        if (word != 0) {
            return (((long) i) << 6) + subIndex - Long.numberOfLeadingZeros(word); // See LUCENE-3197
        }

        while (--i >= 0) {
            word = bits[i];
            if (word != 0) {
                return (((long) i) << 6) + 63 - Long.numberOfLeadingZeros(word);
            }
        }
        return -1;
    }

    @Override
    public AbstractBitSet remove(final AbstractBitSet other) {
        int idx = Math.min(wlen, other.wlen());
        long[] thisArr = this.bits;
        long[] otherArr = other.bits();
        while (--idx >= 0) {
            thisArr[idx] &= ~otherArr[idx];
        }
        return this;
    }

    @Override
    public void set(final long index) {
        int wordNum = expandingWordNum(index);
        int bit = (int) index & 0x3f;
        long bitmask = 1L << bit;
        bits[wordNum] |= bitmask;
    }

    @Override
    public void set(final long startIndex, final long endIndex) {
        if (endIndex <= startIndex) {
            return;
        }

        int startWord = (int) (startIndex >> 6);

        // since endIndex is one past the end, this is index of the last
        // word to be changed.
        int endWord = expandingWordNum(endIndex - 1);

        long startmask = -1L << startIndex;
        long endmask = -1L >>> -endIndex; // 64-(endIndex&0x3f) is the same as
                                          // -endIndex due to wrap

        if (startWord == endWord) {
            bits[startWord] |= (startmask & endmask);
            return;
        }

        bits[startWord] |= startmask;
        Arrays.fill(bits, startWord + 1, endWord, -1L);
        bits[endWord] |= endmask;
    }

    /** Expert: sets the number of longs in the array that are in use */
    public void setNumWords(int nWords) {
        this.wlen = nWords;
    }

    @Override
    public long size() {
        return capacity();
    }

    @Override
    public void trimTrailingZeros() {
        int idx = wlen - 1;
        while (idx >= 0 && bits[idx] == 0) {
            idx--;
        }
        wlen = idx + 1;
    }

    @Override
    public AbstractBitSet union(final AbstractBitSet other) {
        int newLen = Math.max(wlen, other.wlen());
        ensureCapacityWords(newLen);
        assert (numBits = Math.max(other.numBits(), numBits)) >= 0;

        long[] thisArr = this.bits;
        long[] otherArr = other.bits();
        int pos = Math.min(wlen, other.wlen());
        while (--pos >= 0) {
            thisArr[pos] |= otherArr[pos];
        }
        if (this.wlen < newLen) {
            System.arraycopy(otherArr, this.wlen, thisArr, this.wlen, newLen - this.wlen);
        }
        this.wlen = newLen;
        return this;
    }

    /**
     * Return a new unsafe copy of this mutable bit set.
     *
     * @return a new unsafe copy of this mutable bit set
     */
    public UnsafeBitSet unsafeCopy() {
        return new UnsafeBitSet(bits.clone(), wlen);
    }

    @Override
    public AbstractBitSet xor(final AbstractBitSet other) {
        int newLen = Math.max(wlen, other.wlen());
        ensureCapacityWords(newLen);
        assert (numBits = Math.max(other.numBits(), numBits)) >= 0;

        long[] thisArr = this.bits;
        long[] otherArr = other.bits();
        int pos = Math.min(wlen, other.wlen());
        while (--pos >= 0) {
            thisArr[pos] ^= otherArr[pos];
        }
        if (this.wlen < newLen) {
            System.arraycopy(otherArr, this.wlen, thisArr, this.wlen, newLen - this.wlen);
        }
        this.wlen = newLen;
        return this;
    }

    @Override
    protected long[] bits() {
        return bits;
    }

    @Override
    protected long numBits() {
        return numBits;
    }

    @Override
    protected int wlen() {
        return wlen;
    }

    private int expandingWordNum(final long index) {
        int wordNum = (int) (index >> 6);
        if (wordNum >= wlen) {
            ensureCapacity(index + 1);
            wlen = wordNum + 1;
        }
        assert (numBits = Math.max(numBits, index + 1)) >= 0;
        return wordNum;
    }

    /**
     * Return the cardinality of <code>a and not b</code> or <code>intersection(1, not(b))</code> of the
     * two specified mutable bit sets.  Neither set is modified.
     *
     * @param a first mutable bit set
     * @param b second mutable bit set
     * @return the cardinality of <code>a and not b</code> or <code>intersection(1, not(b))</code> of the
     *    two specified mutable bit sets
     */
    public static long andNotCount(final MutableBitSet a, final MutableBitSet b) {
        long tot = BitUtil.pop_andnot(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
        if (a.wlen > b.wlen) {
            tot += BitUtil.pop_array(a.bits, b.wlen, a.wlen - b.wlen);
        }
        return tot;
    }

    /**
     * Return the cardinality of the intersection of the two specified mutable bit sets.  Neither set is modified.
     *
     * @param a first mutable bit set
     * @param b second mutable bit set
     * @return the cardinality of the intersection of the two specified mutable bit sets
     */
    public static long intersectionCount(final MutableBitSet a, final MutableBitSet b) {
        return BitUtil.pop_intersect(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
    }

    /**
     * Return the cardinality of the union of the two specified mutable bit sets.  Neither set is modified.
     *
     * @param a first mutable bit set
     * @param b second mutable bit set
     * @return the cardinality of the union of the two specified mutable bit sets
     */
    public static long unionCount(final MutableBitSet a, final MutableBitSet b) {
        long tot = BitUtil.pop_union(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
        if (a.wlen < b.wlen) {
            tot += BitUtil.pop_array(b.bits, a.wlen, b.wlen - a.wlen);
        }
        else if (a.wlen > b.wlen) {
            tot += BitUtil.pop_array(a.bits, b.wlen, a.wlen - b.wlen);
        }
        return tot;
    }

    /**
     * Return the cardinality of the exclusive or of the two specified mutable bit sets.  Neither set is modified.
     *
     * @param a first mutable bit set
     * @param b second mutable bit set
     * @return the cardinality of the exclusive or of the two specified mutable bit sets
     */
    public static long xorCount(final MutableBitSet a, final MutableBitSet b) {
        long tot = BitUtil.pop_xor(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
        if (a.wlen < b.wlen) {
            tot += BitUtil.pop_array(b.bits, a.wlen, b.wlen - a.wlen);
        }
        else if (a.wlen > b.wlen) {
            tot += BitUtil.pop_array(a.bits, b.wlen, a.wlen - b.wlen);
        }
        return tot;
    }

    /**
     * Return the number of 64 bit words it would take to hold the specified number of bits.
     * 
     * @param numBits number of bits
     * @return the number of 64 bit words it would take to hold the specified number of bits
     */
    private static int bits2words(final long numBits) {
        return (int) (((numBits - 1) >>> 6) + 1);
    }

    // from RamUsageEstimator.java
    private final static int NUM_BYTES_LONG = 8;

    // from ArrayUtil.java
    private static long[] grow(final long[] array, final int minSize) {
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
