package main;

import nintaco.api.API;
import nintaco.api.ApiSource;
import specie.Generation;
import specie.Specie;

public class Main {
    public static final API api = ApiSource.getAPI();

    public static final int generationSize = 50;
    public static final double percentToBreed = 0.2;
    public static final double baseMutationRate = 0.04;

    private static Generation generation = new Generation();
    private static Specie currentSpecie;

    public static void main(String[] args) {
        currentSpecie = generation.nextSpecie();

        api.addFrameListener(() -> {
            if (currentSpecie == null) {
                return;
            }

            if (!currentSpecie.runFrame()) {
                currentSpecie = generation.nextSpecie();
            }

            generation.display();
        });

        api.run();
    }
}
