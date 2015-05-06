package lpdV01;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
//import java.io.File;

public class GUI extends JFrame{

	private static final long serialVersionUID = 1L;	
	private final GUI gui = this;
	private String source = "Infarmed";
	final InfarmedDataConverter infarDC = new InfarmedDataConverter();
	final InfomedDataConverter infoDC = new InfomedDataConverter();
	final SemanticWebEngine swe = new SemanticWebEngine();
	
	private     JSplitPane  splitPaneV;
	private     JSplitPane  splitPaneH;
	private     JSplitPane  splitPaneH2;
	private     JSplitPane  splitPaneH3;
	private     JPanel      panel1;
	private     JPanel      panel2;
	private     JPanel      panel3;
	private     JPanel      panel4;
	private     JPanel      panel5;
	
	
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
				gui.setSource("Infarmed");
				JTextField byCode = (JTextField) panel1.getComponent(3);
				byCode.setEnabled(true);
			}
		});
		
		JRadioButton jrbInfo = new JRadioButton("Infomed");
		jrbInfo.setMnemonic(KeyEvent.VK_1);
		jrbInfo.setActionCommand("info");
		jrbInfo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {				
				gui.setSource("Infomed");
				JTextField byCode = (JTextField) panel1.getComponent(3);
				byCode.setEnabled(false);
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
		panel3.setLayout(new GridLayout(3, 1));
		panel3.add(new JLabel("Propriedades:"));
		
		JLabel example = new JLabel("(ex: <http://infarmed/Dosagem>|<http://infarmed/CNPEM>)");
		example.setFont(new Font("Serif", Font.PLAIN, 11));
		example.setForeground(Color.GRAY);
		
		panel3.add(example);
		panel3.add(new JTextField());
				
	}
	
	public void createPanel4(){
		panel4 = new JPanel();
		panel4.setLayout( new GridLayout(2, 1) );
		panel4.add(new JButton("Ver Propriedades"));
		panel4.add( new JButton("Pesquisar"));
		
		//Properties Listener
		JButton props = (JButton) panel4.getComponent(0);
		
		props.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
								
				JScrollPane scroll = (JScrollPane) panel5.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
                String properties = null;
				
				try {
					properties = swe.showProperties(source);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				ta.append(properties);
			}
		});
		
		
				
	//Search Listener//
		JButton search = (JButton) panel4.getComponent(1);
		
		search.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	String select = "";
        		String where = "";
        		String property = "";
        		String column = "";
        		String[] splt;
        		
        		String result = null;
        		
        		JScrollPane scroll = (JScrollPane) panel5.getComponent(1);
                JViewport view = scroll.getViewport();
                JTextArea ta = (JTextArea)view.getView();
        		
        		JTextField tf = (JTextField) panel3.getComponent(2);
        		String props = tf.getText();
        		
        		String[] propsList = props.split("\\|");
        		
        		for(String s : propsList){
        			property = s;
        			splt = s.split("/");        			
        			column = splt[splt.length - 1];
        			column = column.replace(">", "");
        			
        			select += " ?" + column;
        			
        			if(property.contains("FI"))
        				where += " ?x " + "<http://infarmed/FI> [ " + property + " ?" + column + " ] .";
        			
        			if(property.contains("RCM"))
        				where += " ?x " + "<http://infarmed/RCM> [ " + property + " ?" + column + " ] .";
        			
        			else
        				where += " ?x " + property + " ?" + column + " .";
        		}
        		
            	JTextField byName = (JTextField) panel1.getComponent(1);
                String text = byName.getText();
                
              //Nome do medicamento
        		if(!text.isEmpty()){                	
        			
                	String value = text.substring(0, 1).toUpperCase() + text.substring(1);
                	
                	try{
                		
                		if(source.equals("Infarmed")){
                    		result = swe.queryInfar("<http://infarmed/Nome_do_Medicamento>", value, select, where);
                    	}
                		
                		if(source.equals("Infomed")){
                    		result = swe.queryInfo("<http://infomed/Nome_do_Medicamento>", value, select, where);
                    	}
                		
                	}
                	catch (Exception e1){
                		e1.printStackTrace();
                	}
                	
                	//append to text area
                	ta.append(result);                	
        		}
        		
        		else{
                	JTextField byCode = (JTextField) panel1.getComponent(3);
                	text = byCode.getText();
                	
                	//Codigo de prescrição
                	if(!text.isEmpty()){
                		try {
                        	result = swe.queryInfar("<http://infarmed/CNPEM>", text, select, where);
                    	}
                    	catch (Exception e1){
                    		e1.printStackTrace();
                    	}
                    	
                    	//append to text area
                		ta.append(result);
                	}
                	
                	else{
                		
                		JTextField bySubstance = (JTextField) panel1.getComponent(5);
                    	text = bySubstance.getText();
                    	
                    	//Substância Activa
                    	if(!text.isEmpty()){
                    		try{
                        		
                        		if(source.equals("Infarmed")){
                            		result = swe.queryInfar("<http://infarmed/Substância_Activa>", text, select, where);
                            	}
                        		
                        		if(source.equals("Infomed")){
                            		result = swe.queryInfo("<http://infomed/Nome_Genérico>", text, select, where);
                            	}
                        		
                        	}
                        	catch (Exception e1){
                        		e1.printStackTrace();
                        	}
                        	
                        	//append to text area
                        	ta.append(result);
                		}
                	}
                }
            }
        });
	}
		
	public void createPanel5(){
		panel5 = new JPanel();
		panel5.setLayout( new BorderLayout() );
		panel5.setPreferredSize( new Dimension( 400, 100 ) );
		panel5.setMinimumSize( new Dimension( 100, 50 ) );

		panel5.add( new JLabel( "Resultados:" ), BorderLayout.NORTH );
		
		JTextArea textArea = new JTextArea();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textArea);
		panel5.add(scrollPane, BorderLayout.CENTER);	
		
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
		splitPaneH2.setRightComponent(panel3);
		
		splitPaneH3 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		splitPaneH3.setLeftComponent(splitPaneH2);
		splitPaneH3.setRightComponent(panel4);
		

		splitPaneV.setLeftComponent( splitPaneH3 );
		splitPaneV.setBottomComponent( panel5 );
	}

	

	public static void main( String args[] ){
				
	    // Create an instance of the test application
	    GUI mainFrame = new GUI();
	    mainFrame.pack();
	    mainFrame.setVisible( true );
	    
	}
		
	
}

