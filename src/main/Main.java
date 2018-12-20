package main;

import nintaco.api.API;
import nintaco.api.ApiSource;
import specie.Generation;
import specie.Specie;

import java.time.LocalTime;

public class Main {
    public static final API api = ApiSource.getAPI();
    private static final int speed = 400;

    public static final int generationSize = 50;
    public static final double percentToBreed = 0.2;
    public static final double baseMutationRate = 0.04;

    private static Generation generation = new Generation();
    private static Specie currentSpecie;

    private static double resumeTime = 0;

    public static void main(String[] args) {
        currentSpecie = generation.nextSpecie();

        api.addFrameListener(() -> {
            if (System.currentTimeMillis() < resumeTime) {
                return;
            }

            api.setSpeed(speed);

            if (currentSpecie == null) {
                return;
            }

            if (!currentSpecie.runFrame()) {
                currentSpecie = generation.nextSpecie();
                resumeTime = System.currentTimeMillis() + 300;
            }

            generation.display();
        });

        api.run();
    }
}
