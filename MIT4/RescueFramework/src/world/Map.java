package world;

import world_debug.ViewLineBreakPoint;
import world_debug.ViewLine;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import rescueframework.RescueFramework;

/**
 * The map representing the state of the world
 */
public class Map implements RobotPercepcion{
    // Cell matrix of the map
    private Cell cells[][];
    // Dimensions of the map
    private int height = 0, width = 0;
    // Image cache for loading every image only once
    private HashMap<String,BufferedImage> imageCache = new HashMap<>();
    // Injured people on the map
    private ArrayList<Injured> injureds = new ArrayList<>();
    // Injureds already transported outside by the robots
    private ArrayList<Injured> savedInjureds = new ArrayList<>();
    // Exit cell of the map to transfer injureds to
    private ArrayList<Cell> exitCells = new ArrayList<>();
    // Robots operating on the map
    private ArrayList<Robot> robots = new ArrayList<>();
    // Viewlines of the robots (for robot view debug)
    private ArrayList<ViewLine> viewLines = new ArrayList<>();
    // Viewline break points of the robots (for robot view debug)
    private ArrayList<ViewLineBreakPoint> viewLineBreakPoints = new ArrayList<>();
    // Path to be displayed on the GUI
    private ArrayList<Path> displayPaths = new ArrayList<>();
    // Visibility distance of the robots
    private final int visibilityRange = 3;
    // Simulation time
    private int time = 0;
    // Start cell specified for the robots
    private Cell startCell = null;
    
    /**
     * Default constructor
     * 
     * @param fileName          Text file to load the map from
     * @param robotCount        Robots to generate after loading of the map
     */
    public Map(String fileName, int robotCount) {
        String line;
        String[] array;
        int mode = 0;
        int row = 0;
        ArrayList<Floor> floorList = new ArrayList<>();
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader (fileName));           
            while((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments
                if (line.startsWith("#") || line.startsWith("//") || line.isEmpty()) continue;
                
                if (mode == 0) {
                    // First line specifies map size
                    array = line.split(" ");
                    width = Integer.valueOf(array[0]);
                    height = Integer.valueOf(array[1]);
                    
                    cells = new Cell[width][height];
                    
                    mode = 1;
                } else if (mode == 1) {
                    // Process row definitions
                    array = line.split(" ");
                    if (array.length != width) {
                        throw new Exception("Invalid row specificaion, row width differs: "+width+" =/= "+array.length+" on line :"+line);
                    } else {
                        for (int i=0; i<width; i++) {
                            cells[i][row] = new Cell(i,row, array[i]);
                            if (array[i].equals("S")) startCell = cells[i][row];
                            else if (array[i].equals("X")) exitCells.add(cells[i][row]);
                        }
                    }

                    row++;
                    if (row>=height) mode = 2;

                } else if (mode == 2) {
                    // Process other objects on the map (obstacles, injured, floor definitions)
                    array = line.split(" ");
                    if (array.length>=4 && array[0].startsWith("Floor")) {
                        // Floor definition found
                        floorList.add(new Floor(Integer.valueOf(array[1]),Integer.valueOf(array[2]),Integer.valueOf(array[3])));
                    } else if (array.length>=4 && array[0].startsWith("Obstacle")) {
                        // Obstacle defined
                        crateObstacle(Integer.valueOf(array[1]),Integer.valueOf(array[2]), array[3]);
                    } else if (array.length>=3 && array[0].startsWith("Injured")) {
                        // Injured defined
                        int injuries;
                        if (array.length>=4) {
                            // Load health level from file
                            injuries = Integer.valueOf(array[3])/robotCount;
                        } else {
                            // Generate random health level
                            injuries = (int)((float)Math.random()*1000F);
                        }
                        
                        // Find affected cell
                        int x = Integer.valueOf(array[1]);
                        int y = Integer.valueOf(array[2]);
                        Cell cell = getCell(x, y);
                        
                        // Create new injured and add to cell
                        Injured inj = new Injured(injuries);
                        cell.setInjured(inj);
                        inj.setLocation(cell);
                        injureds.add(inj);
                    } else {
                        RescueFramework.log("Unknown object definition skipped: "+line);
                    }
                }
            }
            
            reader.close();
        } catch (Exception e) {
            RescueFramework.log("Failed to load map from file: "+fileName);
            e.printStackTrace();
        }
        
        // Share walls between cells
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                cells[x][y].shareWalls(getCell(x,y-1), getCell(x+1,y), getCell(x,y+1), getCell(x-1,y));
            }
        }
        
        // Update cell neighbours
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                cells[x][y].updateAccessibleNeighbours();
            }
        }
        
        // Color floor for rooms
        for (int i=0; i<floorList.size(); i++) {
            floodFillFloor(floorList.get(i));
        }
        
        // Init agents
        if (startCell == null) startCell = getCell(0,0);
        Cell nextStartCell = startCell;
        for (int i=0; i<robotCount;i++) {
            Robot newRobot = new Robot(nextStartCell,this);
            robots.add(newRobot);
            nextStartCell = nextStartCell.getAccessibleNeigbour(1);
        }
        
        // Update agent visibility and repaint GUI
        updateAllRobotVisibleCells();
        RescueFramework.refresh();
    }
      
    /**
     * Get cached image identified by the string definition
     * @param image         String image definition
     * @return              Cached image
     */
    public BufferedImage getCachedImage(String image) {
        if (!imageCache.containsKey(image)) {
            // Image not yet cached
            try {
                BufferedImage img = ImageIO.read(new File("images/"+image+".png"));
                imageCache.put(image, img);
                return img;
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }  
        } else {
            // Image found in the cache
            return imageCache.get(image);
        }
    }
    
    /**
     * Create new obstacle on the map
     * @param x         X coordinate
     * @param y         Y coordinate
     * @param image     The obstacle image
     */
    public void crateObstacle(int x, int y, String image) {
        cells[x][y].setObstacleImage(image);
    }
    
    /**
     * Return the height of the map
     * @return          The height of the map
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Return the width of the map
     * @return          The width of the map
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Return cell at a given position
     * @param x         X coordinate
     * @param y         Y coordinate
     * @return          The cell or null if the coordinates are invalid
     */
    public Cell getCell(int x, int y) {
        if (x>=0 && x<width && y>=0 && y<height)
            return cells[x][y];
        else
            return null;
    }
    
    /**
     * Flood fill block of cells
     * @param floor     The floor object to start filling from
     */
    private void floodFillFloor(Floor floor) {
        int index = 0;
        ArrayList<Cell> cells = new ArrayList<Cell>();
        cells.add(getCell(floor.getX(), floor.getY()));
        
        // Loop through all acessible cells from the floor definition
        while (index<cells.size()) {
            Cell cell = cells.get(index);
            if (cell.isDoor()) {
                // Stop at doors
                index++;
                continue;
            }
            
            for (int direction = 0; direction<4; direction++) {
                Cell neighbour = cell.getAccessibleNeigbour(direction);
                if ( neighbour != null && cells.indexOf(neighbour) == -1) {
                    // Add all accessible neighbour
                    cells.add(neighbour);
                }
            }
            
            index++;
        }
        
        // Apply floor coloring to all cells found
        for (index=0; index<cells.size(); index++) {
            cells.get(index).setFloorColorIndex(floor.getColorCode());
        }
    }
    
    /**
     * Change the robot loation
     * @param robot         The robot to move
     * @param dir           The direction to move to
     * @return              True if the robot is able to move to the specified direction
     */
    public boolean moveRobot(Robot robot, Integer dir) {
        if (dir == null) {
            RescueFramework.log("Robot staying in place.");
            return false;
        }
        
        if (robot.getLocation().accessNeigbours[dir] != null) {
            return moveRobot(robot,robot.getLocation().accessNeigbours[dir]);
        } else {
            RescueFramework.log("Move failed: "+dir+" is inaccessible.");
            return false;
        }
    }
    
    /**
     * Change the robot location
     * @param robot         The robot to move
     * @param cell          The target cell to move the robot to
     * @return              True if the robot is able to move to the specified cell
     */
    public boolean moveRobot(Robot robot, Cell cell) { 
        // Avoid obstacles
        if (cell.hasObstacle()) {
            RescueFramework.log("Move failed: "+cell.getX()+" x "+cell.getY()+" is occupied by an obstacle.");
            return false;
        }
        
        // Drop injured on exti cell
        if (cell.isExit() && robot.hasInjured()) {
            Injured savedInjured = robot.dropInjured();
            savedInjured.setSaved();
            savedInjureds.add(savedInjured);
        }
        
        // Change location
        robot.setCell(cell);
        
        // Pick up new injured
        if (cell.hasInjured()) {
            if (!robot.hasInjured()) {
                cell.getInjured().setLocation(null);
                robot.pickupInjured();
            } else {
                RescueFramework.log("Move failed: "+cell.getX()+" x "+cell.getY()+" is occupied by an injured, and the robot is also carrying one.");
                return false;
            }
        }
        
        // Update robot visibility and GUI
        updateAllRobotVisibleCells();
        RescueFramework.refresh();
        return true;
    }
    
    /**
     * Update the visibility of all robots operating on the map
     */
    public void updateAllRobotVisibleCells() {
        // Reset visibility
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                cells[x][y].setRobotVisibility(false);
            }
        }
        
        // Reset viewlines and break points
        viewLines.clear();
        viewLineBreakPoints.clear();
        
        // Update robot visibility one by one
        for (int index=0; index<robots.size(); index++) {
            updateRobotVisibleCells(robots.get(index));
        }
    }
    
    /**
     * Update the visibility of a single robot
     * @param r             The robot to update visibility for
     */
    public void updateRobotVisibleCells(Robot r) {
        Cell c = r.getLocation();
        Cell targetCell = null;
        
        // Loop through all cells around
        for (int x = c.getX()-visibilityRange; x<=c.getX()+visibilityRange; x++) {
            for (int y = c.getY()-visibilityRange; y<=c.getY()+visibilityRange; y++) {
                targetCell = getCell(x, y);
                
                if (targetCell != null) {
                    // Skip cells to far away
                    if (Math.sqrt((Math.pow(targetCell.getX()-c.getX(),2))+Math.pow(targetCell.getY()-c.getY(),2))>visibilityRange+0.5) continue;
                    
                    // Check visibility
                    boolean visible = checkCellVisibility(c.getX(), c.getY(),targetCell.getX(), targetCell.getY());
                    if (visible) {
                        targetCell.setRobotVisibility(visible);
                    }
                    //viewLines.add(new ViewLine(c.getX()+0.5, c.getY()+0.5,targetCell.getX()+0.5, targetCell.getY()+0.5, visible));
                }
            }
        }
    }
    
    /**
     * Check visibility between two points in the X+ direction
     * @param x1_in     First point X coordinate
     * @param y1_in     First point Y coordinate
     * @param x2_in     Second point X coordinate
     * @param y2_in     Second point Y coordinate
     * @return          True if there is no wall in the way
     */
    public boolean checkCellVisibilityXPlus(int x1_in, int y1_in, int x2_in, int y2_in) {
        boolean logging = false;
        if (logging) RescueFramework.log("-------- Visibility check between "+x1_in+" x "+y1_in+" and "+x2_in+" x "+y2_in);
        
        double dx, dy, a, b;
        
        double x1 = x1_in + 0.5;
        double y1 = y1_in + 0.5;
        double x2 = x2_in + 0.5;
        double y2 = y2_in + 0.5;
        
        // y = a*x+b
        // x = (y-b)/a
        dx = x2-x1;
        dy = y2-y1;
        if (dx!=0) {
            a = (double)dy/(double)dx;
        } else {
            a = 0;
        }
        b = (double)y1-((double)x1*(double)a);
        
        int ydir = -1;
        if (y1<y2) ydir = 1;
        if (logging) RescueFramework.log("dx = "+dx+" dy = "+dy+";   "+b+" = "+y1+"-"+x1+"*"+a+";   y = "+a+"*x+"+b+";   x = (y-"+b+")/"+a+";   ydir="+ydir);
        if (logging) RescueFramework.log("Horizontal and corner check...");
        
        int xCell, yCell;
        double xx = 0, yy = 0;
        // Vertical wall test (only for non vertical lines)
        if (dx != 0) {
            xCell = x1_in+1;
            while (xCell <= x2_in) {
                yy = a*xCell+b;
                
                if (Math.abs(yy-Math.round(yy))<0.01) {
                    // Corner crossing
                    yCell = (int)Math.round(yy);
                    if (logging) RescueFramework.log("Checking x = "+xCell+" -> y="+yy+" -> corner crossing ("+yCell+")");
                    
                    if (ydir>0) {
                        // Direction \
                        if (cells[xCell-1][yCell-1].hasWall(1) && cells[xCell-1][yCell-1].hasWall(2)) {
                            if (logging) {
                                RescueFramework.log("Bottom right corner hit of "+(xCell-1)+" x "+(yCell-1));
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.BLUE));
                            }
                            return false;
                        } else if (cells[xCell][yCell].hasWall(0) && cells[xCell][yCell].hasWall(3)) {
                            if (logging) {
                                RescueFramework.log("Top left corner hit of "+(xCell)+" x "+(yCell));
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.CYAN));
                            }
                            return false;
                        } else if (cells[xCell-1][yCell-1].hasWall(1) && cells[xCell][yCell].hasWall(3)) {
                            if (logging) {
                                RescueFramework.log("Vertical wall hit on "+xCell+" x "+yCell);
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.YELLOW));
                            }
                            return false;
                        } else if (cells[xCell-1][yCell].hasWall(0) && cells[xCell][yCell].hasWall(0)) {
                            if (logging) {
                                RescueFramework.log("Horizontal wall hit on "+xCell+" x "+yCell);
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.YELLOW));
                            }
                            return false;
                        }
                    } else {
                        // Direction /
                        if (cells[xCell-1][yCell].hasWall(0) && cells[xCell-1][yCell].hasWall(1)) {
                            if (logging) {
                                RescueFramework.log("Top right corner hit of "+(xCell-1)+" x "+(yCell));
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.BLUE));
                            }
                            return false;
                        } else if (cells[xCell][yCell-1].hasWall(2) && cells[xCell][yCell-1].hasWall(3)) {
                            if (logging) {
                                RescueFramework.log("Bottom left corner hit of "+(xCell)+" x "+(yCell-1));
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.CYAN));
                            }
                            return false;
                        } else if (cells[xCell][yCell].hasWall(3) && cells[xCell][yCell-1].hasWall(3)) {
                            if (logging) {
                                RescueFramework.log("Vertical wall hit on "+xCell+" x "+yCell);
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.YELLOW));
                            }
                            return false;
                        } else if (cells[xCell-1][yCell].hasWall(0) && cells[xCell][yCell].hasWall(0)) {
                            if (logging) {
                                RescueFramework.log("Horizontal wall hit on "+xCell+" x "+yCell);
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.YELLOW));
                            }
                            return false;
                        }
                    }
                } else {
                    // Wall crossing
                    yCell = (int)Math.floor(yy);
                    if (logging) RescueFramework.log("Checking x = "+xCell+" -> y="+yy+" -> regular crossing. ("+yCell+")"); 
                    
                    if (cells[xCell][yCell].hasWall(3)) {
                        if (logging) {
                            RescueFramework.log("Vertical wall hit at x = "+xCell+" -> y="+yy); 
                            viewLineBreakPoints.add(new ViewLineBreakPoint(xCell,yy,Color.RED));
                        }
                        return false;
                    }
                }
                
                xCell++;
            }
        } else {
            if (logging) RescueFramework.log("Skipping horizontal line.");
        }
        
        // Horizontal wall test (only for non vertical lines) 
        if (logging) RescueFramework.log("Vertical check...");
        if (dy != 0) {  
            if (ydir>0) {
                // Direction \
                
                yCell = y1_in+1;
                while (yCell <= y2_in) {
                    if (a != 0) {
                        xx = (yCell-b)/a;
                    } else {
                        xx = x1_in+0.5;
                    }

                    xCell = (int)Math.floor(xx);
                    if (logging) RescueFramework.log("Checking y="+yCell+" -> x = "+xx+" -> regular crossing. ("+xCell+" x "+yCell+")"); 
                    
                    if (Math.abs(xCell-xx)>0.01) {
                        if (cells[xCell][yCell].hasWall(0)) {
                            if (logging) {
                                RescueFramework.log("Top wall hit on "+xCell+" x "+yCell);
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xx,yCell,Color.lightGray));
                            }
                            return false;
                        }
                    }
                    
                    yCell++;
                }
                
            } else {
                // Direction /
                
                yCell = y1_in;
                while (yCell > y2_in) {
                    if (a != 0) {
                        xx = (yCell-b)/a;
                    } else {
                        xx = x1_in+0.5;
                    }

                    xCell = (int)Math.floor(xx);
                    if (logging) RescueFramework.log("Checking y="+yCell+" -> x = "+xx+" -> regular crossing. ("+xCell+" x "+yCell+")"); 
                    
                    if (Math.abs(xCell-xx)>0.001) {
                        if (cells[xCell][yCell].hasWall(0)) {
                            if (logging) {
                                RescueFramework.log("Bottom wall hit on "+xCell+" x "+yCell);
                                viewLineBreakPoints.add(new ViewLineBreakPoint(xx,yCell,Color.lightGray));
                            }
                            
                            return false;
                        }
                    }
                    yCell--;
                }
            }
        } else {
            if (logging) RescueFramework.log("Skipping vertical line.");
        }    
        
        return true;
    }
    
        /**
     * Check visibility between two points
     * @param x1_in     First point X coordinate
     * @param y1_in     First point Y coordinate
     * @param x2_in     Second point X coordinate
     * @param y2_in     Second point Y coordinate
     * @return          True if there is no wall in the way
     */
    public boolean checkCellVisibility(int x1_in, int y1_in, int x2_in, int y2_in) {
        // The cell always sees itself
        if (x1_in == x2_in && y1_in == y2_in) return true;
        
        // Points above each other
        if (x1_in == x2_in) {
            if (y1_in<y2_in) {
                return checkCellVisibilityXPlus(x1_in, y1_in, x2_in, y2_in);
            } else {
                return checkCellVisibilityXPlus(x2_in, y2_in, x1_in, y1_in);
            }
        } 
        
        // Points next to each other
        if (x1_in<=x2_in) {
            return checkCellVisibilityXPlus(x1_in, y1_in, x2_in, y2_in);
        } else {
            return checkCellVisibilityXPlus(x2_in, y2_in, x1_in, y1_in);
        }
    }
    
    /**
     * Make one step in time
     * 
     * @param stepRobots            If true robots are requetsed to step
     */
    public void stepTime(boolean stepRobots) {
        time++;
        RescueFramework.log(" ---  Step "+time+"");
        
        // Calculate injured states
        for (int i=0; i<injureds.size(); i++) {
            Injured injured = injureds.get(i);
            if (!injured.isSaved()) {
                int prevHealth = injured.getHealth();
                if (prevHealth>0) {
                    prevHealth--;
                    injured.setHealth(prevHealth);
                }
            }
        }
        
        // Display robot paths
        displayPaths.clear();
        //long start = System.currentTimeMillis();
        boolean movingRobot = false;
        for (int i=0; i<robots.size(); i++) {
            Robot robot = robots.get(i);
            
            if (stepRobots) {
                Integer stepDir = robot.step();
                if (stepDir == null) {
                    RescueFramework.log("R"+i+" @ "+robot.getLocation().toString()+" -> sleep");
                } else {
                    RescueFramework.log("R"+i+" @ "+robot.getLocation().toString()+" -> "+stepDir);
                    if (moveRobot(robot, stepDir)) {
                        movingRobot = true;
                    }
                }
            }
            
            Path p = getShortestExitPath(robot.getLocation());
            if (p != null) {
                p.setColor(Color.GREEN);
                displayPaths.add(p);
            }
            
            p = getShortestUnknownPath(robots.get(i).getLocation());
            if (p != null) {
                p.setColor(Color.DARK_GRAY);
                displayPaths.add(p);
            }
            
            p = getShortestInjuredPath(robots.get(i).getLocation());
            if (p != null) {
                p.setColor(Color.RED);
                displayPaths.add(p);
            } 
        }
        //long end = System.currentTimeMillis();
        //RescueFramework.log("Robot decision time: "+(end-start)+" ms");
        
        if (stepRobots && !movingRobot) {
            RescueFramework.log("No moving robot. Pausing simulation.");
            RescueFramework.pause();
        }
        
        if (stepRobots && (injureds.size() == savedInjureds.size())) {
            RescueFramework.log("All injuredare outside. Pausing simulation.");
            RescueFramework.pause();
        }
        
        RescueFramework.refresh();
    }
    
    public int getTime() {
        return time;
    }
    
    public Cell getPathFirstCell(Cell from, Cell to) {
        
        return null;
    }
    
    public ArrayList<Cell> getExitCells() {
        return exitCells;
    }
    
    public ArrayList<Injured> getDiscoveredInjureds() {
        ArrayList<Injured> result = new ArrayList<Injured>();
        for (int i=0; i<injureds.size(); i++) {
            if (injureds.get(i).isDiscovered()) result.add(injureds.get(i));
        }
        return result;
    }
    
    public ArrayList<Cell> getUnknownCells() {
        ArrayList<Cell> result = new ArrayList<Cell>();
        
         for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                if (!cells[x][y].isDiscovered()) {
                    result.add(cells[x][y]);
                }
            }
        }
        
        return result;
    }
    
    public Path getShortestPath(Cell start, ArrayList<Cell> targetCells) {
        if (targetCells.size() == 0) {
            return null;
        }
        
        int bestLength = -1;
        Path bestPath = AStarSearch.search(start, targetCells.get(0),-1);
        if (bestPath != null) bestLength = bestPath.getLength();
        
        for (int i=1; i<targetCells.size(); i++) {
            
            Path thisPath = AStarSearch.search(start, targetCells.get(i), bestLength);
            if (thisPath != null && (bestPath == null || thisPath.getLength()<bestPath.getLength())) {
                bestPath = thisPath;
                bestLength = bestPath.getLength();
            }
        }
        
        return bestPath;
    }
    
    
    public Path getShortestExitPath(Cell start) {
        return getShortestPath(start, exitCells);
    }
    
    public Path getShortestUnknownPath(Cell start) {
        return getShortestPath(start, getUnknownCells());
    }
    
    public Path getShortestInjuredPath(Cell start) {
        ArrayList<Injured> knownInjuredList = getDiscoveredInjureds();
        ArrayList<Cell> cellList = new ArrayList<>();
        for (int i=0; i<knownInjuredList.size(); i++) {
            Cell location = knownInjuredList.get(i).getLocation();
            if (location != null) {
                cellList.add(location);
            }
        }
        return getShortestPath(start, cellList);
    }
    
    public int getScore() {
        int score = 0;
        for (int i=0; i<savedInjureds.size(); i++) {
            if (savedInjureds.get(i).isAlive()) score++;
        }
        return score;
    }
    
    public int getMaxScore() {
        return injureds.size();
    }
    
    public ArrayList<Robot> getRobots() {
        return robots;
    }
    
    public ArrayList<Injured> getSavedInjureds() {
        return savedInjureds;
    }
    
    public ArrayList<Path> getDisplayPaths() {
        return displayPaths;
    }

    
}
