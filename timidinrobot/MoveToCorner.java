package timidinrobot;

import java.util.Random;
import robocode.*;

public class MoveToCorner implements State {

  private TimidinRobot robot;
  private Random random = new Random();
  private double angleOffset = 40; // Offset de ángulo por defecto
  private double dangerThreshold = 100; // Umbral de distancia para prever choques
  private double wallBuffer = 40; // Distancia mínima a las paredes

  public MoveToCorner(TimidinRobot timidinRobot) {
    this.robot = timidinRobot;
  }

  @Override
  public void run() {
    double dx = robot.targetX - robot.getX();
    double dy = robot.targetY - robot.getY();
    double distanceToTarget = Math.hypot(dx, dy);
    double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
    double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());

    // Girar hacia el objetivo
    robot.setTurnRight(turnAngle);

    // Verificar si hay enemigos en la trayectoria
    if (isEnemyInPath()) {
      avoidEnemy(); // Evitar al enemigo si está en la trayectoria
    } else {
      // Verificar distancia a las paredes
      avoidWalls();

      // Mover en línea recta hacia el objetivo
      robot.setAhead(distanceToTarget);
    }

    if (distanceToTarget < 40) {
      robot.setState(
          new Attack(robot)); // Cambiar al estado de ataque si estamos cerca del objetivo
    }

    // Ejecutar comandos
    robot.execute();
  }

  private boolean isEnemyInPath() {
    // Escanea hacia adelante para detectar enemigos dentro de un umbral
    for (int i = -90; i <= 90; i += 30) { // Escanear en un rango de -90 a 90 grados
      robot.setTurnRadarRight(30); // Mueve el radar en incrementos de 30 grados
      if (robot.getOthers() > 0) { // Asumiendo que siempre hay enemigos
        double enemyBearing = robot.getRadarHeading(); // Obtener la dirección del enemigo
        if (Math.abs(robot.normalizeBearing(enemyBearing - robot.getHeading())) < 30) {
          return true; // Hay un enemigo en la trayectoria
        }
      }
    }
    return false; // No hay enemigos en la trayectoria
  }

  private void avoidEnemy() {
    // Ajustar el movimiento para evitar al enemigo
    double enemyBearing = robot.getRadarHeading(); // Obtener el ángulo hacia el enemigo
    double turnAngle =
        enemyBearing > 0 ? -angleOffset : angleOffset; // Girar suavemente lejos del enemigo
    robot.setTurnRight(turnAngle);
    robot.setAhead(100); // Mover un poco hacia adelante para separarse
  }

  private void avoidWalls() {
    double robotX = robot.getX();
    double robotY = robot.getY();
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();

    // Calcular la distancia a cada pared
    double distanceToLeftWall = robotX - wallBuffer;
    double distanceToRightWall = battlefieldWidth - robotX - wallBuffer;
    double distanceToTopWall = robotY - wallBuffer;
    double distanceToBottomWall = battlefieldHeight - robotY - wallBuffer;

    // Ajustar dirección si está demasiado cerca de una pared
    if (distanceToLeftWall < 0
        || distanceToRightWall < 0
        || distanceToTopWall < 0
        || distanceToBottomWall < 0) {
      // Si está a punto de chocar, gira en dirección opuesta
      double angleToTurn =
          (distanceToLeftWall < 0)
              ? 90
              : (distanceToRightWall < 0) ? -90 : (distanceToTopWall < 0) ? 0 : 180;
      robot.setTurnRight(angleToTurn);
      robot.setAhead(50); // Mueve hacia adelante para alejarse de la pared
    }
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // Manejar el evento cuando se escanea otro robot
    // Podrías almacenar información del enemigo aquí si es necesario
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {
    double enemyBearing = e.getBearing();
    robot.setBack(50); // Retroceder al chocar con un robot

    // Girar hacia el enemigo para apuntar correctamente
    double gunTurnAngle = robot.getHeading() + enemyBearing - robot.getGunHeading();
    robot.setTurnGunRight(robot.normalizeBearing(gunTurnAngle));
    robot.fire(3); // Dispara al enemigo

    // Decidir en qué dirección girar: izquierda o derecha
    if (enemyBearing > 0) {
      robot.setTurnLeft(angleOffset + random.nextInt(45)); // Gira a la izquierda aleatoriamente
    } else {
      robot.setTurnRight(angleOffset + random.nextInt(45)); // Gira a la derecha aleatoriamente
    }

    robot.execute(); // Ejecutar los comandos
    robot.ahead(100); // Avanzar para separarse del enemigo
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Lógica de choque con la pared (igual que antes)
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();

    double robotX = robot.getX();
    double robotY = robot.getY();

    double cornerThreshold = 20; // Ajustar según sea necesario

    // Verificar si el robot está en una esquina
    boolean isInCorner =
        (robotX < cornerThreshold && robotY < cornerThreshold)
            || // Esquina superior izquierda
            (robotX < cornerThreshold && robotY > battlefieldHeight - cornerThreshold)
            || // Esquina inferior izquierda
            (robotX > battlefieldWidth - cornerThreshold && robotY < cornerThreshold)
            || // Esquina superior derecha
            (robotX > battlefieldWidth - cornerThreshold
                && robotY > battlefieldHeight - cornerThreshold); // Esquina inferior derecha

    if (isInCorner) {
      robot.setTurnRight(0); // Asegúrate de que no esté girando
      robot.setAhead(0); // No moverse
      return; // Salir del método
    }

    // Lógica de giro por impacto
    double angleToTurn;
    double bearing = event.getBearing();

    if (bearing > 0) {
      angleToTurn = -90; // Girar a la izquierda
    } else {
      angleToTurn = 90; // Girar a la derecha
    }

    robot.setTurnRight(angleToTurn);
    robot.setAhead(100); // Avanzar un poco después de girar
    robot.execute(); // Ejecutar los comandos
  }
}
