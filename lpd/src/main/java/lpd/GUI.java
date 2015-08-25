package lpd;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
//import java.io.File;

public class GUI extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> sources = new ArrayList<String>();
	
	private SemanticWebEngine swe = new SemanticWebEngine();
	
	private     JSplitPane  splitPaneV;
	private     JSplitPane  splitPaneV2;
	
	private     JSplitPane  splitPaneH;
	private     JSplitPane  splitPaneH2;
	private     JSplitPane  splitPaneH3;
	
	private     JPanel      panel1;
	private     JPanel      panel2;
	private     JPanel      panel3;
	private     JPanel      panel4;
	private     JPanel      panel5;
	private     JPanel      panel6;
	
//	public GUI(){
//		this.sources = new ArrayList<String>();
//		this.swe = new SemanticWebEngine();
//	}
	

	public void createPanel1(){
		panel1 = new JPanel();
		panel1.setLayout( new GridLayout(2, 2) );

		// Add some buttons
		panel1.add(new JLabel("Nome do Medicamento:"));
		panel1.add( new JTextField());
		
		panel1.add(new JLabel("Substância Activa:"));
		panel1.add( new JTextField());
	}

	
	public void createPanel2(){
		
		JCheckBox checkInfar = new JCheckBox("Infarmed");
		checkInfar.setSelected(false);
		
		checkInfar.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
		        JCheckBox cb = (JCheckBox) event.getSource();
		        if (cb.isSelected()) {
		        	sources.add("infarmed");
		            
		        } else {
		            int index = sources.indexOf("infarmed");
		            sources.remove(index);
		        }
		    }
		});
		
		JCheckBox checkInfo = new JCheckBox("Infomed");
		checkInfo.setSelected(false);
		
		checkInfo.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
		        JCheckBox cb = (JCheckBox) event.getSource();
		        if (cb.isSelected()) {
		        	sources.add("infomed");
		            
		        } else {
		            int index = sources.indexOf("infomed");
		            sources.remove(index);
		        }
		    }
		});
				
		JPanel jplCheckBox = new JPanel();
		jplCheckBox.setLayout(new GridLayout(0, 1));
		jplCheckBox.add(checkInfar);
		jplCheckBox.add(checkInfo);

		panel2 = new JPanel();
		panel2.setLayout( new GridLayout(1, 1) );
		panel2.add(jplCheckBox);
	}
	
	
	public void createPanel3(){
		panel3 = new JPanel();
		panel3.setLayout(new GridLayout(3, 1));
		panel3.add(new JLabel("Propriedades:"));
		
		JLabel example = new JLabel("(ex: <prop 1>|<prop 2>|<prop 3>)");
		example.setFont(new Font("Serif", Font.PLAIN, 11));
		example.setForeground(Color.GRAY);
		
		panel3.add(example);
		panel3.add(new JTextField());
	}
	
	
	public void createPanel4(){
		panel4 = new JPanel();
		panel4.setLayout(new GridLayout(3, 1));
		
		panel4.add(new JLabel("Regra de Mapeamento:"));
		
		JLabel example = new JLabel("(ex: <prop 1>-<prop 2>|<prop 3>-<prop 4)");
		example.setFont(new Font("Serif", Font.PLAIN, 11));
		example.setForeground(Color.GRAY);
		
		panel4.add(example);
		panel4.add(new JTextField());
	}
	
	
	
	public void createPanel5(){
		panel5 = new JPanel();
		panel5.setLayout( new GridLayout(2, 1) );
		panel5.add(new JButton("Ver Propriedades"));
		panel5.add( new JButton("Pesquisar"));
		
		//Properties Listener
		JButton props = (JButton) panel5.getComponent(0);
		
		props.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
								
				JScrollPane scroll = (JScrollPane) panel6.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
                String properties = null;
                
                for(String src: sources){
				
					try {
						properties = swe.showProperties(src);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					ta.append(properties);
                }
			}
		});
				
		//Search Listener//
		JButton search = (JButton) panel5.getComponent(1);
		
		search.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e){
            	
            	String result = null;
        		
        		JScrollPane scroll = (JScrollPane) panel6.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
        		
              //Properties List
        		JTextField tf = (JTextField) panel3.getComponent(2);
        		String props = tf.getText();
        		
        		//properties list (format: <http://....> )
        		String[] propsList = props.split("\\|");
        		
        		//Mapping rules
        		JTextField mapping = (JTextField) panel4.getComponent(2);
        		String mappings = mapping.getText();
        		
        		//mapping list (format: <http://...>-<http://...> )
        		String[] mappingRules = null;
        		if(!mappings.equals(""))
        			mappingRules = mappings.split("\\|");
        		
        		
        		//Medicine Name
            	JTextField byName = (JTextField) panel1.getComponent(1);
                String text = byName.getText();
                
        		if(!text.isEmpty()){                	
        			
                	String value = text.substring(0, 1).toUpperCase() + text.substring(1);
                	
                	try{                		
                		result = swe.makeQuery("Name", value, sources, propsList, mappingRules);
                	}
                	catch (Exception e1){
                		e1.printStackTrace();
                	}
                	
                	//append to text area
                	ta.append(result);
        		}        		
        		else{
                		
        			//Active substance
            		JTextField bySubstance = (JTextField) panel1.getComponent(3);
                	text = bySubstance.getText();
                	
                	if(!text.isEmpty()){
                		try{
                    		result = swe.makeQuery("Substance", text, sources, propsList, mappingRules);
                    	}
                    	catch (Exception e1){
                    		e1.printStackTrace();
                    	}
                    	
                    	//append to text area
                    	ta.append(result);
            		}
                }
            }
        });
	}
	
		
	public void createPanel6(){
		panel6 = new JPanel();
		panel6.setLayout( new BorderLayout() );
		panel6.setPreferredSize( new Dimension( 400, 100 ) );
		panel6.setMinimumSize( new Dimension( 100, 50 ) );

		panel6.add( new JLabel( "Resultados:" ), BorderLayout.NORTH );
		
		JTextArea textArea = new JTextArea();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textArea);
		panel6.add(scrollPane, BorderLayout.CENTER);	
		
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
		createPanel6();

		// Create a splitter pane
		splitPaneV = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		topPanel.add( splitPaneV, BorderLayout.CENTER );
		
		splitPaneV2 = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		topPanel.add( splitPaneV2, BorderLayout.CENTER );

		splitPaneH = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH.setLeftComponent( panel1 );
		splitPaneH.setRightComponent( panel2 );
		
		splitPaneH2 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH2.setLeftComponent(splitPaneH);
		splitPaneH2.setRightComponent(panel3);
		
		splitPaneH3 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH3.setLeftComponent(panel4);
		splitPaneH3.setRightComponent(panel5);
		
		splitPaneV.setLeftComponent(splitPaneH2);
		splitPaneV.setBottomComponent(splitPaneH3);		

		splitPaneV2.setLeftComponent( splitPaneV );
		splitPaneV2.setBottomComponent( panel6 );
	}

	

	public static void main( String args[] ){
				
	    // Create an instance of the test application
	    GUI mainFrame = new GUI();
	    mainFrame.pack();
	    mainFrame.setVisible( true );
	    
	}
		
	//<http://infarmed/Nome_do_Medicamento>|<http://infarmed/Substância_Activa>
}