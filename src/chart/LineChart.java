package chart;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import wsc.problem.WSCInitializer;

public class LineChart {

	public void createLineChart(List<String> entropy4Gen, List<Double> discountRate4Gen) {
		DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
		// for (String value : entropy4Gen) {
		for (int i = 0; i < entropy4Gen.size(); i++) {
			String[] s = entropy4Gen.get(i).split("\\s+");
			line_chart_dataset.addValue(Double.parseDouble(s[0]), "mean entropy", i + "");
			line_chart_dataset.addValue(Double.parseDouble(s[1]), "Max entropy", i + "");
			line_chart_dataset.addValue(Double.parseDouble(s[2]), "min entropy", i + "");
			line_chart_dataset.addValue(discountRate4Gen.get(i), "adaptive discount rate", i + "");
//			line_chart_dataset.addValue((WSCInitializer.bestFitnessSoFar.get(i).fitness) * 2, "fitness*2", i + "");
		}

		JFreeChart lineChartObject = ChartFactory.createLineChart("Changes in Entropy and Discount rate",
				"Generation", "Value", line_chart_dataset, PlotOrientation.VERTICAL, true, true, false);

		int width = 640; /* Width of the image */
		int height = 480; /* Height of the image */
		File lineChart = new File("EntropyChanges.jpeg");
		try {
			ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}