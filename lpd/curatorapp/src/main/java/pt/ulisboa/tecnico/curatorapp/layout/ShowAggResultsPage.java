package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class ShowAggResultsPage {
	
	private SemanticWebEngine swe;
	private CardLayout card;
	private JFrame frame;
	private JPanel contentPanel;
	private JSplitPane pageAggResult;
	
	private JLabel numInstances;
	private JTextArea output;
	private JComboBox<String> sourcesList;

	public ShowAggResultsPage(SemanticWebEngine swe, JFrame gui, CardLayout cl, JPanel content){
		
		this.swe = swe;
		card = cl;
		frame = gui;
		contentPanel = content;
		
		pageAggResult = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		numInstances = new JLabel("Number of instances: ");
		output = new JTextArea();
		output.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageAggResult;
	}
	
	private void createPage(){
		
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel titlePanel = new JPanel(new GridLayout(3, 1));
		JPanel sourcesPanel = new JPanel(new GridLayout(1, 4));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 6));
		
		JPanel numInstLabel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JScrollPane scrollPane = new JScrollPane();
		
		JLabel resultTitle = new JLabel("Filtering results");
		JLabel sourceTitle = new JLabel("Sources: ");
		JButton labelButton = new JButton("Label");
		JButton nextButton = new JButton("Next ");
		JButton backButton = new JButton(" Back");
		
		ArrayList<String> sources = new ArrayList<String>();
		String[] sourcesArray = new String[sources.size()];
		
		resultTitle.setFont(new Font("Arial", Font.BOLD, 15));
		
		sources.add("- Select an option -");
		sources.addAll(swe.getSources());
		
		sourcesArray = sources.toArray(sourcesArray);
		sourcesList = new JComboBox<String>(sourcesArray);
		sourcesList.setSelectedIndex(0);
		sourcesList.addActionListener(new SourcesListListener());
		
		sourcesPanel.add(sourceTitle);
		sourcesPanel.add(sourcesList);
		sourcesPanel.add(new JLabel(""));
		sourcesPanel.add(new JLabel(""));
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		labelButton.addActionListener(new LabelListener());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 3;
		numInstLabel.add(numInstances, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		numInstLabel.add(labelButton, gbc);
		
		titlePanel.add(resultTitle);
		titlePanel.add(sourcesPanel);
		titlePanel.add(numInstLabel);
		
		//-----//
		
		scrollPane.setPreferredSize(new Dimension(400, 325));
		scrollPane.setViewportView(output);
		
		upPanel.add(titlePanel);
		upPanel.add(scrollPane);
		upPanel.setDividerSize(1);
		
		//----//
		
		nextButton.setIcon(new ImageIcon("..\\src\\main\\resources\\right arrow16px.png"));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		nextButton.addActionListener(new NextListener());
		
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new BackListener());
		
		buttonPanel.add(backButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(nextButton);
		
		pageAggResult.add(upPanel);
		pageAggResult.add(buttonPanel);
		pageAggResult.setDividerSize(1);
	}
	
	private class SourcesListListener implements ActionListener{

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			String className = "http://" + source + "/Medicine";
			
			String numInst = swe.countClassInstances(className, "");
			String result = swe.selectAllInfo(className, "");
			
			numInstances.setText("Number of instances: " + numInst);
			output.setText(result);
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
	
	private class NextListener implements ActionListener{

		private MappingPage mappingPage;
		private JSplitPane pageRules;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			mappingPage = new MappingPage(frame, swe, card, contentPanel);
			pageRules = mappingPage.getPage();
			pageRules.setName("pageRules");
			
			contentPanel.add(pageRules, "pageRules");
			card.show(contentPanel, "pageRules");
		}
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			
			swe.resetDB();
			
			contentPanel.remove(pageAggResult);
			contentPanel.revalidate();
			card.show(contentPanel, "pageAggRules");
		}
	}
}
