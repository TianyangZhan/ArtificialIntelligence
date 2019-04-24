import java.util.Comparator;
import java.util.Collections;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/// A sample AI that takes a very suboptimal path.
/**
 * Dijkstra Algorithm
 * @author Kendall Lui
 */
public class DijkstraAI implements AIModule
{
    
    static class PointSorter implements Comparator<Point> {
        private double[][] map;
        public PointSorter(final double[][] map) {
            this.map = map;
        }
        
        public int compare(Point one, Point two)
        {
            double oneCost = map[one.x][one.y];
            double twoCost = map[two.x][two.y];
            return Double.compare(oneCost, twoCost);
        }
    }
    
    // Creates the path to the goal using Dijkstra's
    public List<Point> createPath(final TerrainMap map)
    {
        
        double[][] dijkstraVals = new double[map.getWidth()][map.getHeight()]; // Creates a board containing all the Dijkstra Path cost value
        Point[][] paths = new Point[map.getWidth()][map.getHeight()]; // Adjacency Matrix for paths.
        for(double[] column: dijkstraVals) {
            Arrays.fill(column, Double.MAX_VALUE);
        }
        
        //Initialize Fringe Queue
        PointSorter sorter = new PointSorter(dijkstraVals);
        PriorityQueue<Point> fringeQueue = new PriorityQueue<Point>(map.getWidth(), sorter);
        
        //Dijkstra's
        Point EndPoint = map.getEndPoint(); //Store Endpoint
        
        Point CurrentPoint = map.getStartPoint();
        Point[] Points;
        dijkstraVals[CurrentPoint.x][CurrentPoint.y] = 0;
        fringeQueue.add(CurrentPoint);
        while(!CurrentPoint.equals(EndPoint)) { // Terminates upon reaching goal state.
            CurrentPoint = fringeQueue.peek();
            fringeQueue.remove(CurrentPoint);
            Points = map.getNeighbors(CurrentPoint);
            
            for(Point p: Points) {
                double total_cost = dijkstraVals[CurrentPoint.x][CurrentPoint.y] + map.getCost(CurrentPoint,p);
                if(total_cost < dijkstraVals[p.x][p.y]) {
                    dijkstraVals[p.x][p.y] = total_cost;
                    fringeQueue.add(new Point(p));
                    paths[p.x][p.y] = CurrentPoint;
                }
            }
        }
        
        Point StartPoint = map.getStartPoint();
        final ArrayList<Point> path = new ArrayList<Point>(); //Stores final path
        path.add(new Point(CurrentPoint));
        while(!CurrentPoint.equals(StartPoint))
        {
            CurrentPoint = paths[CurrentPoint.x][CurrentPoint.y];
            path.add(CurrentPoint);
        }
        Collections.reverse(path);
        return path;
        
    }
}
