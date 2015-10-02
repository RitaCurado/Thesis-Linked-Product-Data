package layout;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import lpd.SemanticWebEngine;

public class SecondPage {

	private JSplitPane page2;
	private JSplitPane pageSearch;	
	private SearchPage searchPage;

	private JLabel title;

	private JTextField drugName;
	private JTextField activeSubsName;

	private JButton backButton;
	private JButton nextButton;

	private JComboBox<String> sourcesList;

	private ArrayList<String> chosenSources;
	private ArrayList<JCheckBox> checkBoxes;
	private String[] sources = {"Infarmed", "Infomed", "Both"};

	private CardLayout card;
	private JPanel contentPanel;
	private JPanel criteriaPanel;
	private JFrame frame;
	private SemanticWebEngine swe;

	public SecondPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel cotent){
		page2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		title = new JLabel("Search Criteria");
		title.setFont(new Font("Arial", Font.BOLD, 15));

		sourcesList = new JComboBox<String>(sources);

		drugName = new JTextField();
		activeSubsName = new JTextField();

		backButton = new JButton(" Back");
		nextButton = new JButton("Next ");
		
		chosenSources = new ArrayList<String>();
		checkBoxes = new ArrayList<JCheckBox>();

		this.swe = swe;
		card = cl;
		contentPanel = cotent;
		frame = gui;

		drugName.setEnabled(false);
		activeSubsName.setEnabled(false);
		this.createPage();

	}

	public JSplitPane getPage(){
		return page2;
	}

	public String getDrugInfo(){
		return drugName.getText();
	}

	public String getActiveSubsName(){
		return activeSubsName.getText();
	}

	private void createPage(){

		criteriaPanel = new JPanel();
		JPanel upPanel = new JPanel();
		JPanel downPanel = new JPanel();
		JPanel sourcePanel = new JPanel();
		JSplitPane downPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JLabel hint = new JLabel("Choose the search criteria from one or both of the sources");

		hint.setFont(new Font("Arial", Font.PLAIN, 11));
		hint.setForeground(Color.GRAY);

		upPanel.setLayout(new GridLayout(3, 1));
		downPanel.setLayout(new GridLayout(1, 6));

		sourcePanel.setLayout(new GridLayout(1, 2));
		criteriaPanel.setLayout(new GridLayout(0, 2));
		criteriaPanel.setPreferredSize(new Dimension(400, 325));
		criteriaPanel.setBackground(Color.WHITE);

		sourcesList.addActionListener(new comboListListener());
		sourcesList.setSelectedIndex(0);

		backButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("return16px.png")));
		backButton.addActionListener(new backListener());
		
		nextButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("right arrow16px.png")));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
	    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		nextButton.addActionListener(new nextListener());

		sourcePanel.add(sourcesList);
		sourcePanel.add(new JLabel());

		upPanel.add(title);
		upPanel.add(hint);
		upPanel.add(sourcePanel);

		downPanel.add(backButton);
		downPanel.add(new JLabel());
		downPanel.add(new JLabel());
		downPanel.add(new JLabel());
		downPanel.add(new JLabel());
		downPanel.add(nextButton);

		downPane.add(criteriaPanel);
		downPane.add(downPanel);
		downPane.setDividerSize(1);

		page2.add(upPanel);
		page2.add(downPane);
		page2.setDividerSize(0);
	}

	private class comboListListener implements ActionListener{

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			String source = (String) cb.getSelectedItem();
			ArrayList<String> classes = null, props = null;
			checkListener cl = new checkListener();
			String[] bothProps = {"Nome do Medicamento", "Substância Activa"};
			JCheckBox checkB;
			JTextField tf;
			
			if(source.contentEquals("Both")){
				criteriaPanel.removeAll();
				criteriaPanel.revalidate();
				
				for(int i=0; i<bothProps.length; i++){
					
					checkB = new JCheckBox(bothProps[i]);
					checkB.addActionListener(cl);
					checkB.setBackground(Color.WHITE);
					
					tf = new JTextField();
					tf.setEditable(false);
					tf.setBackground(Color.WHITE);
					
					criteriaPanel.add(checkB);
					criteriaPanel.add(tf);
				}
				
				chosenSources.clear();
				chosenSources.add("Infarmed");
				chosenSources.add("Infomed");
			}
			
			else{
				
				chosenSources.clear();
				chosenSources.add(source);
				
				try {
					classes = swe.showSourceClasses(source.toLowerCase());
					props = swe.showClassProperties(classes.get(0));
					
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
					
				} catch (Exception e1) {
					e1.printStackTrace();
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

	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(page2);
			contentPanel.revalidate();
			card.show(contentPanel, "page1");
		}
	}

	private class nextListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			JTextField tf = null;
			HashMap<String, String> searchCriteria = new HashMap<String, String>();
			String[] byName = {"<http://www.infarmed.pt/Nome_do_Medicamento>", "<http://www.infomed.pt/Nome_do_Medicamento>"};
			String[] bySubstance = {"<http://www.infarmed.pt/Substância_Activa>", "<http://www.infomed.pt/Nome_Genérico>"};

			if(!checkBoxes.isEmpty()){
				
				if(chosenSources.size() > 1){
					
					for(JCheckBox jcb: checkBoxes){
						
						for (int i = 0; i < criteriaPanel.getComponentCount(); i++) {
							if (criteriaPanel.getComponent(i) == jcb){
								tf = (JTextField) criteriaPanel.getComponent(i+1);
								break;
							}
						}
						
						if(jcb.getText().contentEquals("Nome do Medicamento")){
							searchCriteria.put(byName[0], tf.getText());
							searchCriteria.put(byName[1], tf.getText());
						}
						if(jcb.getText().contentEquals("Substância Activa")){
							searchCriteria.put(bySubstance[0], tf.getText());
							searchCriteria.put(bySubstance[1], tf.getText());
						}				
					}
				}				
				else{
					
					for(JCheckBox jcb: checkBoxes){
						
						for (int i = 0; i < criteriaPanel.getComponentCount(); i++) {
							if (criteriaPanel.getComponent(i) == jcb){
								tf = (JTextField) criteriaPanel.getComponent(i+1);
								break;
							}
						}
						searchCriteria.put(jcb.getText(), tf.getText());				
					}
				}
				
				searchPage = new SearchPage(frame, swe, card, contentPanel, chosenSources, searchCriteria);
				pageSearch = searchPage.getPage();
				pageSearch.setName("pageSearch");
				
				contentPanel.add(pageSearch, "pageSearch");
				card.show(contentPanel, "pageSearch");
				
			}
			else{
				JOptionPane.showMessageDialog(frame, "You have to select at least a property and give it a value.",
						"Attention!", JOptionPane.WARNING_MESSAGE);
			}

		}
	}

}
