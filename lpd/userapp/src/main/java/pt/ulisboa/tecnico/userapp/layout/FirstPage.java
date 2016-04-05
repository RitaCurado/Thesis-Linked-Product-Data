package pt.ulisboa.tecnico.userapp.layout;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class FirstPage {

	private SemanticWebEngine swe;
	
	private CardLayout card;
	private String className;
	private JFrame frame;

	private JPanel contentPanel;
	private JSplitPane page1;
	private JSplitPane properties;

	private JLabel instancesTitle;
	private JTabbedPane tabbedPane;
	private JList<String> propsList;
	
	private ArrayList<String> sources;
	private DefaultListModel<String> propsListModel;
	private HashMap<String, JList<String>> listsByTab;

	public FirstPage(JFrame gui, SemanticWebEngine swe, CardLayout cl, JPanel content){
		this.swe = swe;
		card = cl;
		contentPanel = content;
		frame = gui;

		className = "";

		page1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		properties = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tabbedPane = new JTabbedPane();
		
		sources = swe.getSources();
		instancesTitle = new JLabel("Number of instances: ");

		this.createPage();
	}

	public JSplitPane getPage(){
		return page1;
	}

	public String getClassName(){
		return className;
	}

	private void createPage(){
		JPanel instances, nextPanel;
		JSplitPane sourceInfo, upPanel, downPanel;
		JLabel infoTitle = new JLabel("Sources' Classes:");
		JButton show = new JButton("Instances ");
		JButton next = new JButton("Next ");
		JButton back = new JButton(" Back");
		
		instances = new JPanel();
		nextPanel = new JPanel();
		sourceInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
		downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		show.setBackground(new Color(198, 218, 230));
		show.setIcon(new ImageIcon("..\\src\\main\\resources\\listing16px.png"));
		show.setVerticalTextPosition(SwingConstants.CENTER);
	    show.setHorizontalTextPosition(SwingConstants.LEFT);
		show.addActionListener(new InstListener());
		
		next.setBackground(new Color(198, 218, 230));
		next.setIcon(new ImageIcon("..\\src\\main\\resources\\right arrow16px.png"));
		next.setVerticalTextPosition(SwingConstants.CENTER);
	    next.setHorizontalTextPosition(SwingConstants.LEFT);
		next.addActionListener(new NextListener());
		
		back.setBackground(new Color(198, 218, 230));
		back.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		back.addActionListener(new BackListener());

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
		nextPanel.add(back);
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
		SelectionListener sl = new SelectionListener();
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
			result = swe.showSourceClasses(source.toLowerCase(), "beginning");

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
			
			String[] sSplit = source.split("\\.");
			String sourceName = sSplit[1];
			String tab = sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1);
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
		JPanel propsPanel = new JPanel();
		JLabel propertiesTitle = new JLabel("Properties:");
		JScrollPane propsScrollPane = new JScrollPane();
		
		propsListModel = new DefaultListModel<String>();
		propsList = new JList<String>(propsListModel);

		propsList.addListSelectionListener(new PropsSelectListener());

		propsPanel.setLayout(new BorderLayout());
		propsPanel.setPreferredSize(new Dimension(400, 150));

		propsScrollPane.setViewportView(propsList);
		propsPanel.add(propsScrollPane);

		properties.add(propertiesTitle);
		properties.add(propsPanel);
		properties.setDividerSize(0);
	}
	
	private class SelectionListener implements ListSelectionListener{

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
				classProps = swe.showClassProperties(className, "beginning");
				classInstances = swe.countClassInstances(className, "beginning");
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
	
	private class PropsSelectListener implements ListSelectionListener{

		private PropertyValuesPage propsPage;
		private JSplitPane pagePropValues;
		
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

	private class InstListener implements ActionListener{
		
		private InstancesPage instPage;
		private JSplitPane pageInstances;

		@Override
		public void actionPerformed(ActionEvent event) {

			if(className != ""){

				instPage = new InstancesPage(swe, frame, className, card, contentPanel);
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

	private class NextListener implements ActionListener{

		private ChooseMapRulePage choosePage;
		private JSplitPane pageChoose;
		
		@Override
		public void actionPerformed(ActionEvent event) {

			choosePage = new ChooseMapRulePage(frame, swe, card, contentPanel);
			pageChoose = choosePage.getPage();
			pageChoose.setName("pageChoose");

			contentPanel.add(pageChoose, "pageChoose");
			card.show(contentPanel, "pageChoose");
		}
	}

	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			contentPanel.remove(page1);
			contentPanel.revalidate();
			card.show(contentPanel, "pageInfo");
		}
	}
	
}
