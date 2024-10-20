package timidinrobot;

import robocode.*;

public class Attack implements State {

  private TimidinRobot robot; // referència al TimidinRobot

  public Attack(TimidinRobot timidinRobot) {
    this.robot = timidinRobot;
  }

  @Override
  public void run() {

    robot.setTurnRadarRight(360); // Gira el radar buscant enemics
    // Dispara si hi ha un robot escanejat
    if (robot.lastScannedRobot != null) {
      aimAndFire(robot.lastScannedRobot);  
    }
    robot.execute();  // Executar les comandes
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // En escanejar un robot, emmagatzemar la informació i disparar
    robot.lastScannedRobot = e;
    aimAndFire(e);
  }

  private void aimAndFire(ScannedRobotEvent e) {
    // Calcular l'angle de gir per a apuntar el canó cap l'enemic
    double gunTurnAngle = robot.getHeading() + e.getBearing() - robot.getGunHeading();
    // Girar el canó
    robot.setTurnGunRight(robot.normalizeBearing(gunTurnAngle));  

    // Calcula la potència del tret en funció de la distància l'enemic
    double firePower = Math.min(400 / e.getDistance(), 3);
    robot.fire(firePower);  // Dispara 
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {}

  @Override
  public void onHitWall(HitWallEvent e) {}
}
