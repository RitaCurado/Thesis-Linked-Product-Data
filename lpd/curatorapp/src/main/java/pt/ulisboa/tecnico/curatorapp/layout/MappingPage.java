package pt.ulisboa.tecnico.curatorapp.layout;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.jena.rdf.model.Model;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class MappingPage {

	private SemanticWebEngine swe;
	
	private JSplitPane mappingPage;
	private CardLayout card;
	private JPanel contentPanel;
	private JFrame frame;

	private JTabbedPane tabPane;
	private JTextField newClassName, mappRules;
	private JLabel sourceName;
	
	private ArrayList<String> sources;
	private ArrayList<JCheckBox> checkedSources;
	private HashMap<String, JTextArea> textAreaBySource;
	private HashMap<String, JList<String>> listBySource;

	private String classNameDefault;
	private String ruleDefault;
	private String className;
	private String rule;

	public MappingPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		sources = swe.getSources();
		
		mappingPage = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tabPane = new JTabbedPane();
		sourceName = new JLabel(" ", SwingConstants.LEFT);
		newClassName = new JTextField();
		mappRules = new JTextField();
		
		
		checkedSources = new ArrayList<JCheckBox>();
		textAreaBySource = new HashMap<String, JTextArea>();
		listBySource = new HashMap<String, JList<String>>();
		
		classNameDefault = "Ex: MappingBySomeProperty";
		ruleDefault = "Ex: <www.s1.com/p1>-<www.s2.com/p3>|<www.s1.com/p4>-<www.s2.com/p1>";
		className = "";
		rule = "";
		
		this.createPage();
	}

	public JSplitPane getPage(){
		return mappingPage;
	}

	public void createPage(){
		JButton backButton, nextButton, removeButton;
		nextButton = new JButton(" Create");
		removeButton = new JButton("Remove ");
		backButton = new JButton("Cancel");
		
		JPanel infoPanel = new JPanel(new GridBagLayout());
		JPanel autoPanel = new JPanel(new GridBagLayout());
		JPanel fillPanel = new JPanel(new GridBagLayout());
		JPanel checkPanel = new JPanel(new GridLayout(0, 1));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 5));

		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane middlePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		GridBagConstraints gbc = new GridBagConstraints();
		
		CheckListener cl = new CheckListener();
		ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
		
		String cbName, sName;
		String[] sSplit;

		for(String source: sources){
			if(!source.contains("+")){
				sSplit = source.split("\\.");
				sName = sSplit[1];
				cbName = sName.substring(0, 1).toUpperCase() + sName.substring(1);
				checkBoxes.add(new JCheckBox(cbName));
			}
		}
		for(JCheckBox cb: checkBoxes){
			cb.addActionListener(cl);
			checkPanel.add(cb);
		}
		
//	-- CheckBoxes & TabPane --------------------------
		this.createTabbedPane();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		infoPanel.add(checkPanel, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 4;
		infoPanel.add(tabPane, gbc);
		
//	-- Automatic source name -------------------------
		JLabel label = new JLabel("Mapping Rules");
		label.setFont(new Font("Arial", Font.BOLD, 16));
		
		JButton consultButton = new JButton("Consult DB");
		consultButton.addActionListener(new ConsultListener());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		autoPanel.add(consultButton, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		autoPanel.add(new JLabel(" "), gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 3;
		autoPanel.add(label, gbc);		
		
		//--------------
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.25;
		fillPanel.add(new JLabel("Source Name:"), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 10;
		fillPanel.add(sourceName, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		fillPanel.add(new JLabel(" "), gbc);
		
//	-- Class name & Mapping rule ---------------------
		TextFieldListener tfl = new TextFieldListener();

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0.25;
		fillPanel.add(new JLabel("Class Name:"), gbc);

		newClassName.setText(classNameDefault);
		newClassName.setForeground(Color.LIGHT_GRAY);
		newClassName.addFocusListener(tfl);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 10;
		fillPanel.add(newClassName, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 3;
		fillPanel.add(new JLabel(" "), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0.25;
		fillPanel.add(new JLabel("Rules:"), gbc);

		mappRules.setText(ruleDefault);
		mappRules.setForeground(Color.LIGHT_GRAY);
		mappRules.addFocusListener(tfl);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 10;
		fillPanel.add(mappRules, gbc);
//	-----------------------------------------------------
		

		fillPanel.setPreferredSize(new Dimension(400, 170));
		
		middlePanel.add(autoPanel);
		middlePanel.add(fillPanel);
		middlePanel.setDividerSize(0);
		
		upPanel.add(infoPanel);
		upPanel.add(middlePanel);
		upPanel.setDividerSize(0);
		
//	-- Previous & Next buttons --------------------------
		nextButton.setForeground(Color.WHITE);
		nextButton.setFont(new Font("Arial", Font.BOLD, 15));
		nextButton.setBackground(new Color(005, 220, 105));
		nextButton.setIcon(new ImageIcon("..\\src\\main\\resources\\checked16px.png"));

		removeButton.setForeground(Color.WHITE);
		removeButton.setFont(new Font("Arial", Font.BOLD, 15));
		removeButton.setBackground(new Color(226, 006, 021));
		removeButton.setIcon(new ImageIcon("..\\src\\main\\resources\\delete85.png"));
		removeButton.setVerticalTextPosition(SwingConstants.CENTER);
		removeButton.setHorizontalTextPosition(SwingConstants.LEFT);
		
		backButton.setFont(new Font("Arial", Font.BOLD, 15));
		
		backButton.addActionListener(new BackListener());
		nextButton.addActionListener(new CreateListener());
		removeButton.addActionListener(new RemoveListener());

		buttonPanel.add(new JLabel());
		buttonPanel.add(nextButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(backButton);
		buttonPanel.add(new JLabel());
//	-----------------------------------------------------
		
		buttonPanel.setPreferredSize(new Dimension(400, 10));
		
		
		mappingPage.add(upPanel);
		mappingPage.add(buttonPanel);
		mappingPage.setDividerSize(1);
	}
	
	private void createTabbedPane(){

		String tab, sourceName;
		String[] sSplit;
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
			
			sSplit = source.split("\\.");
			sourceName = sSplit[1];
			tab = sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1);
			tabPane.addTab(tab, splitPaneBySource.get(source));
		}
	}

	private class CheckListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			int counter = 0;
			String cbName = "";
			String finalSource = "";
			JCheckBox cb = (JCheckBox) event.getSource();
			
			sources = swe.getSources();
			
			if (cb.isSelected()) {
				checkedSources.add(cb);
			} else {
				checkedSources.remove(cb);
			}
			
			if(checkedSources.size() == 0)
				finalSource = "";
			else{
				for(String source: sources){
					counter = 0;
					for(JCheckBox jcb: checkedSources){
						cbName = jcb.getText().toLowerCase();
						if(source.contains(cbName))
							counter++;
					}
					if(counter == checkedSources.size()){
						finalSource = "http://" + source;
						break;
					}
					else
						finalSource = "";
				}
				
				if(finalSource == ""){
					finalSource = "http://www.";
					
					for(JCheckBox jcb: checkedSources){
						finalSource += jcb.getText().toLowerCase();
						finalSource += "+";
					}
					finalSource = finalSource.substring(0, finalSource.length()-1);
					finalSource += ".pt";
					
				}
			}
			
			sourceName.setText(finalSource);
		}
	}
	
	private class SelectionListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			ArrayList<String> classProps;
			String className;
			
			for(String key: listBySource.keySet()){
				JList<String> value = listBySource.get(key);
				
				if(!value.isSelectionEmpty()){
					className = value.getSelectedValue();
					className = className.substring(2, className.length()-2);
					//System.out.println("ClassName: " + className);
					
//					classProps.addAll(swe.showClassProperties(className));
//					classProps.addAll(swe.showNodeProperties(className));
					classProps = swe.showClassProperties(className);
					
					textAreaBySource.get(key).setText("");
					for(String p: classProps){
						p = p.replace(" ", "");
						textAreaBySource.get(key).append(p);
						textAreaBySource.get(key).append("\n");
					}
					value.clearSelection();
				}
			}
		}		
	}
	
	private class TextFieldListener implements FocusListener{

		JTextField tf;

		@Override
		public void focusGained(FocusEvent e) {
			tf = (JTextField) e.getSource();

			if(tf.getText().contentEquals(classNameDefault)||tf.getText().contentEquals(ruleDefault)){
				tf.setText("");
				tf.setForeground(Color.BLACK);
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
			tf = (JTextField) e.getSource();

			if(tf.getText().contentEquals("")){
				tf.setForeground(Color.LIGHT_GRAY);

				if(tf.equals(newClassName))
					newClassName.setText(classNameDefault);

				if(tf.equals(mappRules))
					mappRules.setText(ruleDefault);
			}
			else{
				if(tf.equals(newClassName))
					className = tf.getText();

				if(tf.equals(mappRules))
					rule = tf.getText();
			}
		}
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(mappingPage);
			contentPanel.revalidate();
			card.show(contentPanel, "page1");
		}
	}

	private class CreateListener implements ActionListener{
		
		private JSplitPane pageResult;
		private ResultsPage resultPage;

		@Override
		public void actionPerformed(ActionEvent e) {
			
			int sid = 0;
			String clName;
			String source = "";
			HashMap<String, String> subjectBySource = new HashMap<String, String>();
			HashMap<String, ArrayList<String>> propsBySource = new HashMap<String, ArrayList<String>>();
			HashMap<String, ArrayList<String>> nodesBySource = new HashMap<String, ArrayList<String>>();
			
			if(className.contentEquals("") || rule.contentEquals("")){
				JOptionPane.showMessageDialog(frame, 
						"The \"Class Name\" and \"Rules\" fields should not be empty.", "Attention!", JOptionPane.WARNING_MESSAGE);
			}
			else{
				//get all source properties
				for(JCheckBox cb: checkedSources){
					sid++;
					source = cb.getText().toLowerCase();
					subjectBySource.put(source, "s" + sid);
					clName = "http://www." + source + ".pt/Medicine";
					try {
//						ArrayList<String> props = swe.showClassProperties(clName);
//						props.addAll(swe.showNodeProperties(clName));
						propsBySource.put(source, swe.showClassProperties(clName));
						nodesBySource.put(source, swe.showNodeProperties(clName));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
				Model resultModel = swe.mappingConstructQuery(subjectBySource, propsBySource, nodesBySource,
										sourceName.getText(), className, mappRules.getText().split("\\|"));
				
				
				resultPage = new ResultsPage(swe, card, contentPanel, resultModel);
				pageResult = resultPage.getPage();
				pageResult.setName("pageResult");
				
				contentPanel.add(pageResult, "pageResult");
				card.show(contentPanel, "pageResult");
			}
		}
	}
	
	private class RemoveListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(className.contentEquals("")){
				JOptionPane.showMessageDialog(frame, 
						"The \"Class Name\" field should not be empty.", "Attention!", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	private class ConsultListener implements ActionListener{
		
		private JSplitPane pageConsult;
		private ConsultDB consultPage;

		@Override
		public void actionPerformed(ActionEvent e) {
			consultPage = new ConsultDB(swe, card, contentPanel);
			pageConsult = consultPage.getPage();
			pageConsult.setName("pageConsult");
			
			contentPanel.add(pageConsult, "pageConsult");
			card.show(contentPanel, "pageConsult");
		}
	}
}
