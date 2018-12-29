package species;

import java.util.ArrayList;
import java.util.List;

class Species {
    private Network representative;
    private List<Network> networks = new ArrayList<>();
    private final int id;
    private static int nextId = 0;

    {
        id = ++nextId;
    }

    Species(Network representative) {
        this.representative = representative;
        add(representative);
    }

    boolean add(Network network) {
        if (representative.isCompatible(network)) {
            networks.add(network);
            network.setSpecies(id);
            return true;
        } else {
            return false;
        }
    }

    List<Network> getNetworks() {
        return new ArrayList<>(networks);
    }

    int getSize() {
        return networks.size();
    }

    boolean notEmpty() {
        return !networks.isEmpty();
    }

    void clear() {
        networks.clear();
    }
}
