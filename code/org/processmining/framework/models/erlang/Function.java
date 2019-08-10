package org.processmining.framework.models.erlang;

import java.util.ArrayList;
import java.util.List;

public class Function {

	public final String name;

	public final List<Statement> statements;

	public final List<Function> functions;

	public final List<Function> visibleFunctions;

	public final List<String> synchronizePids;

	public Function(String name, List<Statement> statements,
			List<Function> functions, List<Function> visibleFunctions,
			List<String> synchronizePids) {
		super();
		String tmp = name.replaceAll("\\\\n", "_");
		this.name = tmp.substring(0, 1).toLowerCase() + tmp.substring(1);
		this.statements = statements;
		this.functions = functions;
		// for (Function function : functions) {
		// if (!function.functions.isEmpty()) {
		// int i = 0;
		// }
		// }
		this.visibleFunctions = visibleFunctions;
		this.synchronizePids = synchronizePids;
	}

	public Function(String name, List<Statement> statements) {
		this(name, statements, new ArrayList<Function>(),
				new ArrayList<Function>(), new ArrayList<String>());
	}

	public Function(String name, List<Statement> statements,
			List<Function> functions, List<Function> visibleFunctions) {
		this(name, statements, functions, visibleFunctions,
				new ArrayList<String>());
	}

	public Function(String name, Statement... statements) {
		this.name = name;
		this.statements = new ArrayList<Statement>();
		for (Statement statement : statements) {
			this.statements.add(statement);
		}
		this.functions = new ArrayList<Function>();
		this.visibleFunctions = new ArrayList<Function>();
		this.synchronizePids = new ArrayList<String>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name + "(");
		int index = 0;
		for (String str : synchronizePids) {
			builder.append(str);
			if (index < synchronizePids.size() - 1) {
				builder.append(", ");
			}
		}
		builder.append(") -> \n");
		index = 0;
		for (Statement statement : statements) {
			builder.append(statement.toString("\t"));
			if (index < statements.size() - 1) {
				builder.append(",\n");
			} else {
				builder.append(".\n\n");
			}
			index++;
		}
		for (Function function : functions) {
			builder.append(function.toString());
		}
		return builder.toString();
	}

	public int numberOfArguments() {
		return synchronizePids.size();
	}
}
