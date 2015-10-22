package pt.ulisboa.tecnico.userapp.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class ResultsPage {

	private JSplitPane resultPage;
	private CardLayout card;
	private JPanel contentPanel;
	private String queryResult;
	
	public ResultsPage(CardLayout cl, JPanel content, String result){
		
		card = cl;
		contentPanel = content;
		queryResult = result;
		resultPage = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return resultPage;
	}
	
	private void createPage(){
		
		JTextArea results = new JTextArea();
		JScrollPane scrollPane = new JScrollPane();
		JPanel buttonPanel = new JPanel();
		
		JButton backButton = new JButton(" Back");
		JButton homepage = new JButton(" Homepage");
		
		GridBagConstraints gbc = new GridBagConstraints();
		buttonPanel.setLayout(new GridBagLayout());
		
		
		homepage.setIcon(new ImageIcon(getClass().getClassLoader().getResource("home168.png")));
		homepage.addActionListener(new homeListener());
		
		backButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("return1.png")));
		backButton.addActionListener(new backListener());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		buttonPanel.add(backButton, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		buttonPanel.add(homepage, gbc);
		
		results.setText(queryResult);
		scrollPane.setViewportView(results);
		scrollPane.setPreferredSize(new Dimension(400, 390));
		
		resultPage.add(scrollPane);
		resultPage.add(buttonPanel);
		resultPage.setDividerSize(1);
	}
	
	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			contentPanel.remove(resultPage);
			contentPanel.revalidate();
			card.show(contentPanel, "pageSearch");			
		}
	}
	
	private class homeListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Component[] allComps = contentPanel.getComponents();
			
			for(int i=1; i < allComps.length; i++){
				contentPanel.remove(allComps[i]);
			}
			
			contentPanel.revalidate();			
			card.show(contentPanel, "page1");			
		}
	}
}
