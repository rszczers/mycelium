import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import processing.core.PApplet;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by rszczers on 18.03.17.
 */
public class Hyphae {
    private int length;
    private float branchingRate;
    private PApplet context;
    private Box2DProcessing world;
    private Vec2 start;
    private ArrayList<Hyphae> childrens;
    private ArrayList<CollisionShape> shapes;
    private Tip tip;
    private Fungus fungus;
    private int[] color;

    public Hyphae(PApplet context, Box2DProcessing world, Fungus fungus, Vec2 start, double phi, float v0) {
        this.length = 0;
        this.fungus = fungus;
        this.context = context;
        this.world = world;
        this.branchingRate = 0.5f;
        this.start = start;
        this.childrens = new ArrayList<>();
        this.tip = new Tip(world, fungus, start.add(new Vec2(0, -10)), new Ball(context, world, 10));

        // Rotacja wektora prędkości czubka strzępka
        if (phi > 0.1 || phi < -0.1) {
            tip.getBody().setLinearVelocity(
                    world.vectorPixelsToWorld(new Vec2(1.0f, 1.0f)).addLocal(tip.getBody().getLinearVelocity()).mulLocal(
                            world.scalarPixelsToWorld(v0)));
            Vec2 velocityRot = tip.getBody().getLinearVelocity().clone();
            velocityRot.x = v0 * (float) (velocityRot.x * Math.cos(phi) - velocityRot.y * Math.sin(phi));
            velocityRot.y = v0 * (float) (velocityRot.x * Math.sin(phi) + velocityRot.y * Math.cos(phi));
//            System.out.println("Prędkość na początku: " + tip.getBody().getLinearVelocity() + "\n" +
//                    "Prędkość po rotacji: " + velocityRot);
            this.tip.getBody().setLinearVelocity(velocityRot);
        }
        // Konstrukcja kształtu do kolizji i wyświetlania
        shapes = new ArrayList<>();

        Vec2[] last = new Vec2[] {
            world.coordPixelsToWorld(start.add(new Vec2(5.0f, 5.0f))),
            world.coordPixelsToWorld(start.add(new Vec2(-5.0f, 5.0f))),
        };
        Vec2[] next = new Vec2[] {
            world.coordPixelsToWorld(start.add(new Vec2(5.0f, 0.0f))),
            world.coordPixelsToWorld(start.add(new Vec2(-5.0f, 0.0f))),
        };

        Random rand = new Random();
        color = new int[] {rand.nextInt() % 127 + 127, rand.nextInt() % 127 + 127, rand.nextInt() % 127 + 127, 127};
        shapes.add(new CollisionShape(context, world, last, next, color));
    }


    public void grow() {
            int size = shapes.size();
            Vec2[] last = shapes.get(size - 1).getLast(); //Znajdź dwie współrzędne ostatniego CollisionShape'a
            Vec2[] next = nextVertices(); //Znajdź dwie współrzędne dla czubka strzępka
            float d = dist(shapes.get(size - 1), tip.getBody());
            //        System.out.println("Distance: " + d);
            if (d >= 20.0f) {
                shapes.add(new CollisionShape(context, world, last, next, color));
                if (length % 10 == 9) {
                    bisect();
                }
                this.length++;
            }

        for (Hyphae h :
                childrens) {
            h.grow();
        }
    }

    /**
     * [0] stands for left vertex,
     * [1] stands for right vertex.
     * @return
     */
    private Vec2[] nextVertices() {
        float phi = - (float)Math.PI/2;
        Vec2[] coords = new Vec2[2];
        Vec2 tipLocation = tip.getBody().getPosition();
        float r = world.scalarPixelsToWorld(((Ball) tip.getInterp()).getRadius())/2;
        Vec2 direction = tip.getBody().getLinearVelocity().clone();
        float tmp = direction.x;
        direction.x = -1 * direction.y;
        direction.y = tmp;
        direction.normalize();
        direction.mulLocal(r);
        coords[1] = tipLocation.add(direction);
        coords[0] = tipLocation.sub(direction);
        return coords;
    }

    private Hyphae bisect() {
        Vec2 newStart = world.getBodyPixelCoord(tip.getBody());
        Random random = new Random();
        float t = random.nextBoolean() ? 1.0f : -1.0f;
        Hyphae newHyphae = new Hyphae(context, world, fungus, newStart, t*Math.PI/6, tip.getBody().getLinearVelocity().length());
        childrens.add(newHyphae);
        return newHyphae;
    }

    private float dist(CollisionShape s1, CollisionShape s2) {
        return world.scalarWorldToPixels(s1.getPosition().sub(s2.getPosition()).length());
    }

    private float dist(CollisionShape s1, Body s2) {
        Vec2 ps1 = world.getBodyPixelCoord(s1.getBody()).clone();
        Vec2 ps2 = world.getBodyPixelCoord(s2).clone();
        float d = ps1.subLocal(ps2).length();
        return d;
    }

    public void applyForce(Vec2 force) {
        tip.applyForce(force);
        for (Hyphae h :
                childrens) {
            getTip().applyForce(force);
        }
    }

    public void display() {
        Vec2[] tmp = nextVertices();
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = world.coordWorldToPixels(tmp[i]);
        }
        context.pushMatrix();
        context.strokeWeight(3);
        context.ellipse(tmp[0].x, tmp[0].y, 5.0f, 5.0f);
        context.textSize(12);
//        context.text("1", tmp[0].x, tmp[0].y-20);
        context.ellipse(tmp[1].x, tmp[1].y, 5.0f, 5.0f);
//        context.text("2", tmp[1].x, tmp[1].y-20);
        context.strokeWeight(1);
        context.popMatrix();
        for (CollisionShape p:
                shapes) {
            p.display();
        }
        for (Hyphae h :
                childrens) {
            h.display();
        }
        tip.display();
    }

    public Tip getTip() {
        return tip;
    }
}
