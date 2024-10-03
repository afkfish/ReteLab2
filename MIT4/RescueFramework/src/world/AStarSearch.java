package world;

import java.awt.Color;
import java.util.ArrayList;
import rescueframework.RescueFramework;

/**
 * Static class performing A* search
 */
public class AStarSearch {
    // Open list of the nodes
    static ArrayList<AStarCell> openList = new ArrayList<>();
    // Closed list of the nodes
    static ArrayList<AStarCell> closedList = new ArrayList<>();
    
    /**
     * Find path between start and target no longer than maxDistance
     * 
     * @param start         The start cell to search from
     * @param target        The target cell to search to
     * @param maxDistance   The maximum length of the path to consider
     * @param color         Color of the path
     * @return              A Path if exists between start and target or NULL
     */
    public static Path search(Cell start, Cell target, int maxDistance, Color color) {
        Path result = search(start,target, maxDistance);
        if (result != null) result.setColor(color);
        return result;
    }
    
    
    /**
     * Find path between start and target no longer than maxDistance
     * 
     * @param start         The start cell to search from
     * @param target        The target cell to search to
     * @param maxDistance   The maximum length of the path to consider
     * @return              A Path if exists between start and target or NULL
     */
    public static Path search(Cell start, Cell target, int maxDistance) {
        // Disable console logging
        boolean verbose = false; 
        if (verbose) RescueFramework.log("AStarSearch between "+start.getX()+"x"+start.getY()+" and "+target.getX()+"x"+target.getY()+" max distance "+maxDistance);
        
        // Init open and closed lists
        openList.clear();
        closedList.clear();
        
        // Init the start cell and add to the open list
        AStarCell startCell = new AStarCell(start, 0, heuristics(start,target), null);
        openList.add(startCell);
        
        // Loop while the open list is not empty
        int iteration = 0;
        while (openList.size()>0) {
            iteration++;
            if (verbose) {
                System.out.print("--- Iteration #"+iteration+". Openlist: ");
                RescueFramework.log(listToString(openList));
                System.out.print("Closed list: ");
                RescueFramework.log(listToString(closedList));
            }
            
            // Choose next node
            int bestIndex = 0;
            double bestF = openList.get(bestIndex).getF();
            for (int i=1; i<openList.size();i++) {
                if (openList.get(i).getF()<bestF) {                    
                    bestIndex = i;
                    bestF = openList.get(i).getF();
                }
            }
            
            // Expand node with best F
            AStarCell selectedCell = openList.get(bestIndex);
            if (verbose) RescueFramework.log("  Processing node "+selectedCell.getCell().getX()+" x "+selectedCell.getCell().getY()+" ("+(Math.round(100*(selectedCell.getSumG()+selectedCell.getH()))/100d)+")");
            
            // Check max distance constraint
            if (maxDistance>0 && selectedCell.getSumG()>maxDistance) {
                if (verbose) RescueFramework.log("    Max distance reached ("+maxDistance+"), giving up!");
                return null;
            }
            
            // Check if target reached
            if (selectedCell.getCell().equals(target)) {
                if (verbose) RescueFramework.log("    Target reached: "+target.getX()+" x "+target.getY()+". Building path back:");
                Path result = new Path();
                while (selectedCell != null) {
                    if (verbose) System.out.print(selectedCell.getCell().getX()+" x "+selectedCell.getCell().getY()+" --> ");
                    result.addFirstCell(selectedCell.getCell());
                    selectedCell = selectedCell.getParent();
                }
                if (verbose) RescueFramework.log("");
                return result;
            }
            
            // Add known neighbours to the open list
            for (int dir = 0; dir<4; dir++) {
                Cell possibleNeighbour = selectedCell.getCell().getAccessibleNeigbour(dir);
                if (possibleNeighbour != null) {
                    if (possibleNeighbour.isDiscovered() || possibleNeighbour.equals(target)){
                        
                        if (!possibleNeighbour.hasObstacle() || possibleNeighbour.equals(target)) {
                            if (!possibleNeighbour.hasInjured() || possibleNeighbour.equals(target)) {
                                boolean skip = false;

                                // Check on open list
                                for (int i=0; i<openList.size(); i++) {
                                    if (openList.get(i).getCell().equals(possibleNeighbour)) {
                                        skip = true;
                                        break;
                                    }
                                }
                                if (skip) {
                                    if (verbose) RescueFramework.log("    Already on open list.");
                                    continue;
                                }

                                // Check on closed list
                                for (int i=0; i<closedList.size(); i++) {
                                    if (closedList.get(i).getCell().equals(possibleNeighbour)) {
                                        skip = true;
                                        break;
                                    }
                                }
                                if (skip) {
                                    if (verbose) RescueFramework.log("    Already on closed list.");
                                    continue;
                                }

                                // Add possible neighbour to open list
                                if (verbose) RescueFramework.log("    Adding to open list!");
                                openList.add(new AStarCell(possibleNeighbour, selectedCell.getSumG()+1, heuristics(possibleNeighbour,target), selectedCell));
                            } else {
                                if (verbose) RescueFramework.log("    Occupied by injured."); 
                            }
                        } else {
                           if (verbose) RescueFramework.log("    Occupied by obstacle."); 
                        }
                    } else {
                        if (verbose) RescueFramework.log("    Not discovered yet.");
                    }
                } else {
                     if (verbose) RescueFramework.log("    No neighbour in dir "+dir+".");
                }
            }
            
            // Add selected node to the closed list
            openList.remove(selectedCell);
            closedList.add(selectedCell);
        }
        
        return null;
    }
    
    /**
     * Calculate heuristics value between two cells
     * 
     * @param c1        The first cell
     * @param c2        The second cell
     * @return          The heuristics value between the two cells
     */
    private static double heuristics(Cell c1, Cell c2) {
        return Math.sqrt(Math.pow(c1.getX()-c2.getX(),2)+Math.pow(c1.getY()-c2.getY(),2));
    }
    
    /**
     * Print node list as String for console debugging 
     * @param list      The list of cells
     * @return          The string list of the cells
     */
    public static String listToString(ArrayList<AStarCell> list) {
        String result = "";
        int g; 
        double h;
        for (int i=0; i<list.size(); i++) {
            AStarCell cell = list.get(i);
            g = cell.getSumG();
            h = cell.getH();
                    
            result = result + cell.getCell().getX()+"x"+cell.getCell().getY()+"("+g+"+"+(Math.round(100*(h))/100d)+"="+(Math.round(100*(g+h))/100d)+"), ";
        }
        return result;
    }
}
