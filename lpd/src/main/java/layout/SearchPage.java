package layout;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchPage {
	
	private JSplitPane searchPage;
	private JButton backButton;
	private JButton nextButton;
	private CardLayout card;
	private JPanel contentPanel;
	
	public SearchPage(CardLayout cl, JPanel content){
		backButton = new JButton("Cancel");
		nextButton = new JButton("Search");
		searchPage = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		card = cl;
		contentPanel = content;
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return searchPage;
	}
	
	private void createPage(){
		
		backButton.setBackground(Color.RED);
		nextButton.setBackground(Color.GREEN);
		
		backButton.addActionListener(new backListener());
		
		searchPage.add(backButton);
		searchPage.add(nextButton);
	}
	
	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(searchPage);
			contentPanel.revalidate();
			card.show(contentPanel, "page2");
		}
	}

}
