package pt.ulisboa.tecnico.core;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

	InfarmedDataConverter infarDC;
	InfomedDataConverter infoDC;

	private Model infarModel, infoModel;
	private Model dbInitialModel, dbFilters, dbMappings; //, dbSourcesOriginal
	private Model dbAllSet, dbMappingSet;
	private int numMatches;
	private ArrayList<String> sources;
	private HashMap<String, Integer> sourceID;
	private HashMap<String, Integer> initialInsts;
	private HashMap<String, Integer> instsAfterAggs;
	private HashMap<String, Model> dbsWithoutMappings;
	private QueryExecution qe;

	/* ---- Constructor ---- */
	public SemanticWebEngine(String s){
		
		String directory;
		Dataset dataset;
		
		sources = new ArrayList<String>();
		sourceID = new HashMap<String, Integer>();
		initialInsts = new HashMap<String, Integer>();
		instsAfterAggs = new HashMap<String, Integer>();
		dbsWithoutMappings = new HashMap<String, Model>();
		
		directory = "..\\TDB_filters";
		dataset = TDBFactory.createDataset(directory);
		this.dbFilters = dataset.getDefaultModel();
		
		directory = "..\\TDB_mappings";
		dataset = TDBFactory.createDataset(directory);
		this.dbMappings = dataset.getDefaultModel();
		
		
		if(s.contentEquals("curator")){
			
//			directory = "..\\TDB_test";
//			dataset = TDBFactory.createDataset(directory);
//			this.dbMappingSet = dataset.getDefaultModel();
			
			/*
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
			*/
			
			try {
				FileUtils.deleteDirectory(new File("..\\TDB"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			directory = "..\\TDB";
			dataset = TDBFactory.createDataset(directory);
			this.dbInitialModel = dataset.getDefaultModel();
			
			
			infarModel = ModelFactory.createDefaultModel();
			infoModel = ModelFactory.createDefaultModel();
			
			infarModel.read("../miniInfarDB.rdf");
			infoModel.read("../miniInfoDB.rdf");
			
			//dbModel.add(dbSourcesOriginal);
			dbInitialModel.add(infarModel);
			dbInitialModel.add(infoModel);
			dbInitialModel.commit();
			
			// run a query
				String q = "select * where {?s ?p ?o}";
				Query query = QueryFactory.create(q);
				QueryExecution qexec = QueryExecutionFactory.create(query, dbInitialModel);
				ResultSet results = qexec.execSelect();
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(new File("initialModel.txt"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				ResultSetFormatter.out(fos, results);
				qexec.close();
			// ------------
			
			//renameProblematicURIs();
			this.infarDC = new InfarmedDataConverter();
			this.infoDC = new InfomedDataConverter();
		}
		
		if(s.contentEquals("user")){
			
			directory = "..\\TDB";
			dataset = TDBFactory.createDataset(directory);
			this.dbInitialModel = dataset.getDefaultModel();
		}
		
		sources = getSources();
		defineSourceID();
		defineInitialInsts();
		//createInitialModelsBySource();
		dbMappingSet = ModelFactory.createDefaultModel();
		dbAllSet = ModelFactory.createDefaultModel();
	}

	
	public void resetDB(){
//		dbInitialModel.removeAll();
//		dbInitialModel.add(dbSourcesOriginal);
//		dbInitialModel.commit();
		
		dbInitialModel.add(infarModel);
		dbInitialModel.add(infoModel);
		dbInitialModel.commit();
		dbAllSet.removeAll();
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
			initialInsts.put(source, Integer.parseInt(this.countClassInstances(source + "/Medicine", "beginning")));
		}
	}
	
	private void createInitialModelsBySource(){
		Model model;
		Query query;
		String queryString;
		QueryExecution qexec;
		//ResultSet results;
		
		for(String source: sources){
			dbsWithoutMappings.put(source, ModelFactory.createDefaultModel());
			model = dbsWithoutMappings.get(source);
			
			queryString = "CONSTRUCT {?s ?p ?o}"
						+ " WHERE {"
								+ "{?s ?p ?o "
								+ "FILTER (regex(str(?s), '" + source + "'))}"
							+ "UNION"
								+ "{?s ?p ?o "
								+ "FILTER (isBlank(?s) && ( regex(str(?p), '" + source + "') || regex(str(?o), '" + source + "') ))}"
						+ "}";
			query = QueryFactory.create(queryString);
			qexec = QueryExecutionFactory.create(query, dbInitialModel);
			
			model.add(qexec.execConstruct());
			qexec.close();
		}
	}
	
	public void setInstsAfterAggs(HashMap<String, Integer> values){
		instsAfterAggs = values;
	}
	
	public void setNumMappings(int i){
		numMatches = i;
	}
	
	public HashMap<String, Integer> getInitialInsts(){
		return initialInsts;
	}
	
	public HashMap<String, Integer> getInstsAfterAggs(){
		return instsAfterAggs;
	}
	
	public int getNumMatches(){
		return numMatches;
	}
	
	public int getResultInstNum(){
		int result = 0;
		
		for(String source: instsAfterAggs.keySet()){
			result += instsAfterAggs.get(source);
		}
		
		result -= numMatches;
		
		return result;
	}
	
	public boolean testDBexists(){ //TODO ELIMINAR FUNÇAO
		if(dbMappingSet != null)
			return true;
		else
			return false;
	}
	
	
	/* ---- Classes ---- */
	
	public HashMap<String, Integer> getSourcesId(){
		return sourceID;
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
				+ "FILTER (regex(str(?s), \"http://www.[A-Za-z]*.pt\"))"
				+ "}";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbInitialModel);
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
			composedSrc = spltSource[1];
			
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
				model = dbInitialModel;
				break;
			case "afterAgg":
				model = dbAllSet;
				break;
			case "afterMapp":
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
		//result = result.replace("-", "_");
		result = result.replace("|", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			classes.add(spltResult[i]);
		}

		return classes;
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
				model = dbInitialModel;
				break;
			case "afterAgg":
				model = dbAllSet;
				break;
			case "afterMapp":
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

	public ArrayList<String> showClassProperties(String cl, String flowtime){

		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Model model = null;
		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> props = new ArrayList<String>();
		
		switch (flowtime) {
			case "beginning":
				model = dbInitialModel;
				break;
			case "afterAgg":
				model = dbAllSet;
				break;
			case "afterMapp":
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
		//result = result.replace("-", "_");
		result = result.replace("|", "");
		result = result.replace(" ", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			props.add(spltResult[i]);
		}

		return props;
	}
	
	public ArrayList<String> showNodeProperties(String cl, String flowtime){

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
			case "afterAgg":
				model = dbAllSet;
				break;
			case "afterMapp":
				model = dbsWithoutMappings.get(getPropertySource(cl, false));
				if(model == null)
					model = dbMappingSet;
				break;
			default:
				break;
		}
		
		String queryString = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?cl ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#range> ?o ."
				+ " FILTER (regex(str(?cl), '" + cl + "'))}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		//result = result.replace("-", "_");
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
	
	public String getPropertySource(String property, boolean justName){

		String splitProp[] = null;
		String source[] = null;

		splitProp = property.split("/");
		
		if(justName){
			source = splitProp[2].split("\\.");
			return source[1];
		}

		return splitProp[2];
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
		//output = output.replace("-", "_");
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

	public String selectAllInfo(String className, String flowtime){
		int count = 0, sid;
		String output = "";
		ArrayList<String> props = null, nodes = null;
		String sortedBegin, sortedSelect, sortedEnd;
		String select = "", beginSelect = "", endSelect = "", where = "", column = "";
		
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
			case "afterAgg":
				model = dbAllSet;
				break;
			case "afterMapp":
				model = dbsWithoutMappings.get(getPropertySource(className, false));
				if(model == null)
					model = dbMappingSet;
				break;
			default:
				break;
		}
		
		props = this.showClassProperties(className, flowtime);
		nodes = this.showNodeProperties(className, flowtime);

		if(props != null){
			for(String property: props){
				
				if(property.contains("/match") || property.contains("firstInst"))
					continue;
				
				String s = getPropertySource(property, false);

				count = StringUtils.countMatches(property, ":");
				column = this.getPropertyName(property);
				
				if(count > 1){
					beginSelect += " ?" + column;
					where += this.writeClauses(null, property, "simple", -1, -1);
				}
				else{
					sid = sourceID.get(s);

					
					count = StringUtils.countMatches(property, "/");
					if(count > 3){
						endSelect += " ?" + sid + column;
						where += this.writeClauses(null, property, "simpleNumC", sid, -1);
					}
					else{
						if(!nodes.contains(property)){
							select += " ?" + sid + column;
							where += this.writeClauses(null, property, "simpleNum", sid, -1);
						}
					}
				}

			}
			
			sortedBegin = orderString(beginSelect);
	        sortedSelect = orderString(select);
	        sortedEnd = orderString(endSelect);
	        
			select = sortedBegin + sortedSelect + sortedEnd;
			
			queryString = "SELECT " + select + "\n"
					+ "WHERE {"
					+ " ?s a \"" + className + "\" ."
					+ where + "}";

			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, model);
			results = qe.execSelect();
			ResultSetFormatter.out(go, results, query);

			output = go.toString();
			//output = output.replace("-", "_");

			qe.close();
		}

		return output;
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
			
			column = index + getPropertyName(p);
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
		qexec = QueryExecutionFactory.create(query, dbInitialModel);
		results = qexec.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");

		qexec.close();
		
		return result;
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
		
		q = "CONSTRUCT{\n"
				+ schema
				+ variables
			+ "}\n"
			+ "WHERE{\n"
				+ where
				+ "\nOPTIONAL{\n"
					+ optional
				+ "}"
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
			return " ?s " + prop + " ?" + sid + column + " .";

		case "simpleNumC":
			return " ?s " + composedProp + " [ " + prop + " ?" + sid + column + " ] .";

		case "mappingOnPropS":
			return " ?" + subject + " " +  prop + " ?" + column + " .";

		case "mappingOnPropC":
			return " ?" + subject + " " + composedProp + " [ " + prop + " ?" + column + " ] .";

		case "normalMappingS":
			return " ?" + subject + " " +  prop + " ?o" + oid + " .";

		case "normalMappingC":
			return " ?" + subject + " " + composedProp + " [ " + prop + " ?o" + oid + " ] .";

		case "explicitMappingS":
			return " <" + subject + "> " +  prop + " ?o" + oid + " .";
			
		case "explicitMappingC":
			return " <" + subject + "> " + composedProp + " [ " + prop + " ?o" + oid + " ] .";

		case "remainPropsS":
			return " ?" + subject + " " +  prop + " ?" + sid + column + " .";

		case "remainPropsC":
			return " ?" + subject + " " + composedProp + " [ " + prop + " ?" + sid + column + " ] .";

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
		//result = result.replace("-", "_");
		result = result.replace("|", "");

		qe.close();
		
		spltResult = result.split("\\r?\\n");
		for(int i=3; i < spltResult.length-1; i++){
			rules.add(spltResult[i]);
		}

		return rules;
	}
	
	public int chooseMappingRule(String rule, String mode){

		Query query;
		QuerySolution qs;
		QueryExecution qe;
		ResultSet results;
		Model resultModel;
		
		//dbMappingSet = ModelFactory.createDefaultModel();
		
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
		
		//TODO update the other sets and count instances dbsWithoutMappings
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


	private void resetAllSet() {
		dbAllSet.removeAll();
		dbAllSet.add(dbMappingSet);
		for(String key: dbsWithoutMappings.keySet()){
			dbAllSet.add(dbsWithoutMappings.get(key));
		}
	}
	
	public ArrayList<String> queryTestMapping(String ruleName){

		String queryResult;
		String instances;
		ArrayList<String> results = new ArrayList<String>();
		
		queryResult = selectAllInfo(ruleName, "afterMapp");
		instances = countClassInstances(ruleName, "afterMapp");
		
		results.add(queryResult);
		results.add(instances);
		
		return results;
	}
	
	private String getParentNode(String parent){
		String name;
		String[] split;
		String result = parent;
		
		result = result.substring(1, result.length()-1);
		split = result.split("/");
		
		result = "";		
		for(int i=0; i < split.length-1; i++){
			result += split[i];
			result += "/";
		}
		
		name = split[split.length-1];
		result += name.charAt(0);
		
		name = name.substring(1);
		name = name.toLowerCase();
		
		result += name;
		result += "Node";
		
		return result;
	}

	public HashMap<String,String> mappingConstructQuery(HashMap<String, String> subjectBySource, HashMap<String,
			ArrayList<String>> propsBySource, HashMap<String,ArrayList<String>> nodesBySource,
			String sourceName, String className, String[] mappingRules){

		int bid = 0;
		int propID = 0;
		int numSources;
		
		String newClass;
		String matchProp, firstMatchProp;
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
				
				list = propsBySource.get(source);
				list.remove(property);
				
				where += writeClauses(subjectBySource.get(source), property, "normalMappingS", -1, propID);

				props[sourcesIndex.get(getPropertySource(property, true))] = getPropertyName(property);
			}
			
			for(int index=0; index < props.length; index++){
				conjuction += props[index] + ":";
			}
			
			//Conjunction property name. <http://www.s1+s2.pt/p1:p2>
			propConj = "<" + sourceName + "/" + conjuction.substring(0, conjuction.length()-1) + ">";
			
			if(!checkPropertyExistance(propConj, dbInitialModel)){
				schema += " " + propConj + " <" + RDF.type + "> <" + RDF.Property + "> .";
			}			
			
			schema += " " + propConj + " <" + RDFS.domain + "> '" + newClass + "' .";
			variables += writeClauses("s1", propConj, "normalMappingS", -1, propID);
			
			Arrays.fill(props, null);
			conjuction = "";
		}
		
//		--- Treat properties that don't appear in any mapping rule ---
		int countSlash;
		String parent, parentNode;
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
						bid++;
						parentNode = getParentNode(parent);
						
						schema += " " + parent + " <" + RDFS.domain + "> '" + newClass + "' .";
						schema += " _:b" + bid + " <" + RDF.type + "> '" + parentNode + "' .";
						
						variables += " ?s1 " + parent + " _:b" + bid + " .";
						optional += " ?" + subjectBySource.get(key) + " " + parent + " _:b" + bid + " .";
						
						bnodeByParent.put(parent, bid);
						nodes.remove(parent);
					}
					
					variables += " _:b" + bnodeByParent.get(getComposedProperty(p)) + " " + p + " ?o" + propID + " .";
					optional += writeClauses(subjectBySource.get(key), p, "normalMappingC", -1, propID);
					
				}
				else{ //Simple properties
					schema += " " + p + " <" + RDFS.domain + "> '" + newClass + "' .";
					
					if(nodes.contains(p)){
						bid++;
						parentNode = getParentNode(p);
						
						schema += " _:b" + bid + " <" + RDF.type + "> '" + parentNode + "' .";
						
						variables += " ?s1 " + p + " _:b" + bid + " .";
						optional += " ?" + subjectBySource.get(key) + " " + p + " _:b" + bid + " .";
						
						bnodeByParent.put(p, bid);
						nodes.remove(p);
					}
					else{
					
						variables += writeClauses("s1", p, "normalMappingS", -1, propID);
						optional += writeClauses(subjectBySource.get(key), p, "normalMappingS", -1, propID);
					}
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
				+ "<" + rule + "> ?p ?Mapping_Criteria ."
				+ " FILTER (regex(str(?p), '/rule')) }";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbMappings);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
//		result = result.replace("-", "_");
//		result = result.replace("|", "");

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
				+ "<" + rule + "> ?p ?Aggregation_Criteria ."
				+ " ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> }";
		
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbFilters);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		//result = result.replace("-", "_");
		//result = result.replace("|", "");

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
		//result = result.replace("-", "_");
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
		createInitialModelsBySource();
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
	
	
	private ArrayList<Statement> getSchema(Model model, String rule){
		Statement s;
		Query query;
		QuerySolution qs;
		ResultSet result;
		String queryString;
		QueryExecution qexec;
		
		String[] criteriaSplit;
		String[] criteriaArray = showMappingCriteria(rule).split("\\r?\\n");
		String criteria = criteriaArray[3];
		
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
					+ "FILTER ( regex(str(?p), '" + RDF.type + "') && regex(str(?o), '" + RDF.Property + "'))"
				+ "}"
				+ "}";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model);
		result = qexec.execSelect();
		//ResultSetFormatter.out(System.out, result);
		
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
	
}