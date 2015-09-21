package lpd;

import javax.swing.*;

import java.awt.*;

import layout.FirstPage;

public class GUI extends JFrame{

	private static final long serialVersionUID = 1L;

	private CardLayout cl;
	private JPanel contentPanel;
	private JSplitPane page1;

	private FirstPage firstPage;
	
	private SemanticWebEngine swe;

	public GUI(){
		super("Advanced Search App");

		swe = new SemanticWebEngine();
		cl = new CardLayout();
		contentPanel = new JPanel();

		firstPage = new FirstPage(swe, cl, contentPanel);
		page1 = firstPage.getPage();

		contentPanel.setLayout(cl);
		contentPanel.add(page1, "page1");

		this.setContentPane(contentPanel);
		cl.show(contentPanel, "page1");
	}

	public static void main(String[] args){
		GUI g = new GUI();
		g.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		g.setSize(800, 362);
		g.setVisible(true);
		g.setResizable(true);
	}

}