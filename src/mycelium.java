import processing.core.PApplet;

public class mycelium extends PApplet {
    private static int WIDTH = 800;
    private static int HEIGHT = 600;
    private static int GRID = 8;

    LSystem lsystem;
    VectorField vf = new VectorField(GRID);

    public static void main(String[] args) {
        PApplet.main("mycelium", args);
    }
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void setup() {

    }

    public void draw() {
        background(180);
        drawGrid();
        drawVectorField(vf);
        vf.setBlock(new int[] {mouseX/(WIDTH/GRID), mouseY/(HEIGHT/GRID)}, 1);
        text(mouseX/(WIDTH/GRID) + "; " + mouseY/(HEIGHT/GRID) + "\n" , width / 2, 60);
    }

    private void drawGrid() {
        stroke(15);
        for (int x = WIDTH/GRID; x < WIDTH; x += WIDTH/GRID) {
            line(x, 0, x, HEIGHT);
        }
        for (int y = HEIGHT/GRID; y < HEIGHT; y += HEIGHT/GRID) {
            line(0, y, WIDTH, y);
        }
    }

    private void drawVectorField(VectorField vf) {
        stroke(127,0,0);
        for (int i = 0; i < vf.getBlock().length; i++) {
            for (int j = 0; j < vf.getBlock().length; j++) {
                int[] c = vf2c(i, j);
                int x1 = c[0]+(WIDTH/(2*GRID));
                int y1 = c[1]+(HEIGHT/(2*GRID));
                int x2 = x1+5;
                int y2 = y1+5;
                line(x1, y1, x2, y2);
            }
        }

    }

    private int[] c2vf(int x, int y) {
        return new int[] {x/(WIDTH/GRID), y/(HEIGHT/GRID)};
    }


    private int[] vf2c(int x, int y) {
        return new int[] {x*(WIDTH/GRID), y*(HEIGHT/GRID)};
    }

}
