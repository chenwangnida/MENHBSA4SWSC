package wsc.problem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.NaiveLcaFinder;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;

import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.graph.ServiceInput;
import wsc.graph.ServiceOutput;
import wsc.owl.bean.OWLClass;

public class WSCInitializer {
	// NHSBSA settings
	public static Random random;
	public static final int population_size = 100;
	public static int dimension_size;
	public static final int MAX_NUM_ITERATIONS = 100;

	// local search settings
	public static int noOfls = 0;
	public static double Pls = 0.1; // probability of local search
	public static int numOfLS = 30; // the size of neighboring space
	public static int numOfLS4Group = 18;
	public static int numOfLS5Group = 20;

	public static double Pm = 0.1; // probability of local search

	// Constants with of order of QoS attributes
	public static final int TIME = 0;
	public static final int COST = 1;
	public static final int AVAILABILITY = 2;
	public static final int RELIABILITY = 3;

	// Fitness function weights
	public static final double W1 = 0.25;
	public static final double W2 = 0.25;
	public static final double W3 = 0.125;
	public static final double W4 = 0.125;
	public static final double W5 = 0.125;
	public static final double W6 = 0.125;
	public static final double exact = 1.00;
	public static final double plugin = 0.75;

	public static double MINIMUM_COST = Double.MAX_VALUE;
	public static double MINIMUM_TIME = Double.MAX_VALUE;
	public static double MINIMUM_RELIABILITY = 0;
	public static double MINIMUM_AVAILABILITY = 0;
	public static double MINIMUM_MATCHTYPE = 0;
	public static double MININUM_SEMANTICDISTANCE = 0;

	public static double MAXIMUM_COST = Double.MIN_VALUE;
	public static double MAXIMUM_TIME = Double.MIN_VALUE;
	public static double MAXIMUM_RELIABILITY = Double.MIN_VALUE;
	public static double MAXIMUM_AVAILABILITY = Double.MIN_VALUE;
	public static double MAXINUM_MATCHTYPE = 1;
	public static double MAXINUM_SEMANTICDISTANCE = 1;

	// data
	public static List<String> taskInput;
	public static List<String> taskOutput;
	public static InitialWSCPool initialWSCPool;
	public static DirectedGraph<String, DefaultEdge> ontologyDAG;
	public static final String rootconcept = "TOPNODE";
	public static Map<String, double[]> serviceQoSMap = new HashMap<String, double[]>();
	public static Map<String, Service> serviceMap = new HashMap<String, Service>();
	public static Map<Integer, Service> Index2ServiceMap = new HashMap<Integer, Service>();
	public static BiMap<Integer, String> serviceIndexBiMap = HashBiMap.create();
	public static Map<Integer, List<Integer>> layers4SerIndex = new HashMap<Integer, List<Integer>>();

	public static Table<String, String, Double> semanticMatrix;

	// logs settings
	public static String logName;

	// evaluation settings
	public static int evalMax = 20000;
	public static int evalStep = 200;
	public static int evalCounter = 0;

	// public static int NHMIteration = 0;
	public static int NHMCounter = 0;

	// time settings
	public static ArrayList<Long> initTime = new ArrayList<Long>();
	public static ArrayList<Long> time = new ArrayList<Long>();
	public static ArrayList<WSCIndividual> bestFitnessSoFar = new ArrayList<WSCIndividual>();
	public static Long initialisationStartTime;
	public static Long initialization;
	public static Long startTime;

	/**
	 * all the data load entry.
	 *
	 * @param lName,
	 *            taskFileName, serviceFileName, taxonomyFileName
	 */
	public WSCInitializer(String lName, String taskFileName, String serviceFileName, String taxonomyFileName,
			long seed) {

		initialisationStartTime = System.currentTimeMillis();
		logName = lName;
		random = new Random(seed);

		try {
			// register task
			initialTask(taskFileName);

			// register web services associated related ontology
			initialWSCPool = new InitialWSCPool(serviceFileName, taxonomyFileName);

			// construct ontology tree structure
			ontologyDAG = createOntologyDAG(initialWSCPool);

			// Filter web services in repository
			initialWSCPool.allRelevantService4Layers(taskInput, taskOutput);

		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

		dimension_size = WSCInitializer.initialWSCPool.getServiceSequence().size();
		// population_size = dimension_size * 2;

		// NHMIteration = (int) Math.floor(evalMax / population_size);

		// construct matrix storing all semantic quality for query
		semanticMatrix = HashBasedTable.create();
		createSemanticMatrix();

		System.out.println("All service: "
				+ (initialWSCPool.getSwsPool().getServiceList().size() + initialWSCPool.getServiceSequence().size())
				+ "; all relevant service: " + initialWSCPool.getServiceSequence().size() + "; semanticMatrix: "
				+ semanticMatrix.size());

		MapServiceToQoS(initialWSCPool.getServiceSequence());
		MapLayerSer2LayerIndex(initialWSCPool.getLayers());

		calculateNormalisationBounds(initialWSCPool.getServiceSequence(),
				initialWSCPool.getSemanticsPool().getOwlInstHashMap().size());

	}

	private void MapLayerSer2LayerIndex(Map<Integer, List<Service>> layers) {
		for (Integer mapId : layers.keySet()) {
			List<Integer> serIndex4Layer = new ArrayList<Integer>();
			for (Service ser : layers.get(mapId)) {
				serIndex4Layer.add(serviceIndexBiMap.inverse().get(ser.getServiceID()));
			}
			layers4SerIndex.put(mapId, serIndex4Layer);
		}
	}

	/**
	 * Parses the WSC task file with the given name, extracting input and output
	 * values to be used as the composition task.
	 *
	 * @param fileName
	 */
	private void initialTask(String fileName) {
		try {
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			org.w3c.dom.Node provided = doc.getElementsByTagName("provided").item(0);
			NodeList providedList = ((Element) provided).getElementsByTagName("instance");
			taskInput = new ArrayList<String>();
			for (int i = 0; i < providedList.getLength(); i++) {
				org.w3c.dom.Node item = providedList.item(i);
				Element e = (Element) item;
				taskInput.add(e.getAttribute("name"));
			}

			org.w3c.dom.Node wanted = doc.getElementsByTagName("wanted").item(0);
			NodeList wantedList = ((Element) wanted).getElementsByTagName("instance");
			taskOutput = new ArrayList<String>();
			for (int i = 0; i < wantedList.getLength(); i++) {
				org.w3c.dom.Node item = wantedList.item(i);
				Element e = (Element) item;
				taskOutput.add(e.getAttribute("name"));
			}
		} catch (ParserConfigurationException e) {
			System.out.println("Task file parsing failed...");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("Task file parsing failed...");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Task file parsing failed...");
			e.printStackTrace();
		}
	}

	/**
	 * Create a full Ontology tree with data loaded from initialSWSPool
	 *
	 * @param initialWSCPool
	 */

	private static DirectedAcyclicGraph<String, DefaultEdge> createOntologyDAG(InitialWSCPool initialWSCPool) {

		DirectedAcyclicGraph<String, DefaultEdge> g = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge.class);

		HashMap<String, OWLClass> owlClassMap = initialWSCPool.getSemanticsPool().getOwlClassHashMap();

		for (String concept : owlClassMap.keySet()) {
			g.addVertex(concept);

		}

		for (OWLClass owlClass : owlClassMap.values()) {
			if (owlClass.getSubClassOf() != null && !owlClass.getSubClassOf().equals("")) {
				String source = owlClass.getSubClassOf().getResource().substring(1);
				String target = owlClass.getID();
				g.addEdge(source, target);
			}
		}
		return g;
	}

	/**
	 * All parameter-related concepts are converted and pre-calculated semantic
	 * quality with their subsumed existing concepts
	 *
	 * @param fileName
	 */

	private void createSemanticMatrix() {

		Set<OWLClass> parameterconcepts = new HashSet<OWLClass>();

		// Load all parameter-related concepts from task-relevant web services

		for (Service ser : initialWSCPool.getServiceSequence()) {

			for (ServiceInput serInput : ser.getInputList()) {
				OWLClass pConcept = initialWSCPool.getSemanticsPool().getOwlClassHashMap()
						.get(initialWSCPool.getSemanticsPool().getOwlInstHashMap().get(serInput.getInput()).getRdfType()
								.getResource().substring(1));
				parameterconcepts.add(pConcept);

			}

			for (ServiceOutput serOutput : ser.getOutputList()) {

				OWLClass pConcept = initialWSCPool.getSemanticsPool().getOwlClassHashMap()
						.get(initialWSCPool.getSemanticsPool().getOwlInstHashMap().get(serOutput.getOutput())
								.getRdfType().getResource().substring(1));
				parameterconcepts.add(pConcept);

			}
		}

		// Load all parameter-related concepts from given task input and
		// required output

		for (String tskInput : WSCInitializer.taskInput) {
			OWLClass pConcept = initialWSCPool.getSemanticsPool().getOwlClassHashMap().get(initialWSCPool
					.getSemanticsPool().getOwlInstHashMap().get(tskInput).getRdfType().getResource().substring(1));
			parameterconcepts.add(pConcept);
		}

		for (String tskOutput : WSCInitializer.taskOutput) {
			OWLClass pConcept = initialWSCPool.getSemanticsPool().getOwlClassHashMap().get(initialWSCPool
					.getSemanticsPool().getOwlInstHashMap().get(tskOutput).getRdfType().getResource().substring(1));
			parameterconcepts.add(pConcept);
		}

		// System.out.println("All concepts involved in semantic calcu NO.: " +
		// parameterconcepts.size());

		for (OWLClass pCon : parameterconcepts) {
			for (OWLClass pCon0 : parameterconcepts) {
				// if the pCon or PCon all parent class equal to pCon0
				if (initialWSCPool.getSemanticsPool().isSemanticMatchFromConcept(pCon, pCon0)) {

					double similarity = CalculateSimilarityMeasure(WSCInitializer.ontologyDAG, pCon.getID(),
							pCon0.getID());

					semanticMatrix.put(pCon.getID(), pCon0.getID(), similarity);
				}
				// System.out.println(
				// "givenInput: " + pCon + " existInput: " + pCon0 + " Semantic
				// Quality: " + similarity);
			}
		}

		// if (WSCInitializer.semanticMatrix.get(giveninput, existInput)==null){
		// similarity = WSCInitializer.semanticMatrix.get(existInput,
		// giveninput);
		// }else{
		// similarity = WSCInitializer.semanticMatrix.get(giveninput,
		// existInput);
		// }

	}

	public static double CalculateSimilarityMeasure(DirectedGraph<String, DefaultEdge> g, String a, String b) {

		double similarityValue;
		// find instance related concept
		// OWLClass givenClass = semanticsPool.getOwlClassHashMap()
		// .get(semanticsPool.getOwlInstHashMap().get(giveninput).getRdfType().getResource().substring(1));
		// OWLClass relatedClass = semanticsPool.getOwlClassHashMap()
		// .get(semanticsPool.getOwlInstHashMap().get(existInput).getRdfType().getResource().substring(1));
		//
		// String a = givenClass.getID();
		// String b = relatedClass.getID();

		// find the lowest common ancestor
		String lca = new NaiveLcaFinder<String, DefaultEdge>(g).findLca(a, b);

		double N = new DijkstraShortestPath(g, WSCInitializer.rootconcept, lca).getPathLength();
		double N1 = new DijkstraShortestPath(g, WSCInitializer.rootconcept, a).getPathLength();
		double N2 = new DijkstraShortestPath(g, WSCInitializer.rootconcept, b).getPathLength();

		double sim = 2 * N / (N1 + N2);
		// System.out.println("SemanticDistance:" + sim + "
		// ##################");
		//
		// if (isNeighbourConcept(g, a, b) == true) {
		// double L = new DijkstraShortestPath(g, lca, a).getPathLength()
		// + new DijkstraShortestPath(g, lca, b).getPathLength();
		//
		// int D = MaxDepth(g) + 1;
		// int r = 1;
		// double simNew = 2 * N * (Math.pow(Math.E, -r * L / D)) / (N1 + N2);
		// // System.out.println("SemanticDistance2:" + simNew + "
		// // ##################");
		// similarityValue = simNew;
		// } else {
		// similarityValue = sim;
		// }

		return sim;
	}

	private void MapServiceToQoS(List<Service> serviceList) {
		int i = 0;
		for (Service service : serviceList) {
			service.serviceIndex = i;
			service.getInputList().forEach(input -> input.setServiceId(service.getServiceID()));
			service.getOutputList().forEach(output -> output.setServiceId(service.getServiceID()));
			serviceMap.put(service.getServiceID(), service);
			serviceQoSMap.put(service.getServiceID(), service.getQos());
			serviceIndexBiMap.put(i, service.getServiceID());
			Index2ServiceMap.put(i, service);
			i += 1;
		}

	}

	private void calculateNormalisationBounds(List<Service> services, int instSize) {
		for (Service service : services) {
			double[] qos = service.getQos();

			// Availability
			double availability = qos[AVAILABILITY];
			if (availability > MAXIMUM_AVAILABILITY)
				MAXIMUM_AVAILABILITY = availability;

			// Reliability
			double reliability = qos[RELIABILITY];
			if (reliability > MAXIMUM_RELIABILITY)
				MAXIMUM_RELIABILITY = reliability;

			// Time
			double time = qos[TIME];
			if (time > MAXIMUM_TIME)
				MAXIMUM_TIME = time;
			if (time < MINIMUM_TIME)
				MINIMUM_TIME = time;

			// Cost
			double cost = qos[COST];
			if (cost > MAXIMUM_COST)
				MAXIMUM_COST = cost;
			if (cost < MINIMUM_COST)
				MINIMUM_COST = cost;
		}
		// Adjust max. cost and max. time based on the number of services in
		// shrunk repository
		MAXIMUM_COST *= services.size();
		MAXIMUM_TIME *= services.size();
		// MAXINUM_SEMANTICDISTANCE *= instSize / 2;

	}

}
