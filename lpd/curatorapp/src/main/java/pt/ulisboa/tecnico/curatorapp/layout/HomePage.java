package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pt.ulisboa.tecnico.core.SemanticWebEngine;
import pt.ulisboa.tecnico.curatorapp.layout.InstancesPage;
import pt.ulisboa.tecnico.curatorapp.layout.PropertyValuesPage;

public class HomePage {
	
	private SemanticWebEngine swe;
	private InstancesPage instPage;
	private MappingPage mappingPage;
//	private SecondPage secondPage;
	private PropertyValuesPage propsPage;

	private CardLayout card;
	private String className;
	private JFrame frame;

	private JSplitPane page1;
	private JSplitPane pageInstances;
	private JSplitPane pageRules;
//	private JSplitPane page2;
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
	private JList<String> propsList;
	
	private ArrayList<String> sources;
	private DefaultListModel<String> propsListModel;
	private HashMap<String, JList<String>> listsByTab;
	
	public HomePage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		
		page1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sourceInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		properties = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		instances = new JPanel();
		propsPanel = new JPanel();
		nextPanel = new JPanel();
		tabbedPane = new JTabbedPane();
		propsScrollPane = new JScrollPane();

		infoTitle = new JLabel("Sources' Classes:");
		instancesTitle = new JLabel("Number of instances: ");
		propertiesTitle = new JLabel("Properties:");
		show = new JButton("Instances ");
		next = new JButton("Next ");
		
		sources = swe.getSources();

		this.swe = swe;
		card = cl;
		contentPanel = content;
		frame = gui;
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

		show.setIcon(new ImageIcon("..\\src\\main\\resources\\listing16px.png"));
		show.setVerticalTextPosition(SwingConstants.CENTER);
	    show.setHorizontalTextPosition(SwingConstants.LEFT);
		show.addActionListener(new instListener());
		
		next.setIcon(new ImageIcon("..\\src\\main\\resources\\right arrow16px.png"));
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

		String tab, sourceName;
		String[] sSplit;
		ArrayList<String> result = null;
		selectionListener sl = new selectionListener();
		listsByTab = new HashMap<String, JList<String>>();
		
		HashMap<String, JPanel> panelBySource = new HashMap<String, JPanel>();
		HashMap<String, JScrollPane> scrollBySource = new HashMap<String, JScrollPane>();
		HashMap<String, DefaultListModel<String>> listModelBySource = new HashMap<String, DefaultListModel<String>>();
		
		JPanel panel;
		JScrollPane scroll;
		JList<String> tabList;
		DefaultListModel<String> list;
		
		for(String source: sources){			
			panelBySource.put(source, new JPanel());
			scrollBySource.put(source, new JScrollPane());
			listModelBySource.put(source, new DefaultListModel<String>());
			
			panel = panelBySource.get(source);
			panel.setLayout(new BorderLayout());
			panel.setPreferredSize(new Dimension(400, 150));
			
			list = listModelBySource.get(source);
			result = swe.showSourceClasses(source.toLowerCase());

			if(result != null){
				for(String s: result){
					list.addElement(s);
				}
			}
			
			listsByTab.put(source, new JList<String>(list));
			tabList = listsByTab.get(source);
			
			tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tabList.addListSelectionListener(sl);


			scroll = scrollBySource.get(source);
			scroll.setViewportView(tabList);
			panel.add(scroll);
			
			sSplit = source.split("\\.");
			sourceName = sSplit[1];
			tab = sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1);
			tabbedPane.addTab(tab, panel);
		}

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
			
			ArrayList<String> classProps;
			JList <String> list;
			String classInstances = "";
			
			for(String source: sources){
				list = listsByTab.get(source);
				if(!list.isSelectionEmpty()){
					className = list.getSelectedValue();
					className = className.substring(2, className.length()-2);
				}
			}

			if(className != ""){
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
			}
			
			for(String source: sources){
				list = listsByTab.get(source);
				list.clearSelection();
			}

		}		
	}

	private class propsSelectListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			if(!propsList.isSelectionEmpty()){
				
				String prop = propsList.getSelectedValue();
				if(prop != ""){
					propsPage = new PropertyValuesPage(swe, card, contentPanel, className, prop);
					pagePropValues = propsPage.getPage();
					pagePropValues.setName("pageProps");
					
					contentPanel.add(pagePropValues, "pageProps");
					card.show(contentPanel, "pageProps");
				}
				
				propsList.clearSelection();
			}
		}
	}

	private class instListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {

			if(className != ""){

				instPage = new InstancesPage(swe, className, card, contentPanel);
				pageInstances = instPage.getPage();
				pageInstances.setName("pageInst");

				contentPanel.add(pageInstances, "pageInst");
				card.show(contentPanel, "pageInst");
			}
			else{
				JOptionPane.showMessageDialog(frame, "You have to select a class first.", "Attention!", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private class nextListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			
			mappingPage = new MappingPage(frame, swe, card, contentPanel);
			pageRules = mappingPage.getPage();
			pageRules.setName("pageRules");
			
			contentPanel.add(pageRules, "pageRules");
			card.show(contentPanel, "pageRules");
		}
	}

}
