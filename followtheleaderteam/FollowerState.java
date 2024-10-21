package followtheleaderteam;

import robocode.*;

public class FollowerState implements State {
  private FollowTheLeaderTeam robot;
  private long lastPositionUpdate = 0;

  public FollowerState(FollowTheLeaderTeam robot) {
    this.robot = robot;
  }

  @Override
  public void run() {
    followHierarchy();
    robot.execute();
  }

  private void followHierarchy() {
    if (robot.myRole.previous != null) {
      // Delay based on the hierarchy level from the teamMembers list
      int index =
          robot.teamMembers.indexOf(robot.myRole.previous); // Get index from the teamMembers list
      long delay = (index + 1) * 100; // 100 ms delay per hierarchy level

      long currentTime = System.currentTimeMillis();
      if (currentTime - lastPositionUpdate > delay) {
        // Calculate distance to target
        double dx = robot.target.x - robot.getX();
        double dy = robot.target.y - robot.getY();
        double distanceToTarget = Math.hypot(dx, dy);

        // Calculate the normalized direction to the target
        double directionX = dx / distanceToTarget;
        double directionY = dy / distanceToTarget;

        // New target position 100 points away from the original target
        double newTargetX = robot.target.x - directionX * 100; // Subtracting 100 points
        double newTargetY = robot.target.y - directionY * 100; // Subtracting 100 points

        // Calculate the angle to the new target
        double angleToTarget =
            Math.toDegrees(Math.atan2(newTargetX - robot.getX(), newTargetY - robot.getY()));

        // Turn towards the new target
        robot.setTurnRight(robot.normalizeBearing(angleToTarget - robot.getHeading()));

        // Move towards the new target
        robot.setAhead(Math.hypot(newTargetX - robot.getX(), newTargetY - robot.getY()));
        lastPositionUpdate = currentTime; // Update last position update time
      }
    }
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // No action for now
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    // Handle when the robot hits another robot (additional logic can be added here)
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Handle when the robot hits a wall (additional logic can be added here)
  }

  @Override
  public void onMessageReceived(MessageEvent e) {
    // Followers can receive hierarchy messages
    robot.onMessageReceived(e);
  }

  @Override
  public void onRobotDeath(RobotDeathEvent e) {
    robot.onRobotDeath(e);
  }
}
