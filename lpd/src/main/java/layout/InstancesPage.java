package layout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.*;

import lpd.SemanticWebEngine;

public class InstancesPage {
	
	private JLabel source;
	private JLabel classNameLabel;
	private JLabel numInstances;
	private JButton backButton;
	private JTextArea output;
	private JScrollPane scrollPane;
	private JSplitPane pageInstances;
	private SemanticWebEngine swe;
	private String className;
	
	public InstancesPage(SemanticWebEngine swe, String className, JButton back){
		
		pageInstances = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		source = new JLabel("Information source: " + swe.getPropertySource(className));
		classNameLabel = new JLabel("Class name: " + className);
		numInstances = new JLabel("Number of instances: " + swe.countClassInstances(className));
		output = new JTextArea();
		scrollPane = new JScrollPane();
		backButton = back;
		this.swe = swe;
		this.className = className;
		
		createPage();
	}
	
	public JSplitPane getPage(){
		return pageInstances;
	}
	
	public void createPage(){
		JPanel sourceInfo = new JPanel();
		JPanel instPanel = new JPanel();
		JPanel backPanel = new JPanel();
		
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		output.setText(swe.selectAllInfo(className));
		
		sourceInfo.setLayout(new GridLayout(1, 2));
		sourceInfo.add(source);
		sourceInfo.add(classNameLabel);
		
		upPanel.add(sourceInfo);
		upPanel.add(numInstances);
		upPanel.setDividerSize(1);
		
		instPanel.setLayout(new BorderLayout());
		instPanel.setPreferredSize(new Dimension(400, 250));		
		scrollPane.setViewportView(output);
		instPanel.add(scrollPane);
		
		backPanel.setLayout(new GridLayout(1, 6));
		backPanel.add(backButton);
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		backPanel.add(new JLabel());
		
		downPanel.add(instPanel);
		downPanel.add(backPanel);
		downPanel.setDividerSize(1);
		
		pageInstances.add(upPanel);
		pageInstances.add(downPanel);
		pageInstances.setDividerSize(1);
	}

}
