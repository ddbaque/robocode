package timidinrobot;

import java.awt.Color; // Importar para usar la clase Color
import robocode.*;

public class TimidinRobot extends AdvancedRobot {

  private State state; // estat actual del robot

  ScannedRobotEvent lastScannedRobot; // últim esdeveniment de detecció d'un robot enemic
 
  // coordenades de la cantonada objectiu
  double targetX;
  double targetY;

  // canvia l'estat state del robot a l'especificat (newState)
  public void setState(State newState) {
    state = newState;
  }

  // normalitza l'angle angle perquè estigui en el rang de -180 a 180 graus.
  public double normalizeBearing(double angle) {
    while (angle > 180) angle -= 360;
    while (angle < -180) angle += 360;
    return angle;
  }

  @Override
  public void run() {
    // configuració del color de TimidinRobot
    setBodyColor(Color.black); 
    setGunColor(Color.red); 
    setRadarColor(Color.black);
    
    setState(new Detection(this)); // comença a l'estat 0: detection

    // executa el mètode run de l'estat actual continuament
    while (true) {
      state.run();
    }
  }

  // delega el maneig de l'esdeveniment onScannedRobot a l'estat actual
  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    state.onScannedRobot(e);
  }

  // delega el maneig de l'esdeveniment onHitRobot a l'estat actual
  @Override
  public void onHitRobot(HitRobotEvent e) {
    state.onHitRobot(e);
  }

  // delega el maneig de l'esdeveniment onHitWall a l'estat actual
  @Override
  public void onHitWall(HitWallEvent e) {
    state.onHitWall(e);
  }
}
