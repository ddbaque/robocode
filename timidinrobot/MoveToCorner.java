package timidinrobot;

import java.util.Random;
import robocode.*;

public class MoveToCorner implements State {

  private TimidinRobot robot;
  private int lastHitRobotId = -1; // Para llevar el seguimiento del último robot golpeado
  private int hitRobotCount = 0; // Contador de colisiones con el mismo robot
  private Random random = new Random();
  private double angleOffset = 40; // Offset de ángulo por defecto

  public MoveToCorner(TimidinRobot timidinRobot) {
    this.robot = timidinRobot;
  }

  @Override
  public void run() {
    double dx = robot.targetX - robot.getX();
    double dy = robot.targetY - robot.getY();
    double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
    double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());

    // Usar setTurnRight para giros no bloqueantes
    robot.setTurnRight(turnAngle);
    robot.setTurnRadarRight(robot.normalizeBearing(angleToTarget - robot.getRadarHeading()));

    // Usar setAhead para movimiento no bloqueante
    robot.setAhead(Math.hypot(dx, dy));

    if (Math.hypot(dx, dy) < 40) {
      robot.setState(new Attack(robot));
    }

    // Ejecutar comandos
    robot.execute();
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // Manejar cuando se escanea otro robot
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {
    double dx = robot.targetX - robot.getX();
    double dy = robot.targetY - robot.getY();
    double enemyBearing = e.getBearing();
    robot.setBack(50);

    // Primero, giramos hacia el enemigo para apuntar correctamente
    double gunTurnAngle = robot.getHeading() + enemyBearing - robot.getGunHeading();
    robot.setTurnGunRight(robot.normalizeBearing(gunTurnAngle));
    robot.fire(3); // Dispara al enemigo

    // Retrocedemos un poco para separarnos del enemigo

    // Chequear si es el mismo robot que se ha chocado antes
    if (e.getName().equals(robot.getOthers())) {
      hitRobotCount++;
    } else {
      // Reiniciar el contador si es un robot diferente
      lastHitRobotId = e.getName().hashCode();
      hitRobotCount = 1; // Reiniciar el conteo si es un robot nuevo
    }

    // Si el robot ha chocado con el mismo más de una vez, hacer un giro brusco
    if (hitRobotCount > 1) {
      robot.setTurnRight(180); // Girar 180 grados para escapar
      robot.setAhead(100); // Avanzar hacia adelante para salir del sitio
      robot.execute(); // Ejecutar los comandos
      return; // Salir del método
    }

    // Decidir en qué dirección girar: izquierda o derecha
    if (enemyBearing > 0) {
      robot.setTurnLeft(angleOffset + random.nextInt(45)); // Gira a la izquierda aleatoriamente
    } else {
      robot.setTurnRight(angleOffset + random.nextInt(45)); // Gira a la derecha aleatoriamente
    }

    // Ejecutar los comandos de movimiento
    robot.execute();

    // Avanzar un poco después de esquivar
    robot.ahead(100); // Mueve hacia adelante para separarse del enemigo
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Obtener las dimensiones del campo de batalla
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();

    // Obtener la posición actual del robot
    double robotX = robot.getX();
    double robotY = robot.getY();

    // Definir un umbral para considerar si el robot está en una esquina
    double cornerThreshold = 30; // Puedes ajustar este valor según sea necesario

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
      // Si está en la esquina, no hacer nada (quedarse quieto)
      robot.setTurnRight(0); // Asegúrate de que no esté girando
      robot.setAhead(0); // No moverse
      return; // Salir del método para evitar la lógica de giro
    }

    // Determina la dirección del giro en función de la pared impactada
    double angleToTurn;
    double bearing = event.getBearing();

    if (bearing > 0) {
      // Si choca con la pared a la derecha, gira a la izquierda
      angleToTurn = -90; // Girar a la izquierda
    } else {
      // Si choca con la pared a la izquierda, gira a la derecha
      angleToTurn = 90; // Girar a la derecha
    }

    // Girar no bloqueante hacia la dirección determinada
    robot.turnRight(angleToTurn);
    robot.ahead(100); // Avanzar un poco después de girar
    robot.execute(); // Ejecutar los comandos

    // Lógica para salir de una situación atrapada
    if (isSurrounded()) {
      robot.turnRight(180); // Girar 180 grados para escapar
      robot.setAhead(100); // Avanzar hacia adelante para salir del sitio
      robot.execute(); // Ejecutar los comandos
    }
  }

  // Método para comprobar si el robot está rodeado
  private boolean isSurrounded() {
    // Lógica para verificar si el robot está rodeado por paredes y otros robots
    // Puedes utilizar la posición actual del robot y la cantidad de enemigos visibles
    return robot.getOthers() > 1; // Por ejemplo, si hay más de un enemigo visible
  }
}
