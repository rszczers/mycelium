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
    private boolean isGrowing;

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
        this.isGrowing = true;
        Vec2 middle = world.coordWorldToPixels(base[0].add(base[1]).mul(0.5f));
        Vec2 tipOffset = base[0].sub(base[1]);

        float tmp = tipOffset.x;
        tipOffset.x = tipOffset.y;
        tipOffset.y = tmp;
        tipOffset.normalize();

        int tipWidth = (int) world.scalarWorldToPixels(base[0].sub(base[1]).length());
        this.tip = new Tip(world, fungus, middle.add(tipOffset.mulLocal(tipWidth)), new Ball(context, world, tipWidth), this);

        // Rotacja wektora prędkości czubka strzępka
        //if (phi > 0.1 || phi < -0.1) {
        if (parent != null) {
            Vec2 velocityRot = parent.getTip().getBody().getLinearVelocity().clone();// new Vec2(0, v0);
            tmp = velocityRot.x;

            velocityRot.x = (float) (velocityRot.x * Math.cos(phi) - velocityRot.y * Math.sin(phi));
            velocityRot.y = (float) (tmp * Math.sin(phi) + velocityRot.y * Math.cos(phi));
//            System.out.println("Prędkość na początku: " + tip.getBody().getLinearVelocity() + "\n" +
//                    "Prędkość po rotacji: " + velocityRot);
            this.tip.getBody().setLinearVelocity(velocityRot);

        } else {
            this.tip.getBody().setLinearVelocity(new Vec2(0.0f, 0.1f));
        }

        // Konstrukcja kształtu do kolizji i wyświetlania
        shapes = new ArrayList<>();
        Random rand = new Random();
        color = new int[]{rand.nextInt() % 127 + 127, rand.nextInt() % 127 + 127, rand.nextInt() % 127 + 127, 127};
        shapes.add(new CollisionShape(context, world, base, base, color));
    }


    public void grow(float boxWidth, float boxHeight) {
        if (isGrowing) {
            for (CollisionShape colS :
                    shapes) {
                if (!colS.isHasBody())
                    colS.init();
            }

            int size = shapes.size();
            float d = dist(shapes.get(size - 1), tip.getBody());

            if (d >= boxHeight) {
                if (length % 20 == 5) {
                    Random random = new Random();
                    float leftOrRight = random.nextBoolean() ? 1.0f : -1.0f;
                    bisect(leftOrRight, boxWidth, boxHeight);
                }
                Vec2[] last = shapes.get(size - 1).getTop(); //Znajdź dwie współrzędne ostatniego CollisionShape'a
                CollisionShape newShape = new CollisionShape(context, world, last, nextVertices(), color);
                shapes.add(newShape);
                this.length++;
            }
        }
        for (Hyphae h :
                childrens) {
            h.grow(boxWidth, boxHeight);
        }
    }

    public void killHyphae() {
        tip.getBody().setLinearVelocity(new Vec2(0.0f, 0.0f));
        tip.getBody().setAngularVelocity(0.0f);
        tip.getBody().setGravityScale(0);
        this.isGrowing = false;
        mycelium.tipsToDelete.add(tip);
        tip.setVisible(false);
    }

    public void joinHyphae(Vec2[] sideVertices){
            Vec2[] lastVertices = this.getLastVertices();
            this.shapes.add(new CollisionShape(context, world,  lastVertices, sideVertices, color));
    }

    public void collisionWithHyphae(CollisionShape collisionShape, float boxWidth, float boxHeight){
        Vec2[] left = collisionShape.getLeft();
        Vec2[] right = collisionShape.getRight();
        Vec2[] lastVert = this.getLastVertices();
        Vec2 midLeft = left[0].add(left[1]).mulLocal(0.5f);
        Vec2 midRight = right[0].add(right[1]).mulLocal(0.5f);
        Vec2 midLastVert = lastVert[0].add(lastVert[1]).mulLocal(0.5f);
        float distLeft = midLeft.sub(midLastVert).length();
        float distRight = midRight.sub(midLastVert).length();
        float min = Math.min(distLeft, distRight);

        Vec2 tmp = right[0];
        right[0] = right[1];
        right[1] = tmp;
        tmp = left[0];
        left[0] = left[1];
        left[1] = tmp;

        if(min < distLeft){
            if(boxWidth < boxHeight) {
                right[0] = right[0].sub(right[1]);
                right[0].normalize();
                right[0].mulLocal(world.scalarPixelsToWorld(boxWidth));
                right[0] = right[1].add(right[0]);
            } else {
                throw new IllegalArgumentException("Za duże");
            }

            System.out.println("Łączę w prawo!");
            joinHyphae(right);
        } else {
            System.out.println("Łoncze w lewo!");
            if(boxWidth < boxHeight) {
                left[1] = left[1].sub(left[0]);
                left[1].normalize();
                left[1].mulLocal(world.scalarPixelsToWorld(boxWidth));
                left[1] = left[0].add(left[1]);
            } else {
                throw new IllegalArgumentException("Za duże");
            }
            joinHyphae(left);
        }
        killHyphae();
    }

    /**
     * [0] stands for left vertex,
     * [1] stands for right vertex.
     *
     * @return
     */
    private Vec2[] nextVertices() {
        Vec2[] coords = new Vec2[2];
        Vec2 tipLocation = tip.getBody().getPosition().clone();
        float r = world.scalarPixelsToWorld(((Ball) tip.getInterp()).getRadius()) / 2;
        Vec2 direction = new Vec2(tip.getBody().getLinearVelocity());
        Vec2 offset = new Vec2(direction);
        direction.normalize();
        direction.mulLocal(2*r);
        float tmp = offset.x;
        offset.x = -1 * offset.y;
        offset.y = tmp;
        offset.normalize();
        offset.mulLocal(r);
        coords[0] = new Vec2(tipLocation);
        coords[1] = new Vec2(tipLocation);
        coords[0].addLocal(offset).subLocal(direction);
        coords[1].subLocal(offset).subLocal(direction);
        return coords;
    }


    private void bisect(float leftOrRight, float boxWidth, float boxHeight) {
        CollisionShape shapeToGrow = shapes.get(shapes.size() - 1);
        Vec2[] lastInit;
        if (leftOrRight == 1.0f) {  // Rosnij w lewo (sprawdzić)
            lastInit = shapeToGrow.getLeft();

        } else {    // Rosnij w prawo
            lastInit = shapeToGrow.getRight();
        }

        if(boxWidth < boxHeight) {
            lastInit[0] = lastInit[0].sub(lastInit[1]);
            lastInit[0].normalize();
            lastInit[0].mulLocal(world.scalarPixelsToWorld(boxWidth));
            lastInit[0] = lastInit[1].add(lastInit[0]);
        } else {
            throw new IllegalArgumentException("Za duże");
        }

        Hyphae newHyphae = new Hyphae(context, world, fungus, this, lastInit, leftOrRight * Math.PI / 3, tip.getBody().getLinearVelocity().length());
        childrens.add(newHyphae);
    }

    private float dist(CollisionShape s1, CollisionShape s2) {
        return world.scalarWorldToPixels(s1.getPosition().sub(s2.getPosition()).length());
    }

    private float dist(CollisionShape s1, Body s2) {
        Vec2 ps1 = new Vec2(world.getBodyPixelCoord(s1.getBody()));
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

    public void display(boolean showCollisionShapes) {
        if (showCollisionShapes) {
            for (CollisionShape p :
                    shapes) {
                p.display();
            }
        }
        for (Hyphae h :
                childrens) {
            h.display(showCollisionShapes);
        }
    }

    public Tip getTip() {
        return tip;
    }

    public void setTipVelocity(Vec2 v) {
        this.tip.getBody().setLinearVelocity(v);
    }

    public ArrayList<CollisionShape> getShapes() {
        return shapes;
    }

    public void addToShapes(Vec2[] top, Vec2[] bottom) {
        CollisionShape collisionShape = new CollisionShape(context, world, bottom, top, color);
        this.shapes.add(collisionShape);
    }

    public Vec2[] getLastVertices() {
        int size = this.shapes.size();
        return this.shapes.get(size - 1).getTop();
    }

    public boolean getIsGrowing(){
        return isGrowing;
    }

    public void setIsGrowing(boolean isGrowing){
        this.isGrowing = isGrowing;
    }
}
