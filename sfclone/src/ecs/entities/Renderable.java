package ecs.entities;

import com.jogamp.opengl.GL2;
import ecs.Component;
import ecs.Entity;

import java.util.LinkedList;

/**
 * Created by alexa on 4/30/2016.
 */
public abstract class Renderable extends Entity {

    private LinkedList<Component> components;

    public Renderable(GL2 gl) {
        super(gl);
        components = new LinkedList<>();
    }

    public Renderable(GL2 gl, float[] pos) {
        super(gl, pos);
        components = new LinkedList<>();
    }

    public void update() {
        components.forEach(Component::update);
    }

    public void render() {
        components.forEach(Component::render);
    }

}
