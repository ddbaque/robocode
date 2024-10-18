package timidinrobot;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public class TimidinRobot extends AdvancedRobot {

  private State s;

  ScannedRobotEvent lastScannedRobot;
  double targetX;
  double targetY;

  @Override
  public void run() {
    setAdjustGunForRobotTurn(false);
    setAdjustRadarForRobotTurn(false);
    setAdjustRadarForGunTurn(false);

    setState(new Detection(this));

    while (true) {
      s.run();
    }
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    s.onScannedRobot(e);
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {
    s.onHitRobot(e);
  }

  @Override
  public void onHitWall(HitWallEvent e) {
    s.onHitWall(e);
  }

  public void setState(State state) {
    s = state;
  }

  public double normalizeBearing(double angle) {
    while (angle > 180) angle -= 360;
    while (angle < -180) angle += 360;
    return angle;
  }
}
