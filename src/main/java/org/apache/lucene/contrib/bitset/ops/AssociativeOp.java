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

package org.apache.lucene.contrib.bitset.ops;

import org.dishevelled.bitset.ImmutableBitSet;
import org.dishevelled.bitset.MutableBitSet;

/**
 * An operation that could easily be split in parts, producing partial results to accumulate at the end
 */
public interface AssociativeOp {

  /**
   * Performs the implemented operation getting bits from the given bitset and accumulating results on the accumulator
   *
   * @param accumulator the bitset where to accumulate results
   * @param bitset      the bitset from where to get the bits
   */
  void compute(MutableBitSet accumulator, ImmutableBitSet bitset);
}
