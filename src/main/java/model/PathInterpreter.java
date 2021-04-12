package model;

import util.Point;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathInterpreter {

	private interface tokenLambda {
		Double get(int currentIndex, int add);
	}

	public static Path2D getPath(String pathText){
		Path2D returnPath = new Path2D.Double();
		returnPath.moveTo(0.0, 0.0);

		List<String> tokens = tokenize(pathText);
		Point p = new Point(0.0,0.0);

		List<String> allowedTokens = Arrays.asList("M", "m", "L", "l", "C", "c", "Q", "q");

		tokenLambda next = (int currentIndex, int add) -> Double.parseDouble(tokens.get(currentIndex + add));

		for (int x = 0; x < tokens.size(); x++) {
			String token = tokens.get(x);
			if (allowedTokens.contains(token)) {

				try {
					switch (token) {
						case "M" -> returnPath.moveTo(next.get(x, 1), next.get(x, 2));
						case "m" -> returnPath.moveTo(next.get(x, 1) + p.x, next.get(x, 2) + p.y);
						case "L" -> returnPath.lineTo(next.get(x, 1), next.get(x, 2));
						case "l" -> returnPath.lineTo(next.get(x, 1) + p.x, next.get(x, 2) + p.y);
						case "Q" -> returnPath.quadTo(next.get(x, 1), next.get(x, 2),next.get(x, 3), next.get(x,4));
						case "q" -> returnPath.quadTo(next.get(x, 1) + p.x, next.get(x, 2) + p.y,next.get(x, 3) + p.x, next.get(x,4) + p.y);
						case "C" -> returnPath.curveTo(next.get(x, 1), next.get(x, 2),next.get(x, 3), next.get(x,4), next.get(x,5), next.get(x,6));
						case "c" -> returnPath.curveTo(next.get(x, 1) + p.x, next.get(x, 2) + p.y,next.get(x, 3) + p.x, next.get(x,4) + p.y, next.get(x,5) + p.x, next.get(x,6) + p.y);
					}
				p = Point.fromPoint2D(returnPath.getCurrentPoint());
					
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					System.out.println(e);
				}
			}
			
		}
		
		return returnPath;
	}

	private enum TokenType {
		NONE, TEXT, NUMBER, UNRECOGNIZED
	}

	public static List<String> tokenize(String pathText) {
		List<String> tokens = new ArrayList<>();
		StringBuilder partialToken = new StringBuilder();

		TokenType type = TokenType.NONE;
		List<Character> allowedChars = Arrays.asList('M', 'm', 'L', 'l', 'C', 'c', 'Q', 'q');
		for (char ch: pathText.toCharArray()) {
			TokenType newType = TokenType.UNRECOGNIZED;

			if (allowedChars.contains(ch)) newType = TokenType.TEXT;
			if (Character.isDigit(ch) || ch == '.' || ch == '-') newType = TokenType.NUMBER;
			if (newType != type && type != TokenType.NONE) {
				if (type != TokenType.UNRECOGNIZED) tokens.add(partialToken.toString());
				partialToken = new StringBuilder();
			}
			partialToken.append(ch);
			type = newType;
		}
		if (type == TokenType.UNRECOGNIZED) return tokens;
		
		tokens.add(partialToken.toString());
		return tokens;
	}

/*	public static String ShortenText(String text) {
		List<String> tokens = tokenize(text);
		StringBuilder sb = new StringBuilder();
		for (String s : tokens) {
			if (s.equals("")) continue;
			sb.append(s);
			if (Character.isDigit(s.charAt(0)) || s.charAt(0) == '.' || s.charAt(0) == '-') sb.append(" ");
		}
		
		return sb.toString();
	}

	public static CornerInfo FindCorner(Point2D p, String path) {
		CornerInfo ci = new CornerInfo();
		List<String> tokens = tokenize(path);
		Point2D origin = new Point(0.0, 0.0);
		Point2D target = new Point(0.0, 0.0);
		
		List<String> subtokens = new ArrayList<>();
		
		tokens.add("M"); //for the final loop
		for (String token : tokens) {
			if (token.toUpperCase().equals("M") || token.toUpperCase().equals("L") || token.toUpperCase().equals("C")|| token.toUpperCase().equals("Q")) {
				if (subtokens.size() > 0) {
					
					String t = subtokens.get(0);
					
					try {
					if (t.toUpperCase().equals("M") && subtokens.size() >= 3) target = new Point(Double.parseDouble(subtokens.get(1)),Double.parseDouble(subtokens.get(2)));
					if (t.toUpperCase().equals("L") && subtokens.size() >= 3) target = new Point(Double.parseDouble(subtokens.get(1)),Double.parseDouble(subtokens.get(2)));
					if (t.toUpperCase().equals("Q") && subtokens.size() >= 5) target = new Point(Double.parseDouble(subtokens.get(3)),Double.parseDouble(subtokens.get(4)));
					if (t.toUpperCase().equals("C") && subtokens.size() >= 7) target = new Point(Double.parseDouble(subtokens.get(5)),Double.parseDouble(subtokens.get(6)));
					}catch (NumberFormatException e) {}
					
					CornerInfo _ci = new CornerInfo();
					PointAndPercentage PaP = FindNearestPoint(p, origin, subtokens);
					
					if (PaP != null && PaP.point != null) { //null if M command
						Point2D P = PaP.point;
						
						if (P != null) {
							_ci.pointOnLine = P;
							_ci.distance = p.distance(P);
							_ci.tokens = subtokens;
							_ci.percentage = PaP.percentage;
							_ci.begin = origin;
							_ci.end = target;
							
							if (ci.pointOnLine == null || _ci.distance < ci.distance) {
								ci = _ci;
							}
							
						}	
					}
					
					origin = target;
					
						
					
					subtokens = new ArrayList<>();
				}
			}
				
			subtokens.add(token);
			
			
		}
		
		return ci;	
	}
	
	
	//Checks what kind of curve type is provided and then uses the appropriate calculation
	public static PointAndPercentage FindNearestPoint(Point2D p, Point2D origin, List<String> tokens) {
		try {
		
			//Normalize locations, if relative -> Changes list by reference (but is needed later)
			if (tokens.get(0).equals("m") || tokens.get(0).equals("l") || tokens.get(0).equals("c") || tokens.get(0).equals("q")) {
				tokens.set(1, ""+ (Double.parseDouble(tokens.get(1))+origin.x));
				tokens.set(2, ""+ (Double.parseDouble(tokens.get(2))+origin.y));
				
			}
			if (tokens.get(0).equals("c") || tokens.get(0).equals("q")) {
				tokens.set(3, ""+ (Double.parseDouble(tokens.get(3))+origin.x));
				tokens.set(4, ""+ (Double.parseDouble(tokens.get(4))+origin.y));
			}
			if (tokens.get(0).equals("c")) {
				tokens.set(5, ""+ (Double.parseDouble(tokens.get(5))+origin.x));
				tokens.set(6, ""+ (Double.parseDouble(tokens.get(6))+origin.y));
			}
			tokens.set(0, tokens.get(0).toUpperCase());
			
			//Calculations
			if (tokens.get(0).toUpperCase().equals("M")) {
				return null;
				//return new model.PointAndPercentage(new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2))), 1.0);
			}
			
			if (tokens.get(0).toUpperCase().equals("L")) {
				Point2D l1 = origin;
				Point2D l2 = new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2)));
				
				//off the left side
				if (l2.distance(p) > l1.distance(l2) && l2.distance(p) > l1.distance(p)) {
					return new PointAndPercentage(l1,0.0);
				}
				//off the right side
				if (l1.distance(p) > l2.distance(l1) && l1.distance(p) > l2.distance(p)) {
					return new PointAndPercentage(l2,1.0);
				}
				
				
				double distance = distanceLinePoint(p, l1, l2);
				double Csq = Math.pow(l1.distance(p),2);
				double A = Math.sqrt(Csq - Math.pow(distance,2));
				double perc = A/(l1.distance(l2));
				
				return new PointAndPercentage(percOnLine(perc, l1, l2),perc);
				
			}
			
			if (tokens.get(0).toUpperCase().equals("C") || tokens.get(0).toUpperCase().equals("Q")) {
				//Duplicate control point for translation Q -> C
				if (tokens.get(0).toUpperCase().equals("Q")) {
					tokens.add(tokens.get(3));
					tokens.add(tokens.get(4));
					tokens.set(3, tokens.get(1));
					tokens.set(4, tokens.get(2));
				}
				
				//Calculate 100 points and find the nearest of them
				Point2D bestP = null;
				double bestX = 0.0;
				double shortestDistance = Double.MAX_VALUE;
				
				for (double x = 0.0; x < 1.0; x+=0.01) {
					Point2D AB = percOnLine(x, origin,  new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2))));
					Point2D BC = percOnLine(x, new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2))), new Point(Double.parseDouble(tokens.get(3)), Double.parseDouble(tokens.get(4))));
					Point2D CD = percOnLine(x, new Point(Double.parseDouble(tokens.get(3)), Double.parseDouble(tokens.get(4))), new Point(Double.parseDouble(tokens.get(5)), Double.parseDouble(tokens.get(6))));
					
					Point2D ABBC = percOnLine(x, AB, BC);
					Point2D BCCD = percOnLine(x, BC, CD);
					
					Point2D P = percOnLine(x, ABBC, BCCD);
					
					double distance = P.distance(p);
					
					
					if (distance < shortestDistance) {
						shortestDistance = distance;
						bestP = P;
						bestX = x;
					}
				}
				return new PointAndPercentage(bestP,bestX);
				
			}
			
			
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			return null;
		}
		
		return null;
		
	}
	
	private static Point2D percOnLine(double x, Point2D l1, Point2D l2) {
		//Return point at x% along line l1-l2
		return new Point(l1.x+x*(l2.x-l1.x),l1.y+x*(l2.y-l1.y));
	}

	private static double distanceLinePoint(Point2D point, Point2D l1, Point2D l2) {
		return Math.abs((l2.y - l1.y)*point.x - (l2.x - l1.x)*point.y + l2.x*l1.y - l2.y*l1.x) /
				Math.sqrt(Math.pow(l2.y-l1.y, 2.0) + Math.pow(l2.x-l1.x, 2.0));
	}

	
	static List<LineInfo> lines = new ArrayList<>();
	
	//Find the shortest path between origin and target (avoiding crossing wrong1 and wrong2) and returning perc% of the way. 
	public static Point2D FindEdge(CornerInfo origin, CornerInfo target, CornerInfo wrong1, CornerInfo wrong2, double perc,
			String path) {
		List<String> tokens = tokenize(path); //path is upcase and absolute (b/c of corner algorithm)
		
		if (path.equals("M 10.0 0.0 C 23.400000000000002 0.0 23.200000000000003 20.0 10.0 20.0 C -3.4000000000000004 20.0 -3.4000000000000004 0.0 10.0 0.0")) {
			
			
			float xy = 3;
			float yy = xy+3;
			float zz = yy+3;
		}
		
		//Make list of all lines in path and whether they contain one of the corners
		List<String> subtokens = new ArrayList<>();
		
		lines = new ArrayList<>();
		
		LineInfo firstLine = new LineInfo();
		Point2D begin = new Point(0.0, 0.0);
		Point2D end = new Point(0.0, 0.0);
		
		tokens.add("M");
		for (String token: tokens) {
			if (token.toUpperCase().equals("M") || token.toUpperCase().equals("L") || token.toUpperCase().equals("C")|| token.toUpperCase().equals("Q")) {
				if (subtokens.size() > 0) {
					
					String t = subtokens.get(0);
					
					try {
					if (t.toUpperCase().equals("M") && subtokens.size() >= 3) end = new Point(Double.parseDouble(subtokens.get(1)),Double.parseDouble(subtokens.get(2)));
					if (t.toUpperCase().equals("L") && subtokens.size() >= 3) end = new Point(Double.parseDouble(subtokens.get(1)),Double.parseDouble(subtokens.get(2)));
					if (t.toUpperCase().equals("Q") && subtokens.size() >= 5) end = new Point(Double.parseDouble(subtokens.get(3)),Double.parseDouble(subtokens.get(4)));
					if (t.toUpperCase().equals("C") && subtokens.size() >= 7) end = new Point(Double.parseDouble(subtokens.get(5)),Double.parseDouble(subtokens.get(6)));
					}catch (NumberFormatException e) {}
					
					LineInfo li = new LineInfo(); //enhanced info about lines, so transversing and pathfinding is possible
					li.tokens = subtokens;
					if (subtokens.equals(origin.tokens)) {
						li.hasOrigin = true;
						li.originPerc = origin.percentage;
						firstLine = li;
					}
					if (subtokens.equals(target.tokens)) {
						li.hasTarget = true;
						li.targetPerc = target.percentage;
					}
					if (subtokens.equals(wrong1.tokens)) {
						li.hasWrong = true;
						li.wrongPerc = wrong1.percentage;
					}
					if (subtokens.equals(wrong2.tokens)) {
						li.hasWrong = true;
						li.wrongPerc = wrong2.percentage;
					}
					li.begin = begin;
					li.end = end;
					
					if (li.begin != null) { //No Move-Command
						lines.add(li);
					}
					
					begin = end;
					
					subtokens = new ArrayList<>();
				}
			}
				
			subtokens.add(token);	
		}
		
		//Calculate line lengths and reset lines
		for (LineInfo li : lines) {
			li.CalculateLength();
			li.totalPathLength = 0.0;
		}
		
		//Now we have a list of lines with all information necessary to continue..
		//Find a shortest path through all lines with info abt. used lines, how to traverse each line and % of begin and end
		InfoPack ip = FindShortestRoute(firstLine, false, firstLine.originPerc, true, 0.0);
		
		if (ip == null) 
			return null; //No direct path found.
		
		//Traverse the path up to perc%
		double partialLength = perc * ip.length;
		
		//Test if target is on very first line
		double originLength = 0.0;
		if (!ip.reverses.get(0)) originLength = ip.lines.get(0).length * (1.0 - ip.lines.get(0).originPerc);
		if ( ip.reverses.get(0)) originLength = ip.lines.get(0).length * (ip.lines.get(0).originPerc);
		
		if (partialLength <= originLength) {
			if (!ip.reverses.get(0)) return percOnCurve((ip.lines.get(0).length * (ip.lines.get(0).originPerc) + partialLength)/ip.lines.get(0).length, ip.lines.get(0).tokens, ip.lines.get(0).begin, ip.lines.get(0).end);
			if ( ip.reverses.get(0)) return percOnCurve((ip.lines.get(0).length * (1.0-ip.lines.get(0).originPerc) + partialLength)/ip.lines.get(0).length, ip.lines.get(0).tokens, ip.lines.get(0).end, ip.lines.get(0).begin);		
		}
		partialLength -= originLength;
		
		//On any further line
		for (int c = 1; c < ip.reverses.size(); c++) {
			if (ip.lines.get(c).length >= partialLength) {
				if (!ip.reverses.get(c)) return percOnCurve(partialLength / ip.lines.get(c).length, ip.lines.get(c).tokens, ip.lines.get(c).begin, ip.lines.get(c).end);
				if ( ip.reverses.get(c)) return percOnCurve(1.0 - partialLength / ip.lines.get(c).length, ip.lines.get(c).tokens, ip.lines.get(c).end, ip.lines.get(c).begin);
			}
			partialLength -= ip.lines.get(c).length;
		}
		
		
		
		return null;
	}
	
	
	private static Point2D percOnCurve(double x, List<String> tokens, Point2D begin, Point2D end) {
		if (tokens.get(0).equals("L")) return percOnLine(x,begin,end);
		
		assert (tokens.get(0).equals("C"));
		
		Point2D AB = percOnLine(x, begin,  new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2))));
		Point2D BC = percOnLine(x, new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2))), new Point(Double.parseDouble(tokens.get(3)), Double.parseDouble(tokens.get(4))));
		Point2D CD = percOnLine(x, new Point(Double.parseDouble(tokens.get(3)), Double.parseDouble(tokens.get(4))), new Point(Double.parseDouble(tokens.get(5)), Double.parseDouble(tokens.get(6))));
		
		Point2D ABBC = percOnLine(x, AB, BC);
		Point2D BCCD = percOnLine(x, BC, CD);
		
		Point2D P = percOnLine(x, ABBC, BCCD);
		
		return P;
	}

	private static InfoPack FindShortestRoute(LineInfo currentLine, boolean reverse, double perc, boolean isFirstLine, double lengthSoFar) {
		if (currentLine.totalPathLength > 0.0) return null;
		
		if (currentLine.hasTarget) { //Target found
			if ((currentLine.hasWrong && !reverse && currentLine.targetPerc <= currentLine.wrongPerc)
					|| (currentLine.hasWrong && reverse && currentLine.targetPerc >= currentLine.wrongPerc)
					|| !currentLine.hasWrong) {
				
				if (currentLine.hasOrigin) {//direct connection
					if (!reverse) lengthSoFar -= currentLine.originPerc * currentLine.length;
					if (reverse) lengthSoFar -= (1.0-currentLine.originPerc) * currentLine.length;
					
				}
				
				if (!reverse) return new InfoPack(currentLine,lengthSoFar+ currentLine.targetPerc * currentLine.length, reverse);
				if (reverse) return new InfoPack(currentLine,lengthSoFar+ (1.0 - currentLine.targetPerc) * currentLine.length, reverse);
				
			} else {
				//End - Can't reach Target this way
				return null;
			}
		}
		
		//Current line has no target
		
		List<Point2D> edgePoints = new ArrayList<>();
		
		if (isFirstLine) {
			if (!currentLine.hasWrong || (currentLine.hasWrong && currentLine.wrongPerc >= currentLine.originPerc)) edgePoints.add(currentLine.begin);
			if (!currentLine.hasWrong || (currentLine.hasWrong && currentLine.wrongPerc <= currentLine.originPerc)) edgePoints.add(currentLine.end);
		} else {
			if (!currentLine.hasWrong) { //wrong edge + target edge was already dealt with. Any wrong corner would have to be touched
				if (reverse) edgePoints.add(currentLine.begin);
				if (!reverse) edgePoints.add(currentLine.end);
			}
		}
		
		//Make a list of all potential connecting lines
		List<LineInfo> beginCandidates = new ArrayList<>();
		List<LineInfo> endCandidates = new ArrayList<>();
		
		for (LineInfo line : lines) {
			for (Point2D ep : edgePoints) {
				if (line.begin.equals(ep) && line != currentLine && !line.tokens.get(0).equals("M")) beginCandidates.add(line);
				if (line.end.equals(ep) && line != currentLine && !line.tokens.get(0).equals("M")) endCandidates.add(line);
			}
		}
		
		List<InfoPack> ips = new ArrayList<>();
		for (LineInfo line : beginCandidates) {
			double lsf = lengthSoFar;
			if (isFirstLine && line.begin.equals(currentLine.begin)) lsf = currentLine.length * (perc);
			if (isFirstLine && !line.begin.equals(currentLine.begin)) lsf = currentLine.length * (1.0-perc);
			
			if (!isFirstLine) lsf = lengthSoFar + currentLine.length;
			currentLine.totalPathLength = lsf; //So we know when to a line was visited before and can be reached quicker
			InfoPack ip = FindShortestRoute(line,false,0.0,false, lsf);
			if (ip != null) ip.reverses.add(0,(line.begin.equals(currentLine.begin)));
			ips.add(ip);
		}
		for (LineInfo line : endCandidates) {
			double lsf = lengthSoFar;
			if (isFirstLine && line.end.equals(currentLine.begin)) lsf = currentLine.length * (perc);
			if (isFirstLine && !line.end.equals(currentLine.begin)) lsf = currentLine.length * (1.0-perc);
			if (!isFirstLine) lsf = lengthSoFar + currentLine.length;
			
			currentLine.totalPathLength = lsf;
			InfoPack ip = FindShortestRoute(line,true,0.0,false, lsf);
			if (ip != null) ip.reverses.add(0,(line.end.equals(currentLine.begin)));
			ips.add(ip);
		}
		InfoPack shortestRoute = null;
		double shortest = Double.MAX_VALUE;
		
		for (InfoPack ip: ips) {
			if (ip == null) continue;
			ip.lines.add(0,currentLine);
			
			if (ip.length < shortest) {
				shortest = ip.length;
				shortestRoute = ip;
			}
		}
		
		return shortestRoute;
	}
	
	//1-top, 2-right, 4-bottom, 8-left border. 
	//Come here, for all edge segments <> 0.5
	public static Point2D FindEdge(BoxShape bs, int edge, float segment, String path) {
		if (edge == 1) return FindEdge(bs.acNW, bs.acNE, bs.acSE, bs.acSW, segment, path);
		if (edge == 2) return FindEdge(bs.acNE, bs.acSE, bs.acSW, bs.acNW, segment, path);
		if (edge == 4) return FindEdge(bs.acSE, bs.acSW, bs.acNW, bs.acNE, segment, path);
		if (edge == 8) return FindEdge(bs.acSW, bs.acNW, bs.acNE, bs.acSE, segment, path);
		
		return null;
	}*/
}


class InfoPack {
	List<LineInfo> lines = new ArrayList<>();
	double length = 0.0;
	List<Boolean> reverses = new ArrayList<>();
	
	public InfoPack(LineInfo line, double length, boolean reverse) {
		lines.add(0,line);
		this.length = length;
		reverses.add(0,reverse);
	}
}


class LineInfo {
	public List<String> tokens = new ArrayList<>();
	public Point begin, end; //Line begin and end -> for finding connections
	public boolean hasOrigin = false;
	public boolean hasTarget = false;
	public boolean hasWrong = false;
	public double originPerc = 0.0;
	public double targetPerc = 0.0;
	public double wrongPerc = 0.0;
	public double length = 0.0; //a2+b2=c2
	
	public double totalPathLength = 0.0; //used for recursive function
	
	public void CalculateLength() {
		if (tokens.size() == 0) return;
		
		if (tokens.get(0).equals("L")) {
			length = Math.sqrt(Math.pow(end.x-begin.x, 2)+Math.pow(end.y-begin.y, 2));
		} 
		
		else if (tokens.get(0).equals("C") || tokens.get(0).equals("Q")) {
			//Duplicate control point for translation Q -> C
			if (tokens.get(0).toUpperCase().equals("Q")) {
				tokens.add(tokens.get(3));
				tokens.add(tokens.get(4));
				tokens.set(3, tokens.get(1));
				tokens.set(4, tokens.get(2));
				tokens.set(0, "C");
			}
			
			Point lastPoint = begin;
			
			double totalLength = 0.0;
			
			for (double x = 0.01; x <= 1.0; x+=0.01) {
				Point AB = percOnLine(x, begin,  new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2))));
				Point BC = percOnLine(x, new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2))), new Point(Double.parseDouble(tokens.get(3)), Double.parseDouble(tokens.get(4))));
				Point CD = percOnLine(x, new Point(Double.parseDouble(tokens.get(3)), Double.parseDouble(tokens.get(4))), new Point(Double.parseDouble(tokens.get(5)), Double.parseDouble(tokens.get(6))));
				
				Point ABBC = percOnLine(x, AB, BC);
				Point BCCD = percOnLine(x, BC, CD);
				
				Point P = percOnLine(x, ABBC, BCCD);
				
				totalLength += P.distance(lastPoint);
				
				
				lastPoint = P;
			}
			length = totalLength;
		}
		
	}
	
	private static Point percOnLine(double x, Point l1, Point l2) {
		//Return point at x% along line l1-l2
		return new Point(l1.x+x*(l2.x-l1.x),l1.y+x*(l2.y-l1.y));
	}
	
}


class PointAndPercentage {
	public Point point;
	public double percentage;

	public PointAndPercentage(Point point, double percentage) {
		this.point = point;
		this.percentage = percentage;
	}
}


