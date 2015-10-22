package pt.ulisboa.tecnico.userapp.domain;

import java.awt.*;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;
import pt.ulisboa.tecnico.userapp.layout.FirstPage;


public class UserApp extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private CardLayout cl;
	private JPanel contentPanel;
	
	private JSplitPane page1;
	private FirstPage firstPage;	
	private SemanticWebEngine swe;
	
	public UserApp(){
		super("Advanced Search App");
		swe = new SemanticWebEngine();
		cl = new CardLayout();
		contentPanel = new JPanel();

		firstPage = new FirstPage(this, swe, cl, contentPanel);
		page1 = firstPage.getPage();
		page1.setName("page1");

		contentPanel.setLayout(cl);
		contentPanel.add(page1, "page1");

		this.setContentPane(contentPanel);
		cl.show(contentPanel, "page1");
	}

	public static void main(String[] args){
	
		UserApp ui = new UserApp();
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setSize(800, 480);
		ui.setVisible(true);
		ui.setResizable(true);
    }
}
