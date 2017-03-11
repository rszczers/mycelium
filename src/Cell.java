import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

/**
 * Created by rszczers on 10.03.17.
 */
public class Cell {
    private Vec2 location;
    private Interpretation interp;
    private Box2DProcessing world;
    private Body body;

    public Cell(Box2DProcessing world, Vec2 location, Interpretation interp) {
        this.world = world;
        this.location = location;
        this.interp = interp;

        // Ustalenie pozycji komórki w BodyDef
        interp.getBodyDef().position.set(world.coordPixelsToWorld(location));

        // Inicjalizacja ciała
        body = world.createBody(interp.getBodyDef());

        // Dowiązanie ciała do kształtu
        body.createFixture(interp.getFixtureDef());
    }

    public void applyForce(Vec2 force) {
        body.applyForce(world.coordPixelsToWorld(force), world.coordPixelsToWorld(location));
    }

    public void display() {
        this.location = body.getPosition();
        Vec2 v = world.getBodyPixelCoord(body);
        float phi = body.getAngle();
        interp.display(v, phi);
    }

    public Vec2 getLocation() {
        return location;
    }


    void killBody() {
        world.destroyBody(body);
    }
}
