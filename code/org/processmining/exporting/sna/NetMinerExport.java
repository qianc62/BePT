package org.processmining.exporting.sna;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.analysis.originator.OTMatrix2DTableModel;
import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

import cern.colt.matrix.DoubleMatrix2D;

public class NetMinerExport implements ExportPlugin {

	public NetMinerExport() {
	}

	public String getName() {
		return "NetMiner";
	}

	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/netminerexport";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof SocialNetworkMatrix
					|| o[i] instanceof OTMatrix2DTableModel) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {

		Object[] o = object.getObjects();

		SocialNetworkMatrix snMatrix = null;
		OTMatrix2DTableModel otMatrix = null;
		DoubleMatrix2D matrix = null;
		DoubleMatrix2D roleMatrix = null;
		DoubleMatrix2D orgUnitMatrix = null;

		String[] users = null;
		String[] tasks = null;

		// the case of SNA result
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof SocialNetworkMatrix) {
				snMatrix = (SocialNetworkMatrix) o[i];
				users = snMatrix.getNodeNames();
				matrix = snMatrix.getMatrix();
			}
		}

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof OTMatrix2DTableModel) {
				otMatrix = (OTMatrix2DTableModel) o[i];
				tasks = otMatrix.getTasks();
				if (users == null)
					users = otMatrix.getUsers();
			}
		}

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
		Message.add("<NetMinerExport>", Message.TEST);

		writeSnaPart(users, matrix, bw);

		int numAFF = 0;
		if (otMatrix != null)
			numAFF++;

		if (snMatrix != null) {
			orgUnitMatrix = snMatrix.getOrgUnitMatrix();
			if (orgUnitMatrix != null)
				numAFF++;

			roleMatrix = snMatrix.getRoleMatrix();
			if (roleMatrix != null)
				numAFF++;
		}

		if (numAFF != 0)
			bw.write("\nAFF#=" + numAFF + "\n");

		if (otMatrix != null)
			writeOTPart(tasks, users, otMatrix, bw);

		if (orgUnitMatrix != null)
			writeAffiliatedMatrix("OrgUnitAssignment", (String[]) snMatrix
					.getOrgUnitName().toArray(
							new String[snMatrix.getOrgUnitName().size()]),
					users.length, orgUnitMatrix, bw);

		if (roleMatrix != null)
			writeAffiliatedMatrix("RoleAssignment", (String[]) snMatrix
					.getRoleName().toArray(
							new String[snMatrix.getRoleName().size()]),
					users.length, roleMatrix, bw);

		bw.close();
		Message.add("</NetMinerExport>", Message.TEST);
		return;
	}

	public String getFileExtension() {
		return "NTF";
	}

	private void writeSnaPart(String[] users, DoubleMatrix2D matrix,
			BufferedWriter bw) throws IOException {

		int matrixSize = users.length;

		bw.write("NTF=2.4\nTITLE=EXPORTED\n");
		bw.write("N=" + String.valueOf(matrixSize) + "\n");
		Message.add("<SummaryOfNetMiner networkSize=\""
				+ String.valueOf(matrixSize) + "\">", Message.TEST);
		bw.write("NODELIST=");

		for (int j = 0; j < users.length; j++) {
			if (users[j].equals(""))
				bw.write("noname,");
			else
				bw.write(users[j] + ",");
		}
		bw.write("\n\nADJ#=1\n\n");
		bw.write("ADJNAME=name\n");
		bw.write("TYPE=FULLMATRIX,DIAGONAL=NO\n");
		bw.write("DIRECTION=YES,WEIGHT=YES\n\n");

		bw.write("DATA\n");

		for (int j = 0; j < matrixSize; j++) {
			for (int k = 0; k < matrixSize; k++)
				if (j != k) {
					if (matrix == null)
						bw.write(Double.toString(0.0) + ",");
					else
						bw.write(Double.toString(matrix.get(j, k)) + ",");
				}
			bw.write("\n");
		}

		bw.write("\n/DATA\n\n");
		Message.add("<SummaryOfNetMiner writeSocialNetwork=\"OK\">",
				Message.TEST);
	}

	private void writeOTPart(String[] tasks, String[] users,
			OTMatrix2DTableModel matrix, BufferedWriter bw) throws IOException {

		bw.write("AFFNAME=OTMmatrix\n");
		bw.write("CAT#=" + String.valueOf(matrix.getColumnCount() - 1) + "\n");
		Message.add("<SummaryOfNetMiner OTMatrixSize=\""
				+ String.valueOf(matrix.getColumnCount() - 1) + "\">",
				Message.TEST);
		bw.write("CATNAME=");

		for (int j = 0; j < tasks.length; j++) {
			String temp;
			temp = tasks[j].replace(',', '.');
			bw.write(temp + "@ ,");
		}

		bw.write("\nDATA\n");

		int i = 0;
		for (int j = 0; j < matrix.getRowCount(); j++) {
			if (users[i].endsWith((String) matrix.getValueAt(j, 0))) {
				for (int k = 0; k < tasks.length; k++)
					bw.write((String) matrix.getValueAt(j, k + 1) + ",");
				bw.write("\n");
				if (i < users.length - 1)
					i++;
				else
					break;
			}

		}
		bw.write("/DATA\n\n");
		Message.add("<SummaryOfNetMiner writeOriginatorByTaskMatrix=\"OK\">",
				Message.TEST);
	}

	private void writeAffiliatedMatrix(String AfMatrixName, String[] catNAME,
			int numOfUsers, DoubleMatrix2D matrix, BufferedWriter bw)
			throws IOException {

		bw.write("\nAFFNAME=" + AfMatrixName + "\n");
		bw.write("CAT#=" + catNAME.length + "\n");
		Message.add("<SummaryOfNetMiner AffMatrixSize=\""
				+ String.valueOf(catNAME.length) + "\">", Message.TEST);
		bw.write("CATNAME=");

		for (int j = 0; j < catNAME.length; j++) {
			String temp;
			temp = catNAME[j].replace(',', '.');
			bw.write(temp + ",");
		}

		bw.write("\nDATA\n");

		for (int i = 0; i < numOfUsers; i++) {
			for (int j = 0; j < catNAME.length; j++)
				bw.write(String.valueOf(matrix.get(i, j)) + ",");
			bw.write("\n");
		}
		bw.write("/DATA\n\n");

		Message.add("<SummaryOfNetMiner writeAffiliatedMatrix=\"OK\">",
				Message.TEST);
	}
}
