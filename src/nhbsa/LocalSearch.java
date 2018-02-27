package nhbsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.graph.ServiceGraph;
import wsc.problem.WSCEvaluation;
import wsc.problem.WSCGraph;
import wsc.problem.WSCIndividual;
import wsc.problem.WSCInitializer;

public class LocalSearch {

	// stochastic local search
	public List<WSCIndividual> swapChunk(List<WSCIndividual> population, Random random, WSCGraph graGenerator,
			WSCEvaluation eval) {

		// swap

		int split = 0;
		for (WSCIndividual indi : population) {

			double rSLS = random.nextDouble();

			if (rSLS < WSCInitializer.Pls) {

				List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

				for (int nOfls = 0; nOfls < WSCInitializer.numOfLS; nOfls++) {

					// obtain the split position of the individual
					split = indi.getSplitPosition();

					WSCIndividual indi_temp = new WSCIndividual();
					List<Integer> serQueue_temp = new ArrayList<Integer>(); // service Index arrayList
					List<Integer> chunk1_temp = new ArrayList<Integer>(); // service Index chunk1
					List<Integer> chunk2_temp = new ArrayList<Integer>(); // service Index chunk2
					List<Integer> chunk3_temp = new ArrayList<Integer>(); // service Index chunk3
					List<Integer> chunk4_temp = new ArrayList<Integer>(); // service Index chunk4

					// deep clone the services into a service queue for indi_temp
					for (Integer ser : indi.serQueue) {
						serQueue_temp.add(ser);

					}

					if (split == 0) {
						System.out.println(split);
					}

					int swap_a = random.nextInt(split);// between 0 (inclusive) and split (exclusive)

					int swap_b = split + random.nextInt(WSCInitializer.dimension_size - split);// between
																								// split(inclusive)
																								// and
																								// itemsize
					// (exclusive)

					// initial chunks for the individual
					for (int i = 0; i < WSCInitializer.dimension_size; i++) {

						if (i < swap_a) {
							chunk1_temp.add(serQueue_temp.get(i));
						}

						if ((i >= swap_a) && (i < split)) {
							chunk2_temp.add(serQueue_temp.get(i));
						}

						if ((i >= split) && (i < swap_b)) {
							chunk3_temp.add(serQueue_temp.get(i));
						}

						if (i >= swap_b) {
							chunk4_temp.add(serQueue_temp.get(i));
						}

					}

					// a chunk from swap_a to split (exclusive)
					serQueue_temp.clear();
					//swap chunk2 and chunk4
					serQueue_temp.addAll(chunk1_temp);
					serQueue_temp.addAll(chunk4_temp);
					serQueue_temp.addAll(chunk3_temp);
					serQueue_temp.addAll(chunk2_temp);

					indi_temp.serQueue = serQueue_temp;

					List<Service> serviceCandidates = new ArrayList<Service>();
					for (int n = 0; n < indi_temp.serQueue.size(); n++) {

						// deep clone may be not needed if no changes are applied to the pointed service
						serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
					}

					// set the service candidates according to the sampling
					InitialWSCPool.getServiceCandidates().clear();
					InitialWSCPool.setServiceCandidates(serviceCandidates);

					List<Integer> usedSerQueue = new ArrayList<Integer>();

					ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

					// evaluate the update_graph and calculate the fitness

					// adjust the bias according to the valid solution from the service queue
					List<Integer> usedQueue = graGenerator.usedQueueofLayers("startNode", update_graph, usedSerQueue);
					// set up the split index for the updated individual
					indi_temp.setSplitPosition(usedQueue.size());

					// add unused queue to form a complete a vector-based individual
					List<Integer> serQueue = graGenerator.completeSerQueueIndi(usedQueue, indi_temp.serQueue);

					// set the serQueue to the updatedIndividual
					indi_temp.serQueue = serQueue;

					// evaluate updated updated_graph
					eval.aggregationAttribute(indi_temp, update_graph);
					eval.calculateFitness(indi_temp);

					// add
					indi_neigbouring.add(indi_temp);
				}

				Collections.sort(indi_neigbouring);

				// if the best of neighbour solutions is better than the parent

				if (indi_neigbouring.get(0).fitness > indi.fitness) {
					population.set(population.indexOf(indi), indi_neigbouring.get(0));
				}
			}

		}

		return population;
	}

}
