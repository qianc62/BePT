package org.processmining.framework.models.erlang;

import java.util.List;

import org.processmining.framework.models.bpel.util.Pair;

public class ChoiceStatement extends Statement {

	public final List<Pair<String, ? extends Statement>> choices;

	private static int varIndex = 1;

	public ChoiceStatement(List<Pair<String, ? extends Statement>> choices) {
		this.choices = choices;
	}

	@Override
	public String toString(String pad) {
		StringBuilder builder = new StringBuilder();
		if (choices.size() == 1) {
			builder.append(choices.get(0).second.toString(pad));
		} else {
			int startVarIndex = varIndex - 1;
			for (int i = 1; i <= choices.size() - 1; i++) {
				builder.append(pad + "X" + (startVarIndex + i)
						+ " = random:uniform(),\n");
			}
			builder.append(pad + "if\n");
			int index = 1;
			double propability = Math.round(100 / choices.size()) / 100.0;
			for (Pair<String, ? extends Statement> choice : choices) {
				if (index < choices.size()) {
					builder.append(pad + "\tX" + (startVarIndex + index)
							+ " < " + propability + " ->\n"
							+ choice.second.toString(pad + "\t\t"));
					varIndex++;
				} else {
					builder.append(pad + "\ttrue ->\n"
							+ choice.second.toString(pad + "\t\t"));
				}
				// builder.append(pad + " " + choice.first + " ->\n"
				// + choice.second.toString(pad + " "));
				if (index < choices.size()) {
					builder.append(";\n");
				}
				index++;
			}
			builder.append("\n" + pad + "end");
			varIndex += choices.size();
		}
		return builder.toString();
	}
}
