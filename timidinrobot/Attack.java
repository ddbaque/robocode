package timidinrobot;

import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

// Move to corneer
public class Attack extends State {

  public Attack(TimidinRobot rob) {
    super(rob);
  }

  @Override
  public void run() {
    robot.turnRadarRight(360);
    if (robot.lastScannedRobot != null) {
      aimAndFire(robot.lastScannedRobot);
    }
    robot.execute();
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    robot.lastScannedRobot = e;
    aimAndFire(e);
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    // Handle the event when the robot hits another robot
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Handle the event when the robot hits a wall
  }

  @Override
  public void onBulletHit(BulletHitEvent event) {
    // Handle the event when the robot's bullet hits another robot
  }

  private void aimAndFire(ScannedRobotEvent e) {
    double gunTurnAngle = robot.getHeading() + e.getBearing() - robot.getGunHeading();
    robot.turnGunRight(robot.normalizeBearing(gunTurnAngle));

    double firePower = Math.min(400 / e.getDistance(), 3);
    robot.fire(firePower);
  }
}
