package game.entities;

import com.jogamp.opengl.GL2;
import ecs.Entity;
import ecs.components.LightEmitter;
import game.Main;

/**
 * Created by alexa on 5/4/2016.
 */
public class Projectile extends Entity {

    float dZ;
    int listId;

    float[] pos;
    boolean friendly;

    GL2 gl;

    public Projectile(GL2 gl, float[] pos, boolean friendly) {
        super(gl, pos);

        this.gl = gl;
        this.pos = new float[] { pos[0], pos[1], pos[2] };
        this.friendly = friendly;

        dZ = 10.0f;
        listId = 0;

        createDisplayList();
    }

    @Override
    public void update() {
        pos[2] -= dZ;

        if(pos[2] >= 50) {
            Main.world.removeEntity(this);
        }
    }

    @Override
    public void render() {
        gl.glPushMatrix();

        gl.glTranslatef(pos[0], pos[1], pos[2]);

        gl.glColor3d(0, 1, 0);
        gl.glLineWidth(3.0f);
        gl.glBegin(GL2.GL_LINES);

        // left laser
        gl.glVertex3d(-0.25, 0, 0);
        gl.glVertex3d(-0.25, 0, 5);

        // right laser
        gl.glVertex3d(0.25, 0, 0);
        gl.glVertex3d(0.25, 0, 5);

        gl.glTranslatef(-pos[0], -pos[1], -pos[2]);

        gl.glEnd();

        gl.glPopMatrix();
    }

    private void createDisplayList() {
        listId = gl.glGenLists(1);

        gl.glNewList(listId, GL2.GL_COMPILE);

        gl.glColor3d(0, 0, 1);
        gl.glLineWidth(5.0f);
        gl.glBegin(GL2.GL_LINES);

        // left laser
        gl.glVertex3d(-1, 0, 0);
        gl.glVertex3d(-1, 0, 2);

        // right laser
        gl.glVertex3d(1, 0, 0);
        gl.glVertex3d(1, 0, 2);

        gl.glEnd();

        gl.glEndList();
    }
}
