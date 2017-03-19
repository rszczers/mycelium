import Box2D.Box2DProcessing;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.PApplet;
import processing.core.PShape;

import java.nio.channels.Pipe;
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

    private ArrayList<PolygonShape> collisionShapes;
    private ArrayList<PShape> displayShapes;

//    private ArrayList<BodyDef> bdArr;
//    private ArrayList<FixtureDef> fdArr;

    private Body body;
    private BodyDef bd;
    private FixtureDef fd;

    private Tip tip;
    public Hyphae(PApplet context, Box2DProcessing world, Vec2 start, Vec2 initialForce) {
        this.length = 0;
        this.context = context;
        this.world = world;
        this.branchingRate = 0.5f;
        this.start = start;
        this.collisionShapes = new ArrayList<>();
        this.displayShapes = new ArrayList<>();

        this.tip = new Tip(world, start.add(new Vec2(0, -10)), new Ball(context, world, 20));
        tip.applyForce(initialForce);

        bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(world.coordPixelsToWorld(start));
        body = world.createBody(bd);
        System.out.println("Start: " + start);

        PolygonShape initCollisionShape = new PolygonShape();
        Vec2[] vertices = {
            new Vec2(10, 10),
            new Vec2(10, 0),
            new Vec2(-10, 0),
            new Vec2(-10, 10),
        };

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = world.vectorPixelsToWorld(vertices[i]);
        }
        initCollisionShape.set(vertices, vertices.length);
        body.createFixture(initCollisionShape, 1.0f);

        collisionShapes.add(initCollisionShape);

        float x = world.getBodyPixelCoord(body).x;
        float y = world.getBodyPixelCoord(body).y;
        PShape ps = context.createShape();
        context.pushMatrix();
            ps.beginShape();
                ps.fill(10, 127);
                ps.stroke(0);
                ps.strokeWeight(0);
                ps.vertex(x + 10, y + 10);
                ps.vertex(x - 10, y + 10);
                ps.vertex(x - 10, y);
                ps.vertex(x + 10, y);
            ps.endShape(PApplet.CLOSE);
        context.shape(ps);
        context.popMatrix();
        displayShapes.add(ps);
    }

    /**
     * Metoda do wykorzystania w petli głownej draw()
     * konstruuje kształty do kolizji
     */
    public void grow() {
        Vec2 last1 = collisionShapes.get(collisionShapes.size()-1).getVertex(1);
        Vec2 last2 = collisionShapes.get(collisionShapes.size()-1).getVertex(2);

        Vec2 lastMid = last1.add(last2).mulLocal(0.5f); //Środek końca ostatniego czworokąta
        Vec2 tipPosition = tip.getBody().getPosition();

        if (tipPosition.sub(lastMid).length() >= 10) {
//            System.out.println(tipPosition.sub(lastMid).length() + "Robie!");
            PolygonShape cs = createCollisionShapes(
                    new Vec2[] {
                        last1,
                        last2});

            PShape ds = createDisplayShapes(
                    new Vec2[] {
                        world.coordWorldToPixels(last1),
                        world.coordWorldToPixels(last2)
                    });

//            System.out.println(world.coordWorldToPixels(last1) + " : " + world.coordPixelsToWorld(last2));

            body.createFixture(cs, 1.0f);
            collisionShapes.add(cs);
            displayShapes.add(ds);
        }
    }

    /**
     * Zwróć dwia punkty z tipa do budowy kształtów do kolizji
     *
     * .     .
     * |     |
     * *-----*
     * 2     1
     * ^     ^
     * |     |
     * 3     4
     * *-----*
     * |     |
     * |     |
     * *-----*
     * 2     1
     *
     * tmp[0] niech będzie 3->2, tmp[1] niech będzie 4->1
     * @return
     */
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

    /**
     * Wygeneruj PShape z czterech punktów
     * @return
     */
    private PShape createDisplayShapes(Vec2[] initCoord) {
        Vec2[] tmp = createNewVerticesForShapes();
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = world.coordWorldToPixels(tmp[i]);
        }

        PShape ps = context.createShape();
        context.pushMatrix();
            ps.beginShape();
                ps.fill(10, 127);
                ps.stroke(0);
                ps.strokeWeight(0);
                ps.vertex(tmp[0].x, tmp[0].y);
                ps.vertex(initCoord[0].x, initCoord[0].y);
                ps.vertex(initCoord[1].x, initCoord[1].y);
                ps.vertex(tmp[1].x, tmp[1].y);
//                ps.setVisible(false);
        ps.endShape(PApplet.CLOSE);
        context.shape(ps);
        context.popMatrix();
        return ps;
    }

    /**
     * Generuj shape dla kolizji
     * @return
     */
    private PolygonShape createCollisionShapes(Vec2[] initCoord) {
        Vec2[] tmp = createNewVerticesForShapes();
        Vec2[] vertices = {
            initCoord[0],
            tmp[0],
            tmp[1],
            initCoord[1]
        };
        PolygonShape ps = new PolygonShape();
        ps.set(vertices, vertices.length);
        return ps;
    }


    /**
     * Dodaje dziecko
     * @return
     */
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
