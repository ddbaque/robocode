package followtheleaderteam;

import robocode.*;
import java.awt.geom.Point2D;

public class LeaderState implements State {
  private FollowTheLeaderTeam robot;

  public LeaderState(FollowTheLeaderTeam robot) {
    this.robot = robot;
  }

  @Override
  public void run() {
    goToCenter();
  }

  private void goToCenter() {
    double x = 0; // El líder va al centro
    double y = 0;
    robot.goTo(x, y);
  }

  @Override
  public void onMessageReceived(MessageEvent e) {
    // El líder maneja mensajes de la jerarquía
    robot.onMessageReceived(e);
  }

  @Override
  public void onRobotDeath(RobotDeathEvent e) {
    robot.onRobotDeath(e);
  }
}
