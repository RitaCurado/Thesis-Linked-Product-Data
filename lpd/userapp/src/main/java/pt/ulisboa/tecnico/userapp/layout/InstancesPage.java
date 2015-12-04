package pt.ulisboa.tecnico.userapp.layout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class InstancesPage {
	
	private JSplitPane pageInstances;
	
	private SemanticWebEngine swe;
	private CardLayout card;
	private JPanel contentPanel;
	private String className;

	public InstancesPage(SemanticWebEngine swe, String className, CardLayout cl, JPanel content){
		
		card = cl;
		contentPanel = content;
		this.swe = swe;
		this.className = className;
		
		pageInstances = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		this.createPage();
	}

	public JSplitPane getPage(){
		return pageInstances;
	}

	private void createPage(){
		JLabel source = new JLabel("Information source: " + swe.getPropertySource(className));
		JLabel classNameLabel = new JLabel("Class name: " + className);
		JLabel numInstances = new JLabel("Number of instances: " + swe.countClassInstances(className));
		
		JButton backButton = new JButton(" Back");
		JTextArea output = new JTextArea();
		
		JPanel sourceInfo = new JPanel();
		JPanel instPanel = new JPanel();
		JPanel backPanel = new JPanel();

		JScrollPane scrollPane = new JScrollPane();
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		output.setText(swe.selectAllInfo(className));
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new backListener());

		sourceInfo.setLayout(new GridLayout(1, 2));
		sourceInfo.add(source);
		sourceInfo.add(classNameLabel);

		upPanel.add(sourceInfo);
		upPanel.add(numInstances);
		upPanel.setDividerSize(1);

		instPanel.setLayout(new BorderLayout());
		instPanel.setPreferredSize(new Dimension(400, 362));		
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
	
	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(pageInstances);
			contentPanel.revalidate();
			card.show(contentPanel, "page1");
		}
	}

}
