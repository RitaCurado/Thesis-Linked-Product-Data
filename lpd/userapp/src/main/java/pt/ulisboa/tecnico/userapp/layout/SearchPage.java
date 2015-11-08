package pt.ulisboa.tecnico.userapp.layout;

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
	private JTextField propsList, mappRules;
	
	private HashMap<String, JList<String>> listBySource;
	private HashMap<String, JTextArea> textAreaBySource;

	private String className;
	private String properties, mapRule;
	private HashMap<String, String> searchCriteria;
	private ArrayList<String> sources;

	private String propDefault = "Ex: <www.s1.com/p1>|<www.s2.com/p2>";
	private String ruleDefault = "Ex: <www.s1.com/p1>-<www.s2.com/p3>|<www.s1.com/p4>-<www.s2.com/p1>";

	public SearchPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content, ArrayList<String> sources, HashMap<String, String> sc){
		tabPane = new JTabbedPane();
		backButton = new JButton("Cancel");
		nextButton = new JButton(" Search");
		
		listBySource = new HashMap<String, JList<String>>();
		textAreaBySource = new HashMap<String, JTextArea>();

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

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.25;
		middlePanel.add(new JLabel("Mapping Rules:"), gbc);

		mappRules.setText("Ex: <www.s1.com/p1>-<www.s2.com/p3>|<www.s1.com/p4>-<www.s2.com/p1>");
		mappRules.setForeground(Color.LIGHT_GRAY);
		mappRules.addFocusListener(tfl);

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
		nextButton.setIcon(new ImageIcon("..\\src\\main\\resources\\search100.png"));

		backButton.setForeground(Color.WHITE);
		backButton.setFont(new Font("Arial", Font.BOLD, 16));
		backButton.setBackground(new Color(226, 006, 021));
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\delete85.png"));
		backButton.setVerticalTextPosition(SwingConstants.CENTER);
		backButton.setHorizontalTextPosition(SwingConstants.LEFT);

		backButton.addActionListener(new BackListener());
		nextButton.addActionListener(new SearchListener());

		buttonPanel.add(new JLabel());
		buttonPanel.add(new JLabel());
		buttonPanel.add(nextButton);
		buttonPanel.add(backButton);
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
		
		HashMap<String, JPanel> classPanelBySource = new HashMap<String, JPanel>();
		HashMap<String, JPanel> propPanelBySource = new HashMap<String, JPanel>();
		HashMap<String, JSplitPane> splitPaneBySource = new HashMap<String, JSplitPane>();
		HashMap<String, JScrollPane> scrollClassBySource = new HashMap<String, JScrollPane>();
		HashMap<String, JScrollPane> scrollPropBySource = new HashMap<String, JScrollPane>();
		HashMap<String, DefaultListModel<String>> listModelBySource = new HashMap<String, DefaultListModel<String>>();

		GridBagConstraints gbc = new GridBagConstraints();
		SelectionListener sl = new SelectionListener();
		
		for(String source: sources){
			result = null;
			
			JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			sp.setDividerSize(1);
			splitPaneBySource.put(source, sp);
			
			classPanelBySource.put(source, new JPanel(new GridBagLayout()));
			propPanelBySource.put(source, new JPanel(new GridBagLayout()));
			
			scrollClassBySource.put(source, new JScrollPane());
			scrollPropBySource.put(source, new JScrollPane());
			
			listModelBySource.put(source, new DefaultListModel<String>());
			
			textAreaBySource.put(source, new JTextArea("\n\n\n\n"));
			
			try {
				result = swe.showSourceClasses(source.toLowerCase());
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(result != null){
				for(String s: result){
					listModelBySource.get(source).addElement(s);
				}
			}
			
			JList<String> jlist = new JList<String>(listModelBySource.get(source));
			jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jlist.addListSelectionListener(sl);
			listBySource.put(source, jlist);
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.weighty = 0;
			classPanelBySource.get(source).add(new JLabel("Classes:"), gbc);

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 1;
			classPanelBySource.get(source).add(listBySource.get(source), gbc);
			scrollClassBySource.get(source).setViewportView(classPanelBySource.get(source));

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0;
			gbc.weighty = 0;
			propPanelBySource.get(source).add(new JLabel("Properties:"), gbc);

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			propPanelBySource.get(source).add(textAreaBySource.get(source), gbc);
			scrollPropBySource.get(source).setViewportView(propPanelBySource.get(source));

			splitPaneBySource.get(source).add(scrollClassBySource.get(source));
			splitPaneBySource.get(source).add(scrollPropBySource.get(source));
			
			tabPane.addTab(source, splitPaneBySource.get(source));
		}
		
	}


	private class SelectionListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			ArrayList<String> classProps = null;
			
			for(String key: listBySource.keySet()){
				JList<String> value = listBySource.get(key);
				
				if(!value.isSelectionEmpty()){
					className = value.getSelectedValue();
					try {
						classProps = swe.showClassProperties(className);
					} catch (Exception e) {
						e.printStackTrace();
					}
					textAreaBySource.get(key).setText("");
					for(String p: classProps){
						p = p.replace(" ", "");
						textAreaBySource.get(key).append(p);
						textAreaBySource.get(key).append("\n");
					}
				}
			}
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
