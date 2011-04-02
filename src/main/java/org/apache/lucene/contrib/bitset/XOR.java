package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;

import java.io.IOException;

public class XOR implements BitSetOperation {

  @Override
  public void compute(OpenBitSetDISI accumulator, DocIdSet bitset) throws IOException {
    if (bitset instanceof OpenBitSet) {
      accumulator.xor((OpenBitSet) bitset);
    } else if (bitset instanceof SortedVIntList) {
      accumulator.inPlaceXor(bitset.iterator());
    } else {
      throw new IllegalArgumentException("Not supported:" + bitset);
    }
  }

  @Override
  public OpenBitSetDISI newAccumulator(int bitsetSize) {
    return new OpenBitSetDISI(bitsetSize);
  }

}