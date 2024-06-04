public class Triangle3D {
    Point3D p1;
    Point3D p2;
    Point3D p3;
    Point3D p1o;
    Point3D p2o;
    Point3D p3o;
    Point3D c;
    Point3D n;
    Triangle3D(Point3D p1, Point3D p2, Point3D p3, Point3D colour){
      this.p1 = p1;
      this.p2 = p2;
      this.p3 = p3;
      Point3D a = new Point3D(p2.x-p1.x,p2.y-p1.y,p2.z-p1.z);
      Point3D b = new Point3D(p3.x-p1.x,p3.y-p1.y,p3.z-p1.z);
      this.n = new Point3D(a.y*b.z-a.z*b.y,a.z*b.x-a.x*b.z,a.x*b.y-a.y*b.x);
      this.c = colour;
    }
}