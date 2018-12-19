package main;

import nintaco.api.API;
import nintaco.api.ApiSource;

public class Main {
    public static final API api = ApiSource.getAPI();

    private static final int generationSize = 50;

    public static void main(String[] args) {
        api.addFrameListener(() -> {
            System.out.println(api.readCPU(0x071C));
        });

        api.run();
    }
}
