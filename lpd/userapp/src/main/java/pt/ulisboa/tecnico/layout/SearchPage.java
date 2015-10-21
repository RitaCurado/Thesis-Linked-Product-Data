package pt.ulisboa.tecnico.layout;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class SearchPage {

	private SemanticWebEngine swe;
	private JSplitPane pageResult;	
	private ResultsPage resultPage;

	private JSplitPane searchPage;
	private CardLayout card;
	private JPanel contentPanel;
	private JFrame frame;

	private JButton backButton, nextButton;

	private JTabbedPane tabPane;
	private JList<String> infarList, infoList;	
	private JTextArea infarText, infoText;
	private JTextField propsList, mappRules;

	private String className;
	private String properties, mapRule;
	private HashMap<String, String> searchCriteria;
	private ArrayList<String> sources;

	private String propDefault = "Ex: <www.s1.com/p1>|<www.s2.com/p2>";
	private String ruleDefault = "Ex: <www.s1.com/p1>-<www.s2.com/p3>|<www.s1.com/p4>-<www.s2.com/p1>";

	public SearchPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content, ArrayList<String> sources, HashMap<String, String> sc){
		tabPane = new JTabbedPane();
		backButton = new JButton(" Cancel");
		nextButton = new JButton("Search ");

		propsList = new JTextField();
		mappRules = new JTextField();
		properties = "";
		mapRule = "";

		this.sources = sources;
		searchPage = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		this.swe = swe;
		card = cl;
		contentPanel = content;
		frame = gui;
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

		JSplitPane rulesPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JPanel buttonPanel = new JPanel(new GridLayout(1, 6));
		
		GridBagConstraints gbc = new GridBagConstraints();

		this.createTabbedPane();

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		upPanel.add(new JLabel("Information Sources:"), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 3;
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
		
		RadioListener rl = new RadioListener();

		JRadioButton r6 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Dosagem>-<http://www.infomed.pt/Dosagem>");
		r6.setBackground(Color.WHITE);
		r6.addActionListener(rl);
		
		JRadioButton r7 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Genérico>-<http://www.infomed.pt/Genérico>");
		r7.setBackground(Color.WHITE);
		r7.addActionListener(rl);
		
		JRadioButton r11 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Substância_Activa>-<http://www.infomed.pt/Nome_Genérico>|"
				+ "<http://www.infarmed.pt/Dosagem>-<http://www.infomed.pt/Dosagem>");
		r11.setBackground(Color.WHITE);
		r11.addActionListener(rl);
		
		JRadioButton r14 = new JRadioButton("<http://www.infarmed.pt/Nome_do_Medicamento>-<http://www.infomed.pt/Nome_do_Medicamento>|"
				+ "<http://www.infarmed.pt/Dosagem>-<http://www.infomed.pt/Dosagem>|"
				+ "<http://www.infarmed.pt/Genérico>-<http://www.infomed.pt/Genérico>");
		r14.setBackground(Color.WHITE);
		r14.addActionListener(rl);

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

		backButton.addActionListener(new BackListener());
		nextButton.addActionListener(new SearchListener());

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
		SelectionListener sl = new SelectionListener();

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

		infoClass = new JPanel(new GridBagLayout());
		infoProp = new JPanel(new GridBagLayout());
		infoClassScroll = new JScrollPane();
		infoPropScroll = new JScrollPane();

		infoListModel = new DefaultListModel<String>();
		infoText = new JTextArea("\n\n\n\n");

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

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		infoClass.add(new JLabel("Classes:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		infoClass.add(infoList, gbc);
		infoClassScroll.setViewportView(infoClass);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		infoProp.add(new JLabel("Properties:"), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		infoProp.add(infoText, gbc);
		infoPropScroll.setViewportView(infoProp);

		infoPanel.add(infoClassScroll);
		infoPanel.add(infoPropScroll);

		tabPane.addTab("Infarmed", infarPanel);
		tabPane.addTab("Infomed", infoPanel);

	}


	private class SelectionListener implements ListSelectionListener{

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

				for(String p: classProps){
					p = p.replace(" ", "");
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
				infoText.setText("");

				for(String p: classProps){
					p = p.replace(" ", "");
					infoText.append(p);
					infoText.append("\n");
				}
			}

//			infarList.clearSelection();
//			infoList.clearSelection();
		}		
	}

	private class TextFieldListener implements FocusListener{

		JTextField tf;

		@Override
		public void focusGained(FocusEvent e) {
			tf = (JTextField) e.getSource();

			if(tf.getText().contentEquals(propDefault)||tf.getText().contentEquals(ruleDefault)){
				tf.setText("");
				tf.setForeground(Color.BLACK);
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
			tf = (JTextField) e.getSource();

			if(tf.getText().contentEquals("")){
				tf.setForeground(Color.LIGHT_GRAY);

				if(tf.equals(propsList))
					propsList.setText(propDefault);

				if(tf.equals(mappRules))
					mappRules.setText(ruleDefault);
			}
			else{
				if(tf.equals(propsList))
					properties = tf.getText();

				if(tf.equals(mappRules))
					mapRule = tf.getText();
			}
		}
	}

	private class RadioListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton rb = (JRadioButton) e.getSource();

			if(rb.isSelected()){
				mapRule = rb.getText();
			}

			else{
				if(!mappRules.getText().contentEquals(ruleDefault))
					mapRule = mappRules.getText();
				else
					mapRule = "";
			}
		}
	}

	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(searchPage);
			contentPanel.revalidate();
			card.show(contentPanel, "page2");
		}
	}

	private class SearchListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			String queryResult = "";
			
			if(properties.contentEquals("")){
				JOptionPane.showMessageDialog(frame, "Properties List should not be empty.", "Attention!", JOptionPane.WARNING_MESSAGE);
			}
			else{
				
				if(mapRule.contentEquals(""))
					queryResult = swe.makeQuery(sources, searchCriteria, properties.split("\\|"), null);
				else
					swe.makeQuery(sources, searchCriteria, properties.split("\\|"), mapRule.split("\\|"));
				
				resultPage = new ResultsPage(card, contentPanel, queryResult);
				pageResult = resultPage.getPage();
				pageResult.setName("pageResult");
				
				contentPanel.add(pageResult, "pageResult");
				card.show(contentPanel, "pageResult");
			}
		}
	}

}
