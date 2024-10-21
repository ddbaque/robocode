package followtheleaderteam;

import java.awt.geom.Point2D;
import java.io.IOException;
import robocode.*;

public class LeaderState implements State {

  private FollowTheLeaderTeam robot;
  private long startTime;
  private long lastScanTime; // Track last scan time
  private double safeMarginX;
  private double safeMarginY;
  private double[][] corners; // Array to hold corner coordinates
  private int currentCornerIndex = 0; // To track which corner we are moving towards
  private static final double ESCAPE_DISTANCE = 100; // Distance to escape from detected robots
  private static final double ANGLE_OFFSET = 40; // Angle offset for evasion

  public LeaderState(FollowTheLeaderTeam robot) {
    this.robot = robot;

    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();

    // Calculate 10% margins
    this.safeMarginX = battlefieldWidth * 0.1;
    this.safeMarginY = battlefieldHeight * 0.1;

    // Define corners taking into account the safe margin
    this.corners =
        new double[][] {
          {safeMarginX, safeMarginY}, // (0,0) corner
          {safeMarginX, battlefieldHeight - safeMarginY}, // (0,1) corner
          {battlefieldWidth - safeMarginX, battlefieldHeight - safeMarginY}, // (1,1) corner
          {battlefieldWidth - safeMarginX, safeMarginY} // (1,0) corner
        };

    this.startTime = System.currentTimeMillis();
    this.lastScanTime = System.currentTimeMillis(); // Initialize last scan time
  }

  @Override
  public void run() {
    // Check if 20 seconds have passed for reversing hierarchy
    if (System.currentTimeMillis() - startTime > 15000) {
      try {
        robot.broadcastMessage("REVERSE_HIERARCHY");
      } catch (IOException e) {
        robot.out.println("Error sending reverse message: " + e.getMessage());
      }
      robot.reverseHierarchy();
      return;
    }
    if (System.currentTimeMillis() - lastScanTime > 4000) {
      lastScanTime = System.currentTimeMillis();
      robot.setTurnRadarRight(360); // Sweep radar
    }
    // Get the current target corner coordinates
    double targetX = corners[currentCornerIndex][0];
    double targetY = corners[currentCornerIndex][1];

    // Calculate the angle to the target
    double dx = targetX - robot.getX();
    double dy = targetY - robot.getY();
    double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));

    // Turn towards the target
    double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());
    robot.turnRight(turnAngle);

    // Move towards the target
    double distanceToTarget = Math.hypot(dx, dy);
    robot.setAhead(distanceToTarget);

    // Check if we have reached the target corner
    if (distanceToTarget < 20) { // Adjust this threshold as necessary
      currentCornerIndex = (currentCornerIndex + 1) % corners.length; // Move to the next corner
    }

    // Send the current position to the followers
    robot.reportPosition();

    robot.execute();
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    String scannedRobotName = e.getName();
    if (robot.isTeammate(scannedRobotName)) {
      return; // Ignore teammates
    }
    // If a scanned robot is close, evade
    if (e.getDistance() < ESCAPE_DISTANCE) {
      evade(e);
    } else {
      // Store enemy position when detected
      double enemyX =
          robot.getX()
              + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
      double enemyY =
          robot.getY()
              + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));
      robot.enemyPosition = new Point2D.Double(enemyX, enemyY);

      // Broadcast the enemy position to followers
      try {
        robot.broadcastMessage(robot.enemyPosition);
      } catch (IOException ex) {
        robot.out.println("Error broadcasting enemy position: " + ex.getMessage());
      }
      double dx = enemyX - robot.getX();
      double dy = enemyY - robot.getY();
      double angleToEnemy = Math.toDegrees(Math.atan2(dx, dy));
      double turnGun = robot.normalizeBearing(angleToEnemy - robot.getGunHeading());

      // Turn the gun towards the enemy
      robot.setTurnGunRight(turnGun);

      // Fire based on distance to the enemy
      double distanceToEnemy = e.getDistance();
      if (distanceToEnemy < 150) {
        robot.setFire(3); // High power if close
      } else if (distanceToEnemy < 300) {
        robot.setFire(2); // Medium power if medium distance
      } else {
        robot.setFire(1); // Low power if far away
      }
    }
  }

  private void evade(ScannedRobotEvent e) {
    double enemyBearing = e.getBearing();
    double escapeAngle =
        enemyBearing > 0 ? -ANGLE_OFFSET : ANGLE_OFFSET; // Choose direction to turn
    robot.setTurnRight(
        escapeAngle + robot.random.nextInt(45)); // Turn randomly between 0 and 45 degrees
    robot.setBack(ESCAPE_DISTANCE); // Move back to escape
    robot.execute(); // Execute the movement commands
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {
    robot.out.println("entroroororor");
    robot.back(0); // Retrocede un poco para separarse del enemigo
    // Decidir en qué dirección girar (izquierda o derecha) dependiendo de la posición relativa del
    // enemigo (bearing)
    double enemyBearing = event.getBearing();
    if (enemyBearing > 0) {
      robot.setTurnLeft(ANGLE_OFFSET + robot.random.nextInt(45)); // Gira a la izquierda
    } else {
      robot.setTurnRight(ANGLE_OFFSET + robot.random.nextInt(45)); // Gira a la derecha
    }
    robot.execute(); // Ejecutar las órdenes de movimiento
  }

  @Override
  public void onHitWall(HitWallEvent event) {
    // Manejar cuando el robot golpea una pared
    double bearing = event.getBearing();
    double turnAngle = (bearing > 0) ? -90 : 90; // Girar 90 grados hacia el lado opuesto
    robot.turnRight(turnAngle);
    robot.setAhead(100); // Avanzar un poco después de girar
    robot.execute(); // Ejecutar las órdenes
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
