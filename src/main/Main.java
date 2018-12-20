package main;

import nintaco.api.API;
import nintaco.api.ApiSource;
import nintaco.api.Colors;
import specie.Generation;
import specie.Specie;

public class Main {
    public static final API api = ApiSource.getAPI();
    private static final int speed = 400;

    public static final int generationSize = 10;
    public static final double percentToBreed = 0.2;
    public static final double baseMutationRate = 0.04;

    private static Generation generation = new Generation();
    private static Specie currentSpecie;

    private static final long waitTime = 1000;
    private static long resumeTime = 0;

    public static void main(String[] args) {
        currentSpecie = generation.nextSpecie();

        api.addFrameListener(() -> {
            api.setColor(Colors.WHITE);
            api.drawString(generation.getDisplay()[0], 10, 35, true);
            api.drawString(generation.getDisplay()[1], 10, 45, true);
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
