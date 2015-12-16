package pt.ulisboa.tecnico.core;

import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


public class SemanticWebEngine {

	InfarmedDataConverter infarDC;
	InfomedDataConverter infoDC;

	Model dbModel, dbSourcesOriginal, dbFilters, dbMappings;
	ArrayList<String> sources;
	
	QueryExecution qe;

	public SemanticWebEngine() {
		
		sources = new ArrayList<String>();

		// open TDB dataset
		String directory;
		Dataset dataset = null;

		directory = "..\\TDB_sources_original";
		dataset = TDBFactory.createDataset(directory);
		this.dbSourcesOriginal = dataset.getDefaultModel();

		if (dbSourcesOriginal.isEmpty()) {
			System.out.println("Model is empty!!");
			dataset.begin(ReadWrite.WRITE);

			try {
				this.infarDC = new InfarmedDataConverter(dbSourcesOriginal);
				this.infoDC = new InfomedDataConverter(dbSourcesOriginal);
				dataset.commit();
			}

			finally {
				dataset.end();
			}

			// run a query
			String q = "select * where {?s ?p ?o}";
			Query query = QueryFactory.create(q);
			QueryExecution qexec = QueryExecutionFactory.create(query, dbSourcesOriginal);
			ResultSet results = qexec.execSelect();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File("output.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, results);
		}
		else{
			System.out.println("Model exists");
			this.infarDC = new InfarmedDataConverter();
			this.infoDC = new InfomedDataConverter();
		}
		
		directory = "..\\TDB_filters";
		dataset = TDBFactory.createDataset(directory);
		this.dbFilters = dataset.getDefaultModel();

		directory = "..\\TDB_mappings";
		dataset = TDBFactory.createDataset(directory);
		this.dbMappings = dataset.getDefaultModel();
		
		directory = "..\\TDB";
		dataset = TDBFactory.createDataset(directory);
		this.dbModel = dataset.getDefaultModel();
		


		dbModel.add(dbSourcesOriginal);
		//dbModel.add(dbFilters);
		dbModel.add(dbMappings);
		dbModel.commit();

	}
	
	
	
	public ArrayList<String> getSources(){
		Query query;
		QueryExecution qe;
		ResultSet results;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		String queryString, result, source;
		String[] spltResult, spltSource;
		
		queryString = "SELECT DISTINCT ?s\n"
				+ "WHERE{"
				+ " ?s a <http://www.w3.org/2000/01/rdf-schema#Class> ."
				+ "FILTER (regex(str(?s), \"http://www.[A-Za-z+]*.pt\"))"
				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);
		qe.close();
		
		result = go.toString();
		spltResult = result.split("\\r?\\n");
		
		for(int i=3; i < spltResult.length-1; i++){
			source = spltResult[i];
			source = source.replace("|", "");
			source = source.replace(" ", "");
			source = source.replace("<", "");
			source = source.replace(">", "");
			
			spltSource = source.split("/");
			
			if(!sources.contains(spltSource[2]))
				sources.add(spltSource[2]);
		}
		
		return sources;
	}

	public String getPropertySource(String property){

		String splitProp[] = null;
		String source[] = null;

		splitProp = property.split("/");
		source = splitProp[2].split("\\.");

		return source[1];
	}
	
	public ArrayList<String> showSourceClasses(String source){

		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> classes = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		if(source.contains("+"))
			source = source.replace("+", "\\\\+");

		String queryString = "SELECT DISTINCT ?class\n"
				+ "WHERE {"
				+ "?class a <http://www.w3.org/2000/01/rdf-schema#Class> ."
				+ "FILTER (regex(str(?class), '" + source + "')) }";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			classes.add(spltResult[i]);
		}

		return classes;
	}
	
	public String countClassInstances(String cl){
		Query query;
		QueryExecution qe;
		ResultSet results;
		String[] result;
		String queryString;
		String numInstances = "";
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		if(cl.contains("+"))
			cl = cl.replace("+", "\\\\+");

		queryString = "SELECT (COUNT(DISTINCT ?s) as ?c)\n"
				+ "WHERE {"
				+ " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?cl ."
				+ " FILTER (regex(str(?cl), '" + cl + "'))}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString().split("\\r?\\n");
		numInstances = result[3].substring(2, 5);

		return numInstances;
	}

	public ArrayList<String> showClassProperties(String cl){

		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> props = new ArrayList<String>();

		if(cl.contains("+"))
			cl = cl.replace("+", "\\\\+");
		
		String queryString = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ "{"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
				+ "}"
				+ "UNION"
				+ "{"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?o ."
				+ " ?s <http://www.w3.org/2000/01/rdf-schema#range> ?o ."
				+ " ?s a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?s <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
				+ "}"
				+ " FILTER (regex(str(?cl), '" + cl + "'))}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");
		result = result.replace(" ", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			props.add(spltResult[i]);
		}

		return props;
	}
	
	public ArrayList<String> showNodeProperties(String cl){

		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> props = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		if(cl.contains("+"))
			cl = cl.replace("+", "\\\\+");
		
		String queryString = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#range> ?o ."
				+ " FILTER (regex(str(?cl), '" + cl + "'))}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");
		result = result.replace(" ", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			props.add(spltResult[i]);
		}

		return props;
	}

	public String showPropertyValues(String property){
		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString;
		String output = "";
		
		String propName = this.getPropertyName(property);
		if(propName.contains(":")){
			String[] split = propName.split("\\:");
			propName = split[0];
		}
		
		queryString = "SELECT DISTINCT ?" + propName +"\n"
				+ "WHERE {"
				+ " ?s " + property + " ?" + propName + " ."
				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		output = go.toString();
		output = output.replace("-", "_");
		output = output.replace("|", "");

		qe.close();
		
		return output;
	}
	
	public ArrayList<String> showAggregationRules(){
		
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> rules = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();

		String queryString = "SELECT DISTINCT ?rule\n"
				+ "WHERE {"
				+ "?rule a ?class ."
				+ "?class a <http://www.w3.org/2000/01/rdf-schema#Class> .}";
				//+ "FILTER (regex(str(?class), '" + source + "')) }";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbFilters);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			rules.add(spltResult[i]);
		}

		return rules;
	}
	
	/* Query methods */
	public String selectAllInfo(String className){
		int count = 0, sid = 0, index;
		String output = "";
		ArrayList<String> props = null;
		String select = "", where = "", column = "";
		HashMap<String, Integer> sourceID = new HashMap<String, Integer>();

		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString;
		ByteArrayOutputStream go = new ByteArrayOutputStream();

		try {
			props = this.showClassProperties(className);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(props != null){
			for(String property: props){
				
				String s = getPropertySource(property);
				if(sourceID.containsKey(s)){
					index = sourceID.get(s);
				}
				else{
					sid++;
					sourceID.put(s, sid);
					index = sid;
				}

				column = this.getPropertyName(property);
				
				if(column.contains(":")){
					String[] split = column.split("\\:");
					column = split[0];
					select += " ?" + column;
					where += this.writeClauses(null, property, "simple", -1, -1);
				}
				else{
					select += " ?" + column + index;
					
					count = StringUtils.countMatches(property, "/");
					if(count > 3)
						where += this.writeClauses(null, property, "simpleNumC", index, -1);
					else
						where += this.writeClauses(null, property, "simpleNum", index, -1);
				}

			}

			queryString = "SELECT " + select + "\n"
							+ "WHERE {"
							+ " ?s a \"" + className + "\" ."
							+ where + "}";

			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, dbModel);
			results = qe.execSelect();
			ResultSetFormatter.out(go, results, query);

			output = go.toString();
			output = output.replace("-", "_");

			qe.close();
		}

		return output;
	}
	
	public void createAggregationRule(String source, String ruleName, String criteria){
				
		String baseURI = "http://" + source;
		Resource mainClass, criteriaProp, newRule;
		
		if(!checkPropertyExistance(baseURI + "/criteria", dbFilters)){
			mainClass = dbFilters.createResource(baseURI + "/AggregationRule");
			mainClass.addProperty(RDF.type, RDFS.Class);
			
			criteriaProp = dbFilters.createResource(baseURI + "/criteria");
			criteriaProp.addProperty(RDF.type, RDF.Property);
			criteriaProp.addProperty(RDFS.domain, mainClass.getURI());
		}
		
		newRule = dbFilters.createResource(baseURI + "/" + ruleName);
		newRule.addProperty(RDF.type, baseURI + "/AggregationRule");
		newRule.addProperty(dbFilters.getProperty(baseURI + "/criteria"), criteria);
		
	}
		
	public Model mappingConstructQuery(HashMap<String, String> subjectBySource, HashMap<String, ArrayList<String>> propsBySource,
			HashMap<String,ArrayList<String>> nodesBySource, String sourceName, String className, String[] mappingRules){
		
		int propID = 0;
		
		String newClass;
		String propConj = "";
		String conjuction = "";
		
		String source = "";
		String schema = "";
		String variables = "";
		String where = "";
		String optional = "";
		
		HashMap<String, Integer> sourcesIndex = getSourcesIndex(sourceName);
		ArrayList<String> list;
		String[] props = new String[sourcesIndex.keySet().size()];
		String[] ruleProperties;
		
//		--- create new mapping class ---
		newClass = sourceName + "/" + className;
		
		schema += " <" + newClass + "> <" + RDF.type + "> <" + RDFS.Class + "> .";
		variables += " ?s1 <" + RDF.type + "> \"" + newClass + "\" .";

		
//		--- extract info from each rule ---
		for(String mapRule: mappingRules){
			
			propID++;
			ruleProperties = mapRule.split("-"); //extract info from each property inside a rule
			
			for(String property: ruleProperties){
				source = getPropertySource(property);
				
				list = propsBySource.get(source);
				list.remove(property);
				
				where += writeClauses(subjectBySource.get(source), property, "normalMappingS", -1, propID);
				
				props[sourcesIndex.get(source)] = getPropertyName(property);
			}
			
			for(int index=0; index < props.length; index++){
				conjuction += props[index] + ":";
			}
			
			//Conjunction property name. <http://www.s1+s2.pt/p1:p2>
			propConj = "<" + sourceName + "/" + conjuction.substring(0, conjuction.length()-1) + ">";
			
			if(!checkPropertyExistance(propConj, dbModel)){
				schema += " " + propConj + " <" + RDF.type + "> <" + RDF.Property + "> .";
			}			
			
			schema += " " + propConj + " <" + RDFS.domain + "> \"" + newClass + "\" .";
			variables += writeClauses("s1", propConj, "normalMappingS", -1, propID);
			
			Arrays.fill(props, null);
		}
		
//		--- Treat properties that don't appear in any mapping rule ---
		int countSlash;
		String parent;
		ArrayList<String> properties, nodes;
		
		
		for(String key: propsBySource.keySet()){
			
			properties = propsBySource.get(key);
			nodes = nodesBySource.get(key);
			
			for(String p: properties){
				
				propID++;
				
				countSlash = StringUtils.countMatches(p, "/");
				
				if(countSlash > 3){ //Complex properties
					parent = getComposedProperty(p);
					
					if(nodes.contains(parent)){
						schema += " " + parent + " <" + RDFS.domain + "> \"" + newClass + "\" .";
						variables += writeClauses("s1", parent, "normalMappingS", -1, propID);
						optional += writeClauses(subjectBySource.get(key), parent, "normalMappingS", -1, propID);
						
						nodes.remove(parent);
						propID++;
					}
					
					variables += writeClauses("s1", p, "normalMappingS", -1, propID);
					optional += writeClauses(subjectBySource.get(key), p, "normalMappingC", -1, propID);
				}
				else{ //Simple properties
					schema += " " + p + " <" + RDFS.domain + "> \"" + newClass + "\" .";
					variables += writeClauses("s1", p, "normalMappingS", -1, propID);
					optional += writeClauses(subjectBySource.get(key), p, "normalMappingS", -1, propID);
					
					if(nodes.contains(p))
						nodes.remove(p);
				}
			}
		}
		
		Model resultModel = constructModelDB(schema, variables, where, optional);
		dbMappings.add(resultModel);
		dbMappings.commit();
		
		dbModel.add(resultModel);
		dbModel.commit();
		
		return resultModel;
	}

	public String makeSelectQuery(HashMap<String, String> searchCriteria, String chosenClass){
		
		int index, countSlash;
		String value, column, select, where, filter, result;
		
		index = countSlash = 0;
		result = select = where = filter = "";
		
		ArrayList<String> props = showClassProperties(chosenClass);
		
		for(String key: searchCriteria.keySet()){
			index++;
			props.remove(key);
			value = searchCriteria.get(key);
			
			column = getPropertyName(key) + index;
			select += " ?" + column;
			where += writeClauses(null, key, "simpleNum", index, -1);
			
			if(filter.equals(""))
				filter += "FILTER( regex(?" + column + ", \"" + value + "\", \"i\")";
			else
				filter += " && regex(?" + column + ", \"" + value + "\", \"i\")";
		}
		filter += ")";
		
		for(String p: props){
			index++;
			
			column = getPropertyName(p) + index;
			select += " ?" + column;
			
			countSlash = StringUtils.countMatches(p, "/");
			
			if(countSlash > 3){ //Complex properties
				where += writeClauses(null, p, "simpleNumC", index, -1);
			}
			else
				where += writeClauses(null, p, "simpleNum", index, -1);
		}
		
		result = selectQueryDB(select, where, filter, chosenClass);
		
		return result;
	}
	
	
	/* Private methods */
	private String getPropertyName(String property){

		int count;
		String column = "";
		String[] splt = null;

		count = StringUtils.countMatches(property, "/");
		splt = property.split("/");

		if(count > 3){
			column = splt[splt.length-2];
			column += "_";
			column += splt[splt.length - 1];
		}		
		else{
			column = splt[splt.length - 1];			
		}

		column = column.replace(">", "");
		column = column.replace("(", "");
		column = column.replace(")", "");
		
		if(column.contains(":")){
			String[] split = column.split("\\:");
			column = split[0];
		}

		return column;
	}
	
	private boolean checkPropertyExistance(String property, Model model){
		
		String query;
		Query qf;
		QueryExecution qexec;
		boolean result;
		
		query = "ASK{ ?s a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ "FILTER (regex(str(?s), \"" + property + "\"))}";
		
		qf = QueryFactory.create(query);
		qexec = QueryExecutionFactory.create(qf, model);
		result = qexec.execAsk();
		qexec.close();
		
		return result;
	}
	
	private String getComposedProperty(String property){

		String[] splt = null;
		String cProp = "";

		splt = property.split("/");
		for(int i = 0; i < splt.length-1; i++){
			cProp += splt[i];
			cProp += "/";
		}

		cProp = cProp.substring(0, cProp.length()-1);
		cProp += ">";

		return cProp;
	}
	
	private HashMap<String, Integer> getSourcesIndex(String sourceConjuc){

		HashMap<String, Integer> sourcesIndex = new HashMap<String, Integer>();
		String[] splitByPoint;
		String[] splitByPlus = sourceConjuc.split("\\+");

		for(int i=0; i < splitByPlus.length; i++){
			if(splitByPlus[i].contains(".")){
				splitByPoint = splitByPlus[i].split("\\.");
				if(splitByPoint[0].contains("www"))
					sourcesIndex.put(splitByPoint[1], i);
				else
					sourcesIndex.put(splitByPoint[0], i);
			}
			else
				sourcesIndex.put(splitByPlus[i], i);
		}

		return sourcesIndex;
	}
	
	private String writeClauses(String subject, String prop, String mode, int sid, int oid){

		int count;
		String column, composedProp = "";

		column = this.getPropertyName(prop);
		
		count = StringUtils.countMatches(prop, "/");
		if(count >3)
			composedProp = this.getComposedProperty(prop);

		switch(mode){

		case "simple":
			return " ?s " + prop + " ?" + column + " .";

		case "simpleComp":
			return " ?s " + composedProp + " [ " + prop + " ?" + column + " ] .";
			
		case "simpleNum":
			return " ?s " + prop + " ?" + column + sid + " .";

		case "simpleNumC":
			return " ?s " + composedProp + " [ " + prop + " ?" + column + sid + " ] .";

		case "mappingOnPropS":
			return " ?" + subject + " " +  prop + " ?" + column + " .";

		case "mappingOnPropC":
			return " ?" + subject + " " + composedProp + " [ " + prop + " ?" + column + " ] .";

		case "normalMappingS":
			return " ?" + subject + " " +  prop + " ?o" + oid + " .";

		case "normalMappingC":
			return " ?" + subject + " " + composedProp + " [ " + prop + " ?o" + oid + " ] .";

		case "remainPropsS":
			return " ?" + subject + " " +  prop + " ?" + column + sid + " .";

		case "remainPropsC":
			return " ?" + subject + " " + composedProp + " [ " + prop + " ?" + column + sid + " ] .";

		default:
			break;
		}

		return "";
	}
	
	private String selectQueryDB(String select, String where, String filter, String chosenClass){
		String q, result;
		Query query;
		QueryExecution qexec;
		ResultSet results;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		q = "SELECT" + select + "\n"
			+ "WHERE {"
			+ " ?s a \"" + chosenClass + "\" ."
			+ where
			+ filter + "}";
		
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, dbModel);
		results = qexec.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");

		qexec.close();
		
		return result;
	}
	
	private Model constructModelDB(String schema, String variables, String where, String optional){
		String q;
		Query query;
		Model result;
		QueryExecution qexec;
		
		q = "CONSTRUCT{"
				+ schema
				+ variables
			+ "}"
			+ "WHERE{"
				+ where
				+ "OPTIONAL{"
					+ optional
				+ "}"
			+ "}";
		
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, dbModel);
		result = qexec.execConstruct();		
		qexec.close();
		
		return result;
	}

	
	
	
	
}