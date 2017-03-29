import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import processing.core.PApplet;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by rszczers on 18.03.17.
 */
public class Fungus {
    private ArrayList<Tip> leafs;
    private Vec2 location;
    private ArrayList<Hyphae> roots;
    private PApplet context;
    private Box2DProcessing world;

    public Fungus(Box2DProcessing world, PApplet context, Vec2 location) {
        this.location = location;
        this.context = context;
        this.world = world;
        this.roots = new ArrayList<>();
        this.leafs = new ArrayList<>();
    }

    public void addRoot(Vec2 location) {
        Vec2[] last = new Vec2[]{
                world.coordPixelsToWorld(location.add(new Vec2(5.0f, 5.0f))),
                world.coordPixelsToWorld(location.add(new Vec2(-5.0f, 5.0f))),
        };
        Vec2[] next = new Vec2[]{
                world.coordPixelsToWorld(location.add(new Vec2(-5.0f, 5.0f))),
                world.coordPixelsToWorld(location.add(new Vec2(5.0f, 5.0f))),
        };



        roots.add(new Hyphae(context, world, this, null, next, 0.0, 0.0f));
    }

    public void grow() {
        for (Hyphae r :
                roots) {
            r.grow();
        }
    }

    public void applyForce(Vec2 force) {
//        for (Hyphae h :
//                roots) {
//            h.applyForce(force);
//        }
        for (Tip t :
                leafs) {
            t.applyForce(force);
        }
    }

    public void display() {
//        context.pushMatrix();
//        context.translate(location.x, location.y);
//        context.fill(255, 0, 0);
//        context.ellipse(0, 0, 10, 10);
//        context.noStroke();
//        context.popMatrix();

        for (Hyphae h:
             roots) {
            h.display();
        }
    }

    public ArrayList<Tip> getTips() {
        return leafs;
    }

    public void addTip(Tip a) {
        leafs.add(a);
    }
}
