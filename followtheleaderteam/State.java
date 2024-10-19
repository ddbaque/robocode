package followtheleaderteam;

import robocode.*;

public interface State {
  void run();

  void onMessageReceived(MessageEvent e);

  void onRobotDeath(RobotDeathEvent e);
}
