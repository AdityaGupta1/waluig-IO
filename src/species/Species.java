package species;

import java.util.ArrayList;
import java.util.List;

public class Species {
    private Network representative;
    private List<Network> networks = new ArrayList<>();

    public boolean add(Network network) {
        if (representative.isCompatible(network)) {
            networks.add(network);
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        networks.clear();
    }
}
