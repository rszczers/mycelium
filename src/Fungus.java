import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import processing.core.PApplet;

import java.util.ArrayList;

/**
 * Created by rszczers on 18.03.17.
 */
public class Fungus {
    Vec2 location;
    ArrayList<Hyphae> roots;
    PApplet context;
    Box2DProcessing world;

    public Fungus(Box2DProcessing world, PApplet context, Vec2 location) {
        this.location = location;
        this.context = context;
        this.world = world;
        this.roots = new ArrayList<>();
    }

    public void addRoot(Vec2 location) {
        roots.add(new Hyphae(context, world, location, 0.0));
    }

    public void grow() {
        for (Hyphae r :
                roots) {
            r.grow();
        }
    }

    public void applyForce(Vec2 force) {
        for (Hyphae h :
                roots) {
            h.applyForce(force);
        }
    }

    public void display() {
        context.pushMatrix();
        context.translate(location.x, location.y);
        context.fill(255, 0, 0);
        context.ellipse(0, 0, 10, 10);
        context.noStroke();
        context.popMatrix();

        for (Hyphae h:
             roots) {
            h.display();
        }
    }
}
