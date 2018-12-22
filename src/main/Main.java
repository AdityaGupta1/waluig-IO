package main;

import nintaco.api.API;
import nintaco.api.ApiSource;
import nintaco.api.Colors;
import specie.Generation;
import specie.Specie;

public class Main {
    public static final API api = ApiSource.getAPI();
    private static final int speed = 800;

    public static final int generationSize = 100;
    public static final double percentToBreed = 0.1;
    public static final boolean singleParent = false;
    public static final double pressThreshold = 0.5;
    public static final double baseMutationRate = 0.05;
    public static final double mutationRatePerStaleGeneration = 0.02;
    public static final int maxStaleGenerations = 10;

    private static Generation generation;
    private static Specie currentSpecie;

    private static final long waitTime = 200;
    private static long resumeTime = 0;
    public static final int framesBeforeReset = 60;

    public static void main(String[] args) {
        generation = new Generation();
        currentSpecie = generation.nextSpecie();

        api.addFrameListener(() -> {
            api.setColor(Colors.WHITE);
            String[] display = generation.getDisplay();
            for (int i = 0; i < display.length; i++) {
                api.drawString(generation.getDisplay()[i], 10, 35 + (10 * i), false);
            }
            api.setSpeed(speed);

            if (System.currentTimeMillis() < resumeTime) {
                return;
            }

            if (currentSpecie == null) {
                return;
            }

            if (!currentSpecie.runFrame()) {
                currentSpecie = generation.nextSpecie();
                resumeTime = System.currentTimeMillis() + waitTime;
            }
        });

        api.run();
    }
}
