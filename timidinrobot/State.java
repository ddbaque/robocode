package timidinrobot;

import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public abstract class State {
  public TimidinRobot robot;

  public State(TimidinRobot r) {
    robot = r;
  }

  abstract void run();

  abstract void onScannedRobot(ScannedRobotEvent e);

  abstract void onHitRobot(HitRobotEvent e);

  abstract void onHitWall(HitWallEvent e);
}
