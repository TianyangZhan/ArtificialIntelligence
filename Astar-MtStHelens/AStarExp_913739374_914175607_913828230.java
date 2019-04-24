import java.awt.Point;
import java.util.*;

public class AStarExp_913739374_914175607_913828230 implements AIModule {
    private Point StartPoint;
    private Point EndPoint;
    
    
    private class Node {
        Point point;
        double g;
        double f;
        Node parent;
        double dist;
        
        public Node(Point point, double g, double f, Node parent, double dist){
            this.point = point;
            this.g = g;
            this.f = f;
            this.parent = parent;
            this.dist = dist;
        }
    }
    
    private class NodeComp implements Comparator<Node>{
        public int compare(Node n1, Node n2){
            // f value
            if (n1.f < n2.f){
                return -1;
            }else if(n1.f > n2.f){
                return 1;
            }else{
                // break ties
                
                if (n1.dist < n2.dist){
                    return -1;
                }else if(n1.dist > n2.dist){
                    return 1;
                }else{
                    return 0;
                }
            }
        }
    }
    
    // *********************************************************************
    private double getHeuristic(final TerrainMap map, final Point p1, final Point p2) {
        double dh = map.getTile(p2) - map.getTile(p1);
        double numSteps = Math.max(Math.abs(p1.x-p2.x),Math.abs(p1.y-p2.y));
        double stepCost = dh >= 0 ? 2.0 : 0.5;
        if(dh < 0 && Math.abs(dh) > numSteps){
            return Math.pow(0.5, Math.abs(dh)/numSteps) * numSteps;
        }
        return Math.abs(dh)*stepCost + Math.max((numSteps-Math.abs(dh)),0);
        
    }
    
    private double getDist(Point p1){
        double dx = p1.x-this.EndPoint.x;
        double dy = p1.y-this.EndPoint.y;
        return Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2)) + (0.0006) * Math.abs(dx*(this.StartPoint.y-this.EndPoint.y)-(this.StartPoint.x-this.EndPoint.x)*dy);
    }
    
    // *********************************************************************
    
    public List<Point> AStarSearch(final TerrainMap map){
        
        LinkedList<Point> path = new LinkedList<Point>();
        HashSet<Point> Closed = new HashSet<Point>();
        PriorityQueue<Node> Open = new PriorityQueue<Node>(50, new NodeComp());
        Point start = this.StartPoint;
        Point end = this.EndPoint;
        Open.add(new Node(start, 0.0, getHeuristic(map, start, end), null, getDist(start)));
        
        while(Open.size() != 0){
            Node Current = Open.poll();
            Point currentPoint = Current.point;

            if(currentPoint.equals(end)){
                // reconstruct path
                while(Current != null){
                    path.addFirst(Current.point);
                    Current = Current.parent;
                }
                return path;
            }
            
            if(Closed.contains(currentPoint)) {
                continue;
            }
            
            Closed.add(Current.point);
            for(Point neighbor : map.getNeighbors(currentPoint)){
                double pathCost = Current.g + map.getCost(currentPoint, neighbor);
                Open.add(new Node(neighbor, pathCost, pathCost + getHeuristic(map, neighbor, end), Current, getDist(neighbor)));
            }
        }
        
        return path;
        
    }
    
    // Creates the path to the goal.
    public List<Point> createPath(final TerrainMap map){
        this.StartPoint = map.getStartPoint();
        this.EndPoint = map.getEndPoint();
        return AStarSearch(map);
    }
}
