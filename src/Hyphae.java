import Box2D.Box2DProcessing;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
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

    private ArrayList<BodyDef> bd;
    private ArrayList<FixtureDef> fd;

    private Body body;

    private Tip tip;
    public Hyphae(PApplet context, Box2DProcessing world, Vec2 start, Tip tip, Vec2 initialForce) {
        this.context = context;
        this.world = world;
        this.branchingRate = 0.5f;
        this.tip = tip;
        tip.applyForce(initialForce);
        this.start = start;
    }

    /**
     * Metoda do wykorzystania w petli głownej draw()
     * konstruuje kształty do kolizji
     */
    public void grow() {
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
    private Vec2[] getNewVerticesForShapes() {
        return null;
    }

    /**
     * Wygeneruj PShape z czterech punktów
     * @return
     */
    private PShape makeDisplayShape(Vec2[] coord) {
        Vec2 coord0 = collisionShape.get(collisionShape.size()-1).getVertex(2);
        Vec2 coord1 = collisionShape.get(collisionShape.size()-1).getVertex(3);
        Vec2[] tmp = getNewVerticesForShapes();
        PShape ps = new PShape();
        ps.setVisible(false);
        ps.beginShape();
            ps.vertex(coord0.x, coord0.y);
            ps.vertex(coord1.x, coord1.y);
            ps.vertex(tmp[0].x, tmp[0].y);
            ps.vertex(tmp[1].x, tmp[1].y);
        ps.endShape();
        return ps;
    }

    private PolygonShape makeCollisionShape(Vec2[] coord) {
        Vec2[] tmp = getNewVerticesForShapes();
        Vec2[] vertices = {
            collisionShape.get(collisionShape.size()-1).getVertex(2),
            collisionShape.get(collisionShape.size()-1).getVertex(3),
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
