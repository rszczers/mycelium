import Box2D.Box2DProcessing;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Created by rszczers on 11.03.17.
 */
public class BoundaryBox {
    int x;
    int y;
    float width;
    float height;
    Body body;
    BodyDef bd;
    PolygonShape ps;
    PApplet context;

    public BoundaryBox(Box2DProcessing world, PApplet context, int x, int y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.context = context;

        bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(world.coordPixelsToWorld(x, y));

        ps = new PolygonShape();
        float worldWidth = world.scalarPixelsToWorld(width/2);
        float worldHeight = world.scalarPixelsToWorld(height/2);
        ps.setAsBox(worldWidth, worldHeight);

        body = world.createBody(bd);
        body.createFixture(ps, 1);
    }

    void display() {
        context.fill(127, 0, 0);
        context.stroke(127, 0, 0);
        context.rectMode(PConstants.CENTER);
        context.rect(x, y, width, height);
        context.noFill();
    }
}
