package layout;

import javax.swing.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import lpd.SemanticWebEngine;

public class SearchPage {
	
	private SemanticWebEngine swe;
	private JSplitPane pageResult;	
	private ResultsPage resultPage;
	
	private JSplitPane searchPage;
	private CardLayout card;
	private JPanel contentPanel;
	
	private JButton backButton;
	private JButton nextButton;
	
	private JCheckBox infarCb;
	private JCheckBox infoCb;
	
	private JTabbedPane tabPane;
	private JList infarList;
	private JList infoList;
	
	private HashMap<String, String> searchCriteria;
	
	public SearchPage(SemanticWebEngine swe, CardLayout cl, JPanel content, HashMap<String, String> sc){
		tabPane = new JTabbedPane();
		backButton = new JButton(" Cancel");
		nextButton = new JButton("Search ");
		searchPage = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.swe = swe;
		card = cl;
		contentPanel = content;
		searchCriteria = sc;
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return searchPage;
	}
	
	private void createPage(){
		
		JPanel upPanel = new JPanel();
		JPanel middlePanel = new JPanel();
		JPanel downPanel = new JPanel();
		
		JPanel checkPanel = new JPanel();
		JPanel rulesPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		
		GridBagConstraints gbc = new GridBagConstraints();

		upPanel.setLayout(new GridBagLayout());
		middlePanel.setLayout(new GridBagLayout());
		downPanel.setLayout(new GridLayout(2, 1));
		
		checkPanel.setLayout(new GridLayout(5, 1));
		rulesPanel.setLayout(new GridLayout(2, 1));
		buttonPanel.setLayout(new GridBagLayout());
		
		JCheckBox infarCB = new JCheckBox("Infarmed");
		JCheckBox infoCB = new JCheckBox("Infomed");
		
		checkPanel.add(new JLabel());
		checkPanel.add(infarCB);
		checkPanel.add(new JLabel());
		checkPanel.add(infoCB);
		checkPanel.add(new JLabel());
		
		this.createTabbedPane();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.2;
		upPanel.add(checkPanel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 2;
		upPanel.add(tabPane, gbc);
		
//		-------------------------------------------
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.08;
		middlePanel.add(new JLabel("Properties List:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 3;
		middlePanel.add(new JTextField(), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.08;
		middlePanel.add(new JLabel("Mapping Rules:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 3;
		middlePanel.add(new JTextField(), gbc);
		
//		-------------------------------------------
		
		JPanel suggestions = new JPanel();
		suggestions.setPreferredSize(new Dimension(400, 100));
		
		rulesPanel.add(new JLabel("Mapping Suggestions"));
		rulesPanel.add(new JTextArea());
		
//		-------------------------------------------
		
		nextButton.setForeground(Color.WHITE);
		nextButton.setFont(new Font("Arial", Font.BOLD, 16));
		nextButton.setBackground(new java.awt.Color(005, 220, 105));
		nextButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("search100.png")));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
	    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		
		backButton.setForeground(Color.WHITE);
		backButton.setFont(new Font("Arial", Font.BOLD, 16));
		backButton.setBackground(new java.awt.Color(226, 006, 021));
		backButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("delete85.png")));
		
		backButton.addActionListener(new backListener());
		nextButton.addActionListener(new searchListener());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		buttonPanel.add(backButton, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0;
		buttonPanel.add(nextButton, gbc);
		
//		-------------------------------------------
		
		downPanel.add(middlePanel);
		downPanel.add(rulesPanel);
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new GridLayout(2, 1));
		
		bottom.add(downPanel);
		bottom.add(buttonPanel);
		
		searchPage.add(upPanel);
		searchPage.add(bottom);
		searchPage.setDividerSize(0);
	}
	
	private void createTabbedPane(){

		String result = null;
		String[] classes;
		JTextArea infarText, infoText;
		JPanel infarClass, infoClass, infarProp, infoProp;
		JSplitPane infarPanel, infoPanel;
		JScrollPane infarScrollPane, infoScrollPane;
		DefaultListModel<String> infarListModel, infoListModel;
		//selectionListener sl;

		//sl = new selectionListener();
		infarPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		infarPanel.setPreferredSize(new Dimension(400, 150));
		infarPanel.setDividerSize(1);
		
		infarClass = new JPanel(new GridLayout(2, 1));
		infarProp = new JPanel(new GridLayout(2, 1));
		infarText = new JTextArea();
		
		infarListModel = new DefaultListModel<String>();
		infarScrollPane = new JScrollPane();

		try {
			result = swe.showSourceClasses("infarmed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			classes = result.split("\\r?\\n");
			for(int i=3; i < classes.length - 1; i++){
				infarListModel.addElement(classes[i]);
			}
		}

		infarList = new JList<String>(infarListModel);
		infarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		infarList.addListSelectionListener(sl);

		infarClass.add(new JLabel("Classes:"));
		infarClass.add(infarList);
		
		infarProp.add(new JLabel("Properties:"));
		infarProp.add(infarText);
		
		infarPanel.add(infarClass);
		infarPanel.add(infarProp);

		infarScrollPane.setViewportView(infarPanel);
//		infarPanel.add(infarScrollPane);
		

		//		------------------------------------------------------------

		result = null;
		infoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		infoPanel.setPreferredSize(new Dimension(400, 150));
		infoPanel.setDividerSize(1);
		
		infoClass = new JPanel(new GridLayout(2, 1));
		infoProp = new JPanel(new GridLayout(2, 1));
		infoText = new JTextArea();
		
		infoListModel = new DefaultListModel<String>();
		infoScrollPane = new JScrollPane();
		
		try {
			result = swe.showSourceClasses("infomed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			classes = result.split("\\r?\\n");
			for(int i=3; i < classes.length - 1; i++){
				infoListModel.addElement(classes[i]);
			}
		}

		infoList = new JList<String>(infoListModel);
		infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		infoList.addListSelectionListener(sl);
		
		infoClass.add(new JLabel("Classes:"));
		infoClass.add(infoList);
		
		infoProp.add(new JLabel("Properties:"));
		infoProp.add(infoText);
		
		infoPanel.add(infoClass);
		infoPanel.add(infoProp);

		infoScrollPane.setViewportView(infoPanel);
//		infoPanel.add(infoScrollPane);
		

		tabPane.addTab("Infarmed", infarScrollPane);
		tabPane.addTab("Infomed", infoScrollPane);

	}
	
	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(searchPage);
			contentPanel.revalidate();
			card.show(contentPanel, "page2");
		}
	}
	
	private class searchListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			resultPage = new ResultsPage(swe, card, contentPanel);
			pageResult = resultPage.getPage();
			pageResult.setName("pageResult");

			contentPanel.add(pageResult, "pageResult");
			card.show(contentPanel, "pageResult");
		}
	}

}
