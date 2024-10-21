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
  private State state; // Emmagatzema l'estat actual

  public void run() {
    // Esperar que tots els membres de l'equip es reportin
    while (getTeammates() == null || getTeammates().length == 0) {
      execute();
    }
    if (getName().equals("followtheleaderteam.FollowTheLeaderTeam* (1)")) iAmFirst = true;

    if (iAmFirst) {
      waitForAllPositions(); // Esperar a que s'hagin rebut totes les posicions
      broadcastHierarchy(); // Enviar la jerarquia a tots els membres de l'equip
    } else {
      reportPosition();
      while (teamMembers.size() < getTeammates().length) {
        execute(); // Mantenir el cicle fins que es rebin totes les posicions
      }
    }
    out.println("flag 1");
    // Comportament continu
    while (true) {
      out.println("While -> My role -> " + myRole);
      state.run();
    }
  }

  private void waitForAllPositions() {
    HierarchyMember me = new HierarchyMember(getName(), new Point2D.Double(getX(), getY()));
    updateTeamHierarchy(me);

    out.println("Esperant totes les posicions " + me.name);
    while (!allPositionsReceived) {
      execute(); // Continuar executant fins que es rebin totes les posicions
    }
    out.println("Posicions obtingudes");
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
        reverseHierarchy(); // Invertir jerarquia quan es rebi el missatge
      }
    }

    if (e.getMessage() instanceof HierarchyMember) {
      HierarchyMember member = (HierarchyMember) e.getMessage();
      target = member.position;
    } else if (e.getMessage() instanceof Point2D.Double) {
      enemyPosition = (Point2D.Double) e.getMessage();

      double distanceToEnemy = enemyPosition.distance(getX(), getY());

      // Calcular l'angle cap a l'enemic
      double dx = enemyPosition.getX() - getX();
      double dy = enemyPosition.getY() - getY();
      double angleToEnemy = Math.toDegrees(Math.atan2(dx, dy));
      double turnGun = normalizeBearing(angleToEnemy - getGunHeading());

      // Girar la pistola cap a l'enemic
      setTurnGunRight(turnGun);

      // Disparar en funció de la distància a l'enemic
      if (distanceToEnemy < 150) {
        setFire(3); // Màxima potència si és a prop
      } else if (distanceToEnemy < 300) {
        setFire(2); // Potència mitjana si està a distància mitjana
      } else {
        setFire(1); // Potència baixa si està lluny
      }
    }
    if (e.getMessage() instanceof HierarchyBroadcast) {
      HierarchyBroadcast hierarchyBroadcast = (HierarchyBroadcast) e.getMessage();
      updateTeamMembers(hierarchyBroadcast);
      updateState();
    } else if (e.getMessage().equals("REVERSE_HIERARCHY")) {
      reverseHierarchy(); // Invertir jerarquia quan es rebi el missatge
    }
  }

  private void updateTeamMembers(HierarchyBroadcast hierarchyBroadcast) {
    out.println("La llista és de -> " + hierarchyBroadcast.members.size());
    teamMembers.clear(); // Netejar la llista actual de membres de l'equip
    for (HierarchyMember member : hierarchyBroadcast.members) {
      updateTeamHierarchy(member); // Afegir nous membres del broadcast
    }
  }

  void reportPosition() {
    HierarchyMember me = new HierarchyMember(getName(), new Point2D.Double(getX(), getY()));
    try {
      broadcastMessage(me);
    } catch (IOException ex) {
      out.println("Error en enviar el missatge: " + ex.getMessage());
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
        out.println("Afegir membre -> " + member.name);
        teamMembers.add(member);
      }
    }

    // Ordenar els membres per distància a l'origen
    Collections.sort(teamMembers, Comparator.comparingDouble(m -> m.position.distance(ORIGIN)));

    // Assignar el líder
    leader = teamMembers.get(0);
    out.println("Líder -> " + leader.name);
    for (int i = 0; i < teamMembers.size(); i++) {
      teamMembers.get(i).previous = (i == 0) ? null : teamMembers.get(i - 1);
      if (teamMembers.get(i).name.equals(getName())) {
        myRole = teamMembers.get(i);
        out.println("El meu rol -> " + myRole.name);
        updateState();
      }
    }
    if (teamMembers.size() == (getTeammates().length + 1)) allPositionsReceived = true;
  }

  private void broadcastHierarchy() {
    try {
      broadcastMessage(new HierarchyBroadcast(teamMembers));
    } catch (IOException ex) {
      out.println("Error en enviar la jerarquia: " + ex.getMessage());
    }
  }

  public void reverseHierarchy() {
    // Invertir la jerarquia
    out.println("Estat de teamMembers abans de la inversió:");
    for (HierarchyMember member : teamMembers) {
      out.println("Membre: " + member.name + ", Posició: " + member.position);
    }
    Collections.reverse(teamMembers);

    // Assignar el nou líder
    leader = teamMembers.get(0);
    for (int i = 0; i < teamMembers.size(); i++) {
      teamMembers.get(i).previous = (i == 0) ? null : teamMembers.get(i - 1);
      if (teamMembers.get(i).name.equals(getName())) {
        myRole = teamMembers.get(i);
        updateState(); // Actualitzar l'estat després d'invertir la jerarquia
      }
    }

    out.println("Estat de teamMembers després de la inversió:");
    for (HierarchyMember member : teamMembers) {
      out.println("Membre: " + member.name + ", Posició: " + member.position);
    }
    out.println("Jerarquia invertida. Nou líder: " + leader.name);
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

  // delega la gestió de l'esdeveniment onHitRobot a l'estat actual
  @Override
  public void onHitRobot(HitRobotEvent e) {
    state.onHitRobot(e);
  }

  // delega la gestió de l'esdeveniment onHitWall a l'estat actual
  @Override
  public void onHitWall(HitWallEvent e) {
    state.onHitWall(e);
  }

  public boolean isTeammate(String robotName) {
    // Lògica per verificar si el robotName és del teu equip
    return robotName.startsWith("followtheleaderteam"); // Substituir amb la lògica actual
  }
}

class HierarchyBroadcast implements java.io.Serializable {
  ArrayList<HierarchyMember> members; // Llista de membres de l'equip en la jerarquia

  HierarchyBroadcast(ArrayList<HierarchyMember> members) {
    this.members = members;
  }
}
