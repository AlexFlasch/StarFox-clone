package ecs;

import com.jogamp.opengl.GL2;
import game.entities.*;

import java.util.LinkedList;

/**
 * Created by alexa on 5/2/2016.
 */
@SuppressWarnings("Convert2streamapi")
public class World {

    public LinkedList<Entity> entities;
    public LinkedList<Asteroid> asteroids;
    public LinkedList<Projectile> projectiles;
    public Arwing arwing;
    public long score;
    GL2 gl;

    public World(GL2 gl) {
        entities = new LinkedList<>();
        asteroids = new LinkedList<>();
        projectiles = new LinkedList<>();
        this.gl = gl;
    }

    public void update() {
        for(Entity e : entities) {
            e.update();
        }
    }

    public void render() {
        for(Entity e : entities) {
            e.render();
        }
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }
}
