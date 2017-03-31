import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import processing.core.PApplet;

/**
 * Created by rszczers on 10.03.17.
 */
public class Tip {
    private Interpretation interp;
    private Box2DProcessing world;
    private Body body;
    private Hyphae owner;

    public Tip(Box2DProcessing world, Fungus fungus, Vec2 location, Interpretation interp, Hyphae owner) {
        this.world = world;
        this.interp = interp;
        this.owner = owner;

        // Ustalenie pozycji komórki w BodyDef
        interp.getBodyDef().position.set(world.coordPixelsToWorld(location));

        // Inicjalizacja ciała
        body = world.createBody(interp.getBodyDef());
        body.setUserData(this);

        // Dowiązanie ciała do kształtu
        body.createFixture(interp.getFixtureDef());

        fungus.addTip(this);
    }

    public void applyForce(Vec2 force) {
        body.applyForce(world.vectorPixelsToWorld(force), body.getPosition());
    }

    public void display() {
        Vec2 v = world.getBodyPixelCoord(body);
        float phi = body.getAngle();
        interp.display(v, phi);
    }

    public Body getBody() {
        return body;
    }
    public Interpretation getInterp() {return interp; }

    void killBody() {
        world.destroyBody(body);
    }

    public Hyphae getOwner(){
        return owner;
    }
}
