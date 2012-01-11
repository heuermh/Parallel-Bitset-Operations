/*
 * Parallel Bitset Operations
 * Copyright (C) 2011 Federico Fissore
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */

package org.apache.lucene.contrib.bitset;

import org.apache.lucene.contrib.bitset.ops.AssociativeOp;
import org.apache.lucene.contrib.bitset.ops.ComparisonOp;

import org.dishevelled.bitset.MutableBitSet;
import org.dishevelled.bitset.ImmutableBitSet;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * BitsetOperationsExecutor is the entry point for performing bitset operations.<br/><br/>
 * You need to create an array of the {@link DocIdSet} you want to operate on, choose the operation to perform (one implementation of {@link org.apache.lucene.contrib.bitset.ops.AssociativeOp} or {@link ComparisonOp}) and call the appropriate perform method.<br/><br/>
 * The input array will be split in as many parts as available cores (as by {@link Runtime#availableProcessors()}), and the given operation will be performed
 */
public final class BitsetOperationsExecutor {
    private static final int MIN_ARRAY_SIZE = 20000;
    private final ExecutorService threadPool;
    private final int minArraySize;

    /**
     * Create a new BitsetOperationsExecutor with the default minArraySize
     *
     * @param threadPool the ExecutorService to use to submit tasks
     */
    public BitsetOperationsExecutor(final ExecutorService threadPool) {
        this(threadPool, MIN_ARRAY_SIZE);
    }

    /**
     * Creates a new BitsetOperationsExecutor with the specified minArraySize
     *
     * @param threadPool   the ExecutorService to use to submit tasks
     * @param minArraySize the minimum size of the input array of DocIdSet. If the input array contains less than the given size, the call will NOT trigger a thread and the calculation will be done in the same calling thread
     */
    public BitsetOperationsExecutor(final ExecutorService threadPool, final int minArraySize) {
        this.threadPool = threadPool;
        this.minArraySize = minArraySize;
    }

    /**
     * Performs a commutative operation on the given array of bitsets
     *
     * @param bs              the bitsets on to compute the operation
     * @param finalBitsetSize the final bitset size (tipically IndexReader.numDocs())
     * @param operation       the operation to perform
     * @return an OpenBitSet, result of the operation
     * @throws Exception
     */
    public MutableBitSet perform(final ImmutableBitSet[] bs, final int finalBitsetSize, final AssociativeOp operation) throws Exception {
        if (bs.length <= minArraySize) {
            return new AssociativeOpCallable(bs, 0, bs.length, finalBitsetSize, operation).call();
        }

        Collection<Callable<MutableBitSet>> ops = new BitSetSlicer<MutableBitSet>() {
            @Override
            protected Callable<MutableBitSet> newOpCallable(final ImmutableBitSet[] bs, final int fromIndex, final int toIndex) {
                return new AssociativeOpCallable(bs, fromIndex, toIndex, finalBitsetSize, operation);
            }
        }.sliceBitsets(bs);

        List<Future<MutableBitSet>> futures = threadPool.invokeAll(ops);
        MutableBitSet[] accumulated = ArrayUtils.toArray(futures);
        ImmutableBitSet[] immutableAccumulated = immutableCopy(accumulated);
        return new AssociativeOpCallable(immutableAccumulated, 0, immutableAccumulated.length, finalBitsetSize, operation).call();
    }

    /**
     * Performs a comparative operation on the given array of bitsets
     *
     * @param bs              the bitsets on to compute the operation
     * @param toCompare       the bitset to compare to the array of bitsets
     * @param finalBitsetSize the final bitset size (tipically IndexReader.numDocs())
     * @param operation       the operation to compute
     * @param <T>             the return type
     * @return an array of objects (whose type is defined by the operation). The array has the same size of the input array of bitsets and order is preserved so the result of the operation performed at bs[N] is at position N in the returned array
     * @throws Exception
     */
    public <T> T[] perform(final ImmutableBitSet[] bs, final ImmutableBitSet toCompare, final int finalBitsetSize, final ComparisonOp<T> operation) throws Exception {
        if (bs.length <= minArraySize) {
            return new ComparisonOpCallable<T>(bs, 0, bs.length, finalBitsetSize, toCompare, operation).call();
        }
        Collection<Callable<T[]>> ops = new BitSetSlicer<T[]>() {
            @Override
            protected Callable<T[]> newOpCallable(final ImmutableBitSet[] bs, final int fromIndex, final int toIndex) {
                return new ComparisonOpCallable<T>(bs, fromIndex, toIndex, finalBitsetSize, toCompare, operation);
            }
        }.sliceBitsets(bs);

        List<Future<T[]>> futures = threadPool.invokeAll(ops);
        T[][] partitionResults = ArrayUtils.toArray(futures);
        return ArrayUtils.flatten(partitionResults);
    }

    private static ImmutableBitSet[] immutableCopy(final MutableBitSet[] mutableBitsets) {
        ImmutableBitSet[] immutableBitsets = new ImmutableBitSet[mutableBitsets.length];
        for (int i = 0, size = mutableBitsets.length; i < size; i++) {
            immutableBitsets[i] = mutableBitsets[i].immutableCopy();
        }
        return immutableBitsets;
    }
}
