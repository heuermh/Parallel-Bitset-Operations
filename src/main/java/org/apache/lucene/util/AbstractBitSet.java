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
}