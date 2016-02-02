package pt.ulisboa.tecnico.core;

import java.io.ByteArrayOutputStream;

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
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


public class SemanticWebEngine {

	InfarmedDataConverter infarDC;
	InfomedDataConverter infoDC;

	Model dbModel, dbSourcesOriginal, dbFilters, dbMappings;
	ArrayList<String> sources;
	
	QueryExecution qe;

	/* ---- Constructor ---- */
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
		dbModel.commit();

	}
	
	public void resetDB(){
		dbModel.removeAll();
		dbModel.add(dbSourcesOriginal);
		dbModel.commit();
	}
	
	
	/* ---- Classes ---- */
	
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
	
	public ArrayList<String> showSourceClasses(String source){

		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> classes = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();

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

	public ArrayList<String> showClassProperties(String cl, String db){

		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Model model = dbModel;
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> props = new ArrayList<String>();
		
		switch(db){
			case "filters":
				model = dbFilters;
				break;
			case "mappings":
				model = dbMappings;
				break;
			case "":
				model = dbModel;
				break;
			default:
				break;
		}
		
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
		qe = QueryExecutionFactory.create(query, model);
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

	
	
	/* ---- Properties ---- */
	
	public String getPropertySource(String property){

		String splitProp[] = null;
		String source[] = null;

		splitProp = property.split("/");
		source = splitProp[2].split("\\.");

		return source[1];
	}
	
	public String showPropertyValues(String property, String db){
		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString;
		String output = "";
		Model modelDB;
		
		if(db.equals("Filters"))
			modelDB = dbFilters;
		else
			modelDB = dbModel;
		
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
		qe = QueryExecutionFactory.create(query, modelDB);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		output = go.toString();
		output = output.replace("-", "_");
		output = output.replace("|", "");

		qe.close();
		
		return output;
	}
	
	private HashMap<String, Integer> getSourcesIndex(String sourceConjuc){

		HashMap<String, Integer> sourcesIndex = new HashMap<String, Integer>();
		String[] splitByPoint;
		String[] splitByPlus = sourceConjuc.split("_");

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
	
	
	
	/* ---- Query methods ---- */
	
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
			props = this.showClassProperties(className, "");
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
	
	public String makeSelectQuery(HashMap<String, String> searchCriteria, String chosenClass){
		
		int index, countSlash;
		String value, column, select, where, filter, result;
		
		index = countSlash = 0;
		result = select = where = filter = "";
		
		ArrayList<String> props = showClassProperties(chosenClass, "");
		
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
	
	
	
	/* ---- Mapping Rules ---- */
	
	public void createMappingRule(String source, String ruleName, HashMap<String, String> queryParts){
		String baseURI = source;
		Resource mainClass, newRule;
		ArrayList<Resource> properties = new ArrayList<Resource>();
		
		mainClass = dbMappings.createResource(baseURI + "/MappingRule");
		
		if(!checkPropertyExistance(baseURI + "/schema", dbMappings)){
			System.out.println("nao existe nada ainda");
			
			mainClass.addProperty(RDF.type, RDFS.Class);
			
			properties.add(dbMappings.createResource(baseURI + "/schema"));
			properties.add(dbMappings.createResource(baseURI + "/variables"));
			properties.add(dbMappings.createResource(baseURI + "/where"));
			properties.add(dbMappings.createResource(baseURI + "/optional"));
			properties.add(dbMappings.createResource(baseURI + "/rule"));
			
			for(Resource p: properties){
				p.addProperty(RDF.type, RDF.Property);
				p.addProperty(RDFS.domain, mainClass.getURI());
			}
		}
		
		newRule = dbMappings.createResource(baseURI + "/" + ruleName);
		newRule.addProperty(RDF.type, mainClass.getURI());
		
		newRule.addProperty(dbMappings.getProperty(baseURI + "/schema"), queryParts.get("schema"));
		newRule.addProperty(dbMappings.getProperty(baseURI + "/variables"), queryParts.get("variables"));
		newRule.addProperty(dbMappings.getProperty(baseURI + "/where"), queryParts.get("where"));
		newRule.addProperty(dbMappings.getProperty(baseURI + "/optional"), queryParts.get("optional"));
		newRule.addProperty(dbMappings.getProperty(baseURI + "/rule"), queryParts.get("rule"));
		
		dbMappings.commit();
		
		// run a query
			String q = "select * where {?s ?p ?o}";
			Query query = QueryFactory.create(q);
			QueryExecution qexec = QueryExecutionFactory.create(query, dbMappings);
			ResultSet results = qexec.execSelect();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File("mappings.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, results);
	}
	
	public void deleteMappingRule (String rule){
		String update;
		
		rule = rule.replace("<", "");
		rule = rule.replace(">", "");
		rule = rule.replace(" ", "");
		
		update = "DELETE {?s ?p ?o}\n"
				+ "WHERE { ?s ?p ?o . "
					+ "FILTER (regex(str(?s), '" + rule + "'))}";
		
		UpdateAction.parseExecute(update, dbMappings);
		UpdateAction.parseExecute(update, dbModel);
		
		dbMappings.commit();
		dbModel.commit();
	}
	
	public ArrayList<String> showMappingRules(String source){
		
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result, queryString = "";
		String[] spltResult;
		ArrayList<String> rules = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		if(source.equals("")){
			
			queryString = "SELECT ?rule\n"
						+ "WHERE {"
						+ " ?rule <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class ."
						+ "FILTER (regex(str(?class), \"MappingRule\")) }";
		}
		else{
			queryString = "SELECT ?rule\n"
						+ "WHERE {"
						+ " ?rule <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class ."
						+ "FILTER (regex(str(?rule), \"" + source + "\") && (regex(str(?class), \"MappingRule\"))) }";
		}
		

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbMappings);
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
	
	public void chooseMappingRule(String rule){
		
		Query query;
		QuerySolution qs;
		QueryExecution qe;
		ResultSet results;
		
		String queryString, p, o;
		String schema = "", variables = "", where = "", optional = "";
		
		queryString = "SELECT ?p ?o\n"
					+ "WHERE {"
						+ " ?s ?p ?o ."
						+ " FILTER (regex(str(?s), \"" +  rule +  "\"))}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbMappings);
		results = qe.execSelect();
		
		while(results.hasNext()){
			qs = results.next();
			
			p = qs.get("p").toString();
			o = qs.get("o").toString();
			
			p = getPropertyName(p);
			
			switch (p) {
				case "schema":
					schema = o;
					break;
	
				case "variables":
					variables = o;
					break;
					
				case "where":
					where = o;
					break;
					
				case "optional":
					optional = o;
					break;
				
				default:
					break;
			}
		}
		
		Model resultModel = constructModelDB(schema, variables, where, optional);
		
		dbModel.add(resultModel);
		dbModel.commit();
	}
	
	public HashMap<String,String> mappingConstructQuery(HashMap<String, String> subjectBySource, HashMap<String,
			ArrayList<String>> propsBySource, HashMap<String,ArrayList<String>> nodesBySource,
			String sourceName, String className, String[] mappingRules){
		
		int propID = 0;
		
		String newClass;
		String propConj = "";
		String conjuction = "";
		
		String source = "";
		String schema = "";
		String variables = "";
		String where = "";
		String optional = "";
		String rule = "";
		
		ArrayList<String> list;
		HashMap<String, Integer> sourcesIndex = getSourcesIndex(sourceName);
		HashMap<String, String> result = new HashMap<String, String>();
		String[] props = new String[sourcesIndex.keySet().size()];
		String[] ruleProperties;
		
//		--- create new mapping class ---
		newClass = sourceName + "/" + className;
		
		schema += " <" + newClass + "> <" + RDF.type + "> <" + RDFS.Class + "> .";
		variables += " ?s1 <" + RDF.type + "> \"" + newClass + "\" .";

		
//		--- extract info from each rule ---
		for(String mapRule: mappingRules){
			
			rule += mapRule + "\n";
			
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
		
		result.put("schema", schema);
		result.put("variables", variables);
		result.put("where", where);
		result.put("optional", optional);
		result.put("rule", rule);
		
		return result;
	}
	
	public String showMappingCriteria(String rule){
	
		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString, result;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		rule = rule.replace("<", "");
		rule = rule.replace(">", "");
		rule = rule.replace(" ", "");
		
		queryString = "SELECT ?Mapping_Criteria\n"
				+ "WHERE {"
				+ " ?s ?p ?Mapping_Criteria ."
				+ " FILTER ( regex(str(?s), '" + rule + "') && regex(str(?p), '/rule') ) }";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbMappings);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");

		qe.close();
		
		return result;
	}

	

	/* ---- Filtering Data (Aggregation Rules) ---- */
	
	public void createAggregationRule(String source, String ruleName, String criteria){
		
		String baseURI = "http://" + source;
		Resource mainClass, criteriaProp, newRule;
		
		if(!checkPropertyExistance(baseURI + "/criteria", dbFilters)){
			System.out.println("nao existe nada ainda");
			mainClass = dbFilters.createResource(baseURI + "/AggregationRule");
			mainClass.addProperty(RDF.type, RDFS.Class);
			
			criteriaProp = dbFilters.createResource(baseURI + "/criteria");
			criteriaProp.addProperty(RDF.type, RDF.Property);
			criteriaProp.addProperty(RDFS.domain, mainClass.getURI());
		}
		
		newRule = dbFilters.createResource(baseURI + "/" + ruleName);
		newRule.addProperty(RDF.type, baseURI + "/AggregationRule");
		newRule.addProperty(dbFilters.getProperty(baseURI + "/criteria"), criteria);
		
		dbFilters.commit();
		
		// run a query
			String q = "select * where {?s ?p ?o}";
			Query query = QueryFactory.create(q);
			QueryExecution qexec = QueryExecutionFactory.create(query, dbFilters);
			ResultSet results = qexec.execSelect();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File("aggs.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, results);
	}
	
	public void deleteAggregationRule(String rule){
		String update;
		
		rule = rule.replace("<", "");
		rule = rule.replace(">", "");
		rule = rule.replace(" ", "");
		
		update = "DELETE {?s ?p ?o}\n"
				+ "WHERE { ?s ?p ?o . "
					+ "FILTER (regex(str(?s), '" + rule + "'))}";
		
		UpdateAction.parseExecute(update, dbFilters);
		UpdateAction.parseExecute(update, dbModel);
		
		dbFilters.commit();
		dbModel.commit();
	}
	
	public String showAggregationCriteria(String rule){
		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString, result;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		rule = rule.replace("<", "");
		rule = rule.replace(">", "");
		rule = rule.replace(" ", "");
		
		queryString = "SELECT ?Aggregation_Criteria\n"
				+ "WHERE {"
				+ " ?s ?p ?Aggregation_Criteria ."
				+ " ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " FILTER (regex(str(?s), '" + rule + "')) }";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbFilters);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");

		qe.close();
		
		return result;
	}
	
	public ArrayList<String> showAggregationRules(String source){
		
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result, queryString = "";
		String[] spltResult;
		ArrayList<String> rules = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		if(source.equals("")){
			
			queryString = "SELECT ?rule\n"
						+ "WHERE {"
						+ " ?rule <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class ."
						+ "FILTER (regex(str(?class), \"AggregationRule\")) }";
		}
		else{
			queryString = "SELECT ?rule\n"
						+ "WHERE {"
						+ " ?rule <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class ."
						+ "FILTER (regex(str(?rule), \"" + source + "\") && (regex(str(?class), \"AggregationRule\"))) }";
		}
		

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
	
	public void filterData(HashMap<String, String> rulesBySource){
		
		// 1. for each source
		// 2. get criteria properties
		// 3. make query to get ResultSet from duplicates
		// 4. filterDB functions to remove all subjects of resultSet from DB
		
		String value, criteria, className;
		String[] spltCriteria, criteriaProps;
		ResultSet resultSet;
		
//		String queryString = "select * where {?s ?p ?o}";
//		Query query = QueryFactory.create(queryString);
//		QueryExecution qexec = QueryExecutionFactory.create(query, dbModel);
//		ResultSet results = qexec.execSelect();
//		try {
//			ResultSetFormatter.out(new FileOutputStream(new File("beforeDelete.txt")), results, query);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		qexec.close();
		
		for(String key: rulesBySource.keySet()){
			value = rulesBySource.get(key);
			
			if(!value.contains("/None")){
				className = "http://www." + key + ".pt/Medicine";
	
				criteria = showAggregationCriteria(value);
				spltCriteria = criteria.split("\\r?\\n");
				criteria = spltCriteria[3];
				criteria = criteria.replace("\"", "");
				
				criteriaProps = criteria.split(",");
				
				resultSet = makeFilteringQuery(className, criteriaProps);
				filterDB(resultSet);
				qe.close();
			}
		}
		
//		queryString = "select * where {?s ?p ?o}";
//		query = QueryFactory.create(queryString);
//		qexec = QueryExecutionFactory.create(query, dbModel);
//		results = qexec.execSelect();
//		try {
//			ResultSetFormatter.out(new FileOutputStream(new File("afterDelete.txt")), results, query);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		qexec.close();
	}
	
	private ResultSet makeFilteringQuery(String className, String[] props){
		Query query;
		ResultSet results;
		String queryString, propName;
		
		queryString = "SELECT ?s ?s1\n"
				+ "WHERE { "
					+ "?s a \"" + className + "\" . "
					+ "?s1 a \"" + className + "\" . ";
		
		for(int i=0; i<props.length; i++){
			propName = getPropertyName(props[i]);
			queryString += "?s " + props[i] + " ?" + propName + " . ";
			queryString += "?s1 " + props[i] + " ?" + propName + " . ";
		}
		
		queryString += "FILTER (?s != ?s1) }";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		
		return results;
	}
	
	private void filterDB(ResultSet results){
		QuerySolution qs;
		String s, s1;
		
		StmtIterator iter;
		Statement stmt;
		
		ArrayList<String> toKeep = new ArrayList<String>();
		ArrayList<String> toDelete = new ArrayList<String>();
		
		ArrayList<Statement> listStmts = new ArrayList<Statement>();
		ArrayList<String> listNodes = new ArrayList<String>();
		
		
		while(results.hasNext()){
			qs = results.next();
			
			s = qs.get("s").toString();
			s1 = qs.get("s1").toString();
			
			if(!toKeep.contains(s) && !toDelete.contains(s)){
				toKeep.add(s);
				toDelete.add(s1);
			}
			if(toKeep.contains(s))
				toDelete.add(s1);
		}
		
		for(String subj: toDelete){
			iter = dbModel.listStatements();
			while(iter.hasNext()){
				stmt = iter.next();
				if(stmt.getSubject().getURI()!= null && stmt.getSubject().getURI().equals(subj)){
					listStmts.add(stmt);
					if(stmt.getObject().isAnon())
						listNodes.add(stmt.getObject().toString());
				}
			}
		}
		
		for(String node: listNodes){
			iter = dbModel.listStatements();
			while(iter.hasNext()){
				stmt = iter.next();
				if(stmt.getSubject().toString().equals(node))
					listStmts.add(stmt);
			}
		}
		
		dbModel.remove(listStmts);
		dbModel.commit();
	}
}