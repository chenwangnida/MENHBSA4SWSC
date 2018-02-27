package nhbsa;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import com.google.common.math.DoubleMath;

import wsc.problem.WSCInitializer;

public class NHBSA {
	private int m_N; // population size
	private int m_L; // length of permutation
	int[][] m_pop = new int[m_N][m_L];// a population matrix
	double[][] m_node; // a node histogram matrix (NHM)
	private double m_bRatio;// a bias for NHM
	double Pls = 0.1; // probability of local search

	// settings for discount learning
	boolean isDiscount = false; // true for considering the learning rate, false for no
	boolean isFirstNHM = true; // true for the first NHM without any discount
	int method = 2; // 1 = constant alpha, 2 = E-EDA, 3= E-EDA dynamic minEntropy, 4 = unfold
					// function of
					// 2, 5 = moving average, 6 = L-EDA for a range
	double lrate = 0.5; // default = 0.5
	boolean isAdaptive = false;// false for default, no adaptive changes according to the entropy of the matrix
	double k = 1.0;
	double[][] m_node_archive;

	// a array for storing entropy
	private static double maxEntropy = 0;
	private static double minEntropy = 0;

	private double[] entropyTemp;
	public static List<Double> entropy4Gen;
	public static List<Double> discountRate4Gen;

	// range
	private static double lowerbound = 0.2;
	private static double upperbound = 0.9;

	public NHBSA(int m_N, int m_L) {
		m_node = new double[m_L][m_L]; // initial a node histogram matrix (NHM)
		m_node_archive = new double[m_L][m_L]; // initial archive for storing a node histogram matrix (NHM)
		entropyTemp = new double[m_L]; // initial an entropy array for storing entropy for a matrix
		entropy4Gen = new ArrayList<Double>(); // initial an entropy array for storing entropy for all matrix through
												// all
		// generations
		discountRate4Gen = new ArrayList<Double>(); // initial an discountRate arraylist for storing the changing
													// disount rate
	}

	public final void setDefaultPara() {
		m_bRatio = 0.0002;// defined by users
		m_bRatio = (m_N * m_bRatio) / m_L; // bias
	}

	public List<int[]> sampling4NHBSA(int sampleSize, Random random) {
		List<int[]> sampled_pop = new ArrayList<int[]>();

		setDefaultPara(); // set bias

		// add bias to all elements of NHM
		for (int i = 0; i < m_L; i++) {
			for (int j = 0; j < m_L; j++) {
				m_node[i][j] = m_bRatio;
			}
		}

		// generate nodes
		int[] nodes = new int[m_L];
		for (int m = 0; m < m_L; m++) {
			nodes[m] = m;
		}

		for (int j = 0; j < m_L; j++) { // for dimension

			for (int node : nodes) {
				double delta_sum = 0.00;
				for (int i = 0; i < m_N; i++) {
					if (m_pop[i][j] == node) {
						delta_sum += 1;
					} else {
						delta_sum += 0;
					}
				}
				m_node[j][node] += delta_sum;
			}
		}

		// check the discount rate is considered in the learning process
		if (isDiscount == true) {

			if (isFirstNHM == true) {
				isFirstNHM = false;
				copy2dArray(m_node_archive, m_node);
				discountRate4Gen.add(lrate);
				calculateEntropy(m_node);

			} else {
				// update m_node using m_node and m_node_archive
				double[][] m_node_updated = new double[m_L][m_L];

				switch (method) {
				case 1: // constant, such as 0.5
					m_node_updated = discountedNHM(m_node_archive, m_node, lrate, m_node_updated);
					break;
				case 2: // linear for a given bound
					calculateEntropy(m_node);
					m_node_updated = adaptive_discountedNHM(m_node_archive, m_node, 1, m_node_updated);
					break;
				case 3:// liearn for a given bound with histogrical minEntropy
					calculateEntropy(m_node);
					m_node_updated = adaptive_discountedNHM(m_node_archive, m_node, 2, m_node_updated);
					break;
				case 4:
					calculateEntropy(m_node);
					m_node_updated = adaptive_discountedNHM(m_node_archive, m_node, 3, m_node_updated);
					break;
				case 5: // moving average
					m_node_updated = adaptive_discountedNHM4MovingAverage(m_node_archive, m_node, m_node_updated);
					break;
				case 6: // moving average for a given bound
					m_node_updated = adaptive_discountedNHM4MovingAverage4Range(m_node_archive, m_node, m_node_updated,
							lowerbound, upperbound);
					break;
				}

				// empty m_node_archive first, is it necessary？

				// store the data of new NHM
				copy2dArray(m_node_archive, m_node);

				// update m_node using m_node_updated
				copy2dArray(m_node, m_node_updated);
			}

		}

		// print NHX Distri
		// printDistribution(m_node, WSCInitializer.NHMCounter);

		WSCInitializer.NHMCounter++;

		// NHBSA/WO Sampling sampleSize numbers of individuals
		for (int no_sample = 0; no_sample < sampleSize; no_sample++) {

			int[] sampledIndi = new int[m_L]; // an individual sampled

			// generate a random position index permutation
			List<Integer> position_permutation = new ArrayList<Integer>();

			for (int m = 0; m < m_L; m++) {
				position_permutation.add(m);
			}

			Collections.shuffle(position_permutation, random);

			// generate a candidate node list
			int[] c_candidates = new int[m_L];
			for (int m = 0; m < m_L; m++) {
				c_candidates[m] = m;
			}

			// set the position counter
			int p_counter = 0;
			while (p_counter < m_L) { // postion_counter for the random
										// permutation

				int position_dimension = position_permutation.get(p_counter);
				// initial numsToGenerate from the candidate node list
				double[] discreteProbabilities = new double[m_L - p_counter];

				// calculate probability and put them into proba[]
				double sum_proba = 0;
				for (int c : c_candidates) {
					sum_proba += m_node[position_dimension][c];
				}
				int m = 0;

				for (int c : c_candidates) {
					discreteProbabilities[m] = m_node[position_dimension][c] / sum_proba;
					m++;
				}

				// sample x of individual for c[r[p]]
				int[] node_x = sampling(c_candidates, discreteProbabilities, random);

				sampledIndi[position_dimension] = node_x[0];

				// remove x from numsToGenerate
				c_candidates = ArrayUtils.removeElement(c_candidates, node_x[0]);

				// update the position counter P+1
				p_counter += 1;
			}

			// printIndi(sampledIndi);
			sampled_pop.add(sampledIndi);
		}
		return sampled_pop;
	}

	private void calculateEntropy(double[][] m_node) {

		int p_counter = 0;
		ArrayUtils.nullToEmpty(entropyTemp);

		while (p_counter < m_L) { // postion_counter for the random
									// permutation
			// initial numsToGenerate from the candidate node list
			double[] discreteProbabilities = new double[m_L];

			// calculate probability and put them into proba[]
			double sum_proba = 0;

			for (int m = 0; m < m_L; m++) {
				sum_proba += m_node[p_counter][m];
			}
			for (int m = 0; m < m_L; m++) {
				discreteProbabilities[m] = m_node[p_counter][m] / sum_proba;
			}

			entropyTemp[p_counter] = entropy(discreteProbabilities);

			p_counter++;
		}

		if (maxEntropy < Arrays.stream(entropyTemp).average().getAsDouble())
			maxEntropy = Arrays.stream(entropyTemp).average().getAsDouble();

		if (minEntropy == 0) {
			minEntropy = Arrays.stream(entropyTemp).min().getAsDouble();

		} else if (minEntropy > Arrays.stream(entropyTemp).min().getAsDouble()) {
			minEntropy = Arrays.stream(entropyTemp).min().getAsDouble();
		}

		entropy4Gen.add(Arrays.stream(entropyTemp).average().getAsDouble());
		System.out.println("MAX_MEAN_ENTROPY" + maxEntropy + ";Min_MEAN_ENTROPY" + minEntropy);

	}

	private double entropy(double[] discreteProbabilities) {
		double entropy_indi = 0;

		for (double p : discreteProbabilities) {
			entropy_indi += -(p * DoubleMath.log2(p));
		}

		return entropy_indi;
	}

	private void copy2dArray(double[][] targetArray, double[][] sourceArray) {
		for (int m = 0; m < sourceArray.length; m++)
			targetArray[m] = sourceArray[m].clone();
	}

	// adaptive method according to the entorpy value
	private double[][] adaptive_discountedNHM(double[][] m_node_archive, double[][] m_node, int functions,
			double[][] m_node_updated) {
		double s = entropy4Gen.get(entropy4Gen.size() - 1);
		double lrate_update = updateLRate(s, functions);
		System.out.println("updated alpha:" + (lrate_update));

		for (int indi_pos = 0; indi_pos < m_L; indi_pos++) {
			for (int index = 0; index < m_L; index++) {
				m_node_updated[indi_pos][index] = m_node_archive[indi_pos][index] * (1 - lrate_update)
						+ m_node[indi_pos][index] * lrate_update;
			}
		}

		// printNHM(m_node_updated);

		discountRate4Gen.add(lrate_update);
		return m_node_updated;
	}
	// adaptive method using moving average

	private double[][] adaptive_discountedNHM4MovingAverage(double[][] m_node_archive, double[][] m_node,
			double[][] m_node_updated) {

		// long updateRate = WSCInitializer.NHMCounter /
		// (long)WSCInitializer.NHMIteration;

		for (int indi_pos = 0; indi_pos < m_L; indi_pos++) {
			for (int index = 0; index < m_L; index++) {
				m_node_updated[indi_pos][index] = m_node_archive[indi_pos][index]
						* (WSCInitializer.MAX_NUM_ITERATIONS - WSCInitializer.NHMCounter)
						/ WSCInitializer.MAX_NUM_ITERATIONS
						+ m_node[indi_pos][index] * WSCInitializer.NHMCounter / WSCInitializer.MAX_NUM_ITERATIONS;
			}
		}

		return m_node_updated;
	}

	private double[][] adaptive_discountedNHM4MovingAverage4Range(double[][] m_node_archive, double[][] m_node,
			double[][] m_node_updated, double lowerbound, double upperbound) {

		// long updateRate = WSCInitializer.NHMCounter /
		// (long)WSCInitializer.NHMIteration;

		double alpha = (WSCInitializer.MAX_NUM_ITERATIONS - 1 - WSCInitializer.NHMCounter) * (upperbound - lowerbound)
				/ (WSCInitializer.MAX_NUM_ITERATIONS - 1) + lowerbound;

		System.out.println(WSCInitializer.NHMCounter + ";" + (alpha));
		discountRate4Gen.add(alpha);

		for (int indi_pos = 0; indi_pos < m_L; indi_pos++) {
			for (int index = 0; index < m_L; index++) {
				m_node_updated[indi_pos][index] = m_node_archive[indi_pos][index] * (1 - alpha)
						+ m_node[indi_pos][index] * alpha;
			}
		}

		return m_node_updated;
	}

	private double[][] discountedNHM(double[][] m_node_archive, double[][] m_node, double lrate,
			double[][] m_node_updated) {

		for (int indi_pos = 0; indi_pos < m_L; indi_pos++) {
			for (int index = 0; index < m_L; index++) {
				m_node_updated[indi_pos][index] = m_node_archive[indi_pos][index] * lrate
						+ m_node[indi_pos][index] * (1 - lrate);
			}
		}

		// printNHM(m_node_updated);
		return m_node_updated;
	}

	// adaptively calculate the discount rate according to the value of entropy
	private double updateLRate(double meanEntropy, int function) {
		double updatedRate = 0;

		switch (function) {
		case 1: // linear
			// updatedRate = 1.0 - meanEntropy * 1 / (maxEntropy) + 0.1;
			updatedRate = meanEntropy * (upperbound - lowerbound) / (maxEntropy) + lowerbound;

			break;
		case 2: // linear with considering the historical minimum
			updatedRate = (meanEntropy - minEntropy) * (upperbound - lowerbound) / (maxEntropy - minEntropy)
					+ lowerbound;

			// updatedRate = (1 - meanEntropy * 1 / maxEntropy) * (1 - meanEntropy * 1 /
			// maxEntropy);
			break;
		case 3:
			updatedRate = (1 - meanEntropy * 1 / maxEntropy) * (1 + meanEntropy * 1 / maxEntropy);
			break;
		}
		return updatedRate;
	}

	// sampling function for sampling one individual requiring
	// re-normalization of the remaining after each sampling
	public int[] sampling(int[] numsToGenerate, double[] discreteProbabilities, Random random) {

		// sample node x with probability
		EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(numsToGenerate,
				discreteProbabilities);

		distribution.reseedRandomGenerator(random.nextInt());

		return distribution.sample(1);
	}

	private double delta_sum_calcu(int m_N, int j, double[][] m_node) {
		double delta_sum = 0;
		for (int k = 0; k < m_N; k++) {
			for (int dimension = 0; dimension < m_L; dimension++) {

				if (m_pop[k][dimension] == j) {
					delta_sum += 1;
				} else {
					delta_sum += 0;
				}
			}
		}
		return delta_sum;
	}

	public int getM_N() {
		return m_N;
	}

	public void setM_N(int m_N) {
		this.m_N = m_N;
	}

	public int getM_L() {
		return m_L;
	}

	public void setM_L(int m_L) {
		this.m_L = m_L;
	}

	public List<Double> getDiscountRate4Gen() {
		return discountRate4Gen;
	}

	public void setDiscountRate4Gen(List<Double> discountRate4Gen) {
		this.discountRate4Gen = discountRate4Gen;
	}

	public void printNHM(int[][] m_node) {
		System.out.println("");
		for (int i = 0; i < m_N; i++) {
			System.out.print("[");
			for (int j = 0; j < m_L; j++) {
				System.out.print(m_node[i][j] + " ");
			}
			System.out.println("]");
		}

	}

	public void printDistribution(double[][] m_node, int NHMCounter) {

		if (NHMCounter < 4) {

			String[][] discreteProbabilities = new String[m_L][m_L];

			for (int position_dimension = 0; position_dimension < m_L; position_dimension++) {
				// initial numsToGenerate from the candidate node list
				// calculate probability and put them into proba[]
				double sum_proba = 0;
				for (int candidate = 0; candidate < m_L; candidate++) {
					sum_proba += m_node[position_dimension][candidate];
				}
				int m = 0;

				for (int candidate = 0; candidate < m_L; candidate++) {
					discreteProbabilities[position_dimension][m] = new BigDecimal(
							m_node[position_dimension][candidate] / sum_proba).setScale(6, BigDecimal.ROUND_HALF_UP)
									.toString();
					m++;
				}

			}

			writeLogs(discreteProbabilities, NHMCounter);
		}
	}

	public void printNHM(double[][] m_node) {
		System.out.println("");
		for (int i = 0; i < m_L; i++) {
			System.out.print("[");
			for (int j = 0; j < m_L; j++) {
				System.out.print(m_node[i][j] + " ");
			}
			System.out.println("]");
		}

	}

	public void printIndi(int[] indi) {
		System.out.println("");
		System.out.print("[");
		for (int i = 0; i < m_N; i++) {
			System.out.print(indi[i] + " ");
		}
		System.out.println("]");
	}

	public int[][] getM_pop() {
		return m_pop;
	}

	public void setM_pop(int[][] m_pop) {
		this.m_pop = m_pop;
	}

	public void writeLogs(String[][] discreteProbabilities, int NHMCounter) {
		try {
			FileWriter writer = new FileWriter(new File("matrix" + NHMCounter));

			for (int i = 0; i < m_L; i++) {
				writer.append("[");
				for (int j = 0; j < m_L; j++) {
					writer.append(discreteProbabilities[i][j] + " ");
				}

				writer.append("]");
				writer.append("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}