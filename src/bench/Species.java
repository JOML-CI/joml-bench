package bench;

import jdk.incubator.vector.FloatVector;

public class Species {
    public static void main(String[] args) {
        System.out.println("SPECIES_64:  " + FloatVector.SPECIES_64);
        System.out.println("SPECIES_128: " + FloatVector.SPECIES_128);
        System.out.println("SPECIES_256: " + FloatVector.SPECIES_256);
        System.out.println("SPECIES_512: " + FloatVector.SPECIES_512);
        System.out.println("SPECIES_MAX: " + FloatVector.SPECIES_MAX);
    }
}
