package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
	
	private JSplitPane pageChoose;
	private String chosenRule;
	
	private JLabel numInstances;
	private JLabel numMatches;
	private JTextArea result;
	private ButtonGroup radioGroup;

	public ChooseMapRulePage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		chosenRule = "";
		
		pageChoose = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		numInstances = new JLabel("Resulting number of instances: ");
		numMatches = new JLabel("Number of mapped instances: ");
		
		radioGroup = new ButtonGroup();
		result = new JTextArea();
		result.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageChoose;
	}
	
	private void createPage(){
		
		JLabel title = new JLabel("Choose Mapping Rule:");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		
		JScrollPane scrollOptions = new JScrollPane();
		JScrollPane scrollResult = new JScrollPane();
		JPanel upPanel = new JPanel(new GridLayout(0, 1));
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 6));
		
		JPanel numInstLabel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JSplitPane choosePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane results = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JButton okButton = new JButton(" OK");
		JButton labelButton = new JButton("Label");
		JButton backButton = new JButton(" Back");
		JButton finishButton = new JButton("Finish ");
		
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
		
		scrollOptions.setPreferredSize(new Dimension(730, 130));
		scrollOptions.setViewportView(radioPanel);
		
		okButton.setForeground(Color.WHITE);
		okButton.setFont(new Font("Arial", Font.BOLD, 15));
		okButton.setBackground(new Color(005, 220, 105));
		okButton.setIcon(new ImageIcon("..\\src\\main\\resources\\checked16px.png"));
		okButton.addActionListener(new OkListener());
		
		choosePanel.add(scrollOptions);
		choosePanel.add(okButton);
		choosePanel.setDividerSize(1);
		
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		labelButton.addActionListener(new LabelListener());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 3;
		numInstLabel.add(numInstances, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 3;
		numInstLabel.add(numMatches, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0.8;
		numInstLabel.add(labelButton, gbc);
		
		upPanel.setPreferredSize(new Dimension(400, 150));
		upPanel.add(title);
		upPanel.add(choosePanel);
		upPanel.add(numInstLabel);
		
		//----//
		
		scrollResult.setPreferredSize(new Dimension(400, 250));
		scrollResult.setViewportView(result);
		
		results.add(upPanel);
		results.add(scrollResult);
		results.setDividerSize(0);
		
		//----//
		
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new BackListener());
		
		finishButton.addActionListener(new FinishListener());
		finishButton.setForeground(Color.WHITE);
		finishButton.setFont(new Font("Arial", Font.BOLD, 15));
		finishButton.setBackground(new Color(030, 144, 255));
		finishButton.setIcon(new ImageIcon("..\\src\\main\\resources\\whiteflag.png"));
		finishButton.setVerticalTextPosition(SwingConstants.CENTER);
		finishButton.setHorizontalTextPosition(SwingConstants.LEFT);
		
		buttonPanel.add(backButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(finishButton);
		
		pageChoose.add(results);
		pageChoose.add(buttonPanel);
		pageChoose.setDividerSize(0);
	}
	
	private class OkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			int instances;
			ArrayList<String> mappingResult;
			
			String rb = radioGroup.getSelection().getActionCommand();
			rb = rb.substring(1, rb.length()-1);
			
			chosenRule = rb;
			instances = swe.chooseMappingRule(rb);
			
			mappingResult = swe.queryTestMapping(rb);
			
			result.setText(mappingResult.get(0));
			numInstances.setText("Resulting number of instances: " + mappingResult.get(1));
			numMatches.setText("Number of mapped instances: " + instances);
			
			swe.setNumMappings(instances);
		}
	}
	
	private class LabelListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String label;
			HashMap<String, Integer> sid = swe.getSourcesId();
			
			label = "Column number - property source \n\n";
			
			for(String key: sid.keySet()){
				label += sid.get(key) + " -  " + key + "\n";
			}
			
			JOptionPane.showMessageDialog(frame, label);
			
		}
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			contentPanel.remove(pageChoose);
			contentPanel.revalidate();
			card.show(contentPanel, "pageRules");
		}
	}
	
	private class FinishListener implements ActionListener{

		private SummaryPage summPage;
		private JSplitPane pageSumm;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			summPage = new SummaryPage(frame, swe, card, contentPanel, chosenRule);
			pageSumm = summPage.getPage();
			pageSumm.setName("pageSumm");
			
			contentPanel.add(pageSumm, "pageSumm");
			card.show(contentPanel, "pageSumm");
		}
	}

}
