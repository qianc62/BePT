package org.processmining;

import java.util.ArrayList;

public interface ProMSplash {

	public abstract void open();

	public abstract void close();

	public abstract void changeText(String s, int status);

	public abstract void setProgress(int progress);

	public abstract ArrayList getLog();

}