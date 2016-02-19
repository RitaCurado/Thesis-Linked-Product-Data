package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class SummaryPage {
	
//	private CardLayout card;
//	private JPanel contentPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private JComboBox<String> sourcesList;
	private JTextArea showInfo;
	private JLabel numInstances;
	private String chosenRule;
	
	private JSplitPane pageSummary;

	public SummaryPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content, String rule){
		this.swe = swe;
		frame = gui;
		chosenRule = rule;
		
		showInfo = new JTextArea();
		showInfo.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		numInstances = new JLabel("Number of instances: ");
		pageSummary = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageSummary;
	}
	
	@SuppressWarnings("rawtypes")
	public void createPage(){
		Set entries;
		Map.Entry mapping;
		Iterator entriesIterator;
		
		String mappRuleSource, equation;
		
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane infoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane tablesPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane instancesPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel sourcesPanel = new JPanel(new GridLayout(2, 3));
		JScrollPane scrolInfo;
		
		JTable initValuesTable, valuesAggsTable;
		JScrollPane initialScroll, aggScroll;
		
		JPanel matchesPanel = new JPanel(new GridLayout(2, 1));
		JPanel finalInstPanel = new JPanel(new GridLayout(2, 1));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 7));

		JSplitPane initialInstsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane afterAggInstsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JLabel initInsts, aggsInsts, numMatches, numFinal;
		JLabel title = new JLabel("Summary");
		JLabel sourcesLabel = new JLabel("Sources:");
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new OkListener());
		
		ArrayList<String> sources = new ArrayList<String>();
		HashMap<String, Integer> initialValues = swe.getInitialInsts();
		HashMap<String, Integer> valuesAfterFilter = swe.getInstsAfterAggs();
		
		Object columnNames[] = {"Source", "Number of instances"};
		Object[][] initValuesArray = new Object[initialValues.size()][2];
		Object[][] afterAggsArray = new Object[valuesAfterFilter.size()][2];
		
//		--------------
		entries = initialValues.entrySet();
		entriesIterator = entries.iterator();

		for(int i=0; entriesIterator.hasNext(); i++){

		    mapping = (Map.Entry) entriesIterator.next();

		    initValuesArray[i][0] = mapping.getKey();
		    initValuesArray[i][1] = mapping.getValue();
		}
		
		initValuesTable = new JTable(initValuesArray, columnNames);
		initialScroll = new JScrollPane(initValuesTable);
		
		initInsts = new JLabel("Initial number of instances:");
		initInsts.setFont(new Font("Arial", Font.BOLD, 13));
		
		//initialInstsPanel.setPreferredSize(new Dimension(300, 150));
		
		initialInstsPanel.setBackground(Color.WHITE);
		initialInstsPanel.add(initInsts);
		initialInstsPanel.add(initialScroll);
		initialInstsPanel.setDividerSize(1);
//		--------------
		
		entries = valuesAfterFilter.entrySet();
		entriesIterator = entries.iterator();
		
		for(int i=0; entriesIterator.hasNext(); i++){
			
			mapping = (Map.Entry) entriesIterator.next();
			
			afterAggsArray[i][0] = mapping.getKey();
			afterAggsArray[i][1] = mapping.getValue();
		}
		
		valuesAggsTable = new JTable(afterAggsArray, columnNames);
		aggScroll = new JScrollPane(valuesAggsTable);
		
		aggsInsts = new JLabel("Number of instances after aggregation rules:");
		aggsInsts.setFont(new Font("Arial", Font.BOLD, 13));
		
		afterAggInstsPanel.setBackground(Color.WHITE);
		afterAggInstsPanel.add(aggsInsts);
		afterAggInstsPanel.add(aggScroll);
		afterAggInstsPanel.setDividerSize(1);
		
		tablesPanel.setPreferredSize(new Dimension(400, 180));
		tablesPanel.setResizeWeight(0.5);
		initialInstsPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 5, 0, Color.WHITE));
		afterAggInstsPanel.setBorder(BorderFactory.createMatteBorder(5, 0, 2, 0, Color.WHITE));
		
		tablesPanel.add(initialInstsPanel);
		tablesPanel.add(afterAggInstsPanel);
		tablesPanel.setDividerSize(1);
//		--------------
		
		
		numMatches = new JLabel("  " + swe.getNumMatches());
		numMatches.setFont(new Font("Arial", Font.PLAIN, 13));
		
		matchesPanel.add(new JLabel("Number of matchings with mapping rule:"));
		matchesPanel.add(numMatches);
		
		//----//
		
		equation = "  (";
		
		for(int i=0; i < afterAggsArray.length; i++){
			equation += afterAggsArray[i][1] + " + ";
		}
		
		equation = equation.substring(0, equation.length()-2);
		equation += ") - " + swe.getNumMatches() + " = ";
		
		numFinal = new JLabel(equation + swe.getResultInstNum());
		numFinal.setFont(new Font("Arial", Font.PLAIN, 13));
		
		
		finalInstPanel.add(new JLabel("Total number of instances:"));
		finalInstPanel.add(numFinal);
		
		leftPanel.setPreferredSize(new Dimension(250, 100));
		leftPanel.setResizeWeight(0.5);
		leftPanel.add(matchesPanel);
		leftPanel.add(finalInstPanel);
		leftPanel.setDividerSize(1);
		
		//----//
		
		sources.add(0, "- Select an option -");
		sources.addAll(swe.getSources());
		
		if(!chosenRule.isEmpty()){
			mappRuleSource = swe.getPropertySource(chosenRule, false);
			sources.add(mappRuleSource);
		}
		
		String[] sourcesArray = new String[sources.size()];
		sourcesArray = sources.toArray(sourcesArray);
		sourcesList = new JComboBox<String>(sourcesArray);
		sourcesList.setSelectedIndex(0);
		sourcesList.addActionListener(new SourcesListListener());
		
		sourcesPanel.add(sourcesLabel);
		sourcesPanel.add(sourcesList);
		sourcesPanel.add(new JLabel(""));
		sourcesPanel.add(numInstances);
		sourcesPanel.add(new JLabel(""));
		sourcesPanel.add(new JLabel(""));
		
		scrolInfo = new JScrollPane(showInfo);
		
		rightPanel.add(sourcesPanel);
		rightPanel.add(scrolInfo);
		rightPanel.setDividerSize(1);
		
		
		instancesPanel.add(leftPanel);
		instancesPanel.add(rightPanel);
		instancesPanel.setDividerSize(1);
		
		//----//
		
		infoPanel.add(tablesPanel);
		infoPanel.add(instancesPanel);
		infoPanel.setDividerSize(1);
		
		//----//
		
		title.setFont(new Font("Arial", Font.BOLD, 15));
		upPanel.setPreferredSize(new Dimension(400, 399));
		
		upPanel.add(title);
		upPanel.add(infoPanel);
		upPanel.setDividerSize(2);
		
		//----//
		
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(okButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		
		pageSummary.add(upPanel);
		pageSummary.add(buttonPanel);
		pageSummary.setDividerSize(1);
	}
	
	private class SourcesListListener implements ActionListener{

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			
			String db = "";
			String result = "";
			String className = "";
			String instances = "0";
			
			if(swe.testDBexists())
				db = "test";

			showInfo.removeAll();
			
			if(!source.equals("- Select an option -")){
				
				if(!source.contains("_")){
					className = "http://" + source + "/Medicine";
					result = swe.selectAllInfo(className, db);
					instances = swe.countClassInstances(className, db);
				}
				else if(!db.contentEquals("")){
					result = swe.selectAllInfo(chosenRule, db);
					instances = swe.countClassInstances(chosenRule, db);
				}
				
				numInstances.setText("Number of instances: " + instances);
				showInfo.setText(result);
			}
		}
	}
	
	private class OkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	}
}
