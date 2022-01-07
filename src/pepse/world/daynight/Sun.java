package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the sun - moves across the sky in an elliptical path.
 */
public class Sun {

    public static final String SUN_TAG = "sun";
    private static final int SUN_SIZE = 200;
    private static final float FINAL_ANGLE_VALUE = 360f;
    private static final int INITIAL_SUN_Y_POSITION = 300;
    private static final int ANGLE_IN_SKY_SHIFT_FACTOR = 90;
    private static final float SUN_WIDTH_ROTATION_FACTOR = 2.5f;
    public static final int INITIAL_POS = 20;
    private static GameObject sun = null;
    private static Vector2 windowDimensions;

    /**
     * This function creates a yellow circle that moves in the sky in an elliptical
     * path (in camera coordinates).
     *
     * @param gameObjects      - The collection of all participating game objects.
     * @param layer            - The number of the layer to which the created sun should be added.
     * @param windowDimensions - The dimensions of the windows.
     * @param cycleLength      - The amount of seconds it takes the game object to complete a full cycle.
     * @return A new game object representing the sun.
     */
    public static GameObject create(
            GameObjectCollection gameObjects,
            int layer,
            Vector2 windowDimensions,
            float cycleLength) {
        Sun.windowDimensions = windowDimensions;
        sun = new GameObject(Vector2.ONES.mult(INITIAL_POS), Vector2.ONES.mult(SUN_SIZE),
                new OvalRenderable(Color.YELLOW));
        sun.setCenter(new Vector2(windowDimensions.x() / 2, INITIAL_SUN_Y_POSITION));
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);
        gameObjects.addGameObject(sun, layer);
        new Transition<>(
                sun,
                Sun::setSunPosition,
                0f,
                FINAL_ANGLE_VALUE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null);
        return sun;
    }

    // given an angle, changing the sun position in and elliptic shape.
    private static void setSunPosition(float angleInSky) {
        angleInSky -= ANGLE_IN_SKY_SHIFT_FACTOR;
        float a = windowDimensions.x() / 2f;
        float b = windowDimensions.y() / SUN_WIDTH_ROTATION_FACTOR;
        sun.setCenter(new Vector2(
                (float) (a * Math.cos(Math.toRadians(angleInSky))) + windowDimensions.x() / 2,
                (float) (b * Math.sin(Math.toRadians(angleInSky)) + windowDimensions.y() / 2))
        );
    }
}