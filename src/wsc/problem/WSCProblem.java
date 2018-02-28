package wsc.problem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.List;

import chart.LineChart;
import nhbsa.LocalSearch;
import nhbsa.Mutation;
//import nhbsa.LocalSearch;
import nhbsa.NHBSA;
import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.graph.ServiceGraph;

public class WSCProblem {
	public static void main(String[] args) throws IOException {

		if (args.length != 5) {
			System.out.println("missing arguments!");
			return;
		}

		WSCInitializer.initialisationStartTime = System.currentTimeMillis();

		WSCInitializer init = new WSCInitializer(args[0], args[1], args[2], args[3], Long.valueOf(args[4]));
		WSCGraph graGenerator = new WSCGraph();
		WSCEvaluation eval = new WSCEvaluation();

		WSCProblem p = new WSCProblem();
		p.NHBSASolver(graGenerator, eval);

	}

	// The main entry to NHBSASolver.
	public void NHBSASolver(WSCGraph graGenerator, WSCEvaluation eval) {

		long initialization = System.currentTimeMillis() - WSCInitializer.initialisationStartTime;

		List<WSCIndividual> population = new ArrayList<WSCIndividual>();
		// entry to NHBSA
		NHBSA nhbsa = new NHBSA(WSCInitializer.population_size, WSCInitializer.dimension_size);

		// random initalize one double size population solutions
		while (population.size() < WSCInitializer.population_size * 2) {
			WSCIndividual individual = new WSCIndividual();
			List<Integer> fullSerQueue = new ArrayList<Integer>();
			List<Integer> usedSerQueue = new ArrayList<Integer>();

			// graph-based representation
			ServiceGraph graph = graGenerator.generateGraph(fullSerQueue);

			// create a queue of services according to breath first search
			List<Integer> usedQueue = graGenerator.usedQueueofLayers("startNode", graph, usedSerQueue);
			// set the position of the split position of the queue
			individual.setSplitPosition(usedQueue.size()); // index from 0 to (splitposition-1)

			// add unused queue to form a complete a vector-based individual
			List<Integer> serQueue = graGenerator.completeSerQueueIndi(usedQueue, fullSerQueue);
			// Set serQueue to individual(do I need deep clone ?)
			individual.serQueue.addAll(serQueue);

			eval.aggregationAttribute(individual, graph);
			eval.calculateFitness(individual);
			population.add(individual);
			// WSCInitializer.evalCounter++;
			// BestIndiSoFar4EvalStep(population);
		}

		// entry to learn the matrix and sampling individuals
		int iteration = 0;
		while (iteration < WSCInitializer.MAX_NUM_ITERATIONS) {
			long startTime = System.currentTimeMillis();
			System.out.println("GENERATION " + iteration);

			// add a local search
			LocalSearch ls = new LocalSearch();
			ls.swapChunk(population, WSCInitializer.random, graGenerator, eval);

			// add a mutation
			// Mutation mutatation = new Mutation();
			// mutatation.swapOne(population, WSCInitializer.random, graGenerator, eval);
			// mutatation.swapTwo(population, WSCInitializer.random, graGenerator, eval);

			// sort the individuals according to the fitness
			Collections.sort(population);

			// update best individual so far
			if (iteration == 0) {
				WSCInitializer.bestFitnessSoFar.add(population.get(0));
			} else {
				if (WSCInitializer.bestFitnessSoFar.get(iteration - 1).fitness < population.get(0).fitness) {
					WSCInitializer.bestFitnessSoFar.add(population.get(0));
				} else {
					WSCInitializer.bestFitnessSoFar.add(WSCInitializer.bestFitnessSoFar.get(iteration - 1));
				}
			}

			// entry to NHBSA

			// NHBSA nhbsa = new NHBSA(WSCInitializer.dimension_size,
			// WSCInitializer.dimension_size);
			// select first half population into matrix
			int[][] m_generation = new int[WSCInitializer.population_size][WSCInitializer.dimension_size];
			for (int m = 0; m < WSCInitializer.population_size; m++) {
				for (int n = 0; n < WSCInitializer.dimension_size; n++) {
					m_generation[m][n] = population.get(m).serQueue.get(n);
				}
			}

			nhbsa.setM_pop(m_generation);
			nhbsa.setM_L(WSCInitializer.dimension_size);
			nhbsa.setM_N(WSCInitializer.population_size);

			List<int[]> pop_updated = nhbsa.sampling4NHBSA(WSCInitializer.population_size * 2, WSCInitializer.random);

			// update the population
			population.clear();

			for (int m = 0; m < pop_updated.size(); m++) {
				int[] id_updated = pop_updated.get(m);
				WSCIndividual indi_updated = new WSCIndividual();
				List<Service> serviceCandidates = new ArrayList<Service>();
				for (int n = 0; n < id_updated.length; n++) {

					// deep clone may be not needed if no changes are applied to the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(id_updated[n]));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> fullSerQueue = new ArrayList<Integer>();
				for (int id : id_updated) {
					fullSerQueue.add(id);
				}

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();
				// adjust the bias according to the valid solution from the service queue

				// create a queue of services according to breathfirstsearch algorithm

				List<Integer> usedQueue = graGenerator.usedQueueofLayers("startNode", update_graph, usedSerQueue);
				// set up the split index for the updated individual
				indi_updated.setSplitPosition(usedQueue.size());

				// add unused queue to form a complete a vector-based individual
				List<Integer> serQueue = graGenerator.completeSerQueueIndi(usedQueue, fullSerQueue);

				// set the serQueue to the updatedIndividual
				indi_updated.serQueue = serQueue;

				// evaluate updated updated_graph
				// eval.aggregationAttribute(indi_updated, updated_graph);
				eval.aggregationAttribute(indi_updated, update_graph);
				eval.calculateFitness(indi_updated);
				population.add(indi_updated);

				// WSCInitializer.evalCounter++;
				// BestIndiSoFar4EvalStep(population);

			}
			WSCInitializer.initTime.add(initialization);
			initialization = (long) 0.0;
			WSCInitializer.time.add(System.currentTimeMillis() - startTime);

			iteration += 1;
		}
		writeLogs();

	}

	public void writeLogs() {
		try {
			FileWriter writer = new FileWriter(new File(WSCInitializer.logName));
			for (int i = 0; i < WSCInitializer.bestFitnessSoFar.size(); i++) {
				writer.append(String.format("%d %d %d %f\n", i, WSCInitializer.initTime.get(i),
						WSCInitializer.time.get(i), WSCInitializer.bestFitnessSoFar.get(i).fitness));
			}
			writer.append(WSCInitializer.bestFitnessSoFar.get(WSCInitializer.bestFitnessSoFar.size() - 1)
					.getStrRepresentation());
			writer.append("\n");

			// print out the entropy for obeservation
			// for (int i = 0; i < NHBSA.discountRate4Gen.size(); i++) {
			// writer.append(String.format("%s\n", NHBSA.discountRate4Gen.get(i)));
			// }
			//
			// LineChart lc = new LineChart();
			// lc.createLineChart(NHBSA.entropy4Gen, NHBSA.discountRate4Gen);

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
