package timidinrobot;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public class TimidinRobot extends AdvancedRobot {

    private Estat e;
    
    ScannedRobotEvent lastScannedRobot;
    double targetX;
    double targetY;

    @Override
    public void run() {
        setAdjustGunForRobotTurn(false);
        setAdjustRadarForRobotTurn(false);
        setAdjustRadarForGunTurn(false);
        
        setState(new Estat0(this));
        
        while(true){
            e.run();
        }
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent event){
        e.onScannedRobot(event);
    }
    
    @Override
    public void onHitRobot(HitRobotEvent event) {
        e.onHitRobot(event);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        e.onHitWall(event);
    }
    
    @Override
    public void onBulletHit(BulletHitEvent event) {
        e.onBulletHit(event);
    }
    
    public void setState(Estat es){
        e = es;
    }
    public double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

}
