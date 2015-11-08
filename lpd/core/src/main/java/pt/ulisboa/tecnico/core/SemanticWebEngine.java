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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SemanticWebEngine {

	InfarmedDataConverter infarDC;
	InfomedDataConverter infoDC;

	Model dbModel;
	ArrayList<String> sources;

	public SemanticWebEngine() {
		
		sources = new ArrayList<String>();

		// open TDB dataset
		String directory = "..\\TDB";
		Dataset dataset = null;

		dataset = TDBFactory.createDataset(directory);

		// assume we want the default model, or we could get a named model here
		this.dbModel = dataset.getDefaultModel();

		if (dbModel.isEmpty()) {
			System.out.println("Model is empty!!");
			dataset.begin(ReadWrite.WRITE);

			try {
				this.infarDC = new InfarmedDataConverter(dbModel);
				this.infoDC = new InfomedDataConverter(dbModel);
				dataset.commit();
			}

			finally {
				dataset.end();
			}

			// run a query
			String q = "select * where {?s ?p ?o}";
			Query query = QueryFactory.create(q);
			QueryExecution qexec = QueryExecutionFactory.create(query, dbModel);
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

//		sources.add("Infarmed");
//		sources.add("Infomed");
	}
	
	public ArrayList<String> getSources(){
		Query query;
		QueryExecution qe;
		ResultSet results;
		ByteArrayOutputStream go = new ByteArrayOutputStream();
		
		String queryString, result, source;
		String[] spltResult;
		
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
			source = getPropertySource(spltResult[i]);
			if(!sources.contains(source))
				sources.add(source);
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

	public String getPropertyName(String property){

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

		return column;
	}

	public String getComposedProperty(String property){

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

	public ArrayList<String> showSourceClasses(String source) throws Exception{

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

	public ArrayList<String> showClassProperties(String cl) throws Exception {

		ByteArrayOutputStream go = new ByteArrayOutputStream();
		//System.out.println("SWE - SHOW CLASS PROPERTIES");

		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;
		String[] spltResult;
		ArrayList<String> props = new ArrayList<String>();

		//cl = cl.substring(2, cl.length()-2);
		//System.out.println("SWE: " + cl);
		
		String queryString = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ "{"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> \"" + cl + "\" ."
				+ "}"
				+ " UNION "
				+ "{"
				+ " ?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?property <http://www.w3.org/2000/01/rdf-schema#domain> ?o ."
				+ " ?s <http://www.w3.org/2000/01/rdf-schema#range> ?o ."
				+ " ?s a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ " ?s <http://www.w3.org/2000/01/rdf-schema#domain> \"" + cl + "\" ."
				+ "} }";

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

	public String countClassInstances(String cl){
		Query query;
		QueryExecution qe;
		ResultSet results;
		String[] result;
		String queryString;
		String numInstances = "";
		ByteArrayOutputStream go = new ByteArrayOutputStream();

		//cl = cl.substring(2, (cl.length()-2));
		//System.out.println("SWE: " + cl);

		queryString = "SELECT (COUNT(DISTINCT ?s) as ?c)\n"
				+ "WHERE {"
				+ " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> \"" + cl + "\" .}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString().split("\\r?\\n");
		numInstances = result[3].substring(2, 5);

		return numInstances;
	}
	
	public String showPropertyValues(String property){
		ByteArrayOutputStream go = new ByteArrayOutputStream();

		Query query;
		QueryExecution qe;
		ResultSet results;
		String queryString;
		String output = "";
		
		String propName = this.getPropertyName(property);
		
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
	
	public String makeConstructQuery(HashMap<String, String> subjectBySource, HashMap<String, ArrayList<String>> propsBySource,
										String sourceName, String className, String[] mappingRules){
		
		int propID = 0;
		int count = 0;
		
		String newClass;
		String propConj;
		String conjuction = "";
		
		String source = "";
		String schema = "";
		String variables = "";
		String where = "";
		String optional = "";
		
		HashMap<String, Integer> sourcesIndex = getSourcesIndex(sourceName);
		String[] props = new String[sourcesIndex.keySet().size()];
		String[] ruleProperties;
		
		newClass = "<" + sourceName + "/" + className + ">";
		
		schema += " " + newClass + " <" + RDF.type + "> <" + RDFS.Class + "> .";
		variables += " ?s1 <" + RDF.type + "> " + newClass + " .";
		
		//extract info from each rule
		for(String mapRule: mappingRules){
			propID++;
			//extract info from each property inside a rule
			ruleProperties = mapRule.split("-");
			for(String property: ruleProperties){
				source = getPropertySource(property);
				
				ArrayList<String> list = propsBySource.get(source);
				list.remove(property);
				
				//where += " ?" + subjectBySource.get(source) + " " + property + " ?o" + propID + " .";
				where += writeClauses(where, subjectBySource.get(source), property, "normalMappingS", -1, propID);
				
				props[sourcesIndex.get(source)] = getPropertyName(property);
			}
			
			for(int index=0; index < props.length; index++){
				conjuction += props[index] + ":";
			}
			
			//Conjunction property name. Ex http://www.s1+s2.pt/p1:p2
			propConj = " <" + sourceName + "/" + conjuction.substring(0, conjuction.length()-2) + ">";
			
			schema += " " + propConj + " <" + RDF.type + "> <" + RDF.Property + "> .";
			schema += " " + propConj + " <" + RDFS.domain + "> " + newClass + " .";
			
			//variables += " ?s1 " + propConj + " ?o" + propID + " .";
			variables += writeClauses(variables, "s1", propConj, "normalMappingS", -1, propID);
			
			Arrays.fill(props, null);
		}
		
		//Treat properties that don't appear in any mapping rule
		ArrayList<String> properties;
		int index, pos;
		
		
		for(String key: propsBySource.keySet()){
			
			properties = propsBySource.get(key);
			for(String p: properties){
				propID++;
				Arrays.fill(props, null);
				
				index = sourcesIndex.get(key);
				for(pos=0; pos < props.length; pos++){
					if(pos == index)
						props[pos] = getPropertyName(p);
					else
						props[pos] = "_";
				}
				conjuction = "";
				for(pos=0; pos < props.length; pos++){
					conjuction += props[pos] + ":";
				}
				conjuction = conjuction.substring(0, conjuction.length()-1);
				propConj = "<" + sourceName + "/" + conjuction + ">";
				
				schema += " " + propConj + " <" + RDF.type + "> <" + RDF.Property + "> .";
				schema += " " + propConj + " <" + RDFS.domain + "> " + newClass + " .";
				
				//variables += " ?s1 " + propConj + " ?o" + propID + " .";
				variables += writeClauses(variables, "s1", propConj, "normalMappingS", -1, propID);
				
				count = StringUtils.countMatches(p, "/");
				if(count > 3)
					optional += writeClauses(optional, subjectBySource.get(key), p, "normalMappingC", -1, propID);
				else
					optional += writeClauses(optional, subjectBySource.get(key), p, "normalMappingS", -1, propID);
			}
		}
		
		System.out.println("SCHEMA: ");
		System.out.println(schema);
		System.out.println("VARIABLES: ");
		System.out.println(variables);
		System.out.println("WHERE: ");
		System.out.println(where);
		System.out.println("OPTIONAL: ");
		System.out.println(optional);
		
		return "";
	}

	public String makeQuery(ArrayList<String> sources, HashMap<String, String> searchCriteria,
								String[] propsList, String[] mappings){

		String select = "";
		String where = "";
		String column = "";

		String propToShow = "";
		String propSource = "";
		String sourceSubj = "";

		String[] mapping = null;

		String partialResult = null;
		String result = "\n";

		int count = 0;
		int sid = 0;
		int oid = 0;
		int subjID = 0;

		boolean mappingOnProp = false; //property in mapping is also in the list of properties to show

		HashMap<String, ArrayList<String>> propsBySource = new HashMap<String, ArrayList<String>>();//key - source name; value - properties list
		HashMap<String, String> subjectBySource = new HashMap<String, String>(); //define variables per source		
		ArrayList<String> oneElement = new ArrayList<String>();//one source at a time if mapping rules doesn't exist
		ArrayList<String> propList = null; //auxiliary properties list by source


		//ignore mapping rules if only one source
		if(sources.size() == 1)
			mappings = null;

		//split properties by source
		for(int i=0; i < sources.size(); i++){
			propsBySource.put(sources.get(i).toLowerCase(), new ArrayList<String>());
		}
		for(String p: propsList){
			propSource = this.getPropertySource(p);
			propList = propsBySource.get(propSource);
			propList.add(p);
		}

		if(mappings != null){			

			for(String s: mappings){
				mapping = s.split("-");
				mappingOnProp = false;

				//find out if property in mapping is to be shown in results table (if so, add it to the select clause)
				for(int i=0; i<2; i++){
					propSource = this.getPropertySource(mapping[i]);
					propList = propsBySource.get(propSource);
					if(propList.contains(mapping[i])){
						mappingOnProp = true;
						propToShow = mapping[i];
						column = getPropertyName(propToShow);
						select += " ?" + column;
						for(int j=0; j<2; j++){
							propSource = this.getPropertySource(mapping[j]);
							propList = propsBySource.get(propSource);
							propList.remove(propList.indexOf(mapping[j]));
						}
						break;
					}
				}

				if(!mappingOnProp)
					oid++;

				for(String prop: mapping){

					propSource = getPropertySource(prop);
					count = StringUtils.countMatches(prop, "/");

					if(!mappingOnProp)
						column = getPropertyName(prop);

					if(!subjectBySource.containsKey(propSource)){
						sid++;
						subjectBySource.put(propSource, "s" + sid);
					}

					if(mappingOnProp){
						if(count > 3)
							where += this.writeClauses(where, subjectBySource.get(propSource), prop, "mappingOnPropC", -1, -1);
						else
							where += this.writeClauses(where, subjectBySource.get(propSource), prop, "mappingOnPropS", -1, -1);
					}
					else{
						if(count > 3)
							where += this.writeClauses(where, subjectBySource.get(propSource), prop, "normalMappingC", -1, oid);
						else
							where += this.writeClauses(where, subjectBySource.get(propSource), prop, "normalMappingS", -1, oid);
					}
				}
			}

			//remain properties (not included in mapping rules)
			for(String s : sources){

				propList = propsBySource.get(s.toLowerCase());
				sourceSubj = subjectBySource.get(s);
				subjID = Integer.parseInt(sourceSubj.substring(1));

				for(String property: propList){

					count = 0;
					column = "";

					column = this.getPropertyName(property);
					select += " ?" + column + subjID;

					count = StringUtils.countMatches(property, "/");

					if(count > 3)
						where += this.writeClauses(where, subjectBySource.get(s), property, "remainPropsC", sid, -1);
					else
						where += this.writeClauses(where, subjectBySource.get(s), property, "remainPropsS", sid, -1);
				}
			}

			partialResult = queryDB(sources, select, where, subjectBySource, searchCriteria);
			result = result.concat(partialResult + "\n");
		}

		//mappings==null
		else{
			
			HashMap<String, HashMap<String, String>> searchCriteriaBySource = new HashMap<String, HashMap<String, String>>();
			String key, value, source;
			
			for(String s: sources)
				searchCriteriaBySource.put(s.toLowerCase(), new HashMap<String, String>());
			
			for(Map.Entry<String, String> entry : searchCriteria.entrySet()){
				key = entry.getKey();
			    value = entry.getValue();
			    source = getPropertySource(key);
			    
			    HashMap<String, String> hmSource = searchCriteriaBySource.get(source);
			    hmSource.put(key, value);
			}
			
			for(String s : sources){

				select = "";
				where = "";

				propList = propsBySource.get(s.toLowerCase());
				result = result.concat(s + "\n");

				oneElement.clear();
				oneElement.add(s);

				for(String property: propList){

					count = 0;
					column = "";

					column = this.getPropertyName(property);
					select += " ?" + column;

					count = StringUtils.countMatches(property, "/");

					if(count > 3)
						where += this.writeClauses(where, null, property, "simpleComp", -1, -1);
					else
						where += this.writeClauses(where, null, property, "simple", -1, -1);
				}

				partialResult = queryDB(oneElement, select, where, null, searchCriteriaBySource.get(s.toLowerCase()));
				result = result.concat(partialResult + "\n");
			}
		}

		return result;
	}

	public String queryDB(ArrayList<String> sources, String select, String where, 
			HashMap<String, String> subjectBySource, HashMap<String, String> searchCriteria){
		
		Query query;
		QueryExecution qe;
		ResultSet results;

		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		//boolean multipleSources = false;
		String queryResult = "";
		String propSource = "";
		String subjectId = "";
		String key, value;
		String searchWhere = "";
		String filters = "";
		int oid = 0;

		String[] byName = {"<http://www.infarmed.pt/Nome_do_Medicamento>", "<http://www.infomed.pt/Nome_do_Medicamento>"};
		String[] bySubstance = {"<http://www.infarmed.pt/Substância_Activa>", "<http://www.infomed.pt/Nome_Genérico>"};
		
//		System.out.println("Select: " + select);
//		System.out.println("Where: " + where);
		
		if(sources.size() == 1){
			//multipleSources = false;
			
			for(Map.Entry<String, String> entry : searchCriteria.entrySet()) {
				oid++;
			    key = entry.getKey();
			    value = entry.getValue();
			    searchWhere += " ?s " + key + " ?obj" + oid + " .";
			    filters += " FILTER regex (str(?obj" + oid + "), \"" + value + "\")";
			}
		}
		else{
			//multipleSources = true;
			
			for(Map.Entry<String, String> entry : searchCriteria.entrySet()) {
				oid++;
			    key = entry.getKey();
			    value = entry.getValue();
			    
			    propSource = getPropertySource(key);
				subjectId = subjectBySource.get(propSource);
				
			    searchWhere += " ?" + subjectId + " " + key + " ?obj" + oid + " .";
			    filters += " FILTER regex (str(?obj" + oid + "), \"" + value + "\")";
			}
		}
		
		

//		String queryString =
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//				"SELECT ?s\n" +
//				"WHERE{" + searchWhere 
//					+ filters
//					+ "}";
		String queryString =
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX Infarmed: <http://www.infarmed.pt/>" +
				"PREFIX Infomed: <http://www.infomed.pt/>" +
				"PREFIX RCM: <http://www.infarmed.pt/RCM/>" +
				"PREFIX FI: <http://www.infarmed.pt/FI/>" +
				"SELECT" + select + "\n" +
				"WHERE{"
					+ where
					+ searchWhere
					+ filters
					+ "}";
		
//		System.out.println(queryString);

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();

		if(!results.hasNext()){
			
			String byNameInfar = searchCriteria.get(byName[0]);
			String byNameInfo = searchCriteria.get(byName[1]);
			String bySubsInfar = searchCriteria.get(bySubstance[0]);
			String bySubsInfo = searchCriteria.get(bySubstance[1]);
			String val;
			Boolean name = false;
			Boolean subs = false;
			
			if(byNameInfar!= null){
				name = true;
				val = byNameInfar;
			}
			else{
				if(byNameInfo != null){
					name = true;
					val = byNameInfo;
				}
				else{
					if(bySubsInfar != null){
						subs = true;
						val = bySubsInfar;
					}
					else{
						subs = true;
						val = bySubsInfo;
					}
				}
			}
			
			if(name){
				try {
					infarDC.getInfarByName(dbModel, val);
					infoDC.getInfoByName(dbModel, val);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(subs){
				try {
					System.out.println("VAL:" + val);
					infarDC.getInfarBySubstance(dbModel, val);
					infoDC.getInfoBySubstance(dbModel, val);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			qe.close();
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, dbModel);
			results = qe.execSelect();
		}


//		if(multipleSources){
//			
//			key = null;
//			value = null;
//			searchWhere = null;
//			filters = null;
//			oid = 0;
//			
//			for(Map.Entry<String, String> entry : searchCriteria.entrySet()) {
//				oid++;
//			    key = entry.getKey();
//			    value = entry.getValue();
//			    
//			    propSource = getPropertySource(key);
//			    subjectId = subjectBySource.get(propSource);
//			    
//			    searchWhere += " ?" + subjectId + " " + key + " ?obj" + oid + " .";
//			    filters += " FILTER regex (str(?obj" + oid + "), \"" + value + "\")";
//			}
//
//
//			queryString =
//					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//					"PREFIX Infarmed: <http://www.infarmed.pt/>" +
//					"PREFIX Infomed: <http://www.infomed.pt/>" +
//					"PREFIX RCM: <http://www.infarmed.pt/RCM/>" +
//					"PREFIX FI: <http://www.infarmed.pt/FI/>" +
//					"SELECT" + select + "\n" +
//					"WHERE{"
//						+ where
//						+ searchWhere
//						+ filters
//						+ "}";
//		}
//		else{
//
//			key = null;
//			value = null;
//			searchWhere = "";
//			filters = "";
//			oid = 0;
//			
//			for(Map.Entry<String, String> entry : searchCriteria.entrySet()) {
//				oid++;
//			    key = entry.getKey();
//			    value = entry.getValue();
//			    searchWhere += " ?s " + key + " ?obj" + oid + " .";
//			    filters += " FILTER regex (str(?obj" + oid + "), \"" + value + "\")";
//			}
//			
//			queryString =
//					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//							"PREFIX Infarmed: <http://www.infarmed.pt/>" +
//							"PREFIX Infomed: <http://www.infomed.pt/>" +
//							"PREFIX RCM: <http://www.infarmed.pt/RCM/>" +
//							"PREFIX FI: <http://www.infarmed.pt/FI/>" +
//							"SELECT" + select + "\n" +
//							"WHERE{"
//							+ where
//							+ searchWhere
//							+ filters
//							+ "}";
//		}
		
		
		
		//System.out.println("QUERY: " + queryString);

//		query = QueryFactory.create(queryString);
//		qe = QueryExecutionFactory.create(query, dbModel);
//		results = qe.execSelect();

		ResultSetFormatter.out(baos, results, query);

		queryResult = baos.toString();
		queryResult = queryResult.replace("-", "_");

		qe.close();

		return queryResult;
	}

	public String selectAllInfo(String className){
		int count = 0;
		String output = "";
		ArrayList<String> props = null;
		String select = "", where = "", column = "";

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

				//property = classProps[i];
				column = this.getPropertyName(property);
				select += " ?" + column;

				count = StringUtils.countMatches(property, "/");
				if(count > 3)
					where += this.writeClauses(where, null, property, "simpleComp", -1, -1);
				else
					where += this.writeClauses(where, null, property, "simple", -1, -1);
			}

			queryString = "SELECT " + select + "\n"
					+ "WHERE {"
					+ " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> \"" + className + "\" ."
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

	private String writeClauses(String where, String subject, String prop, String mode, int sid, int oid){

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


}