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
    private Vec2[] base;
    private Vec2[] head;
    private ArrayList<Hyphae> childrens;
    private Hyphae parent;
    private ArrayList<CollisionShape> shapes;
    private Tip tip;
    private Fungus fungus;
    private int[] color;

    private int direction;

    public Hyphae(PApplet context, Box2DProcessing world, Fungus fungus, Hyphae parent, Vec2[] base, double phi, float v0) {
        this.length = 0;
        this.fungus = fungus;
        this.context = context;
        this.world = world;
        this.branchingRate = 0.5f;
        this.base = base;
        this.childrens = new ArrayList<>();
        this.parent = parent;
        Vec2 middle = world.coordWorldToPixels(base[0].add(base[1]).mul(0.5f));
        Vec2 tipOffset = base[0].sub(base[1]);

        float tmp = tipOffset.x;
        tipOffset.x = tipOffset.y;
        tipOffset.y = tmp;
        tipOffset.normalize();
        this.tip = new Tip(world, fungus, middle.add(tipOffset.mulLocal(12.0f)), new Ball(context, world, 10));


        // Rotacja wektora prędkości czubka strzępka
        if (phi > 0.1 || phi < -0.1) {
            Vec2 velocityRot = parent.getTip().getBody().getLinearVelocity().clone();// new Vec2(0, v0);
            tmp = velocityRot.x;

            velocityRot.x = (float) (velocityRot.x * Math.cos(phi) - velocityRot.y * Math.sin(phi));
            velocityRot.y = (float) (tmp * Math.sin(phi) + velocityRot.y * Math.cos(phi));
//            System.out.println("Prędkość na początku: " + tip.getBody().getLinearVelocity() + "\n" +
//                    "Prędkość po rotacji: " + velocityRot);
            this.tip.getBody().setLinearVelocity(velocityRot);

        }
        // Konstrukcja kształtu do kolizji i wyświetlania
        shapes = new ArrayList<>();
        Random rand = new Random();
        color = new int[]{rand.nextInt() % 127 + 127, rand.nextInt() % 127 + 127, rand.nextInt() % 127 + 127, 127};
        shapes.add(new CollisionShape(context, world, base, base, color));
    }


    public void grow() {
        int size = shapes.size();
        Vec2[] last = shapes.get(size - 1).getTop().clone(); //Znajdź dwie współrzędne ostatniego CollisionShape'a
        Vec2[] next = nextVertices(); //Znajdź dwie współrzędne dla czubka strzępka

        Random random = new Random();
        float leftOrRight = random.nextBoolean() ? 1.0f : -1.0f;

        float d = dist(shapes.get(size - 1), tip.getBody());
        //        System.out.println("Distance: " + d);
        if (d >= 30.0f) {
            if (length % 20 == 10) {
                bisect(leftOrRight);
            }
            shapes.add(new CollisionShape(context, world, last, next, color));
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
     *
     * @return
     */
    private Vec2[] nextVertices() {
        float phi = -(float) Math.PI / 2;
        Vec2[] coords = new Vec2[2];
        Vec2 tipLocation = tip.getBody().getPosition().clone();
//        tipLocation.subLocal(new Vec2(0, 2));
        float r = world.scalarPixelsToWorld(((Ball) tip.getInterp()).getRadius()) / 2;
        Vec2 direction = tip.getBody().getLinearVelocity().clone();
        Vec2 offset = direction.clone();
        offset.normalize();
        offset.mulLocal(r + 1f);
        float tmp = direction.x;
        direction.x = -1 * direction.y;
        direction.y = tmp;
        direction.normalize();
        direction.mulLocal(r);
        coords[0] = tipLocation.add(direction).subLocal(offset);
        coords[1] = tipLocation.sub(direction).subLocal(offset);
        return coords;
    }


    private void bisect(float leftOrRight) {
        CollisionShape shapeToGrow = shapes.get(shapes.size() - 1);
        Vec2[] lastInit;
        if (leftOrRight == 1.0f) {  // Rosnij w lewo (sprawdzić)
            lastInit = shapeToGrow.getLeft();
//            System.out.println("Lewo! ");
        } else {    // Rosnij w prawo
            lastInit = shapeToGrow.getRight();
//            System.out.println("Prawo!");
        }

        Hyphae newHyphae = new Hyphae(context, world, fungus, this, lastInit, leftOrRight * Math.PI / 3, tip.getBody().getLinearVelocity().length());
        childrens.add(newHyphae);
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
//        Vec2[] tmp = nextVertices();
//        for (int i = 0; i < tmp.length; i++) {
//            tmp[i] = world.coordWorldToPixels(tmp[i]);
//        }
//        context.pushMatrix();
//        context.strokeWeight(3);
//        context.ellipse(tmp[0].x, tmp[0].y, 5.0f, 5.0f);
//        context.textSize(12);
//        context.text("1", tmp[0].x, tmp[0].y-20);
//        context.ellipse(tmp[1].x, tmp[1].y, 5.0f, 5.0f);
//        context.text("2", tmp[1].x, tmp[1].y-20);
//        context.strokeWeight(1);
//        context.popMatrix();

//        for (CollisionShape p :
//                shapes) {
//            p.display();
//        }
        for (Hyphae h :
                childrens) {
            h.display();
        }
        //        tip.display();
    }

    public Tip getTip() {
        return tip;
    }

    public ArrayList<CollisionShape> getShapes() {
        return shapes;
    }
}
