package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;
import pepse.world.Terrain;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class Leaf extends Block {
    private static final Color BASE_LEAF_COLOR = new Color(50, 200, 30);
    private static final float FADEOUT_TIME = 10;
    private static final int FALL_VELOCITY = 60;
    private static final float ANGLE_CHANGE_VALUE = 20f;
    private static final float ANGLE_CHANGE_TIME = 2f;
    private static final float SIZE_CHANGE_TIME = 0.5f;
    private static final int SIZE_CHANGE_FACTOR = 5;
    private static final float SWING_FALL_TIME = 0.5f;
    public static final String LEAF_TAG = "leaf";
    private static final int PRIME_NUM = 31;
    private static final int MAX_TIME_BEFORE_FALLING = 60;
    private static final int MAX_TIME_BEFORE_RESPAWNING = 10;
    private final Vector2 leafPotion;
    Transition<Float> angleTransition;
    Transition<Vector2> sizeTransition;
    Transition<Vector2> movementTransition;
    private boolean isCollidedWithGround;
    private Random mainRandom;

    /**
     * constructor.
     *
     * @param leafPotion  - The leaf potion on the screen.
     * @param gameObjects - The collection of all participating game objects.
     * @param leafLayer   - The number of the layer to which the created leaf objects should be added.
     */
    public Leaf(Vector2 leafPotion, GameObjectCollection gameObjects, int leafLayer) {
        super(leafPotion, new RectangleRenderable(ColorSupplier.approximateColor(BASE_LEAF_COLOR)));
        this.leafPotion = leafPotion;
        this.isCollidedWithGround = false;
        this.setTag(LEAF_TAG);
        gameObjects.addGameObject(this, leafLayer);
    }

    /**
     * Stating the animation transitions to simulate wind.
     */
    public void animate() {
        angleTransition = new Transition<>(
                this,
                this.renderer()::setRenderableAngle,
                -ANGLE_CHANGE_VALUE,
                ANGLE_CHANGE_VALUE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                ANGLE_CHANGE_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);

        sizeTransition = new Transition<>(
                this,
                this::setDimensions,
                Vector2.ONES.mult(Block.SIZE),
                Vector2.ONES.mult(Block.SIZE - SIZE_CHANGE_FACTOR),
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                SIZE_CHANGE_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
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
        if (this.isCollidedWithGround) {
            this.transform().setVelocity(Vector2.ZERO);
            this.isCollidedWithGround = false;
        }
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
        if (this.getVelocity().y() <= 0 || !other.getTag().equals(Terrain.GROUND_TAG))
            return;
        this.isCollidedWithGround = true;
        this.transform().setVelocity(Vector2.ZERO);
        this.removeComponent(movementTransition);
        this.physics().setMass(0);
    }

    /**
     * Starting the falling of a leaf with fadeout and downwards velocity.
     */
    public void fall() {
        this.renderer().fadeOut(FADEOUT_TIME, this::setRespawnTransition);
        movementTransition = new Transition<>(
                this,
                this.transform()::setVelocity,
                new Vector2(-FALL_VELOCITY, FALL_VELOCITY),
                new Vector2(FALL_VELOCITY, FALL_VELOCITY),
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                SWING_FALL_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
    }

    // setting up the respawning sequence.
    private void setRespawnTransition() {
        new Transition<>(
                this,
                this.renderer()::setOpaqueness,
                0f,
                0f,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                mainRandom.nextInt(MAX_TIME_BEFORE_RESPAWNING),
                Transition.TransitionType.TRANSITION_ONCE,
                this::respawn);
    }

    // respawning the leaf using full opaqueness and resetting the falling task.
    private void respawn() {
        this.renderer().setOpaqueness(1f);
        this.setTopLeftCorner(this.leafPotion);
        this.transform().setVelocity(Vector2.ZERO);
        new ScheduledTask(
                this,
                mainRandom.nextInt(MAX_TIME_BEFORE_FALLING),
                false,
                this::fall);
    }

    /**
     * set up the seed of the whole game, in order to maintain reconstructed randomly.
     *
     * @param mainSeed - The main Seed of the whole game.
     */
    public void setRandomizer(int mainSeed) {
        this.mainRandom = new Random(Objects.hash(getCenter().x() * PRIME_NUM + getCenter().y(),
                mainSeed));
    }
}
