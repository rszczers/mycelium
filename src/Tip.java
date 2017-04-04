import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import processing.core.PApplet;
import processing.core.PGraphics;

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


    public void makeHypheField(Tip tip, int width, int height, int grid, VectorField vf, float forceField) {
        if (tip.getOwner().getIsGrowing() && tip.getOwner().getLength() > 2) {

            Vec2 tipCorInt = world.coordWorldToPixels(tip.getBody().getPosition());
            float dist = (float) Math.sqrt((height / grid) * (height / grid) + (width / grid) * (width / grid)) + 1; //Przekątna prostokąta +1
            Vec2 tipVeliocity = new Vec2(world.vectorWorldToPixels(tip.getBody().getLinearVelocity())); // Wektor prędkości
            tipVeliocity.normalize();
            tipVeliocity = tipVeliocity.mulLocal(dist);

            Vec2 backTip = tipCorInt.sub(tipVeliocity); // punkt zwiadowca


                Vec2 ortogonalToBackTip = new Vec2(-tipVeliocity.y, tipVeliocity.x); // Obrót znormalizowano-pomnożonego wektora prędkości tipa
                ortogonalToBackTip.normalize();
                ortogonalToBackTip.mulLocal(dist / 2.0f); // wektor jest długości połowy długości przekątnej komórki

                /*
                tutaj jest rozum grzyba, do wyciecia
                 */
                float cellWidth = width/grid;
                float cellHeight = height/grid;

                float r = ((Ball)(interp)).getRadius();
                int fungusIntenectFactor = 20;  // liczba tipów w prawo i w lewo, nie w sumie
                // zalezy od tipa, ale
                float fungusIntelect = r * fungusIntenectFactor;
                int howManyCellsIsIt = (int)(Math.ceil(fungusIntelect / cellWidth));

                int intelectRange = howManyCellsIsIt; // TODO UALEŻNIĆ OD ABSOLUTNEJ WARTOSCI (teraz to jest liczba zwiadowców (po lewej i prawej, w sumie 2*n))

                Vec2[] leftPointArr = new Vec2[intelectRange]; //Tablica lewych zwiadowców
                Vec2[] rightPointArr = new Vec2[intelectRange]; //Tablica prawych zwiadowców


            try {
                for (int i = 0; i < intelectRange; i++) {
                    leftPointArr[i] = new Vec2(backTip).addLocal((ortogonalToBackTip).mul(i + 1)); //Definicja zwiadowców
                    rightPointArr[i] = new Vec2(backTip).subLocal((ortogonalToBackTip).mul(i + 1));
                }
            } catch (Exception e) {
            }

            if (tip.hasChanged(width, height, grid)) {  // Sprawdza, czy tip zmienił komórkę siatki


                try {

                if (this.hasChanged(width, height, grid)) {  // Sprawdza, czy tip zmienił komórkę siatki
                    int[] moreLeft;   // Współrzędne komórek sąsiednich w których są zwiadowcy (definicje w pętli niżej)
                    int[] moreRight; // j/w

                    Vec2 leftForce = leftPointArr[0].subLocal(backTip); // siła w lewo
                    Vec2 rightForce = rightPointArr[0].subLocal(backTip); // siłą w prawo


                    Vec2 backTipVelocity = new Vec2(world.vectorWorldToPixels(this.getBody().getLinearVelocity()));
                    float cosphi = backTipVelocity.x/backTipVelocity.length(); //nachylenie wektora prędkości backtip

//                    backTipVelocity.normalize();
                    int[] cellCoordsInInt = c2vf((int)backTip.x, (int)backTip.y, width, height, grid); //glupi output motody, zamiast Vec2 zwraca tablicę intów
                    Vec2 cellCoordsOfBackTip = new Vec2(cellCoordsInInt[0], cellCoordsInInt[1]);
                    Vec2 backTipCellCenterAbsCoords =
                            middleCords(cellCoordsInInt[0], cellCoordsInInt[1], width, height, grid);
                    Vec2 backTipRelativeToMiddlePosition = backTip.sub(backTipCellCenterAbsCoords);
//                    backTipRelativeToMiddlePosition.normalize();

                    float phi;
                    if (backTipVelocity.y > 0) {
                        phi = (float) (Math.PI / 2 - Math.acos(cosphi));
                    } else {
                        phi = (float) (Math.PI / 2 + Math.acos(cosphi));
                    }

                    Vec2 rotatedRelativeBackTipPosition = new Vec2(
                            (float)(backTipRelativeToMiddlePosition.x * Math.cos(phi) -
                                    backTipRelativeToMiddlePosition.y * Math.sin(phi)),
                            (float)(backTipRelativeToMiddlePosition.x * Math.sin(phi) +
                                    backTipRelativeToMiddlePosition.y * Math.cos(phi)));

//                    float forceFraction = world.scalarPixelsToWorld()
                    if(rotatedRelativeBackTipPosition.x >= 0) {
                        vf.standardBlock(cellCoordsInInt, leftForce, forceValue/(cellWidth*cellWidth));
                    } else {
                        vf.standardBlock(cellCoordsInInt, rightForce, forceValue/(cellWidth*cellWidth));
                    }


                    // TODO Wyznaczyć wektor siły dla komórki w której jest punkt zwiadowca
//                    int[] c = c2vf((int) backTip.x, (int) backTip.y, width, height, grid);
//                    System.out.println(intelectRange);
                    for (int i = 0; i < intelectRange; i++) {
                        moreLeft = c2vf((int) leftPointArr[i].x, (int) leftPointArr[i].y, width, height, grid); //współrzędna komórki w której jest ity
                        // lewy zwiadowca
                        moreRight = c2vf((int) rightPointArr[i].x, (int) rightPointArr[i].y, width, height, grid); // podobnie
                        float fraction = world.scalarPixelsToWorld((i+1)*(i+1)*cellHeight*cellHeight);
//                        System.out.println(fraction);
//                        vf.standardBlock(moreLeft, leftForce, forceValue / ((i+1)*(i+1))); //aktualizuje pole siłowe odpowiednio
                        vf.standardBlock(moreLeft, leftForce, forceValue/fraction); //aktualizuje pole siłowe odpowiednio
//                        vf.standardBlock(moreLeft, leftForce, forceValue/((float)Math.log(i+1)+1.0f)); //aktualizuje pole siłowe odpowiednio
//                        vf.standardBlock(moreRight, rightForce, forceValue / ((i+1)*(i+1)));
                        vf.standardBlock(moreRight, rightForce, forceValue/fraction);
//                        vf.standardBlock(moreRight, rightForce, forceValue/((float)Math.log(i+1)+1.0f));

                    }
                } catch (Exception e) {
                }
//               GRZEBIEŃ
//                int[] c = c2vf((int) backTip.x, (int)backTip.y);
//                Vec2 mid = middleCords(c[0], c[1]);
//                fill(0, 225, 0);
//                ellipse(mid.x, mid.y, 8, 8);
//
//                for (int i = 0; i < intelectRange; i++) {
//                    fill(120, 0, 0);
//                    ellipse(leftPointArr[i].x, leftPointArr[i].y, 5, 5);
//                    ellipse(rightPointArr[i].x, rightPointArr[i].y, 5, 5);
//                }

            }

        }


    }

    public Hyphae getOwner() {
        return owner;
    }
}
