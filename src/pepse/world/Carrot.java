package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.ImageReader;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;
import pepse.PepseGameManager;

import java.awt.*;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class Carrot extends GameObject {
    public static final int CARROT_SIZE = 30;
    public static final String CARROT_IMG_PATH = "pepse/assets/carrot.png";
    public static final int CARROT_TEXT_X_OFFSET = 90;
    public static final float CARROT_MOVEMENT_CHANGE_FACTOR = 0.95f;
    public static final int CARROT_ANGLE_HEADING_UP = 90;
    private static final float LIFE_SPAN = 30;
    public static final int Y_BOUND = 300;
    public static final int X_BOUND = 1000;
    public static final int X_BOUND_OFFSET = 500;
    public static final int TIMER_TEXT_Y_OFFSET = 100;
    public static final int PRIME_NUM = 31;
    public static final int TIMER_TEXT_SIZE = 40;
    public static final int TIMER_TEXT_X_OFFSET = 60;
    private static GameObject timerText;
    private static TextRenderable timerTextRenderable;
    public static int carrotDestroyed = 0;
    public static int highScore = 0;
    public static final int CARROT_SPEED = 400;
    private static final float CARROT_TEXT_SIZE = TIMER_TEXT_Y_OFFSET;
    public static final String CARROTS_DESTROYED_SO_FAR_TEXT =
            "Use Ctrl to destroy as many flying carrots\nas you can before the timer ends!" +
                    "\nSo far destroyed: %d\nHigh score: %d";
    private static TextRenderable carrotTextRenderable;
    private static GameObject carrotText;
    private static int carrotLayer;
    private static ImageReader imageReader;
    private static Vector2 windowDimensions;


    private static GameObjectCollection gameObjects;
    private static Avatar avatar;
    private static Random random;
    private static Function<Float, Float> groundHeightAtFunc;

    /**
     * Construct a new GameObject instance.
     *
     * @param avatar        The avatar of the game to interact with.
     * @param gameObjects   The collection of all participating game objects.
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Carrot(Avatar avatar, GameObjectCollection gameObjects, Vector2 topLeftCorner, Vector2 dimensions,
                  Renderable renderable, int layer, ImageReader imageReader,
                  Vector2 windowDimensions) {
        super(topLeftCorner, dimensions, renderable);
        Carrot.carrotLayer = layer;
        Carrot.imageReader = imageReader;
        Carrot.windowDimensions = windowDimensions;
        Carrot.avatar = avatar;
        Carrot.gameObjects = gameObjects;
        Carrot.random = new Random(Objects.hash(PepseGameManager.SEED,
                topLeftCorner.x() * PRIME_NUM + topLeftCorner.y()));


        if (carrotTextRenderable == null) {
            setUpOnScreenTexts();
        }
    }

    // setting up every text output the user need on the screen
    private void setUpOnScreenTexts() {
        Carrot.carrotTextRenderable = new TextRenderable("");
        Carrot.carrotTextRenderable.setColor(Color.BLUE);
        Carrot.carrotText = new GameObject(Vector2.ZERO, Vector2.ONES.mult(CARROT_TEXT_SIZE),
                carrotTextRenderable);
        Carrot.gameObjects.addGameObject(carrotText, Layer.UI);
        Carrot.timerTextRenderable = new TextRenderable("");
        Carrot.timerTextRenderable.setColor(Color.RED);
        Carrot.timerText = new GameObject(Vector2.ZERO, Vector2.ONES.mult(TIMER_TEXT_SIZE),
                timerTextRenderable);
        Carrot.gameObjects.addGameObject(timerText, Layer.UI);
        new Transition<>(timerText,
                (i) -> timerTextRenderable.setString(String.valueOf((i.intValue()))),
                60f,
                0f,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                TIMER_TEXT_X_OFFSET,
                Transition.TransitionType.TRANSITION_LOOP, Carrot::updateHighScore);
    }

    // called every time the timer ends
    private static void updateHighScore() {
        if (highScore <= carrotDestroyed)
            highScore = carrotDestroyed;
        carrotDestroyed = 0;
    }

    /**
     * setting up a function in order for the carrots to know where the ground is
     *
     * @param groundHeightAtFunc - the function to call upon
     */
    public static void setGroundHeightAtFunc(Function<Float, Float> groundHeightAtFunc) {
        Carrot.groundHeightAtFunc = groundHeightAtFunc;
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
        carrotTextRenderable.setString(String.format(CARROTS_DESTROYED_SO_FAR_TEXT,
                carrotDestroyed, highScore));
        carrotText.setCenter(avatar.getTopLeftCorner().add(new Vector2(
                -(windowDimensions.x() / 2 - CARROT_TEXT_X_OFFSET), -windowDimensions.y() / 2)));
        timerText.setCenter(avatar.getTopLeftCorner().add(new Vector2(
                -(windowDimensions.x() / 2 - TIMER_TEXT_X_OFFSET),
                -windowDimensions.y() / 2 + TIMER_TEXT_Y_OFFSET)));

        if (random.nextFloat() >= CARROT_MOVEMENT_CHANGE_FACTOR) {
            setVelocity(getVelocity().rotated(CARROT_TEXT_X_OFFSET));
            renderer().setRenderableAngle(renderer().getRenderableAngle() + CARROT_TEXT_X_OFFSET);
        }
        Float terrainHeight = groundHeightAtFunc.apply(getTopLeftCorner().x());
        if (getTopLeftCorner().y() + CARROT_SIZE > terrainHeight) {
            setVelocity(Vector2.UP.mult(CARROT_SPEED));
            renderer().setRenderableAngle(CARROT_ANGLE_HEADING_UP);
        }
    }

    /**
     * creates a randomly placed flying carrot
     */
    public static void create() {
        Vector2 topLeftCorner = new Vector2(
                avatar.getTopLeftCorner().x() + random.nextInt(X_BOUND) - X_BOUND_OFFSET,
                avatar.getTopLeftCorner().y() - random.nextInt(Y_BOUND));
        Carrot carrot = new Carrot(avatar, gameObjects, topLeftCorner, Vector2.ONES.mult(CARROT_SIZE),
                imageReader.readImage(CARROT_IMG_PATH, true), carrotLayer, imageReader,
                windowDimensions);
        carrot.setVelocity(Vector2.RIGHT.mult(CARROT_SPEED));
        gameObjects.addGameObject(carrot, carrotLayer);
        new ScheduledTask(carrot,
                LIFE_SPAN,
                false, () -> gameObjects.removeGameObject(carrot, carrotLayer));
    }
}
