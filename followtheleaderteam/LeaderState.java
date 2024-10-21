package followtheleaderteam;

import java.awt.geom.Point2D;
import java.io.IOException;
import robocode.*;

public class LeaderState implements State {

  private FollowTheLeaderTeam robot;
  private long startTime;
  private long lastScanTime; // Seguiment del temps de l'última exploració
  private double safeMarginX;
  private double safeMarginY;
  private double[][] corners; // Matriu per contenir les coordenades de les cantonades
  private int currentCornerIndex = 0; // Seguiment de la cantonada cap on ens estem movent
  private static final double ESCAPE_DISTANCE = 100; // Distància per escapar de robots detectats
  private static final double ANGLE_OFFSET = 40; // Desplaçament angular per l'evitació

  public LeaderState(FollowTheLeaderTeam robot) {
    this.robot = robot;

    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();

    // Calcular marges de seguretat del 10%
    this.safeMarginX = battlefieldWidth * 0.1;
    this.safeMarginY = battlefieldHeight * 0.1;

    // Definir les cantonades tenint en compte el marge de seguretat
    this.corners =
        new double[][] {
          {safeMarginX, safeMarginY}, // Cantonada (0,0)
          {safeMarginX, battlefieldHeight - safeMarginY}, // Cantonada (0,1)
          {battlefieldWidth - safeMarginX, battlefieldHeight - safeMarginY}, // Cantonada (1,1)
          {battlefieldWidth - safeMarginX, safeMarginY} // Cantonada (1,0)
        };

    this.startTime = System.currentTimeMillis();
    this.lastScanTime = System.currentTimeMillis(); // Inicialitzar l'últim temps d'exploració
  }

  @Override
  public void run() {
    // Comprovar si han passat 15 segons per invertir la jerarquia
    if (System.currentTimeMillis() - startTime > 15000) {
      try {
        robot.broadcastMessage("REVERSE_HIERARCHY");
      } catch (IOException e) {
        robot.out.println("Error enviant missatge d'inversió: " + e.getMessage());
      }
      robot.reverseHierarchy();
      return;
    }
    if (System.currentTimeMillis() - lastScanTime > 4000) {
      lastScanTime = System.currentTimeMillis();
      robot.setTurnRadarRight(360); // Escanejar amb el radar
    }
    // Obtenir les coordenades de la cantonada objectiu actual
    double targetX = corners[currentCornerIndex][0];
    double targetY = corners[currentCornerIndex][1];

    // Calcular l'angle cap a l'objectiu
    double dx = targetX - robot.getX();
    double dy = targetY - robot.getY();
    double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));

    // Girar cap a l'objectiu
    double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());
    robot.turnRight(turnAngle);

    // Avançar cap a l'objectiu
    double distanceToTarget = Math.hypot(dx, dy);
    robot.setAhead(distanceToTarget);

    // Comprovar si hem arribat a la cantonada objectiu
    if (distanceToTarget < 20) { // Ajustar aquest llindar si és necessari
      currentCornerIndex =
          (currentCornerIndex + 1) % corners.length; // Passar a la següent cantonada
    }

    // Enviar la posició actual als seguidors
    robot.reportPosition();

    robot.execute();
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    String scannedRobotName = e.getName();
    if (robot.isTeammate(scannedRobotName)) {
      return; // Ignorar companys d'equip
    }
    // Si un robot escanejat està a prop, evadir
    if (e.getDistance() < ESCAPE_DISTANCE) {
      evade(e);
    } else {
      // Emmagatzemar la posició de l'enemic quan es detecta
      double enemyX =
          robot.getX()
              + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
      double enemyY =
          robot.getY()
              + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));
      robot.enemyPosition = new Point2D.Double(enemyX, enemyY);

      // Enviar la posició de l'enemic als seguidors
      try {
        robot.broadcastMessage(robot.enemyPosition);
      } catch (IOException ex) {
        robot.out.println("Error enviant la posició de l'enemic: " + ex.getMessage());
      }
      double dx = enemyX - robot.getX();
      double dy = enemyY - robot.getY();
      double angleToEnemy = Math.toDegrees(Math.atan2(dx, dy));
      double turnGun = robot.normalizeBearing(angleToEnemy - robot.getGunHeading());

      // Girar la pistola cap a l'enemic
      robot.setTurnGunRight(turnGun);

      // Disparar segons la distància a l'enemic
      double distanceToEnemy = e.getDistance();
      if (distanceToEnemy < 150) {
        robot.setFire(3); // Alta potència si està a prop
      } else if (distanceToEnemy < 300) {
        robot.setFire(2); // Potència mitjana si està a distància mitjana
      } else {
        robot.setFire(1); // Baixa potència si està lluny
      }
    }
  }

  private void evade(ScannedRobotEvent e) {
    double enemyBearing = e.getBearing();
    double escapeAngle =
        enemyBearing > 0 ? -ANGLE_OFFSET : ANGLE_OFFSET; // Triar la direcció de gir
    robot.setTurnRight(
        escapeAngle + robot.random.nextInt(45)); // Girar de manera aleatòria entre 0 i 45 graus
    robot.setBack(ESCAPE_DISTANCE); // Retrocedir per escapar
    robot.execute(); // Executar les ordres de moviment
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    robot.out.println("Entrat en col·lisió amb un robot");
    robot.back(0); // Retrocedir una mica per separar-se de l'enemic
    // Decidir en quina direcció girar (esquerra o dreta) segons la posició relativa de l'enemic
    double enemyBearing = event.getBearing();
    if (enemyBearing > 0) {
      robot.setTurnLeft(ANGLE_OFFSET + robot.random.nextInt(45)); // Girar cap a l'esquerra
    } else {
      robot.setTurnRight(ANGLE_OFFSET + robot.random.nextInt(45)); // Girar cap a la dreta
    }
    robot.execute(); // Executar les ordres de moviment
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Manejar quan el robot colpeja una paret
    double bearing = event.getBearing();
    double turnAngle = (bearing > 0) ? -90 : 90; // Girar 90 graus cap al costat oposat
    robot.turnRight(turnAngle);
    robot.setAhead(100); // Avançar una mica després de girar
    robot.execute(); // Executar les ordres
  }

  @Override
  public void onMessageReceived(MessageEvent e) {
    // El líder maneja missatges de la jerarquia
    robot.onMessageReceived(e);
  }

  @Override
  public void onRobotDeath(RobotDeathEvent e) {
    robot.onRobotDeath(e);
  }
}
