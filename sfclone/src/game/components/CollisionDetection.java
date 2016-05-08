package game.components;

import ecs.Component;
import ecs.Entity;

/**
 * Created by alexa on 5/5/2016.
 */
public class CollisionDetection extends Component {

    public Entity parent;

    double[] boundingBox;

    public CollisionDetection() {
        super();
        boundingBox = new double[6];



        this.parent = super.parent;
    }

    @Override
    public void update() {
        // update the position of the entity's bounding box

        boundingBox = super.parent.getBoundingBox();
    }

    @Override
    public void render() {

    }

    public boolean collides(CollisionDetection cd) {
        // implement simple AABB-style collision
        double[] a = this.boundingBox;
        double[] b = cd.boundingBox;

        // 0 = left
        // 1 = right
        // 2 = up
        // 3 = down
        // 4 = near
        // 5 = far

        boolean hit = (a[0] <= b[1] && a[1] >= b[1]) &&
                (a[3] <= b[2] && a[2] >= b[3]) &&
                (a[5] <= b[4] && a[4] >= b[5]);

        if(hit) {
            System.out.println("Collision detected.");
        }

        return hit;
    }
}
