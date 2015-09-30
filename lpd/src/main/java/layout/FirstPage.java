package layout;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

import lpd.SemanticWebEngine;

public class FirstPage {

	private SemanticWebEngine swe;
	private InstancesPage instPage;
	private SecondPage secondPage;
	private PropertyValuesPage propsPage;

	private CardLayout card;
	private String className;

	private JSplitPane page1;
	private JSplitPane pageInstances;
	private JSplitPane page2;
	private JSplitPane pagePropValues;

	private JTabbedPane tabbedPane;
	private JSplitPane sourceInfo;
	private JSplitPane properties;
	private JSplitPane upPanel;
	private JSplitPane downPanel;

	private JPanel instances;
	private JPanel propsPanel;
	private JPanel nextPanel;
	private JPanel contentPanel;

	private JLabel infoTitle;
	private JLabel instancesTitle;
	private JLabel propertiesTitle;

	private JButton show;
	private JButton next;

	private JScrollPane propsScrollPane;

	private JList<String> infarList;
	private JList<String> infoList;
	private JList<String> propsList;
	private DefaultListModel<String> propsListModel;

	public FirstPage(SemanticWebEngine swe, CardLayout cl, JPanel content){
		page1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sourceInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		properties = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		instances = new JPanel();
		propsPanel = new JPanel();
		nextPanel = new JPanel();
		tabbedPane = new JTabbedPane();
		//		propertiesText = new JTextArea();
		propsScrollPane = new JScrollPane();

		infoTitle = new JLabel("Sources' Classes:");
		instancesTitle = new JLabel("Number of instances: ");
		propertiesTitle = new JLabel("Properties:");
		show = new JButton("Instances ");
		next = new JButton("Next ");

		this.swe = swe;
		card = cl;
		contentPanel = content;
		className = "";

		this.createPage();
	}

	public JSplitPane getPage(){
		return page1;
	}

	public String getClassName(){
		return className;
	}

	private void createPage(){

		show.setIcon(new ImageIcon(getClass().getClassLoader().getResource("listing16px.png")));
		show.setVerticalTextPosition(SwingConstants.CENTER);
	    show.setHorizontalTextPosition(SwingConstants.LEFT);
		show.addActionListener(new instListener());
		
		next.setIcon(new ImageIcon(getClass().getClassLoader().getResource("right arrow16px.png")));
		next.setVerticalTextPosition(SwingConstants.CENTER);
	    next.setHorizontalTextPosition(SwingConstants.LEFT);
		next.addActionListener(new nextListener());

		this.createTabbedPane();

		sourceInfo.add(infoTitle);
		sourceInfo.add(tabbedPane);
		sourceInfo.setDividerSize(0);

		instances.setLayout(new GridLayout(1, 6));
		instances.add(instancesTitle);
		instances.add(new JLabel());
		instances.add(new JLabel());
		instances.add(new JLabel());
		instances.add(new JLabel());
		instances.add(show);
		instances.setPreferredSize(new Dimension(400, 35));

		this.createPropsPane();

		nextPanel.setLayout(new GridLayout(1, 6));
		nextPanel.add(new JLabel());
		nextPanel.add(new JLabel());
		nextPanel.add(new JLabel());
		nextPanel.add(new JLabel());
		nextPanel.add(new JLabel());
		nextPanel.add(next);

		upPanel.add(sourceInfo);
		upPanel.add(instances);
		upPanel.setDividerSize(0);

		downPanel.add(properties);
		downPanel.add(nextPanel);
		downPanel.setDividerSize(0);

		page1.add(upPanel);
		page1.add(downPanel);
		page1.setDividerSize(0);
	}

	private void createTabbedPane(){

		ArrayList<String> result = null;
		JPanel infarPanel;
		JPanel infoPanel;
		JScrollPane infarScrollPane;
		JScrollPane infoScrollPane;
		DefaultListModel<String> infarListModel;
		DefaultListModel<String> infoListModel;
		selectionListener sl;

		sl = new selectionListener();
		infarPanel = new JPanel();
		infarPanel.setLayout(new BorderLayout());
		infarPanel.setPreferredSize(new Dimension(400, 150));

		infarListModel = new DefaultListModel<String>();

		try {
			result = swe.showSourceClasses("infarmed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			for(String s: result){
				infarListModel.addElement(s);
			}
		}

		infarList = new JList<String>(infarListModel);
		infarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		infarList.addListSelectionListener(sl);


		infarScrollPane = new JScrollPane();
		infarScrollPane.setViewportView(infarList);
		infarPanel.add(infarScrollPane);

		//		------------------------------------------------------------

		result = null;
		infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.setPreferredSize(new Dimension(400, 150));
		infoListModel = new DefaultListModel<String>();

		try {
			result = swe.showSourceClasses("infomed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			for(String s: result){
				infoListModel.addElement(s);
			}
		}

		infoList = new JList<String>(infoListModel);
		infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		infoList.addListSelectionListener(sl);

		infoScrollPane = new JScrollPane();
		infoScrollPane.setViewportView(infoList);
		infoPanel.add(infoScrollPane);


		tabbedPane.addTab("Infarmed", infarPanel);
		tabbedPane.addTab("Infomed", infoPanel);
		tabbedPane.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent e) {
	        	propsListModel.removeAllElements();
				propsList.revalidate();
	        }
	    });

	}

	private void createPropsPane(){
		propsListModel = new DefaultListModel<String>();
		propsList = new JList<String>(propsListModel);

		propsList.addListSelectionListener(new propsSelectListener());

		propsPanel.setLayout(new BorderLayout());
		propsPanel.setPreferredSize(new Dimension(400, 150));

		propsScrollPane.setViewportView(propsList);
		propsPanel.add(propsScrollPane);

		properties.add(propertiesTitle);
		properties.add(propsPanel);
		properties.setDividerSize(0);
	}

	private class selectionListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			
			ArrayList<String> classProps = null;
			String classInstances = "";

			if(!infarList.isSelectionEmpty()){
				className = infarList.getSelectedValue();
			}

			if(!infoList.isSelectionEmpty()){
				className = infoList.getSelectedValue();
			}

			if(className != ""){
				try {
					classProps = swe.showClassProperties(className);
					classInstances = swe.countClassInstances(className);
					instancesTitle.setText("Number of instances: " + classInstances);
					propsListModel.removeAllElements();
					propsList.revalidate();

					if(classProps != null){
						for(String prop: classProps){
							propsListModel.addElement(prop);
						}
						propsList.revalidate();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			infarList.clearSelection();
			infoList.clearSelection();
		}		
	}

	private class propsSelectListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent e) {

			String prop = propsList.getSelectedValue();
			if(prop != ""){
				propsPage = new PropertyValuesPage(swe, card, contentPanel, className, prop);
				pagePropValues = propsPage.getPage();
				pagePropValues.setName("pageProps");

				contentPanel.add(pagePropValues, "pageProps");
				card.show(contentPanel, "pageProps");
			}

		}
	}

	private class instListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {

			if(className != null){

				instPage = new InstancesPage(swe, className, card, contentPanel);
				pageInstances = instPage.getPage();
				pageInstances.setName("pageInst");

				contentPanel.add(pageInstances, "pageInst");
				card.show(contentPanel, "pageInst");
			}
		}
	}

	private class nextListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {

			secondPage = new SecondPage(swe, card, contentPanel);
			page2 = secondPage.getPage();
			page2.setName("page2");

			contentPanel.add(page2, "page2");
			card.show(contentPanel, "page2");
		}
	}

}
