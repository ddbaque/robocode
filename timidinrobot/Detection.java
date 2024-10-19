package timidinrobot;

import robocode.*;

public class Detection implements State {
  
  private TimidinRobot robot;

  public Detection(TimidinRobot timidinRobot) {
    this.robot = timidinRobot;
  }

  @Override
  public void run() {
    // Usar setTurnRadarRight para girar el radar de manera no bloqueante
    robot.setTurnRadarRight(360);
    robot.execute();  // Ejecutar los comandos no bloqueantes
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // Calcular la esquina más lejana al detectar un robot
    calculateFurthestCorner(e);
    robot.setState(new MoveToCorner(robot));  // Cambiar a estado MoveToCorner
  }

  private void calculateFurthestCorner(ScannedRobotEvent e) {
    // Calcular las coordenadas del enemigo en función de su distancia y ángulo
    double enemyX =
        robot.getX()
            + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
    double enemyY =
        robot.getY()
            + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));

    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();
    double offset = 15;

    // Coordenadas de las cuatro esquinas del campo de batalla con un pequeño margen de seguridad
    double[] cornersX = {offset, offset, battlefieldWidth - offset, battlefieldWidth - offset};
    double[] cornersY = {offset, battlefieldHeight - offset, offset, battlefieldHeight - offset};

    double maxDistance = -1;
    // Buscar la esquina más lejana al enemigo
    for (int i = 0; i < cornersX.length; i++) {
      double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
      if (distance > maxDistance) {
        maxDistance = distance;
        robot.targetX = cornersX[i];  // Actualizar la esquina objetivo X
        robot.targetY = cornersY[i];  // Actualizar la esquina objetivo Y
      }
    }
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    // Manejar cuando el robot golpea a otro robot (se puede añadir lógica adicional aquí)
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Manejar cuando el robot golpea una pared (se puede añadir lógica adicional aquí)
  }
}
