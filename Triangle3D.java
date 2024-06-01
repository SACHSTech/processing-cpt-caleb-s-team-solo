public class Triangle3D {
    Point3D p1;
    Point3D p2;
    Point3D p3;
    Point3D c;
    Point3D n;
    Triangle3D(Point3D p1, Point3D p2, Point3D p3, Point3D normal, Point3D colour){
      this.p1 = p1;
      this.p2 = p2;
      this.p3 = p3;
      this.c = colour;
      this.n = normal;
    }
}
