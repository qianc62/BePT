package org.processmining.analysis.differences;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.processmining.analysis.differences.processdifferences.ProcessAutomaton;
import org.processmining.analysis.differences.processdifferences.ProcessDifference;
import org.processmining.analysis.differences.processdifferences.ProcessDifferences;
import org.processmining.analysis.differences.relations.Relation;
import org.processmining.analysis.differences.relations.Tuple;
import org.processmining.analysis.epc.similarity.Checker;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCHierarchy;
import org.processmining.framework.util.StopWatch;
import org.processmining.importing.aml.amlImport;
import org.processmining.mining.epcmining.EPCResult;

public class MultiDifferencesUI extends JPanel {

	private static final List<Tuple<String, String>> pairsOfEPCIds = new ArrayList<Tuple<String, String>>();
	private static final String tableHeadings[] = { "EPC1Path", "EPC2Path",
			"#Functions", "#Events", "#Control", "#ITER", "#ADDDEP",
			"#DIFFDEP", "#DIFFMOM", "#ADDCOND", "#DIFFCONF", "#SKIPPED",
			"#DIFFSTART", "PROCTIME" };
	private static final double syntacticCutoff = 0.9;

	// GUI
	private DefaultTableModel tabmResults;
	private JTable tabResults;
	private JScrollPane scpTableScrolls;

	// Data
	private List<EPCHierarchy> epcHierarchies;
	private Checker checker = new Checker(false);

	public MultiDifferencesUI(List<EPCHierarchy> epcHierarchies) {
		this.epcHierarchies = epcHierarchies;
		initListOfEPCs();
		initListOfMoreEPCs();
		guiPrepare();
	}

	private void initListOfMoreEPCs() {
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Be_1yk5",
						"group.root#sales and distribution#third party order processing#purchase requisition#purchase requisition"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Be_204a",
						"group.root#sales and distribution#third party order processing#invoice verification#invoice verification"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Be_25my",
						"group.root#sales and distribution#third party order processing#goods receipt#goods receipt"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Be_2tnc",
						"group.root#sales and distribution#third party order processing#invoice verification#invoice verification"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Be_394s",
						"group.root#sales and distribution#third party order processing#purchase requisition#purchase requisition"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Be_3a62",
						"group.root#sales and distribution#third party order processing#invoice verification#invoice verification"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ku_9bjf",
						"group.root#sales and distribution#complaints processing#shipment#shipment"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ku_9bjf",
						"group.root#sales and distribution#sending samples and advertising materials#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_4fbt",
						"group.root#sales and distribution#pre sales handling#pre sales handling"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4fin",
						"group.root#sales and distribution#pre sales handling#sales support cas#sales support cas"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4jln",
						"group.root#sales and distribution#foreign trade processing#preference processing#preference processing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4k75",
						"group.root#sales and distribution#foreign trade processing#declarations to the authorities#declarations to the authorities"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4mai",
						"group.root#sales and distribution#foreign trade processing#legal control#legal control"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_4mua",
						"group.root#sales and distribution#rebate processing#rebate processing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_4q66",
						"group.root#sales and distribution#complaints processing#complaints processing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_4xoy",
						"group.root#sales and distribution#complaints processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4ymf",
						"group.root#sales and distribution#empties and returnable packaging handling#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4ymf",
						"group.root#sales and distribution#sales order processing makeassembly to order#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4ymf",
						"group.root#sales and distribution#sales order processing standard#shipment#shipment"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4zsw",
						"group.root#sales and distribution#complaints processing#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_4zsw",
						"group.root#sales and distribution#consignment processing#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_531s",
						"group.root#sales and distribution#empties and returnable packaging handling#sales order#sales order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_57p5",
						"group.root#sales and distribution#empties and returnable packaging handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_58l9",
						"group.root#sales and distribution#complaints processing#shipment#shipment"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_58l9",
						"group.root#sales and distribution#sending samples and advertising materials#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_5i83",
						"group.root#sales and distribution#consignment processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5jtb",
						"group.root#sales and distribution#empties and returnable packaging handling#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5jtb",
						"group.root#sales and distribution#sales order processing makeassembly to order#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5jtb",
						"group.root#sales and distribution#sales order processing standard#shipment#shipment"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_5kzj",
						"group.root#sales and distribution#intercompany handling#shipping#shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5tcy",
						"group.root#sales and distribution#third party order processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5tcy",
						"group.root#sales and distribution#cash salesrush order handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_5tcy",
						"group.root#sales and distribution#consignment processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5x4o",
						"group.root#sales and distribution#sales order processing makeassembly to order#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5x4o",
						"group.root#sales and distribution#sales order processing standard#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5x4o",
						"group.root#sales and distribution#intercompany handling#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_5ycw",
						"group.root#sales and distribution#sending samples and advertising materials#shipping#shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_68lb",
						"group.root#sales and distribution#third party order processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_68lb",
						"group.root#sales and distribution#cash salesrush order handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_68lb",
						"group.root#sales and distribution#intercompany handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_68lb",
						"group.root#sales and distribution#sending samples and advertising materials#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_6bms",
						"group.root#sales and distribution#sales order processing makeassembly to order#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_6bms",
						"group.root#sales and distribution#complaints processing#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_6m1w",
						"group.root#sales and distribution#sales order processing makeassembly to order#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_6m1w",
						"group.root#sales and distribution#sales order processing standard#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_6m1w",
						"group.root#sales and distribution#consignment processing#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_6s89",
						"group.root#sales and distribution#sales order processing makeassembly to order#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_6s89",
						"group.root#sales and distribution#sales order processing standard#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_6s89",
						"group.root#sales and distribution#intercompany handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_6s89",
						"group.root#sales and distribution#consignment processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7180",
						"group.root#sales and distribution#cash salesrush order handling#sales order#sales order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7647",
						"group.root#sales and distribution#sales order processing makeassembly to order#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7647",
						"group.root#sales and distribution#sales order processing standard#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_7647",
						"group.root#sales and distribution#intercompany handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_7647",
						"group.root#sales and distribution#consignment processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_77z0",
						"group.root#sales and distribution#sales order processing makeassembly to order#shipping#shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_77z0",
						"group.root#sales and distribution#sales order processing standard#shipping#shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7c1w",
						"group.root#sales and distribution#sales order processing standard#sales order processing standard"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7if9",
						"group.root#sales and distribution#third party order processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7if9",
						"group.root#sales and distribution#cash salesrush order handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_7kcl",
						"group.root#sales and distribution#complaints processing#shipment#shipment"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7kcl",
						"group.root#sales and distribution#sending samples and advertising materials#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7lu4",
						"group.root#sales and distribution#sales order processing makeassembly to order#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7lu4",
						"group.root#sales and distribution#sales order processing standard#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7lu4",
						"group.root#sales and distribution#intercompany handling#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7lu4",
						"group.root#sales and distribution#consignment processing#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7n23",
						"group.root#sales and distribution#cash salesrush order handling#shipping#shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7s3r",
						"group.root#sales and distribution#sales order processing makeassembly to order#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7s3r",
						"group.root#sales and distribution#sales order processing standard#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_7s3r",
						"group.root#sales and distribution#consignment processing#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7suf",
						"group.root#sales and distribution#sales order processing standard#sales order#sales order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_7uuo",
						"group.root#sales and distribution#sales order processing makeassembly to order#sales order processing makeassembly to order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_80w9",
						"group.root#sales and distribution#third party order processing#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_80w9",
						"group.root#sales and distribution#cash salesrush order handling#billing#billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_82t3",
						"group.root#sales and distribution#complaints processing#shipment#shipment"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_82t3",
						"group.root#sales and distribution#sending samples and advertising materials#transport#transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_84am",
						"group.root#sales and distribution#sales order processing makeassembly to order#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_84am",
						"group.root#sales and distribution#sales order processing standard#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_84am",
						"group.root#sales and distribution#intercompany handling#warehouse management#warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_85il",
						"group.root#sales and distribution#cash salesrush order handling#shipping#shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_8ak0",
						"group.root#sales and distribution#sales order processing makeassembly to order#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_8ak0",
						"group.root#sales and distribution#sales order processing standard#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>("1Ve_8ak0",
						"group.root#sales and distribution#consignment processing#costing#costing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"1Ve_8bao",
						"group.root#sales and distribution#sales order processing makeassembly to order#sales order#sales order"));
	}

	private void initListOfEPCs() {
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Consignment Processing#Warehouse Management#Warehouse Management",
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Warehouse management#Warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Consignment Processing#Billing#Billing",
						"Group.Root#Sales and Distribution#Sending Samples and Advertising Materials#Billing#Billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Purchase Requisition#Purchase Requisition",
						"Group.Root#Procurement#Procurement of Materials and External Services#Purchase Requisition#Purchase Requisition"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Cash Sales/Rush Order Handling",
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Cash Sales/Rush Order Handling"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Goods Receipt#Goods Receipt",
						"Group.Root#Procurement#Procurement of Materials and External Services#Goods Receipt#Goods Receipt"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Consignment Processing#Warehouse Management#Warehouse Management",
						"Group.Root#Sales and Distribution#Complaints Processing#Warehouse Management#Warehouse Management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Shipping#Shipping",
						"Group.Root#Personnel Development#Personnel Appraisal#Personnel Appraisal"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Sales order#Sales order",
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Sales Order#Sales Order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Billing#Billing",
						"Group.Root#Sales and Distribution#Intercompany Handling#Billing#Billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Sales Order Processing: Make/Assembly To Order",
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Sales Order Processing (Standard)"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Transport#Transport",
						"Group.Root#Procurement#Source Administration#Source Administration"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Purchasing#Purchasing",
						"Group.Root#Revenue and Cost Controlling#Profit and Cost Planning#Profit Planning#Profit Planning"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Sales Order#Sales Order",
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Sales order#Sales order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Intercompany Handling#Warehouse Management#Warehouse Management",
						"Group.Root#Compensation Management#Long-Term Incentives#Monitoring of Long-Term Incentives Program#Monitoring of Long-Term Incentives Program"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Shipping#Shipping",
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Shipping#Shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Sales Order#Sales Order",
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Sales order#Sales order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sending Samples and Advertising Materials#Shipping#Shipping",
						"Group.Root#Treasury#Process OTC Derivative Transactions [TR-DE]#Transaction Processing#Transaction Processing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Shipping#Shipping",
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Shipping#Shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Invoice Verification#Invoice Verification",
						"Group.Root#Procurement#Procurement of Materials and External Services#Invoice Verification#Invoice Verification"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Sales Order#Sales Order",
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Sales Order#Sales Order"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Foreign Trade Processing#Preference Processing#Preference Processing",
						"Group.Root#Financial Accounting#Accounts Payable#Vendor Account Analysis#Vendor Account Analysis"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Complaints Processing#Shipment#Shipment",
						"Group.Root#Sales and Distribution#Empties and Returnable Packaging Handling#Transport#Transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Costing#Costing",
						"Group.Root#Project Management#Execution#Production Processing#Production Processing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Warehouse Management#Warehouse Management",
						"Group.Root#Sales and Distribution#Consignment Processing#Warehouse Management#Warehouse Management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Foreign Trade Processing#Documentary Payments#Documentary Payments",
						"Group.Root#Sales and Distribution#Pre-Sales Handling#Customer Inquiry#Customer Inquiry"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Complaints Processing#Billing#Billing",
						"Group.Root#Sales and Distribution#Empties and Returnable Packaging Handling#Billing#Billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Risk/Credit Management#Risk/Credit Management",
						"Group.Root#Environment, Health and Safety#Occupational Health#Injury/Illness Log#Injury/Illness Log"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Warehouse Management#Warehouse Management",
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Warehouse management#Warehouse management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Complaints Processing#Shipping#Shipping",
						"Group.Root#Plant Maintenance#Project-Based Maintenance Processing#Capacity Planning#Capacity Planning"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Intercompany Handling#Warehouse Management#Warehouse Management",
						"Group.Root#Sales and Distribution#Complaints Processing#Warehouse Management#Warehouse Management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Billing#Billing",
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Billing#Billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Costing#Costing",
						"1Pr_f7e-"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Empties and Returnable Packaging Handling#Billing#Billing",
						"Group.Root#Sales and Distribution#Consignment Processing#Billing#Billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Warehouse management#Warehouse management",
						"Group.Root#Procurement#Return Deliveries#Warehouse#Warehouse"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Consignment Processing#Consignment Processing",
						"Group.Root#Project Management#Execution#Materials Procurement and Service Processing#Materials Procurement and Service Processing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Intercompany Handling#Billing#Billing",
						"Group.Root#Sales and Distribution#Consignment Processing#Billing#Billing"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Empties and Returnable Packaging Handling#Transport#Transport",
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Transport#Transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Consignment Processing#Sales Order#Sales Order",
						"Group.Root#Production Planning and Procurement Planning#Market-Oriented Planning#Master Production Scheduling#Master Production Scheduling"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Invoice Verification#Invoice Verification",
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Invoice Verification#Invoice Verification"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Shipping#Shipping",
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Shipping#Shipping"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Shipping#Shipping",
						"Group.Root#Asset Accounting#Handling Fixed Assets#Investment Support#Investment Support"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Risk/Credit Management#Risk/Credit Management",
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Risk/Credit Management#Risk/Credit Management"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Rebate Processing#Rebate Settlement#Rebate Settlement",
						"Group.Root#Sales and Distribution#Rebate Processing#Rebate Settlement#Rebate Settlement"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Empties and Returnable Packaging Handling#Sales Order#Sales Order",
						"Group.Root#Asset Accounting#Handling Fixed Assets#Group Requirements#Group Requirements"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing: Make/Assembly To Order#Transport#Transport",
						"Group.Root#Sales and Distribution#Sending Samples and Advertising Materials#Transport#Transport"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Third-Party Order Processing#Invoice Verification#Invoice Verification",
						"Group.Root#Financial Accounting#Consolidation#Intercompany Eliminations#Intercompany Eliminations"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Cash Sales/Rush Order Handling#Shipping#Shipping",
						"Group.Root#Enterprise Controlling#Consolidation#Preparatory Work: Integration#Preparatory Work: Integration"));
		pairsOfEPCIds
				.add(new Tuple<String, String>(
						"Group.Root#Sales and Distribution#Sales Order Processing (Standard)#Costing#Costing",
						"Group.Root#Plant Maintenance#Planned Maintenance Processing#Maintenance Planning#Maintenance Planning"));
	}

	private void guiPrepare() {
		tabmResults = new DefaultTableModel();
		for (String heading : tableHeadings) {
			tabmResults.addColumn(heading);
		}
		tabResults = new JTable(tabmResults);

		JButton jbAnalyze = new JButton("Start");
		jbAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyze();
			}
		});

		scpTableScrolls = new JScrollPane();
		scpTableScrolls
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scpTableScrolls
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scpTableScrolls.setViewportView(tabResults);

		setLayout(new BorderLayout());
		add(scpTableScrolls, BorderLayout.CENTER);
		add(jbAnalyze, BorderLayout.SOUTH);
	}

	private void analyze() {
		int i = 0;
		for (Tuple<String, String> t : pairsOfEPCIds) {
			System.out.println(i);
			i++;
			String epc1Id = t.e1;
			String epc2Id = t.e2;
			ConfigurableEPC epc1 = getEPCById(epc1Id);
			ConfigurableEPC epc2 = getEPCById(epc2Id);
			if (epc1 == null) {
				tabmResults.addRow(new Object[] { "Could not find EPC 1." });
				return;
			}
			if (epc2 == null) {
				tabmResults.addRow(new Object[] { "Could not find EPC 2." });
				return;
			}

			ProcessDifferencesThread pdt = new ProcessDifferencesThread(
					(ModelGraph) epc1, (ModelGraph) epc2, fillRelation(epc1,
							epc2));
			pdt.start();

			int secondsToWait = 3 * 60 * 60;
			int secondsWaited = 0;
			while ((secondsWaited < secondsToWait) && !pdt.getDone()) {
				secondsWaited++;
				try {
					Thread.currentThread().sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String proctime;
			pdt.stop();

			Set<ProcessDifference> pdtresult = new HashSet<ProcessDifference>();
			if (!pdt.getDone()) {
				proctime = "INF";
			} else {
				pdtresult = pdt.getResult();
				proctime = Long.toString(pdt.getProcTime());
			}
			Iterator<ProcessDifference> j = pdtresult.iterator();

			int iter = 0; // "#ITER"
			int adddep = 0; // "#ADDDEP"
			int diffdep = 0; // "#DIFFDEP"
			int diffmom = 0; // "#DIFFMOM"
			int addcond = 0; // "#ADDCOND"
			int diffcond = 0; // "#DIFFCONF"
			int skipped = 0; // "#SKIPPED"
			int diffstart = 0; // "#DIFFSTART"
			while (j.hasNext()) {
				ProcessDifference pd = j.next();
				switch (pd.getDifferenceType()) {
				case ProcessDifference.TYPE_ITERATIVE:
					iter++;
					break;
				case ProcessDifference.TYPE_ADD_DEPENDENCIES:
					adddep++;
					break;
				case ProcessDifference.TYPE_DIFF_DEPENDENCIES:
					diffdep++;
					break;
				case ProcessDifference.TYPE_DIFF_MOMENTS:
					diffmom++;
					break;
				case ProcessDifference.TYPE_ADD_CONDITIONS:
					addcond++;
					break;
				case ProcessDifference.TYPE_DIFF_CONDITIONS:
					diffcond++;
					break;
				case ProcessDifference.TYPE_SKIPPED_ACTIVITY:
					skipped++;
					break;
				case ProcessDifference.TYPE_DIFF_START:
					diffstart++;
					break;
				default:
					break;
				}
			}
			int funcs = epc1.getFunctions().size() + epc2.getFunctions().size();
			int events = epc1.getEvents().size() + epc2.getEvents().size();
			int ctrls = epc1.getVerticeList().size()
					+ epc2.getVerticeList().size() - funcs - events;
			tabmResults.addRow(new Object[] { epc1Id, epc2Id, funcs, events,
					ctrls, iter, adddep, diffdep, diffmom, addcond, diffcond,
					skipped, diffstart, proctime });
		}
	}

	private Relation<ModelGraphVertex, ModelGraphVertex> fillRelation(
			ConfigurableEPC epc1, ConfigurableEPC epc2) {
		Relation<ModelGraphVertex, ModelGraphVertex> r = new Relation<ModelGraphVertex, ModelGraphVertex>();
		for (Object o1 : epc1.getFunctions()) {
			for (Object o2 : epc2.getFunctions()) {
				EPCFunction f1 = (EPCFunction) o1;
				EPCFunction f2 = (EPCFunction) o2;
				if (checker.syntacticEquivalenceScore(f1.getIdentifier(), f2
						.getIdentifier()) >= syntacticCutoff) {
					r.addR(new Tuple(f1, f2));
				}
			}
		}
		return r;
	}

	private ConfigurableEPC getEPCById(String id) {
		ConfigurableEPC foundEPC = null;
		for (Iterator<EPCHierarchy> epchi = epcHierarchies.iterator(); epchi
				.hasNext()
				&& foundEPC == null;) {
			EPCHierarchy epch = epchi.next();
			foundEPC = findEPCin(id, epch.getRoots(), epch);
		}
		return foundEPC;
	}

	private boolean idsMatch(String id1, String id2) {
		if (id1.equals(id2)) {
			return true;
		}
		String nid1 = id1.toLowerCase();
		nid1 = nid1.replaceAll("[^a-zA-Z_0-9]", "");
		String nid2 = id2.toLowerCase();
		nid2 = nid2.replaceAll("[^a-zA-Z_0-9]", "");
		if (nid1.equals(nid2)) {
			return true;
		}
		return false;
	}

	private ConfigurableEPC findEPCin(String id, Collection<Object> os,
			EPCHierarchy h) {
		int nextDot = id.indexOf('#');
		if (nextDot == -1) {
			// Check if os contains a ConfigurableEPC with this id
			// If so return that ConfigurableEPC, if not return null
			for (Object o : os) {
				if (o instanceof ConfigurableEPC) {
					if (idsMatch(((ConfigurableEPC) o).getIdentifier(), id)) {
						return (ConfigurableEPC) o;
					}
				}
			}
			return null;
		} else {
			// Get subid (part of id until first dot)
			String subid = id.substring(0, nextDot);
			String restid = id.substring(nextDot + 1);
			// If this contains a hierarchy with the given name, which contains
			// the ConfigurableEPC
			for (Object o : os) {
				if (idsMatch(o.toString(), subid)) {
					ConfigurableEPC foundEPC = findEPCin(restid, h
							.getChildren(o), h);
					if (foundEPC != null) {
						return foundEPC;
					}
				}
			}
			return null;
		}
	}
}

class ProcessDifferencesThread extends Thread {

	ModelGraph m1;
	ModelGraph m2;
	Relation<ModelGraphVertex, ModelGraphVertex> r;
	Set<ProcessDifference> result;
	boolean done = false;
	long proctime = 0;

	public ProcessDifferencesThread(ModelGraph m1, ModelGraph m2,
			Relation<ModelGraphVertex, ModelGraphVertex> r) {
		this.m1 = m1;
		this.m2 = m2;
		this.r = r;
		this.result = null;
	}

	public void run() {
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		ProcessDifferences diffsProcessor = new ProcessDifferences(m1, m2, r);
		result = diffsProcessor.proceduralDifferences();
		proctime = stopwatch.stop();
		setDone(true);
	}

	public synchronized boolean getDone() {
		return done;
	}

	public synchronized void setDone(boolean done) {
		this.done = done;
	}

	public Set<ProcessDifference> getResult() {
		return result;
	}

	public long getProcTime() {
		return proctime;
	}
}