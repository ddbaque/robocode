package followtheleaderteam;

import java.awt.geom.Point2D;

class HierarchyMember implements java.io.Serializable {
  String name;
  Point2D.Double position;
  HierarchyMember previous;

  HierarchyMember(String name, Point2D.Double position) {
    this.name = name;
    this.position = position;
  }
}
