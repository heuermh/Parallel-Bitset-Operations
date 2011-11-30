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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.apache.lucene.util.AbstractBitSet;
import org.apache.lucene.util.ImmutableBitSet;

abstract class BitSetSlicer<T> {

    public Collection<Callable<T>> sliceBitsets(final ImmutableBitSet[] bs) {
        int sliceSize = bs.length / Runtime.getRuntime().availableProcessors(); // make this configurable

        int numOfOps = bs.length / sliceSize;
        if (bs.length % sliceSize != 0) {
            numOfOps++;
        }

        Collection<Callable<T>> ops = new LinkedList<Callable<T>>();
        for (int i = 0; i < numOfOps; i++) {
            int startIndex = i * sliceSize;
            ops.add(newOpCallable(bs, startIndex, lastIndex(bs.length, startIndex, sliceSize)));
        }
        return ops;
    }

    protected abstract Callable<T> newOpCallable(ImmutableBitSet[] bs, int startIndex, int i);

    private int lastIndex(final int numberOfBitsets, final int startIndex, final int sliceSize) {
        int remaining = numberOfBitsets - startIndex;
        return remaining > sliceSize ? startIndex + sliceSize : startIndex + remaining;
    }
}
