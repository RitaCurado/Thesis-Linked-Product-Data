package layout;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import lpd.SemanticWebEngine;

public class FirstPage {

	private SemanticWebEngine swe;
	private String className;

	private JSplitPane page1;
	private JTabbedPane tabbedPane;

	private JSplitPane sourceInfo;
	private JSplitPane properties;
	private JSplitPane upPanel;
	private JSplitPane downPanel;

	private JPanel instances;
	private JPanel propsPanel;
	private JPanel nextPanel;

	private JLabel infoTitle;
	private JLabel instancesTitle;
	private JLabel propertiesTitle;

	private JButton show;
	private JButton next;

	private JTextArea propertiesText;
	private JScrollPane propsScrollPane;

	private JList<String> infarList;
	private JList<String> infoList;

	public FirstPage(SemanticWebEngine swe, JButton instancesButton){
		page1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sourceInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		properties = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		instances = new JPanel();
		propsPanel = new JPanel();
		nextPanel = new JPanel();
		tabbedPane = new JTabbedPane();
		propertiesText = new JTextArea();
		propsScrollPane = new JScrollPane();

		infoTitle = new JLabel("Sources' Classes:");
		instancesTitle = new JLabel("Number of instances: ");
		propertiesTitle = new JLabel("Properties:");
		show = instancesButton;
		next = new JButton("Next >");

		this.swe = swe;
		className = "";

		createPage();
	}

	public JSplitPane getPage(){
		return page1;
	}

	public String getClassName(){
		return className;
	}

	public void createPage(){

		propertiesText.setEditable(false);

		createTabbedPane();

		sourceInfo.add(infoTitle);
		sourceInfo.add(tabbedPane);
		sourceInfo.setDividerSize(0);

		instances.setLayout(new GridLayout(1, 5));
		instances.add(instancesTitle);
		instances.add(new JLabel());
		instances.add(new JLabel());
		instances.add(new JLabel());
		instances.add(show);

		propsPanel.setLayout(new BorderLayout());
		propsPanel.setPreferredSize(new Dimension(400, 100));

		propsScrollPane.setViewportView(propertiesText);
		propsPanel.add(propsScrollPane);

		properties.add(propertiesTitle);
		properties.add(propsPanel);
		properties.setDividerSize(0);

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

	public void createTabbedPane(){

		String result = null;
		String[] classes;
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
		infarPanel.setPreferredSize(new Dimension(400, 100));

		infarListModel = new DefaultListModel<String>();

		try {
			result = swe.showSourceClasses("infarmed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			classes = result.split("\\r?\\n");
			for(int i=3; i < classes.length - 1; i++){
				infarListModel.addElement(classes[i]);
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
		infoPanel.setPreferredSize(new Dimension(400, 100));
		infoListModel = new DefaultListModel<String>();

		try {
			result = swe.showSourceClasses("infomed");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(result != null){
			classes = result.split("\\r?\\n");
			for(int i=3; i < classes.length - 1; i++){
				infoListModel.addElement(classes[i]);
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

	}

	public class selectionListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			//String className = "";
			String classProps = "";
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
					propertiesText.setText("");
					propertiesText.append(classProps);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			infarList.clearSelection();
			infoList.clearSelection();
		}		
	}

}
