package ecs.components;

import com.jogamp.opengl.GL2;
import ecs.Component;
import ecs.Entity;

/**
 * Created by alexa on 5/1/2016.
 */
public class LightEmitter extends Component {

    Entity parent;
    float[] pos;

    float[][] lightSettings;
    float[] lightModel = new float[]{0.7f, 0.7f, 0.7f, 1.0f};

    GL2 gl;

    public LightEmitter(GL2 gl, float[][] settings, float[] color) {
        super();
        this.gl = gl;
        pos = new float[3];
        lightSettings = settings;
    }

    @Override
    public void update() {
        pos = parent.pos;
    }

    @Override
    public void render() {
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightSettings[0], 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightSettings[1], 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSettings[2], 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lightModel, 0);
    }
}
