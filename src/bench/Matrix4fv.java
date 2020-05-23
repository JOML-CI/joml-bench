package bench;

import jdk.incubator.vector.VectorShuffle;

import static jdk.incubator.vector.FloatVector.SPECIES_128;
import static jdk.incubator.vector.FloatVector.SPECIES_256;

class Matrix4fv {
    /*
     * Having the VectorShuffles as static final fields here is MUUUCH faster than creating them inline in the methods!!!
     */
    static final VectorShuffle<Float> s0000 = SPECIES_128.shuffleFromValues(0, 0, 0, 0);
    static final VectorShuffle<Float> s1111 = SPECIES_128.shuffleFromValues(1, 1, 1, 1);
    static final VectorShuffle<Float> s2222 = SPECIES_128.shuffleFromValues(2, 2, 2, 2);
    static final VectorShuffle<Float> s3333 = SPECIES_128.shuffleFromValues(3, 3, 3, 3);
    static final VectorShuffle<Float> s00004444 = SPECIES_256.shuffleFromValues(0, 0, 0, 0, 4, 4, 4, 4); //_MM_SHUFFLE(0, 0, 0, 0)
    static final VectorShuffle<Float> s11115555 = SPECIES_256.shuffleFromValues(1, 1, 1, 1, 5, 5, 5, 5); //_MM_SHUFFLE(1, 1, 1, 1)
    static final VectorShuffle<Float> s22226666 = SPECIES_256.shuffleFromValues(2, 2, 2, 2, 6, 6, 6, 6); //_MM_SHUFFLE(2, 2, 2, 2)
    static final VectorShuffle<Float> s33337777 = SPECIES_256.shuffleFromValues(3, 3, 3, 3, 7, 7, 7, 7); //_MM_SHUFFLE(3, 3, 3, 3)
    static final VectorShuffle<Float> s01230123 = SPECIES_256.shuffleFromValues(0, 1, 2, 3, 0, 1, 2, 3); //_mm256_permute2f128_ps(..., 0x00);
    static final VectorShuffle<Float> s45674567 = SPECIES_256.shuffleFromValues(4, 5, 6, 7, 4, 5, 6, 7); //_mm256_permute2f128_ps(..., 0x11);
}
