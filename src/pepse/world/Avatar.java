package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;
import pepse.PepseGameManager;
import pepse.world.trees.Tree;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Random;

/**
 * An avatar can move around the world.
 */
public class Avatar extends GameObject {

    public static final String AVATAR_TAG = "avatar";
    private static final float GRAVITY = 500;
    private static final float VELOCITY_X = 300;
    private static final float VELOCITY_Y = -300;
    private static final int AVATAR_MAX_Y_VELOCITY = 500;
    private static final int ENERGY_TEXT_DIS_ABOVE_AVATAR = 50;
    private static final int MAX_ENERGY = 100;
    private static final int AVATAR_SIZE = 70;
    private static final int ENERGY_TEXT_SIZE = 20;
    private static final float TIME_BETWEEN_CLIPS = 0.05f;
    private static final float SIZE_Y_FACTOR = 1.2f;
    public static final String PEPSE_ASSETS_AVATAR_RIGHT_1_PNG = "pepse/assets/avatar-right1.png";
    public static final String PEPSE_ASSETS_AVATAR_RIGHT_2_PNG = "pepse/assets/avatar-right2.png";
    public static final String PEPSE_ASSETS_AVATAR_RIGHT_3_PNG = "pepse/assets/avatar-right3.png";
    public static final String PEPSE_ASSETS_AVATAR_RIGHT_4_PNG = "pepse/assets/avatar-right4.png";
    public static final String PEPSE_ASSETS_AVATAR_RIGHT_5_PNG = "pepse/assets/avatar-right5.png";
    public static final String PEPSE_ASSETS_AVATAR_RIGHT_6_PNG = "pepse/assets/avatar-right6.png";
    public static final int PRIME_NUM = 31;
    public static final int MID_AIR_INDICATOR = 200;
    public static final String PEPSE_ASSETS_AVATAR_LAND_RIGHT_PNG = "pepse/assets/avatar-land-right.png";
    public static final String PEPSE_ASSETS_AVATAR_STILL_RIGHT_PNG = "pepse/assets/avatar-still-right.png";
    public static final String PEPSE_ASSETS_AVATAR_FLY_RIGHT_PNG = "pepse/assets/avatar-fly-right.png";
    public static final String PEPSE_ASSETS_AVATAR_FIRE_JPG = "pepse/assets/avatar-fire.jpg";
    private static GameObjectCollection gameObjects;
    private static ImageReader imageReader;
    private static AnimationRenderable running_animation;
    private static Avatar avatar;
    private static UserInputListener inputListener;
    private static double energy;
    private static GameObject energyText;
    private static TextRenderable energyTextRenderable;
    private final Random random;
    private Terrain terrain;
    private float windowHalfX;
    private int terrain_buffer;
    private boolean isRunningAnimation;
    private Tree tree;
    private boolean isFacingRight = true;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Avatar(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable) {
        super(topLeftCorner, dimensions, renderable);
        this.random = new Random(Objects.hash(PepseGameManager.SEED,
                getTopLeftCorner().x() * PRIME_NUM + getTopLeftCorner().y()));
        running_animation = new AnimationRenderable(new String[]{
                PEPSE_ASSETS_AVATAR_RIGHT_1_PNG,
                PEPSE_ASSETS_AVATAR_RIGHT_2_PNG,
                PEPSE_ASSETS_AVATAR_RIGHT_3_PNG,
                PEPSE_ASSETS_AVATAR_RIGHT_4_PNG,
                PEPSE_ASSETS_AVATAR_RIGHT_5_PNG,
                PEPSE_ASSETS_AVATAR_RIGHT_6_PNG},
                imageReader, true, TIME_BETWEEN_CLIPS);

    }

    /**
     * This function creates an avatar that can travel the world and is followed by the camera.
     * The can stand, walk, jump and fly, and never reaches the end of the world.
     *
     * @param gameObjects   - The collection of all participating game objects.
     * @param layer         - The number of the layer to which the created avatar should be added.
     * @param topLeftCorner - The location of the top-left corner of the created avatar.
     * @param inputListener - Used for reading input from the user.
     * @param imageReader   - Used for reading images from disk or from within a jar.
     * @return A newly created representing the avatar.
     */
    public static Avatar create(GameObjectCollection gameObjects, int layer, Vector2 topLeftCorner,
                                UserInputListener inputListener, ImageReader imageReader) {
        Avatar.gameObjects = gameObjects;
        Avatar.imageReader = imageReader;
        avatar = new Avatar(topLeftCorner, new Vector2(AVATAR_SIZE, AVATAR_SIZE * SIZE_Y_FACTOR), null);
        avatar.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        avatar.transform().setAccelerationY(GRAVITY);
        Avatar.inputListener = inputListener;
        Avatar.energyTextRenderable = new TextRenderable("");
        energyTextRenderable.setColor(Color.RED);
        Avatar.energyText = new GameObject(Vector2.ZERO, Vector2.ONES.mult(ENERGY_TEXT_SIZE),
                energyTextRenderable);

        gameObjects.addGameObject(energyText, Layer.UI);

        avatar.setTag(AVATAR_TAG);
        Avatar.energy = MAX_ENERGY;
        gameObjects.addGameObject(avatar, layer);
        return avatar;
    }

    /**
     * set up variables regarding the visual objects of the game to handel deletion and creation.
     *
     * @param terrain          - an object handling the terrain of the game.
     * @param tree             - an object handling the trees of the game.
     * @param windowController - for window dimensions.
     * @param terrain_buffer   - buffer fixes for visual glitches.
     */
    public void setTerrainChange(Terrain terrain,
                                 Tree tree, WindowController windowController, int terrain_buffer) {
        this.terrain = terrain;
        this.windowHalfX = windowController.getWindowDimensions().x() / 2f;
        this.terrain_buffer = terrain_buffer;
        this.tree = tree;
    }

    /**
     * Should be called once per frame.
     *
     * @param deltaTime The time elapsed, in seconds, since the last frame. Can
     *                  be used to determine a new position/velocity by multiplying
     *                  this delta with the velocity/acceleration respectively
     *                  and adding to the position/velocity:
     *                  velocity += deltaTime*acceleration
     *                  pos += deltaTime*velocity
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float avatarX = avatar.getTopLeftCorner().x();
        float xVel = 0;

        energyText.setCenter(avatar.getCenter().add(Vector2.UP.mult(ENERGY_TEXT_DIS_ABOVE_AVATAR)));
        energyTextRenderable.setString("" + (int) energy);
        this.isRunningAnimation = false;

        // avatar is mid air
        if (Math.abs(getVelocity().y()) > MID_AIR_INDICATOR)
            renderer().setRenderable(imageReader.readImage(PEPSE_ASSETS_AVATAR_LAND_RIGHT_PNG, true));

        // limiting the max downwards velocity
        if (avatar.getVelocity().y() > AVATAR_MAX_Y_VELOCITY)
            avatar.setVelocity(new Vector2(avatar.getVelocity().x(), AVATAR_MAX_Y_VELOCITY));

        // applying changes according to user input (moving left or right)
        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) xVel = avatarHeadingLeft(avatarX, xVel);
        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) xVel = avatarHeadingRight(avatarX, xVel);
        transform().setVelocityX(xVel);

        // avatar is not moving horizontally
        if (getVelocity().y() == 0) {
            if (!isRunningAnimation)
                renderer().setRenderable(imageReader.readImage(PEPSE_ASSETS_AVATAR_STILL_RIGHT_PNG, true));
            if (energy < MAX_ENERGY) energy += 0.5f;
            if (inputListener.isKeyPressed(KeyEvent.VK_SPACE)) transform().setVelocityY(VELOCITY_Y);
        }

        // avatar in flying
        if (energy > 0 && inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                inputListener.isKeyPressed(KeyEvent.VK_SHIFT)) {
            transform().setVelocityY(VELOCITY_Y);
            energy -= 0.5f;
            renderer().setRenderable(imageReader.readImage(PEPSE_ASSETS_AVATAR_FLY_RIGHT_PNG, true));
        }

        // randomly creating carrots:
        if (random.nextFloat() > 0.99)
            Carrot.create();
        // avatar in firing
        if (energy > 2 && inputListener.isKeyPressed(KeyEvent.VK_CONTROL)) {
            energy -= 2f;
            renderer().setRenderable(imageReader.readImage(PEPSE_ASSETS_AVATAR_FIRE_JPG, true));
            FireBall.create(gameObjects, imageReader, getTopLeftCorner(), isFacingRight);
        }
    }

    // when avatar is heading right, apply animation, create and delete object according to location.
    private float avatarHeadingRight(float avatarX, float xVel) {
        this.isFacingRight = true;
        renderer().setIsFlippedHorizontally(false);
        startRunningAnimation();
        xVel += VELOCITY_X;

        terrain.createInRange((int) (avatarX + windowHalfX), (int) (avatarX + windowHalfX) + terrain_buffer);

        tree.createInRange((int) (avatarX + windowHalfX), (int) (avatarX + windowHalfX) + terrain_buffer);

        terrain.deleteInRange((int) (avatarX - windowHalfX) - terrain_buffer * 2,
                (int) (avatarX - windowHalfX) - terrain_buffer);

        tree.deleteInRange((int) (avatarX - windowHalfX) - terrain_buffer * 2,
                (int) (avatarX - windowHalfX) - terrain_buffer);

        return xVel;
    }


    // when avatar is heading left, apply animation, create and delete object according to location.
    private float avatarHeadingLeft(float avatarX, float xVel) {
        this.isFacingRight = false;
        renderer().setIsFlippedHorizontally(true);
        startRunningAnimation();
        xVel -= VELOCITY_X;

        terrain.createInRange((int) (avatarX - windowHalfX) - terrain_buffer, (int) (avatarX - windowHalfX));

        tree.createInRange((int) (avatarX - windowHalfX) - terrain_buffer, (int) (avatarX - windowHalfX));

        terrain.deleteInRange((int) (avatarX + windowHalfX) + terrain_buffer,
                (int) (avatarX + windowHalfX) + terrain_buffer * 2);

        tree.deleteInRange((int) (avatarX + windowHalfX) + terrain_buffer,
                (int) (avatarX + windowHalfX) + terrain_buffer * 2);
        return xVel;
    }

    // apply the running animation for the avatar.
    private void startRunningAnimation() {
        if (!isRunningAnimation && getVelocity().y() == 0) {
            renderer().setRenderable(running_animation);
            this.isRunningAnimation = true;
        }
    }


}
