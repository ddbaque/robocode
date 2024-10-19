package timidinrobot;

import robocode.*;

public interface State {
  
  void run();

  void onScannedRobot(ScannedRobotEvent e);

  void onHitRobot(HitRobotEvent e);

  void onHitWall(HitWallEvent e);
}
