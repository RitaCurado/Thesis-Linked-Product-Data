package main;

import javax.swing.*;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class GUI extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private     JSplitPane  splitPaneV;
	private     JSplitPane  splitPaneH;
	private     JPanel      panel1;
	private     JPanel      panel2;
	private     JPanel      panel3;
	private		MedicineInfo medInfo;
	
	public MedicineInfo getMedicineInfo(){
		return medInfo;
	}

	public void createPanel1(){
		panel1 = new JPanel();
		panel1.setLayout( new GridLayout(3, 2) );

		// Add some buttons
		panel1.add(new JLabel("Nome do Medicamento:"));
		panel1.add( new JTextField());
		
		panel1.add(new JLabel("Código de Prescrição:"));
		panel1.add( new JTextField());
		
		panel1.add(new JLabel("Substância Activa:"));
		panel1.add( new JTextField());

	}

	public void createPanel2(){
		panel2 = new JPanel();
		panel2.setLayout( new GridLayout(3, 3) );

		panel2.add(new JLabel(""));
		panel2.add( new JButton("Pesquisar"));
		panel2.add(new JLabel(""));
		panel2.add( new JButton("Composição"));
		panel2.add( new JButton("Indicações"));
		panel2.add( new JButton("Posologia"));
		panel2.add( new JButton("Não Usar se"));
		panel2.add( new JButton("Interações Medicamentosas"));
		panel2.add( new JButton("Efeitos Secundários"));
		
		
	//Search Listener//
		JButton search = (JButton) panel2.getComponent(1);
		
		search.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	MedicineInfo mi = getMedicineInfo();
                File f;
                String line;
                BufferedReader bufferedReader;
                
                JScrollPane scroll = (JScrollPane) panel3.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
                
           //-----------------------------------------------------------//
                
                JTextField byName = (JTextField) panel1.getComponent(1);
                String text = byName.getText();
                
                if(!text.isEmpty()){
                	//Nome do medicamento
                	try {
    					f = mi.InfoByName(text);
    					
    					line = null;
    					
    				    bufferedReader = new BufferedReader(new FileReader(f));
    				 
    				    while ((line = bufferedReader.readLine()) != null)
    				    {
    				        ta.append(line + "\n");
    				    }
    				    bufferedReader.close();
    				} catch (Exception e1) {
    					e1.printStackTrace();
    				}
                	
                }
                else{
                	JTextField byCode = (JTextField) panel1.getComponent(3);
                	text = byCode.getText();
                	//Codigo de prescrição
                	if(!text.isEmpty()){
                		try {
        					f = mi.InfoByCode(text);
        					
        					line = null;
        					
        				    bufferedReader = new BufferedReader(new FileReader(f));
        				 
        				    while ((line = bufferedReader.readLine()) != null)
        				    {
        				        ta.append(line + "\n");
        				    }
        				    bufferedReader.close();
        				} catch (Exception e1) {
        					e1.printStackTrace();
        				}
                	}
                	else{
                		//Substância Activa
                		JTextField bySubstance = (JTextField) panel1.getComponent(5);
                    	text = bySubstance.getText();
                    	
                    	try {
        					f = mi.InfoBySubstance(text);
        					
        					line = null;
        					
        				    bufferedReader = new BufferedReader(new FileReader(f));
        				 
        				    while ((line = bufferedReader.readLine()) != null)
        				    {
        				        ta.append(line + "\n");
        				    }
        				    bufferedReader.close();
        				} catch (Exception e1) {
        					e1.printStackTrace();
        				}
                	}
                }			
            }
        });
		
//		--------------------------------------------------------------------
	//Composition Listener//
		JButton composition = (JButton) panel2.getComponent(3);
		
		composition.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
                MedicineInfo mi = getMedicineInfo();
                Model model = mi.getCompleteModel();
                
                File file = new File("Composition.txt");
                FileOutputStream fout = null;
                String line;
                BufferedReader bufferedReader;
                
                
        		try {
					file.createNewFile();
					 fout = new FileOutputStream(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
        		
                
                JTextField tf = (JTextField) panel1.getComponent(1);
                String medicineName = tf.getText();                
                
                JScrollPane scroll = (JScrollPane) panel3.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
                
                String queryString =
        	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        	    		"PREFIX : <http://medicine/>" +
        				"PREFIX RCM: <http://medicine/RCM/>" +
        				"PREFIX FI: <http://medicine/FI/>"  +
        	    		"SELECT ?Composição\n" +
        	    		"WHERE{ ?x :NAME \"" + medicineName + "\". \n"
        	    		+ "?x :RCM [ RCM:Composition ?Composição ] .}";
        	    
        	    Query query = QueryFactory.create(queryString);
        	    QueryExecution qe = QueryExecutionFactory.create(query, model);
        	    com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
        	    
        	    if(!results.hasNext()){
        	    	ta.append("Campo inexistente para este medicamento");
        	    	qe.close();
        	    }
        	    else{
        	    	ResultSetFormatter.out(fout, results, query);
            	    qe.close();
            	    
            	    line = null;
					
				    try {
						bufferedReader = new BufferedReader(new FileReader(file));
						
						while ((line = bufferedReader.readLine()) != null)
					    {
					        ta.append(line + "\n");
					    }
					    bufferedReader.close();
					    
					} catch (Exception e1) {
						e1.printStackTrace();
					}
        	    }
            }
        });
//		--------------------------------------------------------------------
		
	//Indications Listener//
		JButton indications = (JButton) panel2.getComponent(4);
		
		indications.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	MedicineInfo mi = getMedicineInfo();
                Model model = mi.getCompleteModel();
                
                File file = new File("Indications.txt");
                FileOutputStream fout = null;
                String line;
                BufferedReader bufferedReader;
                
                
        		try {
					file.createNewFile();
					fout = new FileOutputStream(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
        		
                
                JTextField tf = (JTextField) panel1.getComponent(1);
                String medicineName = tf.getText();                
                
                JScrollPane scroll = (JScrollPane) panel3.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
                
                String queryString =
        	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        	    		"PREFIX : <http://medicine/>" +
        				"PREFIX RCM: <http://medicine/RCM/>" +
        				"PREFIX FI: <http://medicine/FI/>"  +
        	    		"SELECT ?Indicações\n" +
        	    		"WHERE{ ?x :NAME \"" + medicineName + "\". \n"
        	    		+ "?x :RCM [ RCM:Indications ?Indicações ] .}";
        	    
        	    Query query = QueryFactory.create(queryString);
        	    QueryExecution qe = QueryExecutionFactory.create(query, model);
        	    com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
        	    
        	    if(!results.hasNext()){
        	    	ta.append("Campo inexistente para este medicamento");
        	    	qe.close();
        	    }
        	    else{
        	    	ResultSetFormatter.out(fout, results, query);
            	    qe.close();
            	    
            	    line = null;
					
				    try {
						bufferedReader = new BufferedReader(new FileReader(file));
						
						while ((line = bufferedReader.readLine()) != null)
					    {
					        ta.append(line + "\n");
					    }
					    bufferedReader.close();
					    
					} catch (Exception e1) {
						e1.printStackTrace();
					}
        	    }
            }
        });
//		--------------------------------------------------------------------
		
	//Posology Listener//
		JButton posology = (JButton) panel2.getComponent(5);
		
		posology.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	MedicineInfo mi = getMedicineInfo();
                Model model = mi.getCompleteModel();
                
                File file = new File("Posology.txt");
                FileOutputStream fout = null;
                String line;
                BufferedReader bufferedReader;
                
                
        		try {
					file.createNewFile();
					fout = new FileOutputStream(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
        		
                
                JTextField tf = (JTextField) panel1.getComponent(1);
                String medicineName = tf.getText();                
                
                JScrollPane scroll = (JScrollPane) panel3.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
                
                String queryString =
        	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        	    		"PREFIX : <http://medicine/>" +
        				"PREFIX RCM: <http://medicine/RCM/>" +
        				"PREFIX FI: <http://medicine/FI/>"  +
        	    		"SELECT ?Posologia\n" +
        	    		"WHERE{ ?x :NAME \"" + medicineName + "\". \n"
        	    		+ "?x :RCM [ RCM:Posology ?Posologia ] .}";
        	    
        	    Query query = QueryFactory.create(queryString);
        	    QueryExecution qe = QueryExecutionFactory.create(query, model);
        	    com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
        	    
        	    if(!results.hasNext()){
        	    	ta.append("Campo inexistente para este medicamento");
        	    	qe.close();
        	    }
        	    else{
        	    	ResultSetFormatter.out(fout, results, query);
            	    qe.close();
            	    
            	    line = null;
					
				    try {
						bufferedReader = new BufferedReader(new FileReader(file));
						
						while ((line = bufferedReader.readLine()) != null)
					    {
					        ta.append(line + "\n");
					    }
					    bufferedReader.close();
					    
					} catch (Exception e1) {
						e1.printStackTrace();
					}
        	    }
            }
        });
//		--------------------------------------------------------------------
		
		//Do not use Listener//
				JButton doNotUse = (JButton) panel2.getComponent(6);
				
				doNotUse.addActionListener(new ActionListener() {
					 
		            public void actionPerformed(ActionEvent e)
		            {
		            	MedicineInfo mi = getMedicineInfo();
		                Model model = mi.getCompleteModel();
		                
		                File file = new File("DoNotUse.txt");
		                FileOutputStream fout = null;
		                String line;
		                BufferedReader bufferedReader;
		                
		                
		        		try {
							file.createNewFile();
							fout = new FileOutputStream(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		        		
		                
		                JTextField tf = (JTextField) panel1.getComponent(1);
		                String medicineName = tf.getText();                
		                
		                JScrollPane scroll = (JScrollPane) panel3.getComponent(1);
		                JViewport view = scroll.getViewport();
		                JTextArea ta = (JTextArea)view.getView();
		                
		                String queryString =
		        	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		        	    		"PREFIX : <http://medicine/>" +
		        				"PREFIX RCM: <http://medicine/RCM/>" +
		        				"PREFIX FI: <http://medicine/FI/>"  +
		        	    		"SELECT ?Não_Usar_se\n" +
		        	    		"WHERE{ ?x :NAME \"" + medicineName + "\". \n"
		        	    		+ "?x :FI [ FI:DoNotUse ?Não_Usar_se ] .}";
		        	    
		        	    Query query = QueryFactory.create(queryString);
		        	    QueryExecution qe = QueryExecutionFactory.create(query, model);
		        	    com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
		        	    
		        	    if(!results.hasNext()){
		        	    	ta.append("Campo inexistente para este medicamento");
		        	    	qe.close();
		        	    }
		        	    else{
		        	    	ResultSetFormatter.out(fout, results, query);
		            	    qe.close();
		            	    
		            	    line = null;
							
						    try {
								bufferedReader = new BufferedReader(new FileReader(file));
								
								while ((line = bufferedReader.readLine()) != null)
							    {
							        ta.append(line + "\n");
							    }
							    bufferedReader.close();
							    
							} catch (Exception e1) {
								e1.printStackTrace();
							}
		        	    }
		            }
		        });
//				--------------------------------------------------------------------
				
			//Interactions Listener//
				JButton interactions = (JButton) panel2.getComponent(7);
				
				interactions.addActionListener(new ActionListener() {
					 
		            public void actionPerformed(ActionEvent e)
		            {
		            	MedicineInfo mi = getMedicineInfo();
		                Model model = mi.getCompleteModel();
		                
		                File file = new File("Interactions.txt");
		                FileOutputStream fout = null;
		                String line;
		                BufferedReader bufferedReader;
		                
		                
		        		try {
							file.createNewFile();
							fout = new FileOutputStream(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		        		
		                
		                JTextField tf = (JTextField) panel1.getComponent(1);
		                String medicineName = tf.getText();                
		                
		                JScrollPane scroll = (JScrollPane) panel3.getComponent(1);
		                JViewport view = scroll.getViewport();
		                JTextArea ta = (JTextArea)view.getView();
		                
		                String queryString =
		        	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		        	    		"PREFIX : <http://medicine/>" +
		        				"PREFIX RCM: <http://medicine/RCM/>" +
		        				"PREFIX FI: <http://medicine/FI/>"  +
		        	    		"SELECT ?Interações\n" +
		        	    		"WHERE{ ?x :NAME \"" + medicineName + "\". \n"
		        	    		+ "?x :FI [ FI:Interactions ?Interações ] .}";
		        	    
		        	    Query query = QueryFactory.create(queryString);
		        	    QueryExecution qe = QueryExecutionFactory.create(query, model);
		        	    com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
		        	    
		        	    if(!results.hasNext()){
		        	    	ta.append("Campo inexistente para este medicamento");
		        	    	qe.close();
		        	    }
		        	    else{
		        	    	ResultSetFormatter.out(fout, results, query);
		            	    qe.close();
		            	    
		            	    line = null;
							
						    try {
								bufferedReader = new BufferedReader(new FileReader(file));
								
								while ((line = bufferedReader.readLine()) != null)
							    {
							        ta.append(line + "\n");
							    }
							    bufferedReader.close();
							    
							} catch (Exception e1) {
								e1.printStackTrace();
							}
		        	    }
		            }
		        });
//				--------------------------------------------------------------------
				
			//Side Effects Listener//
				JButton sideEffects = (JButton) panel2.getComponent(8);
				
				sideEffects.addActionListener(new ActionListener() {
					 
		            public void actionPerformed(ActionEvent e)
		            {
		                MedicineInfo mi = getMedicineInfo();
		                Model model = mi.getCompleteModel();
		                
		                File file = new File("SideEffects.txt");
		                FileOutputStream fout = null;
		                String line;
		                BufferedReader bufferedReader;
		                
		                
		        		try {
							file.createNewFile();
							fout = new FileOutputStream(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		        		
		                
		                JTextField tf = (JTextField) panel1.getComponent(1);
		                String medicineName = tf.getText();                
		                
		                JScrollPane scroll = (JScrollPane) panel3.getComponent(1);
		                JViewport view = scroll.getViewport();
		                JTextArea ta = (JTextArea)view.getView();
		                
		                String queryString =
		        	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		        	    		"PREFIX : <http://medicine/>" +
		        				"PREFIX RCM: <http://medicine/RCM/>" +
		        				"PREFIX FI: <http://medicine/FI/>"  +
		        	    		"SELECT ?Efeitos_Secundários\n" +
		        	    		"WHERE{ ?x :NAME \"" + medicineName + "\". \n"
		        	    		+ "?x :FI [ FI:SideEffects ?Efeitos_Secundários ] .}";
		        	    
		        	    Query query = QueryFactory.create(queryString);
		        	    QueryExecution qe = QueryExecutionFactory.create(query, model);
		        	    com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
		        	    
		        	    if(!results.hasNext()){
		        	    	ta.append("Campo inexistente para este medicamento");
		        	    	qe.close();
		        	    }
		        	    else{
		        	    	ResultSetFormatter.out(fout, results, query);
		            	    qe.close();
		            	    
		            	    line = null;
							
						    try {
								bufferedReader = new BufferedReader(new FileReader(file));
								
								while ((line = bufferedReader.readLine()) != null)
							    {
							        ta.append(line + "\n");
							    }
							    bufferedReader.close();
							    
							} catch (Exception e1) {
								e1.printStackTrace();
							}
		        	    }
		            }
		        });
//				--------------------------------------------------------------------
		
	}

	public void createPanel3(){
		panel3 = new JPanel();
		panel3.setLayout( new BorderLayout() );
		panel3.setPreferredSize( new Dimension( 400, 100 ) );
		panel3.setMinimumSize( new Dimension( 100, 50 ) );

		panel3.add( new JLabel( "Resultados:" ), BorderLayout.NORTH );
		
		JTextArea textArea = new JTextArea();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textArea);
		panel3.add(scrollPane, BorderLayout.CENTER);		
	}
	

	public GUI(){
		setTitle( "Informações sobre produtos farmacêuticos" );
		setBackground( Color.gray );
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		getContentPane().add( topPanel );

		// Create the panels
		createPanel1();
		createPanel2();
		createPanel3();

		// Create a splitter pane
		splitPaneV = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		topPanel.add( splitPaneV, BorderLayout.CENTER );

		splitPaneH = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH.setLeftComponent( panel1 );
		splitPaneH.setRightComponent( panel2 );

		splitPaneV.setLeftComponent( splitPaneH );
		splitPaneV.setRightComponent( panel3 );
	}

	

	public static void main( String args[] ){
				
	    // Create an instance of the test application
	    GUI mainFrame = new GUI();
	    mainFrame.pack();
	    mainFrame.setVisible( true );
	    
	    mainFrame.medInfo = new MedicineInfo();
	}
		
	
}
