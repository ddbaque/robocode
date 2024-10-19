
package followtheleaderteam;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import robocode.*;

public class FollowTheLeaderTeam extends TeamRobot {
  private static final Point2D.Double ORIGIN = new Point2D.Double(0, 0);
  private ArrayList<HierarchyMember> teamMembers = new ArrayList<>();
  private HierarchyMember leader;
  public HierarchyMember myRole;
  private State state; // Almacena el estado actual

  public void run() {
    // Esperar a que todos los miembros del equipo estén reportados
    while (getTeammates() == null || getTeammates().length == 0) {
      execute();
    }

    reportPosition();
    updateState();

    // Comportamiento continuo
    while (true) {
      state.run();
    }
  }

  private void updateState() {
    if (myRole == leader) {
      state = new LeaderState(this);
    } else {
      state = new FollowerState(this);
    }
  }

  public void onMessageReceived(MessageEvent e) {
    if (e.getMessage() instanceof HierarchyMember) {
      HierarchyMember newMember = (HierarchyMember) e.getMessage();
      updateTeamHierarchy(newMember);
      updateState();
    } else if (e.getMessage().equals("REVERSE_HIERARCHY")) {
      reverseHierarchy(); // Invertir jerarquía cuando se reciba el mensaje
    }
  }

  private void reportPosition() {
    HierarchyMember me = new HierarchyMember(getName(), new Point2D.Double(getX(), getY()));
    try {
      broadcastMessage(me);
    } catch (IOException ex) {
      out.println("Error al enviar mensaje: " + ex.getMessage());
    }
    updateTeamHierarchy(me);
  }

  private void updateTeamHierarchy(HierarchyMember member) {
    if (member != null) {
      boolean exists = false;
      for (HierarchyMember m : teamMembers) {
        if (m.name.equals(member.name)) {
          m.position = member.position;
          exists = true;
          break;
        }
      }
      if (!exists) {
        teamMembers.add(member);
      }
    }

    // Ordenar los miembros por distancia al origen
    Collections.sort(teamMembers, Comparator.comparingDouble(m -> m.position.distance(ORIGIN)));

    // Asignar el líder
    leader = teamMembers.get(0);
    for (int i = 0; i < teamMembers.size(); i++) {
      teamMembers.get(i).previous = (i == 0) ? null : teamMembers.get(i - 1);
      if (teamMembers.get(i).name.equals(getName())) {
        myRole = teamMembers.get(i);
        updateState();
      }
    }
  }

  public void reverseHierarchy() {
    // Invertir la jerarquía
    Collections.reverse(teamMembers);

    // Asignar el nuevo líder
    leader = teamMembers.get(0);
    for (int i = 0; i < teamMembers.size(); i++) {
      teamMembers.get(i).previous = (i == 0) ? null : teamMembers.get(i - 1);
      if (teamMembers.get(i).name.equals(getName())) {
        myRole = teamMembers.get(i);
        updateState(); // Actualizar el estado después de invertir la jerarquía
      }
    }

    out.println("Jerarquía invertida. Nuevo líder: " + leader.name);
  }

  public void onRobotDeath(RobotDeathEvent e) {
    teamMembers.removeIf(member -> member.name.equals(e.getName()));
    updateTeamHierarchy(null);
    if (teamMembers.size() > 0) {
      leader = teamMembers.get(0);
      updateState();
    }
  }

  @Override
  public void onPaint(Graphics2D g) {
    if (myRole == leader) {
      g.setColor(Color.RED);
      g.drawOval((int) (getX() - 50), (int) (getY() - 50), 100, 100);
    }
  }

  public double normalizeBearing(double angle) {
    while (angle > 180) angle -= 360;
    while (angle < -180) angle += 360;
    return angle;
  }

  static class HierarchyMember implements java.io.Serializable {
    String name;
    Point2D.Double position;
    HierarchyMember previous;

    HierarchyMember(String name, Point2D.Double position) {
      this.name = name;
      this.position = position;
    }
  }
}
