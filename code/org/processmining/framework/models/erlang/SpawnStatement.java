package org.processmining.framework.models.erlang;

import java.util.List;

public class SpawnStatement extends Statement {

	private final String pid;
	private final String name;
	private final List<String> synchronizePids;

	public SpawnStatement(String pid, String name, List<String> synchronizePids) {
		this.pid = pid;
		this.name = name;
		this.synchronizePids = synchronizePids;
	}

	@Override
	public String toString(String pad) {
		String list = "[";
		int index = 0;
		for (String s : synchronizePids) {
			list += s;
			if (index < synchronizePids.size() - 1)
				list += ", ";
			index++;
		}
		list += "]";
		return pad + pid + " = spawn(petri_net, " + name + ", " + list + ")";
	}

}
