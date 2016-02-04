package pt.ulisboa.tecnico.userapp.layout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class InfoPage {
	
	private JSplitPane pageInfo;
	
	private CardLayout card;
	private JFrame frame;
	private JPanel contentPanel;
	private SemanticWebEngine swe;
	
	private JTextArea mapCriteria;

	public InfoPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		card = cl;
		frame = gui;
		contentPanel = content;
		
		mapCriteria = new JTextArea();
		mapCriteria.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		pageInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageInfo;
	}
	
	public void createPage(){
		
		JLabel title = new JLabel("Information Page");
		
		JLabel filterTitle = new JLabel("The database was previously filtered by an expert user who"
										+ "chose the following filtering rule:");
		
		JLabel mappingTitle = new JLabel("The expert user also created the following mapping rules that"
										+ "can be used ahead by you to map the different data sources:");
		
		JPanel titlePanel = new JPanel(new GridLayout(2, 1));
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 6));
		
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane mappingPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane middlePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane infoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JScrollPane filterScroll = new JScrollPane();
		JScrollPane mapsScroll = new JScrollPane();
		JScrollPane criteriaScroll = new JScrollPane();
		
		JTextArea filterArea = new JTextArea();
		JButton nextButton = new JButton("Next ");
		
		ArrayList<String> mappingRules;
		RadioListener rl = new RadioListener();
		String chosenRules, text = "", queryCrit, criteria;
		String[] splitBySource, splitEach, spltCrit;
		
		//----//
		
		title.setFont(new Font("Arial", Font.BOLD, 15));
		filterTitle.setFont(new Font("Arial", Font.BOLD, 13));
		mappingTitle.setFont(new Font("Arial", Font.BOLD, 13));
		
		filterArea.setFont(new Font("Courier New", Font.PLAIN, 13));
		filterTitle.setBorder(new EmptyBorder(10,0,0,0));
		mappingTitle.setBorder(new EmptyBorder(10,0,0,0));
		
		titlePanel.add(title);
		titlePanel.add(filterTitle);
		
		chosenRules = swe.showChosenRules();
		chosenRules = chosenRules.substring(3, chosenRules.length()-3);
		
		splitBySource = chosenRules.split("\\|");
		
		for(int i=0; i < splitBySource.length; i++){
			
			splitEach = splitBySource[i].split("-");
			queryCrit = swe.showAggregationCriteria(splitEach[1]);
			
			spltCrit = queryCrit.split("\\r?\\n");
			criteria = spltCrit[3];
			
			text += "Source:    " + splitEach[0] + "\n";
			text += "Rule:      " + splitEach[1] + "\n";
			text += "Criteria:  " + criteria + "\n";
			text += "\n" + "--------------" + "\n";
		}
		
		filterArea.setText(text);
		
		filterScroll.setPreferredSize(new Dimension(400, 100));
		filterScroll.setViewportView(filterArea);
		
		upPanel.add(titlePanel);
		upPanel.add(filterScroll);
		upPanel.setDividerSize(1);
		
		//----//
		
		mappingRules = swe.showMappingRules("");
		radioPanel.setBackground(Color.WHITE);
		radioPanel.setPreferredSize(new Dimension(430, 200));
		
		for(String rule: mappingRules){
			JRadioButton rb = new JRadioButton(rule);
			rb.setBackground(Color.WHITE);
			rb.addActionListener(rl);
			
			radioPanel.add(rb);
		}
		
		mapsScroll.setViewportView(radioPanel);
		
		criteriaScroll.setViewportView(mapCriteria);
		
		leftPanel.add(new JLabel("Rules:"));
		leftPanel.add(mapsScroll);
		leftPanel.setDividerSize(0);
		
		rightPanel.add(new JLabel("Map criteria:"));
		rightPanel.add(criteriaScroll);
		rightPanel.setDividerSize(0);
		
		mappingPanel.add(leftPanel);
		mappingPanel.add(rightPanel);
		
		middlePanel.add(mappingTitle);
		middlePanel.add(mappingPanel);
		middlePanel.setDividerSize(0);
		
		//----//
		
		infoPanel.add(upPanel);
		infoPanel.add(middlePanel);
		infoPanel.setDividerSize(0);
		
		nextButton.setIcon(new ImageIcon("..\\src\\main\\resources\\right arrow16px.png"));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		nextButton.addActionListener(new NextListener());
		
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(nextButton);
		
		pageInfo.add(infoPanel);
		pageInfo.add(buttonPanel);
		pageInfo.setDividerSize(1);
	}
	
	private class RadioListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton rb = (JRadioButton) e.getSource();
			String rule = rb.getText();
			String criteria;
			
			rule = rule.substring(2, rule.length()-2);
			criteria = swe.showMappingCriteria(rule);
			
			mapCriteria.setText(criteria);
		}
	}
	
	private class NextListener implements ActionListener{
		
		private JSplitPane page1;
		private FirstPage firstPage;

		@Override
		public void actionPerformed(ActionEvent e) {
			
			firstPage = new FirstPage(frame, swe, card, contentPanel);
			page1 = firstPage.getPage();
			page1.setName("page1");
			
			contentPanel.add(page1, "page1");
			card.show(contentPanel, "page1");
		}
	}
}
