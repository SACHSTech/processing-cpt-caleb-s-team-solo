import processing.core.PApplet;
import java.util.Scanner;
/**
 * Main class to execute sketch
 * @author 
 *
 */
class Main {
  public static void main(String[] args) {
    Scanner s = new Scanner(System.in); 
    String[] processingArgs = {"MySketch"};
    Rendering Rendering = new Rendering();
    PApplet.runSketch(processingArgs, Rendering);
  }
  
}