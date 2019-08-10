package org.processmining.framework.models.transitionsystem;

import java.util.HashMap;

public class PetrifyConstants {
	public final static String EVENTTYPESEPARATOR = "_.";
	public static final String EDGEDOCSSEPARATOR = "._";

	public static HashMap<String, String> BadSymbolsMap;
	public static HashMap<String, String> BadSymbolsMapBack;

	static {
		BadSymbolsMap = new HashMap<String, String>();
		BadSymbolsMapBack = new HashMap<String, String>();

		BadSymbolsMap.put(" ", "_.0");
		BadSymbolsMap.put("-", "_.1");
		BadSymbolsMap.put(":", "_.2");
		BadSymbolsMap.put("/", "_.3");
		BadSymbolsMap.put("(", "_.4");
		BadSymbolsMap.put(")", "_.5");
		BadSymbolsMap.put("&", "_.6");
		BadSymbolsMap.put("%", "_.7");
		BadSymbolsMap.put("?", "_.8");
		BadSymbolsMap.put("!", "_.9");
		BadSymbolsMap.put("#", "_.10");
		BadSymbolsMap.put("+", "_.11");
		BadSymbolsMap.put("*", "_.12");
		BadSymbolsMap.put(";", "_.13");
		BadSymbolsMap.put(",", "_.14");
		BadSymbolsMap.put("{", "_.15");
		BadSymbolsMap.put("}", "_.16");
		BadSymbolsMap.put("~", "_.17");
		BadSymbolsMap.put("$", "_.18");
		BadSymbolsMap.put(" ", "_.19");
		BadSymbolsMap.put("^", "_.20");

		BadSymbolsMapBack.put("_.0", " ");
		BadSymbolsMapBack.put("_.1", "-");
		BadSymbolsMapBack.put("_.2", ":");
		BadSymbolsMapBack.put("_.3", "/");
		BadSymbolsMapBack.put("_.4", "(");
		BadSymbolsMapBack.put("_.5", ")");
		BadSymbolsMapBack.put("_.6", "&");
		BadSymbolsMapBack.put("_.7", "%");
		BadSymbolsMapBack.put("_.8", "?");
		BadSymbolsMapBack.put("_.9", "!");
		BadSymbolsMapBack.put("_.10", "#");
		BadSymbolsMapBack.put("_.11", "+");
		BadSymbolsMapBack.put("_.12", "*");
		BadSymbolsMapBack.put("_.13", ";");
		BadSymbolsMapBack.put("_.14", ",");
		BadSymbolsMapBack.put("_.15", "{");
		BadSymbolsMapBack.put("_.16", "}");
		BadSymbolsMapBack.put("_.17", "~");
		BadSymbolsMapBack.put("_.18", "$");
		BadSymbolsMapBack.put("_.19", " ");
		BadSymbolsMapBack.put("_.20", "^");
	}
}
