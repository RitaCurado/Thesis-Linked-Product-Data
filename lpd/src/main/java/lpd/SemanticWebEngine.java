package lpd;

import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;



import java.util.ArrayList;

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
		
		ByteArrayOutputStream go = new ByteArrayOutputStream ();
		Model model = null;
		String properties = null;
		
		Query query;
	    QueryExecution qe;
	    ResultSet results;
	    String result;
		
		if(source.equals("Infarmed")){
			model = infarDC.getModel();
			if(model == null)
				model = infarDC.getSchemaModel();
			properties = "INFARMED:\n";
		}
		if(source.equals("Infomed")){
			model = infoDC.getModel();
			if(model == null)
				model = infoDC.getSchemaModel();
			properties = "INFOMED:\n";
		}
		
	    
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
		
		properties = properties.concat(result);
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
		properties = properties.concat("\n");
		
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
	
	public String makeQuery(String searchProperty, String value, ArrayList<String> sources, String[] propsList, String[] mappings){
		
		String result = "\n";
		String partialResult = null;
		
		String select = "";
		String where = "";
		String property = "";		
		String column = "";
		String[] splt = null;
		
		ArrayList<String> infarProps = new ArrayList<String>();
		ArrayList<String> infoProps = new ArrayList<String>();
		
		String[] byName = {"<http://infarmed/Nome_do_Medicamento>", "<http://infomed/Nome_do_Medicamento>"};
		String[] bySubstance = {"<http://infarmed/Substância_Activa>", "<http://infomed/Nome_Genérico>"};
		
		for(String p: propsList){
			if(p.contains("infarmed"))
				infarProps.add(p);
			if(p.contains("infomed"))
				infoProps.add(p);
		}
		
		if(mappings == null){
			
			for(String s : sources){
				
				select = "";
				where = "";
				property = "";		
				column = "";
				splt = null;
				
				if(s.equals("Infarmed")){
					
					result = result.concat("INFARMED:\n");
					
					for(String pp: infarProps){
						
						property = pp;
						splt = pp.split("/");        			
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
					
					try {
						
						if(searchProperty.equals("Name")){
							partialResult = queryInfar(byName[0], value, select, where);
							result = result.concat(partialResult + "\n");
						}
						if(searchProperty.equals("Substance")){
							partialResult = queryInfar(bySubstance[0], value, select, where);
							result = result.concat(partialResult + "\n");
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if(s.equals("Infomed")){
					
					result = result.concat("INFOMED:\n");
					
					for(String pp: infoProps){
						
						property = pp;
						splt = pp.split("/");        			
						column = splt[splt.length - 1];
						column = column.replace(">", "");
						
						select += " ?" + column;
						where += " ?x " + property + " ?" + column + " .";
					}
					
					try {
						if(searchProperty.equals("Name")){
							partialResult = queryInfo(byName[1], value, select, where);
							result = result.concat(partialResult + "\n");
						}
						if(searchProperty.equals("Substance")){
							partialResult = queryInfo(bySubstance[1], value, select, where);
							result = result.concat(partialResult + "\n");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		else{
			result = "Under construction";
		}
		
		
		return result;
	}
	
}
