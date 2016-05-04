package game.entities;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import ecs.entities.Renderable;
import game.utils.GLModel;
import game.utils.ObjLoader;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by alexa on 5/2/2016.
 */
public class Arwing extends Renderable implements KeyListener {

    GL2 gl;
    GLUT glut;
    GLModel model;

    int bankAmount;
    float shipBankXOffset;
    float shipBankZOffset;

    public boolean boundariesHit;

    public float[] pos;

    float speed;

    float dX;
    float dY;

    int lastDirection; // left = 1, right = 2, up = 3, down = 4 (gross, I know. Time constraints though. :( )
    int currentDirection; // same applies as above

    public Arwing(GL2 gl) {
        super(gl);
        this.gl = gl;


        model = ObjLoader.LoadModel("sfclone/res/Arwing/arwing.obj", "sfclone/res/Arwing/arwing.mtl", gl);
        System.out.println("Model dimensions (x,y,z): " + model.getXWidth() + " " + model.getYHeight() + " " + model.getZDepth());

        bankAmount = 15;
        shipBankXOffset = 1.5819915f;
        shipBankZOffset = 2.225817f;

        boundariesHit = false;

        speed = 0.125f;

        pos = new float[] {0.0f, 0.0f, -15.0f};

        dX = 0;
        dY = 0;
    }

    @Override
    public void update() {
        pos[0] += dX;
        pos[1] += dY;

        // update all components
        super.update();
    }

    @Override
    public void render() {
        // render arwing position for debugging

        gl.glPushMatrix();

        // make the ship's rear end face us
        gl.glTranslatef(0.0f, 0.0f, pos[1]);
        gl.glTranslatef(0.0f, 0.0f, 0.0f);
        gl.glTranslatef(pos[0], 0.0f, 0.0f);
        gl.glRotated(180, 0, 1, 0);

        // undo banking from changing directions
        /*if(lastDirection == 1) { // left
            gl.glRotated(bankAmount, 0, 0, 1);
            lastDirection = 0;
        }
        if(lastDirection == 2) { // right
            gl.glRotated(bankAmount, 0, 0, 1);
            lastDirection = 0;
        }

        if(lastDirection == 3) { // up
            gl.glRotated(-bankAmount, 1, 0, 0);
            lastDirection = 0;
        }
        if(lastDirection == 4) { // down
            gl.glRotated(bankAmount, 1, 0, 0);
            lastDirection = 0;
        }

        if(currentDirection == 1 && !boundariesHit) { // moving left -> rotate ship to make it look like its banking
            gl.glRotated(bankAmount, 0, 0, 1);
            currentDirection = 0;
        }
        if(currentDirection == 2 && !boundariesHit) { // moving right, do the same
            gl.glRotated(-bankAmount, 0, 0, 1);
            currentDirection = 0;
        }

        if(currentDirection == 3 && !boundariesHit) { // moving up, same idea
            gl.glRotated(bankAmount, 1, 0, 0);
            currentDirection = 0;
        }
        if(currentDirection == 4 && !boundariesHit) { // moving down, you get it by now
            gl.glRotated(-bankAmount, 1, 0, 0);
            currentDirection = 0;
        }*/

        // render all components
        // super.render();

        // move to 0,0,0 to apply correct transformations
        gl.glTranslatef(-pos[0], -pos[1], -pos[2]);

        model.draw(gl);

        gl.glPopMatrix();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                moveLeft();
                currentDirection = 1;
                break;

            case KeyEvent.VK_RIGHT:
                moveRight();
                currentDirection = 2;
                break;

            case KeyEvent.VK_UP:
                moveUp();
                currentDirection = 3;
                break;

            case KeyEvent.VK_DOWN:
                moveDown();
                currentDirection = 4;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                cancelMovement(1);
                lastDirection = 1;
                currentDirection = 0;
                break;

            case KeyEvent.VK_RIGHT:
                cancelMovement(2);
                lastDirection = 2;
                currentDirection = 0;
                break;

            case KeyEvent.VK_UP:
                cancelMovement(3);
                lastDirection = 3;
                currentDirection = 0;
                break;

            case KeyEvent.VK_DOWN:
                cancelMovement(4);
                lastDirection = 4;
                currentDirection = 0;
                break;
        }
    }

    private void moveRight() {
        if(pos[0] >= 3) {
            dX = 0;
            boundariesHit = true;
        }
        else {
            dX = 1 * speed;
            boundariesHit = false;
        }
    }

    private void moveLeft() {
        if(pos[0] <= -3) {
            dX = 0;
            boundariesHit = true;
        }
        else {
            dX = -1 * speed;
            boundariesHit = false;
        }
    }

    private void moveUp() {
        if(pos[1] >= 3) {
            dY = 0;
            boundariesHit = true;
        }
        else {
            dY = 1 * speed;
            boundariesHit = false;
        }
    }

    private void moveDown() {
        if(pos[1] <= -3) {
            dY = 0;
            boundariesHit = true;
        }
        else {
            dY = -1 * speed;
            boundariesHit = false;
        }
    }

    private void cancelMovement(int direction) {
        dX = 0;
        dY = 0;
    }
}
