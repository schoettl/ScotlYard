package kj.scotlyard.graphbuilder.builder;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;

import kj.scotlyard.game.graph.ConnectionEdge;
import kj.scotlyard.game.graph.StationVertex;

/**
 * Dies ist ein Director, der dem Builder die Arbeitsanweisungen gibt.
 * Es ist natuerlich nicht der einzig wahre Director; Das BuilderTool
 * ist z.B. auch einer.
 * @author jakob190590
 *
 */
public final class Director {
	
	/** No instances */
	private Director() { }
	
	/**
	 * Dies ist nur ein Testprogramm.
	 * Als Argument sollte der Dateiname einer Description Datei
	 * uebergeben werden.
	 * @param args
	 */
	public static void main(String... args) {
		if (args.length == 1) {
			
			GraphDescriptionBuilder descBuilder = new GraphDescriptionBuilder();
			GameGraphBuilder graphBuilder = new GameGraphBuilder();
			
			// Nur zum testen, ob sie fehlerlos durchlaufen:
			ToolGraphBuilder toolBuilder = new ToolGraphBuilder();
			
			try {
				construct(args[0], descBuilder, graphBuilder, toolBuilder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println(descBuilder.getDescription());
			System.out.println(graphBuilder.getGameGraph() + ": " 
					+ graphBuilder.getGameGraph().getGraph().vertexSet().size() + " vertices, " 
					+ graphBuilder.getGameGraph().getGraph().edgeSet().size() + " edges");
			
		} else {
			System.out.println("One argument expected: description file pathname.");
		}
	}
	
	/**
	 * Liest aus der angegebenen Description Datei, und sagt den angegebenen Buildern,
	 * was zu tun ist.
	 * @param graphDescriptionFilename Pfad zur Description Datei. Der Aufbau der Datei wird
	 * im <tt>GraphDescriptionBuilder</tt> festgelegt!
	 * @param builders die Builder, denen der Director die Anweisungen geben soll.
	 * @throws IOException bei I/O-Fehler oder ungueltigem Aufbau der Datei.
	 */
	@SuppressWarnings("unchecked")
	public static void construct(String graphDescriptionFilename, GraphBuilder... builders) throws IOException {
		Scanner sc = new Scanner(new File(graphDescriptionFilename));
		
		try {
			while (sc.hasNext()) {
				String elementType = sc.next();
				if (elementType.equals("V")) {
					Class<? extends StationVertex> vertexType = (Class<? extends StationVertex>) Class.forName(sc.next());
					int number = Integer.valueOf(sc.next());
					double x = Double.valueOf(sc.next());
					double y = Double.valueOf(sc.next());
					Point2D.Double pos = new Point2D.Double(x, y);
					for (GraphBuilder graphBuilder : builders) {
						graphBuilder.addVertex(vertexType, number, pos);
					}
					
				} else if (elementType.equals("E")) {
					Class<? extends ConnectionEdge> edgeType = (Class<? extends ConnectionEdge>) Class.forName(sc.next());
					int v1 = Integer.valueOf(sc.next());
					int v2 = Integer.valueOf(sc.next());
					for (GraphBuilder graphBuilder : builders) {
						graphBuilder.addEdge(edgeType, v1, v2);
					}
					
				} else {
					throw new ParseException("Illegal graph element type selector: " + elementType + ". Must be 'V' or 'E' (Vertex/Edge).", 0);
				}
			}
		} catch (Exception e) {
			throw new IOException("Corrupt description file!", e);
		}
	}

}