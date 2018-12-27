package main;

import static main.Constants.*;
import nintaco.api.API;
import nintaco.api.ApiSource;
import nintaco.api.Colors;
import species.Generation;
import species.Network;

public class Main {
    public static final API api = ApiSource.getAPI();

    private static Generation generation;
    private static Network currentNetwork;

    private static long resumeTime = 0;

    public static void main(String[] args) {
        generation = new Generation();
        currentNetwork = generation.nextNetwork();

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

            if (currentNetwork == null) {
                return;
            }

            if (currentNetwork.runFrame()) {
                currentNetwork = generation.nextNetwork();
                resumeTime = System.currentTimeMillis() + waitTime;
            }
        });

        api.run();
    }
}
