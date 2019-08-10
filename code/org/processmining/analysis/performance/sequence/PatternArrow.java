package org.processmining.analysis.performance.sequence;

/**
 * Represents the transfer of work between two data-element instances in a
 * pattern.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class PatternArrow extends Arrow {
	private double avgBeginTime = 0;
	private double avgEndTime = 0;
	private long frequencyBegin = 0;
	private long frequencyEnd = 0;

	public PatternArrow(long beginTime, long endTime, String source,
			String destination) {
		super(source, destination);
		avgBeginTime = beginTime;
		avgEndTime = endTime;
		frequencyBegin = 1;
		frequencyEnd = 1;
	}

	/**
	 * Adjusts the average begin time, so beginTime is included.
	 * 
	 * @param beginTime
	 *            double
	 */
	public void addBeginTime(double beginTime) {
		double totalTime = avgBeginTime * frequencyBegin;
		frequencyBegin++;
		avgBeginTime = (totalTime + beginTime) / frequencyBegin;
	}

	/**
	 * Adjusts the average end time, so endTime is included.
	 * 
	 * @param endTime
	 *            double
	 */
	public void addEndTime(double endTime) {
		double totalTime = avgEndTime * frequencyEnd;
		frequencyEnd++;
		avgEndTime = (totalTime + endTime) / frequencyEnd;
	}

	/**
	 * Returns the average begin time of the arrow
	 * 
	 * @return double
	 */
	public double getAverageBeginTime() {
		return avgBeginTime;
	}

	/**
	 * Returns the average end time of the arrow
	 * 
	 * @return double
	 */
	public double getAverageEndTime() {
		return avgEndTime;
	}

	/**
	 * Returns the average time between the begin and the end of the arrow
	 * 
	 * @return double
	 */
	public double getTimeIn() {
		return (avgEndTime - avgBeginTime);
	}

	/**
	 * Checks whether an arrow is equal to another one
	 * 
	 * @param obj
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PatternArrow) {
			PatternArrow other = (PatternArrow) obj;
			try {
				if (other.getAverageBeginTime() == this.getAverageBeginTime()
						&& other.getAverageEndTime() == this
								.getAverageEndTime()
						&& other.getSource().equals(this.getSource())
						&& other.getDestination().equals(this.getDestination())) {
					return true;
				} else {
					return false;
				}
			} catch (NullPointerException ex) {
				return false;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}

}
