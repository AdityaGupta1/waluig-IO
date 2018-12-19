package specie;

import main.Main;
import nintaco.api.API;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Generation {
    private static final API api = Main.api;

    private int currentGeneration = 0;
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

    public void advance() {
        printStats();

        currentGeneration++;
        currentSpecie = -1;

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
    }

    public Specie[] sort() {
        Arrays.sort(generation);
        return generation;
    }

    private Specie getCurrent() {
        return generation[currentSpecie];
    }

    public Specie nextSpecie() {
        api.loadState("states/SMB.save");
        currentSpecie++;

        if (currentSpecie >= size) {
            advance();
            return nextSpecie();
        }

        return getCurrent();
    }

    public void printStats() {
        sort();
        int mean = (int) Math.round(Stream.of(generation).mapToInt(Specie::getFitness).limit(numberToBreed)
                .summaryStatistics().getAverage());
        String top = IntStream.iterate(0, x -> x++).limit(numberToBreed)
                .mapToObj(i -> "" + i + ") " + generation[i] + "\n").collect(Collectors.joining());

        System.out.println("generation " + currentGeneration);
        System.out.println("mean: " + mean);
        System.out.println("---------------");
        System.out.println(top);
        System.out.println();
    }

    public void display() {
        api.drawString("generation " + currentGeneration + ", specie " + currentSpecie + "/" + size, 10, 10, true);
        api.drawString("fitness: " + getCurrent().getFitness(), 10, 40, true);
    }
}