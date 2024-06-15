import processing.core.PApplet;
import processing.event.KeyEvent;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.List;   


/* 
 *  
 * The purpose of this program is to use 2d processing to make a 3d game
 * No 3d methods are used throughout the project.
 * 
 * Author: @duocaleb
*/
public class Rendering extends PApplet {
  // Initalizing the variables
  double dblFocalLength = 500;  
  // Scene stuff
  double cameraX = 0;
  double cameraY = 0;
  double cameraZ = 0;
  double mouseRotationX = 0;
  double mouseRotationY = 0;
  double mouseCenteredX = 0;
  double mouseCenteredY = 0;
  double mouseSensitivity = 180;
  List<Triangle3D> TriangleList3D = new ArrayList<>();
  int intScreenSize;
  double platformH1 = 0;
  // Emphasis on the box part, the hitboxes are non-rotatable rectangular prisms.
  List<Point3D[]> HitBoxList = new ArrayList<>();
  List<Triangle2D> TriangleList2D = new ArrayList<>();
  double[] zBuffer;
  int scDiv2;


  // Lighting stuff
  double ambientLightStrength = 0.1;
  Point3D ambientLightColor = new Point3D(255,255,255);
  Point3D skyColour = new Point3D(0,0,255);
  List<Point3D[]> LightSources = new ArrayList<>();

  // Player stuff
  double playerHeight = 100;
  double playerXPos = 0;
  double playerZPos = 0;
  double playerYPos = 0;
  double playerXVel = 0;
  double playerZVel = 0;
  double playerYVel = 0;
  double playerSpeed = 2;
  double jumpHeight = 7;
  double prevX = 0;
  double prevZ = 0;

  // Other stuff
  double deltaTime = 60/frameRate;
  boolean[] isKeyPressed = new boolean[256];
  boolean hasJump = true;
  Robot mouseStealer9000;
  double devStats = -1;
  double menu = -1;
  boolean mouseClicked = false;

  public void settings() {
    size(displayWidth, displayHeight);
    fullScreen();
  }

  public void setup() {

    // Setting up moving the mouse to the center of the screen
    try {
      mouseStealer9000 = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
    noCursor();
    mouseStealer9000.mouseMove(displayWidth/2, displayHeight/2);
    frameRate(60);
    intScreenSize = Math.min(displayHeight,displayWidth);
    zBuffer = new double[intScreenSize*intScreenSize];
    scDiv2 = intScreenSize/2;
  }

  // Initialization ends here
  public void draw() {
    // Makes sure deltaTime never goes > 1(random accelleration, higher than expected speed, ect)
    if(frameRate > 60){
      frameRate = 60;
    }
    deltaTime = 60/frameRate;

    // Drawing part
    clear();
    if(menu == -1){
      cursorMovement();
      renderInOrder();
      moveChar();
      noCursor();

      // fps counter n other stuff. mostly for testing.
      if(devStats == 1){
        displayStats();
      }
    }
    else{
      clear();
      fill(255,255,255);
      rect(displayWidth/2-150,displayHeight/2+300,300,100);
      rect(displayWidth/2-150,displayHeight/2+100,300,100);
      rect(displayWidth/2-150,displayHeight/2-100,300,100);
      text("You are paused!", displayWidth/2-300,displayHeight/2-300);
      textSize(30);
      text("Press ESC again to unpause", displayWidth/2-200,displayHeight/2-250);
      fill(0,0,0);
      text("Reset mouse(c)", displayWidth/2-105,displayHeight/2+160);
      text("Toggle devstats(f)", displayWidth/2-120,displayHeight/2-45);
      fill(255,0,0);
      textSize(80);
      text("EXIT", displayWidth/2-85,displayHeight/2+380);
      if(mouseX > displayWidth/2-150 && mouseX < displayWidth/2+250 && mouseY > displayHeight/2+300 && mouseY < displayHeight/2+400 && mousePressed){
        exit();
      }
      if(mouseX > displayWidth/2-150 && mouseX < displayWidth/2+250 && mouseY > displayHeight/2+100 && mouseY < displayHeight/2+200 && mousePressed){
        mouseStealer9000.mouseMove(displayWidth/2, displayHeight/2);
        mouseCenteredX = 0;
        mouseCenteredY = 0;
      }
      if(mouseX > displayWidth/2-150 && mouseX < displayWidth/2+250 && mouseY > displayHeight/2-100 && mouseY < displayHeight/2){
        //Just press f. I cant deal with this stupidly inconsistant code anymore
      }
    }
  }

  // 3D framework starts here

  /**+
   * Draws lines onto the screen. Outlines every triangle. Used for debugging purposes. 
   */
  public void drawLines(){
    float a = scDiv2;
    for(int x = 0; x < TriangleList2D.size(); x++){
      float x1 = displayWidth-a+(float)TriangleList2D.get(x).p1.x;
      float x2 = displayWidth-a+(float)TriangleList2D.get(x).p2.x;
      float x3 = displayWidth-a+(float)TriangleList2D.get(x).p3.x;
      float y1 = displayHeight-a+(float)TriangleList2D.get(x).p1.y;
      float y2 = displayHeight-a+(float)TriangleList2D.get(x).p2.y;
      float y3 = displayHeight-a+(float)TriangleList2D.get(x).p3.y;
      line(x1,y1,x2,y2);
      line(x2,y2,x3,y3);
      line(x3,y3,x1,y1);
    }
  }

  /**
   * Determines the Point of Intersection between two points and a line defined by y = m(x+a)+b. y and x can be any of x, y or z.
   * 
   * @param p1 First point that is used
   * @param p2 Second point that is used
   * @param m m of the _ = m(_+a)+b equation
   * @param b b of the _ = m(_+a)+b equation
   * @param a a of the _ = m(_+a)+b equation
   * @param order determines which order the equation goes in. As an example, an order of {2,0,1} would use the equation z = m(x+a)+b. The last number in the order is not used
   * @return The x,y, and z of the intersection between the two points and the line as a 3D point.
   */
  public Point3D findPoI(Point3D p1, Point3D p2, double m, double b, double a, int[] order){

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

    // Equation made by me strugling way too much on desmos(i could have just used vectors)
    coords[order[0]] = ((coordsList[1][0]-b-m*a)*coordsList[0][1]-(coordsList[1][1]-b-m*a)*coordsList[0][0])/(m*coordsList[0][1]-m*coordsList[0][0]-coordsList[1][1]+coordsList[1][0]);
    coords[order[1]] = m*(coords[order[0]]+a)+b;
    // Linear interpolation between points
    coords[order[2]] = coordsList[2][0]+(coordsList[2][1]-coordsList[2][0])*Math.sqrt((Math.pow((coords[order[0]]-coordsList[0][0]),2)+Math.pow((coords[order[1]]-coordsList[1][0]),2))/(Math.pow((coordsList[0][1]-coordsList[0][0]),2)+Math.pow((coordsList[1][1]-coordsList[1][0]),2)));

    return new Point3D(coords[0],coords[1],coords[2]);
  }

  /**
   * 
   * Culls triangles on a plane. can be configured to < or > said plane with the sign param.
   * 
   * @param m m of the equation for PoI
   * @param b b of the equation for PoI
   * @param a a of the equation for PoI
   * @param order order for finding the PoI
   * @param sign 1 is for >, -1 is for <.
   */
  public void cullTriangles(double m, double b, double a, int[] order, int sign){
    // Define a new temp list for the new list to go into.
    List<Triangle3D> TriangleList3DTemp = new ArrayList<>();
    for (int count = 0; count < TriangleList3D.size(); count++) {
      double[][] PList = new double[3][3];
      for(int x = 0; x < 3; x++){
        if(order[x] == 0){
          PList[x][0] = TriangleList3D.get(count).p1.x;
          PList[x][1] = TriangleList3D.get(count).p2.x;
          PList[x][2] = TriangleList3D.get(count).p3.x;
        }
        else if(order[x] == 1){
          PList[x][0] = TriangleList3D.get(count).p1.y;
          PList[x][1] = TriangleList3D.get(count).p2.y;
          PList[x][2] = TriangleList3D.get(count).p3.y;
        }
        else if(order[x] == 2){
          PList[x][0] = TriangleList3D.get(count).p1.z;
          PList[x][1] = TriangleList3D.get(count).p2.z;
          PList[x][2] = TriangleList3D.get(count).p3.z;
        }
      }
      double[] x = {TriangleList3D.get(count).p1.x,TriangleList3D.get(count).p2.x,TriangleList3D.get(count).p3.x};
      double[] y = {TriangleList3D.get(count).p1.y,TriangleList3D.get(count).p2.y,TriangleList3D.get(count).p3.y};
      double[] z = {TriangleList3D.get(count).p1.z,TriangleList3D.get(count).p2.z,TriangleList3D.get(count).p3.z};
      Point3D normal = TriangleList3D.get(count).n;
      Point3D c = TriangleList3D.get(count).c;
      List<Point3D> whatsOut = new ArrayList<>();
      List<Point3D> whatsIn = new ArrayList<>();
      // Checks if the point is inside or outside of the line.
      for(int count2 = 0; count2 < 3; count2++){
        if(sign*PList[1][count2] > sign*(((PList[0][count2]+a)*m)+b)){
          whatsOut.add(new Point3D(x[count2], y[count2], z[count2]));
        }
        else{
          whatsIn.add(new Point3D(x[count2], y[count2], z[count2]));
        }
      }

      // Do nothing if size == 3(means all points are out and triangle doesnt exist)

      // If 2 points are outside, form 1 triangle with the 2 intersections and the 1 point inside.
      if(whatsOut.size() == 2){
        Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), m,b, a,order);
        Point3D PoI2 = findPoI(whatsOut.get(1), whatsIn.get(0), m,b, a,order);
        TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
      }
      // If 1 point is outside, form 2 triangles with the 2 intersections and 2 points inside.
      else if(whatsOut.size() == 1){
        Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), m,b, a,order);
        Point3D PoI2 = findPoI(whatsOut.get(0), whatsIn.get(1), m,b, a,order);
        TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
        TriangleList3DTemp.add(new Triangle3D(whatsIn.get(0),whatsIn.get(1),PoI2,c));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
      }
      // If all points are inside, keep the triangle as is.
      else if(whatsOut.size() == 0){
        // Keep the same triangle
        TriangleList3DTemp.add(TriangleList3D.get(count));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
      }
    }

    // Replace TriangleList3D with the temp list defined earlier
    TriangleList3D.clear();
    for(int x = 0; x < TriangleList3DTemp.size(); x++){
      TriangleList3D.add(TriangleList3DTemp.get(x));
    }
    // Clear temp list for next time this method is called
    TriangleList3DTemp.clear();
  }

  /**
   * Projects point onto the screen.
   */
  public void projectPoints() {
    // So that everything fits into the 2d space
    frustrumCull();

    TriangleList2D.clear();
    for(int a = 0; a < TriangleList3D.size(); a++){
      double[] x = {TriangleList3D.get(a).p1.x, TriangleList3D.get(a).p2.x, TriangleList3D.get(a).p3.x};
      double[] y = {TriangleList3D.get(a).p1.y, TriangleList3D.get(a).p2.y, TriangleList3D.get(a).p3.y};
      double[] z = {TriangleList3D.get(a).p1.z, TriangleList3D.get(a).p2.z, TriangleList3D.get(a).p3.z};

      for(int count = 0; count < 3; count++){
        x[count] = -(((x[count]-cameraX) * (dblFocalLength)) / ((z[count]-cameraZ)));
        y[count] = -(((y[count]-cameraY) * (dblFocalLength)) / ((z[count]-cameraZ)));
      }
      TriangleList2D.add(new Triangle2D(new Point2D(x[0], y[0], TriangleList3D.get(a).p1.z), new Point2D(x[1],y[1],TriangleList3D.get(a).p2.z), new Point2D(x[2],y[2],TriangleList3D.get(a).p3.z), TriangleList3D.get(a).n, TriangleList3D.get(a).c));
    }
  }

  /**
   * 
   * Takes in a point, along with how much it has been rotated, and then returns the original point
   * 
   * @param P Point to reverse the rotation of
   * @param angleXZ Angle that has been rotated along the XZ plane
   * @param angleXY Angle that has been rotated along the XY plane
   * @param angleYZ Angle that has been rotated along the YZ plane
   * @return The point before the rotation
   */
  public Point3D ReverseRotatePoint(Point3D P, double angleXZ, double angleXY, double angleYZ) {
    angleXZ = Math.toRadians(angleXZ);
    angleXY = Math.toRadians(angleXY);
    angleYZ = Math.toRadians(angleYZ);
    double x;
    double z;
    double y;
    // Opposite order of operations from rotatePoint().
    double x1 = P.x;
    double y1 = P.y;
    double z1 = P.z;
    z = z1 - cameraZ;
    y = y1 - cameraY;
    y1 = z * Math.sin(angleYZ) - y * Math.cos(angleYZ) + cameraY;
    z1 = -z * Math.cos(angleYZ) - y * Math.sin(angleYZ) + cameraZ;
    x = x1 - cameraX;
    y = y1 - cameraY;
    x1 = -x * Math.cos(angleXY) - y * Math.sin(angleXY) + cameraX;
    y1 = x * Math.sin(angleXY) - y * Math.cos(angleXY) + cameraY;
    x = x1 - cameraX;
    z = z1 - cameraZ;
    x1 = - x * Math.cos(angleXZ) - z * Math.sin(angleXZ) + cameraX;
    z1 = x * Math.sin(angleXZ) - z * Math.cos(angleXZ) + cameraZ;
    return new Point3D(x1,y1,z1);
  }

  /**
   * 
   * Takes in a point and rotates it acording the 3 angles coresponding with xz, xy and yz.
   * 
   * @param P Original point to rotate.
   * @param angleXZ How much to rotate in terms of XZ
   * @param angleXY How much to rotate in terms of XY
   * @param angleYZ How much to rotate in terms of YZ
   * @return The rotated point
   */
  public Point3D rotatePoint(Point3D P, double angleXZ, double angleXY, double angleYZ) {
    angleXZ = Math.toRadians(angleXZ);
    angleXY = Math.toRadians(angleXY);
    angleYZ = Math.toRadians(angleYZ);
    double x;
    double z;
    double y;
    double x1 = P.x;
    double y1 = P.y;
    double z1 = P.z;
    // 3 2d rotation matricies in a row, getting you to the full 3d rotation.
    x = x1 - cameraX;
    z = z1 - cameraZ;
    x1 = x * Math.cos(angleXZ) - z * Math.sin(angleXZ) + cameraX;
    z1 = x * Math.sin(angleXZ) + z * Math.cos(angleXZ) + cameraZ;
    x = x1 - cameraX;
    y = y1 - cameraY;
    x1 = x * Math.cos(angleXY) - y * Math.sin(angleXY) + cameraX;
    y1 = x * Math.sin(angleXY) + y * Math.cos(angleXY) + cameraY;
    z = z1 - cameraZ;
    y = y1 - cameraY;
    y1 = z * Math.sin(angleYZ) + y * Math.cos(angleYZ) + cameraY;
    z1 = z * Math.cos(angleYZ) - y * Math.sin(angleYZ) + cameraZ;
    return new Point3D(x1,y1,z1);
  }

  /**
   * 
   * takes in 3 points, returns the area of the 2d triangle formed.
   * 
   * @param x1 first x
   * @param y1 first y
   * @param x2 second x
   * @param y2 second y
   * @param x3 third x
   * @param y3 third y
   * @return the area of the 2d triangle formed by the 3 points.
   */
  public double area(double x1, double y1, double x2, double y2, double x3, double y3){
    return Math.abs((x1*(y2-y3) + x2*(y3-y1)+x3*(y1-y2))/2.0);
  }

  /**
   * 
   * Takes in 1 2d point and another 3 2d points, which form the verticies of a triangle 
   * and an array of 3 depths corresponding to those vertices.
   * Returns the z value of the first 2d point.
   * 
   * @param point The new point.
   * @param vertices An array of the vertices
   * @param depths An array of the z values of the vertices.
   * @return The Z value of the new point
   */
  public double interpolateDepth(Point2D point, Point2D[] vertices, double[] depths) {
    Point2D p1 = vertices[0];
    Point2D p2 = vertices[1];
    Point2D p3 = vertices[2];
    double tA = area(p1.x,p1.y,p2.x,p2.y,p3.x,p3.y);
    // Bayesian co-ordinates, essensially gives 3 l variables each coresponding to a value between 0-1
    // Representing the total percent of the area of the full trianlge that the triangle formed between the new point and 2 verticies takes.
    // All add up to 1.  
    double l1 = area(p1.x,p1.y,p2.x,p2.y,point.x,point.y)/tA;
    double l2 = area(p1.x,p1.y,p3.x,p3.y,point.x,point.y)/tA;
    double l3 = area(p3.x,p3.y,p2.x,p2.y,point.x,point.y)/tA;
    // Took me over 5 days of multiple hours each to debug this one line :(
    // Uses 1/(z-cameraz) instead of just z-cameraz because after the prespective divide, z-cameraz is no longer a linear variance across the 
    // Triangle, but 1/(z-cameraz) is. Idk why tbh, but all the articles i read had some pretty legit looking math.
    double interpolatedDepth = 1/(l1*(1/(depths[2]-cameraZ))+l2*(1/(depths[1]-cameraZ))+l3*(1/(depths[0]-cameraZ))) + cameraZ;
    return interpolatedDepth;
  }

  /**
   * Culls the tirangles in the 3D list so everything is within the player's fov. everything outside is not loaded.
   */
  public void frustrumCull(){
    cullTriangles(intScreenSize/(2*dblFocalLength),cameraX,-cameraZ, new int[]{2,0,1},1);
    cullTriangles(-intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1},-1);
    cullTriangles(intScreenSize/(2*dblFocalLength),cameraY, -cameraZ, new int[]{2,1,0},1);
    cullTriangles(-intScreenSize/(2*dblFocalLength),cameraY, -cameraZ, new int[]{2,1,0},-1);
  }

  /**
   * Draws the faces! 
   */
  public void drawFaces(){
    // This took me over a month + reading more than 20 articles on 3d rendering, asking reddit, looking through wikipedia and looking thorugh blogs just to understand how this works and how to implement it.
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
      double z1 = p1.z;
      double z2 = p2.z;
      double z3 = p3.z;
      double A = area (x1, y1, x2, y2, x3, y3);
      Point2D[] pointsList = {p1,p2,p3};
      int yMin = (int)Math.min(Math.min(p1.y,p2.y),p3.y);
      int yMax = (int)Math.max(Math.max(p1.y, p2.y),p3.y);
      int xMin = (int)Math.min(Math.min(p1.x, p2.x),p3.x);
      int xMax = (int)Math.max(Math.max(p1.x, p2.x),p3.x);
      // Makes sure that the point is never projected off screen
      if(xMax > scDiv2){
        xMax = scDiv2;
      }
      if(xMin < -scDiv2){
        xMin = -scDiv2;
      }
      if(yMax > scDiv2){
        yMax = scDiv2;
      }
      if(yMin < -scDiv2){
        yMin = -scDiv2;
      }
      Point3D normal = TriangleList2D.get(r).n;
      double nLength = Math.sqrt(normal.x*normal.x+normal.y*normal.y+normal.z*normal.z);
      for(int x = xMin; x < xMax; x++){
        for(int y = yMin; y < yMax; y++){
          double A1 = area (x, y, x2, y2, x3, y3);
          double A2 = area (x1, y1, x, y, x3, y3);
          double A3 = area (x1, y1, x2, y2, x, y);
          if((int)A == (int)(A1 + A2 + A3)){
            double z = interpolateDepth(new Point2D(x, y,0), pointsList, new double[] {z1,z2,z3});
            if(z < 3000 && z < zBuffer[((scDiv2-y)-1)*intScreenSize+(scDiv2+x)]){
              zBuffer[((scDiv2-y)-1)*intScreenSize+(scDiv2+x)] = z;
              // "Let there be light" ahh code
              Point3D pColor = new Point3D(TriangleList2D.get(r).c.x/255,TriangleList2D.get(r).c.y/255,TriangleList2D.get(r).c.z/255);
              Point3D diffuseLight = new Point3D(0,0,0);
              Point3D originalPoint = new Point3D(-(x * (z - cameraZ)) / dblFocalLength + cameraX,-(y * (z - cameraZ)) / dblFocalLength + cameraY, z);
              originalPoint = ReverseRotatePoint(originalPoint, -mouseRotationX, 0, -mouseRotationY);
              Point3D newNormal = new Point3D(normal.x/nLength,normal.y/nLength,normal.z/nLength);
              for(int lightCount = 0; lightCount < LightSources.size(); lightCount++){
                Point3D lightPosition = LightSources.get(lightCount)[0];
                Point3D lightColour = LightSources.get(lightCount)[1];
                double diffuseLightStrength = LightSources.get(lightCount)[2].x;
                Point3D newLightColour = new Point3D(lightColour.x/255, lightColour.y/255, lightColour.z/255);
                Point3D lightDirection = new Point3D(lightPosition.x-originalPoint.x, lightPosition.y-originalPoint.y, lightPosition.z-originalPoint.z); 
                double lLength = Math.sqrt(lightDirection.x*lightDirection.x+lightDirection.y*lightDirection.y+lightDirection.z*lightDirection.z);
                lightDirection = new Point3D(lightDirection.x/lLength,lightDirection.y/lLength,lightDirection.z/lLength);
                double diffuseLightFactor = lightDirection.x*newNormal.x+lightDirection.y*newNormal.y+lightDirection.z*newNormal.z;
                if(diffuseLightFactor < 0){
                  diffuseLightFactor = (1+diffuseLightFactor);
                }

                diffuseLight.x += (diffuseLightFactor*newLightColour.x)*(diffuseLightStrength/lLength);
                diffuseLight.y += (diffuseLightFactor*newLightColour.y)*(diffuseLightStrength/lLength);
                diffuseLight.z += (diffuseLightFactor*newLightColour.z)*(diffuseLightStrength/lLength);
            }

              // Ambient Light
              Point3D ambientLight = new Point3D(ambientLightColor.x*ambientLightStrength/255,ambientLightColor.y*ambientLightStrength/255,ambientLightColor.z*ambientLightStrength/255);
              
              // Adding light
              pColor.x = Math.abs(pColor.x*255.0*(diffuseLight.x+ambientLight.x));
              pColor.y = Math.abs(pColor.y*255.0*(diffuseLight.y+ambientLight.y));
              pColor.z = Math.abs(pColor.z*255.0*(diffuseLight.z+ambientLight.z));
              int c = color((int)pColor.x,(int)pColor.y,(int)pColor.z);
              set(scDiv2+x+(displayWidth-intScreenSize)/2,(scDiv2+y)+1+(displayHeight-intScreenSize)/2,c);
            }
          }
        }
      }
    }
  }

  /**
   * Renders the entire scene in order
   */
  public void renderInOrder(){
    // Clears/resets all lists and buffers
    TriangleList2D.clear();
    TriangleList3D.clear();
    LightSources.clear();
    HitBoxList.clear();
    for(int x = 0; x < zBuffer.length; x++){
      zBuffer[x] =  Float.POSITIVE_INFINITY;
    }
    // Add the scene
    addScene();

    // Project points
    projectPoints();

    // Draw all faces
    drawFaces();
  }

  // Game elements start here

  /**
   * Gets the rotation of the cursor. 
   * mouseSensitivity controls how many degrees the camera moves every time the mouse goes across half of the screen.
   */
  public void cursorMovement(){
    if (mouseCenteredX == 0 && mouseCenteredY == 0){
      mouseCenteredX = mouseX;
      mouseCenteredY = mouseY;
    }

    mouseRotationX += deltaTime*((mouseSensitivity*(mouseX - mouseCenteredX)))/scDiv2;
    mouseRotationY -= deltaTime*((mouseSensitivity*(mouseY - mouseCenteredY)))/scDiv2;
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
    mouseStealer9000.mouseMove(displayWidth/2, displayHeight/2);
  }
  
  public void keyPressed() {
    if(keyCode == ESC){
      key = 0;
      cursor();
      menu *= -1;
    }
    if(key != 65535){
      isKeyPressed[(int)Character.toLowerCase(key)] = true;
    }
    else if(key == CODED){
      if(keyCode == SHIFT){
        isKeyPressed[255] = true;
      }
    }
  }
  
  public void keyReleased() {
    if(key != 65535){
      isKeyPressed[(int)Character.toLowerCase(key)] = false;
    }
    else if(key == CODED && keyCode == SHIFT){
      isKeyPressed[255] = false;
    }
  }

  public void keyTyped(KeyEvent event) {
    if(Character.toLowerCase((char)key) == 'f'){
      devStats *= -1;
    }
    if(Character.toLowerCase((char)key) == 'c'){
      mouseStealer9000.mouseMove(displayWidth/2, displayHeight/2);
      mouseCenteredX = 0;
      mouseCenteredY = 0;
    }
  }
  /**
   * Moves the character! Slightly more complex than it's 2d counterpart, 
   * it takes the amount that the camera is facing in each direciton and uses them to create a realistic feeling movement system.
   * 
   * This also includes hit box collisions, and also gravity
   * 
   * hit box collisions: Takes the list of hitboxes(max point and min points) and checks if the player is inside of it, with one of the 
   * variables(x,y, or z) being inside it on the NEXT frame. If yes, stops movement in that direction.
   * 
   * gravity: if on floor, dont move down. if not on floor, move down. if inside floor, move to floor.
   * floor is defined by either the top of the box that the bottom of the player is currently inside, or the highest top of
   * box that is below the character.
   * ceiling is similar. it is defined by either the bottom of the box that the top of the player is in, or the lowest bottom of box that is 
   * above the top of the player. if touching ceiling, set yvel to 0 and let gravity work.
   */
  public void moveChar() {
    // If statements. Lots of em.
    double rotB = Math.toRadians(mouseRotationX);

    double zVelStore = 0;
    double xVelStore = 0;
    if(isKeyPressed[(int)'w']){
      zVelStore += playerSpeed*Math.cos(rotB);
      xVelStore += -playerSpeed*Math.sin(rotB);
    }
    if(isKeyPressed[(int)'s']){
      zVelStore += -playerSpeed*Math.cos(rotB);
      xVelStore += playerSpeed*Math.sin(rotB);
    }
    if(isKeyPressed[(int)'a']){
      zVelStore += playerSpeed*Math.sin(rotB);
      xVelStore += playerSpeed*Math.cos(rotB);
    }
    if(isKeyPressed[(int)'d']){
      zVelStore += -playerSpeed*Math.sin(rotB);
      xVelStore += -playerSpeed*Math.cos(rotB);
    }
    if(zVelStore != 0){
      playerZVel = zVelStore;
    }
    if(xVelStore != 0){
      playerXVel = xVelStore;
    }
    if(isKeyPressed[(int)' '] && hasJump){
      playerYVel = jumpHeight;
      hasJump = false;
    } 

    if(isKeyPressed[255]){
      playerSpeed = 5;
    }
    else{
      playerSpeed = 2.5;
    }
    // No running off the map :(
    double floor = -50;
    double ceiling = Double.POSITIVE_INFINITY;
    boolean floorDecided = false;
    for(int l = 0; l < HitBoxList.size(); l++){
      Point3D min = HitBoxList.get(l)[0];
      Point3D max = HitBoxList.get(l)[1];
      
      if(playerXPos+playerXVel*deltaTime+20 > min.x && playerXPos+playerXVel*deltaTime-20 < max.x && playerYPos+playerHeight+10 > min.y && playerYPos < max.y && playerZPos+20 > min.z && playerZPos-20 < max.z){
        playerXVel = 0;
      }
      if(playerXPos+20 > min.x && playerXPos-20 < max.x && playerYPos+playerHeight+10 > min.y && playerYPos < max.y && playerZPos+playerZVel*deltaTime+20 > min.z && playerZPos+playerZVel*deltaTime-20 < max.z){
        playerZVel = 0;
      }

      // Checks if you're in the box, sets the floor to the top of box if you are
      if(playerXPos+18 > min.x && playerXPos-18 < max.x && playerZPos+18 > min.z && playerZPos-18 < max.z && playerYPos > min.y && playerYPos < max.y){
        floor = max.y;
        floorDecided = true;
      }
      // If not in a box, check if 1. the top of the box is below you and 2. if the top of the box is above the current floor 
      else if(playerXPos+18 > min.x && playerXPos-18 < max.x && playerZPos+18 > min.z && playerZPos-18 < max.z && playerYPos > max.y && max.y > floor && !floorDecided){
        floor = max.y;
      }

      if(playerXPos+18 > min.x && playerXPos-18 < max.x && playerZPos+18 > min.z && playerZPos-18 < max.z && playerYPos+playerHeight+10 < min.y && min.y < ceiling){
        ceiling = min.y;
      }

    }
    prevX = playerXPos;
    prevZ = playerZPos;
    playerYPos += playerYVel*deltaTime;
    playerXPos += playerXVel*deltaTime;
    playerZPos += playerZVel*deltaTime;
    playerXVel *= 0.92/deltaTime;
    playerZVel *= 0.92/deltaTime;

    if(playerXVel < 0.1 && playerXVel > -0.1){
      playerXVel = 0;
    }
    if(playerZVel < 0.1 && playerZVel > -0.1){
      playerZVel = 0;
    }
    if(playerYPos+playerHeight+10 >= ceiling){
      playerYVel = 0;
      playerYPos = ceiling-playerHeight-11;
    }
    if(playerYPos > floor+2){
      playerYVel -= 0.2*deltaTime;
      hasJump = false;
    }
    else if(playerYPos < floor+2){
      playerYVel = 0;
      playerYPos = floor+2;
      hasJump = true;
    }
    else{
      playerYVel = 0;
      hasJump = true;
    }
    

    // Camera following
    cameraX = playerXPos;
    cameraZ = playerZPos;
    cameraY = playerYPos+playerHeight;

  }

  // Objects are defined here

  /**
   * 
   * Adds a box to the scene
   * Not really a cube, but the name was too iconic
   * 
   * @param minPoint Lowest point of the box
   * @param maxPoint Highest point of the box
   * @param colour1 Colour of face 1
   * @param colour2 Colour of face 2
   * @param colour3 Colour of face 3
   * @param colour4 Colour of face 4
   * @param colour5 Colour of face 5
   * @param colour6 Colour of face 6
   * @param hitBox Do you add a hitbox or not
   */
  public void addCube(Point3D minPoint, Point3D maxPoint, Point3D colour1, Point3D colour2, Point3D colour3, Point3D colour4, Point3D colour5, Point3D colour6, boolean hitBox){
    Point3D[] PointList = new Point3D[8];
    Point3D[] colourList = new Point3D[6];
    for(int x = 0; x < 8; x++){
      // Defining a new point cause it doesnt work otherwise
      PointList[x] = new Point3D(0,0,0);
    }
    int[][] Connections = {{2,3,1},{1,0,2},{6,2,0},{0,4,6},{0,1,5},{5,4,0},{3,7,5},{5,1,3},{6,7,3},{3,2,6},{7,6,4},{4,5,7}};
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

    for(int x = 0; x < 4; x += 1){
      PointList[x*2].x = maxX+1;
      PointList[x*2+1].x = minX-1;
      PointList[(x/2)*2+x].y = maxY;
      PointList[(x/2)*2+x+2].y = minY;
      PointList[x].z = minZ;
      PointList[x+4].z = maxZ;
    }
    // Pre-rotation, for hitboxes
    if(hitBox){
      HitBoxList.add(new Point3D[]{minPoint, maxPoint});
    }

    for(int x = 0; x < 8; x++){
      PointList[x] = rotatePoint(PointList[x], -mouseRotationX, 0, -mouseRotationY);
    }
    // Post-rotation, for the display itself.
    for(int a = 0; a < Connections.length; a++){
      Point3D p1 = PointList[Connections[a][0]];
      Point3D p2 = PointList[Connections[a][1]];
      Point3D p3 = PointList[Connections[a][2]];
      TriangleList3D.add(new Triangle3D(p1,p2,p3,colourList[(int)a/2]));
      // calculates the normal(world space), replaces it.
      Point3D p1o = ReverseRotatePoint(p1,-mouseRotationX, 0, -mouseRotationY);
      Point3D p2o = ReverseRotatePoint(p2,-mouseRotationX, 0, -mouseRotationY);
      Point3D p3o = ReverseRotatePoint(p3,-mouseRotationX, 0, -mouseRotationY);
      Point3D v1o = new Point3D(p2o.x-p1o.x,p2o.y-p1o.y,p2o.z-p1o.z);
      Point3D v2o = new Point3D(p3o.x-p1o.x,p3o.y-p1o.y,p3o.z-p1o.z);
      TriangleList3D.get(TriangleList3D.size()-1).n = new Point3D(Math.round((v1o.y*v2o.z-v1o.z*v2o.y)*100.0)/100.0,Math.round((v1o.z*v2o.x-v1o.x*v2o.z)*100.0)/100.0,Math.round((v1o.x*v2o.y-v1o.y*v2o.x)*100.0)/100.0);
    }
  }

  /**
   * Adds a triangle to the scene.
   * 
   * @param p1o vertex 1
   * @param p2o vertex 2
   * @param p3o vertex 3
   * @param colour colour of the triangle
   */
  public void addTriangle(Point3D p1o, Point3D p2o, Point3D p3o, Point3D colour){  
    Point3D p1 = rotatePoint(p1o, -mouseRotationX, 0, -mouseRotationY);
    Point3D p2 = rotatePoint(p2o, -mouseRotationX, 0, -mouseRotationY);
    Point3D p3 = rotatePoint(p3o, -mouseRotationX, 0, -mouseRotationY);
    TriangleList3D.add(new Triangle3D(p1,p2,p3,colour));
    Point3D v1 = new Point3D(p2o.x-p1o.x,p2o.y-p1o.y,p2o.z-p1o.z);
    Point3D v2 = new Point3D(p3o.x-p1o.x,p3o.y-p1o.y,p3o.z-p1o.z);
    Point3D n = new Point3D(v1.y*v2.z-v1.z*v2.y,v1.z*v2.x-v1.x*v2.z,v1.x*v2.y-v1.y*v2.x);
    TriangleList3D.get(TriangleList3D.size()-1).n = n;
  }

  /**
   * 
   * Adds an invisible hitbox.
   * 
   * @param max Max point
   * @param min Min point
   */
  public void addHitBox(Point3D max, Point3D min){
    HitBoxList.add(new Point3D[]{max,min});
  }

  /**
   * Displays stats.
   */
  public void displayStats(){
    fill(255, 255, 255);
    textSize(18);
    text("fps: " + Math.round(frameRate*100.0)/100.0, 20, 20);
    text("Mouse x Rotation: " + Math.round((mouseRotationX)*100.0)/100.0, 20, 50);
    text("Mouse y Rotation: " + Math.round((mouseRotationY)*100.0)/100.0, 20, 80);
    text("Player x: " + Math.round((playerXPos)*100.0)/100.0, displayWidth-170, 20);
    text("Player y: " + Math.round((playerYPos)*100.0)/100.0, displayWidth-170, 50);
    text("Player z: " + Math.round((playerZPos)*100.0)/100.0, displayWidth-170, 80);
    text("Player x vel: " + Math.round((playerXVel)*100.0)/100.0, displayWidth-170, 110);
    text("Player y vel: " + Math.round((playerYVel)*100.0)/100.0, displayWidth-170, 140);
    text("Player z vel: " + Math.round((playerZVel)*100.0)/100.0, displayWidth-170, 170);

  }

  /**
   * 
   * @param L worldspace position
   * @param colour colour of the light in terms of color(colour.x, colour.y, colour.z)
   * @param misc x = lightstrength, y = size of cube, z = spare variable(idk what to do with it)
   */
  public void addLight(Point3D L, Point3D colour, Point3D misc){
    LightSources.add(new Point3D[] {L,colour,misc});
    addCube(new Point3D(L.x+misc.y, L.y+misc.y, L.z+misc.y), 
            new Point3D(L.x-misc.y,L.y-misc.y,L.z-misc.y),
            colour,
            colour,
            colour,
            colour,
            colour,
            colour,
            false);
  }

  // The current scene. Objects added go in here.

  public void addScene(){
    Point3D color = new Point3D(255,255,255);
    // Cubes!
    addCube(new Point3D(-50,-50,50), 
            new Point3D(50,20,150),
            color,
            color,
            color,
            color,
            color,
            color,
            true);
    addCube(new Point3D(100,70,50), 
            new Point3D(200,140,150),
            color,
            color,
            color,
            color,
            color,
            color,
            true);
    addCube(new Point3D(200,190,-50), 
            new Point3D(300,260,-10),
            color,
            color,
            color,
            color,
            color,
            color,
            true);
    addCube(new Point3D(100,310,-150), 
            new Point3D(200,380,-50),
            color,
            color,
            color,
            color,
            color,
            color,
            true);
    addCube(new Point3D(-60,430,-150), 
            new Point3D(40,500,-50),
            color,
            color,
            color,
            color,
            color,
            color,
            true);
    addCube(new Point3D(-150,635+205*sin((float)Math.toRadians(platformH1)),-150), 
            new Point3D(-50,705+205*sin((float)Math.toRadians(platformH1)),-50),
            color,
            color,
            color,
            color,
            color,
            color,
            true);
    platformH1 += deltaTime*0.5;
    addCube(new Point3D(-250,830,-150), 
            new Point3D(-150,910,-50),
            color,
            color,
            color,
            color,
            color,
            color,
            true);
    // Lights!
    addLight(new Point3D(0,0,0), new Point3D(255,255,255), new Point3D(50,5,0));
    addLight(new Point3D(0,400,0), new Point3D(150,150,255), new Point3D(50,5,0));
    addLight(new Point3D(0,800,0), new Point3D(50,50,255), new Point3D(50,5,0));

  }
}