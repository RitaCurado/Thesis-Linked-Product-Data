package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class SummaryPage {
	
//	private CardLayout card;
//	private JPanel contentPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private JSplitPane pageSummary;

	public SummaryPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
//		card = cl;
//		contentPanel = content;
		
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
		
		JTable initValuesTable, valuesAggsTable;
		JScrollPane initialScroll, aggScroll;
				
		JPanel topPanel = new JPanel(new GridLayout(0, 1));
		JPanel matchesPanel = new JPanel(new GridLayout(2, 1));
		JPanel finalInstPanel = new JPanel(new GridLayout(2, 1));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 7));

		JSplitPane initialInstsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane afterAggInstsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JLabel initInsts, aggsInsts, numMatches, numFinal;
		JLabel title = new JLabel("Summary");
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new OkListener());
		
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
//		--------------
		
		
		numMatches = new JLabel("  " + swe.getNumMatches());
		numMatches.setFont(new Font("Arial", Font.PLAIN, 13));
		
		matchesPanel.add(new JLabel("Number of matchings with mapping rule:"));
		matchesPanel.add(numMatches);
		
		//----//
		
		numFinal = new JLabel("   " + swe.getResultInstNum());
		numFinal.setFont(new Font("Arial", Font.PLAIN, 13));
		
		
		finalInstPanel.add(new JLabel("Final number of instances:"));
		finalInstPanel.add(numFinal);
		
		//----//

		title.setFont(new Font("Arial", Font.BOLD, 15));
		initialInstsPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 5, 0, Color.WHITE));
		afterAggInstsPanel.setBorder(BorderFactory.createMatteBorder(5, 0, 2, 0, Color.WHITE));
		
		topPanel.setPreferredSize(new Dimension(400, 395));
		topPanel.add(title);		
		topPanel.add(initialInstsPanel);
		topPanel.add(afterAggInstsPanel);
		topPanel.add(matchesPanel);
		topPanel.add(finalInstPanel);
		
		//----//
		
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(okButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		
		pageSummary.add(topPanel);
		pageSummary.add(buttonPanel);
		pageSummary.setDividerSize(1);
	}
	
	private class OkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	}
}
