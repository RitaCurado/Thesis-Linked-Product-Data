package pt.ulisboa.tecnico.curatorapp.layout;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	
	private JLabel numInstances;
	private JTextArea result;
	private ButtonGroup radioGroup;

	public ChooseMapRulePage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		pageChoose = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		numInstances = new JLabel("Number of mapped instances: ");
		result = new JTextArea();
		radioGroup = new ButtonGroup();
		
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
		
		JSplitPane choosePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane results = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JButton okButton = new JButton(" OK");
		JButton backButton = new JButton(" Back");
		JButton finishButton = new JButton("Finish ");
		
		ArrayList<String> mappingRules = swe.showMappingRules("");
		radioPanel.setBackground(Color.WHITE);
		
		for(String rule: mappingRules){
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
		
		upPanel.setPreferredSize(new Dimension(400, 150));
		upPanel.add(title);
		upPanel.add(choosePanel);
		upPanel.add(numInstances);
		
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
			
			ArrayList<String> mappingResult;
			
			String rb = radioGroup.getSelection().getActionCommand();
			rb = rb.substring(2, rb.length()-2);
			
			swe.chooseMappingRule(rb, "test");
			
			mappingResult = swe.queryTestMapping(rb);
			
			result.setText(mappingResult.get(0));
			numInstances.setText("Number of mapped instances: " + mappingResult.get(1));	
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

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	}

}
