package followtheleaderteam;

import robocode.*;

public interface State {
  void run();

  void onScannedRobot(ScannedRobotEvent e);

  void onHitRobot(HitRobotEvent e);

  void onHitWall(HitWallEvent e);

  void onMessageReceived(MessageEvent e);

  void onRobotDeath(RobotDeathEvent e);
}
