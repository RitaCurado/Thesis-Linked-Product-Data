package pt.ulisboa.tecnico.userapp.layout;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class SecondPage {

	private CardLayout card;
	private JPanel contentPanel;
	private JPanel criteriaPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private JSplitPane page2;
	private String chosenClass;

	private JComboBox<String> mappingsList;
	private JComboBox<String> sourcesList;

	private ArrayList<JCheckBox> checkBoxes;
	

	public SecondPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel cotent){
		
		this.swe = swe;
		card = cl;
		contentPanel = cotent;
		frame = gui;
		
		page2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		checkBoxes = new ArrayList<JCheckBox>();

		this.createPage();

	}

	public JSplitPane getPage(){
		return page2;
	}

	private void createPage(){
		
		JLabel title = new JLabel("Search Criteria");
		title.setFont(new Font("Arial", Font.BOLD, 15));
		
		JButton cancelButton = new JButton(" Cancel");
		JButton searchButton = new JButton("Search ");

		JScrollPane criteriaScroll;
		criteriaPanel = new JPanel(new GridLayout(0, 2));
		JPanel upPanel = new JPanel(new GridLayout(5, 1));
		JPanel downPanel = new JPanel(new GridLayout(1, 6));
		JPanel sourcePanel = new JPanel(new GridLayout(1, 2));
		JPanel mappingPanel = new JPanel(new GridLayout(1, 2));
		JSplitPane downPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
//		-------------
		
		ArrayList<String> sources = new ArrayList<String>();
		sources.add("- Select an option -");
		sources.addAll(swe.getSources());
		
		String[] sourcesArray = new String[sources.size()];
		sourcesArray = sources.toArray(sourcesArray);

		sourcesList = new JComboBox<String>(sourcesArray);
//		-------------
		ArrayList<String> mappingRules = new ArrayList<String>();
		mappingRules.add("- Select an option -");
		mappingRules.addAll(swe.showMappingRules(""));
		
		String[] mappsArray = new String[mappingRules.size()];
		mappsArray = mappingRules.toArray(mappsArray);
		
		mappingsList = new JComboBox<String>(mappsArray);
//		-------------
		
		criteriaPanel.setBackground(Color.WHITE);
		criteriaScroll = new JScrollPane(criteriaPanel);
		criteriaScroll.setPreferredSize(new Dimension(400, 270));

		sourcesList.setSelectedIndex(0);
		sourcesList.addActionListener(new SourcesListListener());
		mappingsList.setSelectedIndex(0);
		mappingsList.addActionListener(new MappListListener());

		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
		cancelButton.setBackground(new Color(226, 006, 021));
		cancelButton.setIcon(new ImageIcon("..\\src\\main\\resources\\delete85.png"));
		cancelButton.addActionListener(new CancelListener());
		
		searchButton.setForeground(Color.WHITE);
		searchButton.setFont(new Font("Arial", Font.BOLD, 16));
		searchButton.setBackground(new Color(005, 220, 105));
		searchButton.setIcon(new ImageIcon("..\\src\\main\\resources\\search100.png"));
		searchButton.setVerticalTextPosition(SwingConstants.CENTER);
	    searchButton.setHorizontalTextPosition(SwingConstants.LEFT);
		searchButton.addActionListener(new SearchListener());
//		--------------

		sourcePanel.add(sourcesList);
		sourcePanel.add(new JLabel());
		mappingPanel.add(mappingsList);
		mappingPanel.add(new JLabel());

		upPanel.add(title);
		upPanel.add(new JLabel("Sources:"));
		upPanel.add(sourcePanel);
		upPanel.add(new JLabel("Mapping Rules:"));
		upPanel.add(mappingPanel);

		downPanel.add(new JLabel());
		downPanel.add(new JLabel());
		downPanel.add(cancelButton);
		downPanel.add(searchButton);
		downPanel.add(new JLabel());
		downPanel.add(new JLabel());

		downPane.add(criteriaScroll);
		downPane.add(downPanel);
		downPane.setDividerSize(1);

		page2.add(upPanel);
		page2.add(downPane);
		page2.setDividerSize(0);
	}
	
	private class MappListListener implements ActionListener{

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String mapping = (String) cb.getSelectedItem();
			
			JTextField tf;
			JCheckBox checkB;
			checkListener cl = new checkListener();
			ArrayList<String> props = null;
			
			if(mapping.equals("- Select an option -")){
				sourcesList.setEnabled(true);
				criteriaPanel.removeAll();
				criteriaPanel.revalidate();
			}
			else{
				sourcesList.setEnabled(false);
				chosenClass = mapping.substring(2, mapping.length()-2);
				props = swe.showClassProperties(chosenClass, "afterAgg");
				
				criteriaPanel.removeAll();
				criteriaPanel.revalidate();

				for(String p: props){
					p = p.replace(" ", "");

					checkB = new JCheckBox(p);
					checkB.addActionListener(cl);
					checkB.setBackground(Color.WHITE);

					tf = new JTextField();
					tf.setEditable(false);
					tf.setBackground(Color.WHITE);

					criteriaPanel.add(checkB);
					criteriaPanel.add(tf);
				}
			}
			criteriaPanel.revalidate();
		}
	}
	
	private class SourcesListListener implements ActionListener{
		
		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			
			JTextField tf;
			JCheckBox checkB;
			checkListener cl = new checkListener();
			ArrayList<String> classes, props;
			
			if(source.equals("- Select an option -")){
				mappingsList.setEnabled(true);
				criteriaPanel.removeAll();
			}
			else{
				mappingsList.setEnabled(false);

				classes = swe.showSourceClasses(source.toLowerCase(), "afterAgg");
				chosenClass = classes.get(0).substring(2, classes.get(0).length()-2);
				
				props = swe.showClassProperties(chosenClass, "afterAgg");

				criteriaPanel.removeAll();
				criteriaPanel.revalidate();

				for(String p: props){
					p = p.replace(" ", "");

					checkB = new JCheckBox(p);
					checkB.addActionListener(cl);
					checkB.setBackground(Color.WHITE);

					tf = new JTextField();
					tf.setEditable(false);
					tf.setBackground(Color.WHITE);

					criteriaPanel.add(checkB);
					criteriaPanel.add(tf);
				}
			}
			criteriaPanel.revalidate();
		}
	}

	private class checkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			JTextField tf = null;

			for (int i = 0; i < criteriaPanel.getComponentCount(); i++) {
				if (criteriaPanel.getComponent(i) == cb)
					tf = (JTextField) criteriaPanel.getComponent(i+1);
			}

			if(cb.isSelected()){
				tf.setEditable(true);
				checkBoxes.add(cb);
			}
			else{
				tf.setEditable(false);
				checkBoxes.remove(cb);
			}
		}
	}

	private class CancelListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(page2);
			contentPanel.revalidate();
			card.show(contentPanel, "pageChoose");
		}
	}

	private class SearchListener implements ActionListener{

		private ResultsPage resultPage;
		private JSplitPane pageResult;

		@Override
		public void actionPerformed(ActionEvent e) {

			JTextField tf = null;
			HashMap<String, String> searchCriteria = new HashMap<String, String>();

			if(!checkBoxes.isEmpty()){
				
				for(JCheckBox jcb: checkBoxes){

					for (int i = 0; i < criteriaPanel.getComponentCount(); i++) {
						if (criteriaPanel.getComponent(i) == jcb){
							tf = (JTextField) criteriaPanel.getComponent(i+1);
							break;
						}
					}
					//properties and respective values
					searchCriteria.put(jcb.getText(), tf.getText());				
				}

				//Make the query and produce the results in the resultsPpage
				//list all properties from chosenClass where the properties searchCriteria have the chose values
				//query input: searchCriteria; chosenClass
				//query output: string (table with the results)
				
				//chosenClass = chosenClass.substring(2, chosenClass.length()-2);
				String queryResult = swe.makeSelectQuery(searchCriteria, chosenClass);
				
				resultPage = new ResultsPage(card, contentPanel, queryResult);
				pageResult = resultPage.getPage();
				pageResult.setName("pageResult");
				
				contentPanel.add(pageResult, "pageResult");
				card.show(contentPanel, "pageResult");
				
			}
			else{
				JOptionPane.showMessageDialog(frame, "You have to select at least a property and give it a value.",
						"Attention!", JOptionPane.WARNING_MESSAGE);
			}

		}
	}

}
