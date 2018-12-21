package specie;

import main.Main;
import nintaco.api.API;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Stream;

public class Generation {
    private static final API api = Main.api;

    private int currentGeneration = 1;
    private final int size = Main.generationSize;
    private Specie[] generation;
    private int currentSpecie = -1;
    private final int numberToBreed = (int) (Main.percentToBreed * size);

    static double mutationRate = Main.baseMutationRate;

    private static final Random random = new Random();

    public Generation() {
        generation = new Specie[size];
        for (int i = 0; i < size; i++) {
            generation[i] = new Specie();
        }
    }

    private void advance() {
        printStats();

        currentGeneration++;
        currentSpecie = -1;

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

    private Specie getCurrent() {
        return generation[currentSpecie];
    }

    public Specie nextSpecie() {
        return nextSpecie(true);
    }

    private Specie nextSpecie(boolean load) {
        if (load) {
            api.loadState("states/SMB.save");
        }
        currentSpecie++;

        if (currentSpecie >= size) {
            advance();
            return nextSpecie(false);
        }

        return getCurrent();
    }

    private void printStats() {
        Arrays.sort(generation, Collections.reverseOrder());
        Specie[] top = Arrays.copyOfRange(generation, 0, numberToBreed);
        int mean = (int) Math.round(Stream.of(top).mapToInt(Specie::getFitness).average().getAsDouble());

        System.out.println("generation " + currentGeneration);
        System.out.println("mean: " + mean);
        System.out.println("---------------");
        for (int i = 0; i < numberToBreed; i++) {
            System.out.println("" + (i + 1) + ") " + top[i]);
        }
        System.out.println();
    }

    public String[] getDisplay() {
        return new String[]{
                "generation " + currentGeneration + ", specie " + (currentSpecie + 1) + "/" + size,
                getCurrent().toString()
        };
    }
}