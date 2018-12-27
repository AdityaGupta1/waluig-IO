package main;

public class Constants {
    public static final int speed = 800;

    public static final int generationSize = 100;
    public static final double pressThreshold = 0.5;

    public static final long waitTime = 200;
    public static final int framesBeforeReset = 60;

    public static final double addConnectionChance = 0.9; // chance to add a connection between two random existing nodes
    public static final double addNodeChance = 0.5; // chance to add a node that splits a connection in two
    public static final double mutateWeightsChance = 0.25; // 10% chance to leave all weights untouched
    public static final double mutateWeightChance = 0.9; // 25% chance for an individual weight to change
    public static final double mutateWeightStep = 0.1; // maximum value to change an individual weight by
    public static final double enableChance = 0.2; // chance to enable a random connection
    public static final double disableChance = 0.4; // chance to disable a random enabled connection

    public static final double deltaDisjoint = 2.0;
    public static final double deltaWeights = 0.4;
    public static final double compatibilityThreshold = 3.0;
}
