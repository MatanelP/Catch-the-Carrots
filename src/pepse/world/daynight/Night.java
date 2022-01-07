package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Darkens the entire window.
 */
public class Night {

    public static final String NIGHT_TAG = "night";
    private static final Float MIDNIGHT_OPACITY = 0.5f;

    /**
     * This function creates a black rectangular game object that covers the entire game window and changes
     * its opaqueness in a cyclic manner, in order to resemble day-to-night transitions.
     *
     * @param gameObjects      - The collection of all participating game objects.
     * @param layer            - The number of the layer to which the created game object should be added.
     * @param windowDimensions - The dimensions of the windows.
     * @param cycleLength      - The amount of seconds it takes the created object to complete a full cycle.
     * @return A new game object representing day-to-night transitions.
     */
    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        GameObject night = new GameObject(Vector2.ZERO, windowDimensions,
                new RectangleRenderable(Color.BLACK));
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(night, layer);
        night.setTag(NIGHT_TAG);

        new Transition<>(
                night, // the game object being changed
                night.renderer()::setOpaqueness, // the method to call
                0f, // initial transition value
                MIDNIGHT_OPACITY, // final transition value
                Transition.CUBIC_INTERPOLATOR_FLOAT, // use a cubic interpolator
                cycleLength / 2, // transition fully over half a day
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null); // nothing further to execute upon reaching final value

        return night;
    }
}
