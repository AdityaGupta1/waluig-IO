package species;

import main.Main;
import nintaco.api.API;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Generation {
    private static final API api = Main.api;

    private int currentGeneration = 1;
    private final int size = Main.generationSize;
    private Network[] generation;
    private int currentNetwork = -1;
    private final int numberToBreed = (int) (Main.percentToBreed * size);

    private int staleGenerations = 0;
    static double mutationRate = Main.baseMutationRate;

    private static final Random random = new Random();

    public Generation() {
        generation = new Network[size];
        for (int i = 0; i < size; i++) {
            generation[i] = new Network();
        }
    }

    private int lastMean = 0;

    private void advance() {
        int mean = printStats();

        if (mean == lastMean) {
            staleGenerations++;
        } else {
            staleGenerations = 0;
        }

        if (staleGenerations > Main.maxStaleGenerations) {
            new Thread(() -> Main.main(null)).start(); // should reset
            return;
        }

        mutationRate = Main.baseMutationRate + Main.mutationRatePerStaleGeneration * staleGenerations;
        lastMean = mean;

        currentGeneration++;
        currentNetwork = -1;

        for (int i = numberToBreed; i < size; i++) {
            Supplier<Network> randomNetwork = () -> generation[random.nextInt(numberToBreed)];
            generation[i] = new Network(randomNetwork.get(), randomNetwork.get());
        }
    }

    private Network getCurrent() {
        return generation[currentNetwork];
    }

    public Network nextNetwork() {
        return nextNetwork(true);
    }

    private Network nextNetwork(boolean load) {
        if (load) {
            api.loadState("states/SMB.save");
        }
        currentNetwork++;

        if (currentNetwork >= size) {
            advance();
            return nextNetwork(false);
        }

        getCurrent().resetFitness();
        return getCurrent();
    }

    private int printStats() {
        Arrays.sort(generation, Collections.reverseOrder());
        Network[] top = Arrays.copyOfRange(generation, 0, numberToBreed);
        int mean = (int) Math.round(Stream.of(top).mapToInt(Network::getFitness).average().getAsDouble());

        System.out.println("generation " + currentGeneration);
        System.out.println("mean: " + mean);
        System.out.println("---------------");
        for (int i = 0; i < numberToBreed; i++) {
            System.out.println("" + (i + 1) + ") " + top[i]);
        }
        System.out.println();

        return mean;
    }

    public String[] getDisplay() {
        return new String[]{
                "generation " + currentGeneration + ", network " + (currentNetwork + 1) + "/" + size,
                getCurrent().toString(),
                "stale: " + staleGenerations + ", mutation rate: " + mutationRate
        };
    }
}