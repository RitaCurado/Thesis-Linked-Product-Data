package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class ConsultDB {
	
	private SemanticWebEngine swe;
	private CardLayout card;
	private JPanel contentPanel;
	
	private JSplitPane pageConsult;
	
	public ConsultDB(SemanticWebEngine swe, CardLayout cl, JPanel content){
		
		this.swe = swe;
		card = cl;
		contentPanel = content;
		
		pageConsult = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pageConsult;
	}
	
	private void createPage(){
		
		JTextArea output = new JTextArea("");
		JButton backButton = new JButton(" Back");
		JScrollPane scrollPane = new JScrollPane();
		
		JPanel backPanel = new JPanel();
		
		ArrayList<String> sources = swe.getSources();
		ArrayList<String> classSources;
		
		for(String s: sources){
			output.append("Source: " + s + "\n");
			
			classSources = swe.showSourceClasses(s);
			for(String cl: classSources){
				
				cl = cl.replace(" <", "");
				cl = cl.replace("> ", "");

				output.append("#: " + swe.countClassInstances(cl) + "\n");
				output.append(swe.selectAllInfo(cl));
				output.append("\n\n");
			}
		}
				
		scrollPane.setViewportView(output);
		scrollPane.setPreferredSize(new Dimension(400, 390));
		
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new BackListener());
		
		backPanel.setLayout(new GridLayout(1, 6));
		backPanel.add(backButton);
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		
		pageConsult.add(scrollPane);
		pageConsult.add(backPanel);
		pageConsult.setDividerSize(1);
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			contentPanel.remove(pageConsult);
			contentPanel.revalidate();
			card.show(contentPanel, "pageRules");
		}
	}

}
