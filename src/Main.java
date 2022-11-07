import processing.core.*;

public class Main extends PApplet {
    public void settings() {
        size(400, 400, P3D);
    }
    public void draw() {
        background(0);
        fill(255);
        ellipse(mouseX, mouseY, 50, 50);
    }
    public void mouseClicked() {
        println("Mouse clicked at " + mouseX + ", " + mouseY);
    }
    public static void main(String[] args) {
        PApplet.runSketch(new String[] { "Main" }, new Main());
    }
}