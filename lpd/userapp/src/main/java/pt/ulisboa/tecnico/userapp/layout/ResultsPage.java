package pt.ulisboa.tecnico.userapp.layout;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class ResultsPage {

	private SemanticWebEngine swe;
	private JFrame frame;
	private JSplitPane resultPage;
	private CardLayout card;
	private JPanel contentPanel;
	private String queryResult, rule;
	
	public ResultsPage(SemanticWebEngine swe, JFrame gui, CardLayout cl, JPanel content, String result, String rule){
		
		this.swe = swe;
		this.rule = rule;
		frame = gui;
		card = cl;
		contentPanel = content;
		queryResult = result;
		resultPage = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return resultPage;
	}
	
	private void createPage(){
		
		String[] numValues;
		JLabel numMatches;
		JTextArea results = new JTextArea();
		JScrollPane scrollPane = new JScrollPane();
		JPanel infoPanel = new JPanel(new GridLayout(1, 3));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 5));
		JSplitPane upPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JButton labelButton = new JButton("Label");
		JButton backButton = new JButton(" Back");
		JButton homepage = new JButton(" Homepage");
		JButton finishButton = new JButton("Finish ");
		
		results.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		numValues = queryResult.split("\\r?\\n");
		numMatches = new JLabel("Number of matches: " + (numValues.length - 4));
		
		labelButton.setBackground(new Color(198, 218, 230));
		labelButton.addActionListener(new LabelListener());
		
		infoPanel.add(numMatches);
		infoPanel.add(new JLabel());
		infoPanel.add(labelButton);
		
		homepage.setForeground(Color.WHITE);
		homepage.setFont(new Font("Arial", Font.BOLD, 15));
		homepage.setBackground(new Color(0, 0, 153));
		homepage.setIcon(new ImageIcon("..\\src\\main\\resources\\home16px.png"));
		homepage.addActionListener(new homeListener());
		
		backButton.setBackground(new Color(198, 218, 230));
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new backListener());
		
		finishButton.addActionListener(new FinishListener());
		finishButton.setForeground(Color.WHITE);
		finishButton.setFont(new Font("Arial", Font.BOLD, 15));
		finishButton.setBackground(new Color(030, 144, 255));
		finishButton.setIcon(new ImageIcon("..\\src\\main\\resources\\whiteflag.png"));
		finishButton.setVerticalTextPosition(SwingConstants.CENTER);
		finishButton.setHorizontalTextPosition(SwingConstants.LEFT);
		
		buttonPanel.add(backButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(homepage);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(finishButton);
		
		results.setText(queryResult);
		scrollPane.setViewportView(results);
		scrollPane.setPreferredSize(new Dimension(400, 370));
		
		upPane.add(infoPanel);
		upPane.add(scrollPane);
		upPane.setDividerSize(1);
		
		resultPage.add(upPane);
		resultPage.add(buttonPanel);
		resultPage.setDividerSize(1);
	}
	
	private class LabelListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String label;
			HashMap<String, Integer> sid = swe.getSourcesId();
			
			label = "Column number - property source \n\n";
			
			if(!rule.contentEquals(""))
				label += "0 -  " + swe.getPropertySource(rule, false) + "\n";
			
			for(String key: sid.keySet()){
				label += sid.get(key) + " -  " + key + "\n";
			}
			
			JOptionPane.showMessageDialog(frame, label);
			
		}
	}
	
	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			contentPanel.remove(resultPage);
			contentPanel.revalidate();
			card.show(contentPanel, "page2");			
		}
	}
	
	private class homeListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Component[] allComps = contentPanel.getComponents();
			
			for(int i=1; i < allComps.length; i++){
				contentPanel.remove(allComps[i]);
			}
			
			contentPanel.revalidate();			
			card.show(contentPanel, "page1");			
		}
	}
	
	private class FinishListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	}
}
