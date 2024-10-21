package followtheleaderteam;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import robocode.*;

public class FollowTheLeaderTeam extends TeamRobot {
  private static final Point2D.Double ORIGIN = new Point2D.Double(0, 0);
  ArrayList<HierarchyMember> teamMembers = new ArrayList<>();
  Random random = new Random();
  Point2D.Double target;
  Point2D.Double enemyPosition;
  private HierarchyMember leader;
  private boolean iAmFirst = false;
  private boolean allPositionsReceived = false;
  public HierarchyMember myRole;
  private State state; // Almacena el estado actual

  public void run() {
    // Esperar a que todos los miembros del equipo estén reportados
    while (getTeammates() == null || getTeammates().length == 0) {
      execute();
    }
    if (getName().equals("followtheleaderteam.FollowTheLeaderTeam* (1)")) iAmFirst = true;

    if (iAmFirst) {
      waitForAllPositions(); // Wait for all positions to be received
      broadcastHierarchy(); // Broadcast the hierarchy to all team members
    } else {
      reportPosition();
      while (teamMembers.size() < getTeammates().length) {
        execute(); // Mantener el ciclo hasta recibir todas las posiciones
      }
    }
    out.println("flag 1");
    // Comportamiento continuo
    while (true) {
      out.println("While -> My role -> " + myRole);
      state.run();
    }
  }

  private void waitForAllPositions() {
    HierarchyMember me = new HierarchyMember(getName(), new Point2D.Double(getX(), getY()));
    updateTeamHierarchy(me);

    out.println("Esperando todas las posiciones " + me.name);
    while (!allPositionsReceived) {
      execute(); // Keep executing until all positions are received
    }
    out.println("Posiciones obtenidas");
  }

  private void updateState() {
    if (myRole == leader) {
      state = new LeaderState(this);
    } else {
      state = new FollowerState(this);
    }
  }

  public void onMessageReceived(MessageEvent e) {
    if (iAmFirst && !allPositionsReceived) {
      if (e.getMessage() instanceof HierarchyMember) {
        HierarchyMember newMember = (HierarchyMember) e.getMessage();
        updateTeamHierarchy(newMember);
        updateState();
      } else if (e.getMessage().equals("REVERSE_HIERARCHY")) {
        reverseHierarchy(); // Invertir jerarquía cuando se reciba el mensaje
      }
    }

    if (e.getMessage() instanceof HierarchyMember) {
      HierarchyMember member = (HierarchyMember) e.getMessage();
      target = member.position;
    } else if (e.getMessage() instanceof Point2D.Double) {
      enemyPosition = (Point2D.Double) e.getMessage();

      double distanceToEnemy = enemyPosition.distance(getX(), getY());

      // Calculate the angle to the enemy
      double dx = enemyPosition.getX() - getX();
      double dy = enemyPosition.getY() - getY();
      double angleToEnemy = Math.toDegrees(Math.atan2(dx, dy));
      double turnGun = normalizeBearing(angleToEnemy - getGunHeading());

      // Turn the gun towards the enemy
      setTurnGunRight(turnGun);

      // Fire based on distance to the enemy
      if (distanceToEnemy < 150) {
        setFire(3); // High power if close
      } else if (distanceToEnemy < 300) {
        setFire(2); // Medium power if medium distance
      } else {
        setFire(1); // Low power if far away
      }
    }
    if (e.getMessage() instanceof HierarchyBroadcast) {
      HierarchyBroadcast hierarchyBroadcast = (HierarchyBroadcast) e.getMessage();
      updateTeamMembers(hierarchyBroadcast);
      updateState();
    } else if (e.getMessage().equals("REVERSE_HIERARCHY")) {
      reverseHierarchy(); // Invertir jerarquía cuando se reciba el mensaje
    }
  }

  private void updateTeamMembers(HierarchyBroadcast hierarchyBroadcast) {
    out.println("La lista es de -> " + hierarchyBroadcast.members.size());
    teamMembers.clear(); // Clear the current team members list
    for (HierarchyMember member : hierarchyBroadcast.members) {
      updateTeamHierarchy(member); // Add new members from the broadcast
    }
  }

  void reportPosition() {
    HierarchyMember me = new HierarchyMember(getName(), new Point2D.Double(getX(), getY()));
    try {
      broadcastMessage(me);
    } catch (IOException ex) {
      out.println("Error al enviar mensaje: " + ex.getMessage());
    }
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
        out.println("Add member -> " + member.name);
        teamMembers.add(member);
      }
    }

    // Ordenar los miembros por distancia al origen
    Collections.sort(teamMembers, Comparator.comparingDouble(m -> m.position.distance(ORIGIN)));

    // Asignar el líder
    leader = teamMembers.get(0);
    out.println("Leader -> " + leader.name);
    for (int i = 0; i < teamMembers.size(); i++) {
      teamMembers.get(i).previous = (i == 0) ? null : teamMembers.get(i - 1);
      if (teamMembers.get(i).name.equals(getName())) {
        myRole = teamMembers.get(i);
        out.println("My role -> " + myRole.name);
        updateState();
      }
    }
    if (teamMembers.size() == (getTeammates().length + 1)) allPositionsReceived = true;
  }

  private void broadcastHierarchy() {
    try {
      broadcastMessage(new HierarchyBroadcast(teamMembers));
    } catch (IOException ex) {
      out.println("Error sending hierarchy: " + ex.getMessage());
    }
  }

  public void reverseHierarchy() {
    // Invertir la jerarquía
    out.println("Estado de teamMembers antes de la inversión:");
    for (HierarchyMember member : teamMembers) {
      out.println("Miembro: " + member.name + ", Posición: " + member.position);
    }
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

    out.println("Estado de teamMembers después de la inversión:");
    for (HierarchyMember member : teamMembers) {
      out.println("Miembro: " + member.name + ", Posición: " + member.position);
    }
    out.println("Jerarquía invertida. Nuevo líder: " + leader.name);
  }

  public void onRobotDeath(RobotDeathEvent e) {
    teamMembers.removeIf(member -> member.name.equals(e.getName()));
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

  public boolean isTeammate(String robotName) {
    // Example logic to check if the robotName is in your team
    return robotName.startsWith("followtheleaderteam"); // Replace with actual logic
  }
}

class HierarchyBroadcast implements java.io.Serializable {
  ArrayList<HierarchyMember> members; // List of team members in the hierarchy

  HierarchyBroadcast(ArrayList<HierarchyMember> members) {
    this.members = members;
  }
}
