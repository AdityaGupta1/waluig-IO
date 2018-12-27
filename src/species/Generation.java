package species;

import static main.Constants.generationSize;

public class Generation {
    private int currentGeneration = 0;
    private final Network[] generation = new Network[generationSize];

    public Generation() {
        for (int i = 0; i < generationSize; i++) {
            generation[i] = new Network();
        }
    }

    private void advance() {
        // TODO
        printStats();
    }

    private Network nextNetwork() {
        // TODO
        return null;
    }

    private void printStats() {
        // TODO
    }

    public String[] getDisplay() {
        // TODO
        return null;
    }
}