import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import processing.core.PApplet;

import java.util.ArrayList;

/**
 * Created by rszczers on 18.03.17.
 */
public class Fungus {
    Vec2 location;
    ArrayList<Hyphae> root;
    PApplet context;
    Box2DProcessing world;

    public Fungus(Box2DProcessing world, PApplet context, Vec2 location) {
        this.location = location;
        this.context = context;
        this.world = world;

    }

    public void grow() {

    }

    public void display() {
        context.pushMatrix();
        context.translate(location.x, location.y);
        context.fill(255, 0, 0);
        context.ellipse(0, 0, 1, 1);
        context.noStroke();
        context.popMatrix();
    }
}
