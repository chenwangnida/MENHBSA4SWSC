package nhbsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.graph.ServiceGraph;
import wsc.problem.WSCEvaluation;
import wsc.problem.WSCGraph;
import wsc.problem.WSCIndividual;
import wsc.problem.WSCInitializer;

public class LocalSearch {

	// local search for random one-point swap for Top 1 and 5 from groups by
	// fitness
	// distribution with 20
	// neighbors
	public List<WSCIndividual> randomSwapOne5GroupByFit(List<WSCIndividual> population, Random random,
			WSCGraph graGenerator, WSCEvaluation eval) {
		int split = 0;

		Collections.sort(population);
		List<WSCIndividual> solutions4LS = new ArrayList<WSCIndividual>();

		// obtain individuals for selection
		solutions4LS.add(population.get(0));

		final double fitnessSize = (population.get(0).fitness - population.get(population.size() - 1).fitness) / 5;

		List<WSCIndividual> partition1 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition2 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition3 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition4 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition5 = new ArrayList<WSCIndividual>();

		for (int i = 0; i < population.size(); i++) {
			WSCIndividual indi = population.get(i);
			if (indi.getFitness() >= (population.get(0).fitness - fitnessSize)) {
				partition1.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 2)) {
				partition2.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 3)) {
				partition3.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 4)) {
				partition4.add(population.get(i));
			} else {
				partition5.add(population.get(i));
			}
		}

		// need to fix the bug

		if (partition1.size() != 0) {
			solutions4LS.add(partition1.get(random.nextInt(partition1.size())));
		}

		if (partition2.size() != 0) {
			solutions4LS.add(partition2.get(random.nextInt(partition2.size())));
		}

		if (partition3.size() != 0) {
			solutions4LS.add(partition3.get(random.nextInt(partition3.size())));
		}
		if (partition4.size() != 0) {
			solutions4LS.add(partition4.get(random.nextInt(partition4.size())));
		}
		if (partition5.size() != 0) {
			solutions4LS.add(partition5.get(random.nextInt(partition5.size())));
		}

		List<WSCIndividual> solutions4LSWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(solutions4LS));

		for (WSCIndividual indi : solutions4LSWithoutDuplicates) {

			split = indi.getSplitPosition();
			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS5Group; nOfls++) {

				// obtain the split position of the individual

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList

				// deep clone the services into a service queue for indi_temp
				for (Integer ser : indi.serQueue) {
					serQueue_temp.add(ser);

				}

				indi_temp.serQueue = serQueue_temp;

				int swap_a = random.nextInt(split);// between 0 (inclusive) and
													// split (exclusive)

				int swap_b = split + random.nextInt(WSCInitializer.dimension_size - split);// between
																							// split(inclusive)
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

					// deep clone may be not needed if no changes are applied to
					// the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

				// evaluate the update_graph and calculate the fitness

				// adjust the bias according to the valid solution from the
				// service queue
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

		return population;
	}
 
	public List<WSCIndividual> randomSwapOnefromTop6ByFit(List<WSCIndividual> population, Random random,
			WSCGraph graGenerator, WSCEvaluation eval) {
		int split = 0;

		Collections.sort(population);
		List<WSCIndividual> solutions4LS = new ArrayList<WSCIndividual>();

		// obtain individuals for selection
		solutions4LS.add(population.get(0));
		solutions4LS.add(population.get(1));
		solutions4LS.add(population.get(2));
		solutions4LS.add(population.get(3));
		solutions4LS.add(population.get(4));
		solutions4LS.add(population.get(5));


		List<WSCIndividual> solutions4LSWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(solutions4LS));

		for (WSCIndividual indi : solutions4LSWithoutDuplicates) {

			split = indi.getSplitPosition();
			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS5Group; nOfls++) {

				// obtain the split position of the individual

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList
				Set<Integer> unused_ser = new HashSet<Integer>();

				// deep clone the services into a service queue for indi_temp
				for (int index = 0; index < indi.serQueue.size(); index++) {
					int ser = indi.serQueue.get(index);
					serQueue_temp.add(ser);
					// obtain unused service set
					if (index >= split) {
						unused_ser.add(indi.serQueue.get(index));
					}
				}

				indi_temp.serQueue = serQueue_temp;

				int swap_a = random.nextInt(split);// between 0 (inclusive) and
													// split (exclusive)
				Integer item_a = indi_temp.serQueue.get(swap_a);

				// initial swap_b for index of list, item_b for service index
				int swap_b = -1;
				Integer item_b = -1;
				int continue4Swap = 0;

				// obtain services in the same layer of the selected service

				for (List<Integer> ser_lay : WSCInitializer.layers4SerIndex.values()) {
					if (ser_lay.contains(item_a)) {
						// obtain an intersection of services in the layer and
						// unused list
						List<Integer> swap_b_list = Lists
								.newArrayList(Sets.intersection(Sets.newHashSet(ser_lay), unused_ser));
						if (swap_b_list.size() > 0) {
							continue4Swap = 1;
							item_b = swap_b_list.get(random.nextInt(swap_b_list.size()));
							swap_b = indi_temp.serQueue.indexOf(item_b);
						}
						break;
					}
				}

				if (continue4Swap == 1) {

					Integer item_temp = new Integer(item_a);

					indi_temp.serQueue.set(swap_a, item_b);
					indi_temp.serQueue.set(swap_b, item_temp);

					List<Service> serviceCandidates = new ArrayList<Service>();
					for (int n = 0; n < indi_temp.serQueue.size(); n++) {

						// deep clone may be not needed if no changes are applied to
						// the pointed service
						serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
					}

					// set the service candidates according to the sampling
					InitialWSCPool.getServiceCandidates().clear();
					InitialWSCPool.setServiceCandidates(serviceCandidates);

					List<Integer> usedSerQueue = new ArrayList<Integer>();

					ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

					// evaluate the update_graph and calculate the fitness

					// adjust the bias according to the valid solution from the
					// service queue
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
			}

			Collections.sort(indi_neigbouring);

			// if the best of neighbour solutions is better than the parent

			if (indi_neigbouring.get(0).fitness > indi.fitness) {
				population.set(population.indexOf(indi), indi_neigbouring.get(0));
			}

		}

		return population;
	}
	
	
	// local search for random one-point swap for Top 1 and 5 from groups by
	// fitness
	// distribution with 20
	// neighbors
	public List<WSCIndividual> randomSwapOnefromLayers5GroupByFit(List<WSCIndividual> population, Random random,
			WSCGraph graGenerator, WSCEvaluation eval) {
		int split = 0;

		Collections.sort(population);
		List<WSCIndividual> solutions4LS = new ArrayList<WSCIndividual>();

		// obtain individuals for selection
		solutions4LS.add(population.get(0));

		final double fitnessSize = (population.get(0).fitness - population.get(population.size() - 1).fitness) / 5;

		List<WSCIndividual> partition1 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition2 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition3 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition4 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition5 = new ArrayList<WSCIndividual>();

		for (int i = 0; i < population.size(); i++) {
			WSCIndividual indi = population.get(i);
			if (indi.getFitness() >= (population.get(0).fitness - fitnessSize)) {
				partition1.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 2)) {
				partition2.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 3)) {
				partition3.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 4)) {
				partition4.add(population.get(i));
			} else {
				partition5.add(population.get(i));
			}
		}

		// need to fix the bug

		if (partition1.size() != 0) {
			solutions4LS.add(partition1.get(random.nextInt(partition1.size())));
		}

		if (partition2.size() != 0) {
			solutions4LS.add(partition2.get(random.nextInt(partition2.size())));
		}

		if (partition3.size() != 0) {
			solutions4LS.add(partition3.get(random.nextInt(partition3.size())));
		}
		if (partition4.size() != 0) {
			solutions4LS.add(partition4.get(random.nextInt(partition4.size())));
		}
		if (partition5.size() != 0) {
			solutions4LS.add(partition5.get(random.nextInt(partition5.size())));
		}

		List<WSCIndividual> solutions4LSWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(solutions4LS));

		for (WSCIndividual indi : solutions4LSWithoutDuplicates) {

			split = indi.getSplitPosition();
			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS5Group; nOfls++) {

				// obtain the split position of the individual

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList
				Set<Integer> unused_ser = new HashSet<Integer>();

				// deep clone the services into a service queue for indi_temp
				for (int index = 0; index < indi.serQueue.size(); index++) {
					int ser = indi.serQueue.get(index);
					serQueue_temp.add(ser);
					// obtain unused service set
					if (index >= split) {
						unused_ser.add(indi.serQueue.get(index));
					}
				}

				indi_temp.serQueue = serQueue_temp;

				int swap_a = random.nextInt(split);// between 0 (inclusive) and
													// split (exclusive)
				Integer item_a = indi_temp.serQueue.get(swap_a);

				// initial swap_b for index of list, item_b for service index
				int swap_b = -1;
				Integer item_b = -1;
				int continue4Swap = 0;

				// obtain services in the same layer of the selected service

				for (List<Integer> ser_lay : WSCInitializer.layers4SerIndex.values()) {
					if (ser_lay.contains(item_a)) {
						// obtain an intersection of services in the layer and
						// unused list
						List<Integer> swap_b_list = Lists
								.newArrayList(Sets.intersection(Sets.newHashSet(ser_lay), unused_ser));
						if (swap_b_list.size() > 0) {
							continue4Swap = 1;
							item_b = swap_b_list.get(random.nextInt(swap_b_list.size()));
							swap_b = indi_temp.serQueue.indexOf(item_b);
						}
						break;
					}
				}

				if (continue4Swap == 1) {

					Integer item_temp = new Integer(item_a);

					indi_temp.serQueue.set(swap_a, item_b);
					indi_temp.serQueue.set(swap_b, item_temp);

					List<Service> serviceCandidates = new ArrayList<Service>();
					for (int n = 0; n < indi_temp.serQueue.size(); n++) {

						// deep clone may be not needed if no changes are applied to
						// the pointed service
						serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
					}

					// set the service candidates according to the sampling
					InitialWSCPool.getServiceCandidates().clear();
					InitialWSCPool.setServiceCandidates(serviceCandidates);

					List<Integer> usedSerQueue = new ArrayList<Integer>();

					ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

					// evaluate the update_graph and calculate the fitness

					// adjust the bias according to the valid solution from the
					// service queue
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
			}

			Collections.sort(indi_neigbouring);

			// if the best of neighbour solutions is better than the parent

			if (indi_neigbouring.get(0).fitness > indi.fitness) {
				population.set(population.indexOf(indi), indi_neigbouring.get(0));
			}

		}

		return population;
	}

	// local search for random two-point swap for Top 1 and 5 from groups by
	// fitness
	// distribution with 20
	// neighbors
	public List<WSCIndividual> randomSwapTwo5GroupByFit(List<WSCIndividual> population, Random random,
			WSCGraph graGenerator, WSCEvaluation eval) {
		int split = 0;

		Collections.sort(population);
		List<WSCIndividual> solutions4LS = new ArrayList<WSCIndividual>();

		// obtain individuals for selection
		solutions4LS.add(population.get(0));

		final double fitnessSize = (population.get(0).fitness - population.get(population.size() - 1).fitness) / 5;

		List<WSCIndividual> partition1 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition2 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition3 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition4 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition5 = new ArrayList<WSCIndividual>();

		for (int i = 0; i < population.size(); i++) {
			WSCIndividual indi = population.get(i);
			if (indi.getFitness() >= (population.get(0).fitness - fitnessSize)) {
				partition1.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 2)) {
				partition2.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 3)) {
				partition3.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 4)) {
				partition4.add(population.get(i));
			} else {
				partition5.add(population.get(i));
			}
		}

		// need to fix the bug

		if (partition1.size() != 0) {
			solutions4LS.add(partition1.get(random.nextInt(partition1.size())));
		}

		if (partition2.size() != 0) {
			solutions4LS.add(partition2.get(random.nextInt(partition2.size())));
		}

		if (partition3.size() != 0) {
			solutions4LS.add(partition3.get(random.nextInt(partition3.size())));
		}
		if (partition4.size() != 0) {
			solutions4LS.add(partition4.get(random.nextInt(partition4.size())));
		}
		if (partition5.size() != 0) {
			solutions4LS.add(partition5.get(random.nextInt(partition5.size())));
		}

		List<WSCIndividual> solutions4LSWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(solutions4LS));

		for (WSCIndividual indi : solutions4LSWithoutDuplicates) {

			// obtain the split position of the individual
			split = indi.getSplitPosition();
			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS5Group; nOfls++) {

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList

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
						.toArray();// between
									// split(inclusive)

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

					// deep clone may be not needed if no changes are applied to
					// the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

				// evaluate the update_graph and calculate the fitness

				// adjust the bias according to the valid solution from the
				// service queue
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

		return population;

	}

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
					List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																			// Index
																			// arrayList
					List<Integer> chunk1_temp = new ArrayList<Integer>(); // service
																			// Index
																			// chunk1
					List<Integer> chunk2_temp = new ArrayList<Integer>(); // service
																			// Index
																			// chunk2
					List<Integer> chunk3_temp = new ArrayList<Integer>(); // service
																			// Index
																			// chunk3
					List<Integer> chunk4_temp = new ArrayList<Integer>(); // service
																			// Index
																			// chunk4

					// deep clone the services into a service queue for
					// indi_temp
					for (Integer ser : indi.serQueue) {
						serQueue_temp.add(ser);

					}

					if (split == 0) {
						System.out.println(split);
					}

					int swap_a = random.nextInt(split);// between 0 (inclusive)
														// and split (exclusive)

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
					// swap chunk2 and chunk4
					serQueue_temp.addAll(chunk1_temp);
					serQueue_temp.addAll(chunk4_temp);
					serQueue_temp.addAll(chunk3_temp);
					serQueue_temp.addAll(chunk2_temp);

					indi_temp.serQueue = serQueue_temp;

					List<Service> serviceCandidates = new ArrayList<Service>();
					for (int n = 0; n < indi_temp.serQueue.size(); n++) {

						// deep clone may be not needed if no changes are
						// applied to the pointed service
						serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
					}

					// set the service candidates according to the sampling
					InitialWSCPool.getServiceCandidates().clear();
					InitialWSCPool.setServiceCandidates(serviceCandidates);

					List<Integer> usedSerQueue = new ArrayList<Integer>();

					ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

					// evaluate the update_graph and calculate the fitness

					// adjust the bias according to the valid solution from the
					// service queue
					List<Integer> usedQueue = graGenerator.usedQueueofLayers("startNode", update_graph, usedSerQueue);
					// set up the split index for the updated individual
					indi_temp.setSplitPosition(usedQueue.size());

					// add unused queue to form a complete a vector-based
					// individual
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

	// local search for Top 3
	public List<WSCIndividual> swapChunk4Elite(List<WSCIndividual> population, Random random, WSCGraph graGenerator,
			WSCEvaluation eval) {

		// swap

		int split = 0;

		Collections.sort(population);
		for (int elite = 0; elite < 3; elite++) {
			WSCIndividual indi = population.get(elite);

			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS; nOfls++) {

				// obtain the split position of the individual
				split = indi.getSplitPosition();

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList
				List<Integer> chunk1_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk1
				List<Integer> chunk2_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk2
				List<Integer> chunk3_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk3
				List<Integer> chunk4_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk4

				// deep clone the services into a service queue for indi_temp
				for (Integer ser : indi.serQueue) {
					serQueue_temp.add(ser);

				}

				if (split == 0) {
					System.out.println(split);
				}

				int swap_a = random.nextInt(split);// between 0 (inclusive) and
													// split (exclusive)

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
				// swap chunk2 and chunk4
				serQueue_temp.addAll(chunk1_temp);
				serQueue_temp.addAll(chunk4_temp);
				serQueue_temp.addAll(chunk3_temp);
				serQueue_temp.addAll(chunk2_temp);

				indi_temp.serQueue = serQueue_temp;

				List<Service> serviceCandidates = new ArrayList<Service>();
				for (int n = 0; n < indi_temp.serQueue.size(); n++) {

					// deep clone may be not needed if no changes are applied to
					// the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

				// evaluate the update_graph and calculate the fitness

				// adjust the bias according to the valid solution from the
				// service queue
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

		return population;
	}

	// local search for Top 1 and 4 from groups by fitness distribution with 18
	// neighbors
	public List<WSCIndividual> swapChunk4GroupByFit(List<WSCIndividual> population, Random random,
			WSCGraph graGenerator, WSCEvaluation eval) {

		// swap

		int split = 0;

		Collections.sort(population);
		List<WSCIndividual> solutions4LS = new ArrayList<WSCIndividual>();

		// obtain individuals for selection
		solutions4LS.add(population.get(0));

		final double fitnessSize = (population.get(0).fitness - population.get(population.size() - 1).fitness) / 4;

		List<WSCIndividual> partition1 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition2 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition3 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition4 = new ArrayList<WSCIndividual>();

		for (int i = 0; i < population.size(); i++) {
			WSCIndividual indi = population.get(i);
			if (indi.getFitness() >= (population.get(0).fitness - fitnessSize)) {
				partition1.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 2)) {
				partition2.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 3)) {
				partition3.add(population.get(i));
			} else {
				partition4.add(population.get(i));
			}
		}

		// need to fix the bug

		if (partition1.size() != 0) {
			solutions4LS.add(partition1.get(random.nextInt(partition1.size())));
		}

		if (partition2.size() != 0) {
			solutions4LS.add(partition2.get(random.nextInt(partition2.size())));
		}

		if (partition3.size() != 0) {
			solutions4LS.add(partition3.get(random.nextInt(partition3.size())));
		}
		if (partition4.size() != 0) {
			solutions4LS.add(partition4.get(random.nextInt(partition4.size())));
		}

		List<WSCIndividual> solutions4LSWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(solutions4LS));

		for (WSCIndividual indi : solutions4LSWithoutDuplicates) {

			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS4Group; nOfls++) {

				// obtain the split position of the individual
				split = indi.getSplitPosition();

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList
				List<Integer> chunk1_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk1
				List<Integer> chunk2_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk2
				List<Integer> chunk3_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk3
				List<Integer> chunk4_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk4

				// deep clone the services into a service queue for indi_temp
				for (Integer ser : indi.serQueue) {
					serQueue_temp.add(ser);

				}

				if (split == 0) {
					System.out.println(split);
				}

				int swap_a = random.nextInt(split);// between 0 (inclusive) and
													// split (exclusive)

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
				// swap chunk2 and chunk4
				serQueue_temp.addAll(chunk1_temp);
				serQueue_temp.addAll(chunk4_temp);
				serQueue_temp.addAll(chunk3_temp);
				serQueue_temp.addAll(chunk2_temp);

				indi_temp.serQueue = serQueue_temp;

				List<Service> serviceCandidates = new ArrayList<Service>();
				for (int n = 0; n < indi_temp.serQueue.size(); n++) {

					// deep clone may be not needed if no changes are applied to
					// the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

				// evaluate the update_graph and calculate the fitness

				// adjust the bias according to the valid solution from the
				// service queue
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

		return population;
	}

	// local search for Top 1 and 5 from groups by fitness distribution with 18
	// neighbors
	public List<WSCIndividual> swapChunk5GroupByFit(List<WSCIndividual> population, Random random,
			WSCGraph graGenerator, WSCEvaluation eval) {

		// swap

		int split = 0;

		Collections.sort(population);
		List<WSCIndividual> solutions4LS = new ArrayList<WSCIndividual>();

		// obtain individuals for selection
		solutions4LS.add(population.get(0));

		final double fitnessSize = (population.get(0).fitness - population.get(population.size() - 1).fitness) / 5;

		List<WSCIndividual> partition1 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition2 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition3 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition4 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition5 = new ArrayList<WSCIndividual>();

		for (int i = 0; i < population.size(); i++) {
			WSCIndividual indi = population.get(i);
			if (indi.getFitness() >= (population.get(0).fitness - fitnessSize)) {
				partition1.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 2)) {
				partition2.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 3)) {
				partition3.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 4)) {
				partition4.add(population.get(i));
			} else {
				partition5.add(population.get(i));

			}
		}

		// need to fix the bug

		if (partition1.size() != 0) {
			solutions4LS.add(partition1.get(random.nextInt(partition1.size())));
		}

		if (partition2.size() != 0) {
			solutions4LS.add(partition2.get(random.nextInt(partition2.size())));
		}

		if (partition3.size() != 0) {
			solutions4LS.add(partition3.get(random.nextInt(partition3.size())));
		}
		if (partition4.size() != 0) {
			solutions4LS.add(partition4.get(random.nextInt(partition4.size())));
		}
		if (partition5.size() != 0) {
			solutions4LS.add(partition5.get(random.nextInt(partition5.size())));
		}

		List<WSCIndividual> solutions4LSWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(solutions4LS));

		for (WSCIndividual indi : solutions4LSWithoutDuplicates) {

			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS5Group; nOfls++) {

				// obtain the split position of the individual
				split = indi.getSplitPosition();

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList
				List<Integer> chunk1_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk1
				List<Integer> chunk2_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk2
				List<Integer> chunk3_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk3
				List<Integer> chunk4_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk4

				// deep clone the services into a service queue for indi_temp
				for (Integer ser : indi.serQueue) {
					serQueue_temp.add(ser);

				}

				if (split == 0) {
					System.out.println(split);
				}

				int swap_a = random.nextInt(split);// between 0 (inclusive) and
													// split (exclusive)

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
				// swap chunk2 and chunk4
				serQueue_temp.addAll(chunk1_temp);
				serQueue_temp.addAll(chunk4_temp);
				serQueue_temp.addAll(chunk3_temp);
				serQueue_temp.addAll(chunk2_temp);

				indi_temp.serQueue = serQueue_temp;

				List<Service> serviceCandidates = new ArrayList<Service>();
				for (int n = 0; n < indi_temp.serQueue.size(); n++) {

					// deep clone may be not needed if no changes are applied to
					// the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

				// evaluate the update_graph and calculate the fitness

				// adjust the bias according to the valid solution from the
				// service queue
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

		return population;
	}

	// local search for Top 1 and 5 from groups by fitness distribution with 20
	// neighbors
	public List<WSCIndividual> adaptiveSwapChunk4GroupByFit(List<WSCIndividual> population, Random random,
			WSCGraph graGenerator, WSCEvaluation eval) {

		// swap

		int split = 0;

		Collections.sort(population);
		List<WSCIndividual> solutions4LS = new ArrayList<WSCIndividual>();

		// obtain individuals for selection
		solutions4LS.add(population.get(0));

		final double fitnessSize = (population.get(0).fitness - population.get(population.size() - 1).fitness) / 5;

		List<WSCIndividual> partition1 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition2 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition3 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition4 = new ArrayList<WSCIndividual>();
		List<WSCIndividual> partition5 = new ArrayList<WSCIndividual>();

		for (int i = 0; i < population.size(); i++) {
			WSCIndividual indi = population.get(i);
			if (indi.getFitness() >= (population.get(0).fitness - fitnessSize)) {
				partition1.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 2)) {
				partition2.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 3)) {
				partition3.add(population.get(i));
			} else if (indi.getFitness() >= (population.get(0).fitness - fitnessSize * 4)) {
				partition4.add(population.get(i));
			} else {
				partition5.add(population.get(i));
			}
		}

		// need to fix the bug

		if (partition1.size() != 0) {
			solutions4LS.add(partition1.get(random.nextInt(partition1.size())));
		}

		if (partition2.size() != 0) {
			solutions4LS.add(partition2.get(random.nextInt(partition2.size())));
		}

		if (partition3.size() != 0) {
			solutions4LS.add(partition3.get(random.nextInt(partition3.size())));
		}
		if (partition4.size() != 0) {
			solutions4LS.add(partition4.get(random.nextInt(partition4.size())));
		}

		if (partition5.size() != 0) {
			solutions4LS.add(partition5.get(random.nextInt(partition5.size())));
		}

		List<WSCIndividual> solutions4LSWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(solutions4LS));

		for (WSCIndividual indi : solutions4LSWithoutDuplicates) {

			double adaptiveLength = (WSCInitializer.MAX_NUM_ITERATIONS - 1 - WSCInitializer.NHMCounter)
					* (indi.getSplitPosition() - 1 - 1) / (WSCInitializer.MAX_NUM_ITERATIONS - 1) + 1;

			double lastPos = -1;

			List<WSCIndividual> indi_neigbouring = new ArrayList<WSCIndividual>();

			for (int nOfls = 0; nOfls < WSCInitializer.numOfLS4Group; nOfls++) {

				// obtain the split position of the individual
				split = indi.getSplitPosition();

				WSCIndividual indi_temp = new WSCIndividual();
				List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																		// Index
																		// arrayList
				List<Integer> chunk1_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk1
				List<Integer> chunk2_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk2
				List<Integer> chunk3_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk3
				List<Integer> chunk4_temp = new ArrayList<Integer>(); // service
																		// Index
																		// chunk4

				// deep clone the services into a service queue for indi_temp
				for (Integer ser : indi.serQueue) {
					serQueue_temp.add(ser);

				}

				// obtain last start positions for valid length for LS, position
				// 0 is always
				// included
				if (split >= adaptiveLength) {
					lastPos = split - adaptiveLength;
				}

				// obtain index 0 to lastPos for the startpos of a range

				// random obtain the startpos from the range and generate parts
				// for LS

				if (split == 0) {
					System.out.println(split);
				}

				int swap_a = random.nextInt(split);// between 0 (inclusive) and
													// split (exclusive)

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
				// swap chunk2 and chunk4
				serQueue_temp.addAll(chunk1_temp);
				serQueue_temp.addAll(chunk4_temp);
				serQueue_temp.addAll(chunk3_temp);
				serQueue_temp.addAll(chunk2_temp);

				indi_temp.serQueue = serQueue_temp;

				List<Service> serviceCandidates = new ArrayList<Service>();
				for (int n = 0; n < indi_temp.serQueue.size(); n++) {

					// deep clone may be not needed if no changes are applied to
					// the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = graGenerator.generateGraphBySerQueue();

				// evaluate the update_graph and calculate the fitness

				// adjust the bias according to the valid solution from the
				// service queue
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

			// if the best of neighbor solutions is better than the parent

			if (indi_neigbouring.get(0).fitness > indi.fitness) {
				population.set(population.indexOf(indi), indi_neigbouring.get(0));
			}

		}

		return population;
	}

}
