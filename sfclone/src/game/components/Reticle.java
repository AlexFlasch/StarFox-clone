package game.components;

import com.jogamp.opengl.GL2;
import ecs.Component;

/**
 * Created by alexa on 5/3/2016.
 */
public class Reticle extends Component{

    GL2 gl;

    float[] pos;
    float[] parentPos;

    int listId;

    public Reticle(GL2 gl) {
        this.gl = gl;

        listId = 0;
        createDisplayList();
    }

    @Override
    public void update() {

    }

    @Override
    public void render() {

        gl.glPushMatrix();

        gl.glTranslated(0, 0, 20);

        gl.glCallList(listId);

        gl.glPopMatrix();

        gl.glColor3f(1.0f, 1.0f, 1.0f);
    }

    private void createDisplayList() {
        listId = gl.glGenLists(1);
        gl.glNewList(listId, GL2.GL_COMPILE);

        gl.glBegin(GL2.GL_LINES);

        gl.glColor3f(0.317f, 0.96f, 0.564f);
        gl.glLineWidth(1.0f);

        // first square
        // bottom
        gl.glVertex3d(-0.4, -0.5, -0.5);
        gl.glVertex3d(0.4, -0.5, -0.5);

        // right
        gl.glVertex3d(0.5, -0.4, -0.5);
        gl.glVertex3d(0.5, 0.4, -0.5);

        // top
        gl.glVertex3d(0.4, 0.5, -0.5);
        gl.glVertex3d(-0.4, 0.5, -0.5);

        // left
        gl.glVertex3d(-0.5, 0.4, -0.5);
        gl.glVertex3d(-0.5, -0.4, -0.5);
        // end first square

        // connecting lines
        // bottom left
        gl.glVertex3d(-0.5, -0.5, -0.4);
        gl.glVertex3d(-0.35, -0.35, 0.4);

        // bottom right
        gl.glVertex3d(0.5, -0.5, -0.4);
        gl.glVertex3d(0.35, -0.35, 0.4);

        // top right
        gl.glVertex3d(0.5, 0.5, -0.4);
        gl.glVertex3d(0.35, 0.35, 0.4);

        // top left
        gl.glVertex3d(-0.5, 0.5, -0.4);
        gl.glVertex3d(-0.35, 0.35, 0.4);
        // end connecting lines

        // second square
        // bottom
        gl.glVertex3d(-0.3, -0.35, 0.5);
        gl.glVertex3d(0.3, -0.35, 0.5);

        // right
        gl.glVertex3d(0.35, -0.3, 0.5);
        gl.glVertex3d(0.35, 0.3, 0.5);

        // top
        gl.glVertex3d(-0.3, 0.35, 0.5);
        gl.glVertex3d(0.3, 0.35, 0.5);

        // left
        gl.glVertex3d(-0.35, -0.3, 0.5);
        gl.glVertex3d(-0.35, 0.3, 0.5);
        // end second square

        // detail lines
        // bottom left
        gl.glVertex3d(-0.25, -0.25, 0.5);
        gl.glVertex3d(-0.15, -0.15, 0.5);

        // bottom right
        gl.glVertex3d(0.25, -0.25, 0.5);
        gl.glVertex3d(0.15, -0.15, 0.5);

        // top right
        gl.glVertex3d(0.25, 0.25, 0.5);
        gl.glVertex3d(0.15, 0.15, 0.5);

        // top left
        gl.glVertex3d(-0.25, 0.25, 0.5);
        gl.glVertex3d(-0.15, 0.15, 0.5);
        // end detail lines

        gl.glEnd();

        gl.glEndList();
    }
}
