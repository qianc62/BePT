package org.processmining.tests.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;

public class ValuePlotter {

	private String outputDirectory;
	private boolean timerRunning = false;
	private boolean timerPaused = false;
	private long startTime = 0;
	private long startMemory = 0;
	private long accumulatedTime = 0;

	@BeforeMethod
	public void setupLogging(ITestContext context) {
		outputDirectory = context.getOutputDirectory();
		new File(outputDirectory).mkdirs();
	}

	public void plot(String name, double value) throws IOException {
		File outputFile = new File(outputDirectory, name);
		OutputStreamWriter out = new FileWriter(outputFile, false);

		out.write("YVALUE=" + value + System.getProperty("line.separator"));
		out.close();
	}

	public void plotMemory(String name) throws IOException {
		plot(name, (getMemoryUsage() - startMemory) / (1024.0 * 1024.0));
	}

	public void plotTimer(String name) throws IOException {
		long time = accumulatedTime + (getCurrentTime() - startTime);
		assert timerRunning || timerPaused : "Timer not started";
		plot(name, time / 1000.0);
	}

	public void startTimer() {
		timerRunning = true;
		startTime = getCurrentTime();
	}

	public void pauseTimer() {
		assert !timerPaused : "Cannot pause timer, because it's already paused";
		if (timerRunning) {
			timerRunning = false;
			timerPaused = true;
			accumulatedTime += getCurrentTime() - startTime;
		}
	}

	public void resumeTimer() {
		assert !timerRunning : "Cannot resume timer, because it's still running";
		if (timerPaused) {
			timerPaused = false;
			startTimer();
		}
	}

	public void computeMemoryUsageFromThisPointOn() {
		startMemory = getMemoryUsage();
	}

	private long getMemoryUsage() {
		pauseTimer();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		resumeTimer();

		return Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
	}

	private long getCurrentTime() {
		return System.currentTimeMillis();
	}
}
