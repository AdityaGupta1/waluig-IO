package species;

import main.Main;
import nintaco.api.API;

import static main.Constants.*;

public class Generation {
    private static final API api = Main.api;

    private int currentGeneration = 1;
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