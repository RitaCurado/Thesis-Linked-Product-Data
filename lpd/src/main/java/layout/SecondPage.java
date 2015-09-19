package layout;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class SecondPage {
	
	private JSplitPane page2;
	private JSplitPane pageSearch;	
	private SearchPage searchPage;
	
	private JLabel title;
	private JRadioButton drugTitle;
	private JRadioButton activeSubsTitle;
	private JTextField drugName;
	private JTextField activeSubsName;
	private JButton backButton;
	private JButton nextButton;
	private String searchCriteria;
	
	private CardLayout card;
	private JPanel contentPanel;
	
	public SecondPage(CardLayout cl, JPanel cotent){
		page2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		title = new JLabel("Search Criteria");
		drugTitle = new JRadioButton("Drug name: ");
		activeSubsTitle = new JRadioButton("Active Substance: ");
		drugName = new JTextField();
		activeSubsName = new JTextField();
		backButton = new JButton("< Back");
		nextButton = new JButton("Next >");
		searchCriteria = "";
		
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
	
	public String getSearchCriteria(){
		return searchCriteria;
	}
	
	public void createPage(){
		
		JPanel upPanel = new JPanel();
		JPanel downPanel = new JPanel();
		JPanel titlePane = new JPanel();
		
		upPanel.setLayout(new GridLayout(3, 4));
		downPanel.setLayout(new GridLayout(1, 5));
		titlePane.setLayout(new GridLayout(4, 1));
		
		radioListener listener = new radioListener();
		
		drugTitle.addActionListener(listener);
		activeSubsTitle.addActionListener(listener);
		
		backButton.addActionListener(new backListener());
		nextButton.addActionListener(new nextListener());
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(drugTitle);
		bg.add(activeSubsTitle);
		
		upPanel.add(new JLabel());
		upPanel.add(drugTitle);
		upPanel.add(drugName);
		upPanel.add(new JLabel());
		
		upPanel.add(new JLabel());
		upPanel.add(new JLabel());
		upPanel.add(new JLabel());
		upPanel.add(new JLabel());
		
		upPanel.add(new JLabel());
		upPanel.add(activeSubsTitle);
		upPanel.add(activeSubsName);
		upPanel.add(new JLabel());
		
		titlePane.add(title);
		titlePane.add(upPanel);
		titlePane.add(new JLabel());
		titlePane.add(new JLabel());
		
		downPanel.add(backButton);
		downPanel.add(new JLabel());
		downPanel.add(new JLabel());
		downPanel.add(new JLabel());
		downPanel.add(nextButton);
		
		page2.add(titlePane);
		page2.add(downPanel);
		page2.setDividerSize(0);
	}
	
	private class radioListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton rb = (JRadioButton) e.getSource();
			
			if(rb.equals(drugTitle)){
				drugName.setEnabled(true);
				activeSubsName.setEnabled(false);
				searchCriteria = "Name";
			}
			if(rb.equals(activeSubsTitle)){
				activeSubsName.setEnabled(true);
				drugName.setEnabled(false);
				searchCriteria = "Substance";
			}
		}
	}
	
	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			card.show(contentPanel, "page1");
		}
	}
	
	private class nextListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			searchPage = new SearchPage(card, contentPanel);
			pageSearch = searchPage.getPage();
			
			contentPanel.add(pageSearch, "pageSearch");
			card.show(contentPanel, "pageSearch");
			
		}
	}

}
