package timidinrobot;

import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public class Estat0 extends Estat {


    public Estat0(TimidinRobot rob) {
        super(rob);
    }

    @Override
    public void run() {
        robot.setTurnRadarRight(360);
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        calculateFurthestCorner(e);
    robot.setState(new Estat1(robot));
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        // Handle the event when the robot hits another robot
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Handle the event when the robot hits a wall
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        // Handle the event when the robot's bullet hits another robot
    }

    private void calculateFurthestCorner(ScannedRobotEvent e) {
        // Use the robot instance to get the necessary battlefield and robot position details
        double enemyX = robot.getX() + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
        double enemyY = robot.getY() + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));

        double battlefieldWidth = robot.getBattleFieldWidth();
        double battlefieldHeight = robot.getBattleFieldHeight();
        double[] cornersX = { 0, 0, battlefieldWidth, battlefieldWidth };
        double[] cornersY = { 0, battlefieldHeight, 0, battlefieldHeight };

        double maxDistance = -1;
        for (int i = 0; i < cornersX.length; i++) {
            double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
            if (distance > maxDistance) {
                maxDistance = distance;
                robot.targetX = cornersX[i];
                robot.targetY = cornersY[i];
            }
        }
    }
}
