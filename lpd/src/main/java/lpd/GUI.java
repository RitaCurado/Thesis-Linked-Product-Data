package lpd;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import layout.FirstPage;
import layout.InstancesPage;
import layout.SearchPage;
import layout.SecondPage;

public class GUI extends JFrame{

	private static final long serialVersionUID = 1L;

	private CardLayout cl;
	private JPanel contentPanel;
	private JSplitPane page1, pageInstances, page2, pageSearch;

	private JButton instances;
	private JButton backButton;
	private JButton nextButton;
	
	private String className;

	private FirstPage firstPage;
	private SecondPage secondPage;
	private InstancesPage instPage;
	private SearchPage searchPage;
	
	private SemanticWebEngine swe;
	private String visibleComponent;

	public GUI(){
		super("Advanced Search App");

		swe = new SemanticWebEngine();
		cl = new CardLayout();
		contentPanel = new JPanel();

		instances = new JButton("Instances");
		instances.addActionListener(new instancesButton());

		backButton = new JButton("< Back");
		backButton.addActionListener(new backButton());

		firstPage = new FirstPage(swe, cl, contentPanel);
		page1 = firstPage.getPage();

		contentPanel.setLayout(cl);
		contentPanel.add(page1, "page1");
		

		this.setContentPane(contentPanel);
		cl.show(contentPanel, "page1");
		visibleComponent = "page1";




	}

	public class instancesButton implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {

//			className = firstPage.getClassName();
//
//			instPage = new InstancesPage(swe, className, backButton);
//			pageInstances = instPage.getPage();
//			contentPanel.add(pageInstances, "pageInst");
//
//			cl.show(contentPanel, "pageInst");
//			visibleComponent = "pageInstances";
		}
	}

	public class backButton implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			
			if(visibleComponent.equals("pageInstances") || visibleComponent.equals("page2")){
				cl.show(contentPanel, "page1");
				visibleComponent = "page1";
			}
			
			if(visibleComponent.equals("pageSearch")){
				cl.show(contentPanel, "page2");
				visibleComponent = "page2";
			}
		}
	}
	


	public static void main(String[] args){
		GUI g = new GUI();
		g.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		g.setSize(800, 362);
		g.setVisible(true);
		g.setResizable(true);
	}

}