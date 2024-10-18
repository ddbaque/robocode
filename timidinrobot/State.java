package timidinrobot;

import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public abstract class State {
  public TimidinRobot robot;

  public State(TimidinRobot rob) {
    robot = rob;
  }

  abstract void run();

  abstract void onScannedRobot(ScannedRobotEvent event);

  abstract void onHitRobot(HitRobotEvent event);

  abstract void onHitWall(HitWallEvent event);

  abstract void onBulletHit(BulletHitEvent event);
}
