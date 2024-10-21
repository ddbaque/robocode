
package timidinrobot;

import java.util.Random;
import robocode.*;

public class MoveToCorner implements State {

  private TimidinRobot robot; // referència al TimidinRobot
  private int lastHitRobotId = -1; // Per a portar el seguiment de l'últim robot colpejat
  private int hitRobotCount = 0; // Contador de col·lisions amb el mateix robot
  private Random random = new Random();
  private double angleOffset = 40; // Offset d'angle per defecte

  private boolean scanCompleted = false; // Indica si el radar ha completat un escaneig de 360 graus

  public MoveToCorner(TimidinRobot timidinRobot) {
    this.robot = timidinRobot;
  }

  @Override
  public void run() {
    if (!scanCompleted) {
      robot.turnRadarRight(360); // Gira el radar 360 grados para escanear el campo
      robot.execute();
      return; // Esperar a que se complete el escaneo
    }

    // Calcula la direcció cap a la cantonada objectiu
    double dx = robot.targetX - robot.getX();
    double dy = robot.targetY - robot.getY();
    // Calcula l'angle que ha de girar el robot per posicionar-se de cara a la cantonada objectiu
    double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
    double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());

    // Gira el robot per posicionar-se de cara a la cantonada objectiu
    robot.setTurnRight(turnAngle);

    // Mueve el robot en línea recta hacia la esquina objetivo
    robot.setAhead(Math.hypot(dx, dy));

    // Alinear el cañón y el radar al final del movimiento con el "heading" del tanque
    alignGunAndRadarWithTank();

    // Si el robot arriba a la cantonada objectiu canvia a l'estat 2: Attack
    if (Math.hypot(dx, dy) < 40) {
      robot.setState(new Attack(robot));
    }

    robot.execute(); // Ejecutar las órdenes
  }

  // Método para alinear el cañón y el radar con el "heading" del tanque
  private void alignGunAndRadarWithTank() {
    // Alinear el cañón con el heading del tanque
    double gunTurnAngle = robot.normalizeBearing(robot.getHeading() - robot.getGunHeading());
    robot.setTurnGunRight(gunTurnAngle);

    // Alinear el radar con el cañón (que estará alineado con el tanque)
    double radarTurnAngle = robot.normalizeBearing(robot.getHeading() - robot.getRadarHeading());
    robot.setTurnRadarRight(radarTurnAngle);
    
    robot.execute(); // Ejecutar las órdenes de alineación
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {
    // Càlcul de la distància del robot a la cantonada objectiu
    double dx = robot.targetX - robot.getX();
    double dy = robot.targetY - robot.getY();

    double enemyBearing = e.getBearing();
    robot.setBack(50); // Retrocedeix una mica per a separar-se de l'enemic

    // Càlcul de l'angle que ha de girar canó per disparar a l'enemic amb el que hem col·lisionat
    double gunTurnAngle = robot.getHeading() + enemyBearing - robot.getGunHeading();
    // Gira el canó
    robot.setTurnGunRight(robot.normalizeBearing(gunTurnAngle));
    robot.fire(3); // Dispara a l'enemic

    // Alinear el cañón y el radar al final del evento
    alignGunAndRadarWithTank();

    // Comprovar si és el mateix robot que s'ha xocat abans
    if (e.getName().equals(robot.getOthers())) {
      hitRobotCount++;
    } else {
      // Reinicia el contador si és un robot diferent
      lastHitRobotId = e.getName().hashCode();
      hitRobotCount = 1;
    }

    // Si el robot ha col·lisionat amb el mateix enemic més d'un cop, fa un gir brusc
    if (hitRobotCount > 1) {
      robot.setTurnRight(180); // Girar 180 graus per escapar
      robot.setAhead(100); // Avançar cap endavant per a sortir del lloc
      robot.execute(); // Ejecutar las comandes
      return; // Sortir del mètode
    }

    // Decidir en quina direcció girar (esquerra o dreta) depenent de la possició relativa de
    // l'enemic (bearing)
    if (enemyBearing > 0) {
      robot.setTurnLeft(
          angleOffset + random.nextInt(45)); // Gira a l'esquerra amb un angle aleatori
    } else {
      robot.setTurnRight(angleOffset + random.nextInt(45)); // Gira a la dreta amb un angle aleatori
    }

    // Executar les comandes de moviment
    robot.execute();

    // Avançar una mica dessprés d'esquivar
    robot.ahead(100); // Es mou cap endavant per a separar-se de l'enemic

    // Alinear el cañón y el radar después de moverse
    alignGunAndRadarWithTank();
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Obté la mida del camp
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();

    // Obté la possició actual del robot
    double robotX = robot.getX();
    double robotY = robot.getY();

    // Definir un llindar per a considerar si el robot està en una cantonada
    double cornerThreshold = 30;

    // Verificar si el robot està en una cantonada
    boolean isInCorner =
        (robotX < cornerThreshold && robotY < cornerThreshold)
            || // Cantonada superior esquerra
            (robotX < cornerThreshold && robotY > battlefieldHeight - cornerThreshold)
            || // Cantonada inferior esquerra
            (robotX > battlefieldWidth - cornerThreshold && robotY < cornerThreshold)
            || // Cantonada superior dreta
            (robotX > battlefieldWidth - cornerThreshold
                && robotY > battlefieldHeight - cornerThreshold); // Cantonada inferior dreta

    if (isInCorner) {
      // Si és a la cantonada, no fer res (quedar-se quiet)
      robot.setTurnRight(0); // Assegurar  que no està girant
      robot.setAhead(0); // No moure's
      return; // Sortir del mètode per a evitar la lògica de gir
    }

    // Determina la direcció de gir en funció de la paret impactada
    double angleToTurn;
    double bearing = event.getBearing();

    if (bearing > 0) {
      // Si xoca amb la paret a la dreta, gira a l'esquerra
      angleToTurn = -90;
    } else {
      // Si xoca amb la paret a l'esquerra, gira a la dreta
      angleToTurn = 90;
    }

    // Gir no bloquejant cap a la direcció determinada
    robot.turnRight(angleToTurn);
    robot.ahead(100); // Avançar una mica després de girar
    robot.execute(); // Executar les comandes

    // Alinear el cañón y el radar después del evento de choque con la pared
    alignGunAndRadarWithTank();

    // Lògica per a sortir d'una situació en la que el robot es troba envoltat
    if (isSurrounded()) {
      robot.turnRight(180); // Girar 180 graus per a escapar
      robot.setAhead(100); // Avançar cap al davant per a sortir del lloc
      robot.execute(); // Ejecutar les comandes
    }
  }

  // Comprovar si el robot està envoltat
  private boolean isSurrounded() {
    return robot.getOthers() > 1; // si hi ha més d'un enemic visible
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    robot.out.println("robot escaneado" );
    // Si el robot detectado está a menos de 50 unidades de distancia, esquiva
    if (e.getDistance() < 100) {
      robot.back(10); // Retroceder una mica

      // Gira en direcció aleatòria per esquivar
      if (e.getBearing() > 0) {
        robot.setTurnLeft(angleOffset + random.nextInt(45)); // Gira a l'esquerra
      } else {
        robot.setTurnRight(angleOffset + random.nextInt(45)); // Gira a la dreta
      }
      robot.ahead(100); // Avanza para alejarse
      robot.execute(); // Ejecutar las órdenes

      // Alinear el cañón y el radar después de esquivar
      alignGunAndRadarWithTank();
    } 

    // Marcar que el escaneo ya ha sido completado después del primer enemigo escaneado
    scanCompleted = true;
  }
}
