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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class ChooseAggRulePage {

	private CardLayout card;
	private JPanel contentPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private JSplitPane pageChoose;
	
	private JComboBox<String> sourcesList;
	
	private JPanel radioPanel;
	private JList<String> chosenRulesList;
	private DefaultListModel<String> rulesModel;
	
	public ChooseAggRulePage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		pageChoose = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		radioPanel = new JPanel(new GridLayout(0, 1));
		rulesModel = new DefaultListModel<String>();
		chosenRulesList = new JList<String>(rulesModel);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageChoose;
	}
	
	private void createPage(){
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JSplitPane choicesPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel titlePanel = new JPanel(new GridLayout(5, 1));
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		
		JButton cancelButton = new JButton(" Cancel");
		JButton okButton = new JButton("    Ok  ");
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		// ---- //
		
		JLabel title = new JLabel("Choose Aggregation Rule");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		
		JLabel subtitle = new JLabel("Choose one rule per source");
		subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
		
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
		
		titlePanel.add(title);
		titlePanel.add(subtitle);
		titlePanel.add(new JLabel("Source:"));
		titlePanel.add(sourcesList);
		titlePanel.add(new JLabel());
		
		// ---- //
		
		choicesPanel.setPreferredSize(new Dimension(400, 270));
		radioPanel.setPreferredSize(new Dimension(430, 240));
		radioPanel.setBackground(Color.WHITE);
		
		JScrollPane radioScrollPane = new JScrollPane(radioPanel);
		leftPanel.add(new JLabel("Rules:"));
		leftPanel.add(radioScrollPane);
		leftPanel.setDividerSize(0);
		
		JScrollPane listScrollPane = new JScrollPane(chosenRulesList);
		rightPanel.add(new JLabel("Chosen Rules:"));
		rightPanel.add(listScrollPane);
		rightPanel.setDividerSize(0);
		
		choicesPanel.add(leftPanel);
		choicesPanel.add(rightPanel);
		
		// ---- //
		
		okButton.setForeground(Color.WHITE);
		okButton.setFont(new Font("Arial", Font.BOLD, 15));
		okButton.setBackground(new Color(005, 220, 105));
		okButton.setIcon(new ImageIcon("..\\src\\main\\resources\\checked16px.png"));
		okButton.setVerticalTextPosition(SwingConstants.CENTER);
		okButton.setHorizontalTextPosition(SwingConstants.LEFT);
		okButton.addActionListener(new OkListener());
		
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(226, 006, 021));
		cancelButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		cancelButton.addActionListener(new CancelListener());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,0,5);  //right padding
		buttonPanel.add(cancelButton, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,5,0,0);  //left padding
		buttonPanel.add(okButton, gbc);
		
		// ---- //
		
		upPanel.add(titlePanel);
		upPanel.add(choicesPanel);
		upPanel.setDividerSize(0);
		
		pageChoose.add(upPanel);
		pageChoose.add(buttonPanel);
		pageChoose.setDividerSize(1);
	}
	
	private class SourcesListListener implements ActionListener{
		
		RadioListener rl = new RadioListener();

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			
			JRadioButton rb;
			ButtonGroup buttonGroup = new ButtonGroup();
			ArrayList<String> aggRules = swe.showAggregationRules(source);
			aggRules.add("None");
			
			radioPanel.removeAll();
			
			for(String agg: aggRules){
				rb = new JRadioButton(agg);
				rb.setBackground(Color.WHITE);
				rb.addItemListener(rl);
				buttonGroup.add(rb);
				radioPanel.add(rb);
			}
			
			radioPanel.revalidate();
			radioPanel.repaint();
		}
	}
	
	private class RadioListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent e) {
			JRadioButton rb = (JRadioButton) e.getSource();
			String name = rb.getText();
			
			if(name.equals("None")){
				name = "<http://" + sourcesList.getSelectedItem().toString() + "/None>";
			}
			
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if(!rulesModel.contains(name))
					rulesModel.addElement(name);
		    }
		    else if (e.getStateChange() == ItemEvent.DESELECTED) {
		    	rulesModel.removeElement(name);
		    }
			
			chosenRulesList.revalidate();
			chosenRulesList.repaint();
		}
	}
	
	private class CancelListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(pageChoose);
			contentPanel.revalidate();
			card.show(contentPanel, "pageAggregation");
		}
	}
	
	private class OkListener implements ActionListener{

		private MappingPage mappingPage;
		private JSplitPane pageRules;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(rulesModel.isEmpty()){
				JOptionPane.showMessageDialog(frame, "You have to select an aggregation rule per source.",
						"Attention!", JOptionPane.WARNING_MESSAGE);
			}
			else{
				
				Enumeration<String> enumerate = rulesModel.elements();
				HashMap<String, String> rulesBySource = new HashMap<String, String>();
				String rule, source;
				
				while(enumerate.hasMoreElements()){
					rule = enumerate.nextElement();
					rule = rule.replace(" ", "");
					
					source = swe.getPropertySource(rule);
					rulesBySource.put(source, rule);
				}
				
				swe.filterData(rulesBySource);
				
				mappingPage = new MappingPage(frame, swe, card, contentPanel);
				pageRules = mappingPage.getPage();
				pageRules.setName("pageRules");
				
				contentPanel.add(pageRules, "pageRules");
				card.show(contentPanel, "pageRules");
			}
			
		}
	}
	
}
