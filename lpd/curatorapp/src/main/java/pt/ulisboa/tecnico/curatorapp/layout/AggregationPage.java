package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class AggregationPage {

	private CardLayout card;
	private JPanel contentPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private JSplitPane pageAggregation;
	private JPanel criteriaPanel;

	private JComboBox<String> aggregationsList;
	private JComboBox<String> sourcesList;
	private JTextField ruleName;

	private ArrayList<JCheckBox> checkBoxes;
	
	public AggregationPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		pageAggregation = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		criteriaPanel = new JPanel(new GridLayout(0, 1));
		criteriaPanel.setBackground(Color.WHITE);
		criteriaPanel.setPreferredSize(new Dimension(450, 85));

		ruleName = new JTextField("...");
		checkBoxes = new ArrayList<JCheckBox>();

		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageAggregation;
	}
	
	private void createPage(){
		
		JPanel titlePanel = new JPanel(new GridLayout(3, 1));
		JPanel buttonPanel = new JPanel(new GridLayout(4, 2));
		JPanel createRules = new JPanel(new GridLayout(2, 1));
		
		JPanel aggregationPanel = new JPanel(new GridBagLayout());
		JPanel newRulePanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JSplitPane existentRules = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane up = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane down = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		JButton nextButton = new JButton("Next ");
		JButton cancelButton = new JButton(" Cancel");
		JButton createButton = new JButton("Create ");
		
//		--- Dropdowns init ---
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		ArrayList<String> sources = new ArrayList<String>();
		sources.add("- Select an option -");
		sources.addAll(swe.getSources());

		for(String s: sources){
			if(s.contains("+"))
				indexes.add(sources.indexOf(s));
		}
		for(int i: indexes){
			sources.remove(i);
		}
		
		String[] sourcesArray = new String[sources.size()];
		sourcesArray = sources.toArray(sourcesArray);
		sourcesList = new JComboBox<String>(sourcesArray);
		
		ArrayList<String> aggregationRules = new ArrayList<String>();
		aggregationRules.add("- Select an option -");
		aggregationRules.addAll(swe.showAggregationRules());
		
		String[] filtersArray = new String[aggregationRules.size()];
		filtersArray = aggregationRules.toArray(filtersArray);		
		aggregationsList = new JComboBox<String>(filtersArray);
		
//		----------------------
		
//		--- AggregationPanel ---
		JLabel title = new JLabel("Data Filtering");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		
		titlePanel.add(title);
		titlePanel.add(new JLabel());
		titlePanel.add(new JLabel("Aggregation Rules:"));
		
		nextButton.setIcon(new ImageIcon("..\\src\\main\\resources\\right arrow16px.png"));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
	    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		//nextButton.addActionListener(new nextListener());
	    
	    gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		aggregationPanel.add(aggregationsList, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.9;
		aggregationPanel.add(new JLabel(" "), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0.6;
		aggregationPanel.add(nextButton, gbc);
		
		existentRules.add(titlePanel);
		existentRules.add(aggregationPanel);
		existentRules.setDividerSize(0);
//		------------------------
		
//		--- NewRulePanel ---
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		newRulePanel.add(new JLabel("Sources:"), gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		newRulePanel.add(new JLabel(" "), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		newRulePanel.add(new JLabel("Rule Name:"), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		newRulePanel.add(sourcesList, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		newRulePanel.add(new JLabel(" "), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 2.5;
		newRulePanel.add(ruleName, gbc);
		
		up.add(existentRules);
		up.add(newRulePanel);
		up.setDividerSize(0);
//		--------------------
		
//		--- CreateRulesPanel ---
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
		cancelButton.setBackground(new Color(226, 006, 021));
		cancelButton.setIcon(new ImageIcon("..\\src\\main\\resources\\delete85.png"));
		
		createButton.setForeground(Color.WHITE);
		createButton.setFont(new Font("Arial", Font.BOLD, 15));
		createButton.setBackground(new Color(005, 220, 105));
		createButton.setIcon(new ImageIcon("..\\src\\main\\resources\\checked16px.png"));
		
		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		buttonPanel.add(cancelButton);
		buttonPanel.add(createButton);
		
		createRules.add(new JLabel("  Choose the properties that have to be identical in both entries"));
		createRules.add(buttonPanel);
		
		down.add(criteriaPanel);
		down.add(createRules);
		down.setDividerSize(1);
//		------------------------
		
		pageAggregation.add(up);
		pageAggregation.add(down);
		pageAggregation.setDividerSize(1);
	}
}
