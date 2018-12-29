package main;

public class Constants {
    public static final int speed = 800;

    public static final boolean displayDisabledConnections = true;

    public static final int generationSize = 100;
    public static final int generationTopNumber = 10; // how many top networks to calculate stats for

    public static final double pressThreshold = 0;

    public static final long waitTime = 200;
    public static final int framesBeforeReset = 60;

    public static final double crossoverChance = 0.75; // chance that two networks will cross over (as opposed to one network simply mutating on its own)
    public static final double speciesTopPercent = 0.1; // percent of an individual species to breed (rounds up)

    public static final double addConnectionChance = 1.5; // chance to add a connection between two random existing nodes
    public static final double addNodeChance = 0.5; // chance to add a node that splits a connection in two
    public static final double mutateWeightsChance = 0.25; // chance to select an individual network for weight mutations
    public static final double mutateWeightChance = 0.9; // chance for an individual weight to change
    public static final double randomWeightMax = 2; // maximum absolute value for assigning a new, random weight
    public static final double mutateWeightStep = 0.1; // maximum value to change an individual weight by
    public static final double enableChance = 0.2; // chance to enable a random connection
    public static final double disableChance = 0.4; // chance to disable a random enabled connection

    public static final double deltaDisjoint = 0.8;
    public static final double deltaExcess = 1.0;
    public static final double deltaWeights = 0.4;
    public static final double compatibilityThreshold = 8.0;
}
