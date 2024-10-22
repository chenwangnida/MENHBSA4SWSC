package wsc.problem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nhbsa.LocalSearch;
import nhbsa.NHBSA;
import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.graph.ServiceGraph;

public class WSCProblem {
	public static void main(String[] args) throws IOException {

		if (args.length != 6) {
			System.out.println("missing arguments!");
			return;
		}

		WSCInitializer.initialisationStartTime = System.currentTimeMillis();

		WSCInitializer init = new WSCInitializer(args[0], args[1], args[2], args[3], Long.valueOf(args[4]), Integer.valueOf(args[5]));
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
			// fullSerQueue is initially empty, but updated with the services used in the
			// generated graph
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
			
			
			//Configure local search methods
			if(WSCInitializer.ls_type == 0) {
				//constrained layer-based one-point swap
				ls.randomSwapOnefromLayers5GroupByFit(population, WSCInitializer.random, graGenerator, eval);
			}
			
			if(WSCInitializer.ls_type == 1) {
				//constrained one-point swap
				ls.randomSwapOne5GroupByFit(population, WSCInitializer.random, graGenerator, eval);
			}

			if(WSCInitializer.ls_type == 2) {
				//constrained two-point swap
				ls.randomSwapTwo5GroupByFit(population, WSCInitializer.random, graGenerator, eval);
			}
			
			if(WSCInitializer.ls_type == 3) {
				//constrained one block swap
				ls.swapChunk5GroupByFit(population, WSCInitializer.random, graGenerator, eval);
			}
			
			if(WSCInitializer.ls_type == 4) {
				//constrained layer-based one-point swap on top 6 
				ls.randomSwapOnefromTop6ByFit(population, WSCInitializer.random, graGenerator, eval);
			}
			

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

			// select first half population into matrix and archive
			List<WSCIndividual> archive = new ArrayList<WSCIndividual>();

			int[][] m_generation = new int[WSCInitializer.population_size][WSCInitializer.dimension_size];
			for (int m = 0; m < WSCInitializer.population_size; m++) {
				archive.add(population.get(m));
				for (int n = 0; n < WSCInitializer.dimension_size; n++) {
					m_generation[m][n] = population.get(m).serQueue.get(n);
				}
			}

			nhbsa.setM_pop(m_generation);
			nhbsa.setM_L(WSCInitializer.dimension_size);
			nhbsa.setM_N(WSCInitializer.population_size);

			// sample only half number of pop
			List<int[]> pop_updated = nhbsa.sampling4NHBSA(WSCInitializer.population_size, WSCInitializer.random);

			population.clear();

			// add another half number of pop to population
			population.addAll(archive);
			//System.out.print("archv:"+archive.size());


			// update the population with pop_updated
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
			
			//System.out.print("POP:"+population.size());

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
