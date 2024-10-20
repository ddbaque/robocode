package timidinrobot;

import robocode.*;

public class Detection implements State {
  
  private TimidinRobot robot; // referència al TimidinRobot

  public Detection(TimidinRobot timidinRobot) {
    this.robot = timidinRobot;
  }

  @Override
  public void run() {
    // Gira el radar buscant enemics
    robot.setTurnRadarRight(360); 
    robot.execute();  // Executar les comandes 
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    // Calcula la cantonada més llunyana al detectar un robot
    calculateFurthestCorner(e);
    robot.setState(new MoveToCorner(robot));  // Canvia a l'estat 1: MoveToCorner
  }

  // Calcula la cantonada més allunyada de l'enemic detectat
  private void calculateFurthestCorner(ScannedRobotEvent e) {
    // Calcula les coordenades de l'enemic en funció de la distància entre el robot i l'enemic
    //i l'angle absolut cap a l'enemic (heading+bearing)
    double enemyX =
        robot.getX()
            + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
    double enemyY =
        robot.getY()
            + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));

    // Obté la mida del camp
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();
    double offset = 15; //marge de seguretat 

    // Definim les coordenades de les quatre cantonades del camp de batalla amb un marge de seguretat
    double[] cornersX = {offset, offset, battlefieldWidth - offset, battlefieldWidth - offset};
    double[] cornersY = {offset, battlefieldHeight - offset, offset, battlefieldHeight - offset};

    double maxDistance = -1; // Ens assegurem que la primera distància comparada es guardi (ja que sempre serà més gran que -1)
    // Cerca la cantonada més llunyana a l'enemic
    for (int i = 0; i < cornersX.length; i++) {
      // Calcula la distància de l'enemic a la cantonada
      double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
      // Compara la distància calculada amb la màxima que ja hem comparat
      if (distance > maxDistance) {
        maxDistance = distance;
        // Actualitza la cantonada objectiu
        robot.targetX = cornersX[i]; 
        robot.targetY = cornersY[i];  
      }
    }
  }

  @Override
  public void onHitRobot(HitRobotEvent event) {}

  @Override
  public void onHitWall(HitWallEvent event) {}

}
