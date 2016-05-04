package ecs;

import com.jogamp.opengl.GL2;

import java.util.LinkedList;

/**
 * Created by alexa on 5/2/2016.
 */
public class World {

    public LinkedList<Entity> entities;
    GL2 gl;

    public World(GL2 gl) {
        entities = new LinkedList<>();
        this.gl = gl;
    }

    public void update() {
        entities.forEach(Entity::update);
    }

    public void render() {
        entities.forEach(Entity::render);
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }
}
