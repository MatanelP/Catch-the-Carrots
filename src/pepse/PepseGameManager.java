package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.*;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Tree;

import java.awt.*;
import java.util.Random;

/**
 * The main class of the simulator.
 */
public class PepseGameManager extends GameManager {
    private static final int TERRAIN_BUFFER = 60;
    public static final int SEED = 9;
    private static final Random mainRandom = new Random(SEED);
    private static final Color SUN_HALO_COLOR = new Color(255, 255, 0, 20);
    private static final int SUN_HALO_LAYER_OFFSET = 10;
    private static final int CYCLE_LENGTH = 30;
    private static final int TERRAIN_TOP_BLOCKS_LAYER_OFFSET = 21;
    private static final int TREE_TRUNK_LAYER_OFFSET = 22;

    /**
     * Runs the entire simulation.
     *
     * @param args - not in use.
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }


    /**
     * The method will be called once when a GameGUIComponent is created,
     * and again after every invocation of windowController.resetGame().
     *
     * @param imageReader      Contains a single method: readImage, which reads an image from disk.
     *                         See its documentation for help.
     * @param soundReader      Contains a single method: readSound, which reads a wav file from
     *                         disk. See its documentation for help.
     * @param inputListener    Contains a single method: isKeyPressed, which returns whether
     *                         a given key is currently pressed by the user or not. See its
     *                         documentation.
     * @param windowController Contains an array of helpful, self-explanatory methods
     *                         concerning the window.
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);

        Terrain terrain = setUpBackGround(windowController);

        Vector2 initialAvatarLocation = new Vector2(windowController.getWindowDimensions().x() / 2,
                terrain.groundHeightAt(windowController.getWindowDimensions().x() / 2) - Block.SIZE);

        Avatar avatar = setUpActiveElements(imageReader, inputListener, windowController, terrain,
                initialAvatarLocation);

        generalSettings(windowController, initialAvatarLocation, avatar);
    }

    // setting up general game related settings
    private void generalSettings(WindowController windowController, Vector2 initialAvatarLocation,
                                 Avatar avatar) {
        // camera
        setCamera(new Camera(avatar,
                windowController.getWindowDimensions().mult(0.5f).subtract(initialAvatarLocation),
                windowController.getWindowDimensions(), windowController.getWindowDimensions()));

        // setting up coalitions (avatar - trunk, leaf - ground)
        gameObjects().layers().shouldLayersCollide(Layer.BACKGROUND + TREE_TRUNK_LAYER_OFFSET,
                Layer.STATIC_OBJECTS + 1, true);
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT,
                Layer.BACKGROUND + TERRAIN_TOP_BLOCKS_LAYER_OFFSET, true);
    }

    // setting up game elements which are active in some sense
    private Avatar setUpActiveElements(ImageReader imageReader, UserInputListener inputListener,
                                       WindowController windowController, Terrain terrain,
                                       Vector2 initialAvatarLocation) {
        // tree
        Tree tree = new Tree(gameObjects(), Layer.BACKGROUND + 20, terrain::groundHeightAt);
        tree.setRandomaizer(mainRandom, SEED);
        tree.createInRange(0, (int) windowController.getWindowDimensions().x());

        // avatar
        Avatar avatar = Avatar.create(gameObjects(), Layer.DEFAULT, initialAvatarLocation,
                inputListener, imageReader);
        avatar.setTerrainChange(terrain, tree, windowController,
                TERRAIN_BUFFER);

        // base carrot
        gameObjects().addGameObject(new Carrot(avatar, gameObjects(), Vector2.ZERO, Vector2.ZERO,
                null, Layer.DEFAULT + 1, imageReader, windowController.getWindowDimensions()));
        Carrot.setGroundHeightAtFunc(terrain::groundHeightAt);
        Carrot.create();
        return avatar;
    }

    // setting up all game related background elements
    private Terrain setUpBackGround(WindowController windowController) {
        // sky
        Sky.create(gameObjects(), windowController.getWindowDimensions(), Layer.BACKGROUND);

        // terrain
        Terrain terrain = new Terrain(gameObjects(), Layer.STATIC_OBJECTS,
                windowController.getWindowDimensions(), SEED);
        terrain.createInRange(0, (int) windowController.getWindowDimensions().x() + TERRAIN_BUFFER);

        // night
        Night.create(gameObjects(), Layer.FOREGROUND, windowController.getWindowDimensions(), CYCLE_LENGTH);

        // sun
        GameObject sun = Sun.create(gameObjects(), Layer.BACKGROUND + 1,
                windowController.getWindowDimensions(), CYCLE_LENGTH);

        // sun-halo
        SunHalo.create(gameObjects(), Layer.BACKGROUND + SUN_HALO_LAYER_OFFSET, sun, SUN_HALO_COLOR);
        return terrain;
    }
}
