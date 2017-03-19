import Box2D.Box2DProcessing;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.PApplet;
import processing.core.PShape;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by rszczers on 18.03.17.
 */
public class Hyphae {
    private float branchingRate;

    private PApplet context;
    private Box2DProcessing world;

    private Vec2 start;
    private ArrayList<Hyphae> childrens;

    private ArrayList<PolygonShape> collisionShape;
    private ArrayList<PShape> displayShape;

//    private ArrayList<BodyDef> bdArr;
//    private ArrayList<FixtureDef> fdArr;

    private Body body;
    private BodyDef bd;
    private FixtureDef fd;

    private Tip tip;
    public Hyphae(PApplet context, Box2DProcessing world, Vec2 start, Tip tip, Vec2 initialForce) {
        this.context = context;
        this.world = world;
        this.branchingRate = 0.5f;
        this.tip = tip;
        tip.applyForce(initialForce);
        this.start = start;

        bd = new BodyDef();
        bd.type = BodyType.STATIC;
        body = world.createBody(bd);
        bd.position.set(world.coordPixelsToWorld(start));
    }

    /**
     * Metoda do wykorzystania w petli głownej draw()
     * konstruuje kształty do kolizji
     */
    public void grow() {
        Vec2 last1 = collisionShape.get(collisionShape.size()-1).getVertex(2);
        Vec2 last2 = collisionShape.get(collisionShape.size()-1).getVertex(3);
        Vec2 lastMid = last1.add(last2).mulLocal(0.5f); //Środek końca ostatniego czworokąta
        Vec2 tipPosition = tip.getBody().getPosition();
        if(tipPosition.sub(lastMid).length() >= 100) {
            PolygonShape cs = createCollisionShape(new Vec2[] {last1, last2});
            PShape ds = createDisplayShape(new Vec2[] {world.vectorWorldToPixels(last1),
                            world.vectorWorldToPixels(last2)});
            body.createFixture(cs, 1.0f);
            collisionShape.add(cs);
            displayShape.add(ds);
//                bisect();

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
        float r = ((Ball) tip.getInterp()).getRadius();
        Vec2 direction = tip.getBody().getLinearVelocity().clone();
        direction.x = direction.x * (float)Math.cos(phi) - direction.y * (float)Math.sin(phi);
        direction.y = direction.x * (float)Math.sin(phi) + direction.y * (float)Math.cos(phi);
        direction.normalize();
        direction.mulLocal(r);
        coords[0] = tipLocation.add(direction);
        coords[1] = tipLocation.sub(direction);
        return coords;
    }

    /**
     * Wygeneruj PShape z czterech punktów
     * @return
     */
    private PShape createDisplayShape(Vec2[] initCoord) {
        Vec2[] tmp = createNewVerticesForShapes();
        PShape ps = new PShape();
        ps.setVisible(false);
        ps.beginShape();
            ps.vertex(initCoord[0].x, initCoord[0].y);
            ps.vertex(initCoord[1].x, initCoord[1].y);
            ps.vertex(tmp[0].x, tmp[0].y);
            ps.vertex(tmp[1].x, tmp[1].y);
        ps.endShape();
        return ps;
    }

    /**
     * Generuj shape dla kolizji
     * @return
     */
    private PolygonShape createCollisionShape(Vec2[] initCoord) {
        Vec2[] tmp = createNewVerticesForShapes();
        Vec2[] vertices = {
            initCoord[0],
            initCoord[1],
            tmp[0],
            tmp[1]
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
        Tip newTip = new Tip(world, newLocation, new Ball(context, world, 20));
        Hyphae newHyphae = new Hyphae(context, world, newLocation, newTip, newForce);

        childrens.add(newHyphae);
        return newHyphae;
    }

    public void display() {
        for (PShape ps :
                displayShape) {
            ps.setVisible(true);
        }
    }
}
