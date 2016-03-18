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
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
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

	private JComboBox<String> aggregationsList, sourcesList;
	private JTextField ruleName;
	private JButton removeButton, createButton;

	private ArrayList<JCheckBox> checkBoxes;
	
	public AggregationPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		pageAggregation = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		criteriaPanel = new JPanel(new GridLayout(0, 1));
		criteriaPanel.setBackground(Color.WHITE);
		criteriaPanel.setPreferredSize(new Dimension(350, 350));

		ruleName = new JTextField("");
		removeButton = new JButton(" Remove");
		createButton = new JButton("Create ");
		checkBoxes = new ArrayList<JCheckBox>();

		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageAggregation;
	}
	
	private void createPage(){
		
		JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel createPanel = new JPanel(new GridLayout(2, 1));
		JPanel choicesPanel = new JPanel(new GridLayout(9, 1));
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		JButton cancelButton = new JButton(" Back");
		JButton nextButton = new JButton("Next ");
		
//		--- leftPanel ---
		leftPanel.setPreferredSize(new Dimension(400, 100));
		
		JLabel title = new JLabel("Data Aggregation Rules");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		
		JLabel rulesLabel = new JLabel("New Rules");
		rulesLabel.setFont(new Font("Arial", Font.BOLD, 14));
		
		ArrayList<String> aggregationRules = new ArrayList<String>();
		aggregationRules.add("- Select an option -");
		aggregationRules.addAll(swe.showAggregationRules(""));
		
		String[] filtersArray = new String[aggregationRules.size()];
		filtersArray = aggregationRules.toArray(filtersArray);		
		aggregationsList = new JComboBox<String>(filtersArray);
		aggregationsList.setSelectedIndex(0);
		aggregationsList.addActionListener(new AggregationListListener());
		
		//----//
		
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
		sourcesList.setSelectedIndex(0);
		sourcesList.addActionListener(new SourcesListListener());
		
		choicesPanel.add(title);
		choicesPanel.add(new JLabel("Existing Rules:"));
		choicesPanel.add(aggregationsList);
		choicesPanel.add(new JLabel());
		choicesPanel.add(rulesLabel);
		choicesPanel.add(new JLabel("Sources:"));
		choicesPanel.add(sourcesList);
		choicesPanel.add(new JLabel("Rule Name:"));
		choicesPanel.add(ruleName);
		
		//----//
		
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
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0,0,10,0);  //bottom padding
		buttonPanel.add(removeButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		buttonPanel.add(cancelButton, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		buttonPanel.add(nextButton, gbc);
		
		leftPanel.add(choicesPanel);
		leftPanel.add(buttonPanel);
		leftPanel.setDividerSize(1);
//		-----------------
		
//		--- rightPanel ---
		
		createButton.setForeground(Color.WHITE);
		createButton.setFont(new Font("Arial", Font.BOLD, 15));
		createButton.setBackground(new Color(005, 220, 105));
		createButton.setIcon(new ImageIcon("..\\src\\main\\resources\\checked16px.png"));
		createButton.addActionListener(new CreateListener());
		
		createPanel.add(new JLabel("Choose the properties that have to assume the same value in duplicated entries"));
		createPanel.add(createButton);
		
		rightPanel.add(criteriaPanel);
		rightPanel.add(createPanel);
		rightPanel.setDividerSize(1);
//		------------------
		
		pageAggregation.add(leftPanel);
		pageAggregation.add(rightPanel);
		pageAggregation.setDividerSize(1);
		
	}
	
	private class AggregationListListener implements ActionListener{

		JTextArea ta = new JTextArea();

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String rule = (String) cb.getSelectedItem();
			String result;
			
			if(rule.equals("- Select an option -")){
				sourcesList.setEnabled(true);
				createButton.setEnabled(true);
				removeButton.setEnabled(false);
				criteriaPanel.removeAll();
			}
			else{
				sourcesList.setEnabled(false);
				createButton.setEnabled(false);
				removeButton.setEnabled(true);
				
				result = swe.showAggregationCriteria(rule);
				result = result.replace(",", ",\n ");
				
				ta.setFont(new Font("Courier New", Font.PLAIN, 13));
				ta.setText(result);
				criteriaPanel.add(ta);
			}
			criteriaPanel.revalidate();
			criteriaPanel.repaint();
		}
	}
	
	private class SourcesListListener implements ActionListener{
		
		CheckListener cl = new CheckListener();

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			
			JCheckBox checkB;
			ArrayList<String> classes, props;
			
			if(source.equals("- Select an option -")){
				aggregationsList.setEnabled(true);
				removeButton.setEnabled(true);
				createButton.setEnabled(false);
				criteriaPanel.removeAll();
			}
			else{
				aggregationsList.setEnabled(false);
				removeButton.setEnabled(false);
				createButton.setEnabled(true);

				classes = swe.showSourceClasses(source.toLowerCase(), "beginning");
				props = swe.showClassProperties(classes.get(0).substring(2, classes.get(0).length()-2), "beginning");

				criteriaPanel.removeAll();
				criteriaPanel.revalidate();

				for(String p: props){
					p = p.replace(" ", "");

					checkB = new JCheckBox(p);
					checkB.addActionListener(cl);
					checkB.setBackground(Color.WHITE);

					criteriaPanel.add(checkB);
				}
			}
			
			criteriaPanel.revalidate();
			criteriaPanel.repaint();
		}
	}
	
	private class CheckListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();

			if(cb.isSelected()){
				checkBoxes.add(cb);
			}
			else{
				checkBoxes.remove(cb);
			}
		}
	}
	
	private class NextListener implements ActionListener{

//		private MappingPage mappingPage;
//		private JSplitPane pageRules;
		
		private ChooseAggRulePage aggRulePage;
		private JSplitPane pageAggRules;
		
		@Override
		public void actionPerformed(ActionEvent event) {
			
			aggRulePage = new ChooseAggRulePage(frame, swe, card, contentPanel);
			pageAggRules = aggRulePage.getPage();
			pageAggRules.setName("pageAggRules");
			
			contentPanel.add(pageAggRules, "pageAggRules");
			card.show(contentPanel, "pageAggRules");
			
//			mappingPage = new MappingPage(frame, swe, card, contentPanel);
//			pageRules = mappingPage.getPage();
//			pageRules.setName("pageRules");
//			
//			contentPanel.add(pageRules, "pageRules");
//			card.show(contentPanel, "pageRules");
		}
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(pageAggregation);
			contentPanel.revalidate();
			card.show(contentPanel, "page1");
		}
	}
	
	private class RemoveListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String rule = aggregationsList.getSelectedItem().toString();
			
			if(rule.equals("- Select an option -")){
				JOptionPane.showMessageDialog(frame, "You have to select a rule to be removed.",
						"Attention!", JOptionPane.WARNING_MESSAGE);
			}
			else{
				swe.deleteAggregationRule(rule);
				aggregationsList.removeItem(rule);
				aggregationsList.revalidate();
				
				aggregationsList.setSelectedIndex(0);
				
				checkBoxes.clear();
				
				JOptionPane.showMessageDialog(frame, "The rule was successfully removed.", "Information",
						JOptionPane.INFORMATION_MESSAGE);
				
				sourcesList.setSelectedIndex(0);
				aggregationsList.setEnabled(true);
				
				criteriaPanel.removeAll();
				criteriaPanel.repaint();
			}
		}
	}
	
	private class CreateListener implements ActionListener{
		

		@Override
		public void actionPerformed(ActionEvent event) {
			
			if(!checkBoxes.isEmpty()){
				
				String source = (String) sourcesList.getSelectedItem();
				String rulename = ruleName.getText();
				String criteria = "";
				
				if(!rulename.equals("")){
				
					for(JCheckBox cb: checkBoxes){
						if(!criteria.equals(""))
							criteria += ",";
						
						criteria += cb.getText();
					}
					
					swe.createAggregationRule(source, rulename, criteria);
					
					for(int i=aggregationsList.getItemCount()-1; i>0; i--){
						aggregationsList.removeItemAt(i);
					}
					
					ArrayList<String> aggregationRules = swe.showAggregationRules("");
					
					for(String agg: aggregationRules){
						aggregationsList.addItem(agg);
					}
					
					aggregationsList.setSelectedIndex(0);
					aggregationsList.revalidate();
					
					ruleName.setText("");
					
					checkBoxes.clear();
					
					JOptionPane.showMessageDialog(frame, "The rule was successfully created.", "Information",
							JOptionPane.INFORMATION_MESSAGE);
					
					sourcesList.setSelectedIndex(0);
					aggregationsList.setEnabled(true);
					
					criteriaPanel.removeAll();
					criteriaPanel.repaint();
				}
				else
					JOptionPane.showMessageDialog(frame, "You have to give a name for the new rule.",
							"Attention!", JOptionPane.WARNING_MESSAGE);
				
			}
			else {
				JOptionPane.showMessageDialog(frame, "You have to select at least a property from the up panel.",
						"Attention!", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
