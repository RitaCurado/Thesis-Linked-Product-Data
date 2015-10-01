package layout;

import javax.swing.*;
import javax.swing.event.*;

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
		
		JPanel upPanel = new JPanel(new GridBagLayout());
		JPanel middlePanel = new JPanel(new GridBagLayout());
		JSplitPane downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel checkPanel = new JPanel(new GridLayout(7, 1));
		JSplitPane rulesPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JPanel buttonPanel = new JPanel(new GridLayout(1, 6));
		
		checkListener cl = new checkListener();
		GridBagConstraints gbc = new GridBagConstraints();
		
		infarCb = new JCheckBox("Infarmed");
		infarCb.addActionListener(cl);
		
		infoCb = new JCheckBox("Infomed");
		infoCb.addActionListener(cl);
		
		checkPanel.add(new JLabel("Information Sources:"));
		checkPanel.add(new JLabel());
		checkPanel.add(infarCb);
		checkPanel.add(new JLabel());
		checkPanel.add(infoCb);
		checkPanel.add(new JLabel());
		checkPanel.add(new JLabel());
		
		this.createTabbedPane();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
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
		gbc.weightx = 0.25;
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
		gbc.weightx = 0.25;
		middlePanel.add(new JLabel("Mapping Rules:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 3;
		middlePanel.add(mappRules, gbc);
		
//		-------------------------------------------
		
		JRadioButton r6 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Dosagem>-<http://www.infomed.pt/Dosagem>");
		r6.setBackground(Color.WHITE);
		JRadioButton r7 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Genérico>-<http://www.infomed.pt/Genérico>");
		r7.setBackground(Color.WHITE);
		JRadioButton r11 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Substância_Activa>-<http://www.infomed.pt/Nome_Genérico>|"
				+ "<http://www.infarmed.pt/Dosagem>-<http://www.infomed.pt/Dosagem>");
		r11.setBackground(Color.WHITE);
		JRadioButton r14 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Dosagem>-<http://www.infomed.pt/Dosagem>|"
				+ "<http://www.infarmed.pt/Genérico>-<http://www.infomed.pt/Genérico>");
		r14.setBackground(Color.WHITE);
		
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 221, 242)));
		radioPanel.add(r6);
		radioPanel.add(r7);
		radioPanel.add(r11);
		radioPanel.add(r14);
		
		JScrollPane radioScrollPane = new JScrollPane();
		radioScrollPane.setViewportView(radioPanel);
		
		JLabel suggestions = new JLabel("Mapping Suggestions:");
		suggestions.setBackground(new Color(200, 221, 242));
		suggestions.setOpaque(true);
		
		rulesPanel.add(suggestions);
		rulesPanel.add(radioScrollPane);
		rulesPanel.setDividerSize(0);
		
//		-------------------------------------------
		
		nextButton.setForeground(Color.WHITE);
		nextButton.setFont(new Font("Arial", Font.BOLD, 16));
		nextButton.setBackground(new Color(005, 220, 105));
		nextButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("search100.png")));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
	    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		
		backButton.setForeground(Color.WHITE);
		backButton.setFont(new Font("Arial", Font.BOLD, 16));
		backButton.setBackground(new Color(226, 006, 021));
		backButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("delete85.png")));
		
		backButton.addActionListener(new backListener());
		nextButton.addActionListener(new searchListener());

		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		buttonPanel.add(backButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		
//		-------------------------------------------
		
		downPanel.add(middlePanel);
		downPanel.add(rulesPanel);
		downPanel.setDividerSize(0);
		
		JSplitPane bottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		downPanel.setPreferredSize(new Dimension(400, 206));
		bottom.add(downPanel);
		bottom.add(buttonPanel);
		bottom.setDividerSize(0);
		
		upPanel.setPreferredSize(new Dimension(400, 190));
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
					infarText.append("\n");
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
					infoText.append("\n");
				}
			}

			infarList.clearSelection();
			infoList.clearSelection();
		}		
	}

}
