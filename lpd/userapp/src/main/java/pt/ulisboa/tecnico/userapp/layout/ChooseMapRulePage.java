package pt.ulisboa.tecnico.userapp.layout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class ChooseMapRulePage {
	private CardLayout card;
	private JPanel contentPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private String chosenRule;
	private ButtonGroup radioGroup;
	
	private JSplitPane pageChoose;
	private JLabel numInstances;
	private JTextArea result;
	private JComboBox<String> sourcesList;

	public ChooseMapRulePage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		chosenRule = "";
		
		pageChoose = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		sourcesList = new JComboBox<String>();
		
		numInstances = new JLabel("Number of instances: ");
		
		radioGroup = new ButtonGroup();
		result = new JTextArea();
		result.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageChoose;
	}
	
	private void createPage(){
		
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		JPanel sourcesPanel = new JPanel(new GridLayout(2, 3));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 6));
		
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane infoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane choosePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane instancesPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JScrollPane scrollRadio, scrollResult;
		
		JButton okButton = new JButton("OK");
		JButton backButton = new JButton(" Back");
		JButton nextButton = new JButton("Next ");
		
		
		JLabel sourcesLabel = new JLabel("Sources:");
		JLabel title = new JLabel("Choose Mapping Rule");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		
		//----//
		
		ArrayList<String> mappingRules = swe.showMappingRules("");
		radioPanel.setBackground(Color.WHITE);
		
		for(String rule: mappingRules){
			rule = rule.replace(" ", "");
			JRadioButton rb = new JRadioButton(rule);
			rb.setActionCommand(rule);
			rb.setBackground(Color.WHITE);
			radioGroup.add(rb);
			radioPanel.add(rb);
		}
		
		scrollRadio = new JScrollPane(radioPanel);
		scrollRadio.setPreferredSize(new Dimension(700, 50));
		
		okButton.setForeground(Color.WHITE);
		okButton.setFont(new Font("Arial", Font.BOLD, 15));
		okButton.setBackground(new Color(005, 220, 105));
		okButton.setIcon(new ImageIcon("..\\src\\main\\resources\\checked16px.png"));
		okButton.addActionListener(new OkListener());
		
		choosePanel.add(scrollRadio);
		choosePanel.add(okButton);
		choosePanel.setDividerSize(1);
		
		//----//
		
		sourcesList.addItem("- Select an option -");
		for(String src: swe.getSources()){
			sourcesList.addItem(src);
		}
		sourcesList.addActionListener(new SourcesListListener());
		sourcesList.setSelectedIndex(0);
		
		sourcesPanel.add(sourcesLabel);
		sourcesPanel.add(sourcesList);
		sourcesPanel.add(new JLabel(""));
		sourcesPanel.add(numInstances);
		sourcesPanel.add(new JLabel(""));
		sourcesPanel.add(new JLabel(""));
		
		scrollResult = new JScrollPane(result);
		
		instancesPanel.add(sourcesPanel);
		instancesPanel.add(scrollResult);
		instancesPanel.setDividerSize(1);
		
		//----//
		
		infoPanel.add(choosePanel);
		infoPanel.add(instancesPanel);
		infoPanel.setDividerSize(2);
		
		//----//
		
		upPanel.setPreferredSize(new Dimension(400, 400));
		upPanel.add(title);
		upPanel.add(infoPanel);
		upPanel.setDividerSize(0);
		
		backButton.setBackground(new Color(198, 218, 230));
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new BackListener());
		
		nextButton.setBackground(new Color(198, 218, 230));
		nextButton.setIcon(new ImageIcon("..\\src\\main\\resources\\right arrow16px.png"));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		nextButton.addActionListener(new NextListener());
		
		buttonPanel.add(backButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(nextButton);
		
		pageChoose.add(upPanel);
		pageChoose.add(buttonPanel);
		pageChoose.setDividerSize(0);
		
	}
	
	private class OkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			String rb = radioGroup.getSelection().getActionCommand();
			rb = rb.substring(1, rb.length()-1);
			
			chosenRule = rb;
			swe.chooseMappingRule(rb);
			
			sourcesList.addItem(swe.getPropertySource(chosenRule, false));
			sourcesList.repaint();
			
			JOptionPane.showMessageDialog(frame, "The rule '" + chosenRule + "' has been chosen",
					"Information", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			contentPanel.remove(pageChoose);
			contentPanel.revalidate();
			card.show(contentPanel, "page1");
		}
	}
	
	private class NextListener implements ActionListener{

		private SecondPage secondPage;
		private JSplitPane page2;
		
		@Override
		public void actionPerformed(ActionEvent event) {

			secondPage = new SecondPage(frame, swe, card, contentPanel, chosenRule);
			page2 = secondPage.getPage();
			page2.setName("page2");

			contentPanel.add(page2, "page2");
			card.show(contentPanel, "page2");
		}
	}
	
	private class SourcesListListener implements ActionListener{

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			
			String output = "";
			String className = "";
			String instances, flowtime;
			
			if(chosenRule.contentEquals(""))
				flowtime = "allNewSet";
			else
				flowtime = "oneNewSet";

			result.removeAll();
			
			if(!source.equals("- Select an option -")){
				
				if(!source.contains("_")){
					className = "http://" + source + "/Medicine";
					output = swe.selectAllInfo(className, flowtime);
					instances = swe.countClassInstances(className, flowtime);
				}
				else {
					output = swe.selectAllInfo(chosenRule, flowtime);
					instances = swe.countClassInstances(chosenRule, flowtime);
				}
				
				numInstances.setText("Number of instances: " + instances);
				result.setText(output);
			}
		}
	}
}
