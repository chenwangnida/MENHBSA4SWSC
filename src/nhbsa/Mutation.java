package nhbsa;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.graph.ServiceGraph;
import wsc.problem.WSCEvaluation;
import wsc.problem.WSCGraph;
import wsc.problem.WSCIndividual;
import wsc.problem.WSCInitializer;

public class Mutation {

	// stochastic local search
	public List<WSCIndividual> swapOne(List<WSCIndividual> population, Random random, WSCGraph graGenerator,
			WSCEvaluation eval) {

		// swap

		int split = 0;
		for (WSCIndividual indi : population) {

			double rSLS = random.nextDouble();

			if (rSLS < WSCInitializer.Pm) {

				// obtain the split position of the individual
				split = indi.getSplitPosition();

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service Index arrayList

				// deep clone the services into a service queue for indi_temp
				for (Integer ser : indi.serQueue) {
					serQueue_temp.add(ser);

				}

				indi_temp.serQueue = serQueue_temp;

				if (split == 0) {
					System.out.println(split);
				}

				int swap_a = random.nextInt(split);// between 0 (inclusive) and split (exclusive)

				int swap_b = split + random.nextInt(WSCInitializer.dimension_size - split);// between split(inclusive)
																							// and
																							// itemsize
				// (exclusive)

				Integer item_a = indi_temp.serQueue.get(swap_a);
				Integer item_b = indi_temp.serQueue.get(swap_b);
				Integer item_temp = new Integer(item_a);

				indi_temp.serQueue.set(swap_a, item_b);
				indi_temp.serQueue.set(swap_b, item_temp);

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

				// adjust the bias according to the valid solution from the service queue
				List<Integer> usedQueue = graGenerator.usedQueueofLayers("startNode", update_graph, usedSerQueue);
				// set up the split index for the updated individual
				indi_temp.setSplitPosition(usedQueue.size());

				// add unused queue to form a complete a vector-based individual
				List<Integer> serQueue = graGenerator.completeSerQueueIndi(usedQueue, indi_temp.serQueue);

				// set the serQueue to the updatedIndividual
				indi_temp.serQueue = serQueue;

				// replace it current individual
				population.set(population.indexOf(indi), indi_temp);
			}

		}

		return population;
	}

	public List<WSCIndividual> swapTwo(List<WSCIndividual> population, Random random, WSCGraph graGenerator,
			WSCEvaluation eval) {

		// swap

		int split = 0;
		for (WSCIndividual indi : population) {

			double rSLS = random.nextDouble();

			if (rSLS < WSCInitializer.Pm) {

				// obtain the split position of the individual
				split = indi.getSplitPosition();

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service Index arrayList

				// deep clone the services into a service queue for indi_temp
				for (Integer ser : indi.serQueue) {
					serQueue_temp.add(ser);

				}

				indi_temp.serQueue = serQueue_temp;

				if (split == 0) {
					System.out.println(split);
				}

				int[] swap_a_arr = random.ints(0, split).distinct().limit(2).toArray();

				int[] swap_b_arr = random.ints(split, WSCInitializer.dimension_size - split).distinct().limit(2)
						.toArray();// between split(inclusive)

				for (int i = 0; i < swap_a_arr.length; i++) {
					int swap_a = swap_a_arr[i];
					int swap_b = swap_b_arr[i];
					
					Integer item_a = indi_temp.serQueue.get(swap_a);
					Integer item_b = indi_temp.serQueue.get(swap_b);
					Integer item_temp = new Integer(item_a);

					indi_temp.serQueue.set(swap_a, item_b);
					indi_temp.serQueue.set(swap_b, item_temp);

				}


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

				// adjust the bias according to the valid solution from the service queue
				List<Integer> usedQueue = graGenerator.usedQueueofLayers("startNode", update_graph, usedSerQueue);
				// set up the split index for the updated individual
				indi_temp.setSplitPosition(usedQueue.size());

				// add unused queue to form a complete a vector-based individual
				List<Integer> serQueue = graGenerator.completeSerQueueIndi(usedQueue, indi_temp.serQueue);

				// set the serQueue to the updatedIndividual
				indi_temp.serQueue = serQueue;

				// replace it current individual
				population.set(population.indexOf(indi), indi_temp);
			}

		}

		return population;
	}

}
