import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import processing.core.PApplet;
import processing.core.PGraphics;

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

    public void addRoot(Vec2 location, int width) {
        float off = ((float)width/2.0f);
        Vec2[] last = new Vec2[]{
                world.coordPixelsToWorld(location.add(new Vec2(off, off))),
                world.coordPixelsToWorld(location.add(new Vec2(-off, off))),
        };
        Vec2[] next = new Vec2[]{
                world.coordPixelsToWorld(location.add(new Vec2(-off, off))),
                world.coordPixelsToWorld(location.add(new Vec2(off, off))),
        };

        Hyphae firstHyphae = new Hyphae(context, world, this, null, next, 0.0, 1.0f);
        firstHyphae.getShapes().get(0).init();
        roots.add(firstHyphae);

    }

    public void grow(float boxWidth, float boxHeight) {
        for (Hyphae r :
                roots) {
            r.grow(boxWidth, boxHeight);
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

    public void display(boolean showCollisionShapes, PGraphics layer) {
//        context.pushMatrix();
//        context.translate(location.x, location.y);
//        context.fill(255, 0, 0);
//        context.ellipse(0, 0, 10, 10);
//        context.noStroke();
//        context.popMatrix();
        for (int i = 0; i < roots.size(); i++) {
            roots.get(i).display(showCollisionShapes, layer);
        }
    }


    public void removeTip(Tip tip){
        leafs.remove(tip);
    }

    public ArrayList<Tip> getTips() {
        return leafs;
    }

    public void addTip(Tip a) {
        leafs.add(a);
    }

    public ArrayList<Hyphae> getRoots() {
        return roots;
    }
}
