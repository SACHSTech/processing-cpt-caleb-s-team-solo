public class Triangle2D {
    Point2D p1;
    Point2D p2;
    Point2D p3;
    Point3D c;
    Point3D n;
    Triangle2D(Point2D point1, Point2D point2, Point2D point3, Point3D normal, Point3D colour){
      this.p1 = point1;
      this.p2 = point2;
      this.p3 = point3;
      this.c = colour;
      this.n = normal;
    }
}
