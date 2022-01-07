package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.ScheduledTask;
import danogl.gui.ImageReader;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class FireBall extends GameObject {
    public static final int SIZE = 50;
    public static final double TIME_BETWEEN_CLIPS = 0.1f;
    public static final int FIRE_BALL_SPEED = 500;
    public static final int LIFE_SPAN = 7;
    public static final int Y_OFFSET = 25;
    public static final int X_LEFT_OFFSET = -130;
    public static final int X_RIGHT_OFFSET = 50;
    public static final float WIDE_FACTOR = 3f;
    private final GameObjectCollection gameObjects;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     * @param gameObjects   Manage all object in the game.
     */
    public FireBall(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable,
                    GameObjectCollection gameObjects) {
        super(topLeftCorner, dimensions, renderable);

        this.gameObjects = gameObjects;
    }

    /**
     * crating a new fireBall the avatar shoots.
     *
     * @param gameObjects    - The collection of all participating game objects.
     * @param imageReader    - Used for reading images from disk or from within a jar.
     * @param topLeftCorner  - The location of the top-left corner of the created fireBall.
     * @param fireToTheRight - Indicator if the avatar is facing right.
     */
    public static void create(GameObjectCollection gameObjects, ImageReader imageReader,
                              Vector2 topLeftCorner, boolean fireToTheRight) {
        AnimationRenderable fireBallAnimation = new AnimationRenderable(new String[]{
                "pepse/assets/fireball-left1.png",
                "pepse/assets/fireball-left2.png",
                "pepse/assets/fireball-left3.png",
                "pepse/assets/fireball-left4.png"},
                imageReader, true, TIME_BETWEEN_CLIPS);
        Vector2 fireBallLocation = topLeftCorner.add(new Vector2(
                fireToTheRight ? X_RIGHT_OFFSET : X_LEFT_OFFSET, Y_OFFSET));
        FireBall fireBall = new FireBall(fireBallLocation, new Vector2(SIZE * WIDE_FACTOR, SIZE),
                fireBallAnimation, gameObjects);
        if (fireToTheRight) {
            fireBall.setVelocity(Vector2.RIGHT.mult(FIRE_BALL_SPEED));
            fireBall.renderer().setIsFlippedHorizontally(true);
        } else
            fireBall.setVelocity(Vector2.LEFT.mult(FIRE_BALL_SPEED));

        gameObjects.addGameObject(fireBall, Layer.DEFAULT + 2);
        gameObjects.layers().shouldLayersCollide(Layer.DEFAULT + 1, Layer.DEFAULT + 2, true);
        fireBall.renderer().fadeOut(LIFE_SPAN);
        new ScheduledTask(fireBall,
                LIFE_SPAN,
                false, () -> gameObjects.removeGameObject(fireBall, Layer.DEFAULT + 2));

    }

    /**
     * Called on the first frame of a collision.
     *
     * @param other     The GameObject with which a collision occurred.
     * @param collision Information regarding this collision.
     *                  A reasonable elastic behavior can be achieved with:
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        gameObjects.removeGameObject(other, Layer.DEFAULT + 1);
        gameObjects.removeGameObject(this, Layer.DEFAULT + 2);
        Carrot.carrotDestroyed++;
    }
}
