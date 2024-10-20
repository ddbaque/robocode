package timidinrobot;

import robocode.*;

public interface State {
  
  // lògica principal de l'estat
  void run();

  // lògica per quan el radar detecta un robot enemic
  void onScannedRobot(ScannedRobotEvent e);

  // lògica per quan el robot col·lideix amb un altre robot
  void onHitRobot(HitRobotEvent e);

  // lògica per quan el robot colpeja una paret
  void onHitWall(HitWallEvent e);
}
