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

  // Funció per seguir la jerarquia
  private void followHierarchy() {
    if (robot.myRole.previous != null) {
      // Retard basat en el nivell de jerarquia de la llista de teamMembers
      int index =
          robot.teamMembers.indexOf(
              robot.myRole.previous); // Obtenir l'índex de la llista de teamMembers
      long delay = (index + 1) * 100; // 100 ms de retard per nivell de jerarquia

      long currentTime = System.currentTimeMillis();
      if (currentTime - lastPositionUpdate > delay) {
        // Calcular la distància a l'objectiu
        double dx = robot.target.x - robot.getX();
        double dy = robot.target.y - robot.getY();
        double distanceToTarget = Math.hypot(dx, dy);

        // Calcular la direcció normalitzada cap a l'objectiu
        double directionX = dx / distanceToTarget;
        double directionY = dy / distanceToTarget;

        // Nova posició objectiu a 100 unitats de distància de l'objectiu original
        double newTargetX = robot.target.x - directionX * 100; // Restar 100 unitats
        double newTargetY = robot.target.y - directionY * 100; // Restar 100 unitats

        // Calcular l'angle cap al nou objectiu
        double angleToTarget =
            Math.toDegrees(Math.atan2(newTargetX - robot.getX(), newTargetY - robot.getY()));

        // Girar cap al nou objectiu
        robot.setTurnRight(robot.normalizeBearing(angleToTarget - robot.getHeading()));

        // Avançar cap al nou objectiu
        robot.setAhead(Math.hypot(newTargetX - robot.getX(), newTargetY - robot.getY()));
        lastPositionUpdate =
            currentTime; // Actualitzar el temps de l'última actualització de posició
      }
    }
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // Cap acció per ara
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    // Gestionar quan el robot colpeja un altre robot (es pot afegir lògica addicional aquí)
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Gestionar quan el robot colpeja una paret (es pot afegir lògica addicional aquí)
  }

  @Override
  public void onMessageReceived(MessageEvent e) {
    // Els seguidors poden rebre missatges de la jerarquia
    robot.onMessageReceived(e);
  }

  @Override
  public void onRobotDeath(RobotDeathEvent e) {
    robot.onRobotDeath(e);
  }
}
