import nintaco.api.API;
import nintaco.api.ApiSource;

public class Main {
    private static final API api = ApiSource.getAPI();

    public static void main(String[] args) {
        api.addFrameListener(() -> {
            System.out.println(api.readCPU(0x071C));
        });

        api.run();
    }
}
