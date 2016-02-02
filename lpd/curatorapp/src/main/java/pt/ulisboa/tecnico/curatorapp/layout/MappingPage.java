package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class MappingPage {

	private CardLayout card;
	private JPanel contentPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private JSplitPane pageMapping;

	private JComboBox<String> mappingsList, sourcesList;
	private JLabel sourceName;
	private JTextField ruleName, rules;
	private JButton removeButton, createButton;
	private JTextArea showInfo;

	private ArrayList<String> sources;
	private ArrayList<JCheckBox> checkBoxes;
	private String ruleNameDefault, newRuleName;
	private String ruleDefault, newRule;
	
	public MappingPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		pageMapping = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		sourceName = new JLabel(" ", SwingConstants.LEFT);
		ruleName = new JTextField("");
		rules = new JTextField("");
		showInfo = new JTextArea();
		removeButton = new JButton(" Remove");
		createButton = new JButton("Create ");
		
		sources = swe.getSources();
		checkBoxes = new ArrayList<JCheckBox>();
		ruleNameDefault = "Ex: MappingBySomeProperty";
		ruleDefault = "Ex: <www.s1.com/p1>-<www.s2.com/p3>|<www.s1.com/p4>-<www.s2.com/p1>";

		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageMapping;
	}
	
	private void createPage(){
		
		JSplitPane upPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JSplitPane showPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane choosePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		choosePanel.setPreferredSize(new Dimension(400, 100));
		
		JScrollPane checkScroll = new JScrollPane();
		JPanel titlePanel = new JPanel(new GridLayout(5, 1));
		JPanel checkPanel = new JPanel(new GridLayout(0, 1));
		checkPanel.setBackground(Color.WHITE);
		
		JPanel sourcesPanel = new JPanel(new GridLayout(2, 1));
		JScrollPane showScroll = new JScrollPane();		
		
		JPanel infoPanel = new JPanel(new GridBagLayout());
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JButton cancelButton = new JButton(" Cancel");
		JButton nextButton = new JButton("Next ");
		
//		--- upPanel ---
		
		JLabel title = new JLabel("Sources Mapping Rules");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		
		ArrayList<String> mappingRules = new ArrayList<String>();
		mappingRules.add("- Select an option -");
		mappingRules.addAll(swe.showMappingRules(""));
		
		String[] filtersArray = new String[mappingRules.size()];
		filtersArray = mappingRules.toArray(filtersArray);		
		mappingsList = new JComboBox<String>(filtersArray);
		mappingsList.setSelectedIndex(0);
		mappingsList.addActionListener(new MappingListListener());
		
		titlePanel.add(title);
		titlePanel.add(new JLabel("Existing Rules:"));
		titlePanel.add(mappingsList);
		titlePanel.add(new JLabel(""));
		titlePanel.add(new JLabel("Sources to map:"));
		
		//----//
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		CheckListener cl = new CheckListener();
		
		for(String s: sources){
			if(s.contains("+"))
				indexes.add(sources.indexOf(s));
		}
		for(int i: indexes){
			sources.remove(i);
		}
		
		for(String source: sources){
			JCheckBox cb = new JCheckBox(source);
			cb.setBackground(Color.WHITE);
			cb.addActionListener(cl);
			checkPanel.add(cb);
		}
		
		checkScroll.setViewportView(checkPanel);
		
		
		choosePanel.add(titlePanel);
		choosePanel.add(checkScroll);
		choosePanel.setDividerSize(0);
		
		//----//
		
		
		sources.add(0, "- Select an option -");
		
		String[] sourcesArray = new String[sources.size()];
		sourcesArray = sources.toArray(sourcesArray);
		sourcesList = new JComboBox<String>(sourcesArray);
		sourcesList.setSelectedIndex(0);
		sourcesList.addActionListener(new SourcesListListener());
		
		sources.remove(0);
		
		showInfo.setText("\n\n\n");
		showScroll.setViewportView(showInfo);
		
		sourcesPanel.add(new JLabel("Sources:"));
		sourcesPanel.add(sourcesList);
		
		
		showPanel.add(sourcesPanel);
		showPanel.add(showScroll);
		showPanel.setDividerSize(0);
		
		
		upPanel.add(choosePanel);
		upPanel.add(showPanel);
		upPanel.setDividerSize(1);
		upPanel.setPreferredSize(new Dimension(400, 265));
		
//		--- downPanel ---
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		infoPanel.add(new JLabel("Source Name:"), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 10;
		infoPanel.add(sourceName, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		infoPanel.add(new JLabel(" "), gbc);
		
		TextFieldListener tfl = new TextFieldListener();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		infoPanel.add(new JLabel("Rule Name:"), gbc);
		
		ruleName.setText(ruleNameDefault);
		ruleName.setForeground(Color.LIGHT_GRAY);
		ruleName.addFocusListener(tfl);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 10;
		infoPanel.add(ruleName, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1;
		infoPanel.add(new JLabel(" "), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 1;
		infoPanel.add(new JLabel("Rules:"), gbc);

		rules.setText(ruleDefault);
		rules.setForeground(Color.LIGHT_GRAY);
		rules.addFocusListener(tfl);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 10;
		infoPanel.add(rules, gbc);
		
		//-----//
		
		createButton.setForeground(Color.WHITE);
		createButton.setFont(new Font("Arial", Font.BOLD, 15));
		createButton.setBackground(new Color(005, 220, 105));
		createButton.setIcon(new ImageIcon("..\\src\\main\\resources\\checked16px.png"));
		createButton.addActionListener(new CreateListener());
		
		removeButton.setForeground(Color.WHITE);
		removeButton.setFont(new Font("Arial", Font.BOLD, 15));
		removeButton.setBackground(new Color(226, 006, 021));
		removeButton.setIcon(new ImageIcon("..\\src\\main\\resources\\delete85.png"));
		removeButton.addActionListener(new RemoveListener());
		
		nextButton.setIcon(new ImageIcon("..\\src\\main\\resources\\right arrow16px.png"));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		nextButton.addActionListener(new NextListener());
		
		cancelButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		cancelButton.addActionListener(new BackListener());
		
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(15,200,5,5);
		buttonPanel.add(removeButton, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(15,5,5,200);
		buttonPanel.add(createButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5,200,0,5);
		buttonPanel.add(cancelButton, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5,5,0,200);
		buttonPanel.add(nextButton, gbc);
		
		downPanel.add(infoPanel);
		downPanel.add(buttonPanel);
		downPanel.setDividerSize(0);
		
//		-----------------
		
		pageMapping.add(upPanel);
		pageMapping.add(downPanel);
		pageMapping.setDividerSize(1);
		
	}
	
	private class MappingListListener implements ActionListener{

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String rule = (String) cb.getSelectedItem();
			String result;
			
			if(rule.equals("- Select an option -")){
				sourcesList.setEnabled(true);
				showInfo.setText("");
			}
			else{
				sourcesList.setEnabled(false);
				result = swe.showMappingCriteria(rule);
				showInfo.setText(result);
			}
		}
	}
	
	private class SourcesListListener implements ActionListener{

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			
			String result = "";
			ArrayList<String> classes, props;
			
			if(source.equals("- Select an option -")){
				mappingsList.setEnabled(true);
				showInfo.setText("");
			}
			else{
				mappingsList.setEnabled(false);

				classes = swe.showSourceClasses(source.toLowerCase());
				props = swe.showClassProperties(classes.get(0).substring(2, classes.get(0).length()-2), "");
				
				showInfo.removeAll();

				for(String p: props){
					p = p.replace(" ", "");
					
					result += p;
					result += "\n";
				}
				
				showInfo.setText(result);
			}
		}
	}
	
	private class CheckListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			int counter = 0;
			String text;
			String cbName = "";
			String finalSource = "";
			
			sources.clear();
			sources = swe.getSources();

			JCheckBox cb = (JCheckBox) e.getSource();

			if(cb.isSelected()){
				checkBoxes.add(cb);
			}else{
				checkBoxes.remove(cb);
			}
			
			if(checkBoxes.size() == 0)
				finalSource = "";
			else{
				for(String source: sources){
					counter = 0;
					for(JCheckBox jcb: checkBoxes){
						cbName = jcb.getText().toLowerCase();
						if(source.contains(cbName))
							counter++;
					}
					if(counter == checkBoxes.size()){
						finalSource = "http://" + source;
						break;
					}
					else
						finalSource = "";
				}
				
				if(finalSource == ""){
					finalSource = "http://www.";
					
					for(JCheckBox jcb: checkBoxes){
						text = jcb.getText();
						finalSource += text.substring(4, text.length()-3);
						finalSource += "_";
					}
					finalSource = finalSource.substring(0, finalSource.length()-1);
					finalSource += ".pt";
					
				}
			}
			
			sourceName.setText(finalSource);
			
		}
	}
	
	private class TextFieldListener implements FocusListener{

		JTextField tf;

		@Override
		public void focusGained(FocusEvent e) {
			tf = (JTextField) e.getSource();

			if(tf.getText().contentEquals(ruleNameDefault)||tf.getText().contentEquals(ruleDefault)){
				tf.setText("");
				tf.setForeground(Color.BLACK);
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
			tf = (JTextField) e.getSource();

			if(tf.getText().contentEquals("")){
				tf.setForeground(Color.LIGHT_GRAY);

				if(tf.equals(ruleName))
					ruleName.setText(ruleNameDefault);

				if(tf.equals(rules))
					rules.setText(ruleDefault);
			}
			else{
				if(tf.equals(ruleName))
					newRuleName = tf.getText();

				if(tf.equals(rules))
					newRule = tf.getText();
			}
		}
	}
	
	private class NextListener implements ActionListener{
		
		private ChooseMapRulePage mapRulePage;
		private JSplitPane pageMapRules;
		
		@Override
		public void actionPerformed(ActionEvent event) {
			
			mapRulePage = new ChooseMapRulePage(frame, swe, card, contentPanel);
			pageMapRules = mapRulePage.getPage();
			pageMapRules.setName("pageAggRules");
			
			contentPanel.add(pageMapRules, "pageMapRules");
			card.show(contentPanel, "pageMapRules");
		}
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			
			contentPanel.remove(pageMapping);
			contentPanel.revalidate();
			card.show(contentPanel, "pageAggResults");
		}
	}
	
	private class RemoveListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String rule = mappingsList.getSelectedItem().toString();
			
			if(rule.equals("- Select an option -")){
				JOptionPane.showMessageDialog(frame, "You have to select a rule to be removed.",
						"Attention!", JOptionPane.WARNING_MESSAGE);
			}
			else{
				swe.deleteMappingRule(rule);
				mappingsList.removeItem(rule);
				mappingsList.revalidate();
				
				mappingsList.setSelectedIndex(0);
				
				checkBoxes.clear();
				
				JOptionPane.showMessageDialog(frame, "The rule was successfully removed.", "Information",
						JOptionPane.INFORMATION_MESSAGE);
				
				sourcesList.setSelectedIndex(0);
				mappingsList.setEnabled(true);
				
				showInfo.removeAll();
			}
		}
	}
	
	private class CreateListener implements ActionListener{
		

		@Override
		public void actionPerformed(ActionEvent event) {
			
			int sid = 0;
			String clName;
			String source = "";
			String[] splitDot;
			HashMap<String, String> subjectBySource = new HashMap<String, String>();
			HashMap<String, ArrayList<String>> propsBySource = new HashMap<String, ArrayList<String>>();
			HashMap<String, ArrayList<String>> nodesBySource = new HashMap<String, ArrayList<String>>();
			
			if(ruleName.getText().contentEquals(ruleNameDefault) || rules.getText().contentEquals(ruleDefault)){
				JOptionPane.showMessageDialog(frame, 
						"The \"Class Name\" and \"Rules\" fields should not be empty.", "Attention!", JOptionPane.WARNING_MESSAGE);
			}
			if(checkBoxes.isEmpty()){
				JOptionPane.showMessageDialog(frame, 
						"Please use the checkboxes to select the sources which are being mapped.", "Attention!", JOptionPane.WARNING_MESSAGE);
			}
			else{
				//get all source properties
				for(JCheckBox cb: checkBoxes){
					sid++;
					
					splitDot = cb.getText().split("\\.");
					source = splitDot[1];
					
					subjectBySource.put(source, "s" + sid);
					clName = "http://www." + source + ".pt/Medicine";

					try {
						propsBySource.put(source, swe.showClassProperties(clName, ""));
						nodesBySource.put(source, swe.showNodeProperties(clName));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
				HashMap<String, String> queryParts = swe.mappingConstructQuery(subjectBySource, propsBySource,
						nodesBySource, sourceName.getText(), newRuleName, newRule.split("\\|"));
				
				swe.createMappingRule(sourceName.getText(), ruleName.getText(), queryParts);
				
				for(int i=mappingsList.getItemCount()-1; i>0; i--){
					mappingsList.removeItemAt(i);
				}
				
				ArrayList<String> mappingRules = swe.showMappingRules("");
				
				for(String agg: mappingRules){
					mappingsList.addItem(agg);
				}
				
				JOptionPane.showMessageDialog(frame, "The rule was successfully created.", "Information",
						JOptionPane.INFORMATION_MESSAGE);
				
				
				mappingsList.setSelectedIndex(0);
				mappingsList.revalidate();
				
				sourcesList.setSelectedIndex(0);
				mappingsList.setEnabled(true);
				
				for(JCheckBox cb: checkBoxes){
					cb.setSelected(false);
				}
				checkBoxes.clear();
				
				sourceName.setText("");
				
				ruleName.setForeground(Color.LIGHT_GRAY);
				ruleName.setText(ruleNameDefault);
				
				rules.setForeground(Color.LIGHT_GRAY);
				rules.setText(ruleDefault);
				
				showInfo.setText("");
			}
		}
	}
	
}