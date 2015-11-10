package pt.ulisboa.tecnico.curatorapp.layout;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

public class ResultsPage {

	private JSplitPane resultPage;
	private CardLayout card;
	private JPanel contentPanel;
	private String queryResult;
	
	public ResultsPage(CardLayout cl, JPanel content, Model result){
		
		card = cl;
		contentPanel = content;
		resultPage = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		String q, className;
		String[] spltResult;
		Query query;
		QueryExecution qexec;
		ResultSet results;
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		
		q = "SELECT DISTINCT ?class\n"
				+ "WHERE {"
				+ "?class a <http://www.w3.org/2000/01/rdf-schema#Class> .}";
		
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, result);
		results = qexec.execSelect();		
		ResultSetFormatter.out(baos, results, query);
		qexec.close();
		
		queryResult = baos.toString();
		
		spltResult = queryResult.split("\\r?\\n");
		className = spltResult[3];
		className = className.replace("|", "");
		className = className.replace(" ", "");
		className = className.replace("<", "");
		className = className.replace(">", "");
		
		if(className.contains("+"))
			className = className.replace("+", "\\\\+");
		
		q = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ "{"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
				+ "}"
				+ " UNION "
				+ "{"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?o ."
				+ " ?s <http://www.w3.org/2000/01/rdf-schema#range> ?o ."
				+ " ?s a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?s <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
				+ "} "
				+ " FILTER (regex(str(?cl), '" + className + "'))}";
		
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, result);
		results = qexec.execSelect();		
		ResultSetFormatter.out(baos, results, query);

		queryResult = baos.toString();
		queryResult = queryResult.replace("-", "_");
		queryResult = queryResult.replace("|", "");

		qexec.close();
		
		this.createPage();
	}
	
	public JSplitPane getPage(){
		return resultPage;
	}
	
	private void createPage(){
		
		JTextArea results = new JTextArea();
		JScrollPane scrollPane = new JScrollPane();
		JPanel buttonPanel = new JPanel();
		
		JButton homepage = new JButton(" Homepage");
		
		GridBagConstraints gbc = new GridBagConstraints();
		buttonPanel.setLayout(new GridBagLayout());
		
		
		homepage.setIcon(new ImageIcon("..\\src\\main\\resources\\home168.png"));
		homepage.addActionListener(new HomeListener());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		buttonPanel.add(new JLabel(""), gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		buttonPanel.add(homepage, gbc);
		
		results.setText(queryResult);
		scrollPane.setViewportView(results);
		scrollPane.setPreferredSize(new Dimension(400, 390));
		
		resultPage.add(scrollPane);
		resultPage.add(buttonPanel);
		resultPage.setDividerSize(1);
	}
	
	private class HomeListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Component[] allComps = contentPanel.getComponents();
			
			for(int i=1; i < allComps.length; i++){
				contentPanel.remove(allComps[i]);
			}
			
			contentPanel.revalidate();			
			card.show(contentPanel, "page1");			
		}
	}
}
