package pepse.world.trees;

import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.util.Vector2;
import pepse.world.Block;

import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Represents the leaves of a tree.
 */
public class Leaves {

    private static final int MAX_TIME_BEFORE_FALLING = 60;
    private static final int PRIME_NUM = 31;
    private static final int LEAF_RADIUS = 5;
    private final GameObjectCollection gameObject;
    private final Function<Float, Float> groundHeightAtFunc;
    private final int leavesLayer;
    private final int treeHeight;
    private final int treeX;
    private final int mainSeed;

    /**
     * constructor.
     *
     * @param treeX              - The X positions of the tree.
     * @param gameObject         - The collection of all participating game objects.
     * @param groundHeightAtFunc - A function that return the ground height at a given location.
     * @param leavesLayer        - The number of the layer to which the created leaf objects should be added.
     * @param treeHeight         - The height of the tree.
     * @param mainSeed           - The main seed of the game.
     */
    public Leaves(int treeX, GameObjectCollection gameObject, Function<Float, Float> groundHeightAtFunc,
                  int leavesLayer, int treeHeight, int mainSeed) {
        this.gameObject = gameObject;
        this.groundHeightAtFunc = groundHeightAtFunc;
        this.leavesLayer = leavesLayer;
        this.treeHeight = treeHeight;
        this.treeX = treeX;
        this.mainSeed = mainSeed;
        create();
    }

    // creates the actual leaf blocks, also setting up a scheduled task for the animation and falling actions.
    private void create() {
        for (int j = 0; j < LEAF_RADIUS; j++) {
            for (int k = 1; k <= LEAF_RADIUS; k++) {
                Vector2 leafPotion = new Vector2(treeX - Block.SIZE * 2 + Block.SIZE * j,
                        groundHeightAtFunc.apply((float) treeX) - Block.SIZE * (treeHeight + k - 1));
                Leaf leaf = new Leaf(leafPotion, gameObject, leavesLayer);
                leaf.setRandomizer(mainSeed);

                new ScheduledTask(leaf,
                        new Random(Objects.hash(leafPotion.x() * PRIME_NUM + leafPotion.y(),
                                mainSeed)).nextFloat(),
                        false, leaf::animate);

                new ScheduledTask(leaf,
                        new Random(Objects.hash(leafPotion.x() * PRIME_NUM + leafPotion.y(),
                                mainSeed)).nextInt(MAX_TIME_BEFORE_FALLING),
                        false, leaf::fall);
            }
        }
    }
}
