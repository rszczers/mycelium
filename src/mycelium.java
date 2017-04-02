import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.LinkedList;

import Box2D.Box2DProcessing;


public class mycelium extends PApplet {
    private Box2DProcessing world;

    private final static int WIDTH = 1200;
    private final static int HEIGHT = 800;
    private final static int GRID = 20;

    private static final int HYPHAE_WIDTH = 5;
    private static final int HYPHAE_HEIGHT = 40;

    private boolean drawCells = true;
    private boolean drawGrids = true;
    private boolean drawVectorFields = true;
    private boolean drawLabels = true;
    private boolean calculatePhysics = true;
    private boolean toggleBoundaries = false;
    private boolean toggleForceField = true;
    private boolean toggleGravity = true;
    private boolean drawfps = true;

    private boolean toggleBackgroundLayer = true;
    private boolean toggleDebugLayer = true;
    private boolean toggleFungiLayer = true;
    private boolean toggleInterfaceLayer = true;

    public PGraphics backgroundLayer;
    public PGraphics debugLayer;
    public PGraphics fungiLayer;
    public PGraphics interfaceLayer;

    public static LinkedList<Tip> tipsToDelete;

    private static float[][][] cellColor = new float[GRID][GRID][3];

    /**
     * Definicje obiektów na scenie
     */
//    private ArrayList<Tip> tips = new ArrayList<>();
    private Fungus fungi;
    private ArrayList<BoundaryBox> boundaries = new ArrayList<>();
    private VectorField vf = new VectorField(GRID);
    private ArrayList<Tip> tcoll;


    public static void main(String[] args) {
        PApplet.main("mycelium", args);
    }

    public void settings() {
        size(WIDTH, HEIGHT, P3D);
    }

    public void setup() {
        frameRate(60);
        background(127);
        tipsToDelete = new LinkedList<>();

        backgroundLayer = createGraphics(WIDTH, HEIGHT);
        debugLayer = createGraphics(WIDTH, HEIGHT);
        fungiLayer = createGraphics(WIDTH, HEIGHT);
        interfaceLayer = createGraphics(WIDTH, HEIGHT);

        world = new Box2DProcessing(this, 10);
        world.createWorld();
        if (toggleGravity) {
            world.setGravity(0, 10);
        } else {
            world.setGravity(0, 0);
        }

        world.listenForCollisions();

        // określ kolory kwadratów w których pole jest takie samo.
        if (drawCells) {
            for (int i = 0; i < GRID; i++) {
                for (int j = 0; j < GRID; j++) {
                    for (int k = 0; k < 3; k++) {
                        cellColor[i][j][k] = 100 + vf.getBlock()[i][j].length();
                    }
                }
            }
        }

        // ograniczenia
        if (toggleBoundaries) {
            boundaries.add(new BoundaryBox(world, this,
                    WIDTH / 2, 5, WIDTH, 10));
            boundaries.add(new BoundaryBox(world, this,
                    WIDTH / 2, HEIGHT - 5, WIDTH, 10));
            boundaries.add(new BoundaryBox(world, this,
                    5, HEIGHT / 2, 10, HEIGHT));
            boundaries.add(new BoundaryBox(world, this,
                    WIDTH - 5, HEIGHT / 2, 10, HEIGHT));
        }


        fungi = new Fungus(world, this, new Vec2(width / 2, height / 2));
    }

    public void draw() {
        if (calculatePhysics)
            world.step();
        /**
         * Apply force to tip
         */
        tcoll = fungi.getTips();
        for (int i = 0; i < tcoll.size(); i++) {
            if (toggleForceField) {
                try {
                    Tip t = tcoll.get(i);
                    Vec2 coords = world.coordWorldToPixels(t.getBody().getPosition());
                    float[] bp = {coords.x, coords.y};
                    int[] xy = c2vf((int) bp[0], (int) bp[1]);
                    t.applyForce(vf.getBlock()[xy[0]][xy[1]]); // !!!
                } catch (ArrayIndexOutOfBoundsException e) {
                    tcoll.get(i).killBody(); // usuń obiekt z systemu fizycznego
                    tcoll.remove(i); //wyrzucanie obiektów, które wyleciały poza scenę
                }
            }
        }

        /**
         * Background shader
         */
        if(toggleBackgroundLayer) {
            backgroundLayer.beginDraw();
            background(250);
            backgroundLayer.endDraw();
        }

        /**
         * Debug screen
         */
        if(toggleDebugLayer) {
            debugLayer.beginDraw();
            if (drawCells)
                drawCells();
            if (drawGrids)
                drawGrid();
            if (drawVectorFields)
                drawVectorField(vf);

            for (Tip t :
                    tcoll) {
                t.display();
            }

            if (toggleBoundaries) {
                for (BoundaryBox t :
                        boundaries) {
                    t.display();
                }
            }
            debugLayer.endDraw();
        }

        /**
         * Interface screen
         */
        if(toggleInterfaceLayer) {
            interfaceLayer.beginDraw();
            if (drawLabels) {
                fill(16, 16, 153);
                textSize(32);
                text(mouseX / (WIDTH / GRID) + "; " + mouseY / (HEIGHT / GRID) + "\n", width / 2, 60);
            }
            if (drawfps) {
                fill(0, 255, 0);
                text(frameRate, 10, 60);
            }
            interfaceLayer.endDraw();
        }

        /**
         * Fungus screen
         */
        if (toggleFungiLayer) {
            fungiLayer.beginDraw();
            fungi.grow(HYPHAE_WIDTH, HYPHAE_HEIGHT);
            fungi.display(toggleFungiLayer);
            fungiLayer.endDraw();
        }

        if(toggleBackgroundLayer)
            image(backgroundLayer, 0, 0);
        if(toggleDebugLayer)
            image(debugLayer, 0, 0);
        if(toggleFungiLayer)
            image(fungiLayer, 0, 0);
        if(toggleInterfaceLayer)
            image(interfaceLayer, 0, 0);
    }


    /**
     * Dodawanie obiektów przez kliknięcie lewym przyciskiem myszki
     */
    public void mouseClicked() {
        fungi.addRoot(new Vec2(mouseX, mouseY), HYPHAE_WIDTH);
    }


    public void beginContact(Contact c) {

        Fixture f1 = c.getFixtureA();
        Fixture f2 = c.getFixtureB();

        Body b1 = f1.getBody();
        Body b2 = f2.getBody();

        Object object1 = b1.getUserData();
        Object object2 = b2.getUserData();

        /**
         * Kolizja Tip -- CollisionShape
         **/
        if (object1.getClass() == Tip.class && object2.getClass() == CollisionShape.class) {
            Tip tip = (Tip) object1;
            CollisionShape collisionShape = (CollisionShape) object2;
            Hyphae tipOwner = tip.getOwner();
            tipOwner.collisionWithHyphae(collisionShape, HYPHAE_WIDTH, HYPHAE_HEIGHT);
        }

        /**
         * Kolizja CollisionShape -- Tip
         **/
        if (object1.getClass() == CollisionShape.class && object2.getClass() == Tip.class) {
            CollisionShape collisionShape = (CollisionShape) object1;
            Tip tip = (Tip) object2;
            Hyphae tipOwner = tip.getOwner();
            tipOwner.collisionWithHyphae(collisionShape, HYPHAE_WIDTH, HYPHAE_HEIGHT);
        }

        /**
         * Kolizja Tip -- Tip
         */
        if (object1.getClass() == Tip.class && object2.getClass() == CollisionShape.class) {
            Tip tip1 = (Tip) object1;
            CollisionShape tip2 = (CollisionShape) object2;
            System.out.println("Tip-Tip");
        }

        /**
         * Kolizja Tip -- Boundary
         */
        if (object1.getClass() == Tip.class && object2.getClass() == BoundaryBox.class) {
            Tip tip = (Tip) object1;
            tip.getOwner().killHyphae();
        }
        /**
         * Kolizja Boundary -- Tip
         */
        if (object2.getClass() == Tip.class && object1.getClass() == BoundaryBox.class) {
            Tip tip = (Tip) object2;
            tip.getOwner().killHyphae();
        }
    }

    /**
     * Mało mądra metoda do rysowania siatki
     */
    private void drawGrid() {
        stroke(90);
        strokeWeight(1);
        for (int x = WIDTH / GRID; x < WIDTH; x += WIDTH / GRID) {
            line(x, 0, x, HEIGHT);
        }
        for (int y = HEIGHT / GRID; y < HEIGHT; y += HEIGHT / GRID) {
            line(0, y, WIDTH, y);
        }
    }

    /**
     * Rysuje wektory pola wektorowego
     *
     * @param vf
     */
    private void drawVectorField(VectorField vf) {
        stroke(1, 0, 0);
        for (int i = 0; i < vf.getBlock().length; i++) {
            for (int j = 0; j < vf.getBlock().length; j++) {
                int[] c = vf2c(i, j);
                int x1 = c[0] + (WIDTH / (2 * GRID));
                int y1 = c[1] + (HEIGHT / (2 * GRID));
                int x2 = x1 + (int) (vf.getBlock()[i][j].x);
                int y2 = y1 + (int) (vf.getBlock()[i][j].y);
                line(x1, y1, x2, y2);

                PVector px2 = new PVector(x2, y2);
                PVector px1 = new PVector(x1, y1);
                PVector cx1 = px1.sub(px2).normalize().rotate(radians(15)).mult(10).add(px2);
                line(x2, y2, cx1.x, cx1.y);
                PVector cx2 = px1.sub(px2).rotate(radians(-30)).add(px2);
                line(x2, y2, cx2.x, cx2.y);

            }
        }
    }

    /**
     * Zaznacza obszary, w których pole wektorowe zdefiniowane jest tak samo
     */
    private void drawCells() {
        rectMode(CORNER);
        int dw = WIDTH / GRID;
        int dh = HEIGHT / GRID;
        for (int x = 0; x < WIDTH; x += dw) {
            for (int y = 0; y < HEIGHT; y += dh) {
                strokeWeight(1);
                stroke(1);
                fill(cellColor[x / dw][y / dh][0], cellColor[x / dw][y / dh][1], cellColor[x / dw][y / dh][2]);
                rect(x, y, WIDTH / GRID, HEIGHT / GRID);
            }
        }
    }

    /**
     * Zamienia współrzędne ekranu na współrzędne tablicy komórek
     *
     * @param x
     * @param y
     * @return
     */
    private int[] c2vf(int x, int y) {
        return new int[]{x / (WIDTH / GRID), y / (HEIGHT / GRID)};
    }

    /**
     * Zamienia współrzędne tablicy komórek na współrzędne piksela lewego górnego rogu odpowiedniej komórki
     *
     * @param x
     * @param y
     * @return
     */
    private int[] vf2c(int x, int y) {
        return new int[]{x * (WIDTH / GRID), y * (HEIGHT / GRID)};
    }

    public static void addTipToDelete(Tip tip) {
        tipsToDelete.add(tip);
    }
}
