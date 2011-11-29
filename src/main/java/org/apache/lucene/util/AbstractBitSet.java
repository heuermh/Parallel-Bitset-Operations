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

    public abstract int length(); // bits.length << 6;
    public abstract long size(); // same as capacity()
    public abstract long capacity(); // 1 greater than the index of the last bit
    public abstract long cardinality(); // number of bits set to true
    public abstract boolean isEmpty(); // cardinality == 0
    public abstract boolean get(int index);
    public abstract boolean fastGet(int index);
    public abstract boolean get(long index);
    public abstract boolean fastGet(long index);
    public abstract int getBit(int index);
    public abstract int nextSetBit(int index);
    public abstract long nextSetBit(long index);
    public abstract int prevSetBit(int index);
    public abstract long prevSetBit(long index);

    // optional
    public abstract void set(long index);
    public abstract void fastSet(int index);
    public abstract void fastSet(long index);
    public abstract void set(long startIndex, long endIndex);
    public abstract void fastClear(int index);
    public abstract void fastClear(long index);
    public abstract void clear(long index);
    public abstract void clear(int startIndex, int endIndex);
    public abstract void clear(long startIndex, long endIndex);
    public abstract boolean getAndSet(int index);
    public abstract boolean getAndSet(long index);
    public abstract void fastFlip(int index);
    public abstract void fastFlip(long index);
    public abstract void flip(long index);
    public abstract boolean flipAndGet(int index);
    public abstract boolean flipAndGet(long index);
    public abstract void flip(long startIndex, long endIndex);
    public abstract void ensureCapacityWords(int numWords);
    public abstract void ensureCapacity(long numBits);
    public abstract void trimTrailingZeros();

    public abstract boolean intersects(AbstractBitSet other);
    public abstract AbstractBitSet intersect(AbstractBitSet other);
    public abstract AbstractBitSet union(AbstractBitSet other);
    public abstract AbstractBitSet remove(AbstractBitSet other);
    public abstract AbstractBitSet xor(AbstractBitSet other);
    public abstract AbstractBitSet and(AbstractBitSet other);
    public abstract AbstractBitSet or(AbstractBitSet other);
    public abstract AbstractBitSet andNot(AbstractBitSet other);
}