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

import java.util.concurrent.Callable;

import org.dishevelled.bitset.ImmutableBitSet;

abstract class AbstractOpCallable<T> implements Callable<T> {

  protected final ImmutableBitSet[] bs;
  protected final int finalBitsetSize;
  protected final int fromIndex;
  protected final int toIndex;

  public AbstractOpCallable(final ImmutableBitSet[] bs, final int fromIndex, final int toIndex, final int finalBitsetSize) {
    if (bs == null || bs.length == 0) {
      throw new IllegalArgumentException("bit sets cannot be null or empty");
    }
    this.bs = bs;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
    this.finalBitsetSize = finalBitsetSize;
  }
}
