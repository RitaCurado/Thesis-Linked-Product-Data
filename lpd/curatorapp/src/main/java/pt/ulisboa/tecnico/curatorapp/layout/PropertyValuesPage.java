package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class PropertyValuesPage {
	
	private CardLayout card;
	private JPanel contentPanel;
	private SemanticWebEngine swe;
	private JLabel source;
	private JLabel classNameLabel;
	private JLabel propNameLabel;
	private String property;
	private JSplitPane pagePropValues;
	
	
	public PropertyValuesPage(SemanticWebEngine swe, CardLayout cl, JPanel content, String className, String prop) {
		card = cl;
		contentPanel = content;
		this.swe = swe;
		this.property = prop;
		
		pagePropValues = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		source = new JLabel("Information source: " + swe.getPropertySource(className));
		classNameLabel = new JLabel("Class name: " + className);
		propNameLabel = new JLabel("Property name: " + prop);
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return pagePropValues;
	}

	private void createPage(){
		String[] numValues;
		
		JTextArea output = new JTextArea();
		JButton backButton = new JButton(" Back");
		JLabel numInstances = new JLabel("Number of distinct values: ");
		JScrollPane scrollPane = new JScrollPane();
		
		JPanel sourceInfo, propInfo, instPanel, backPanel;
		JSplitPane upPanel, downPanel;
		
		output.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		sourceInfo = new JPanel();
		propInfo = new JPanel();
		instPanel = new JPanel();
		backPanel = new JPanel();
		
		upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		output.setText(swe.showPropertyValues(property, ""));
		numValues = output.getText().split("\\r?\\n");
		numInstances.setText("Number of distinct values: " + ((numValues.length)-4));
		
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new backListener());

		sourceInfo.setLayout(new GridLayout(1, 2));
		sourceInfo.add(source);
		sourceInfo.add(classNameLabel);
		
		propInfo.setLayout(new GridLayout(1, 2));
		propInfo.add(numInstances);
		propInfo.add(propNameLabel);

		upPanel.add(sourceInfo);
		upPanel.add(propInfo);
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

		pagePropValues.add(upPanel);
		pagePropValues.add(downPanel);
		pagePropValues.setDividerSize(1);
	}
	
	private class backListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(pagePropValues);
			contentPanel.revalidate();
			card.show(contentPanel, "page1");
		}
	}

}
