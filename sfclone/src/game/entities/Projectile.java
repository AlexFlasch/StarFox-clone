package game.entities;

import com.jogamp.opengl.GL2;
import ecs.Component;
import ecs.Entity;
import game.Main;
import game.components.CollisionDetection;

/**
 * Created by alexa on 5/4/2016.
 */
public class Projectile extends Entity {

    float dZ;
    int listId;

    public boolean isCollideable = true;

    float[] pos;
    boolean friendly;

    CollisionDetection cd;

    double leftPoint, rightPoint, upPoint, downPoint, nearPoint, farPoint;

    GL2 gl;

    public Projectile(GL2 gl, float[] pos, boolean friendly) {
        super(gl, pos);

        this.gl = gl;
        this.pos = new float[] { pos[0], pos[1], pos[2] };
        this.friendly = friendly;

        cd = new CollisionDetection();
        this.addComponent(cd);
        Main.world.projectiles.add(this);

        dZ = 10.0f;
        listId = 0;

        leftPoint = pos[0] - 1;
        rightPoint = pos[0] + 1;
        upPoint = pos[1] + 1;
        downPoint = pos[1] - 1;
        nearPoint = pos[2] + 2;
        farPoint = pos[2] - 2;

        createDisplayList();
    }

    @Override
    public void update() {
        pos[2] -= dZ;

        leftPoint = pos[0] - 1;
        rightPoint = pos[0] + 1;
        upPoint = pos[1] + 1;
        downPoint = pos[1] - 1;
        nearPoint = pos[2] + 2;
        farPoint = pos[2] - 2;

        if(pos[2] >= 50) {
            Main.world.removeEntity(this);
        }

        this.components.forEach(Component::update);
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

        gl.glColor3f(1.0f, 1.0f, 1.0f);
    }

    @Override
    public double[] getBoundingBox() {
        return new double[]
                {
                        leftPoint,
                        rightPoint,
                        upPoint,
                        downPoint,
                        nearPoint,
                        farPoint
                };
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
