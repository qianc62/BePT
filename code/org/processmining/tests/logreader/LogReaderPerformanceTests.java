package org.processmining.tests.logreader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.ui.OpenLogSettings;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;
import org.processmining.tests.utils.ValuePlotter;
import org.testng.annotations.Test;

public class LogReaderPerformanceTests extends ValuePlotter {

	@Test
	public void testPass() throws IOException, InterruptedException {
		computeMemoryUsageFromThisPointOn();
		startTimer();
		Thread.sleep(500);
		pauseTimer();
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 1000000; i++) {
			list.add(i);
		}
		Thread.sleep(1000);
		resumeTimer();
		Thread.sleep(1500);
		plotTimer("logreader_cpu");
		plotMemory("logreader_mem");

	}

	private LogReader openLog(String filename) throws Exception {
		LogFile logFile = LogFile.getInstance(filename);
		OpenLogSettings settings = new SlickerOpenLogSettings(logFile);

		return LogReaderFactory
				.createInstance(settings.getLogFilter(), logFile);
	}
}
