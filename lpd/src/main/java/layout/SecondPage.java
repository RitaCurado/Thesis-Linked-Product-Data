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
	private JLabel from;

	private JTextField drugName;
	private JTextField activeSubsName;

	private JButton backButton;
	private JButton nextButton;

	private JComboBox<String> sourcesList;

	private ArrayList<JCheckBox> checkBoxes;
	private String[] sources = {"Infarmed", "Infomed"};

	private CardLayout card;
	private JPanel contentPanel;
	private JPanel criteriaPanel;
	private SemanticWebEngine swe;

	public SecondPage(SemanticWebEngine swe, CardLayout cl, JPanel cotent){
		page2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		title = new JLabel("Search Criteria");
		title.setFont(new Font("Arial", Font.BOLD, 15));

		from = new JLabel("From:");		
		sourcesList = new JComboBox<String>(sources);

		drugName = new JTextField();
		activeSubsName = new JTextField();

		backButton = new JButton(" Back");
		nextButton = new JButton("Next ");
		
		checkBoxes = new ArrayList<JCheckBox>();

		this.swe = swe;
		card = cl;
		contentPanel = cotent;

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
		JLabel hint = new JLabel("Choose the search criteria from one of the sources");

		hint.setFont(new Font("Arial", Font.PLAIN, 11));
		hint.setForeground(Color.LIGHT_GRAY);

		upPanel.setLayout(new GridLayout(3, 1));
		downPanel.setLayout(new GridLayout(1, 6));

		sourcePanel.setLayout(new GridLayout(1, 4));
		criteriaPanel.setLayout(new GridLayout(0, 2));
		criteriaPanel.setPreferredSize(new Dimension(400, 325));

		sourcesList.addActionListener(new comboListListener());
		sourcesList.setSelectedIndex(0);

		backButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("return16px.png")));
		backButton.addActionListener(new backListener());
		
		nextButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("right arrow16px.png")));
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
	    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		nextButton.addActionListener(new nextListener());

		sourcePanel.add(new JLabel());
		sourcePanel.add(from);
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
			JCheckBox checkB;
			JTextField tf;

			try {
				classes = swe.showSourceClasses(source.toLowerCase());
				props = swe.showClassProperties(classes.get(0));

				criteriaPanel.removeAll();
				criteriaPanel.revalidate();

				for(String p: props){
					checkB = new JCheckBox(p);
					checkB.addActionListener(cl);
					tf = new JTextField();
					tf.setEditable(false);

					criteriaPanel.add(checkB);
					criteriaPanel.add(tf);
				}

				criteriaPanel.revalidate();

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private class checkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			JTextField tf = null;
			
			System.out.println(cb.getText());

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

			HashMap<String, String> searchCriteria = new HashMap<String, String>();
			JTextField tf = null;

			for(JCheckBox jcb: checkBoxes){

				for (int i = 0; i < criteriaPanel.getComponentCount(); i++) {
					if (criteriaPanel.getComponent(i) == jcb){
						tf = (JTextField) criteriaPanel.getComponent(i+1);
						break;
					}
				}

				searchCriteria.put(jcb.getText(), tf.getText());				
			}

			searchPage = new SearchPage(swe, card, contentPanel, searchCriteria);
			pageSearch = searchPage.getPage();
			pageSearch.setName("pageSearch");

			contentPanel.add(pageSearch, "pageSearch");
			card.show(contentPanel, "pageSearch");

		}
	}

}
