package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.*;
import java.util.HashSet;

/**
 * Responsible for the creation and management of terrain.
 */
public class Terrain {
    public static final String GROUND_TAG = "ground";
    private static final float GROUND_HEIGHT_FACTOR = 0.75f;
    private final GameObjectCollection gameObjects;
    private final int groundLayer;
    private final Vector2 windowDimensions;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final int TERRAIN_DEPTH = 20;
    private static HashSet<Integer> isTerrainForX;
    private final NoiseGenerator gen;


    /**
     * constructor
     *
     * @param gameObjects      - The collection of all participating game objects.
     * @param groundLayer      - The number of the layer to which the created ground objects should be added.
     * @param windowDimensions - The dimensions of the windows.
     * @param seed             - A seed for a random number generator.
     */
    public Terrain(GameObjectCollection gameObjects, int groundLayer, Vector2 windowDimensions, int seed) {
        this.gameObjects = gameObjects;
        this.groundLayer = groundLayer;
        this.windowDimensions = windowDimensions;
        isTerrainForX = new HashSet<>();
        gen = new NoiseGenerator(Math.floor(seed));
    }

    /**
     * This method return the ground height at a given location.
     *
     * @param x - A number.
     * @return The ground height at the given location.
     */
    public float groundHeightAt(float x) {
        return windowDimensions.y() * (GROUND_HEIGHT_FACTOR - (float) gen.noise(x / Block.SIZE));
    }

    /**
     * This method creates terrain in a given range of x-values.
     *
     * @param minX - The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX - The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void createInRange(int minX, int maxX) {
        for (int i = minX; i < maxX; i++) {
            if (i % Block.SIZE != 0 || isTerrainForX.contains(i)) {
                continue;
            }
            isTerrainForX.add(i);
            float blockY = groundHeightAt(i);
            Block block = new Block(new Vector2(i, blockY),
                    new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR)));
            block.setTag(GROUND_TAG);
            gameObjects.addGameObject(block, groundLayer + 1);
            for (int j = 0; j < TERRAIN_DEPTH; j++) { //adding blocks below surface
                block = new Block(new Vector2(i, blockY + j * Block.SIZE),
                        new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR)));
                block.setTag(GROUND_TAG);
                block.getDimensions().y();
                gameObjects.addGameObject(block, groundLayer);
            }
        }
    }

    /**
     * This method deletes terrain in a given range of x-values.
     *
     * @param minX - The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX - The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void deleteInRange(int minX, int maxX) {
        for (int i = minX; i < maxX; i++) {
            if (i % Block.SIZE != 0 || !isTerrainForX.contains(i)) {
                continue;
            }
            isTerrainForX.remove(i);
        }
        for (GameObject block : gameObjects.objectsInLayer(groundLayer)) {
            if (block.getTag().equals(GROUND_TAG) &&
                    block.getTopLeftCorner().x() <= maxX && block.getTopLeftCorner().x() >= minX)
                gameObjects.removeGameObject(block, groundLayer);
        }
        for (GameObject block : gameObjects.objectsInLayer(groundLayer + 1)) {
            if (block.getTag().equals(GROUND_TAG) &&
                    block.getTopLeftCorner().x() <= maxX && block.getTopLeftCorner().x() >= minX)
                gameObjects.removeGameObject(block, groundLayer + 1);
        }

    }
}
