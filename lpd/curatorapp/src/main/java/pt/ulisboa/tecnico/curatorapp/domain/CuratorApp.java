package pt.ulisboa.tecnico.curatorapp.domain;

import java.awt.*;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;
import pt.ulisboa.tecnico.curatorapp.layout.HomePage;

public class CuratorApp extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private CardLayout cl;
	private JPanel contentPanel;
	
	private SemanticWebEngine swe;
	
	private JSplitPane page1;
	private HomePage homePage;
	
	public CuratorApp(){
		super("Advanced Search App - Data Curator");
		swe = new SemanticWebEngine();
		cl = new CardLayout();
		contentPanel = new JPanel();
		
		homePage = new HomePage(this, swe, cl, contentPanel);
		page1 = homePage.getPage();
		page1.setName("page1");

		contentPanel.setLayout(cl);
		contentPanel.add(page1, "page1");

		this.setContentPane(contentPanel);
		cl.show(contentPanel, "page1");
	}
	
    public static void main(String[] args){
    	
    	CuratorApp ui = new CuratorApp();
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setSize(890, 480);
		ui.setVisible(true);
		ui.setResizable(true);
    }
}
