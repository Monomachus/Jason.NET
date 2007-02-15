package jason.environment.grid;



/**
 * Simple model for a grid world (with Agents and obstacles)
 * 
 * @author Jomi
 */
public class GridWorldModel {

    public static final int       CLEAN    = 0;
    public static final int       AGENT    = 2;
    public static final int       OBSTACLE = 4;

    int                           width, height;
    public int[][]                data = null; 
    protected Location[]          agPos;
    protected GridWorldView       view;

    protected GridWorldModel(int w, int h, int nbAgs) {
        width  = w;
        height = h;

        // int data
        data = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = CLEAN;
            }
        }

        agPos = new Location[nbAgs];
        for (int i = 0; i < agPos.length; i++) {
            agPos[i] = new Location(-1, -1);
        }
    }

    public void setView(GridWorldView v) {
        view = v;
    }
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNbOfAgs() {
        return agPos.length;
    }

    public boolean inGrid(Location l) {
        return inGrid(l.x, l.y);
    }
    
    public boolean inGrid(int x, int y) {
        return y >= 0 && y < height && x >= 0 && x < width;
    }

    public boolean hasObject(int obj, Location l) {
        return hasObject(obj, l.x, l.y);
    }
    public boolean hasObject(int obj, int x, int y) {
        return inGrid(x, y) && (data[x][y] & obj) != 0;
    }

    // gets how many objects of some kind are in the grid
    public int countObjects(int obj) {
    	int c = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (hasObject(obj,i,j)) {
                	c++;
                }
            }
        }
        return c;
    }
    
    
    public void add(int value, Location l) {
        add(value, l.x, l.y);
    }

    public void add(int value, int x, int y) {
        data[x][y] |= value;
        if (view != null) view.update(x,y);
    }

    public void addWall(int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                add(OBSTACLE, x, y);
            }
        }
    }

    public void remove(int value, Location l) {
        remove(value, l.x, l.y);
    }

    public void remove(int value, int x, int y) {
        data[x][y] &= ~value;
        if (view != null) view.update(x,y);
    }

    public void setAgPos(int ag, Location l) throws Exception {
        Location oldLoc = getAgPos(ag);
    	//if (isFree(l)) {
        if (oldLoc != null) {
            remove(AGENT, oldLoc.x, oldLoc.y);
        }
        agPos[ag] = l;
        add(AGENT, l);
    	//} else if (oldLoc != null  && !oldLoc.equals(l)) { // just warns if the new location is different
    	//	throw new Exception("can not place the agent "+ag+" in "+l+" because it is not a free location.");
    	//}
    }

    public void setAgPos(int ag, int x, int y) throws Exception {
        setAgPos(ag, new Location(x, y));
    }

    public Location getAgPos(int ag) {
        try {
            if (agPos[ag].x == -1)
                return null;
            else
                return (Location)agPos[ag].clone();
        } catch (Exception e) {
            return null;
        }
    }

    /** return the agent at x,y */
    public int getAgAtPos(int x, int y) {
        for (int i=0; i<agPos.length; i++) {
            if (agPos[i].x == x && agPos[i].y == y) {
                return i;
            }
        }
        return -1;
    }

    public boolean isFree(Location l) {
        return isFree(l.x, l.y);
    }

    public boolean isFree(int x, int y) {
        return inGrid(x, y) && (data[x][y] & OBSTACLE) == 0 && (data[x][y] & AGENT) == 0;
    }

    public boolean isFreeOfObstacle(Location l) {
        return isFreeOfObstacle(l.x, l.y);
    }

    public boolean isFreeOfObstacle(int x, int y) {
        return inGrid(x, y) && (data[x][y] & OBSTACLE) == 0;
    }

}
