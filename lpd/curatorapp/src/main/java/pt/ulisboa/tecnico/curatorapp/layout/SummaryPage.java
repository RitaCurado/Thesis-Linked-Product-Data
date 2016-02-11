package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class SummaryPage {
	
	private CardLayout card;
	private JPanel contentPanel;
	private JFrame frame;
	private SemanticWebEngine swe;
	
	private JSplitPane pageSummary;

	public SummaryPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		frame = gui;
		card = cl;
		contentPanel = content;
		
		pageSummary = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageSummary;
	}
	
	public void createPage(){
		
		JPanel topPanel = new JPanel();
		JPanel buttonPanel = new JPanel(new GridLayout(1, 7));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new OkListener());
		
		topPanel.setPreferredSize(new Dimension(400, 395));
		
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(okButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		
		pageSummary.add(topPanel);
		pageSummary.add(buttonPanel);
		pageSummary.setDividerSize(1);
	}
	
	private class OkListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	}
}
