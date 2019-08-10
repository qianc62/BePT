package org.processmining.analysis.redesign;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import java.util.*;
import java.io.*;

/**
 * The statistics calculation for the simulation results.
 * 
 * @author Mariska Netjes
 */

public class Statistics {

	protected File nodeFolder;
	private String fileName;
	private List<String> files = new ArrayList<String>();
	private Map meansFiles = new HashMap();
	private Map statsFiles = new HashMap();

	/**
	 * The statistics are calculated based on the values in some folder In this
	 * case, the values are values for the throughput time.
	 * 
	 * @param folder
	 *            File the folder location that includes the sim folders A sim
	 *            folder contains the values for one sub run.
	 */
	public Statistics(File folder) {
		this.nodeFolder = folder;
		for (File simFolder : folder.listFiles()) {
			if (simFolder.getAbsolutePath().contains("sim")) {
				File tptFile = simFolder.listFiles()[0];
				String tptLoc = tptFile.getPath();
				String tpt = "tpt";
				files.add(tptLoc);
				meansFiles.put(tpt, new ArrayList());
				statsFiles.put(tpt, new ArrayList());
			}
		}
	}

	/**
	 * The statistics are calculated based on the values in some file In this
	 * case, the values are values for the throughput time.
	 * 
	 * @param name
	 *            String the folder location
	 */
	public Statistics(String name) {
		this.nodeFolder = new File(name);
		this.fileName = name;
		String tpt = "tpt";
		meansFiles.put(tpt, new ArrayList());
		statsFiles.put(tpt, new ArrayList());
	}

	/**
	 * The actual calculation of the statistics for one file.
	 */
	public void calcForOneFile() {
		/**
		 * read numbers in file
		 */
		try {
			DescriptiveStatistics statsFile = DescriptiveStatistics
					.newInstance();
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			DataInputStream dis = null;
			fis = new FileInputStream(fileName);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			while (dis.available() != 0) {
				/**
				 * this statement reads the line from the file and print it to
				 * the console.
				 */
				String readVal = dis.readLine();
				double doubleRead = Double.parseDouble(readVal);
				statsFile.addValue(doubleRead);
			}
			/**
			 * dispose all the resources after using them.
			 */
			fis.close();
			bis.close();
			dis.close();
			/**
			 * values for a single file
			 */
			double N = statsFile.getN();
			double mean = statsFile.getMean();
			double max = statsFile.getMax();
			double min = statsFile.getMin();
			double variance = statsFile.getVariance();
			double standdev = statsFile.getStandardDeviation();
			double firstQuartile = statsFile.getPercentile(25);
			double median = statsFile.getPercentile(50);
			double thirdQuartile = statsFile.getPercentile(75);
			/**
			 * add single file values to arrayList
			 */
			List listStats = (List) statsFiles.get("tpt");
			listStats.add(N);
			listStats.add(mean);
			listStats.add(max);
			listStats.add(min);
			listStats.add(variance);
			listStats.add(standdev);
			listStats.add(firstQuartile);
			listStats.add(median);
			listStats.add(thirdQuartile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The actual calculation of the statistics for multiple files.
	 */
	public void calc() {
		for (String file : files) {
			String fileDest = file;

			/**
			 * read numbers in file
			 */
			try {
				DescriptiveStatistics statsFile = DescriptiveStatistics
						.newInstance();
				FileInputStream fis = null;
				BufferedInputStream bis = null;
				DataInputStream dis = null;
				fis = new FileInputStream(fileDest);
				bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);
				while (dis.available() != 0) {
					/**
					 * this statement reads the line from the file and print it
					 * to the console.
					 */
					String readVal = dis.readLine();
					double doubleRead = Double.parseDouble(readVal);
					statsFile.addValue(doubleRead);
				}
				/**
				 * dispose all the resources after using them.
				 */
				fis.close();
				bis.close();
				dis.close();
				/**
				 * values for a single file
				 */
				double mean = statsFile.getMean();
				double sd = statsFile.getStandardDeviation();
				/**
				 * add single file values to arrayList
				 */
				List list = (List) this.meansFiles.get("tpt");
				list.add(mean);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		/**
		 * calculate now the final statistics
		 */
		// for (String file2 : files) {
		List listMeans = (List) meansFiles.get("tpt");
		DescriptiveStatistics stats = DescriptiveStatistics.newInstance();
		ListIterator it = listMeans.listIterator();
		while (it.hasNext()) {
			double next = (Double) it.next();
			stats.addValue(next);
		}
		/**
		 * finished calculation for one file, gather results.
		 */
		double N = stats.getN();
		double mean = stats.getMean();
		double max = stats.getMax();
		double min = stats.getMin();
		double variance = stats.getVariance();
		double standdev = stats.getStandardDeviation();
		double firstQuartile = stats.getPercentile(25);
		double median = stats.getPercentile(50);
		double thirdQuartile = stats.getPercentile(75);
		List listStats = (List) statsFiles.get("tpt");
		listStats.add(N);
		listStats.add(mean);
		listStats.add(max);
		listStats.add(min);
		listStats.add(variance);
		listStats.add(standdev);
		listStats.add(firstQuartile);
		listStats.add(median);
		listStats.add(thirdQuartile);
	}

	/**
	 * Writes the statistic results to a file
	 * 
	 * @param location
	 *            String location of the file to write to.
	 * @return result File the created file with statistics.
	 */
	public File createResultFile(String location) {
		File result = new File(location);
		try {
			result.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * write the calculated statistics to the result file
		 */
		FileWriter out = null;
		try {
			out = new FileWriter(location);
			out
					.write("The simulation results for the throughput time of the node "
							+ nodeFolder.getName() + " are: \n");
			out.write("N =" + getN() + "\n");
			out.write("mean =" + getMean() + "\n");
			out.write("max =" + getMax() + "\n");
			out.write("min =" + getMin() + "\n");
			out.write("variance =" + getVariance() + "\n");
			out.write("standard deviation =" + getStandardDeviation() + "\n");
			out.write("confidence interval = (" + getConf95LowerBound() + " ; "
					+ getConf95UpperBound() + ")");
			out.flush();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Returns the number of sub runs
	 * 
	 * @return value Double the number of sub runs
	 */
	public Double getN() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(0);
		return value;
	}

	/**
	 * Returns the calculation mean
	 * 
	 * @return value Double the mean
	 */
	public Double getMean() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(1);
		return value;
	}

	/**
	 * Returns the calculated maximum
	 * 
	 * @return value Double the maximum
	 */
	public Double getMax() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(2);
		return value;
	}

	/**
	 * Returns the calculated minimum
	 * 
	 * @return value Double the minimum
	 */
	public Double getMin() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(3);
		return value;
	}

	/**
	 * Returns the calculated variance
	 * 
	 * @return value Double the variance
	 */
	public Double getVariance() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(4);
		return value;
	}

	/**
	 * Returns the calculated standard deviation
	 * 
	 * @return value Double the standard deviation
	 */
	public Double getStandardDeviation() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(5);
		return value;
	}

	/**
	 * Returns the calculated first quartile
	 * 
	 * @return value Double the first quartile
	 */
	public Double getFirstQuartile() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(6);
		return value;
	}

	/**
	 * Returns the calculated median
	 * 
	 * @return value Double the median
	 */
	public Double getMedian() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(7);
		return value;
	}

	/**
	 * Returns the calculated third quartile
	 * 
	 * @return value Double the third quartile
	 */
	public Double getThirdQuartile() {
		String fn = (String) statsFiles.keySet().iterator().next();
		Double value = ((List<Double>) statsFiles.get(fn)).get(8);
		return value;
	}

	/**
	 * Returns the calculated lower bound of a 95% confidence interval
	 * 
	 * @return value Double the 95% lower bound
	 */
	public Double getConf95LowerBound() {
		Double value = getMean() - 1.96 * getStandardDeviation();
		return value;
	}

	/**
	 * Returns the calculated upper bound of a 95% confidence interval
	 * 
	 * @return value Double the 95% upper bound
	 */
	public Double getConf95UpperBound() {
		Double value = getMean() + 1.96 * getStandardDeviation();
		return value;
	}

}
