package org.processmining.importing.coloredcontrolflownet;

import java.io.Reader;

import org.processmining.framework.models.coloredcontrolflownet.ColoredControlFlowNet;
import org.processmining.framework.models.coloredcontrolflownet.PlaceType;
import org.processmining.framework.models.coloredcontrolflownet.inscription.ArcInscription;
import org.processmining.importing.coloredcontrolflownet.arcinscriptionparser.Scanner;
import org.processmining.importing.coloredcontrolflownet.arcinscriptionparser.parser;
import org.processmining.importing.simplecpn.SimpleCPNParser;

public class ColoredControlFlowNetParser
		extends
		SimpleCPNParser<parser, PlaceType, Object, ArcInscription, Object, ColoredControlFlowNet> {

	@Override
	public parser getArcParser(Reader reader) {
		return new parser(new Scanner(reader));
	}

	@Override
	public ColoredControlFlowNet getNewEmptyNet() {
		return new ColoredControlFlowNet();
	}

	@Override
	public PlaceType getPlaceAnnotation(String type) {
		if ("PID".equals(type))
			return PlaceType.PID;
		else if ("STATE".equals(type))
			return PlaceType.STATE;
		return null;
	}
}