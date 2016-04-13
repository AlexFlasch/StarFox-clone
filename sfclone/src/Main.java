import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import jogamp.opengl.glu.GLUquadricImpl;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

/**
 * Created by alexa on 4/12/2016.
 */
public class Main extends JFrame implements GLEventListener {

    static GLU glu = new GLU();
    static GLCapabilities capabilities;
    static FPSAnimator fps;

    static int windowWidth;
    static int windowHeight;
    static double aspectRatio;

    GLModel arwing;

    public Main() {
        super("SpaceMammal 128");
    }

    public static void main(String[] args) {
        windowWidth = 1600;
        windowHeight = 900;

        aspectRatio = (16.0 / 9.0) * (windowWidth / windowHeight);

        Controls controls = new Controls();

        capabilities = new GLCapabilities(GLProfile.getGL4ES3());
        capabilities.setDoubleBuffered(true);
        capabilities.setHardwareAccelerated(true);

        GLJPanel canvas = new GLJPanel();
        canvas.addGLEventListener(new Main());

        fps = new FPSAnimator(canvas, 60);

        JFrame frame = new JFrame("SpaceMammal 128");

        frame.addKeyListener(controls);
        frame.addMouseListener(controls);

        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(canvas);
        frame.setVisible(true);
        frame.requestFocus();
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL4 gl4 = glAutoDrawable.getGL().getGL4();
        GL2 gl = glAutoDrawable.getGL().getGL2();

        arwing = ObjLoader.LoadModel("sfclone/res/Arwing/arwing.obj", "sfclone/res/Arwing/arwing.mtl", gl);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_CULL_FACE);

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-100, 100, -100, 100, -100, 100);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        gl.glClear(GL4.GL_CLEAR_BUFFER | GL4.GL_DEPTH_BUFFER_BIT);

        float gAmb[] = {0.2f, 0.2f, 0.2f, 1.0f};
        float amb[] = {0.7f, 0.7f, 0.7f, 1.0f};
        float diff[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float spec[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float pos[] = {0.0f, 5.0f, 0.0f, 1.0f};

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, amb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diff, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, spec, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, gAmb, 0);

        gl.glShadeModel(GL2.GL_SMOOTH);

        fps.start();
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL4 gl4 = glAutoDrawable.getGL().getGL4();
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        gl.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-100, 100, -100, 100, -100, 100); // everything is within a 200x200x200 cube
        glu.gluPerspective(30, aspectRatio, 1, 50);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, -5, 0, 0, 0, 0, 0, 0); // eyePos, lookAtPos, eyeUp

        glu.gluSphere(glu.gluNewQuadric(), 10, 25, 25);

        gl.glPushMatrix();
        arwing.opengldraw(gl);

        // gl.glRotated(1.0, 0, 1, 0);
        gl.glTranslated(0, 0, 15);
        gl.glScaled(0.25, 0.25, 0.25);
        gl.glPopMatrix();

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }
}
