package layout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
	
	private JButton backButton, nextButton;	
	private JCheckBox infarCb, infoCb;
	
	private JTabbedPane tabPane;
	private JList<String> infarList, infoList;	
	private JTextArea infarText, infoText;
	private JTextField propsList, mappRules;
	
	private String className;
	private HashMap<String, String> searchCriteria;
	private ArrayList<String> sources;
	
	public SearchPage(SemanticWebEngine swe, CardLayout cl, JPanel content, HashMap<String, String> sc){
		tabPane = new JTabbedPane();
		backButton = new JButton(" Cancel");
		nextButton = new JButton("Search ");
		
		propsList = new JTextField();
		mappRules = new JTextField();
		
		sources = new ArrayList<String>();
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
		
		checkListener cl = new checkListener();
		GridBagConstraints gbc = new GridBagConstraints();

		upPanel.setLayout(new GridBagLayout());
		middlePanel.setLayout(new GridBagLayout());
		downPanel.setLayout(new GridLayout(2, 1));
		
		checkPanel.setLayout(new GridLayout(5, 1));
		rulesPanel.setLayout(new GridLayout(2, 1));
		buttonPanel.setLayout(new GridBagLayout());
		
		infarCb = new JCheckBox("Infarmed");
		infarCb.addActionListener(cl);
		
		infoCb = new JCheckBox("Infomed");
		infoCb.addActionListener(cl);
		
		checkPanel.add(new JLabel());
		checkPanel.add(infarCb);
		checkPanel.add(new JLabel());
		checkPanel.add(infoCb);
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
		
		TextFieldListener tfl = new TextFieldListener();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.08;
		middlePanel.add(new JLabel("Properties List:"), gbc);
		
		propsList.setText("Ex: <www.s1.com/p1>|<www.s2.com/p2>");
		propsList.setForeground(Color.LIGHT_GRAY);
		propsList.addFocusListener(tfl);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 3;
		middlePanel.add(propsList, gbc);
		
		mappRules.setText("Ex: <www.s1.com/p1>-<www.s2.com/p3>|<www.s1.com/p4>-<www.s2.com/p1>");
		mappRules.setForeground(Color.LIGHT_GRAY);
		mappRules.addFocusListener(tfl);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.08;
		middlePanel.add(new JLabel("Mapping Rules:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 3;
		middlePanel.add(mappRules, gbc);
		
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

		ArrayList<String> result = null;
		JPanel infarClass, infoClass, infarProp, infoProp;
		JSplitPane infarPanel, infoPanel;
		JScrollPane infarClassScroll, infoClassScroll;
		JScrollPane infarPropScroll, infoPropScroll;
		DefaultListModel<String> infarListModel, infoListModel;
		
		GridBagConstraints gbc = new GridBagConstraints();
		selectionListener sl = new selectionListener();
		
		infarPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		infarPanel.setPreferredSize(new Dimension(400, 150));
		infarPanel.setDividerSize(1);
		
		infarClass = new JPanel(new GridBagLayout());
		infarProp = new JPanel(new GridBagLayout());
		infarClassScroll = new JScrollPane();
		infarPropScroll = new JScrollPane();
		
		infarListModel = new DefaultListModel<String>();
		infarText = new JTextArea("\n\n\n\n");

		try {
			result = swe.showSourceClasses("infarmed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			for(String s: result){
				infarListModel.addElement(s);
			}
		}

		infarList = new JList<String>(infarListModel);
		infarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		infarList.addListSelectionListener(sl);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		infarClass.add(new JLabel("Classes:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		infarClass.add(infarList, gbc);
		infarClassScroll.setViewportView(infarClass);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		infarProp.add(new JLabel("Properties:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		infarProp.add(infarText, gbc);
		infarPropScroll.setViewportView(infarProp);
		
		infarPanel.add(infarClassScroll);
		infarPanel.add(infarPropScroll);


		//		------------------------------------------------------------

		result = null;
		infoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		infoPanel.setPreferredSize(new Dimension(400, 150));
		infoPanel.setDividerSize(1);
		
		infoClass = new JPanel(new GridLayout(2, 1));
		infoProp = new JPanel(new GridLayout(2, 1));
		infoClassScroll = new JScrollPane();
		infoPropScroll = new JScrollPane();
		
		infoListModel = new DefaultListModel<String>();
		infoText = new JTextArea();
		
		try {
			result = swe.showSourceClasses("infomed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			for(String s: result){
				infoListModel.addElement(s);
			}
		}

		infoList = new JList<String>(infoListModel);
		infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		infoList.addListSelectionListener(sl);
		
		infoClass.add(new JLabel("Classes:"));
		infoClass.add(infoList);
		infoClassScroll.setViewportView(infoClass);
		
		infoProp.add(new JLabel("Properties:"));
		infoProp.add(infoText);
		infoPropScroll.setViewportView(infoProp);
		
		infoPanel.add(infoClassScroll);
		infoPanel.add(infoPropScroll);

		tabPane.addTab("Infarmed", infarPanel);
		tabPane.addTab("Infomed", infoPanel);

	}
	
	private class TextFieldListener implements FocusListener{
		
		JTextField tf;

		@Override
		public void focusGained(FocusEvent e) {
			tf = (JTextField) e.getSource();
			tf.setText("");
            tf.setForeground(Color.BLACK);
		}

		@Override
		public void focusLost(FocusEvent e) {
			tf = (JTextField) e.getSource();
			tf.setForeground(Color.LIGHT_GRAY);
			
			if(tf.getText().equals("")){
				
				if(tf.equals(propsList))
					propsList.setText("Ex: <www.s1.com/p1>|<www.s2.com/p2>");
				
				if(tf.equals(mappRules))
					mappRules.setText("Ex: <www.s1.com/p1>-<www.s2.com/p3>|<www.s1.com/p4>-<www.s2.com/p1>");
			}
		}
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
	
	private class checkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			JCheckBox cb = (JCheckBox) event.getSource();
			String name = cb.getText();
			int index = 0;
			
	        if (cb.isSelected()) {
	        	sources.add(name);
	            
	        } else {
	            index = sources.indexOf(name);
	            sources.remove(index);
	        }
		}
	}
	
	private class selectionListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			ArrayList<String> classProps = null;

			if(!infarList.isSelectionEmpty()){
				className = infarList.getSelectedValue();
				try {
					classProps = swe.showClassProperties(className);
				} catch (Exception e) {
					e.printStackTrace();
				}
				infarText.setText("");
				infarText.revalidate();
				
				for(String p: classProps){
					infarText.append(p);
				}
			}

			if(!infoList.isSelectionEmpty()){
				className = infoList.getSelectedValue();
				try {
					classProps = swe.showClassProperties(className);
				} catch (Exception e) {
					e.printStackTrace();
				}
				infoText.removeAll();
				infoText.revalidate();
				
				for(String p: classProps){
					infoText.append(p);
				}
			}

			infarList.clearSelection();
			infoList.clearSelection();
		}		
	}

}
