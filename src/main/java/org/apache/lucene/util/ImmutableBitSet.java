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

/** An "open" BitSet implementation that allows direct access to the array of words
 * storing the bits.
 * <p/>
 * Unlike java.util.bitset, the fact that bits are packed into an array of longs
 * is part of the interface.  This allows efficient implementation of other algorithms
 * by someone other than the author.  It also allows one to efficiently implement
 * alternate serialization or interchange formats.
 * <p/>
 * <code>OpenBitSet</code> is faster than <code>java.util.BitSet</code> in most operations
 * and *much* faster at calculating cardinality of sets and results of set operations.
 * It can also handle sets of larger cardinality (up to 64 * 2**32-1)
 * <p/>
 * The goals of <code>OpenBitSet</code> are the fastest implementation possible, and
 * maximum code reuse.  Extra safety and encapsulation
 * may always be built on top, but if that's built in, the cost can never be removed (and
 * hence people re-implement their own version in order to get better performance).
 * If you want a "safe", totally encapsulated (and slower and limited) BitSet
 * class, use <code>java.util.BitSet</code>.
 * <p/>
 * <h3>Performance Results</h3>
 *
 Test system: Pentium 4, Sun Java 1.5_06 -server -Xbatch -Xmx64M
<br/>BitSet size = 1,000,000
<br/>Results are java.util.BitSet time divided by OpenBitSet time.
<table border="1">
 <tr>
  <th></th> <th>cardinality</th> <th>intersect_count</th> <th>union</th> <th>nextSetBit</th> <th>get</th> <th>iterator</th>
 </tr>
 <tr>
  <th>50% full</th> <td>3.36</td> <td>3.96</td> <td>1.44</td> <td>1.46</td> <td>1.99</td> <td>1.58</td>
 </tr>
 <tr>
   <th>1% full</th> <td>3.31</td> <td>3.90</td> <td>&nbsp;</td> <td>1.04</td> <td>&nbsp;</td> <td>0.99</td>
 </tr>
</table>
<br/>
Test system: AMD Opteron, 64 bit linux, Sun Java 1.5_06 -server -Xbatch -Xmx64M
<br/>BitSet size = 1,000,000
<br/>Results are java.util.BitSet time divided by OpenBitSet time.
<table border="1">
 <tr>
  <th></th> <th>cardinality</th> <th>intersect_count</th> <th>union</th> <th>nextSetBit</th> <th>get</th> <th>iterator</th>
 </tr>
 <tr>
  <th>50% full</th> <td>2.50</td> <td>3.50</td> <td>1.00</td> <td>1.03</td> <td>1.12</td> <td>1.25</td>
 </tr>
 <tr>
   <th>1% full</th> <td>2.51</td> <td>3.49</td> <td>&nbsp;</td> <td>1.00</td> <td>&nbsp;</td> <td>1.02</td>
 </tr>
</table>
 */

public final class ImmutableBitSet extends AbstractBitSet /*implements Serializable */ {
  private final long[] bits;
  private int wlen;   // number of words (elements) used in the array

  // Used only for assert:
  private long numBits;

    // does not clone bits, ick
    private ImmutableBitSet(long[] bits, int numWords, boolean ignore) {
        this.bits = bits;
        this.wlen = numWords;
        this.numBits = wlen * 64;        
    }

  /** Constructs an OpenBitSet from an existing long[].
   * <br/>
   * The first 64 bits are in long[0],
   * with bit index 0 at the least significant bit, and bit index 63 at the most significant.
   * Given a bit index,
   * the word containing it is long[index/64], and it is at bit number index%64 within that word.
   * <p>
   * numWords are the number of elements in the array that contain
   * set bits (non-zero longs).
   * numWords should be &lt= bits.length, and
   * any existing words in the array at position &gt= numWords should be zero.
   *
   */
  public ImmutableBitSet(long[] bits, int numWords) {
    this.bits = bits.clone();
    this.wlen = numWords;
    this.numBits = wlen * 64;
  }
  

  /** Returns the current capacity in bits (1 greater than the index of the last bit) */
  public long capacity() { return bits.length << 6; }

 /**
  * Returns the current capacity of this set.  Included for
  * compatibility.  This is *not* equal to {@link #cardinality}
  */
  public long size() {
    return capacity();
  }

  public int length() {
    return bits.length << 6;
  }

  /** Returns true if there are no set bits */
  public boolean isEmpty() { return cardinality()==0; }

  /** Expert: gets the number of longs in the array that are in use */
  public int getNumWords() { return wlen; }

  /** Expert: sets the number of longs in the array that are in use */
  public void setNumWords(int nWords) { this.wlen=nWords; }

  /** Returns true or false for the specified bit index. */
  public boolean get(int index) {
    int i = index >> 6;               // div 64
    // signed shift will keep a negative index and force an
    // array-index-out-of-bounds-exception, removing the need for an explicit check.
    if (i>=bits.length) return false;

    int bit = index & 0x3f;           // mod 64
    long bitmask = 1L << bit;
    return (bits[i] & bitmask) != 0;
  }


 /** Returns true or false for the specified bit index.
   * The index should be less than the OpenBitSet size
   */
  public boolean fastGet(int index) {
    assert index >= 0 && index < numBits;
    int i = index >> 6;               // div 64
    // signed shift will keep a negative index and force an
    // array-index-out-of-bounds-exception, removing the need for an explicit check.
    int bit = index & 0x3f;           // mod 64
    long bitmask = 1L << bit;
    return (bits[i] & bitmask) != 0;
  }



 /** Returns true or false for the specified bit index
  */
  public boolean get(long index) {
    int i = (int)(index >> 6);             // div 64
    if (i>=bits.length) return false;
    int bit = (int)index & 0x3f;           // mod 64
    long bitmask = 1L << bit;
    return (bits[i] & bitmask) != 0;
  }

  /** Returns true or false for the specified bit index.
   * The index should be less than the OpenBitSet size.
   */
  public boolean fastGet(long index) {
    assert index >= 0 && index < numBits;
    int i = (int)(index >> 6);               // div 64
    int bit = (int)index & 0x3f;           // mod 64
    long bitmask = 1L << bit;
    return (bits[i] & bitmask) != 0;
  }

  /*
  // alternate implementation of get()
  public boolean get1(int index) {
    int i = index >> 6;                // div 64
    int bit = index & 0x3f;            // mod 64
    return ((bits[i]>>>bit) & 0x01) != 0;
    // this does a long shift and a bittest (on x86) vs
    // a long shift, and a long AND, (the test for zero is prob a no-op)
    // testing on a P4 indicates this is slower than (bits[i] & bitmask) != 0;
  }
  */


  /** returns 1 if the bit is set, 0 if not.
   * The index should be less than the OpenBitSet size
   */
  public int getBit(int index) {
    assert index >= 0 && index < numBits;
    int i = index >> 6;                // div 64
    int bit = index & 0x3f;            // mod 64
    return ((int)(bits[i]>>>bit)) & 0x01;
  }


  /*
  public boolean get2(int index) {
    int word = index >> 6;            // div 64
    int bit = index & 0x0000003f;     // mod 64
    return (bits[word] << bit) < 0;   // hmmm, this would work if bit order were reversed
    // we could right shift and check for parity bit, if it was available to us.
  }
  */

  /** @return the number of set bits */
  public long cardinality() {
    return BitUtil.pop_array(bits,0,wlen);
  }

 /** Returns the popcount or cardinality of the intersection of the two sets.
   * Neither set is modified.
   */
  public static long intersectionCount(ImmutableBitSet a, ImmutableBitSet b) {
    return BitUtil.pop_intersect(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
 }

  /** Returns the popcount or cardinality of the union of the two sets.
    * Neither set is modified.
    */
  public static long unionCount(ImmutableBitSet a, ImmutableBitSet b) {
    long tot = BitUtil.pop_union(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
    if (a.wlen < b.wlen) {
      tot += BitUtil.pop_array(b.bits, a.wlen, b.wlen-a.wlen);
    } else if (a.wlen > b.wlen) {
      tot += BitUtil.pop_array(a.bits, b.wlen, a.wlen-b.wlen);
    }
    return tot;
  }

  /** Returns the popcount or cardinality of "a and not b"
   * or "intersection(a, not(b))".
   * Neither set is modified.
   */
  public static long andNotCount(ImmutableBitSet a, ImmutableBitSet b) {
    long tot = BitUtil.pop_andnot(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
    if (a.wlen > b.wlen) {
      tot += BitUtil.pop_array(a.bits, b.wlen, a.wlen-b.wlen);
    }
    return tot;
  }

 /** Returns the popcount or cardinality of the exclusive-or of the two sets.
  * Neither set is modified.
  */
  public static long xorCount(ImmutableBitSet a, ImmutableBitSet b) {
    long tot = BitUtil.pop_xor(a.bits, b.bits, 0, Math.min(a.wlen, b.wlen));
    if (a.wlen < b.wlen) {
      tot += BitUtil.pop_array(b.bits, a.wlen, b.wlen-a.wlen);
    } else if (a.wlen > b.wlen) {
      tot += BitUtil.pop_array(a.bits, b.wlen, a.wlen-b.wlen);
    }
    return tot;
  }


  /** Returns the index of the first set bit starting at the index specified.
   *  -1 is returned if there are no more set bits.
   */
  public int nextSetBit(int index) {
    int i = index>>6;
    if (i>=wlen) return -1;
    int subIndex = index & 0x3f;      // index within the word
    long word = bits[i] >> subIndex;  // skip all the bits to the right of index

    if (word!=0) {
      return (i<<6) + subIndex + BitUtil.ntz(word);
    }

    while(++i < wlen) {
      word = bits[i];
      if (word!=0) return (i<<6) + BitUtil.ntz(word);
    }

    return -1;
  }

  /** Returns the index of the first set bit starting at the index specified.
   *  -1 is returned if there are no more set bits.
   */
  public long nextSetBit(long index) {
    int i = (int)(index>>>6);
    if (i>=wlen) return -1;
    int subIndex = (int)index & 0x3f; // index within the word
    long word = bits[i] >>> subIndex;  // skip all the bits to the right of index

    if (word!=0) {
      return (((long)i)<<6) + (subIndex + BitUtil.ntz(word));
    }

    while(++i < wlen) {
      word = bits[i];
      if (word!=0) return (((long)i)<<6) + BitUtil.ntz(word);
    }

    return -1;
  }


  /** Returns the index of the first set bit starting downwards at
   *  the index specified.
   *  -1 is returned if there are no more set bits.
   */
  public int prevSetBit(int index) {
    int i = index >> 6;
    final int subIndex;
    long word;
    if (i >= wlen) {
      i = wlen - 1;
      if (i < 0) return -1;
      subIndex = 63;  // last possible bit
      word = bits[i];
    } else {
      if (i < 0) return -1;
      subIndex = index & 0x3f;  // index within the word
      word = (bits[i] << (63-subIndex));  // skip all the bits to the left of index
    }

    if (word != 0) {
      return (i << 6) + subIndex - Long.numberOfLeadingZeros(word); // See LUCENE-3197
    }

    while (--i >= 0) {
      word = bits[i];
      if (word !=0 ) {
        return (i << 6) + 63 - Long.numberOfLeadingZeros(word);
      }
    }

    return -1;
  }

  /** Returns the index of the first set bit starting downwards at
   *  the index specified.
   *  -1 is returned if there are no more set bits.
   */
  public long prevSetBit(long index) {
    int i = (int) (index >> 6);
    final int subIndex;
    long word;
    if (i >= wlen) {
      i = wlen - 1;
      if (i < 0) return -1;
      subIndex = 63;  // last possible bit
      word = bits[i];
    } else {
      if (i < 0) return -1;
      subIndex = (int)index & 0x3f;  // index within the word
      word = (bits[i] << (63-subIndex));  // skip all the bits to the left of index
    }

    if (word != 0) {
      return (((long)i)<<6) + subIndex - Long.numberOfLeadingZeros(word); // See LUCENE-3197
    }

    while (--i >= 0) {
      word = bits[i];
      if (word !=0 ) {
        return (((long)i)<<6) + 63 - Long.numberOfLeadingZeros(word);
      }
    }

    return -1;
  }

  /** this = this AND other */
  public ImmutableBitSet intersect(ImmutableBitSet other) {
    int newLen= Math.min(this.wlen,other.wlen);
    long[] thisArr = this.bits.clone();
    long[] otherArr = other.bits;
    // testing against zero can be more efficient
    int pos=newLen;
    while(--pos>=0) {
      thisArr[pos] &= otherArr[pos];
    }
    //if (this.wlen > newLen) {
      // fill zeros from the new shorter length to the old length
    //  Arrays.fill(bits,newLen,this.wlen,0);
    //}
    this.wlen = newLen;
    return new ImmutableBitSet(thisArr, newLen, true);
  }

  /** this = this OR other */
  public ImmutableBitSet union(ImmutableBitSet other) {
    int newLen = Math.max(wlen,other.wlen);
    //ensureCapacityWords(newLen);
    //assert (numBits = Math.max(other.numBits, numBits)) >= 0;

    long[] thisArr = this.bits.clone();
    long[] otherArr = other.bits;
    int pos=Math.min(wlen,other.wlen);
    while(--pos>=0) {
      thisArr[pos] |= otherArr[pos];
    }
    //if (this.wlen < newLen) {
    //  System.arraycopy(otherArr, this.wlen, thisArr, this.wlen, newLen-this.wlen);
    //}
    //this.wlen = newLen;
    return new ImmutableBitSet(thisArr, newLen, true);
  }


  /** Remove all elements set in other. this = this AND_NOT other */
  public ImmutableBitSet remove(ImmutableBitSet other) {
    int idx = Math.min(wlen,other.wlen);
    long[] thisArr = this.bits.clone();
    long[] otherArr = other.bits;
    while(--idx>=0) {
      thisArr[idx] &= ~otherArr[idx];
    }
    return new ImmutableBitSet(thisArr, wlen, true);
  }

  /** this = this XOR other */
  public ImmutableBitSet xor(ImmutableBitSet other) {
    int newLen = Math.max(wlen,other.wlen);
    //ensureCapacityWords(newLen);
    //assert (numBits = Math.max(other.numBits, numBits)) >= 0;

    long[] thisArr = this.bits.clone();
    long[] otherArr = other.bits;
    int pos=Math.min(wlen,other.wlen);
    while(--pos>=0) {
      thisArr[pos] ^= otherArr[pos];
    }
    //if (this.wlen < newLen) {
    //  System.arraycopy(otherArr, this.wlen, thisArr, this.wlen, newLen-this.wlen);
    //}
    //this.wlen = newLen;
    return new ImmutableBitSet(thisArr, newLen, true);
  }


  // some BitSet compatability methods

  //** see {@link intersect} */
  public ImmutableBitSet and(ImmutableBitSet other) {
    return intersect(other);
  }

  //** see {@link union} */
  public ImmutableBitSet or(ImmutableBitSet other) {
    return union(other);
  }

  //** see {@link andNot} */
  public ImmutableBitSet andNot(ImmutableBitSet other) {
    return remove(other);
  }

  /** returns true if the sets have any elements in common */
  public boolean intersects(ImmutableBitSet other) {
    int pos = Math.min(this.wlen, other.wlen);
    long[] thisArr = this.bits;
    long[] otherArr = other.bits;
    while (--pos>=0) {
      if ((thisArr[pos] & otherArr[pos])!=0) return true;
    }
    return false;
  }

  /** returns the number of 64 bit words it would take to hold numBits */
  public static int bits2words(long numBits) {
   return (int)(((numBits-1)>>>6)+1);
  }


  /** returns true if both sets have the same bits set */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ImmutableBitSet)) return false;
    ImmutableBitSet a;
    ImmutableBitSet b = (ImmutableBitSet)o;
    // make a the larger set.
    if (b.wlen > this.wlen) {
      a = b; b=this;
    } else {
      a=this;
    }

    // check for any set bits out of the range of b
    for (int i=a.wlen-1; i>=b.wlen; i--) {
      if (a.bits[i]!=0) return false;
    }

    for (int i=b.wlen-1; i>=0; i--) {
      if (a.bits[i] != b.bits[i]) return false;
    }

    return true;
  }


  @Override
  public int hashCode() {
    // Start with a zero hash and use a mix that results in zero if the input is zero.
    // This effectively truncates trailing zeros without an explicit check.
    long h = 0;
    for (int i = bits.length; --i>=0;) {
      h ^= bits[i];
      h = (h << 1) | (h >>> 63); // rotate left
    }
    // fold leftmost bits into right and add a constant to prevent
    // empty sets from returning 0, which is too common.
    return (int)((h>>32) ^ h) + 0x98761234;
  }

  // from RamUsageEstimator.java
  private final static int NUM_BYTES_LONG = 8;

  // from ArrayUtil.java
  private static long[] grow(long[] array, int minSize) {
    assert minSize >= 0: "size must be positive (got " + minSize + "): likely integer overflow?";
    if (array.length < minSize) {
      long[] newArray = new long[oversize(minSize, NUM_BYTES_LONG)];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    } else
      return array;
  }

  private static int oversize(int minTargetSize, int bytesPerElement) {

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
    if (newSize+7 < 0) {
      // int overflowed -- return max allowed array size
      return Integer.MAX_VALUE;
    }

    if (JRE_IS_64BIT) {
      // round up to 8 byte alignment in 64bit env
      switch(bytesPerElement) {
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
    } else {
      // round up to 4 byte alignment in 64bit env
      switch(bytesPerElement) {
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
