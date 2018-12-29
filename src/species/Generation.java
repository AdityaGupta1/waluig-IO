package species;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static main.Constants.*;
import static main.Main.api;

public class Generation {
    private int currentGeneration = 1;
    private int currentNetwork = -1;
    private final Network[] generation = new Network[generationSize];
    private List<Species> species = new ArrayList<>();

    private static final Random random = new Random();

    public Generation() {
        for (int i = 0; i < generationSize; i++) {
            generation[i] = new Network();
        }

        sortIntoSpecies();
    }

    public Network nextNetwork() {
        return nextNetwork(true);
    }

    private Network nextNetwork(boolean load) {
        if (load) {
            api.loadState("states/SMB.save");
        }

        currentNetwork++;

        if (currentNetwork >= generationSize) {
            advance();
            return nextNetwork(false);
        }

        generation[currentNetwork].resetFitness();
        return generation[currentNetwork];
    }

    private void advance() {
        printStats();

        currentGeneration++;
        currentNetwork = -1;

        crossOver();
        sortIntoSpecies();
        Arrays.stream(generation).forEach(Network::mutate);
    }

    private void printStats() {
        Arrays.sort(generation, Collections.reverseOrder());
        Network[] top = Arrays.copyOfRange(generation, 0, generationTopNumber);
        int mean = (int) Math.round(Stream.of(top).mapToInt(Network::getFitness).average().getAsDouble());

        System.out.println("generation " + currentGeneration);
        System.out.println("mean: " + mean);
        System.out.println("---------------");
        for (int i = 0; i < generationTopNumber; i++) {
            System.out.println("" + (i + 1) + ") " + top[i]);
        }
        System.out.println();
    }

    private void crossOver() {
        List<Network> newNetworks = new ArrayList<>();
        Map<Species, Integer> offspringPerSpecies = offspringPerSpecies();

        for (Species species : this.species) {
            int offspring = offspringPerSpecies.get(species);

            if (offspring == 0) {
                continue;
            }

            List<Network> top = species.getNetworks().stream().sorted().limit((int) Math.ceil(speciesTopPercent * species.getSize())).collect(Collectors.toList());
            Supplier<Network> randomNetwork = () -> top.get(random.nextInt(top.size()));

            for (int j = 0; j < offspring; j++) {
                if (top.size() == 1 || Math.random() < 1 - crossoverChance) {
                    newNetworks.add(new Network(top.get(0), false));
                } else {
                    Network parent1;
                    Network parent2;

                    do {
                        parent1 = randomNetwork.get();
                        parent2 = randomNetwork.get();
                    } while (parent1 == parent2);

                    newNetworks.add(new Network(parent1, parent2));
                }
            }
        }

        for (int i = 0; i < generationSize; i++) {
            generation[i] = newNetworks.get(i);
        }
    }

    private Map<Species, Integer> offspringPerSpecies() {
        ToDoubleFunction<Species> speciesAdjustedFitness = x -> ((double) x.getNetworks().stream().mapToInt(Network::getFitness).sum()) / x.getSize();
        double totalAdjustedFitness = this.species.stream().mapToDouble(speciesAdjustedFitness).sum();

        List<Species> nonEmptySpecies = this.species.stream().filter(Species::notEmpty).collect(Collectors.toList());
        int[] offspringPerNonEmptySpecies = round(nonEmptySpecies.stream().mapToDouble(x -> generationSize * speciesAdjustedFitness.applyAsDouble(x) / totalAdjustedFitness).toArray());

        Map<Species, Integer> output = new HashMap<>();
        for (int i = 0; i < this.species.size(); i++) {
            Species species = this.species.get(i);

            if (nonEmptySpecies.contains(species)) {
                output.put(species, offspringPerNonEmptySpecies[i]);
            } else {
                output.put(species, 0);
            }
        }

        return output;
    }

    private int[] round(double[] input) {
        if (input.length == 1) {
            return Arrays.stream(input).mapToInt(x -> (int) Math.round(x)).toArray();
        }

        List<Integer> sortedIndices = IntStream.range(0, input.length).boxed()
                .sorted(Comparator.comparingDouble(i -> input[i] - Math.floor(input[i]))).collect(Collectors.toList());

        int[] output = new int[input.length];
        int left = 100 - DoubleStream.of(input).mapToInt(x -> (int) Math.floor(x)).sum();
        for (int i = 0; i < output.length; i++) {
            output[i] = (int) Math.floor(input[i]);
            if (sortedIndices.indexOf(i) < left) {
                output[i]++;
            }
        }

        return output;
    }

    private void sortIntoSpecies() {
        this.species.forEach(Species::clear);

        outer:
        for (Network network : generation) {
            for (Species species : this.species) {
                if (species.add(network)) {
                    continue outer;
                }
            }

            Species species = new Species(network);
            this.species.add(species);
        }

        this.species = this.species.stream().filter(Species::notEmpty).collect(Collectors.toList());
    }

    public String[] getDisplay() {
        return new String[]{
                "generation " + currentGeneration + ", network " + (currentNetwork + 1) + "/" + generationSize,
                generation[currentNetwork].toString()
        };
    }
}