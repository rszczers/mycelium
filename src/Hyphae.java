import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import processing.core.PApplet;
import processing.core.PShape;
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

    public Hyphae(PApplet context, Box2DProcessing world, Vec2 start, Vec2 initialForce) {
        this.length = 0;
        this.context = context;
        this.world = world;
        this.branchingRate = 0.5f;
        this.start = start;

        this.tip = new Tip(world, start.add(new Vec2(0, -10)), new Ball(context, world, 20));
        tip.applyForce(initialForce);


        Vec2[] vertices = {
                world.vectorPixelsToWorld(new Vec2(10, 10)),
                world.vectorPixelsToWorld(new Vec2(10, 0)),
                world.vectorPixelsToWorld(new Vec2(-10, 0)),
                world.vectorPixelsToWorld(new Vec2(-10, 10)),
        };
    }


    public void grow() {
    }

    private Vec2[] createNewVerticesForShapes() {
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
        float forceMag = 5.0f; // Tip odrzucamy w bok z pewną sila forceMag
        double phi = 30;
        Vec2 newForce = new Vec2((float)(forceMag * Math.sin(phi)), (float) (forceMag * Math.cos(phi)));

        Random random = new Random(); // Losujemy czy na lewo czy na prawo
        if (random.nextBoolean())
            newForce.x = newForce.x * -1.0f;

        Vec2 newLocation = world.vectorPixelsToWorld(tip.getBody().getPosition().add(newForce).mul(20.0f));
        Hyphae newHyphae = new Hyphae(context, world, newLocation, newForce);

        childrens.add(newHyphae);
        return newHyphae;
    }

    public void display() {
        Vec2[] tmp = createNewVerticesForShapes();
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = world.coordWorldToPixels(tmp[i]);
        }
        context.pushMatrix();
        context.strokeWeight(3);
        context.ellipse(tmp[0].x, tmp[0].y, 5.0f, 5.0f);
        context.textSize(12);
        context.text("1", tmp[0].x, tmp[0].y-20);
        context.ellipse(tmp[1].x, tmp[1].y, 5.0f, 5.0f);
        context.text("2", tmp[1].x, tmp[1].y-20);
        context.strokeWeight(1);
        context.popMatrix();
        for (PShape ps :
                displayShapes) {
            context.shape(ps);
        }
        tip.display();
    }
}
