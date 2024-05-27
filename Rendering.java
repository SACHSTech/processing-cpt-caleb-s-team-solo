import processing.core.PApplet;
import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.List;   


/* 
 *  
 * The purpose of this program is to use 2d processing to make a 3d game
 * No 3d methods are used throughout the project.
 * Nearly every single method in here took at least a week of work + tweaking after it was finished to make...
 * 
 * Author: @duocaleb
*/
public class Rendering extends PApplet {
  //initalizing the variables and the arrays
  double dblFocalLength = 500;
  int intScreenSize = 800;
  int scDiv2 = intScreenSize/2; // used this so much that i might as well make it a variable
  double cameraX = 0;
  double cameraY = 0;
  double cameraZ = 0;
  double mouseRotationX = 0;
  double mouseRotationY = 0;
  double mouseCenteredX = 0;
  double mouseCenteredY = 0;
  // The amount of degrees it turns per half screen of distance the mouse is moved.
  double mouseSensitivity = 180;
  boolean[] isKeyPressed = new boolean[255];
  int x = 100;
  int y = 100;
  List<Triangle3D> TriangleList3D = new ArrayList<>();
  List<Triangle2D> TriangleList2D = new ArrayList<>();
  float[] zBuffer = new float[intScreenSize*intScreenSize];
  Robot mouseMover;

  public void settings() {
    size(intScreenSize, intScreenSize);
  }

  public void setup() {
    background(45, 150, 207);

    try {
      mouseMover = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
    noCursor();
    mouseMover.mouseMove(displayWidth/2, displayHeight/2);
  }
  int rotate = 0;
  public void draw() {
    TriangleList2D.clear();
    TriangleList3D.clear();
    for(int x = 0; x < zBuffer.length; x++){
      zBuffer[x] =  Float.POSITIVE_INFINITY;
    }
    clear();
    background(45, 150, 207);
    cursorMovement();
    moveChar();
    addCube(new Point3D(-100, -100, 200), 
            new Point3D(100, 100, 400), 
            color(0,0,255),
            new Point3D(0,0,0));
    drawFaces();
    rotate++;
    // projectPoints();
    drawLines(); // For testing purposes only
  }



  public void rotateScene(){
    for(int x = 0; x < TriangleList3D.size(); x++){
      rotatePoint(TriangleList3D.get(x).p1,-mouseRotationX,0,-mouseRotationY);
      rotatePoint(TriangleList3D.get(x).p2,-mouseRotationX,0,-mouseRotationY);
      rotatePoint(TriangleList3D.get(x).p3,-mouseRotationX,0,-mouseRotationY);
    }
  }

  public void increaseX(){

  }

  public void drawLines(){
    float a = scDiv2;
    for(int x = 0; x < TriangleList2D.size(); x++){
      float x1 = a+(float)TriangleList2D.get(x).p1.x;
      float x2 = a+(float)TriangleList2D.get(x).p2.x;
      float x3 = a+(float)TriangleList2D.get(x).p3.x;
      float y1 = a-(float)TriangleList2D.get(x).p1.y;
      float y2 = a-(float)TriangleList2D.get(x).p2.y;
      float y3 = a-(float)TriangleList2D.get(x).p3.y;
      line(x1,y1,x2,y2);
      line(x2,y2,x3,y3);
      line(x3,y3,x1,y1);
    }
  }

  public void addCube(Point3D minPoint, Point3D maxPoint, int colour1, int colour2, int colour3, int colour4, int colour5, int colour6, Point3D Rotation){
    Point3D[] PointList = new Point3D[8];
    int[] colourList = new int[6];
    for(int x = 0; x < 8; x++){
      // Defining a new point cause it doesnt work otherwise
      PointList[x] = new Point3D(0,0,0);
    }
    int[][] Connections = {{0,1,2}, {1,2,3}, {2,3,7}, {2,6,7}, {1,3,7}, {1,5,7}, {4,5,7}, {4,6,7}, {0,4,6}, {0,2,6}, {0,1,5}, {0,4,5}};
    colourList[0] = colour1;
    colourList[1] = colour2;
    colourList[2] = colour3;
    colourList[3] = colour4;
    colourList[4] = colour5;
    colourList[5] = colour6;
    double minX = minPoint.x;
    double minY = minPoint.y;
    double minZ = minPoint.z;
    double maxX = maxPoint.x;
    double maxY = maxPoint.y;
    double maxZ = maxPoint.z;
    double averageX = 0;
    double averageY = 0;
    double averageZ = 0;

    for(int x = 0; x < 4; x += 1){
      PointList[x*2].x = minX;
      PointList[x*2+1].x = maxX;
      PointList[(int)Math.floor(x/2)*2+x].y = minY;
      PointList[(int)Math.floor(x/2)*2+x+2].y = maxY;
      PointList[x].z = minZ;
      PointList[x+4].z = maxZ;
    }
    for(int x = 0; x < 8; x++){
      rotatePoint(PointList[x], -mouseRotationX, 0, -mouseRotationY);
      averageX += PointList[x].x;
      averageY += PointList[x].y;
      averageZ += PointList[x].z;
    }
    averageX /= 8;
    averageY /= 8;
    averageZ /= 8;
    for(int l = 0; l < 8; l++){
      rotateAroundPoint(new Point3D(averageX, averageY, averageZ), PointList[l], new Point3D(Rotation.x, Rotation.y, Rotation.z));
    }
    for(int a = 0; a < 12; a++){
      Point3D p1 = PointList[Connections[a][0]];
      Point3D p2 = PointList[Connections[a][1]];
      Point3D p3 = PointList[Connections[a][2]];
      TriangleList3D.add(new Triangle3D(p1,p2,p3,colourList[(int)a/2]));
    }
  }


  public void addCube(Point3D minPoint, Point3D maxPoint, int colour, Point3D Rotation){
    Point3D[] PointList = new Point3D[8];
    for(int x = 0; x < 8; x++){
      // Defining a new point cause it doesnt work otherwise
      PointList[x] = new Point3D(0,0,0);
    }
    int[][] Connections = {{0,1,2}, {1,2,3}, {2,3,7}, {2,6,7}, {1,3,7}, {1,5,7}, {4,5,7}, {4,6,7}, {0,4,6}, {0,2,6}, {0,1,5}, {0,4,5}};
    double minX = minPoint.x;
    double minY = minPoint.y;
    double minZ = minPoint.z;
    double maxX = maxPoint.x;
    double maxY = maxPoint.y;
    double maxZ = maxPoint.z;
    double averageX = 0;
    double averageY = 0;
    double averageZ = 0;

    for(int x = 0; x < 4; x += 1){
      PointList[x*2].x = minX;
      PointList[x*2+1].x = maxX;
      PointList[(int)Math.floor(x/2)*2+x].y = minY;
      PointList[(int)Math.floor(x/2)*2+x+2].y = maxY;
      PointList[x].z = minZ;
      PointList[x+4].z = maxZ;
    }
    for(int x = 0; x < 8; x++){
      rotatePoint(PointList[x], -mouseRotationX, 0, -mouseRotationY);
      averageX += PointList[x].x;
      averageY += PointList[x].y;
      averageZ += PointList[x].z;
    }
    averageX /= 8;
    averageY /= 8;
    averageZ /= 8;
    for(int l = 0; l < 8; l++){
      rotateAroundPoint(new Point3D(averageX, averageY, averageZ), PointList[l], new Point3D(Rotation.x, Rotation.y, Rotation.z));
    }
    for(int a = 0; a < 12; a++){
      Point3D p1 = PointList[Connections[a][0]];
      Point3D p2 = PointList[Connections[a][1]];
      Point3D p3 = PointList[Connections[a][2]];
      TriangleList3D.add(new Triangle3D(p1,p2,p3,colour));
    }
  }

  public void cursorMovement(){
    if(mouseCenteredX == 0 && mouseCenteredY == 0){
      mouseCenteredX = mouseX;
      mouseCenteredY = mouseY;
    }
    mouseRotationX += (mouseSensitivity*(mouseX - mouseCenteredX))/(scDiv2);
    mouseRotationY += (mouseSensitivity*(mouseY - mouseCenteredY))/(scDiv2);
    if(mouseRotationX >= 360){
      mouseRotationX %= -360;
    }
    if(mouseRotationX <= -360){
      mouseRotationX %= 360;
    }
    if(mouseRotationY >= 90){
      mouseRotationY = 90;
    }
    if(mouseRotationY <= -90){
      mouseRotationY = -90;
    }
    mouseMover.mouseMove(displayWidth/2, displayHeight/2);
  }

  public void keyPressed() {
    if(key != 65535){
      isKeyPressed[(int)key] = true;
    }
  }
  
  public void keyReleased() {
    if(key != 65535){
      isKeyPressed[(int)key] = false;
    }
  }

  public void moveChar() {
    double rotA = Math.toRadians(mouseRotationY);
    double rotB = Math.toRadians(mouseRotationX);
    double a = Math.toRadians(90);
    if(isKeyPressed[(int)'w']){
      cameraZ += 2*Math.cos(rotA)*Math.cos(rotB);
      cameraX += 2*Math.sin(rotA+a)*Math.cos(rotB+a);
      cameraY += 2*Math.sin(rotA);
    }
    if(isKeyPressed[(int)'s']){
      cameraZ -= 2*Math.cos(rotA)*Math.cos(rotB);
      cameraX -= 2*Math.sin(rotA+a)*Math.cos(rotB+a);
      cameraY -= 2*Math.sin(rotA);
    }
    if(isKeyPressed[(int)'a']){
      cameraZ -= 2*Math.cos(rotA)*Math.cos(rotB+a);
      cameraZ -= 2*Math.sin(rotA)*Math.cos(rotB+a); //Dont ask why i need it twice, it doesnt work if i combine it
      cameraX += 2*Math.sin(rotA+a)*Math.cos(rotB);
      cameraX += 2*Math.sin(rotA)*Math.cos(rotB);
    }
    if(isKeyPressed[(int)'d']){
      cameraZ += 2*Math.cos(rotA)*Math.cos(rotB+a);
      cameraZ += 2*Math.sin(rotA)*Math.cos(rotB+a);
      cameraX -= 2*Math.sin(rotA+a)*Math.cos(rotB);
      cameraX -= 2*Math.sin(rotA)*Math.cos(rotB);
    }
    
  }

  public Point3D findPoI(Point3D p1, Point3D p2, double m, double b, double a, int[] order){
    // head hurts, never touching this function ever again

    double[] coords = new double[3];
    double[][] coordsList = new double[3][2];
    for(int x = 0; x < 3; x++){
      if(order[x] == 0){
        coordsList[x][0] = p1.x;
        coordsList[x][1] = p2.x;
      }
      else if(order[x] == 1){
        coordsList[x][0] = p1.y;
        coordsList[x][1] = p2.y;
      }
      else if(order[x] == 2){
        coordsList[x][0] = p1.z;
        coordsList[x][1] = p2.z;
      }
    }

    // Equation made by me strugling way too much stuff on desmos
    coords[order[0]] = ((coordsList[1][0]-b-m*a)*coordsList[0][1]-(coordsList[1][1]-b-m*a)*coordsList[0][0])/(m*coordsList[0][1]-m*coordsList[0][0]-coordsList[1][1]+coordsList[1][0]);
    // Litterally just y = mx except no +b because the FoV is always centered on the 0,0,0 camera.
    coords[order[1]] = m*(coords[order[0]]+a)+b;
    // Find how far the new point is in terms of a % between the other two points, find the z value at that % between the two zs.
    // Similar concept to how z is found for a given point on a triangle
    coords[order[2]] = coordsList[2][0]+(coordsList[2][1]-coordsList[2][0])*Math.sqrt(((coords[order[0]]-coordsList[0][0])*(coords[order[0]]-coordsList[0][0])+(coords[order[1]]-coordsList[1][0])*(coords[order[1]]-coordsList[1][0]))/((coordsList[0][1]-coordsList[0][0])*(coordsList[0][1]-coordsList[0][0])+(coordsList[1][1]-coordsList[1][0])*(coordsList[1][1]-coordsList[1][0])));

    return (new Point3D(coords[0],coords[1],coords[2]));
  }

  public void cullTriangles(){
    List<Triangle3D> TriangleList3DTemp = new ArrayList<>();
    for (int count = 0; count < TriangleList3D.size(); count++) {
      double[] x = {TriangleList3D.get(count).p1.x, TriangleList3D.get(count).p2.x, TriangleList3D.get(count).p3.x};
      double[] y = {TriangleList3D.get(count).p1.y, TriangleList3D.get(count).p2.y, TriangleList3D.get(count).p3.y};
      double[] z = {TriangleList3D.get(count).p1.z, TriangleList3D.get(count).p2.z, TriangleList3D.get(count).p3.z};
      int c = TriangleList3D.get(count).c;
      List<Point3D> whatsOut = new ArrayList<>();
      List<Point3D> whatsIn = new ArrayList<>();
      // CHECK 1
        for(int count2 = 0; count2 < 3; count2++){
          if(x[count2] > ((intScreenSize*(z[count2]-cameraZ))/(2*dblFocalLength))+cameraX){
            whatsOut.add(new Point3D(x[count2], y[count2], z[count2]));
          }
          else{
            whatsIn.add(new Point3D(x[count2], y[count2], z[count2]));
          }
        }
        // Do nothing if size == 3(means all points are out and triangle doesnt exist)
        if(whatsOut.size() == 2){
          Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
          Point3D PoI2 = findPoI(whatsOut.get(1), whatsIn.get(0), intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
          TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c));
        }
        else if(whatsOut.size() == 1){
          Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
          Point3D PoI2 = findPoI(whatsOut.get(0), whatsIn.get(1), intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
          TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c));
          TriangleList3DTemp.add(new Triangle3D(whatsIn.get(0),whatsIn.get(1),PoI2,c));
        }
        else if(whatsOut.size() == 0){
          // Keep the same triangle
          TriangleList3DTemp.add(TriangleList3D.get(count));
        }
      }
      for (int count = 0; count < TriangleList3D.size(); count++) {
        double[] x = {TriangleList3D.get(count).p1.x, TriangleList3D.get(count).p2.x, TriangleList3D.get(count).p3.x};
        double[] y = {TriangleList3D.get(count).p1.y, TriangleList3D.get(count).p2.y, TriangleList3D.get(count).p3.y};
        double[] z = {TriangleList3D.get(count).p1.z, TriangleList3D.get(count).p2.z, TriangleList3D.get(count).p3.z};
        int c = TriangleList3D.get(count).c;
        List<Point3D> whatsOut = new ArrayList<>();
        List<Point3D> whatsIn = new ArrayList<>();
        // CHECK 1
          for(int count2 = 0; count2 < 3; count2++){
            if(x[count2] < -((intScreenSize*(z[count2]-cameraZ))/(2*dblFocalLength))+cameraX){
              whatsOut.add(new Point3D(x[count2], y[count2], z[count2]));
            }
            else{
              whatsIn.add(new Point3D(x[count2], y[count2], z[count2]));
            }
          }
          // Do nothing if size == 3(means all points are out and triangle doesnt exist)
          if(whatsOut.size() == 2){
            Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), -intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
            Point3D PoI2 = findPoI(whatsOut.get(1), whatsIn.get(0), -intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
            TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c));
          }
          else if(whatsOut.size() == 1){
            Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), -intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
            Point3D PoI2 = findPoI(whatsOut.get(0), whatsIn.get(1), -intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1});
            TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c));
            TriangleList3DTemp.add(new Triangle3D(whatsIn.get(0),whatsIn.get(1),PoI2,c));
          }
          else if(whatsOut.size() == 0){
            // Keep the same triangle
            TriangleList3DTemp.add(TriangleList3D.get(count));
          }
        }
    
    
    TriangleList3D.clear();
    for(int x = 0; x < TriangleList3DTemp.size(); x++){
      TriangleList3D.add(TriangleList3DTemp.get(x));
    }
  }

  /*
           y[count]-cameraY > ((intScreenSize*(z[count]-cameraZ))/(2*dblFocalLength)) ||
           x[count]-cameraX < -((intScreenSize*(z[count]-cameraZ))/(2*dblFocalLength)) ||
           y[count]-cameraY < -((intScreenSize*(z[count]-cameraZ))/(2*dblFocalLength))
   */
  /*
   * Project the points onto the screen.
   */
  public void projectPoints() {
    cullTriangles();
    for(int a = 0; a < TriangleList3D.size(); a++){
      double[] z = {TriangleList3D.get(a).p1.z, TriangleList3D.get(a).p2.z, TriangleList3D.get(a).p3.z};
      double[] x = {TriangleList3D.get(a).p1.x, TriangleList3D.get(a).p2.x, TriangleList3D.get(a).p3.x};
      double[] y = {TriangleList3D.get(a).p1.y, TriangleList3D.get(a).p2.y, TriangleList3D.get(a).p3.y};
      for(int count = 0; count < 3; count++){
        x[count] = -(((x[count]-cameraX) * (dblFocalLength)) / ((z[count]-cameraZ)));
        y[count] = -(((y[count]-cameraY) * (dblFocalLength)) / ((z[count]-cameraZ)));
      }
      TriangleList2D.add(new Triangle2D(new Point2D(x[0], y[0], z[0]), new Point2D(x[1],y[1],z[1]), new Point2D(x[2],y[2],z[2]), TriangleList3D.get(a).c));
    }
  }

  public void quadsectList(){
    List<Triangle3D> Triangle3DListTemp = new ArrayList<>();
    for(int x = 0; x < TriangleList3D.size(); x++){
      Point3D pC1 = new Point3D((TriangleList3D.get(x).p1.x+TriangleList3D.get(x).p2.x)/2, 
                              (TriangleList3D.get(x).p1.y+TriangleList3D.get(x).p2.y)/2, 
                              (TriangleList3D.get(x).p1.z+TriangleList3D.get(x).p2.z)/2);
      Point3D pC2 = new Point3D((TriangleList3D.get(x).p2.x+TriangleList3D.get(x).p3.x)/2, 
                              (TriangleList3D.get(x).p2.y+TriangleList3D.get(x).p3.y)/2, 
                              (TriangleList3D.get(x).p2.z+TriangleList3D.get(x).p3.z)/2);
      Point3D pC3 = new Point3D((TriangleList3D.get(x).p1.x+TriangleList3D.get(x).p3.x)/2, 
                              (TriangleList3D.get(x).p1.y+TriangleList3D.get(x).p3.y)/2, 
                              (TriangleList3D.get(x).p1.z+TriangleList3D.get(x).p3.z)/2);
      Point3D p1 = TriangleList3D.get(x).p1;
      Point3D p2 = TriangleList3D.get(x).p2;
      Point3D p3 = TriangleList3D.get(x).p3;
      int c = TriangleList3D.get(x).c;
      Triangle3DListTemp.add(new Triangle3D(p1,pC1,pC3,c));
      Triangle3DListTemp.add(new Triangle3D(p2,pC1,pC2,c));
      Triangle3DListTemp.add(new Triangle3D(p3,pC2,pC3,c));
      Triangle3DListTemp.add(new Triangle3D(pC1,pC2,pC3,c));
    }
    TriangleList3D.clear();
    for(int x = 0; x < Triangle3DListTemp.size(); x++){
      TriangleList3D.add(Triangle3DListTemp.get(x));
    }
  }

  public void rotatePoint(Point3D P, double angleXZ, double angleXY, double angleYZ) {
    angleXZ = Math.toRadians(angleXZ);
    angleXY = Math.toRadians(angleXY);
    angleYZ = Math.toRadians(angleYZ);
    double x;
    double z;
    double y;
    x = P.x - cameraX;
    z = P.z - cameraZ;
    P.x = x * Math.cos(angleXZ) - z * Math.sin(angleXZ) + cameraX;
    P.z = x * Math.sin(angleXZ) + z * Math.cos(angleXZ) + cameraZ;
    x = P.x - cameraX;
    y = P.y - cameraY;
    P.x = x * Math.cos(angleXY) - y * Math.sin(angleXY) + cameraX;
    P.y = x * Math.sin(angleXY) + y * Math.cos(angleXY) + cameraY;
    z = P.z - cameraZ;
    y = P.y - cameraY;
    P.z = z * Math.cos(angleYZ) - y * Math.sin(angleYZ) + cameraZ;
    P.y = z * Math.sin(angleYZ) + y * Math.cos(angleYZ) + cameraY;
  }

  

  public void rotateAroundPoint(Point3D PoR, Point3D P, Point3D Rotation) {
    double angleXZ = Math.toRadians(Rotation.x);
    double angleXY = Math.toRadians(Rotation.y);
    double angleYZ = Math.toRadians(Rotation.z);
    double xC = PoR.x;
    double yC = PoR.y;
    double zC = PoR.z;
    double x;
    double z;
    double y;
    x = P.x - xC;
    z = P.z - zC;
    P.x = x * Math.cos(angleXZ) - z * Math.sin(angleXZ) + xC;
    P.z = x * Math.sin(angleXZ) + z * Math.cos(angleXZ) + zC;
    x = P.x - xC;
    y = P.y - yC;
    P.x = x * Math.cos(angleXY) - y * Math.sin(angleXY) + xC;
    P.y = x * Math.sin(angleXY) + y * Math.cos(angleXY) + yC;
    z = P.z - zC;
    y = P.y - yC;
    P.z = z * Math.cos(angleYZ) - y * Math.sin(angleYZ) + zC;
    P.y = z * Math.sin(angleYZ) + y * Math.cos(angleYZ) + yC;
  }

  public double area(int x1, int y1, int x2, int y2, int x3, int y3){
    return Math.abs((x1*(y2-y3) + x2*(y3-y1)+x3*(y1-y2))/2.0);
  }

public double interpolateDepth(Point2D point, Point2D[] vertices, double[] depths) {
  double[] baryCoords = new double[3];
  double totalArea = area((int)vertices[0].x, (int)vertices[0].y, (int)vertices[1].x, (int)vertices[1].y, (int)vertices[2].x, (int)vertices[2].y);
  baryCoords[0] = area((int)point.x, (int)point.y, (int)vertices[1].x, (int)vertices[1].y, (int)vertices[2].x, (int)vertices[2].y) / totalArea;
  baryCoords[1] = area((int)vertices[0].x, (int)vertices[0].y, (int)point.x, (int)point.y, (int)vertices[2].x, (int)vertices[2].y) / totalArea;
  baryCoords[2] = area((int)vertices[0].x, (int)vertices[0].y, (int)vertices[1].x, (int)vertices[1].y, (int)point.x, (int)point.y) / totalArea;
  double interpolatedDepth = baryCoords[0] * depths[0] + 
                             baryCoords[1] * depths[1] +
                             baryCoords[2] * depths[2];

  return interpolatedDepth;
}

  public void drawFaces(){
    // This took me over a month + reading more than 20 articles on 3d rendering, asking reddit, looking through wikipedia and looking thorugh blogs just to understand how this works and how to implement it.
    projectPoints();
    for(int r = 0; r < TriangleList2D.size(); r++){
      Point2D p1 = TriangleList2D.get(r).p1;
      Point2D p2 = TriangleList2D.get(r).p2;
      Point2D p3 = TriangleList2D.get(r).p3;
      int x1 = (int)p1.x;
      int x2 = (int)p2.x;
      int x3 = (int)p3.x;
      int y1 = (int)p1.y;
      int y2 = (int)p2.y;
      int y3 = (int)p3.y;
      double A = area (x1, y1, x2, y2, x3, y3);
      Point2D[] pointsList = {p1,p2,p3};
      double[] depths = {p1.z,p2.z,p3.z};
      int yMin = (int)Math.min(Math.min(p1.y,p2.y),p3.y);
      int yMax = (int)Math.max(Math.max(p1.y, p2.y),p3.y);
      int xMin = (int)Math.min(Math.min(p1.x, p2.x),p3.x);
      int xMax = (int)Math.max(Math.max(p1.x, p2.x),p3.x);
      if(xMax >= scDiv2){
        xMax = scDiv2;
      }
      if(xMin <= -scDiv2){
        xMin = -scDiv2;
      }
      if(yMax >= scDiv2-1){
        yMax = scDiv2-1;
      }
      if(yMin <= -scDiv2+1){
        yMin = -scDiv2+1;
      }
      // proud to say i came up with the bounding box without looking it up(it's pretty much the only concept in this method i didnt look up)
      for(int x = xMin; x < xMax; x++){
        for(int y = yMin; y < yMax; y++){
          double A1 = area (x, y, x2, y2, x3, y3);
          double A2 = area (x1, y1, x, y, x3, y3);
          double A3 = area (x1, y1, x2, y2, x, y);
          if(A == A1 + A2 + A3){
            double z = interpolateDepth(new Point2D(x, y,0), pointsList, depths);
            if((z-cameraZ) > 0 && (z-cameraZ) < zBuffer[((scDiv2-y)-1)*intScreenSize+(scDiv2+x)]){
              zBuffer[((scDiv2-y)-1)*intScreenSize+(scDiv2+x)] = (float)(z-cameraZ);
              set(scDiv2 + x, scDiv2 - y, TriangleList2D.get(r).c);
            }
          }
        }
      }
    }
  }
}
