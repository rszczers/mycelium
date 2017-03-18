import Box2D.Box2DProcessing;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.PApplet;
import processing.core.PShape;

import java.util.ArrayList;

/**
 * Created by rszczers on 18.03.17.
 */
public class Hyphae {
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
    public Hyphae(Vec2 start, Tip tip, Vec2 force) {
        this.tip = tip;
        tip.applyForce(force);
        this.start = start;
    }

    /**
     * Dodaje dziecko
     * @return
     */

    private Hyphae bisect() {
        Vec2 newLocation = world.vectorPixelsToWorld(tip.getBody().getPosition());
        Hyphae newHyphae = new Hyphae(newLocation);
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
