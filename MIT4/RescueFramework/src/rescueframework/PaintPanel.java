package rescueframework;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JPanel;
import world.Cell;
import world.Injured;
import world.Map;
import world.Path;
import world.Robot;

/**
 * Display panel of the map
 */
public class PaintPanel extends JPanel{  
    // Array for simple number indexing the colors
    private Color indexedColors[] = new Color[9];
    
    // Calculated cell size based on the map size and the actual panel size
    private int cellSize = 0;
    
    // Indicate if painting is already in progress
    public static boolean paintingInProgress = false;
    
    /**
     * Constructor of the pain panel
     */
    public PaintPanel() {
        // Init the parent class, set double buffering and background color
        super();
        setDoubleBuffered(true);
        setBackground(new Color(219, 219, 219));
        
        // Set fixed indexed colors
        indexedColors[0] = new Color(255, 244, 229);
        indexedColors[1] = new Color(184,184,184);
        indexedColors[2] = Color.blue;
        indexedColors[3] = Color.yellow;
        indexedColors[4] = Color.cyan;
        indexedColors[5] = Color.magenta;
        indexedColors[6] = Color.orange;
        indexedColors[7] = Color.gray;
        indexedColors[8] = Color.white;
    }
    
    /**
     * Override the paintComponent method to display map content
     * 
     * @param g     The Graphics object to draw to
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Get the map from the RescueFramework
        Map map = RescueFramework.map;
        if (map == null) {
            return;
        }
        
        // Calculate cellSize
        int cellWidth = (int)Math.floor(getWidth()/map.getWidth());
        int cellHeight = (int)Math.floor(getHeight()/map.getHeight());
        cellSize = Math.min(cellWidth, cellHeight);
        
        // Convert Graphics to Graphics2D
        Graphics2D g2 = (Graphics2D) g;
        
        // Paint cells one by one
        for (int x = 0; x<map.getWidth(); x++) {
            for (int y = 0; y<map.getHeight(); y++) {
                Cell cell = map.getCell(x, y);
                
                // Paint custom floor color
                if (cell.getFloorColorIndex()>=0) {
                    g2.setColor(indexedColors[cell.getFloorColorIndex()]);
                    g2.setStroke(new BasicStroke(0));
                    g2.fillRect(x*cellSize+1, y*cellSize+1, cellSize-1, cellSize-1);
                }

                // Paint robot visibility range
                if (cell.robotSeesIt()) {
                    g2.setColor(new Color(255,0,0,24));
                    g2.setStroke(new BasicStroke(0));
                    g2.fillRect(x*cellSize+1, y*cellSize+1, cellSize-1, cellSize-1);
                }

                // Display cell coordinates on each cell
                /*g2.setColor(Color.black);
                g2.drawString(x+" x "+y, x*cellSize, y*cellSize+20);*/

                // Display obstacle on the cell
                String cellImage = cell.getObstacleImage();
                if (!cellImage.isEmpty()) {
                    g2.drawImage(map.getCachedImage(cellImage), x*cellSize+1, y*cellSize+1, cellSize-1, cellSize-1, null);
                }

                // Display injured on the cell
                drawInjured(g2, cell, cellSize, cell.getInjured());

                // Display wall code on the firs row
                /*if (y == 0) {
                    if (cell.getWallCode()>0) g2.drawString(String.valueOf(cell.getWallCode()), x*cellSize+15, y*cellSize+23);
                }*/

                // Label special cells
                if (cell.getCellType() == 1) {
                    // Exit cell
                    g2.setColor(Color.red);
                    drawCenteredString(g2,"EXIT", new Rectangle(x*cellSize,y*cellSize,cellSize, cellSize), g2.getFont());

                } else if (cell.getCellType() == 2) {
                    // Start cell
                    g2.setColor(Color.red);
                    drawCenteredString(g2,"START", new Rectangle(x*cellSize,y*cellSize,cellSize, cellSize), g2.getFont());
                }

                // Draw fog if enabled
                if (RescueFramework.mainFrame.isFogEnabled()) {
                    if (!cell.isDiscovered()) {
                        g2.setColor(Color.black);
                        g2.setStroke(new BasicStroke(0));
                        g2.fillRect(x*cellSize, y*cellSize, cellSize, cellSize);
                    } else {
                        drawCellWalls(g2,cell,cellSize);
                    }
                } else {
                    drawCellWalls(g2,cell,cellSize);
                }
            }
        }
        
        // Draw agents
        for (int i=0; i<map.getRobots().size(); i++) {
            Robot robot = map.getRobots().get(i);
            int x = robot.getLocation().getX();
            int y = robot.getLocation().getY();
            g2.drawImage(map.getCachedImage("robot1"), x*cellSize+1, y*cellSize+1, cellSize-1, cellSize-1, null);
                      
            // Draw injured if the agent carries one
            drawInjured(g2, robot.getLocation(), cellSize, robot.getInjured());
        }
        
        // Draw saved patients on the bottom row
        int x = 0;
        int y = map.getHeight()-1;
        for (int i=0; i<map.getSavedInjureds().size(); i++) {
            drawInjured(g2, map.getCell(x, y), cellSize, map.getSavedInjureds().get(i));
            x++;
        }
        
        // Draw cell grid above all
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 1}, 0));
        
        // Y axis
        for (x = 0; x<=map.getWidth(); x++) {
            g2.drawLine(x*cellSize, 0, x*cellSize, map.getHeight()*cellSize);
        }
        
        // X axis
        for (y = 0; y<=map.getHeight(); y++) {
            g2.drawLine(0, y*cellSize, map.getWidth()*cellSize, y*cellSize);
        }
        
        // Draw walls where needed after all
        for (x = 0; x<map.getWidth(); x++) {
            for (y = 0; y<map.getHeight(); y++) {
                Cell cell = map.getCell(x, y);
                if (RescueFramework.mainFrame.isFogEnabled()) {
                    if (cell.isDiscovered()) {
                        drawCellWalls(g2,cell,cellSize);
                    }
                } else {
                    drawCellWalls(g2,cell,cellSize);
                }  
            }
        }
        
        // Draw closest exit/injured/undiscovered paths
        g2.setStroke(new BasicStroke(3));
        for (int i=0; i<map.getDisplayPaths().size(); i++) {
            Path path = map.getDisplayPaths().get(i);
            if (path == null) continue;
            
            g2.setColor(path.getColor());
            if (path == null || path.getPath().size()<2) continue;
            for (int j=0; j<path.getPath().size()-1; j++) {
                g2.drawLine(
                        (int)(((double)path.getPath().get(j).getX()+0.5)*cellSize), 
                        (int)(((double)path.getPath().get(j).getY()+0.5)*cellSize), 
                        (int)(((double)path.getPath().get(j+1).getX()+0.5)*cellSize), 
                        (int)(((double)path.getPath().get(j+1).getY()+0.5)*cellSize));
            }
        }
        
        
        // Draw view lines of the robot
        /*
        g2.setStroke(new BasicStroke(3));
        for (int i=0; i<map.viewLines.size(); i++) {
            ViewLine line = map.viewLines.get(i);
            if (line.success) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.RED);
            }
            g2.drawLine((int)(cellSize*line.x1), (int)(cellSize*line.y1), (int)(cellSize*line.x2), (int)(cellSize*line.y2));
        }
        
        // Draw view line end points
        for (int i=0; i<map.viewLineBreakPoints.size(); i++) {
            ViewLineBreakPoint point = map.viewLineBreakPoints.get(i);
            g2.setColor(point.color);
            g2.fillRect((int)(cellSize*point.x)-2, (int)(cellSize*point.y)-2, 5, 5);
        }
        */
        
        paintingInProgress = false;
    }
    
    /**
     * Draw injured on a cell or on a robot
     * 
     * @param g         The Graphics2D to draw to
     * @param cell      The cell to draw to
     * @param cellSize  Size of the cell to draw
     * @param injured   The injured to display
     */
    public void drawInjured(Graphics2D g, Cell cell, int cellSize, Injured injured) {
        if (injured == null) return;
        
        // Determine position
        int x = cell.getX();
        int y = cell.getY();
        
        // Choose alive/dead patient image
        String patientImage = "patient";
        if (injured.getHealth() == 0) {
            patientImage = "patient-dead";
        }
        
        //Draw the image
        g.drawImage(RescueFramework.map.getCachedImage(patientImage), x*cellSize+6, y*cellSize+2, cellSize-6, cellSize-12, null);
        
        // Draw the health bar borders and white fill
        g.setColor(Color.black);
        g.drawRect(x*cellSize+3, (y+1)*cellSize-10, cellSize-8, 6);
        g.setColor(Color.white);
        g.fillRect(x*cellSize+4, (y+1)*cellSize-9, cellSize-9, 5);
        
        // Draw the health value
        if (injured.getHealth() == 0) {
            g.setColor(Color.BLACK);
            g.fillRect(x*cellSize+4, (y+1)*cellSize-9, (int)((cellSize-9)), 5);
        } else {
            g.setColor(calculateColor(1-injured.getHealthRatio()));
            g.fillRect(x*cellSize+4, (y+1)*cellSize-9, (int)((cellSize-10)*injured.getHealthRatio()), 5);
        }
    }
    
    /**
     * Draw walls for a cell
     * 
     * @param g         The Graphics2D object to draw to
     * @param cell      The cell to process
     * @param cellSize  Size of the cells on the panel
     */
    public void drawCellWalls(Graphics2D g, Cell cell, int cellSize) {
        g.setStroke(new BasicStroke(5));
        g.setColor(Color.black);
        
        int x = cell.getX();
        int y = cell.getY();
        
        if (cell.hasWall(0)) g.drawLine((x)*cellSize+1, (y)*cellSize, (x+1)*cellSize-1, (y)*cellSize);
        if (cell.hasWall(1)) g.drawLine((x+1)*cellSize, (y)*cellSize+1, (x+1)*cellSize, (y+1)*cellSize-1);
        if (cell.hasWall(2)) g.drawLine((x)*cellSize+1, (y+1)*cellSize, (x+1)*cellSize-1, (y+1)*cellSize);
        if (cell.hasWall(3)) g.drawLine((x)*cellSize, (y)*cellSize+1, (x)*cellSize, (y+1)*cellSize-1);
    }
    
    /**
     * Display a string centered
     * @param g     The Graphics2D to draw to
     * @param text  The string to display
     * @param rect  The rect to center the text in
     * @param font  The font to be used
     */
    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }
    
    /**
     * Handle mouse clicks on the panel
     * 
     * @param x     X coordinate of the click
     * @param y     Y coordinate of the click
     */
    public void mouseClicked(int x, int y) {
        RescueFramework.log("Click at "+x+"x"+y);
        if (cellSize == 0) return;
        
        // Determine cell
        int cellX = x/cellSize;
        int cellY = y/cellSize;
        
        // Move the first robot if there is one
        Robot r = RescueFramework.map.getRobots().get(0);
        if (r != null) {
            RescueFramework.map.moveRobot(r, RescueFramework.map.getCell(cellX, cellY));
        }
    }
    
    /**
     * Calculate color based on percent value
     * 
     * @param value     Percent value
     * @return          Color belonging to the value
     */
    public Color calculateColor(float value) {
        double red = 0, green = 0, blue = 0;

        // First, green stays at 100%, red raises to 100%
        if (value<0.5) {        
            green = 1.0;
            red = 2 * value;
        }
        
        // Then red stays at 100%, green decays
        if (0.5<=value) {       
            red = 1.0f;
            green = 1.0 - 2 * (value-0.5);
        }
       
        return new Color((int)(255*red), (int)(255*green), (int)(255*blue));
    }
}
