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
      updateState(); // Actualizar el estado después de recibir mensajes
    }
  }

  private void reportPosition() {
    // Informar mi posición a los demás robots del equipo
    HierarchyMember me = new HierarchyMember(getName(), new Point2D.Double(getX(), getY()));
    try {
      broadcastMessage(me);
    } catch (IOException ex) {
      // Manejo de la excepción (puedes loguear el error o simplemente ignorarlo)
      out.println("Error al enviar mensaje: " + ex.getMessage());
    }
    updateTeamHierarchy(me);
  }

  private void updateTeamHierarchy(HierarchyMember member) {
    // Actualizar la lista de miembros y recalcular la jerarquía
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

    // Ordenar los miembros por distancia al origen (0, 0)
    Collections.sort(teamMembers, Comparator.comparingDouble(m -> m.position.distance(ORIGIN)));

    // Asignar el líder
    leader = teamMembers.get(0);
    for (int i = 0; i < teamMembers.size(); i++) {
      teamMembers.get(i).previous = (i == 0) ? null : teamMembers.get(i - 1);
      if (teamMembers.get(i).name.equals(getName())) {
        myRole = teamMembers.get(i);

        // Verificar si este robot es el nuevo líder
        if (myRole == leader) {
          state = new LeaderState(this); // Cambiar el estado a líder si soy el nuevo líder
        } else {
          state = new FollowerState(this); // Asegurarse de que otros robots sean seguidores
        }
      }
    }
  }

  public void onRobotDeath(RobotDeathEvent e) {
    // Recalcular la jerarquía cuando un robot muere
    teamMembers.removeIf(member -> member.name.equals(e.getName()));
    updateTeamHierarchy(null); // Recalcular la jerarquía después de una muerte

    // Asegurar que el nuevo líder es asignado correctamente
    if (teamMembers.size() > 0) {
      leader = teamMembers.get(0); // El nuevo líder es el siguiente en la lista
      if (leader.name.equals(getName())) {
        state = new LeaderState(this); // Cambiar a LeaderState si este robot es el nuevo líder
      }
    }
  }

  @Override
  public void onPaint(Graphics2D g) {
    // Dibujar un círculo alrededor del líder
    if (myRole == leader) {
      g.setColor(Color.RED); // Cambia el color según lo que prefieras
      g.drawOval((int) (getX() - 50), (int) (getY() - 50), 100, 100); // Círculo con radio de 50
    }
  }

  static class HierarchyMember implements java.io.Serializable {
    String name;
    Point2D.Double position;
    HierarchyMember previous; // El robot justo por encima en la jerarquía

    HierarchyMember(String name, Point2D.Double position) {
      this.name = name;
      this.position = position;
    }
  }

  public double normalizeBearing(double angle) {
    while (angle > 180) angle -= 360;
    while (angle < -180) angle += 360;
    return angle;
  }
}
