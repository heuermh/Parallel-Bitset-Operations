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
import org.apache.lucene.util.OpenBitSet;

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
public class BitsetOperationsExecutor {

  private static final int MIN_ARRAY_SIZE = 20000;

  private final ExecutorService threadPool;
  private final int minArraySize;

  /**
   * Create a new BitsetOperationsExecutor with the default minArraySize
   *
   * @param threadPool the ExecutorService to use to submit tasks
   */
  public BitsetOperationsExecutor(ExecutorService threadPool) {
    this(threadPool, MIN_ARRAY_SIZE);
  }

  /**
   * Creates a new BitsetOperationsExecutor with the specified minArraySize
   *
   * @param threadPool   the ExecutorService to use to submit tasks
   * @param minArraySize the minimum size of the input array of DocIdSet. If the input array contains less than the given size, the call will NOT trigger a thread and the calculation will be done in the same calling thread
   */
  public BitsetOperationsExecutor(ExecutorService threadPool, int minArraySize) {
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
  public OpenBitSet perform(OpenBitSet bs, final int finalBitsetSize, final AssociativeOp operation) throws Exception {
    if (bs.capacity() <= minArraySize) {
        return new AssociativeOpCallable(bs, 0, bs.capacity(), finalBitsetSize, operation).call();
    }

    Collection<Callable<OpenBitSet>> ops = new BitSetSlicer<OpenBitSet>() {

      @Override
      protected Callable<OpenBitSet> newOpCallable(OpenBitSet bs, int fromIndex, int toIndex) {
        return new AssociativeOpCallable(bs, fromIndex, toIndex, finalBitsetSize, operation);
      }

    }.sliceBitsets(bs);

    List<Future<OpenBitSet>> futures = threadPool.invokeAll(ops);

    OpenBitSet[] accumulated = ArrayUtils.toArray(futures);

    return new AssociativeOpCallable(accumulated, 0, accumulated.capacity(), finalBitsetSize, operation).call();
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
  public <T> T[] perform(OpenBitSet bs, OpenBitSet toCompare, final int finalBitsetSize, final ComparisonOp<T> operation) throws Exception {
    final OpenBitSet toCompareBs = new OpenBitSet(finalBitsetSize);
    toCompareBs.inPlaceOr(toCompare.iterator());

    if (bs.capacity() <= minArraySize) {
      return new ComparisonOpCallable<T>(bs, 0, bs.capacity(), finalBitsetSize, toCompareBs, operation).call();
    }

    Collection<Callable<T[]>> ops = new BitSetSlicer<T[]>() {

      @Override
      protected Callable<T[]> newOpCallable(OpenBitSet bs, int fromIndex, int toIndex) {
        return new ComparisonOpCallable<T>(bs, fromIndex, toIndex, finalBitsetSize, toCompareBs, operation);
      }

    }.sliceBitsets(bs);

    List<Future<T[]>> futures = threadPool.invokeAll(ops);

    T[][] partitionResults = ArrayUtils.toArray(futures);

    return ArrayUtils.flatten(partitionResults);
  }

}
