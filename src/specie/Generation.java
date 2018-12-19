package specie;

import java.util.Arrays;
import java.util.Random;

public class Generation {
    private int currentGeneration = 0;
    private final int size;
    private Specie[] generation;
    private int currentSpecie = 1;
    private final int numberToBreed;

    private final Random random = new Random();

    public Generation(int size, double percentToBreed) {
        this.size = size;
        this.numberToBreed = (int) (percentToBreed * size);
        generation = new Specie[size];
    }

    public Specie[] advance() {
        currentGeneration++;

        if (currentGeneration == 1) {
            for (int i = 0; i < size; i++) {
                generation[i] = new Specie();
            }
        } else {
            for (int i = numberToBreed; i < size; i++) {
                int parent1;
                int parent2;

                do {
                    parent1 = random.nextInt(numberToBreed);
                    parent2 = random.nextInt(numberToBreed);
                } while (parent1 == parent2);

                generation[i] = new Specie(generation[parent1], generation[parent2]);
            }
        }

        return generation;
    }

    public Specie[] sort() {
        Arrays.sort(generation);
        return generation;
    }
}