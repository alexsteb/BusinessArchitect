package model;

import java.awt.geom.Point2D;
import java.util.List;

public class CornerInfo {
	public double distance = Double.MAX_VALUE;
	public Point2D pointOnLine = null;
	public List<String> tokens; //tokens describing line where point is located
	public double percentage = 0.0;
	public Point2D begin, end;

}
