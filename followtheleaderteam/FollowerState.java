package followtheleaderteam;

import java.awt.geom.Point2D;
import robocode.*;

public class FollowerState implements State {
  private FollowTheLeaderTeam robot;

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
      Point2D.Double target = robot.myRole.previous.position;
    }
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {}

  @Override
  public void onHitRobot(HitRobotEvent event) {
    // Manejar cuando el robot golpea a otro robot (se puede añadir lógica adicional aquí)
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Manejar cuando el robot golpea una pared (se puede añadir lógica adicional aquí)
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
