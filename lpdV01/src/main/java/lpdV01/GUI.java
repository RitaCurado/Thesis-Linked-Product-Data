package lpdV01;

import javax.swing.*;

import com.gargoylesoftware.htmlunit.javascript.host.Text;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class GUI extends JFrame{

	private static final long serialVersionUID = 1L;
	private String source = "Infarmed";
	private final GUI gui = this;
	
	private     JSplitPane  splitPaneV;
	private     JSplitPane  splitPaneH;
	private     JSplitPane  splitPaneH2;
	private     JSplitPane  splitPaneH3;
	private     JPanel      panel1;
	private     JPanel      panel2;
	private     JPanel      panel3;
	private     JPanel      panel4;
	private     JPanel      panel5;
	private		MedicineInfo medInfo;
	
	public MedicineInfo getMedicineInfo(){
		return medInfo;
	}
	
	public void setSource(String s){
		this.source = s;
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
		
		JRadioButton jrbInfar = new JRadioButton("Infarmed");
		jrbInfar.setMnemonic(KeyEvent.VK_0);
		jrbInfar.setActionCommand("infar");
		jrbInfar.setSelected(true);
		jrbInfar.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb;
				gui.setSource("Infarmed");
				
				Component[] components = panel4.getComponents();
			    for(Component com : components) {
			    	cb = (JCheckBox) com;
			    	cb.setSelected(false);
			    	cb.setEnabled(true);
			    }				
				panel4.getComponent(8).setEnabled(false);
			}
		});
		
		JRadioButton jrbInfo = new JRadioButton("Infomed");
		jrbInfo.setMnemonic(KeyEvent.VK_1);
		jrbInfo.setActionCommand("info");
		jrbInfo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {				
				JCheckBox cb;
				gui.setSource("Infomed");
				
				Component[] components = panel4.getComponents();
				for(int i = 0; i < components.length; i++){
					cb = (JCheckBox) components[i];
					cb.setSelected(false);
					if(i == 5 || i == 6 || i == 7 ||
						i == 9 || i == 10 || i == 11 || i == 12 || i == 13){
						cb.setEnabled(false);
					}
					else
						cb.setEnabled(true);
				}
			}
		});
		
		ButtonGroup group = new ButtonGroup();
		group.add(jrbInfar);
		group.add(jrbInfo);
		
		JPanel jplRadio = new JPanel();
		jplRadio.setLayout(new GridLayout(0, 1));
		jplRadio.add(jrbInfar);
		jplRadio.add(jrbInfo);

		panel2 = new JPanel();
		panel2.setLayout( new GridLayout(1, 1) );
		panel2.add(jplRadio);
		
		
		
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
	
	public void createPanel4(){
		panel4 = new JPanel();
		panel4.setLayout( new GridLayout(3, 5) );
		
		JCheckBox name = new JCheckBox("Nome");
		JCheckBox substance = new JCheckBox("Substância");
		JCheckBox type = new JCheckBox("Tipo");
		JCheckBox dosage = new JCheckBox("Dose");
		JCheckBox gen = new JCheckBox("Genérico");
		JCheckBox units = new JCheckBox("Unidades");
		JCheckBox code = new JCheckBox("Código");
		JCheckBox price = new JCheckBox("Preço");
		JCheckBox owner = new JCheckBox("Titular");
		
		JCheckBox composition = new JCheckBox("Composição");
		JCheckBox posology = new JCheckBox("Posologia");
		JCheckBox interactions = new JCheckBox("Interações");
		JCheckBox sideEffects = new JCheckBox("Efeitos_Secundários");
		JCheckBox indications = new JCheckBox("Indicações");
		
		//substance.setEnabled(false);
		
		panel4.add(name);
		panel4.add(substance);
		panel4.add(type);
		panel4.add(dosage);
		panel4.add(gen);
		panel4.add(units);
		panel4.add(code);
		panel4.add(price);
		panel4.add(owner);
		
		panel4.add(composition);
		panel4.add(posology);
		panel4.add(interactions);
		panel4.add(sideEffects);
		panel4.add(indications);
		
		panel4.getComponent(8).setEnabled(false);
	}
		
	public void createPanel5(){
		
		panel5 = new JPanel();
		panel5.setLayout( new GridLayout(3, 1) );
		panel5.add(new JLabel());
		panel5.add( new JButton("Pesquisar"));
		
			
				
	//Search Listener//
		JButton search = (JButton) panel5.getComponent(1);
		
		search.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	String select = "";
        		String where = "";
        		JCheckBox cb;
        		
        		cb = (JCheckBox) panel4.getComponent(0);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":NAME" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(1);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":SUBSTANCE" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(2);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":TYPE" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(3);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":DOSAGE" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(4);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":GENERIC" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(5);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":UNITS" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(6);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":CODE" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(7);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":PRICE" + " ?" + cb.getText() + ".";
        		}
        		cb = (JCheckBox) panel4.getComponent(8);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":HOLDER" + " ?" + cb.getText() + ".";
        		}
        		
        		cb = (JCheckBox) panel4.getComponent(9);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":RCM [ RCM:Composition" + " ?" + cb.getText() + " ] .";
        		}
        		cb = (JCheckBox) panel4.getComponent(10);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":RCM [ RCM:Posology" + " ?" + cb.getText() + " ] .";
        		}
        		cb = (JCheckBox) panel4.getComponent(11);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":FI [ FI:Interactions" + " ?" + cb.getText() + " ] .";
        		}
        		cb = (JCheckBox) panel4.getComponent(12);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":FI [ FI:SideEffects" + " ?" + cb.getText() + " ] .";
        		}
        		cb = (JCheckBox) panel4.getComponent(13);
        		if(cb.isSelected()){
        			select += " ?" + cb.getText();
        			where += "?x" + ":RCM [ RCM:Indications" + " ?" + cb.getText() + " ] .";
        		}
        		
        		//System.out.println(select);
        		//System.out.println(where);
        		
        		//////////////////////////
            	
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
                		//text = text.toLowerCase();                		
                		String s = text.substring(0, 1).toUpperCase() + text.substring(1);
                		
    					f = mi.InfoByName(s, gui.source, select, where);
    					
    					line = null;
    					
    				    bufferedReader = new BufferedReader(new FileReader(f));
    				 
    				    while ((line = bufferedReader.readLine()) != null){
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
        					f = mi.InfoByCode(text, gui.source, select, where);
        					
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
                    	
                    	if(!text.isEmpty()){
                    		try {
            					f = mi.InfoBySubstance(text, gui.source, select, where);
            					
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
                    		if(gui.source.equals("Infarmed"))
                    			mi.getInfarModel().write(System.out, "TTL");
                    		else
                    			mi.getInfoModel().write(System.out, "TTL");
                    	}                    	
                	}
                }			
            }
        }); 
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
		createPanel4();
		createPanel5();

		// Create a splitter pane
		splitPaneV = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		topPanel.add( splitPaneV, BorderLayout.CENTER );

		splitPaneH = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH.setLeftComponent( panel1 );
		splitPaneH.setRightComponent( panel2 );
		
		splitPaneH2 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH2.setLeftComponent(splitPaneH);
		splitPaneH2.setRightComponent(panel4);
		
		splitPaneH3 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH3.setLeftComponent(splitPaneH2);
		splitPaneH3.setRightComponent(panel5);
		

		splitPaneV.setLeftComponent( splitPaneH3 );
		splitPaneV.setBottomComponent( panel3 );
	}

	

	public static void main( String args[] ){
				
	    // Create an instance of the test application
	    GUI mainFrame = new GUI();
	    mainFrame.pack();
	    mainFrame.setVisible( true );
	    
	    mainFrame.medInfo = new MedicineInfo();
	}
		
	
}

