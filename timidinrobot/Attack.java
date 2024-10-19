package timidinrobot;

import robocode.*;

public class Attack implements State {

  private TimidinRobot robot;

  public Attack(TimidinRobot timidinRobot) {
    this.robot = timidinRobot;
  }

  @Override
  public void run() {
    // Usar setTurnRadarRight para un giro de radar no bloqueante
    robot.setTurnRadarRight(360);
    if (robot.lastScannedRobot != null) {
      aimAndFire(robot.lastScannedRobot);  // Disparar si tenemos un robot escaneado
    }
    robot.execute();  // Ejecutar todos los comandos no bloqueantes
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // Al escanear un robot, almacenar la información y atacar
    robot.lastScannedRobot = e;
    aimAndFire(e);
  }

  private void aimAndFire(ScannedRobotEvent e) {
    // Calcular el ángulo de giro para apuntar el cañón hacia el robot enemigo
    double gunTurnAngle = robot.getHeading() + e.getBearing() - robot.getGunHeading();
    robot.setTurnGunRight(robot.normalizeBearing(gunTurnAngle));  // Usar setTurnGunRight para giro no bloqueante

    // Calcular la potencia de disparo en función de la distancia al enemigo
    double firePower = Math.min(400 / e.getDistance(), 3);
    robot.fire(firePower);  // Disparar con la potencia calculada
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {
    // Manejar cuando el robot golpea a otro robot (puedes añadir lógica aquí)
  }

  @Override
  public void onHitWall(HitWallEvent e) {
    // Manejar cuando el robot golpea una pared (puedes añadir lógica aquí)
  }
}
