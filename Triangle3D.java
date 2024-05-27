public class Triangle3D {
    Point3D p1;
    Point3D p2;
    Point3D p3;
    int c;
    boolean rendered;
    Triangle3D(Point3D point1, Point3D point2, Point3D point3, int colour){
      this.p1 = point1;
      this.p2 = point2;
      this.p3 = point3;
      this.c = colour;
    }
}
