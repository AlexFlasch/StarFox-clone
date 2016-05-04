package ecs;

/**
 * Created by alexa on 4/30/2016.
 */
public abstract class Component {

    public Entity parent;

    public Component() {

    }

    public abstract void update();
    public abstract void render();
}
