package followtheleaderteam;

import robocode.*;
import java.awt.geom.Point2D;

public class FollowerState implements State {
  private FollowTheLeaderTeam robot;

  public FollowerState(FollowTheLeaderTeam robot) {
    this.robot = robot;
  }

  @Override
  public void run() {
    followHierarchy();
  }

  private void followHierarchy() {
    if (robot.myRole.previous != null) {
      Point2D.Double target = robot.myRole.previous.position;
      double distanceToTarget = 100; // Distancia de "seguimiento"
      robot.goTo(target.getX(), target.getY(), distanceToTarget);
    }
  }

  @Override
  public void onMessageReceived(MessageEvent e) {
    // Los seguidores pueden recibir mensajes de la jerarquía
    robot.onMessageReceived(e);
  }

  @Override
  public void onRobotDeath(RobotDeathEvent e) {
    robot.onRobotDeath(e);
  }
}
