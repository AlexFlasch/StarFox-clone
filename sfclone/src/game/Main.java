package game;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import ecs.World;
import game.entities.Arwing;
import game.entities.Asteroid;
import game.utils.GLModel;

import javax.swing.*;
import java.awt.*;
import java.util.ConcurrentModificationException;

/**
 * Created by alexa on 4/12/2016.
 */
public class Main extends JFrame implements GLEventListener {

    static GLU glu = new GLU();
    static GLUT glut = new GLUT();
    static GLCapabilities capabilities;
    static FPSAnimator fps;

    Arwing arwing;

    static int windowWidth;
    static int windowHeight;
    static double aspectRatio;

    static int translateAmount = 0;

    float rotAmount = 0f;

    long startingTime;
    long lastAsteroidSpawn;
    long lastScoreIncrement;

    int asteroidsSpawned = 0;

    TextRenderer textRenderer;

    static JFrame panel;
    static JFrame debugPanel;
    static JLabel debugLabel;

    String debugString;

    public static World world;

    public Main() {
        super("SpaceMammal 128");
    }

    public static void main(String[] args) {
        windowWidth = 1600;
        windowHeight = 900;

        aspectRatio = (16.0 / 9.0) * (windowWidth / windowHeight);

        Controls controls = new Controls();

        GLJPanel canvas = new GLJPanel();

        GLCapabilities capabilities = new GLCapabilities(GLProfile.getGL2GL3());
        capabilities.setDoubleBuffered(true);
        capabilities.setHardwareAccelerated(true);

        canvas.addGLEventListener(new Main());

        fps = new FPSAnimator(canvas, 60);

        panel = new JFrame();
        panel.setSize(windowWidth, windowHeight);
        panel.setResizable(false);
        panel.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel.add(canvas);
        panel.setVisible(true);
        panel.requestFocusInWindow();

        debugLabel = new JLabel("Temp");

        debugPanel = new JFrame();
        debugPanel.setSize(300, 300);
        debugPanel.setResizable(false);
        debugPanel.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        debugPanel.add(debugLabel);
        debugPanel.setVisible(true);

//        panel.addKeyListener(controls);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        world = new World(gl);

        arwing = new Arwing(gl);
        panel.addKeyListener(arwing);
        world.addEntity(arwing);

        textRenderer = new TextRenderer(new Font("Verdana", Font.PLAIN, 12));
        textRenderer.begin3DRendering();
        textRenderer.setColor(Color.WHITE);
        textRenderer.setSmoothing(true);

        startingTime = System.currentTimeMillis();
        lastAsteroidSpawn = System.currentTimeMillis();
        lastScoreIncrement = System.currentTimeMillis();

        gl.glMatrixMode(GL2.GL_PROJECTION);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glDepthRange(1.0f, 0.0f);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);
        gl.glEnable(GL2.GL_MULTISAMPLE);
        gl.glShadeModel(GL2.GL_SMOOTH);

        gl.glClearDepth(1.0f);

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glLoadIdentity();
        gl.glOrtho(-100, 100, -100, 100, -100, 100);

        glu.gluPerspective(0.1f, aspectRatio, 1.0f, 270.0f); // fov aspect zNear zFar
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        glu.gluLookAt(0f, 0f, 20f, // eyePos
                0f, 0f, 0f,       // lookAtPos
                0f, 1f, 0f);      // eyeUpPos

        gl.glMatrixMode(GL2.GL_MODELVIEW);

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        float gAmb[] = {0.7f, 0.7f, 0.7f, 1.0f};
        float amb[] = {0.3f, 0.3f, 0.3f, 1.0f};
        float diff[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float spec[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float pos[] = {0.0f, 0.75f, -2.0f, 1.0f};

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, amb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diff, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, spec, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, gAmb, 0);

        fps.start();
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL4 gl4 = glAutoDrawable.getGL().getGL4();
        GL2 gl = glAutoDrawable.getGL().getGL2();

        debugString = "X: " + arwing.pos[0] + " Y: " + arwing.pos[1] + " Z: " + arwing.pos[2];
        debugLabel.setText(debugString);

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        /*gl.glPushMatrix();
        gl.glTranslated(0, 0, -3);
        gl.glScaled(0.5, 0.5, 0.5);
        gl.glRotated(180, 0, 1, 0);

        arwing.draw(gl);
        gl.glPopMatrix();*/

        long currentTime = System.currentTimeMillis();
        if(currentTime - lastScoreIncrement >= 100) {
            world.score++;
            lastScoreIncrement = currentTime;
        }

        gl.glPushMatrix();

//        textRenderer.draw3D("SCORE " + world.score, 0, windowHeight, 0, 2.0f);
        gl.glWindowPos2d(50, windowHeight-100);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "SCORE " + world.score);

        gl.glPopMatrix();

        gl.glPushMatrix();

        // debug stuff so I can tell what's going on :)
//        rotAmount += 0.25f;
//        if(rotAmount >= 360) {
//            rotAmount = 0;
//        }

//        gl.glRotated(rotAmount, 0, 1, 0);

        try {
            world.update();
            world.render();
        }
        catch(ConcurrentModificationException ignored) {

        }


        if(currentTime - lastAsteroidSpawn >= 4000 - (10 * asteroidsSpawned)) {
            lastAsteroidSpawn = currentTime;
            asteroidsSpawned++;
            try{
                world.addEntity(new Asteroid(gl));
            }
            catch(ConcurrentModificationException ignored) {

            }
        }

        gl.glPopMatrix();

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    public static void moveArwingAway() {
        translateAmount--;
        System.out.println("translateAmount: " + translateAmount);
    }

    public static void moveArwingTowards() {
        translateAmount++;
        System.out.println("translateAmount: " + translateAmount);
    }
}
