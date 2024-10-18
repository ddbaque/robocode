package timidinrobot;

import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public class Detection extends State {

  public Detection(TimidinRobot rob) {
    super(rob);
  }

  @Override
  public void run() {
    robot.setTurnRadarRight(360);
    robot.execute();
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    calculateFurthestCorner(e);
    robot.setState(new MoveToCorner(robot));
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

  private void calculateFurthestCorner(ScannedRobotEvent e) {
    // Obtener la posición del robot enemigo en el campo de batalla
    double enemyX =
        robot.getX()
            + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
    double enemyY =
        robot.getY()
            + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));

    // Dimensiones del campo de batalla
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();

    // Desplazamiento interno hacia dentro de las esquinas
    double offset = 15;

    // Definimos las coordenadas de las esquinas ajustadas con el desplazamiento
    double[] cornersX = {offset, offset, battlefieldWidth - offset, battlefieldWidth - offset};
    double[] cornersY = {offset, battlefieldHeight - offset, offset, battlefieldHeight - offset};

    // Buscar la esquina más alejada del enemigo
    double maxDistance = -1;
    for (int i = 0; i < cornersX.length; i++) {
      double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
      if (distance > maxDistance) {
        maxDistance = distance;
        robot.targetX = cornersX[i];
        robot.targetY = cornersY[i];
      }
    }
  }
}
