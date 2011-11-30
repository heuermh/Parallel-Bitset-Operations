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
 * Unsafe bit set.
 */
public class UnsafeBitSet extends MutableBitSet/* implements Cloneable, Serializable */ {

    /**
     * Create a new unsafe bit set with the default number of bits.
     *
     * @see DEFAULT_NUM_BITS
     */
    public UnsafeBitSet() {
        super();
    }

    /**
     * Create a new unsafe bit set with the specified number of bits.
     *
     * @param numBits number of bits
     */
    public UnsafeBitSet(final long numBits) {
        super(numBits);
    }

  /**
   * Create a new unsafe bit set from the specified <code>long[]</code>.
     *
     * @param bits bits stored in <code>long[]</code>
     * @param wlen number of words/elements used in <code>bits</code>
   */
    public UnsafeBitSet(final long[] bits, final int wlen) {
        super(bits, wlen * 64, wlen);  // bits are not cloned in ctr
    }


    /**
     * Return the <code>long[]</code> backing this unsafe bit set.
     *
     * @return the <code>long[]</code> backing this unsafe bit set
     */
    public final long[] getBits() {
        return bits;
    }

    /**
     * Set the <code>long[]</code> backing this unsafe bit set to <code>bits</code>.
     *
     * @param bits bits stored in <code>long[]</code>
     */
    public final void setBits(final long[] bits) {
        this.bits = bits;
    }
}
