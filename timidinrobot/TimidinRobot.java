package timidinrobot;

import java.awt.Color; // Importar para usar la clase Color
import robocode.*;

public class TimidinRobot extends AdvancedRobot {

  private State state;

  ScannedRobotEvent lastScannedRobot;
  double targetX;
  double targetY;

  public void setState(State newState) {
    state = newState;
  }

  public double normalizeBearing(double angle) {
    while (angle > 180) angle -= 360;
    while (angle < -180) angle += 360;
    return angle;
  }

  @Override
  public void run() {
    setBodyColor(Color.black); // Color del cuerpo
    setGunColor(Color.red); // Color del cañón
    setRadarColor(Color.black);
    setState(new Detection(this));

    while (true) {
      state.run();
    }
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    state.onScannedRobot(e);
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {
    state.onHitRobot(e);
  }

  @Override
  public void onHitWall(HitWallEvent e) {
    state.onHitWall(e);
  }
}
