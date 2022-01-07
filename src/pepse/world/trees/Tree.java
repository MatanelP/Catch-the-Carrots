package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import pepse.world.Block;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Responsible for the creation and management of trees.
 */
public class Tree {

    private static final int TREE_HEIGHT = 6;
    private static final double TREE_PLACEMENT_LIMIT = 0.9;
    public static final int MAX_HEIGHT_BOUND = 4;
    private final GameObjectCollection gameObject;
    private final int treeLayer;
    private final Function<Float, Float> groundHeightAtFunc;
    private static HashMap<Integer, Boolean> isTreePossibleInX;
    private static HashMap<Integer, Boolean> isThereTreeInX;
    private Random mainRandom;
    private int mainSeed;

    /**
     * constructor
     *
     * @param gameObject         -  The collection of all participating game objects.
     * @param treeLayer          - The number of the layer to which the created trees objects should be added.
     * @param groundHeightAtFunc - A function that return the ground height at a given location.
     */
    public Tree(GameObjectCollection gameObject, int treeLayer, Function<Float, Float> groundHeightAtFunc) {
        this.gameObject = gameObject;
        this.treeLayer = treeLayer;
        this.groundHeightAtFunc = groundHeightAtFunc;
        isTreePossibleInX = new HashMap<>();
        isThereTreeInX = new HashMap<>();

    }

    /**
     * This method creates trees in a given range of x-values.
     *
     * @param minX - The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX - The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void createInRange(int minX, int maxX) {
        for (int i = minX; i < maxX; i++) {
            int heightChange = new Random(Objects.hash(i, mainSeed)).nextInt(MAX_HEIGHT_BOUND) - 2;
            if (i % Block.SIZE != 0) {
                continue;
            }
            if (!isTreePossibleInX.containsKey(i))
                isTreePossibleInX.put(i, mainRandom.nextDouble() > TREE_PLACEMENT_LIMIT);

            if (isTreePossibleInX.get(i) && (isThereTreeInX.get(i) == null || !isThereTreeInX.get(i))) {
                isThereTreeInX.put(i, true);
                new Trunk(i, gameObject, groundHeightAtFunc, treeLayer + 1,
                        TREE_HEIGHT + heightChange);
                new Leaves(i, gameObject, groundHeightAtFunc, treeLayer + 2,
                        TREE_HEIGHT + heightChange, mainSeed);
            }
        }
    }


    /**
     * This method deletes trees in a given range of x-values.
     *
     * @param minX - The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX - The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void deleteInRange(int minX, int maxX) {
        for (int i = minX; i < maxX; i++) {
            if (i % Block.SIZE != 0 || !isThereTreeInX.containsKey(i)) {
                continue;
            }
            isThereTreeInX.put(i, false);
        }
        for (GameObject block : gameObject.objectsInLayer(treeLayer + 1)) {
            if (block.getTag().equals(Trunk.TRUNK_TAG) &&
                    block.getTopLeftCorner().x() <= maxX && block.getTopLeftCorner().x() >= minX)
                gameObject.removeGameObject(block, treeLayer + 1);
        }
        for (GameObject block : gameObject.objectsInLayer(treeLayer + 2)) {
            if (block.getTag().equals(Leaf.LEAF_TAG) &&
                    block.getTopLeftCorner().x() <= maxX && block.getTopLeftCorner().x() >= minX)
                gameObject.removeGameObject(block, treeLayer + 2);
        }
    }


    /**
     * set up the random object and the seed of the whole game, in order to maintain reconstructed randomly.
     *
     * @param mainRandom - The main Random object of the whole game.
     * @param mainSeed   - The main seed of the whole game.
     */
    public void setRandomaizer(Random mainRandom, int mainSeed) {
        this.mainRandom = mainRandom;
        this.mainSeed = mainSeed;
    }
}
