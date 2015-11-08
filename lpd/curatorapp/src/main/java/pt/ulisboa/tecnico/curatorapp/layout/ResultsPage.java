package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

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
		
		JButton homepage = new JButton(" Homepage");
		
		GridBagConstraints gbc = new GridBagConstraints();
		buttonPanel.setLayout(new GridBagLayout());
		
		
		homepage.setIcon(new ImageIcon("..\\src\\main\\resources\\home168.png"));
		homepage.addActionListener(new HomeListener());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		buttonPanel.add(new JLabel(""), gbc);

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
	
	private class HomeListener implements ActionListener{

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