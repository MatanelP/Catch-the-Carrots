package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.function.Function;

/**
 * Represents a Trunk in the game
 */
public class Trunk {

    public static final String TRUNK_TAG = "trunk";
    private final int treeX;
    private final GameObjectCollection gameObject;
    private final Function<Float, Float> groundHeightAtFunc;
    private final int trunkLayer;
    private final int treeHeight;
    private static final Color BASE_TRUNK_COLOR = new Color(100, 50, 20);


    /**
     * constructor.
     *
     * @param treeX              - The X positions of the tree.
     * @param gameObject         - The collection of all participating game objects.
     * @param groundHeightAtFunc - A function that return the ground height at a given location.
     * @param trunkLayer         - The number of the layer to which the created trunk objects should be added.
     * @param treeHeight         - The height of the tree.
     */
    public Trunk(int treeX, GameObjectCollection gameObject, Function<Float, Float> groundHeightAtFunc,
                 int trunkLayer, int treeHeight) {
        this.treeX = treeX;
        this.gameObject = gameObject;
        this.groundHeightAtFunc = groundHeightAtFunc;
        this.trunkLayer = trunkLayer;
        this.treeHeight = treeHeight;
        createTrunk();
    }

    // creates the blocks that display the trunk
    private void createTrunk() {
        for (int j = 1; j <= treeHeight; j++) {
            GameObject trunk_block = new Block(new Vector2(treeX,
                    groundHeightAtFunc.apply((float) treeX) - j * Block.SIZE),
                    new RectangleRenderable(ColorSupplier.approximateColor(BASE_TRUNK_COLOR)));
            trunk_block.setTag(TRUNK_TAG);
            gameObject.addGameObject(trunk_block, trunkLayer);
        }
    }
}
