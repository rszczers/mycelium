import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by rszczers on 10.03.17.
 */
public class Tip {
    private Interpretation interp;
    private Box2DProcessing world;
    private Body body;
    private Hyphae owner;

    private int[][] coordDiff = new int[2][2];
    private int diffCounder = 0;

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

    public void display(PGraphics layer) {
        Vec2 v = world.getBodyPixelCoord(body);
        float phi = body.getAngle();
        interp.display(v, phi, layer);
    }

    public Body getBody() {
        return body;
    }

    public Interpretation getInterp() {
        return interp;
    }

    public void colideWithOtherTip(Tip tip) {
        Hyphae hyp1 = this.getOwner();
        Hyphae hyp2 = tip.getOwner();
        Vec2[] base = hyp1.getLastVertices();
        Vec2[] top = hyp2.getLastVertices();
        hyp1.addToShapes(base, top);
        hyp1.killHyphae();
        hyp2.killHyphae();

    }

    public void killBody() {
        world.destroyBody(this.body);
        body.setUserData(null);
        owner.setIsGrowing(false);
    }

    /**
     * Zamienia współrzędne ekranu na współrzędne tablicy komórek
     *
     * @param x
     * @param y
     * @return
     */
    private int[] c2vf(int x, int y, int width, int height, int grid) {
        return new int[]{x / (width / grid), y / (height / grid)};
    }

    /**
     * Zamienia współrzędne tablicy komórek na współrzędne piksela lewego górnego rogu odpowiedniej komórki
     *
     * @param x
     * @param y
     * @return
     */
    private int[] vf2c(int x, int y, int width, int height, int grid) {
        return new int[]{x * (width / grid), y * (width / height)};
    }

    /**
     * Zwraca współrzędne środka komórki o danych indeksach siatki (NIE WYŚWIETLACZA)
     *
     * @param x
     * @param y
     * @return
     */
    private Vec2 middleCords(int x, int y, int width, int height, int grid) {
        int[] leftTopCords = vf2c(x, y, width, height, grid);
        Vec2 middleCords = new Vec2(leftTopCords[0], leftTopCords[1]);
        Vec2 midC = middleCords.add((new Vec2(width / grid, height / grid)).mul(0.5f));
        return midC;
    }

    /**
     * Sprawdza, czy tip zmienił komórkę siatki
     *
     * @param width
     * @param height
     * @param grid
     * @return
     */
    public boolean hasChanged(int width, int height, int grid) {
        boolean result;
        Vec2 position = world.coordWorldToPixels(body.getPosition());
        coordDiff[diffCounder] = c2vf((int) position.x, (int) position.y, width, height, grid);
//        System.out.println("POZYCJA\t " + diffCounder + "\t " + Arrays.toString(coordDiff[diffCounder]));
        if (coordDiff[0][0] != coordDiff[1][0] || coordDiff[0][1] != coordDiff[1][1]) {
            result = true;
        } else {
            result = false;
        }
        diffCounder = (diffCounder + 1) % 2;
//        System.out.println("Zmiena? " + result);
        return result;
    }


    public void makeHypheField(int fungusIntenectFactor, int width, int height, int grid, VectorField vf, float forceValue, PGraphics layer) {
        if (this.getOwner().getIsGrowing() && this.hasChanged(width, height, grid) && this.getOwner().getLength() > 1) {
//
            Vec2 tipCorInt = world.coordWorldToPixels(this.getBody().getPosition());
            float dist = (float) Math.sqrt((height / grid) * (height / grid) + (width / grid) * (width / grid)); //Przekątna prostokąta +1
//            float dist = height + 1;
            Vec2 tipVeliocity = new Vec2(world.vectorWorldToPixels(this.getBody().getLinearVelocity())); // Wektor prędkości
            tipVeliocity.normalize();
            tipVeliocity = tipVeliocity.mulLocal(dist);
            Vec2 backTip = tipCorInt.sub(tipVeliocity); // punkt zwiadowca

            float cellWidth = width / grid;
            float cellHeight = height / grid;

            float r = ((Ball) (interp)).getRadius();
            float fungusIntelect = r * fungusIntenectFactor;
            Vec2 maxReachVec = new Vec2(-tipVeliocity.y, tipVeliocity.x); // Obrót znormalizowano-pomnożonego wektora prędkości tipa
            maxReachVec.normalize();
            maxReachVec.mulLocal(fungusIntelect);

            Vec2 leftForce = maxReachVec.mul(-1.0f);
            leftForce.normalize();
            Vec2 rightForce = new Vec2(maxReachVec);
            rightForce.normalize();

            Vec2 leftEnd = backTip.sub(maxReachVec);
            Vec2 rightEnd = backTip.add(maxReachVec);

            int[] backTipCellCoords = c2vf((int) backTip.x, (int) backTip.y, width, height, grid); //glupi output motody, zamiast Vec2 zwraca tablicę intów
//            Vec2 cellCoordsOfBackTip = new Vec2(backTipCellCoords[0], backTipCellCoords[1]);
            int[] xyleft = c2vf((int) leftEnd.x, (int) leftEnd.y, width, height, grid);
            int[] xyright = c2vf((int) rightEnd.x, (int) rightEnd.y, width, height, grid);



            ArrayList<int[]> cellsToChangeToLeft = brasenham(backTipCellCoords[0], backTipCellCoords[1], xyleft[0], xyleft[1], grid);
            ArrayList<int[]> cellsToChangeToRight = brasenham(backTipCellCoords[0], backTipCellCoords[1], xyright[0], xyright[1], grid);
            float s = (float) cellsToChangeToLeft.size();
            for (int i = 0; i < cellsToChangeToLeft.size(); i++) {
//                vf.standardBlock(cellsToChangeToLeft.get(i), leftForce, forceValue/(i+1));
                vf.standardBlock(cellsToChangeToLeft.get(i), leftForce, (-forceValue/s)*i+forceValue);
            }
            for (int i = 0; i < cellsToChangeToRight.size(); i++) {
                vf.standardBlock(cellsToChangeToRight.get(i), rightForce, (-forceValue/s)*i+forceValue);
            }

            Vec2 backTipVelocity = new Vec2(world.vectorWorldToPixels(this.getBody().getLinearVelocity()));
            float cosphi = backTipVelocity.x / backTipVelocity.length(); //nachylenie wektora prędkości backtip
            Vec2 backTipCellCenterAbsCoords =
                    middleCords(backTipCellCoords[0], backTipCellCoords[1], width, height, grid);
            Vec2 backTipRelativeToMiddlePosition = backTip.sub(backTipCellCenterAbsCoords);
            float phi;
            if (backTipVelocity.y > 0) {
                phi = (float) (Math.PI / 2 - Math.acos(cosphi));
            } else {
                phi = (float) (Math.PI / 2 + Math.acos(cosphi));
            }
            Vec2 rotatedRelativeBackTipPosition = new Vec2(
                    (float) (backTipRelativeToMiddlePosition.x * Math.cos(phi) -
                            backTipRelativeToMiddlePosition.y * Math.sin(phi)),
                    (float) (backTipRelativeToMiddlePosition.x * Math.sin(phi) +
                            backTipRelativeToMiddlePosition.y * Math.cos(phi)));

            float backTipFraction = 1.0f;
            if (rotatedRelativeBackTipPosition.x >= 0) {
                vf.standardBlock(backTipCellCoords, rightForce, forceValue);
            } else {
                vf.standardBlock(backTipCellCoords, leftForce, forceValue);
            }
        }
    }

    private ArrayList<int[]> brasenham(int x0, int y0, int x1, int y1, int limit) {
        ArrayList<int[]> out = new ArrayList<>();

        int dx = Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
        int dy = Math.abs(y1-y0), sy = y0<y1 ? 1 : -1;
        int err = (dx>dy ? dx : -dy)/2, e2;

        for(;;) {
            if (x0 >= 0 && y0 >= 0 && x0 < limit && y0 < limit) {
                out.add(new int[]{x0, y0});
            }
            if (x0==x1 && y0==y1) break;
            e2 = err;
            if (e2 >-dx) { err -= dy; x0 += sx; }
            if (e2 < dy) { err += dx; y0 += sy; }
        }

        return out;
    }


    public Hyphae getOwner() {
        return owner;
    }
}
