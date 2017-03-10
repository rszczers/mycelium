import processing.core.PVector;

/**
 * Created by rszczers on 10.03.17.
 */
public class Mover {
    private PVector location;
    private PVector velocity;
    private PVector acceleration;
    private float mass;
    private Interpretation interp;

    public Mover(PVector location, float mass, Interpretation interp) {
        this.location = location;
        this.velocity = new PVector(0, 0);
        this.acceleration = new PVector(0, 0);
        this.mass = mass;
        this.interp = interp;
    }

    public void applyForce(PVector force) {
        PVector f = PVector.div(force,mass);
        acceleration.add(f);

    }

    public void update() {
        velocity.add(acceleration);
        location.add(velocity);
        acceleration.mult(0);
    }

    public void show() {
        interp.show((int)location.x, (int)location.y);
    }

    public PVector getLocation() {
        return location;
    }

    public PVector getVelocity() {
        return velocity;
    }

    public PVector getAcceleration() {
        return acceleration;
    }

    public float getMass() {
        return mass;
    }

    public void setLocation(PVector location) {
        this.location = location;
    }

    public void setVelocity(PVector velocity) {
        this.velocity = velocity;
    }

    public void setAcceleration(PVector acceleration) {
        this.acceleration = acceleration;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }
}
