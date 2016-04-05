package pt.ulisboa.tecnico.userapp.layout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class InstancesPage {
	
	private JSplitPane pageInstances;
	
	private SemanticWebEngine swe;
	private CardLayout card;
	private JFrame frame;
	private JPanel contentPanel;
	private String className;

	public InstancesPage(SemanticWebEngine swe, JFrame gui, String className, CardLayout cl, JPanel content){
		
		card = cl;
		frame = gui;
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
		JLabel source = new JLabel("Information source: " + swe.getPropertySource(className, false));
		JLabel classNameLabel = new JLabel("Class name: " + className);
		JLabel numInstances = new JLabel("Number of instances: " + swe.countClassInstances(className, "beginning"));
		
		JButton labelButton = new JButton("Label");
		JButton backButton = new JButton(" Back");
		JTextArea output = new JTextArea();
		output.setFont(new Font("Courier New", Font.PLAIN, 13));
		
		JPanel sourceInfo = new JPanel();
		JPanel sourceInfo2 = new JPanel();
		JPanel instPanel = new JPanel();
		JPanel backPanel = new JPanel();

		JScrollPane scrollPane = new JScrollPane();
		JSplitPane upPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane downPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		GridBagConstraints gbc = new GridBagConstraints();

		output.setText(swe.selectAllInfo(className, "beginning"));
		
		backButton.setBackground(new Color(198, 218, 230));
		backButton.setIcon(new ImageIcon("..\\src\\main\\resources\\return16px.png"));
		backButton.addActionListener(new BackListener());

		sourceInfo.setLayout(new GridLayout(1, 2));
		sourceInfo.add(source);
		sourceInfo.add(classNameLabel);
		
		sourceInfo2.setLayout(new GridBagLayout());
		
		labelButton.setBackground(new Color(198, 218, 230));
		labelButton.addActionListener(new LabelListener());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.6;
		sourceInfo2.add(numInstances, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		sourceInfo2.add(labelButton, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 1;
		sourceInfo2.add(new JLabel(""), gbc);

		upPanel.add(sourceInfo);
		upPanel.add(sourceInfo2);
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
	
	private class LabelListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String label;
			HashMap<String, Integer> sid = swe.getSourcesId();
			
			label = "Column number - property source \n\n";
			
			for(String key: sid.keySet()){
				label += sid.get(key) + " -  " + key + "\n";
			}
			
			JOptionPane.showMessageDialog(frame, label);
			
		}
	}
	
	private class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent event) {
			contentPanel.remove(pageInstances);
			contentPanel.revalidate();
			card.show(contentPanel, "page1");
		}
	}

}
