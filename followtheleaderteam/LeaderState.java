package followtheleaderteam;

import robocode.*;

public class LeaderState implements State {
  private FollowTheLeaderTeam robot;
  private double marginX;
  private double marginY;

  public LeaderState(FollowTheLeaderTeam robot) {
    this.robot = robot;

    // Calcular el 10% de margen del campo de batalla
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();
    this.marginX = battlefieldWidth * 0.1;
    this.marginY = battlefieldHeight * 0.1;
  }

  @Override
  public void run() {
    // Definir la posición objetivo con un margen de 10%
    double targetX = marginX;
    double targetY = marginY;

    // Calcular el ángulo hacia el objetivo
    double dx = targetX - robot.getX();
    double dy = targetY - robot.getY();
    double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));

    // Ajustar el ángulo de giro del radar y del robot
    double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());
    robot.setTurnRight(turnAngle);
    robot.setTurnRadarRight(robot.normalizeBearing(angleToTarget - robot.getRadarHeading()));

    // Avanzar hacia el objetivo
    double distanceToTarget = Math.hypot(dx, dy);
    robot.setAhead(distanceToTarget);
    robot.fire(1);

    // Ejecutar los comandos
    robot.execute();
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // No hace nada por ahora
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    robot.back(4000);
    // Manejar cuando el robot golpea a otro robot (se puede añadir lógica adicional aquí)
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Manejar cuando el robot golpea una pared (se puede añadir lógica adicional aquí)
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
