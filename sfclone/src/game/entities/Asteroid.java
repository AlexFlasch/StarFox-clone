package game.entities;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import ecs.Component;
import ecs.Entity;
import game.Main;
import game.components.CollisionDetection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by alexa on 5/4/2016.
 */
public class Asteroid extends Entity {

    GL2 gl;
    GLU glu;
    GLUT glut;

    GLUquadric asteroid;
    Texture texture;
    double radius;
    double[] pos;
    double dZ;
    double speed;
    double scale;

    double leftPoint, rightPoint, topPoint, bottomPoint, nearPoint, farPoint;

    CollisionDetection cd;

    public Asteroid(GL2 gl) {
        super(gl);

        this.gl = gl;
        this.glu = new GLU();
        this.glut = new GLUT();

        cd = new CollisionDetection();
        this.addComponent(cd);
        Main.world.asteroids.add(this);

        double x = ThreadLocalRandom.current().nextDouble(10);
        double y = ThreadLocalRandom.current().nextDouble(10);

        scale = 0.25;

        // make sure x and y are relatively within bounds
        x -= 5;
        y -= 5;

        this.pos = new double[] { x, y, -250 };

        this.speed = 1.0;
        this.dZ = 1.0f * speed;

        try {
            String projPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            String texturePath = projPath + "asteroid.bmp";
            InputStream stream = getClass().getResourceAsStream("/asteroid.bmp");
            TextureData data = TextureIO.newTextureData(gl.getGLProfile(), stream, false, "bmp");
            texture = TextureIO.newTexture(data);
        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();
        }

        radius = ThreadLocalRandom.current().nextDouble(9);
        radius += 1; // make the min size 1

        leftPoint = pos[0] - (radius * scale);
        rightPoint = pos[0] + (radius * scale);
        topPoint = pos[1] + (radius * scale);
        bottomPoint = pos[1] - (radius * scale);
        nearPoint = pos[2] + (radius * scale);
        farPoint = pos[2] - (radius * scale);

        System.out.println("Asteroid created: (" + pos[0] + " " + pos[1] + " " + pos[2] + ")");
    }

    public void update() {
        pos[2] += dZ;

        if(pos[2] >= 25) {
            try{
                Main.world.removeEntity(this);
            }
            catch(ConcurrentModificationException ignored) {

            }
        }

        leftPoint = pos[0] - (radius * scale);
        rightPoint = pos[0] + (radius * scale);
        topPoint = pos[1] + (radius * scale);
        bottomPoint = pos[1] - (radius * scale);
        nearPoint = pos[2] + (radius * scale);
        farPoint = pos[2] - (radius * scale);

        // check for collisions with projectiles or the player
        for(Projectile p : Main.world.projectiles) {
            if(p.cd.collides(cd)) {
                Main.world.asteroids.remove(this);
                Main.world.removeEntity(this);
                Main.world.projectiles.remove(p);
                Main.world.removeEntity(p);
                Main.world.score += 100;
            }
            if(Main.world.arwing.cd.collides(cd)) {
                Main.world.removeEntity(Main.world.arwing);
            }
        }

        for(Component c : components) {
            c.update();
        }
    }

    public void render() {
        gl.glPushMatrix();

        gl.glColor3f(1.0f, 1.0f, 1.0f);

        gl.glTranslated(pos[0], pos[1], pos[2]);
        gl.glScaled(scale, scale, scale);
        gl.glRotated(90, 1, 0, 0);

        texture.enable(gl);
        texture.bind(gl);
        asteroid = glu.gluNewQuadric();
        glu.gluQuadricTexture(asteroid, true);
        glu.gluSphere(asteroid, radius, 25, 25);

        gl.glTranslated(-pos[0], pos[1], pos[2]);

        gl.glPopMatrix();
    }

    @Override
    public double[] getBoundingBox() {
        return new double[]
                {
                        leftPoint,
                        rightPoint,
                        topPoint,
                        bottomPoint,
                        nearPoint,
                        farPoint
                };
    }
}
