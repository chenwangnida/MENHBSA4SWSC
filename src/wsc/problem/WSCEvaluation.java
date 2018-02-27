package wsc.problem;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;

import wsc.graph.ServiceEdge;

public class WSCEvaluation {

	public void aggregationAttribute(WSCIndividual individual, DirectedGraph<String, ServiceEdge> directedGraph) {

		double a = 1.0;
		double r = 1.0;
		double t = 0.0;
		double c = 0.0;
		double mt = 1.0;
		double dst = 0.0; // Exact Match dst = 1 ; 0 < = dst < = 1

		// set a, r, c aggregation
		Set<String> verticeSet = directedGraph.vertexSet();

		// Map<String, double[]> SerQoSMap = serviceQoSMap;

		for (String v : verticeSet) {
			if (!v.equals("startNode") && !v.equals("endNode")) {
				double qos[] = WSCInitializer.serviceQoSMap.get(v);
				a *= qos[WSCInitializer.AVAILABILITY];
				r *= qos[WSCInitializer.RELIABILITY];
				c += qos[WSCInitializer.COST];

			}
		}

		// set time aggregation
		t = getLongestPathVertexList(directedGraph, WSCInitializer.serviceQoSMap);

		// set mt,dst aggregation

		for (ServiceEdge serviceEdge : directedGraph.edgeSet()) {
			mt *= serviceEdge.getAvgmt();
			dst += serviceEdge.getAvgsdt();
		}

		individual.setMatchingType(mt);
		individual.setSemanticDistance(dst / directedGraph.edgeSet().size());
		individual.setAvailability(a);
		individual.setReliability(r);
		individual.setTime(t);
		individual.setCost(c);
		individual.setStrRepresentation(directedGraph.toString());
	}

	public double calculateFitness(WSCIndividual individual) {

		double mt = individual.getMatchingType();
		double dst = individual.getSemanticDistance();
		double a = individual.getAvailability();
		double r = individual.getReliability();
		double t = individual.getTime();
		double c = individual.getCost();

		mt = normaliseMatchType(mt);
		dst = normaliseDistanceValue(dst);
		a = normaliseAvailability(a);
		r = normaliseReliability(r);
		t = normaliseTime(t);
		c = normaliseCost(c);

		individual.fitness = ((WSCInitializer.W1 * mt) + (WSCInitializer.W2 * dst) + (WSCInitializer.W3 * a)
				+ (WSCInitializer.W4 * r) + (WSCInitializer.W5 * t) + (WSCInitializer.W6 * c));

		return individual.fitness;
	}

	private double normaliseMatchType(double matchType) {
		if (WSCInitializer.MAXINUM_MATCHTYPE - WSCInitializer.MINIMUM_MATCHTYPE == 0.0)
			return 1.0;
		else
			return (matchType - WSCInitializer.MINIMUM_MATCHTYPE)
					/ (WSCInitializer.MAXINUM_MATCHTYPE - WSCInitializer.MINIMUM_MATCHTYPE);
	}

	private double normaliseDistanceValue(double distanceValue) {
		if (WSCInitializer.MAXINUM_SEMANTICDISTANCE - WSCInitializer.MININUM_SEMANTICDISTANCE == 0.0)
			return 1.0;
		else
			return (distanceValue - WSCInitializer.MININUM_SEMANTICDISTANCE)
					/ (WSCInitializer.MAXINUM_SEMANTICDISTANCE - WSCInitializer.MININUM_SEMANTICDISTANCE);
	}

	public double normaliseAvailability(double availability) {
		if (WSCInitializer.MAXIMUM_AVAILABILITY - WSCInitializer.MINIMUM_AVAILABILITY == 0.0)
			return 1.0;
		else
			return (availability - WSCInitializer.MINIMUM_AVAILABILITY)
					/ (WSCInitializer.MAXIMUM_AVAILABILITY - WSCInitializer.MINIMUM_AVAILABILITY);
	}

	public double normaliseReliability(double reliability) {
		if (WSCInitializer.MAXIMUM_RELIABILITY - WSCInitializer.MINIMUM_RELIABILITY == 0.0)
			return 1.0;
		else
			return (reliability - WSCInitializer.MINIMUM_RELIABILITY)
					/ (WSCInitializer.MAXIMUM_RELIABILITY - WSCInitializer.MINIMUM_RELIABILITY);
	}

	public double normaliseTime(double time) {
		if (WSCInitializer.MAXIMUM_TIME - WSCInitializer.MINIMUM_TIME == 0.0)
			return 1.0;
		else
			return (WSCInitializer.MAXIMUM_TIME - time) / (WSCInitializer.MAXIMUM_TIME - WSCInitializer.MINIMUM_TIME);
	}

	public double normaliseCost(double cost) {
		if (WSCInitializer.MAXIMUM_COST - WSCInitializer.MINIMUM_COST == 0.0)
			return 1.0;
		else
			return (WSCInitializer.MAXIMUM_COST - cost) / (WSCInitializer.MAXIMUM_COST - WSCInitializer.MINIMUM_COST);
	}

	public static double getLongestPathVertexList(DirectedGraph<String, ServiceEdge> g,
			Map<String, double[]> serQoSMap) {
		// A algorithm to find all paths
		AllDirectedPaths<String, ServiceEdge> allPath = new AllDirectedPaths<String, ServiceEdge>(g);
		List<GraphPath<String, ServiceEdge>> pathList = allPath.getAllPaths("startNode", "endNode", true, null);
		double maxTime = 0;
		double sumTime;

		for (int i = 0; i < pathList.size(); i++) {

			sumTime = 0;

			// for (String v : Graphs.getPathVertexList(pathList.get(i))) {
			for (String v : (pathList.get(i).getVertexList())) {
				if (!v.equals("startNode") && !v.equals("endNode")) {
					double qos[] = serQoSMap.get(v);
					sumTime += qos[WSCInitializer.TIME];
				}
			}
			if (sumTime > maxTime) {
				maxTime = sumTime;
			}

		}
		// return pathList.get(IndexPathLength).getEdgeList();
		return maxTime;
	}
}
