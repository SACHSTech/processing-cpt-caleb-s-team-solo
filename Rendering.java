import processing.core.PApplet;
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
  int intScreenSize = 600;
  // 600 instead of 800 because theres 56% less max space to loop through, which means that its far less laggy with only a 200 pixel reduction
  int scDiv2 = intScreenSize/2; // used this so much that i might as well make it a variable

  // Scene stuff
  double cameraX = 0;
  double cameraY = 0;
  double cameraZ = 0;
  double mouseRotationX = 0;
  double mouseRotationY = 0;
  double mouseCenteredX = 0;
  double mouseCenteredY = 0;
  double mouseSensitivity = 180;
  double[] zBuffer = new double[intScreenSize*intScreenSize];
  List<Triangle3D> TriangleList3D = new ArrayList<>();
  List<BlankTriangle3D> HitBoxList = new ArrayList<>(); // used for shadows and used for hitboxes
  List<Triangle2D> TriangleList2D = new ArrayList<>();
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
  double jumpHeight = 5;

  // Other stuff
  double deltaTime = 60/frameRate;
  boolean[] isKeyPressed = new boolean[256];
  boolean hasJump = true;
  Robot mouseStealer9000;
  int ID = 0;

  public void settings() {
    size(intScreenSize, intScreenSize);
  }

  public void setup() {

    try {
      mouseStealer9000 = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
    noCursor();
    mouseStealer9000.mouseMove(displayWidth/2, displayHeight/2);
  }

  // Initialization ends here
  double lightY = 0; 
  public void draw() {
    // Makes sure deltaTime never goes > 1(random accelleration, higher than expected speed, ect)
    if(frameRate > 60){
      frameRate = 60;
    }
    deltaTime = 60/frameRate;
    ID = 0;
    clear();
    cursorMovement();
    moveChar();
    renderInOrder();
    //projectPoints();
    drawLines(); // For testing purposes only
  }

  // 3D framework starts here

  public void drawLines(){
    float a = scDiv2;
    for(int x = 0; x < TriangleList2D.size(); x++){
      float x1 = a+(float)TriangleList2D.get(x).p1.x;
      float x2 = a+(float)TriangleList2D.get(x).p2.x;
      float x3 = a+(float)TriangleList2D.get(x).p3.x;
      float y1 = a+(float)TriangleList2D.get(x).p1.y;
      float y2 = a+(float)TriangleList2D.get(x).p2.y;
      float y3 = a+(float)TriangleList2D.get(x).p3.y;
      line(x1,y1,x2,y2);
      line(x2,y2,x3,y3);
      line(x3,y3,x1,y1);
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

    // Equation made by me strugling way too much on desmos
    coords[order[0]] = ((coordsList[1][0]-b-m*a)*coordsList[0][1]-(coordsList[1][1]-b-m*a)*coordsList[0][0])/(m*coordsList[0][1]-m*coordsList[0][0]-coordsList[1][1]+coordsList[1][0]);
    // Litterally just y = mx except no +b because the FoV is always centered on the 0,0,0 camera.
    coords[order[1]] = m*(coords[order[0]]+a)+b;
    // Find how far the new point is in terms of a % between the other two points, find the z value at that % between the two zs.
    // Similar concept to how z is found for a given point on a triangle
    // Actually pretty simple, the code is just long
    coords[order[2]] = coordsList[2][0]+(coordsList[2][1]-coordsList[2][0])*Math.sqrt((Math.pow((coords[order[0]]-coordsList[0][0]),2)+Math.pow((coords[order[1]]-coordsList[1][0]),2))/(Math.pow((coordsList[0][1]-coordsList[0][0]),2)+Math.pow((coordsList[1][1]-coordsList[1][0]),2)));

    return new Point3D(coords[0],coords[1],coords[2]);
  }

  public void cullTriangles(double m, double b, double a, int[] order, int sign){
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
      int thisID = TriangleList3D.get(count).id;
      Point3D c = TriangleList3D.get(count).c;
      List<Point3D> whatsOut = new ArrayList<>();
      List<Point3D> whatsIn = new ArrayList<>();
      // CHECK 1
      for(int count2 = 0; count2 < 3; count2++){
        if(sign*PList[1][count2] > sign*(((PList[0][count2]+a)*m)+b)){
          whatsOut.add(new Point3D(x[count2], y[count2], z[count2]));
        }
        else{
          whatsIn.add(new Point3D(x[count2], y[count2], z[count2]));
        }
      }
      // Do nothing if size == 3(means all points are out and triangle doesnt exist)
      if(whatsOut.size() == 2){
        Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), m,b, a,order);
        Point3D PoI2 = findPoI(whatsOut.get(1), whatsIn.get(0), m,b, a,order);
        TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c, thisID));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
      }
      else if(whatsOut.size() == 1){
        Point3D PoI1 = findPoI(whatsOut.get(0), whatsIn.get(0), m,b, a,order);
        Point3D PoI2 = findPoI(whatsOut.get(0), whatsIn.get(1), m,b, a,order);
        TriangleList3DTemp.add(new Triangle3D(PoI1,PoI2,whatsIn.get(0),c,thisID));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
        TriangleList3DTemp.add(new Triangle3D(whatsIn.get(0),whatsIn.get(1),PoI2,c,thisID));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
      }
      else if(whatsOut.size() == 0){
        // Keep the same triangle
        TriangleList3DTemp.add(TriangleList3D.get(count));
        TriangleList3DTemp.get(TriangleList3DTemp.size()-1).n = normal;
      }
    }
    TriangleList3D.clear();
    for(int x = 0; x < TriangleList3DTemp.size(); x++){
      TriangleList3D.add(TriangleList3DTemp.get(x));
    }
    TriangleList3DTemp.clear();
  }

  public void projectPoints() {
    // So that everything fits into the 2d space
    frustrumCull();

    TriangleList2D.clear();
    for(int a = 0; a < TriangleList3D.size(); a++){
      double[] x = {TriangleList3D.get(a).p1.x, TriangleList3D.get(a).p2.x, TriangleList3D.get(a).p3.x};
      double[] y = {TriangleList3D.get(a).p1.y, TriangleList3D.get(a).p2.y, TriangleList3D.get(a).p3.y};
      double[] z = {TriangleList3D.get(a).p1.z, TriangleList3D.get(a).p2.z, TriangleList3D.get(a).p3.z};
      int thisID = TriangleList3D.get(a).id;

      for(int count = 0; count < 3; count++){
        x[count] = -(((x[count]-cameraX) * (dblFocalLength)) / ((z[count]-cameraZ)));
        y[count] = -(((y[count]-cameraY) * (dblFocalLength)) / ((z[count]-cameraZ)));
      }
      TriangleList2D.add(new Triangle2D(new Point2D(x[0], y[0], TriangleList3D.get(a).p1.z), new Point2D(x[1],y[1],TriangleList3D.get(a).p2.z), new Point2D(x[2],y[2],TriangleList3D.get(a).p3.z), TriangleList3D.get(a).n, TriangleList3D.get(a).c, thisID));
    }
  }

  public Point3D ReverseRotatePoint(Point3D P, double angleXZ, double angleXY, double angleYZ) {
    angleXZ = Math.toRadians(angleXZ);
    angleXY = Math.toRadians(angleXY);
    angleYZ = Math.toRadians(angleYZ);
    double x;
    double z;
    double y;
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

  public double area(double x1, double y1, double x2, double y2, double x3, double y3){
    return Math.abs((x1*(y2-y3) + x2*(y3-y1)+x3*(y1-y2))/2.0);
  }

  public double interpolateDepth(Point2D point, Point2D[] vertices, double[] depths) {
    Point2D p1 = vertices[0];
    Point2D p2 = vertices[1];
    Point2D p3 = vertices[2];
    double tA = area(p1.x,p1.y,p2.x,p2.y,p3.x,p3.y);
    double l1 = area(p1.x,p1.y,p2.x,p2.y,point.x,point.y)/tA;
    double l2 = area(p1.x,p1.y,p3.x,p3.y,point.x,point.y)/tA;
    double l3 = area(p3.x,p3.y,p2.x,p2.y,point.x,point.y)/tA;
    double interpolatedDepth = 1/(l1*(1/(depths[2]-cameraZ))+l2*(1/(depths[1]-cameraZ))+l3*(1/(depths[0]-cameraZ))) + cameraZ;
    return interpolatedDepth;
  }

  public void frustrumCull(){
    cullTriangles(intScreenSize/(2*dblFocalLength),cameraX,-cameraZ, new int[]{2,0,1},1);
    cullTriangles(-intScreenSize/(2*dblFocalLength),cameraX, -cameraZ, new int[]{2,0,1},-1);
    cullTriangles(intScreenSize/(2*dblFocalLength),cameraY, -cameraZ, new int[]{2,1,0},1);
    cullTriangles(-intScreenSize/(2*dblFocalLength),cameraY, -cameraZ, new int[]{2,1,0},-1);
  }

  public boolean lineIntersectsAllTriangles(Point3D p1, Point3D p2, int faceNumber){
    boolean intersects = false;
    for(int x = 0; x < HitBoxList.size(); x++){
      // Prevents it coliding with itself
      if(HitBoxList.get(x).id != faceNumber){
        Point3D v1 = HitBoxList.get(x).p1;
        Point3D v2 = HitBoxList.get(x).p2;
        Point3D v3 = HitBoxList.get(x).p3;
        double v = area(v1.x,v1.y,v2.x,v2.y,v3.x,v3.y);
      }
    }
    return intersects;
  }

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
      int faceNumber = TriangleList2D.get(r).id;
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
                double diffuseLightFactor = Math.abs(lightDirection.x*newNormal.x+lightDirection.y*newNormal.y+lightDirection.z*newNormal.z);
                if(Math.toDegrees(Math.acos(diffuseLightFactor)) > 180){
                  diffuseLightFactor = Math.cos(Math.acos(diffuseLightFactor-90)); 
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
              //int c = color((int)(TriangleList2D.get(r).c.x), (int)(TriangleList2D.get(r).c.y), (int)(TriangleList2D.get(r).c.z));
              int c = color((int)pColor.x,(int)pColor.y,(int)pColor.z);
              set(scDiv2+x,(scDiv2+y)+1,c);
            }
          }
        }
      }
    }
    // border for the screen cause coulors bunch near the edge cause of my subpar edge culling skills
    for(int x = 0; x < intScreenSize; x++){
      set(0,x,color(0,0,0));
      set(x,intScreenSize-1,color(0,0,0));
      set(intScreenSize-2,x,color(0,0,0));
    }
  }

  public void renderInOrder(){
    // Clear all lists and buffers
    TriangleList2D.clear();
    TriangleList3D.clear();
    LightSources.clear();
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

  // Movement / other game elements start here

  public void cursorMovement(){
    if(mouseCenteredX == 0 && mouseCenteredY == 0){
      mouseCenteredX = mouseX;
      mouseCenteredY = mouseY;
    }
    mouseRotationX += (mouseSensitivity*(mouseX - mouseCenteredX))/(scDiv2);
    mouseRotationY -= (mouseSensitivity*(mouseY - mouseCenteredY))/(scDiv2);
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
    if(key != 65535){
      isKeyPressed[(int)Character.toLowerCase(key)] = true;
    }
    else if(key == CODED && keyCode == SHIFT){
      isKeyPressed[255] = true;
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

  public void moveChar() {
    // If statements. Lots of em.
    double rotA = Math.toRadians(mouseRotationY);
    double rotB = Math.toRadians(mouseRotationX);
    double a = Math.toRadians(90);
    double zVelStore = 0;
    double xVelStore = 0;
    if(isKeyPressed[(int)'w']){
      zVelStore += playerSpeed*Math.cos(rotB);
      xVelStore += -playerSpeed*Math.sin(rotB);

      // Commented code for "flight" while testing
      //cameraZ += playerSpeed*(60/frameRate)*Math.cos(rotA)*Math.cos(rotB);
      //cameraX += playerSpeed*(60/frameRate)*Math.sin(rotA+a)*Math.cos(rotB+a);
      //cameraY += playerSpeed*(60/frameRate)*Math.sin(rotA);
    }
    if(isKeyPressed[(int)'s']){
      zVelStore += -playerSpeed*Math.cos(rotB);
      xVelStore += playerSpeed*Math.sin(rotB);
      //cameraZ -= playerSpeed*(60/frameRate)*Math.cos(rotA)*Math.cos(rotB);
      //cameraX -= playerSpeed*(60/frameRate)*Math.sin(rotA+a)*Math.cos(rotB+a);
      //cameraY -= playerSpeed*(60/frameRate)*Math.sin(rotA);
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
      playerSpeed = 2;
    }
    // No running off the map :(
    if(playerXPos > 950){
      playerXPos = 950;
    }
    if(playerXPos < -950){
      playerXPos = -950;
    }
    if(playerZPos > 950){
      playerZPos = 950;
    }
    if(playerZPos < -950){
      playerZPos = -950;
    }

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
    

    if(cameraY > 100 - playerHeight){
      playerYVel -= 0.2*(60/frameRate);
    }
    else if(cameraY < 100 - playerHeight){
      playerYVel = 0;
      playerYPos = 100 - playerHeight;
      hasJump = true;
    }
    else{
      playerYVel = 0;
      hasJump = true;
    }

    // Camera following
    cameraX = playerXPos;
    cameraZ = playerZPos;
    cameraY = playerYPos;

  }

  // Objects are defined here

  public void addCube(Point3D minPoint, Point3D maxPoint, Point3D colour1, Point3D colour2, Point3D colour3, Point3D colour4, Point3D colour5, Point3D colour6){
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
      PointList[x*2].x = maxX;
      PointList[x*2+1].x = minX;
      PointList[(x/2)*2+x].y = maxY;
      PointList[(x/2)*2+x+2].y = minY;
      PointList[x].z = minZ;
      PointList[x+4].z = maxZ;
    }
    // Pre-rotation, for hitboxes
    for(int a = 0; a < Connections.length; a++){
      Point3D p1 = PointList[Connections[a][0]];
      Point3D p2 = PointList[Connections[a][1]];
      Point3D p3 = PointList[Connections[a][2]];
      HitBoxList.add(new BlankTriangle3D(p1,p2,p3,ID));
      ID++;
    }
    // reset id for later use
    ID -= Connections.length;
    for(int x = 0; x < 8; x++){
      PointList[x] = rotatePoint(PointList[x], -mouseRotationX, 0, -mouseRotationY);
    }
    // Post-rotation, for the display itself.
    for(int a = 0; a < Connections.length; a++){
      Point3D p1 = PointList[Connections[a][0]];
      Point3D p2 = PointList[Connections[a][1]];
      Point3D p3 = PointList[Connections[a][2]];
      Point3D v1 = new Point3D(p2.x-p1.x,p2.y-p1.y,p2.z-p1.z);
      Point3D v2 = new Point3D(p3.x-p1.x,p3.y-p1.y,p3.z-p1.z);
      Point3D p1o = ReverseRotatePoint(p1,-mouseRotationX, 0, -mouseRotationY);
      Point3D p2o = ReverseRotatePoint(p2,-mouseRotationX, 0, -mouseRotationY);
      Point3D p3o = ReverseRotatePoint(p3,-mouseRotationX, 0, -mouseRotationY);
      Point3D v1o = new Point3D(p2o.x-p1o.x,p2o.y-p1o.y,p2o.z-p1o.z);
      Point3D v2o = new Point3D(p3o.x-p1o.x,p3o.y-p1o.y,p3o.z-p1o.z);
      if((((p1.x-cameraX) * dblFocalLength)/((p1.z-cameraZ)))*(((p2.y-cameraY) * dblFocalLength)/((p2.z-cameraZ)))-(((p1.y-cameraY) * dblFocalLength)/((p1.z-cameraZ)))*(((p2.x-cameraX) * dblFocalLength)/((p2.z-cameraZ))) < 0){
        TriangleList3D.add(new Triangle3D(p1,p2,p3,colourList[(int)a/2],ID));
        ID++;
        TriangleList3D.get(TriangleList3D.size()-1).n = new Point3D(Math.round((v1o.y*v2o.z-v1o.z*v2o.y)*100.0)/100.0,Math.round((v1o.z*v2o.x-v1o.x*v2o.z)*100.0)/100.0,Math.round((v1o.x*v2o.y-v1o.y*v2o.x)*100.0)/100.0);
      }
    }
  }

  public void addTriangle(Point3D p1o, Point3D p2o, Point3D p3o, Point3D colour){  
    Point3D p1 = rotatePoint(p1o, -mouseRotationX, 0, -mouseRotationY);
    Point3D p2 = rotatePoint(p2o, -mouseRotationX, 0, -mouseRotationY);
    Point3D p3 = rotatePoint(p3o, -mouseRotationX, 0, -mouseRotationY);
    TriangleList3D.add(new Triangle3D(p1,p2,p3,colour, ID));
    HitBoxList.add(new BlankTriangle3D(p1o, p2o, p3o, ID));
    ID++;
    Point3D v1 = new Point3D(p2o.x-p1o.x,p2o.y-p1o.y,p2o.z-p1o.z);
    Point3D v2 = new Point3D(p3o.x-p1o.x,p3o.y-p1o.y,p3o.z-p1o.z);
    TriangleList3D.get(TriangleList3D.size()-1).n = new Point3D(v1.y*v2.z-v1.z*v2.y,v1.z*v2.x-v1.x*v2.z,v1.x*v2.y-v1.y*v2.x);
  }

  public void addLight(Point3D L, Point3D colour, Point3D misc){
    //L: worldspace position
    //colour: colour of the light in terms of color(colour.x, colour.y, colour.z)
    //misc: x = lightstrength, y = size of cube, z = blank
    LightSources.add(new Point3D[] {L,colour,misc});
    addCube(new Point3D(L.x+misc.y, L.y+misc.y, L.z+misc.y), 
            new Point3D(L.x-misc.y,L.y-misc.y,L.z-misc.y), 
            colour,
            colour,
            colour,
            colour,
            colour,
            colour);
  }

  // The current scene. Objects added go in here.
  public void addScene(){
    // Floor!
    addTriangle(new Point3D(1000, -100, 1000), 
                new Point3D(1000, -100, -1000), 
                new Point3D(-1000,-100,1000),
                new Point3D(100,30,156));
    addTriangle(new Point3D(-1000, -100, 1000), 
                new Point3D(-1000, -100, -1000), 
                new Point3D(1000,-100,-1000),
                new Point3D(100,30,156));

    // Cubes!
    addCube(new Point3D(200,-100,200), 
            new Point3D(400,100,400), 
            new Point3D(130,248,120),
            new Point3D(130,248,120),
            new Point3D(130,248,120),
            new Point3D(130,248,120),
            new Point3D(130,248,120),
            new Point3D(130,248,120));

    // Lights!
    addLight(new Point3D(300,200,100), new Point3D(255,255,255), new Point3D(200,5,0));
  }
}