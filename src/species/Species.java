package species;

import java.util.ArrayList;
import java.util.List;

public class Species {
    private Network representative;
    private List<Network> networks = new ArrayList<>();
    private final int id;
    private static int nextId = 0;

    {
        id = ++nextId;
    }

    public Species(Network representative) {
        this.representative = representative;
        add(representative);
    }

    public boolean add(Network network) {
        if (representative.isCompatible(network)) {
            networks.add(network);
            network.setSpecies(id);
            return true;
        } else {
            return false;
        }
    }

    public List<Network> getNetworks() {
        return new ArrayList<>(networks);
    }

    public int getSize() {
        return networks.size();
    }

    public void clear() {
        networks.clear();
    }
}
