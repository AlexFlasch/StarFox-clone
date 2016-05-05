package game.entities;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import ecs.Entity;
import game.Main;

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

    public Asteroid(GL2 gl) {
        super(gl);

        this.gl = gl;
        this.glu = new GLU();
        this.glut = new GLUT();

        double x = ThreadLocalRandom.current().nextDouble(10);
        double y = ThreadLocalRandom.current().nextDouble(10);

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
    }

    public void render() {
        gl.glPushMatrix();

        gl.glColor3f(1.0f, 1.0f, 1.0f);

        gl.glTranslated(pos[0], pos[1], pos[2]);
        gl.glScaled(0.25, 0.25, 0.25);
        gl.glRotated(90, 1, 0, 0);

        texture.enable(gl);
        texture.bind(gl);
        asteroid = glu.gluNewQuadric();
        glu.gluQuadricTexture(asteroid, true);
        glu.gluSphere(asteroid, radius, 25, 25);

        gl.glTranslated(-pos[0], pos[1], pos[2]);

        gl.glPopMatrix();
    }
}
