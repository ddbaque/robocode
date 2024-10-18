package myteam;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TeamHierarchyRobot extends TeamRobot {
    private static final Point2D.Double ORIGIN = new Point2D.Double(0, 0); // Usar el punto 0,0 como referencia
    private ArrayList<HierarchyMember> teamMembers = new ArrayList<>();
    private HierarchyMember leader;
    private HierarchyMember myRole;

    public void run() {
        // Esperar a que todos los miembros del equipo estén reportados
        while (getTeammates() == null || getTeammates().length == 0) {
            execute();
        }

        reportPosition();

        // Comportamiento continuo
        while (true) {
            if (myRole == leader) {
                goToCenter();
            } else {
                followHierarchy();
            }
            execute();
        }
    }

    private void goToCenter() {
        // El líder va al centro del campo (0, 0)
        goTo(ORIGIN.getX(), ORIGIN.getY());
    }

    private void followHierarchy() {
        if (myRole.previous != null) {
            // Seguir al robot justo por encima de mí en la jerarquía
            Point2D.Double target = myRole.previous.position;
            // Separamos un poco al seguidor para que no esté encima del líder
            double distanceToTarget = 100;  // Distancia de "seguimiento"
            goTo(target.getX(), target.getY(), distanceToTarget);
        }
    }

    // Este método permite a los robots moverse a una posición objetivo manteniendo una distancia
    private void goTo(double x, double y, double offset) {
        double dx = x - getX();
        double dy = y - getY();
        double distance = Point2D.distance(getX(), getY(), x, y);

        // Moverse solo si la distancia es mayor al offset
        if (distance > offset) {
            double angleToTarget = Math.atan2(dy, dx);
            double targetAngle = Utils.normalRelativeAngle(angleToTarget - getHeadingRadians());

            setTurnRightRadians(targetAngle);
            setAhead(distance - offset);
        }
    }

    // Sobrecarga del método goTo() para el líder (sin offset)
    private void goTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        double angleToTarget = Math.atan2(dy, dx);
        double targetAngle = Utils.normalRelativeAngle(angleToTarget - getHeadingRadians());

        setTurnRightRadians(targetAngle);
        setAhead(Point2D.distance(getX(), getY(), x, y));
    }

    public void onMessageReceived(MessageEvent e) {
        if (e.getMessage() instanceof HierarchyMember) {
            // Añadir a la lista de miembros del equipo
            HierarchyMember newMember = (HierarchyMember) e.getMessage();
            updateTeamHierarchy(newMember);
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
            }
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        // Recalcular la jerarquía cuando un robot muere
        teamMembers.removeIf(member -> member.name.equals(e.getName()));
        updateTeamHierarchy(null);
        
        // Asegurar que el nuevo líder es asignado correctamente
        if (teamMembers.size() > 0) {
            leader = teamMembers.get(0); // El nuevo líder es el siguiente en la lista
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
}



// team robot fase 0 TEMPORAL!!!!!!!!  hace elegir lider y cuando se muere pasar al siguiente en la jerarquia