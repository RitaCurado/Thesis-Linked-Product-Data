package pt.ulisboa.tecnico.curatorapp.domain;

import java.awt.*;

import javax.swing.*;

import pt.ulisboa.tecnico.core.SemanticWebEngine;

public class CuratorApp extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private CardLayout cl;
	private JPanel contentPanel;
	
	private SemanticWebEngine swe;
	
	public CuratorApp(){
		super("Advanced Search App - Data Curator");
		swe = new SemanticWebEngine();
		cl = new CardLayout();
		contentPanel = new JPanel();
	}
	
    public static void main(String[] args){
    	
        System.out.println( "Hello World!" );
    }
}
