package pt.ulisboa.tecnico.core;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


public class SemanticWebEngine {

//	InfarmedDataConverter infarDC;
//	InfomedDataConverter infoDC;
	
	DBpediaConverter dbpediaDC;
	LimdbConverter imdbDC;

//	private Model infarModel, infoModel;
	private Model dbInitialModel, dbFilters, dbMappings;
//	private Model dbSourcesOriginal;
	private Model dbAllSet, dbMappingSet, db1, db2;
	private int numMatches, numInstAfterMapp;
	private ArrayList<String> sources;
	private HashMap<String, Integer> sourceID;
	private HashMap<String, Integer> initialInsts;
	private HashMap<String, Integer> instsAfterAggs;
	private HashMap<String, Model> initialDBs;
	private HashMap<String, Model> dbsWithoutMappings;
	private QueryExecution qe;

	/* ---- Constructor ---- */
	public SemanticWebEngine(String s){
		
		String directory;
		Dataset dataset;
		
		sourceID = new HashMap<String, Integer>();
		initialDBs = new HashMap<String, Model>();
		initialInsts = new HashMap<String, Integer>();
		instsAfterAggs = new HashMap<String, Integer>();
		dbsWithoutMappings = new HashMap<String, Model>();
		
		dbMappingSet = ModelFactory.createDefaultModel();
		dbAllSet = ModelFactory.createDefaultModel();
		
		db1 = ModelFactory.createDefaultModel();
		db2 = ModelFactory.createDefaultModel();
		
		
		if(s.contentEquals("curator")){
						
			try {
				FileUtils.deleteDirectory(new File("..\\TDB"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			directory = "..\\TDB_filters";
			dataset = TDBFactory.createDataset(directory);
			this.dbFilters = dataset.getDefaultModel();
			
			directory = "..\\TDB_mappings";
			dataset = TDBFactory.createDataset(directory);
			this.dbMappings = dataset.getDefaultModel();
			
			directory = "..\\TDB";
			try {
				FileUtils.copyDirectory(new File("..\\TDB_new_context"), new File(directory));
			} catch (IOException e) {
				e.printStackTrace();
			}
			dataset = TDBFactory.createDataset(directory);
			this.dbInitialModel = dataset.getDefaultModel();
			
			dbpediaDC = new DBpediaConverter(db1);
			imdbDC = new LimdbConverter(db2);
			
			createInitialModelsBySource(db1);
			createInitialModelsBySource(db2);
			
			ignoringDBLang(db1);
			ignoringDBLang(db2);
			
			sources = new ArrayList<String>(initialDBs.keySet());
			for(String src:sources){
				dbInitialModel.add(initialDBs.get(src));
			}
			dbInitialModel.commit();
		}
		
		if(s.contentEquals("user")){
			
			directory = "..\\TDB_filters";
			dataset = TDBFactory.createDataset(directory);
			this.dbFilters = dataset.getDefaultModel();
			
			directory = "..\\TDB_mappings";
			dataset = TDBFactory.createDataset(directory);
			this.dbMappings = dataset.getDefaultModel();
			
			directory = "..\\TDB";
			dataset = TDBFactory.createDataset(directory);
			this.dbInitialModel = dataset.getDefaultModel();
			
			sources = (ArrayList<String>) getSources(dbInitialModel);
			createInitialModelsBySource();
			for(String src: sources){
				initialDBs.put(src, dbsWithoutMappings.get(src));
			}
		}
		
		
		defineInitialInsts();
		defineSourceID();
		resetAllSet();
		
		for(String source: sources){
			Model model = dbsWithoutMappings.get(source);
		// run a query
		String q = "select * where {?s ?p ?o}";
		Query query = QueryFactory.create(q);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File("initial_" + source + ".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ResultSetFormatter.out(fos, results);
		qexec.close();
		}
	}
//	---------------------------------------------------------------------
	
	public void setInstsAfterAggs(HashMap<String, Integer> values){
		instsAfterAggs = values;
	}
	
	public HashMap<String, Integer> getInstsAfterAggs(){
		return instsAfterAggs;
	}
	
	public int getNumInstAfterMapp() {
		return numInstAfterMapp;
	}
	
	public void setNumMappings(int i){
		numMatches = i;
	}
	
	public int getNumMatches(){
		return numMatches;
	}
	
	public void setNumInstAfterMapp(int numInstAfterMapp) {
		this.numInstAfterMapp = numInstAfterMapp;
	}
	
	public HashMap<String, Integer> getInitialInsts(){
		return initialInsts;
	}
	
	public int getResultInstNum(){
		int result = 0;
		
		for(String source: instsAfterAggs.keySet()){
			result += instsAfterAggs.get(source);
		}
		
		result -= numInstAfterMapp;
		
		return result;
	}
	
	public List<String> getSources(){
		return sources;
	}
	
	public List<String> getSources(Model model){
		Query query;
		QueryExecution qe;
		ResultSet results;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		ArrayList<String> srcs = new ArrayList<String>();
		
		String queryString, result, source;
		String[] spltResult, spltSource;
		
		queryString = "SELECT DISTINCT ?s\n"
				+ "WHERE{"
				+ " ?s a <http://www.w3.org/2000/01/rdf-schema#Class> ."
//				+ "FILTER (regex(str(?s), \"http://www.[A-Za-z]*.pt\"))"
				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query,  model);
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
			
			if(!srcs.contains(spltSource[2]))
				srcs.add(spltSource[2]);
		}
		
		return srcs;
	}
	
	private void defineSourceID(){
		int id = 0;
		
		for(String source: sources){
			id++;
			sourceID.put(source, id);
		}
	}
	
	private void defineInitialInsts(){
		for(String source: sources){
			for(String className: showSourceClasses(source, "beginning")){
				initialInsts.put(source, Integer.parseInt(this.countClassInstances(className, "beginning")));
			}
		}
	}
	
	public HashMap<String, Integer> getSourcesId(){
		return sourceID;
	}
	
	private HashMap<String, Integer> getSourcesIndex(String sourceConjuc){

		int first, last;
		String str;
		String[] splitByUnder = sourceConjuc.split("_");
		HashMap<String, Integer> sourcesIndex = new HashMap<String, Integer>();

		for(int i=0; i < splitByUnder.length; i++){
			
			str = splitByUnder[i];
			
			if(!str.contains("http://")){ //segunda parte
				last = str.lastIndexOf(".");
				sourcesIndex.put(str.substring(0, last), i);
			}
			else{ //primeira parte
				if(str.contains("www")){
					first = str.indexOf(".");
					last = str.lastIndexOf(".");
					
					if(first == last)
						sourcesIndex.put(str.substring(first, str.length()), i);
					else
						sourcesIndex.put(str.substring(0, last), i);
				}
				else{
					last = str.lastIndexOf("/");
					sourcesIndex.put(str.substring(last + 1, str.length()), i);
				}
			}
		}
		return sourcesIndex;
	}
	
	private void createInitialModelsBySource(Model db){
		Model model;
		List<String> auxSources;
		
		auxSources = getSources(db);
		
		for(String source: auxSources){
			initialDBs.put(source, db);
			
			dbsWithoutMappings.put(source, ModelFactory.createDefaultModel());
			model = dbsWithoutMappings.get(source);
			model.add(db);
		}	
	}
	
	private void createInitialModelsBySource(){
		Model model;
		Query query;
		String queryString;
		QueryExecution qexec;
		
		for(String source: sources){
			dbsWithoutMappings.put(source, ModelFactory.createDefaultModel());
			model = dbsWithoutMappings.get(source);
			
			queryString = "CONSTRUCT {?s ?p ?o}"
						+ " WHERE {"
								+ "{?s ?p ?o "
								+ "FILTER (regex(str(?s), '" + source + "'))}"
							+ "UNION"
								+ "{?s ?p ?o "
								+ "FILTER ( regex(str(?p), '" + RDFS.domain + "') && regex(str(?o), '" + source + "') )}"
						+ "}";
			query = QueryFactory.create(queryString);
			qexec = QueryExecutionFactory.create(query, dbInitialModel);
			
			model.add(qexec.execConstruct());
			qexec.close();
		}
	}
	
	private void resetAllSet() {
		dbAllSet.removeAll();
		dbAllSet.add(dbMappingSet);
		for(String key: dbsWithoutMappings.keySet()){
			dbAllSet.add(dbsWithoutMappings.get(key));
		}
	}
	
	private ArrayList<Statement> getSchema(Model model, String rule){
		Statement s;
		Query query;
		QuerySolution qs;
		ResultSet result;
		String queryString;
		QueryExecution qexec;
		
		String[] criteriaSplit;
		//String[] criteriaArray = showMappingCriteria(rule, true).split("\\r?\\n");
		String criteria = showMappingCriteria(rule, true);
		
		ArrayList<String> criteriaList = new ArrayList<String>();
		ArrayList<Statement> schema = new ArrayList<Statement>();
		
		
		//exclude rule criteria properties
		criteriaSplit = criteria.split("-");
		for(int i=0; i < criteriaSplit.length; i++){
			criteriaList.add(criteriaSplit[i].substring(criteriaSplit[i].indexOf("<")+1, criteriaSplit[i].indexOf(">")));
		}
		
		
		queryString = "SELECT ?s ?p ?o "
				+ "WHERE {"
				+ "{"
					+ "?s ?p ?o "
					+ "FILTER ( regex(str(?p), '" + RDFS.domain + "') || regex(str(?p), '" + RDFS.range + "')"
					+ " && !isBlank(?s) ) "
				+ "}"
				+ "UNION"
				+ "{"
					+ "?s ?p ?o "
					+ "FILTER ( regex(str(?p), '" + RDFS.domain + "') || regex(str(?p), '" + RDFS.range + "'))"
				+ "}"
				+ "UNION"
				+ "{"
					+ "?s ?p ?o "
					+ "FILTER ( regex(str(?p), '" + RDF.type + "') && regex(str(?o), '" + RDF.Property + "'))"
				+ "}"
				+ "}";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model);
		result = qexec.execSelect();
		
		while(result.hasNext()){
			qs = result.next();
			
			Resource r = model.getResource(qs.get("s").toString());
			Property p = model.getProperty(qs.get("p").toString());
			RDFNode o = qs.get("o");
			
			if(!criteriaList.contains(r.getURI())){
				s = ResourceFactory.createStatement(r, p, o);
				schema.add(s);
			}
		}
		qexec.close();
		
		return schema;
	}
	
	private String getPropertyName(String property){

		String column = "";
		String[] splt = null;
		
		splt = property.split("/");
		column = splt[splt.length - 1];
		
		if(column.contains(":")){
			splt = column.split("\\:");
			column = splt[0];
		}
		else{
			column = column.replace(">", "");
			column = column.replace("(", "");
			column = column.replace(")", "");
		}
		

		return column;
	}
	
	public String getPropertyDomainSource(String property){
		String s, domain = "";
		Query query;
		QuerySolution qs;
		QueryExecution qexec;
		ResultSet results;
		
		String splitProp[] = null;
		String name;
		
		s = "SELECT ?domain "
			+ "WHERE {"
				+ property + "<" + RDFS.domain + "> ?domain }";
		
		query = QueryFactory.create(s);
		qexec = QueryExecutionFactory.create(query, dbInitialModel);
		results = qexec.execSelect();
		
		if(results.hasNext()){
			qs = results.next();
			domain = qs.get("domain").toString();
			splitProp = domain.split("/");
			name = splitProp[2];
		}
		
		else{
			splitProp = property.split("/");
			name = splitProp[2];
		}

		return name;
	}
	
	public String getPropertySource(String property, boolean justName){

		String name;
		int first, last;
		String splitProp[] = null;

		splitProp = property.split("/");
		name = splitProp[2];
		
		if(justName){
			first = name.indexOf(".");
			last = name.lastIndexOf(".");
			
			
			if(name.contains("www"))
				return name.substring(first+1, last);
			else
				return name.substring(0, last);
			
		}

		return name;
	}
	
	public ArrayList<String> showSourceClasses(String source, String flowtime){

		Model model = null;
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> classes = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		switch (flowtime) {
			case "beginning":
				model = initialDBs.get(source);
				break;
			case "allNewSet":
				model = dbAllSet;
				break;
			case "oneNewSet":
				model = dbsWithoutMappings.get(source);
				if(model == null)
					model = dbMappingSet;
				break;
			default:
				break;
		}

		String queryString = "SELECT DISTINCT ?class\n"
				+ "WHERE {"
				+ "?class a <http://www.w3.org/2000/01/rdf-schema#Class> ."
				+ "FILTER (regex(str(?class), '" + source + "')) }";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("|", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			classes.add(spltResult[i]);
		}

		return classes;
	}
	
	public ArrayList<String> showClassPropertiesToCurator(String cl, String flowtime){

		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Model model = null;
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;		
//		ArrayList<String> nodes = null;
		ArrayList<String> props = new ArrayList<String>();
//		ArrayList<String> toRemove = new ArrayList<String>();
		
		switch (flowtime) {
			case "beginning":
				model = initialDBs.get(getPropertySource(cl, false));
				break;
			case "allNewSet":
				model = dbAllSet;
				break;
			case "oneNewSet":
				model = dbsWithoutMappings.get(getPropertySource(cl, false));
				if(model == null)
					model = dbMappingSet;
				break;
			default:
				break;
		}
		
		String queryString = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ "{"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .}}";
//				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
//				+ "}"
//				+ "UNION"
//				+ "{"
//				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
//				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?o ."
//				+ " ?s <http://www.w3.org/2000/01/rdf-schema#range> ?o ."
//				+ " ?s a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
//				+ " ?s <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
//				+ "}"
//				+ " FILTER (regex(str(?cl), '" + cl + "'))}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("|", "");
		result = result.replace(" ", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			props.add(spltResult[i]);
		}
		
//		nodes = showNodeProperties(cl, flowtime);
//		for(String p: props){
//			if(nodes.contains(p) || p.contains("/firstInst") || p.contains("/match"))
//				toRemove.add(p);
//		}
//		props.removeAll(toRemove);

		return props;
	}
	
	public ArrayList<String> showClassPropertiesToUser(String cl){
		
		ArrayList<String> props = new ArrayList<String>();
		
		if(cl.contentEquals("All")){
			props.add("<http://purl.org/dc/terms/title>");
			props.add("<http://dbpedia.org/property/name>");
			props.add("<http://dbpedia_data.linkedmdb.org/name:actor_name>");
		}
		if(cl.contains("dbpedia")){
			props.add("<http://dbpedia.org/property/name>");
		}
		if(cl.contains("linkedmdb")){
			props.add("<http://purl.org/dc/terms/title>");
		}
		if(cl.contains("_")){
			props.add("<http://dbpedia_data.linkedmdb.org/name:actor_name>");
			props.add("<http://purl.org/dc/terms/title>");
		}
		
		return props;
	}
	
	public ArrayList<String> showAllProperties(String flowtime){
		
		Model model = null;
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> props = new ArrayList<String>();
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		switch (flowtime) {
			case "beginning":
				model = dbInitialModel;
				break;
			case "allNewSet":
				model = dbAllSet;
				break;
			default:
				break;
		}
		
		String queryString = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("|", "");
		result = result.replace(" ", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			props.add(spltResult[i]);
		}

		return props;
	}
	
	public ArrayList<String> showClassProperties(String cl, String flowtime){

		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Model model = null;
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;		
		HashMap<String, String> nodes = null;
		ArrayList<String> props = new ArrayList<String>();
		ArrayList<String> toRemove = new ArrayList<String>();
		
		switch (flowtime) {
			case "beginning":
				model = dbInitialModel;
				break;
			case "allNewSet":
				model = dbAllSet;
				break;
			case "oneNewSet":
				model = dbsWithoutMappings.get(getPropertySource(cl, false));
				if(model == null)
					model = dbMappingSet;
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
		result = result.replace("|", "");
		result = result.replace(" ", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			if(!spltResult[i].contentEquals("<http://data.linkedmdb.org/resource/movie/actor>"))
				props.add(spltResult[i]);
		}
		
		nodes = (HashMap<String, String>) showNodeProperties(cl, flowtime);
		
		for(String p: props){
			if(nodes.containsKey(p) || p.contains("/firstInst") || p.contains("/match"))
				toRemove.add(p);
		}
		props.removeAll(toRemove);

		return props;
	}
	
	public Map<String, String> showNodeProperties(String cl, String flowtime){

		Model model = null;
		Query query;
		QuerySolution qs;
		QueryExecution qe;
		ResultSet results;
		String parent, child;
		HashMap<String, String> childParent = new HashMap<String, String>();
		
		switch (flowtime) {
			case "beginning":
				model = initialDBs.get(getPropertySource(cl, false));
				break;
			case "allNewSet":
				model = dbAllSet;
				break;
			case "oneNewSet":
				model = dbsWithoutMappings.get(getPropertySource(cl, false));
				if(model == null)
					model = dbMappingSet;
				break;
			default:
				break;
		}
		
		String queryString = "SELECT ?parent ?child\n"
				+ "WHERE {"
				+ " ?parent a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?parent <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
				+ " ?parent <http://www.w3.org/2000/01/rdf-schema#range> ?child ."
				+ " FILTER (regex(str(?cl), '" + cl + "'))}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();
		
		while(results.hasNext()){
			qs = results.next();
			parent = qs.get("parent").toString();
			child = qs.get("child").toString();
			
			if(!childParent.containsKey(child))
				childParent.put(child, parent);
		}

		return childParent;
	}
	
	public String showPropertyValues(String property, String db){
		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString;
		String output = "";
		Model modelDB;
		
		
			modelDB = dbInitialModel;
		
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
		output = output.replace("|", "");

		qe.close();
		
		return output;
	}
	
	public ArrayList<String> getDuplicateProps(String className){
		
		ArrayList<String> multiValueProps = new ArrayList<String>();
		String queryString, property;
		Query query;
		QuerySolution qs;
		QueryExecution qexec;
		ResultSet resultSet;
		
		queryString = "SELECT distinct ?p\n"
				+ "WHERE {"
				+ " ?s a '" + className + "' ."
				+ " ?s ?p ?o1, ?o2 ."
				+ " FILTER ((?o1 != ?o2) && !regex(str(?p), 'match')) }";

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, dbMappingSet);
		resultSet = qexec.execSelect();
		//ResultSetFormatter.out(System.out, resultSet);
		
		while(resultSet.hasNext()){
			qs = resultSet.next();
			property = qs.get("p").toString();
			multiValueProps.add("<" + property + ">");
		}
		
		return multiValueProps;
	}
	
	public String countClassInstances(String cl, String flowtime){
		Query query;
		QueryExecution qe;
		ResultSet results;
		String[] result;
		String queryString;
		String numInstances = "";
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		Model model = null;

		switch (flowtime) {
			case "beginning":
				model = initialDBs.get(getPropertySource(cl, false));
				break;
			case "allNewSet":
				model = dbAllSet;
				break;
			case "oneNewSet":
				model = dbsWithoutMappings.get(getPropertySource(cl, false));
				if(model == null)
					model = dbMappingSet;
				break;
			default:
				break;
		}
		
		queryString = "SELECT (COUNT(DISTINCT ?s) as ?c)\n"
				+ "WHERE {"
				+ " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?cl ."
				+ " FILTER (regex(str(?cl), '" + cl + "'))}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString().split("\\r?\\n");
		numInstances = result[3].substring(2, 5);

		numInstances = numInstances.replace(" ", "");
		numInstances = numInstances.replace("|", "");
		
		return numInstances;
	}
	
//	Filtering
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
		result = result.replace("|", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			rules.add(spltResult[i]);
		}

		return rules;
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
		
		queryString = "SELECT ?Filtering_Criteria\n"
				+ "WHERE {"
				+ "<" + rule + "> ?p ?Filtering_Criteria ."
				+ " ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> }";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbFilters);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();

		qe.close();
		
		return result;
	}
	
	public void createAggregationRule(String source, String ruleName, String criteria){
		
		String baseURI = "http://" + source;
		Resource mainClass, criteriaProp, newRule;
		
		if(!checkPropertyExistance(baseURI + "/criteria", dbFilters)){
			//System.out.println("nao existe nada ainda");
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
		
		update = "DELETE "
				+ "WHERE { "
					+ "<" + rule + "> ?p ?o }";
		
		UpdateAction.parseExecute(update, dbFilters);
		
		dbFilters.commit();
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
	
	public void registChosenRules(String chosenRules){
		
		if(checkPropertyExistance("http://filters/chosenRules", dbFilters)){
			deleteAggregationRule("http://filters");
		}
		
		Resource chooseProp = dbFilters.createResource("http://filters/chosenRules");
		chooseProp.addProperty(RDF.type, RDF.Property);
		
		Resource res = dbFilters.createResource("http://filters");
		res.addProperty(dbFilters.getProperty("http://filters/chosenRules"), chosenRules);
		
		dbFilters.commit();
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
				className = "http://" + key + "/Medicine";
	
				criteria = showAggregationCriteria(value);
				spltCriteria = criteria.split("\\r?\\n");
				criteria = spltCriteria[3];
				criteria = criteria.replace("\"", "");
				criteria = criteria.replace("|", "");
				
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
		
		//System.out.println("Query: " + queryString);
		

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbInitialModel);
		results = qe.execSelect();
		
//		try {
//			ResultSetFormatter.out(new FileOutputStream(new File("filt.txt")), results, query);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		
		resetAllSet();
		
		return results;
	}
	
	private void filterDB(ResultSet results){
		QuerySolution qs;
		String s, s1, node, delete;
		
		StmtIterator iter;
		Statement stmt;
		
		ArrayList<String> toKeep = new ArrayList<String>();
		ArrayList<String> toDelete = new ArrayList<String>();
		
		ArrayList<Statement> listStmts = new ArrayList<Statement>();
		
		// run a query
			String q = "select * where {?s ?p ?o}";
			Query query = QueryFactory.create(q);
			QueryExecution qexec = QueryExecutionFactory.create(query, dbInitialModel);
			ResultSet result = qexec.execSelect();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File("beforeFilter.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, result);
			qexec.close();
		
		
		while(results.hasNext()){
			qs = results.next();
			
			s = qs.get("s").toString();
			s1 = qs.get("s1").toString();
			
			
			if(!toKeep.contains(s) && !toDelete.contains(s)){
				toKeep.add(s);
				toDelete.add(s1);
			}
			if(toKeep.contains(s)){
				toDelete.add(s1);
			}
		}
		
		for(String subj: toDelete){
			
			q = "SELECT ?o\n"
				+ "WHERE {"
					+ "<" + subj + "> ?p ?o . "
					+ "FILTER ( isBlank(?o))}";
		
			query = QueryFactory.create(q);
			qexec = QueryExecutionFactory.create(query, dbInitialModel);
			result = qexec.execSelect();
			
			while(result.hasNext()){
				qs = result.next();
				node = qs.get("o").toString();
				
				iter = dbInitialModel.listStatements();
				while(iter.hasNext()){
					stmt = iter.next();
					if(stmt.getSubject().toString().equals(node))
						listStmts.add(stmt);
				}
				
				
			}
			qexec.close();
			dbInitialModel.remove(listStmts);
			dbInitialModel.commit();
			
			delete = "DELETE "
					+ "WHERE { "
					+ "<" + subj + "> ?p ?o }";
			
			UpdateAction.parseExecute(delete, dbInitialModel);
			
			dbInitialModel.commit();
		}
		//createInitialModelsBySource();
		resetAllSet();
		
		// run a query
			q = "select * where {?s ?p ?o}";
			query = QueryFactory.create(q);
			qexec = QueryExecutionFactory.create(query, dbInitialModel);
			result = qexec.execSelect();
			fos = null;
			try {
				fos = new FileOutputStream(new File("afterFilter.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, result);
	}
	
//	--------------------------------
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
		result = result.replace("|", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			rules.add(spltResult[i]);
		}

		return rules;
	}
	
	public String showMappingCriteria(String rule, boolean split){
		
		Query query;
		QueryExecution qe;
		ResultSet results;
		String [] splitCriteria;
		String queryString, result;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		rule = rule.replace("<", "");
		rule = rule.replace(">", "");
		rule = rule.replace(" ", "");
		
		queryString = "SELECT ?Mapping_Criteria\n"
				+ "WHERE {"
				+ "<" + rule + "> ?p ?Mapping_Criteria ."
				+ " FILTER (regex(str(?p), '/rule')) }";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbMappings);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();

		qe.close();
		
		if(split){
			splitCriteria = result.split("\\r?\\n");
			result = "";
			for(int i=3; i < splitCriteria.length-1; i++){
				result += splitCriteria[i];
			}
		}
		
		return result;
	}
	
	public void deleteMappingRule(String rule){
		String update;
		
		rule = rule.replace("<", "");
		rule = rule.replace(">", "");
		rule = rule.replace(" ", "");
		
		update = "DELETE "
				+ "WHERE { "
					+ "<" + rule + "> ?p ?o }";
		
		UpdateAction.parseExecute(update, dbMappings);
		
		dbMappings.commit();
		
		// run a query
			String q = "select * where {?s ?p ?o}";
			Query query = QueryFactory.create(q);
			QueryExecution qexec = QueryExecutionFactory.create(query, dbMappings);
			ResultSet results = qexec.execSelect();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File("afterDeleteMapping.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, results);
	}
	
	public void createMappingRule(String source, String ruleName, HashMap<String, String> queryParts){
		String baseURI = source;
		Resource mainClass, newRule;
		ArrayList<Resource> properties = new ArrayList<Resource>();
		
		mainClass = dbMappings.createResource(baseURI + "/MappingRule");
		
		if(!checkPropertyExistance(baseURI + "/schema", dbMappings)){
			//System.out.println("nao existe nada ainda");
			
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
				fos = new FileOutputStream(new File("afterCreateMapping.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, results);
	}
	
	public HashMap<String,String> mappingConstructQuery(HashMap<String, String> subjectBySource, HashMap<String,
			ArrayList<String>> propsBySource, HashMap<String,Map<String, String>> nodesBySource,
			String sourceName, String className, String[] mappingRules){

		int propID = 0;
		int numSources;
		
		String newClass;
		String parent;
		String matchProp, firstMatchProp;
		String propConj = "";
		String conjuction = "";
		
		String source = "";
		String schema = "";
		String variables = "";
		String where = "";
		String typeWhere = "";
		String optional = "";
		String rule = "";
		
		ArrayList<String> list;
		HashMap<String, Integer> sourcesIndex = getSourcesIndex(sourceName);
		HashMap<String, Integer> bnodeByParent = new HashMap<String, Integer>();
		HashMap<String, String> result = new HashMap<String, String>();
		String[] props = new String[sourcesIndex.keySet().size()];
		String[] ruleProperties;
		
		numSources = sourcesIndex.keySet().size();
		
//		--- create new mapping class ---
		newClass = sourceName + "/" + className;
		
		schema += " <" + newClass + "> <" + RDF.type + "> <" + RDFS.Class + "> .";
		variables += " ?s1 <" + RDF.type + "> '" + newClass + "' .";

		
//		--- extract info from each rule ---
		for(String mapRule: mappingRules){
			
			rule += mapRule + ",";
			
			propID++;
			ruleProperties = mapRule.split("-"); //extract info from each property inside a rule
			
			for(String property: ruleProperties){
				
				source = getPropertySource(property, false);
				
				list = (ArrayList<String>) propsBySource.get(source);
				list.remove(property);
//TODO rever schema				
//				if(nodesBySource.get(source).containsKey(property.substring(1, property.length()-1))){
//					parent = "<" + nodesBySource.get(source).get(property.substring(1, property.length()-1)) +">";
//					where += writeClauses(subjectBySource.get(source), property, "normalMappingC", parent, -1, propID);
//				}
//				else
					where += writeClauses(subjectBySource.get(source), property, "normalMappingS", null, -1, propID);

				String src = getPropertySource(property, true);
				String name = getPropertyName(property);
				props[sourcesIndex.get(src)] = name;
			}
			
			for(int index=0; index < props.length; index++){
				conjuction += props[index] + ":";
			}
			
			//Conjunction property name. <http://www.s1+s2.pt/p1:p2>
			propConj = "<" + sourceName + "/" + conjuction.substring(0, conjuction.length()-1) + ">";
			
			if(!checkPropertyExistance(propConj, dbAllSet)){
				schema += " " + propConj + " <" + RDF.type + "> <" + RDF.Property + "> .";
			}			
			
			schema += " " + propConj + " <" + RDFS.domain + "> '" + newClass + "' .";
			variables += writeClauses("s1", propConj, "normalMappingS", null, -1, propID);
			
			Arrays.fill(props, null);
			conjuction = "";
		}
		
//		--- Treat properties that don't appear in any mapping rule ---
		//int countSlash;
		ArrayList<String> properties, parentTreated;
		HashMap<String, String> nodes;
		
		
		for(String key: propsBySource.keySet()){
			
			parentTreated = new ArrayList<String>();
			properties = (ArrayList<String>) propsBySource.get(key);
			nodes = (HashMap<String, String>) nodesBySource.get(key);
			
			for(String p: properties){
				
				if(p.contentEquals("<http://data.linkedmdb.org/resource/movie/actor>"))
					continue;
				
				propID++;
				
//				countSlash = StringUtils.countMatches(p, "/");
//				if(countSlash > 3){ //Complex properties
				
				if(nodes.containsKey(p)){
					
					parent = nodes.get(p);
					if(!parentTreated.contains(parent)){
						
						schema += " " + parent + " <" + RDFS.domain + "> '" + newClass + "' .";
						
						variables += " ?s1 " + parent + " ?o" + propID + " .";
						optional += " OPTIONAL {?" + subjectBySource.get(key) + " " + parent + " ?o" + propID + " }";
						
						bnodeByParent.put(parent, propID);
						parentTreated.add(parent);
						
						propID++;
					}
					
					variables += " ?o" + bnodeByParent.get(getComposedProperty(p)) + " " + p + " ?o" + propID + " .";
					optional += " OPTIONAL {" + writeClauses(subjectBySource.get(key), p, "normalMappingC", parent, -1, propID) + "}";
				}
				else{ //Simple properties
					schema += " " + p + " <" + RDFS.domain + "> '" + newClass + "' .";
					variables += writeClauses("s1", p, "normalMappingS", null, -1, propID);
					optional += " OPTIONAL {" + writeClauses(subjectBySource.get(key), p, "normalMappingS", null, -1, propID) + "}";
				}
			}
		}
		
		firstMatchProp = sourceName + "/firstInst";
		schema += " <" + firstMatchProp + "> <" + RDF.type + "> <" + RDF.Property + "> .";
		schema += " <" + firstMatchProp + "> <" + RDFS.domain + "> '" + newClass + "' .";
		
		matchProp = sourceName + "/match";
		schema += " <" + matchProp + "> <" + RDF.type + "> <" + RDF.Property + "> .";
		schema += " <" + matchProp + "> <" + RDFS.domain + "> '" + newClass + "' .";
		
		variables += " ?s1" + " <" +  firstMatchProp + "> ?s1 .";

		for(int i = 1; i < numSources; i++){
			
			variables += " ?s1" + " <" +  matchProp + "> ?s" + (i+1) + " .";
		}
		
		rule = rule.substring(0, rule.length()-1); 
		
		result.put("schema", schema);
		result.put("variables", variables);
		result.put("where", typeWhere + where);
		result.put("optional", optional);
		result.put("rule", rule);
		
		return result;
	}
	
	public int chooseMappingRule(String rule){

		Query query;
		QuerySolution qs;
		QueryExecution qe;
		ResultSet results;
		Model resultModel;
		
		int numMatches = 0;

		String queryString, p, o;
		String schema = "", variables = "", where = "", optional = "";
		
		queryString = "SELECT ?p ?o\n"
					+ "WHERE {"
						+ "<" + rule + "> ?p ?o . }";
		
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
		
		ArrayList<Statement> stmts = getSchema(dbInitialModel, rule);
		dbMappingSet.add(stmts);
		
		// run a query
//		String q = "select * where {?s ?p ?o}";
//		query = QueryFactory.create(q);
//		qe = QueryExecutionFactory.create(query, dbMappingSet);
//		ResultSet res = qe.execSelect();
//		ResultSetFormatter.out(System.out, res);
//		qe.close();
		
		ignoringDBLang(dbInitialModel);
		resultModel = constructModelDB(schema, variables, where, optional, dbInitialModel);
		newMappingsURIs(rule, resultModel);
		dbMappingSet.add(resultModel);
		
		// run a query
//		q = "select * where {?s ?p ?o}";
//		query = QueryFactory.create(q);
//		qe = QueryExecutionFactory.create(query, dbMappingSet);
//		res = qe.execSelect();
//		ResultSetFormatter.out(System.out, res);
//		qe.close();
		
		resetAllSet();
		
		// run a query
			queryString = "select * where {?s ?p ?o}";
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, dbAllSet);
			results = qe.execSelect();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File("beforeUpdate.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, results);
			qe.close();
		// ------------
			
		numMatches = updateMappingDB();
		resetAllSet();

		// run a query
			queryString = "select * where {?s ?p ?o}";
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, dbAllSet);
			results = qe.execSelect();
			fos = null;
			try {
				fos = new FileOutputStream(new File("updateResult.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, results);
		
		return numMatches;
	}
	
	private void newMappingsURIs(String rule, Model model){
		ResIterator iter;
		Resource rsrc;
		Property prop;
		QuerySolution qs;
		Statement oldS, newS;
		String oldURI, newURI, uri;
				
		int mappRscrID = 0;
		FileOutputStream fos = null;
		
		
		ArrayList<String> mappsURIs = new ArrayList<String>();
		ArrayList<Resource> toUpdate = new ArrayList<Resource>();
		
	// ------------
		String q = "SELECT DISTINCT ?s "
					+ "WHERE {?s <" + RDF.type + "> '" + rule + "'}";
		Query query = QueryFactory.create(q);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet result = qexec.execSelect();
		
		while(result.hasNext()){
			qs = result.next();
			uri = qs.get("s").toString();
			mappsURIs.add(uri);
		}
		qexec.close();
	// ------------
		
		iter = model.listSubjects();
		
		while(iter.hasNext()){
			rsrc = iter.next();
			
			oldURI = rsrc.getURI();
			
			if(oldURI != null && mappsURIs.contains(oldURI)){
				toUpdate.add(model.getResource(oldURI));
			}
		}
		
		
		for(Resource r: toUpdate){
			
			prop = model.getProperty("http://" + getPropertySource(rule, false), "/firstInst");
			
			oldS = model.getProperty(r, prop);
			newS = oldS.changeObject(oldS.getObject().asResource().getURI());
			
			model.remove(oldS);
			model.add(newS);
			
			mappRscrID++;
			
			newURI = "http://" + getPropertySource(rule, false) + "/";
			newURI += getPropertyName(r.getURI());
			newURI = newURI.substring(0, newURI.lastIndexOf("_"));
			newURI += "_" + mappRscrID;
			
			ResourceUtils.renameResource(r, newURI);
		}
		
		// run a query
			q = "select * where {?s ?p ?o}";
			query = QueryFactory.create(q);
			qexec = QueryExecutionFactory.create(query, model);
			result = qexec.execSelect();
			fos = null;
			try {
				fos = new FileOutputStream(new File("updatedURIs.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ResultSetFormatter.out(fos, result);
			qexec.close();
		// ------------
	}
	
	public int updateMappingDB(){
		
		Model model;
		Query query;
		QuerySolution qs;
		ResultSet results;
		QueryExecution qexec;
		String queryString, deleteStr, uri, source;
		
		int numMatches = 0;			
			
		queryString = "SELECT ?p ?uri "
					+ "WHERE {"
					+ " ?s ?p ?uri . "
					+ "FILTER ( regex(str(?p), 'firstInst') || regex(str(?p), 'match') ) }";
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, dbMappingSet);
		results = qexec.execSelect();
		
		while(results.hasNext()){
			qs = results.next();
			uri = qs.get("uri").toString();
			source = getPropertySource(uri, false);
			model = dbsWithoutMappings.get(source);
			
			if(qs.get("p").toString().contains("match"))
				numMatches++;
			
			deleteStr = "DELETE "
					+ "WHERE { <" + uri + "> ?p ?o . }";
			
			UpdateAction.parseExecute(deleteStr, model);
		}
		qexec.close();
			
		return numMatches;
	}
	
	public ArrayList<String> queryTestMapping(String ruleName){

		String queryResult, instances;
		
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> multiValueProps = getDuplicateProps(ruleName);
		
		queryResult = selectAllInfo(ruleName, "oneNewSet", multiValueProps);
		instances = countClassInstances(ruleName, "oneNewSet");
		
		results.add(queryResult);
		results.add(instances);
		
		return results;
	}
	
	public String showChosenRules(){
		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString, queryResult, result;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		String[] spltResult;
		
		queryString = "SELECT ?Chosen_Rules\n"
				+ "WHERE {"
				+ " ?s ?p ?Chosen_Rules ."
				+ " ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " FILTER (regex(str(?s), 'http://filters') && regex(str(?p), 'http://filters/chosenRules')) }";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbFilters);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		queryResult = go.toString();
		
		spltResult = queryResult.split("\\r?\\n");
		result = spltResult[3];

		qe.close();
		
		return result;
	}
	
	
	
	
	
	
	public String selectAllInfo(String className, String flowtime, ArrayList<String> multiValueProps){
		int count = 0, sid;
		String output = "";
		ArrayList<String> props = null; 
		HashMap<String, String >nodes = null;
		String sortedBegin, sortedSelect, sortedEnd;
		String select = "", beginSelect = "", endSelect = "", selectGroups = "";
		String where = "", groupBy = "", column = "";
		
		Model model = null;
		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		switch (flowtime) {
			case "beginning":
				model = dbInitialModel;
				break;
			case "allNewSet":
				model = dbAllSet;
				break;
			case "oneNewSet":
				model = dbsWithoutMappings.get(getPropertySource(className, false));
				if(model == null)
					model = dbMappingSet;
				break;
			default:
				break;
		}
		
		props = this.showClassProperties(className, flowtime);
		nodes = (HashMap<String, String>) this.showNodeProperties(className, flowtime);

		if(props != null){
			for(String property: props){
				
				String s = getPropertyDomainSource(property);

				count = StringUtils.countMatches(property, ":");
				column = this.getPropertyName(property);
				
				if(count > 1){
					beginSelect += " ?" + column;
					where += this.writeClauses(null, property, "simple", null, -1, -1);
				}
				else{
					if(sourceID.containsKey(s))
						sid = sourceID.get(s);
					else
						sid = 0;

					
					//count = StringUtils.countMatches(property, "/");
					if(nodes.containsKey(property)){
						endSelect += " ?" + sid + column;
						where += this.writeClauses(null, property, "simpleNumC", nodes.get(property), sid, -1);
					}
					else{
						if(multiValueProps!=null && multiValueProps.contains(property)){
							selectGroups += " (GROUP_CONCAT(?" + sid + column + "; separator = '; ') as ?" + sid + column + "s)";
						}
						else{
							select += " ?" + sid + column;
						}
						where += " OPTIONAL {" + this.writeClauses(null, property, "simpleNum", null, sid, -1) + "}";
					}
				}

			}
			
			sortedBegin = orderString(beginSelect);
	        sortedSelect = orderString(select);
	        sortedEnd = orderString(endSelect);
	        
			select = sortedBegin + sortedSelect + selectGroups + sortedEnd;
			
			if(multiValueProps != null)
				groupBy = "GROUP BY " + sortedBegin + sortedSelect + sortedEnd;
			
			queryString = "SELECT " + select + "\n"
					+ "WHERE {"
					+ " ?s a \"" + className + "\" ."
					+ where + "}"
					+ groupBy;
			
			//System.out.println(queryString);

			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, model);
			results = qe.execSelect();
			ResultSetFormatter.out(go, results, query);

			output = go.toString();

			qe.close();
		}

		return output;
	}
	
	private Model constructModelDB(String schema, String variables, String where, String optional, Model model){
		String q;
		Query query;
		Model result;
		QueryExecution qexec;
		
		q = "select * where {?s ?p ?o}";
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File("beforeConstruct.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ResultSetFormatter.out(fos, results);
		qexec.close();
		
		q = "select * where {"
				+ " ?s1 <http://dbpedia.org/property/name> ?name ."
				+ " ?s2 <http://data.linkedmdb.org/resource/movie/actor_name> ?name .}";
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, model);
		results = qexec.execSelect();
		fos = null;
		try {
			fos = new FileOutputStream(new File("matches.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ResultSetFormatter.out(fos, results);
		qexec.close();
		
		q = "CONSTRUCT{\n"
				+ schema
				+ variables
			+ "}\n"
			+ "WHERE{\n"
				+ where + "\n"
				+ optional
			+ "}";
		
		//System.out.println(q);
		
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, model);
		result = qexec.execConstruct();
		qexec.close();
		
		q = "select * where {?s ?p ?o}";
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, result);
		results = qexec.execSelect();
		fos = null;
		try {
			fos = new FileOutputStream(new File("constructRes.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ResultSetFormatter.out(fos, results);
		
		qexec.close();
		
		return result;
	}
	
	public String getComposedSource(String newSource){
		Query query;
		QueryExecution qe;
		ResultSet results;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		int count = 0;
		String queryString, result, source, composedSrc, newCompSource, resultSrc = "";
		String[] spltResult, spltSource, spltNewSource;
		
		queryString = "SELECT DISTINCT ?s\n"
				+ "WHERE{"
				+ " ?s a <http://www.w3.org/2000/01/rdf-schema#Class> ."
				+ "FILTER (regex(str(?s), \"http://www.[A-Za-z_]*.pt\"))"
				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbMappings);
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
				sources.add(spltSource[2]);		//www.s1_s2.pt
		}
		
		for(String src: sources){
			count = 0;
			spltSource = src.split("\\.");
			composedSrc = spltSource[1];	//s1_s2
			
			spltSource = newSource.split("\\.");
			newCompSource = spltSource[1];
			spltNewSource = newCompSource.split("_");
			
			for(String s: spltNewSource){
				if(composedSrc.contains(s))
					count++;
			}
			
			if(count == spltNewSource.length){
				resultSrc = src;
				break;
			}
		}
		
		if(resultSrc == "")
			resultSrc = newSource;
		
		return resultSrc;
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
	
	private String writeClauses(String subject, String prop, String mode, String parent, int sid, int oid){

		String column;

		column = this.getPropertyName(prop);

		switch(mode){

		case "simple":
			return " ?s " + prop + " ?" + column + " .";

		case "simpleComp":
			return " ?s " + parent + " [ " + prop + " ?" + column + " ] .";
			
		case "simpleNum":
			return " ?s " + prop + " ?" + sid + column + " .";

		case "simpleNumC":
			return " ?s " + parent + " [ " + prop + " ?" + sid + column + " ] .";

		case "mappingOnPropS":
			return " ?" + subject + " " +  prop + " ?" + column + " .";

		case "mappingOnPropC":
			return " ?" + subject + " " + parent + " [ " + prop + " ?" + column + " ] .";

		case "normalMappingS":
			return " ?" + subject + " " +  prop + " ?o" + oid + " .";

		case "normalMappingC":
			return " ?" + subject + " " + parent + " [ " + prop + " ?o" + oid + " ] .";

		case "explicitMappingS":
			return " <" + subject + "> " +  prop + " ?o" + oid + " .";
			
		case "explicitMappingC":
			return " <" + subject + "> " + parent + " [ " + prop + " ?o" + oid + " ] .";

		case "remainPropsS":
			return " ?" + subject + " " +  prop + " ?" + sid + column + " .";

		case "remainPropsC":
			return " ?" + subject + " " + parent + " [ " + prop + " ?" + sid + column + " ] .";

		default:
			break;
		}

		return "";
	}
	
	private String orderString(String select) {
		
		String[] chars = select.split(" ");
		Arrays.sort(chars);
		String sortedSelect = "";
		
		for(String s: chars){
			sortedSelect += s + " ";
		}
		
		return sortedSelect;
	}
	
	//experiment
	public void ignoringDBLang(Model model){
		
		Query query;
		QuerySolution qs;
		ResultSet results;
		QueryExecution qexec;
		String queryString, deleteStr, insertStr, sub, name;
		
		ArrayList<String> toDelete = new ArrayList<>();
		ArrayList<String> toInsert = new ArrayList<>();
		
		queryString = "SELECT ?s ?name "
				+ "WHERE {"
				+ " ?s <http://dbpedia.org/property/name> ?name . "
				+ "}";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model);
		results = qexec.execSelect();
		
		while(results.hasNext()){
			qs = results.next();
			
			sub = "<" + qs.get("s").toString() + ">";
			name = qs.get("name").toString();
			
			if(name.contains("@")){
				toDelete.add(sub + " <http://dbpedia.org/property/name> '" + name + "'");
				toInsert.add(sub + " <http://dbpedia.org/property/name> '" + name.substring(0, name.lastIndexOf("@")) + "'");
			}
		}
		qexec.close();
		
		for(String del: toDelete){

			deleteStr = "DELETE "
					+ "WHERE { " + del + " }";
			
			UpdateAction.parseExecute(deleteStr, model);
		}
		qexec.close();
		
		for(String ins: toInsert){
			insertStr = "INSERT DATA { " + ins + " }";
			
			UpdateAction.parseExecute(insertStr, model);
		}
		qexec.close();
	}
	
	private HashMap<String, String> writeWhereToAll(HashMap<String, String> searchCriteria, ArrayList<String> props,
			ArrayList<String> multipleValueProp){
		Integer srcID;
		String select, endSelect, multSelect, where, optional, groupBy;
		String column, source, sourceWhere, sourceFilter;

		select = endSelect = multSelect = where = optional = groupBy = "";
		
		HashMap<String, String> nodes = new HashMap<String, String>();
		HashMap<String, String> whereBySource = new HashMap<String, String>();
		HashMap<String, String> filterBySource = new HashMap<String, String>();
		HashMap<String, String> results = new HashMap<String, String>();

		//searchCriteria = <property, value>
		for(String key: searchCriteria.keySet()){
			source = getPropertySource(key, false);
			nodes = (HashMap<String, String>) showNodeProperties(showSourceClasses(source, "allNewSet").get(0), "allNewSet");

			srcID = sourceID.get(source);
			if(srcID == null)
				srcID = 0;

			column = srcID + getPropertyName(key);

			//SELECT + WHERE
			if(!whereBySource.containsKey(source))
				whereBySource.put(source, "");
			sourceWhere = whereBySource.get(source);
			

			//if(StringUtils.countMatches(key, "/") > 3){
			if(nodes.containsKey(key)){
				if(multipleValueProp != null && multipleValueProp.contains(key))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					endSelect += " ?" + column;

				sourceWhere += writeClauses(null, key, "simpleNumC", nodes.get(key), srcID, -1);
			}
			else{
				if(multipleValueProp != null && multipleValueProp.contains(key))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					select += " ?" + column;

				sourceWhere += writeClauses(null, key, "simpleNum", null, srcID, -1);
			}
			whereBySource.put(source, sourceWhere);

			//FILTER
			if(!filterBySource.containsKey(source))
				filterBySource.put(source, "");
			sourceFilter = filterBySource.get(source);

			if(sourceFilter.isEmpty()){
				sourceFilter += "FILTER ( regex(str(?" + column + "), '" + searchCriteria.get(key) + "', 'i')";
			}
			else{
				sourceFilter += " && regex(str(?" + column + "), '" + searchCriteria.get(key) + "', 'i')";
			}
			filterBySource.put(source, sourceFilter);

			//OPTIONAL
			if(StringUtils.countMatches(key, "/") > 3)
				optional += "OPTIONAL { ?s " + getComposedProperty(key) + " [" + key + " ?" + column + " ] }\n";
			else
				optional += "OPTIONAL { ?s " + key + " ?" + column + " }\n";

			props.remove(key);
		}

		//remaining props
		for(String prop: props){
			source = getPropertySource(prop, false);

			srcID = sourceID.get(source);
			if(srcID == null)
				srcID = 0;

			column = srcID + getPropertyName(prop);

			if(StringUtils.countMatches(prop, "/") > 3){

				if(multipleValueProp != null && multipleValueProp.contains(prop))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					endSelect += " ?" + column;

				optional += "OPTIONAL { ?s " + getComposedProperty(prop) + " [" + prop + " ?" + column + " ] }\n";
			}
			else{

				if(multipleValueProp != null && multipleValueProp.contains(prop))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					select += " ?" + column;

				optional += "OPTIONAL { ?s " + prop + " ?" + column + " }\n";
			}
		}

		for(String key: filterBySource.keySet()){
			filterBySource.put(key, filterBySource.get(key)+ ")");
		}

		for(String src: whereBySource.keySet()){
			where += "{" + whereBySource.get(src) + filterBySource.get(src) + "}";
			where += " UNION ";
		}
		where = where.substring(0, where.lastIndexOf("UNION "));
		where += optional;

		select = orderString(select);
		endSelect = orderString(endSelect);

		if(multipleValueProp != null)
			groupBy = " GROUP BY " + select + endSelect;

		select += multSelect + endSelect;

		results.put("select", select);
		results.put("where", where);
		results.put("groupBy", groupBy);

		return results;
	}

	private HashMap<String, String> writeWhereToOne(HashMap<String, String> searchCriteria, ArrayList<String> props, 
			ArrayList<String> multipleValueProp, String chosenSearch, String db){


		HashMap<String, String> results = new HashMap<String, String>();
		String source, column;
		String select, endSelect, multSelect, where, filter, groupBy;
		Integer srcID;

		HashMap<String, String> nodes = new HashMap<String, String>();
		select = endSelect = multSelect = where = filter = groupBy = "";

		//searchCriteria = <property, value>
		for(String key: searchCriteria.keySet()){

			//source = getPropertySource(key, false);
			source = getPropertyDomainSource(key);
			nodes = (HashMap<String, String>) showNodeProperties(showSourceClasses(source, db).get(0), db);
			
			srcID = sourceID.get(source);
			if(srcID == null)
				srcID = 0;

			column = srcID + getPropertyName(key);

			//if(StringUtils.countMatches(key, "/") > 3){
			if(nodes.containsKey(key)){
				if(multipleValueProp != null && multipleValueProp.contains(key))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					endSelect += " ?" + column;

				where += writeClauses(null, key, "simpleNumC", nodes.get(key), srcID, -1);
			}
			else{
				if(multipleValueProp != null && multipleValueProp.contains(key))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					select += " ?" + column;

				where += writeClauses(null, key, "simpleNum", null, srcID, -1);
			}

			if(filter.isEmpty())
				filter += "FILTER ( regex(str(?" + column + "), '" + searchCriteria.get(key) + "', 'i')";
			else
				filter += " && regex(str(?" + column + "), '" + searchCriteria.get(key) + "', 'i')";

			props.remove(key);
		}
		filter += ")";

		//remaining props
		for(String prop: props){
			source = getPropertySource(prop, false);

			String clName = showSourceClasses(source, db).get(0);
			nodes = (HashMap<String, String>) showNodeProperties(clName, db);

			if(nodes.containsKey(prop))
				continue;

			srcID = sourceID.get(source);
			if(srcID == null)
				srcID = 0;

			column = srcID + getPropertyName(prop);

			//if(StringUtils.countMatches(prop, "/") > 3){
			if(nodes.containsKey(prop)){
				if(multipleValueProp != null && multipleValueProp.contains(prop))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					endSelect += " ?" + column;
				
				where += " OPTIONAL {" + writeClauses(null, prop, "simpleNumC", nodes.get(prop), srcID, -1) + "}";
			}
			else{
				if(multipleValueProp != null && multipleValueProp.contains(prop))
					multSelect += " (GROUP_CONCAT(?" + column + "; separator = '; ') as ?" + column + "s)";
				else
					select += " ?" + column;

				where += " OPTIONAL {" + writeClauses(null, prop, "simpleNum", null, srcID, -1)+ "}";
			}
		}

		select = orderString(select);
		endSelect = orderString(endSelect);

		if(multipleValueProp != null)
			groupBy = " GROUP BY " + select + endSelect;

		select += multSelect + endSelect;

		where = where + filter;

		results.put("select", select);
		results.put("where", where);
		results.put("groupBy", groupBy);

		return results;
	}
	
	public String makeSelectQuery(HashMap<String, String> searchCriteria, String chosenClass, String chosenSearch){
		
		ArrayList<String> props = null;
		String db, select, where, groupBy;
		String criteria, result, newProp;
		String like1, like2;
		//String source, sourceWhere, sourceFilter;
		
		String[] criteriaArray, mappingParts;
				
		ArrayList<String> multipleValueProp = null;
		ArrayList<String> newProps = new ArrayList<String>();
		HashMap<String, String> newCriteria = new HashMap<String, String>();
		HashMap<String, String> whereResults = new HashMap<String, String>();
		
		result = select = where = groupBy = "";
		
		//TODO
		
		if(chosenClass.contentEquals("")){
			db = "allNewSet";
			multipleValueProp = null;
		}
		else{
			db = "oneNewSet";
			multipleValueProp = getDuplicateProps(chosenClass);
			if(multipleValueProp.isEmpty())
				multipleValueProp = null;
		}

		if(chosenSearch.contentEquals("All")){
			if(chosenClass.contentEquals("")){
				props = showClassProperties("All", db);
			}
			else{
				props = showClassProperties(chosenClass, db);
				
				criteria = showMappingCriteria(chosenClass, true);
				criteria = criteria.replace("|", "");
				criteria = criteria.replace("\"", "");
				criteria = criteria.replace(" ", "");
				
				criteriaArray = criteria.split(",");
				
				for(String prop: searchCriteria.keySet()){
					if(prop.contains(":") && getPropertySource(prop, true).equals(getPropertySource(chosenClass, true))){
						mappingParts = null;
						for(int i=0; i < criteriaArray.length; i++){
							
							like1 = "<(.*)/" + getPropertyName(prop) + ">-<(.*)>";
							like2 = "<(.*)>-<(.*)/" + getPropertyName(prop) + ">";
							
							if(criteriaArray[i].matches(like1) || criteriaArray[i].matches(like2)){
								mappingParts = criteriaArray[i].split("-");
								for(int j=0; j < mappingParts.length; j++){
									newProp = mappingParts[j];
									newProp = newProp.substring(newProp.indexOf("<"), newProp.indexOf(">")+1);
									newCriteria.put(newProp, searchCriteria.get(prop));
								}
								break;
							}
						}
					}
				}
				searchCriteria.putAll(newCriteria);
				
				for(String prop: props){
					if(prop.contains(":") && getPropertySource(prop, true).equals(getPropertySource(chosenClass, true))){
						mappingParts = null;
						for(int i=0; i < criteriaArray.length; i++){
							
							like1 = "<(.*)/" + getPropertyName(prop) + ">-<(.*)>";
							like2 = "<(.*)>-<(.*)/" + getPropertyName(prop) + ">";
							
							if(criteriaArray[i].matches(like1) || criteriaArray[i].matches(like2)){								
								mappingParts = criteriaArray[i].split("-");
								for(int j=0; j < mappingParts.length; j++){
									newProp = mappingParts[j];
									newProp = newProp.substring(newProp.indexOf("<"), newProp.indexOf(">")+1);
									newProps.add(newProp);
								}
								break;
							}
						}
					}
				}
				props.addAll(newProps);
			}
			whereResults = writeWhereToAll(searchCriteria, props, multipleValueProp);
		}
		else{
			props = showClassProperties(chosenClass, db);
			whereResults = writeWhereToOne(searchCriteria, props, multipleValueProp, chosenSearch, db);
		}
		
		select = whereResults.get("select");
		where = whereResults.get("where");
		groupBy = whereResults.get("groupBy");
		
		result = selectQueryDB(select, where, groupBy, chosenClass, chosenSearch);
		
		return result;
	}
	
	private String selectQueryDB(String select, String where, String groupBy, String chosenClass, String chosenSearch){
		Model db = null;
		String q, aditionalInfo, result;
		Query query;
		QueryExecution qexec;
		ResultSet results;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		aditionalInfo = "";
		
		if(chosenSearch.contentEquals("All"))
			db = dbAllSet;
		else{
			db = dbsWithoutMappings.get(chosenSearch);
			if(db == null && !chosenClass.isEmpty())
				db = dbMappingSet;
			
			aditionalInfo += " ?s a '" + chosenClass + "' .";
		}
		
		q = "SELECT" + select + "\n"
			+ "WHERE {"
				+ aditionalInfo
				+ where
			+ "}"
			+ groupBy;
		
		System.out.println(q);
		query = QueryFactory.create(q);
		qexec = QueryExecutionFactory.create(query, dbAllSet);
		results = qexec.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();

		qexec.close();
		
		return result;
	}
}
