package species;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Species implements Comparable<Species> {
    private Network representative;
    private List<Network> networks = new ArrayList<>();

    private final int id;
    private static int nextId = 0;
    { id = ++nextId; }

    private Random random = new Random();

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

    public int getId() {
        return id;
    }

    int getSize() {
        return networks.size();
    }

    double getAdjustedFitness() {
        return ((double) networks.stream().mapToInt(Network::getFitness).sum()) / getSize();
    }

    boolean notEmpty() {
        return !networks.isEmpty();
    }

    void reset() {
        representative = networks.get(random.nextInt(networks.size()));
        networks.clear();
    }

    @Override
    public int compareTo(Species other) {
        return Double.compare(this.getAdjustedFitness(), other.getAdjustedFitness());
    }
}
