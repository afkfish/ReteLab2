package world;

import java.util.ArrayList;

/**
 * Interface defining the access function of the robot towards the map
 */
public interface RobotPercepcion {
    // Return the simulation time
    public int getTime();

    // Return all exit cells
    public ArrayList<Cell> getExitCells();
    // Return all unknown cells
    public ArrayList<Cell> getUnknownCells();
    // Return all discovered injured 
    public ArrayList<Injured> getDiscoveredInjureds();
    // Return all robots
    public ArrayList<Robot> getRobots();

    // Returns the shortest path to an exit cell
    public Path getShortestExitPath(Cell start);
    // Returns the shortest path to an unknown cell
    public Path getShortestUnknownPath(Cell start);
    // Returns the shortest path to an injured
    public Path getShortestInjuredPath(Cell start);
}
