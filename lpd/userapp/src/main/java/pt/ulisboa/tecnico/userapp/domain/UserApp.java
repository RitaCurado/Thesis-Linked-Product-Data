package pt.ulisboa.tecnico.userapp.domain;

import java.awt.*;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;
import pt.ulisboa.tecnico.userapp.layout.InfoPage;


public class UserApp extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private CardLayout cl;
	private JPanel contentPanel;
	
	private JSplitPane pageInfo;
	private InfoPage infoPage;	
	private SemanticWebEngine swe;
	
	public UserApp(){
		super("Advanced Search App");
		swe = new SemanticWebEngine("user");
		cl = new CardLayout();
		contentPanel = new JPanel();

		infoPage = new InfoPage(this, swe, cl, contentPanel);
		pageInfo = infoPage.getPage();
		pageInfo.setName("pageInfo");

		contentPanel.setLayout(cl);
		contentPanel.add(pageInfo, "pageInfo");

		this.setContentPane(contentPanel);
		cl.show(contentPanel, "pageInfo");
	}

	public static void main(String[] args){
	
		UserApp ui = new UserApp();
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setSize(890, 480);
		ui.setVisible(true);
		ui.setResizable(true);
    }
}
