import processing.core.PApplet;
/**
 * Main class to execute sketch
 * @author Caleb Chang
 *
 */
class Main {
  public static void main(String[] args) {
    String[] processingArgs = {"MySketch"};
    Test Test = new Test();
    Rendering Rendering = new Rendering();
    PApplet.runSketch(processingArgs, Rendering);
  }
  
}