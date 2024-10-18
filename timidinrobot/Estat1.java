package timidinrobot;

import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

import java.util.Random;

// Move to corneer
public class Estat1 extends Estat {

    private Random random = new Random();
    private double angleOffset = 20; // Default angle offset
    public Estat1(TimidinRobot rob) {

        super(rob);
    }

    @Override
    public void run() {
        double dx = robot.targetX - robot.getX();
        double dy = robot.targetY - robot.getY();
        double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
        double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());

        robot.turnRight(turnAngle);
        robot.setTurnRadarRight(robot.normalizeBearing(angleToTarget - robot.getRadarHeading()));

        robot.ahead(Math.hypot(dx, dy));

        if (Math.hypot(dx, dy) < 40) {
            //currentState = State.ATTACK;
            robot.setState(new Estat2(robot));
        }
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // Handle the event when the robot hits another robot
            double dx = robot.targetX - robot.getX();
            double dy = robot.targetY - robot.getY();
            double enemyBearing = e.getBearing();
            double gunTurnAngle = robot.getHeading() + enemyBearing - robot.getGunHeading();
            robot.turnGunRight(robot.normalizeBearing(gunTurnAngle));
            robot.fire(3);

            double gunToRobotHeading = robot.getHeading() - robot.getGunHeading();
            double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));

        // Calcular el ángulo de giro si giramos a la derecha
        double turnRightAngle = robot.normalizeBearing(angleToTarget - (robot.getHeading() + angleOffset));

        // Calcular el ángulo de giro si giramos a la izquierda
        double turnLeftAngle = robot.normalizeBearing(angleToTarget - (robot.getHeading() - angleOffset));

        // Decidir la dirección que minimiza la distancia a la esquina
        if (Math.abs(turnRightAngle) < Math.abs(turnLeftAngle)) {
            robot.turnRight(angleOffset); // Girar a la derecha
        } else {
            robot.turnLeft(angleOffset); // Girar a la izquierda
        }

            // Retroceder para evitar el choque
            robot.back(50);

            // NUEVO: Lógica mejorada para decidir la dirección y el ángulo de giro
            if (e.getBearing() > 0) {
                robot.turnLeft(angleOffset + random.nextInt(45)); // Girar en una dirección aleatoria entre 45 y 90 grados
            } else {
                robot.turnRight(angleOffset + random.nextInt(45));
            }
     double tankHeading = robot.getHeading();
        // Obtener la dirección actual del cañón
        double gunHeading = robot.getGunHeading();
        // Calcular el ángulo para girar el cañón hacia la dirección del tanque
        double angleToTurn = tankHeading - gunHeading;
        // Girar el cañón hacia el ángulo calculado
        robot.turnGunRight(robot.normalizeBearing(angleToTurn));
        // Seguir avanzando/

            robot.ahead(50);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Handle the event when the robot hits a wall
        double battlefieldWidth = robot.getBattleFieldWidth();
        double battlefieldHeight = robot.getBattleFieldHeight();
        double dx = robot.targetX - robot.getX();
        double dy = robot.targetY - robot.getY();
   if(Math.hypot(dx, dy) > 40) {

    // Retroceder un poco antes de girar
    robot.back(50);

    // Generar 5 ángulos aleatorios y comparar distancias a las paredes
    double[] anglesToTry = new double[5];
    double[] distances = new double[5];

    // Probar con 5 ángulos aleatorios entre -90 y 90 grados (izquierda y derecha)
    for (int i = 0; i < 5; i++) {
        // Generar un ángulo aleatorio entre -90 y 90 grados
        anglesToTry[i] = -90 + random.nextDouble() * 180; // Gira entre -90 (izquierda) y 90 (derecha)
        
        // Simular la nueva posición si giramos en ese ángulo y avanzamos
        double newHeading = robot.normalizeBearing(robot.getHeading() + anglesToTry[i]);
        double newX = robot.getX() + Math.sin(Math.toRadians(newHeading)) * 50; // Simula moverse 50 unidades
        double newY = robot.getY() + Math.cos(Math.toRadians(newHeading)) * 50;

        // Calcular la distancia mínima desde la nueva posición a los bordes del campo de batalla
        double distanceToLeftWall = newX;  // Distancia al borde izquierdo (X = 0)
        double distanceToRightWall = battlefieldWidth - newX; // Distancia al borde derecho
        double distanceToTopWall = battlefieldHeight - newY; // Distancia al borde superior
        double distanceToBottomWall = newY;  // Distancia al borde inferior (Y = 0)

        // La distancia mínima a las paredes es la menor de las cuatro distancias
        double minDistanceToWall = Math.min(Math.min(distanceToLeftWall, distanceToRightWall), 
                                            Math.min(distanceToTopWall, distanceToBottomWall));

        // Guardar la distancia mínima para este ángulo
        distances[i] = minDistanceToWall;
    }

    // Encontrar el ángulo que resulte en la mayor distancia a la pared (el más lejos de cualquier pared)
    int bestIndex = 0;
    for (int i = 1; i < 5; i++) {
        if (distances[i] > distances[bestIndex]) { // Elegir el que maximice la distancia a la pared
            bestIndex = i;
        }
    }

    // Girar hacia el ángulo que maximiza la distancia a las paredes
    robot.turnRight(anglesToTry[bestIndex]);

    // Avanzar después de girar
    robot.ahead(50);
    } 
}

    @Override
    public void onBulletHit(BulletHitEvent event) {
        // Handle the event when the robot's bullet hits another robot
    }
}
