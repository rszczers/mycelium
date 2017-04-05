import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import processing.core.*;

import java.util.ArrayList;
import java.util.LinkedList;

import Box2D.Box2DProcessing;
import processing.opengl.PShader;

public class mycelium extends PApplet {
    private Box2DProcessing world;

    private final static int WIDTH = 800;
    private final static int HEIGHT = 800;
    private static int GRID = 100;
    private static int FUNGUS_BRAIN_SIZE = 20;

    private static final int HYPHAE_WIDTH = 4;
    private static final int HYPHAE_HEIGHT = 10;
    public static final float FORCE_VALUE = 10.0f;
    private static final float GRAVITY_VALUE = 10.0f;
//
    private boolean drawCells = true;
    private boolean drawGrids = true;
    private boolean drawVectorFields = true;
    private boolean drawLabels = true;
    private boolean calculatePhysics = true;
    private boolean toggleBoundaries = false;
    private boolean toggleForceField = true;
    private boolean toggleGravity = false;
    private boolean toggleFps = true;
    private boolean toggleFullscreen = false;
    private boolean drawTips = true;

    private boolean toggleBackgroundLayer = true;
    private boolean toggleDebugLayer = false;
    private boolean toggleFungiLayer = false;
    private boolean toggleInterfaceLayer = false;

    public PGraphics backgroundLayer;
    public PImage backbuffer;
    public PImage flashbuffer;
    public PImage backgroundBuffer;
    public PGraphics debugLayer;
    public PGraphics fungiLayer;
    public PGraphics fungiFlashLayer;
    public PGraphics interfaceLayer;

    private PShader fungiShader;
    private PShader fungiFlashShader;
    private PShader backgroundShader;

    public static LinkedList<Tip> tipsToDelete;

    private static float[][][] cellColor;

    boolean[] lastState = new boolean[4]; // ostatni uruchomiony ekran
    /**
     * Definicje obiektów na scenie
     */
//    private ArrayList<Tip> tips = new ArrayList<>();
    private Fungus fungi;
    private ArrayList<BoundaryBox> boundaries = new ArrayList<>();
    private VectorField vf;
    private ArrayList<Tip> tcoll;

    public static void main(String[] args) {
        PApplet.main("mycelium", args);
    }

    public void settings() {
        size(WIDTH, HEIGHT, P2D);
    }

    public void setup() {
        frameRate(60);
        background(0);
        surface.setResizable(true);

        fungiShader = loadShader("fungusFrag.glsl");
        backgroundShader = loadShader("backgroundFrag.glsl");
        fungiFlashShader = loadShader("flash.glsl");

        tipsToDelete = new LinkedList<>();

        backgroundLayer = createGraphics(width, height, P2D);
        debugLayer = createGraphics(width, height, P2D);
        fungiLayer = createGraphics(width, height, P2D);
        fungiFlashLayer = createGraphics(width, height, P2D);
        interfaceLayer = createGraphics(width, height, P2D);

        backbuffer = new PImage(width, height);
        flashbuffer = new PImage(width, height);
        backgroundBuffer = new PImage(width, height);
        cellColor = new float[GRID][GRID][3];
        world = new Box2DProcessing(this, 10);
        world.createWorld();

        vf = new VectorField(GRID, GRAVITY_VALUE);

        if (toggleGravity) {
            world.setGravity(0, 200);
        } else {
            world.setGravity(0, 0);
        }

        world.listenForCollisions();

        // ograniczenia
        if (toggleBoundaries) {
            boundaries.add(new BoundaryBox(world, this,
                    width / 2, 5, width, 10));
            boundaries.add(new BoundaryBox(world, this,
                    width / 2, height - 5, width, 10));
            boundaries.add(new BoundaryBox(world, this,
                    5, height / 2, 10, height));
            boundaries.add(new BoundaryBox(world, this,
                    width - 5, height / 2, 10, height));
        }

        fungi = new Fungus(world, this, new Vec2(width / 2, height / 2));
    }

    public void draw() {
        if (calculatePhysics)
            world.step();

        /**
         * Debug screen
         */
        if (toggleDebugLayer) {
            debugLayer.beginDraw();
            debugLayer.background(160);
            if (drawCells)
                drawCells();
            if (drawGrids)
                drawGrid();
            if (drawVectorFields) {
                drawVectorField(vf);
            }
            if (drawTips) {
                for (int i = 0; i < tcoll.size(); i++) {
                    tcoll.get(i).display(debugLayer);
                }
            }

            if (toggleBoundaries) {
                for (BoundaryBox t :
                        boundaries) {
                    t.display(debugLayer);
                }
            }

            // określ kolory kwadratów w których pole jest takie samo.
            if (drawCells) {
                for (int i = 0; i < GRID; i++) {
                    for (int j = 0; j < GRID; j++) {
                        for (int k = 0; k < 3; k++) {
                            cellColor[i][j][0] = vf.getBlock()[i][j].length();
                            cellColor[i][j][1] = 60;
                            cellColor[i][j][2] = 60;
                        }
                    }
                }
            }

            fungi.display(toggleDebugLayer, debugLayer);
            debugLayer.endDraw();
        }

//        for (Tip t :
//                tipsToDelete) {
//            t.killBody();
//        }

        fungi.grow(HYPHAE_WIDTH, HYPHAE_HEIGHT);

        /**
         * Apply force to tip
         */
        tcoll = fungi.getTips();
        for (int i = 0; i < tcoll.size(); i++) {
            if (toggleForceField) {
                try {
                    Tip t = tcoll.get(i);
                    Vec2 coords = world.coordWorldToPixels(t.getBody().getPosition());
                    int[] xy = c2vf((int) coords.x, (int) coords.y);
                    t.applyForce(vf.getBlock()[xy[0]][xy[1]]); // !!!
                    t.makeHypheField(FUNGUS_BRAIN_SIZE, WIDTH, HEIGHT, GRID, vf, FORCE_VALUE, debugLayer);
                } catch (ArrayIndexOutOfBoundsException e) {
                    tcoll.get(i).killBody(); // usuń obiekt z systemu fizycznego
                    tcoll.remove(i); //wyrzucanie obiektów, które wyleciały poza scenę
                }
            }
        }

        if (!toggleDebugLayer) {
            /**
             * Fungus screen
             */
            fungiLayer.beginDraw();
            fungiLayer.background(0);
            ArrayList<Vec2> tipsToDisplay = new ArrayList<>();
            for (int i = 0; i < tcoll.size(); i++) {
//                if(tcoll.get(i).isVisible())
                tipsToDisplay.add(world.coordWorldToPixels(tcoll.get(i).getBody().getPosition()));
            }
            fungiShader.set("u_posSize", tipsToDisplay.size());
            fungiShader.set("u_resolution", (float) width, (float) height);
            fungiShader.set("u_buf", backbuffer);
            for (int i = 0; i < tipsToDisplay.size(); i++) {
                fungiShader.set("u_positions[" + i + "]", (float) (tipsToDisplay.get(i).x), (float) height - tipsToDisplay.get(i).y);
            }
            fungiLayer.shader(fungiShader);
            fungiLayer.rect(0, 0, width, height);
            backbuffer = fungiLayer.get();
            fungiLayer.resetShader();
            fungiLayer.endDraw();

            /**
             * Fungus flash screen
             */
            fungiFlashLayer.beginDraw();
            fungiFlashLayer.background(0);
            fungiFlashShader.set("u_posSize", tipsToDisplay.size());
            fungiFlashShader.set("u_resolution", (float) width, (float) height);
            fungiFlashShader.set("u_buf", flashbuffer);
            for (int i = 0; i < tipsToDisplay.size(); i++) {
                fungiFlashShader.set("u_positions[" + i + "]", (float) (tipsToDisplay.get(i).x), (float) height - tipsToDisplay.get(i).y);
            }
            fungiFlashLayer.shader(fungiFlashShader);
            fungiFlashLayer.rect(0, 0, width, height);
            flashbuffer = fungiFlashLayer.get();
            fungiFlashLayer.resetShader();
            fungiFlashLayer.endDraw();

            /**
             * Background shader
             */
            if (toggleBackgroundLayer) {
                backgroundLayer.beginDraw();
                backgroundShader.set("u_resolution", (float) width, (float) height);
                backgroundShader.set("u_time", millis() / 1000.0f);
                backgroundShader.set("u_backbuffer", backbuffer);
                backgroundShader.set("u_flashbuffer", flashbuffer);
                backgroundLayer.shader(backgroundShader);
                backgroundLayer.rect(0, 0, width, height);
                backgroundLayer.resetShader();
                backgroundLayer.endDraw();
            }
        }


        /**
         * Interface screen
         */
        if (toggleInterfaceLayer) {
            interfaceLayer.beginDraw();
            if (drawLabels) {
                interfaceLayer.rectMode(CENTER);
                interfaceLayer.noStroke();
                interfaceLayer.fill(0, 255, 0, 255);
                interfaceLayer.rect(width / 2+50, 50, 115, 50);
                interfaceLayer.rectMode(CORNER);

                interfaceLayer.fill(16, 16, 153);
                interfaceLayer.textSize(32);
                interfaceLayer.text(mouseX / (width / GRID) + "; " + mouseY / (height / GRID) + "\n", width / 2, 60);
            }
            if (toggleFps) {
                interfaceLayer.fill(0, 0, 180, 255);
                interfaceLayer.rect(10, 30, 55, 40);
                interfaceLayer.fill(0, 255, 0);
                interfaceLayer.text((int)frameRate, 10, 60);
            }
            interfaceLayer.endDraw();
        }

        if (toggleBackgroundLayer)
            image(backgroundLayer, 0, 0);
        else if (toggleFungiLayer)
            image(fungiLayer, 0, 0);
        else if (toggleDebugLayer)
            image(debugLayer, 0, 0);

        if (toggleInterfaceLayer) {
            image(interfaceLayer, 0, 0);
        }
    }

    private void saveState() {
        lastState[0] = toggleBackgroundLayer;
        lastState[1] = toggleFungiLayer;
        lastState[2] = toggleDebugLayer;
        lastState[3] = toggleInterfaceLayer;
    }


    private void resetState() {
        toggleBackgroundLayer = lastState[0];
        toggleFungiLayer = lastState[1];
        toggleDebugLayer = lastState[2];
        toggleInterfaceLayer = lastState[3];
    }

    /**
     * Dodawanie obiektów przez kliknięcie lewym przyciskiem myszki
     */
    public void mouseClicked() {
        fungi.addRoot(new Vec2(mouseX, mouseY), HYPHAE_WIDTH);
    }

    public void keyPressed() {
        if (key == 'd' || key == 'D') {
            if (toggleDebugLayer == false) {
                saveState();
                toggleDebugLayer = true;
                toggleFungiLayer = false;
                toggleBackgroundLayer = false;
            } else {
                resetState();
            }
            String tmp = toggleDebugLayer ? "on" : "off";
            System.out.println("Debug mode " + tmp);
        }
        if (key == 'f' || key == 'F') {
            if (toggleFungiLayer == false) {
                saveState();
                toggleFungiLayer = true;
                toggleBackgroundLayer = false;
                toggleDebugLayer = false;
            } else {
                resetState();
            }
            String tmp = toggleFungiLayer ? "on" : "off";
            System.out.println("Fungi layer " + tmp);
        }
        if (key == 'i' || key == 'I') {
            toggleInterfaceLayer = !toggleInterfaceLayer;
            String tmp = toggleInterfaceLayer ? "on" : "off";
            System.out.println("Interface " + tmp);
        }
        if (key == 'b' || key == 'B') {
            if (toggleBackgroundLayer == false) {
                saveState();
                toggleBackgroundLayer = true;
                toggleFungiLayer = false;
                toggleDebugLayer = false;
            } else {
                resetState();
            }
            String tmp = toggleBackgroundLayer ? "on" : "off";
            System.out.println("Background " + tmp);
        }
        if (toggleDebugLayer == true) {
            if (key == 'v' || key == 'V') {
                drawVectorFields = !drawVectorFields;
                System.out.println("VF switched.");;
            }
            if (key == 'c' || key == 'C') {
                drawCells = !drawCells;
                System.out.println("Cells switched.");
            }
            if (key == 'g' || key == 'G') {
                drawGrids = !drawGrids;
                System.out.println("Grid switched.");
            }
            if (key == 's' || key == 'S') {
                drawTips = !drawTips;
                System.out.println("Tips switched.");
            }
            if (key == '1') {
                try {
                    noLoop();
                    this.GRID = 20;
                    reset();
                    loop();
                } catch (Exception e) {}
            }
            if (key == '2') {
                try {
                    noLoop();
                    this.GRID = 40;
                    reset();
                    loop();
                } catch (Exception e) {}
            }
            if (key == '3') {
                try {
                    noLoop();
                    this.GRID = 100;
                    reset();
                    loop();
                } catch (Exception e) {}
            }
            if (key == '4') {
                try {
                    noLoop();
                    this.GRID = 100;
                    reset();
                    loop();
                } catch (Exception e) {}
            }
            if (key == '5') {
                try {
                    noLoop();
                    this.GRID = 400;
                    reset();
                    loop();
                } catch (Exception e) {}
            }
            if (key == '6') {
                try {
                    noLoop();
                    this.GRID = 800;
                    reset();
                    loop();
                } catch (Exception e) {}
            }
        }
        if (key == 'q' || key == 'Q') {
            noLoop();
            exit();
        }
        if (key == 'r' || key == 'R') {
            reset();
        }
        if (key == ENTER || key == RETURN) {
            if (toggleFullscreen == false) {
                reset();
                surface.setSize(displayWidth, displayHeight);
                toggleFullscreen = true;
            } else {
                reset();
                surface.setSize(WIDTH, HEIGHT);
                toggleFullscreen = false;
            }
        }
    }

    private void reset() {
        setup();
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
            if (tipOwner.getIsGrowing()) {
                tipOwner.collisionWithHyphae(collisionShape, HYPHAE_WIDTH, HYPHAE_HEIGHT);
            }
        }

        /**
         * Kolizja CollisionShape -- Tip
         **/
        if (object1.getClass() == CollisionShape.class && object2.getClass() == Tip.class) {
            CollisionShape collisionShape = (CollisionShape) object1;
            Tip tip = (Tip) object2;
            Hyphae tipOwner = tip.getOwner();
            if (tipOwner.getIsGrowing()) {
                tipOwner.collisionWithHyphae(collisionShape, HYPHAE_WIDTH, HYPHAE_HEIGHT);
            }
        }

        /**
         * Kolizja Tip -- Tip
         */
        if (object1.getClass() == Tip.class && object2.getClass() == Tip.class) {
            Tip tip1 = (Tip) object1;
            Tip tip2 = (Tip) object2;
            tip1.colideWithOtherTip(tip2);
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

    public void endContact(Contact c) {}

    /**
     * Mało mądra metoda do rysowania siatki
     */
    private void drawGrid() {
        debugLayer.stroke(90);
        debugLayer.strokeWeight(1);
        for (int x = width / GRID; x < width; x += width / GRID) {
            debugLayer.line(x, 0, x, height);
        }
        for (int y = height / GRID; y < height; y += height / GRID) {
            debugLayer.line(0, y, width, y);
        }
        debugLayer.noStroke();
    }

    /**
     * Rysuje wektory pola wektorowego
     *
     * @param vf
     */
    private void drawVectorField(VectorField vf) {
        debugLayer.stroke(20, 20, 20);
        debugLayer.strokeWeight(1);
        for (int i = 0; i < vf.getBlock().length; i++) {
            for (int j = 0; j < vf.getBlock().length; j++) {
//                Vec2 toShow = world.vectorWorldToPixels(vf.getBlock()[i][j]);
                Vec2 toShow = new Vec2(vf.getBlock()[i][j]);
                toShow.normalize();
                toShow.mulLocal(15.0f);
                int[] c = vf2c(i, j);
                int x1 = c[0] + (width / (2 * GRID));
                int y1 = c[1] + (height / (2 * GRID));
                int x2 = x1 + (int) (toShow.x);
                int y2 = y1 + (int) (toShow.y);
                debugLayer.line(x1, y1, x2, y2);
                PVector px2 = new PVector(x2, y2);
                PVector px1 = new PVector(x1, y1);
                PVector cx1 = px1.sub(px2).normalize().rotate(radians(15)).mult(10).add(px2);
                debugLayer.line(x2, y2, cx1.x, cx1.y);
                PVector cx2 = px1.sub(px2).rotate(radians(-30)).add(px2);
                debugLayer.line(x2, y2, cx2.x, cx2.y);
            }
        }
        debugLayer.noStroke();
    }

    /**
     * Zaznacza obszary, w których pole wektorowe zdefiniowane jest tak samo
     */
    private void drawCells() {
        debugLayer.rectMode(CORNER);
        int dw = width / GRID;
        int dh = height / GRID;
        debugLayer.strokeWeight(0);
//        stroke(255, 0, 0);
        for (int x = 0; x < width; x += dw) {
            for (int y = 0; y < height; y += dh) {
                debugLayer.fill(cellColor[x / dw][y / dh][0], cellColor[x / dw][y / dh][1], cellColor[x / dw][y / dh][2]);
                debugLayer.rect(x, y, width / GRID, height / GRID);
            }
        }
        debugLayer.noStroke();
    }

    /**
     * Zamienia współrzędne ekranu na współrzędne tablicy komórek
     *
     * @param x
     * @param y
     * @return
     */
    private int[] c2vf(int x, int y) {
        return new int[]{x / (width / GRID), y / (height / GRID)};
    }

    /**
     * Zamienia współrzędne tablicy komórek na współrzędne piksela lewego górnego rogu odpowiedniej komórki
     *
     * @param x
     * @param y
     * @return
     */
    private int[] vf2c(int x, int y) {
        return new int[]{x * (width / GRID), y * (height / GRID)};
    }

    /**
     * Zwraca współrzędne środka komórki o podanych indeksach
     *
     * @param x
     * @param y
     * @return
     */
    private Vec2 middleCords(int x, int y) {
        int[] leftTopCords = vf2c(x, y);
        Vec2 middleCords = new Vec2(leftTopCords[0], leftTopCords[1]);
        Vec2 midC = middleCords.add((new Vec2(width / GRID, height / GRID)).mul(0.5f));
        return midC;
    }

    public static void addTipToDelete(Tip tip) {
        tipsToDelete.add(tip);
    }
}

