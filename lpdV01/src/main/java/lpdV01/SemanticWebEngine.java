package lpdV01;

import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

public class SemanticWebEngine {
	
	InfarmedDataConverter infarDC;
	InfomedDataConverter infoDC;
	
	public SemanticWebEngine(){
		this.infarDC = new InfarmedDataConverter();
		this.infoDC = new InfomedDataConverter();
	}
	
	public String showProperties(String source) throws Exception{
		
		Model model = null;		
		ByteArrayOutputStream go = new ByteArrayOutputStream ();
		
		if(source.equals("Infarmed")){
			model = infarDC.getModel();
			if(model == null)
				model = infarDC.getSchemaModel();
		}
		if(source.equals("Infomed")){
			model = infoDC.getModel();
			if(model == null)
				model = infoDC.getSchemaModel();
		}
		
		Query query;
	    QueryExecution qe;
	    ResultSet results;
	    String result;
	    String properties;
	    
	    String queryString =
	    		"SELECT DISTINCT ?class\n" +
	    			"WHERE {" +
	    				  "?class a <http://www.w3.org/2000/01/rdf-schema#Class> .}\n" +
	    				"LIMIT 25\n" +
	    				"OFFSET 0";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results =  qe.execSelect();
		ResultSetFormatter.out(go ,results, query);
		
		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");
		
		properties = result;
		properties = properties.concat("\n");
		
	    qe.close();
		
	    go.reset();
		queryString =
				"SELECT DISTINCT ?property\n" +
		    			"WHERE {" +
		    				  "?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .}\n" +
		    				"LIMIT 25\n" +
		    				"OFFSET 0";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results =  qe.execSelect();
		ResultSetFormatter.out(go ,results, query);
		
		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");
		
		properties = properties.concat(result);
		
	    qe.close();
	    
	    return properties;
	}
	
	
	public String queryInfar(String property, String value, String select, String where) throws Exception{
		
		Query query;
		QueryExecution qe;
		ResultSet results;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		String queryResult = "";
		
		String queryString =
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://infarmed/>" +
	    		"SELECT ?x\n" +
	    		"WHERE{ ?x " + property + " ?s ."
	    				+ "FILTER regex(str(?s)," + "\"" + value + "\"" + ")"
	    				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, infarDC.getModel());
		results =  qe.execSelect();
		
		if(!results.hasNext()){
			
			if(property.equals("<http://infarmed/Nome_do_Medicamento>"))
				infarDC.getInfarByName(value);
			if(property.equals("<http://infarmed/Substância_Activa>"))
				infarDC.getInfarBySubstance(value);
			if(property.equals("<http://infarmed/CNPEM>"))
				infarDC.getInfarByCode(value);
		}
	    
		qe.close();
		
		queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://infarmed/>" +
				"PREFIX RCM: <http://infarmed/RCM/>" +
				"PREFIX FI: <http://infarmed/FI/>"  +
				"SELECT" + select + "\n" +
				"WHERE{ ?x " + property + " ?s ."
				     + where 
				     + "FILTER regex(str(?s)," + "\"" + value + "\"" + ")"
			    	 + "}";
		
		query = QueryFactory.create(queryString);
	    qe = QueryExecutionFactory.create(query, infarDC.getModel());
	    results =  qe.execSelect();
	    ResultSetFormatter.out(baos, results, query);
	    
	    queryResult = baos.toString();
		queryResult = queryResult.replace("-", "_");
	    
	    qe.close();
		
		return queryResult;
	}
	
	public String queryInfo(String property, String value, String select, String where) throws Exception{
		
		Query query;
		QueryExecution qe;
		ResultSet results;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		String queryResult = "";
		
		String queryString =
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://infomed/>" +
	    		"SELECT ?x\n" +
	    		"WHERE{ ?x " + property + " ?s ."
	    				+ "FILTER regex(str(?s)," + "\"" + value + "\"" + ")"
	    				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, infoDC.getModel());
	    results =  qe.execSelect();
	    
	    if(!results.hasNext()){
			
			if(property.equals("<http://infomed/Nome_do_Medicamento>"))
				infoDC.getInfoByName(value);
			if(property.equals("<http://infomed/Nome_Genérico>"))
				infoDC.getInfoBySubstance(value);
		}
	    
	    qe.close();

		queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://infomed/>" +
				"SELECT" + select + "\n" +
				"WHERE{ ?x " + property + " ?s ."
				     + where 
				     + "FILTER regex(str(?s)," + "\"" + value + "\"" + ")"
			    	 + "}";
	    
	    query = QueryFactory.create(queryString);
	    qe = QueryExecutionFactory.create(query, infoDC.getModel());
	    results =  qe.execSelect();
	    
	    ResultSetFormatter.out(baos, results, query);
	    
	    queryResult = baos.toString();
		queryResult = queryResult.replace("-", "_");
	    
	    qe.close();
		
		return queryResult;
	}
	
}
