package lpd;

import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
//import com.hp.hpl.jena.rdf.model.Resource;
//import com.hp.hpl.jena.sparql.resultset.RDFOutput;
import com.hp.hpl.jena.tdb.TDBFactory;

public class SemanticWebEngine {

	InfarmedDataConverter infarDC;
	InfomedDataConverter infoDC;

	Model dbModel;

	public SemanticWebEngine() {

		// open TDB dataset
		String directory = "TDB";
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


	}

	public String showProperties(String source) throws Exception {

		ByteArrayOutputStream go = new ByteArrayOutputStream();
		// Model model = null;
		String properties = "";

		Query query;
		QueryExecution qe;
		ResultSet results;
		String result;

		String queryString = "SELECT DISTINCT ?class\n" + "WHERE {"
				+ "?class a <http://www.w3.org/2000/01/rdf-schema#Class> ."
				+ "FILTER (regex(str(?class), '" + source + "')) }";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");

		properties = properties.concat(result);
		properties = properties.concat("\n");

		qe.close();

		go.reset();
		queryString = "SELECT DISTINCT ?property\n"
				+ "WHERE {"
				+ "?property a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ "FILTER (regex(str(?property), '" + source + "')) }";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();
		ResultSetFormatter.out(go, results, query);

		result = go.toString();
		result = result.replace("-", "_");
		result = result.replace("|", "");

		properties = properties.concat(result);
		properties = properties.concat("\n");

		qe.close();

		return properties;
	}

	public String makeQuery(String searchProperty, String value,
			ArrayList<String> sources, String[] propsList, String[] mappings){

		String select = "";
		String where = "";
		String property = "";
		String column = "";
		String concat = "";
		String propToShow = "";
		String propSource = "";
		String[] mapping = null;
		String[] splt = null;
		
		String partialResult = null;
		String result = "\n";
		
		int count = 0;
		int sid = 0;
		int oid = 0;
		
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
			propSource = getPropertySource(p);
			propList = propsBySource.get(propSource);
			propList.add(p);
		}

		if(mappings != null){			

			for(String s: mappings){
				mapping = s.split("-");
				mappingOnProp = false;

				//find out if property in mapping is to be shown in results table (if so, add it to the select clause)
				for(int i=0; i<2; i++){
					propSource = getPropertySource(mapping[i]);
					propList = propsBySource.get(propSource);
					if(propList.contains(mapping[i])){
						mappingOnProp = true;
						propToShow = mapping[i];
						column = getPropertyName(propToShow);
						select += " ?" + column;
						for(int j=0; j<2; j++){
							propSource = getPropertySource(mapping[j]);
							propList = propsBySource.get(propSource);
							propList.remove(propList.indexOf(mapping[j]));
						}
						break;
					}
				}

				if(!mappingOnProp)
					oid++;

				for(String prop: mapping){

					splt = null;
					concat = "";

					propSource = getPropertySource(prop);
					count = StringUtils.countMatches(prop, "/");
					splt = prop.split("/");
					
					if(!mappingOnProp)
						column = getPropertyName(prop);

					if(!subjectBySource.containsKey(propSource)){
						sid++;
						subjectBySource.put(propSource, "s" + sid);
					}

					if(mappingOnProp){
						if(count > 3){
							for(int i=0; i < splt.length - 1; i++){
								concat += splt[i];
								concat += "/";
							}
							concat = concat.substring(0, concat.length()-1);
							concat += ">";
							where += " ?" + subjectBySource.get(propSource) + " " + concat + " [ " + prop + " ?" + column + " ] .";
						}
						else
							where += " ?" + subjectBySource.get(propSource) + " " + prop + " ?" + column + " .";
					}
					else{
						if(count > 3){
							for(int i=0; i < splt.length - 1; i++){
								concat += splt[i];
								concat += "/";
							}
							concat = concat.substring(0, concat.length()-1);
							concat += ">";
							where += " ?" + subjectBySource.get(propSource) + " " + concat + " [ " + prop + " ?o" + oid + " ] .";
						}
						else
							where += " ?" + subjectBySource.get(propSource) + " " + prop + " ?o" + oid + " .";
					}
				}
			}

			for(String s : sources){

				propList = propsBySource.get(s.toLowerCase());

				for(String pp: propList){

					property = "";
					column = "";
					splt = null;
					concat = "";
					count = 0;

					property = pp;
					splt = pp.split("/");
					column = splt[splt.length - 1];
					column = column.replace(">", "");

					select += " ?" + column;

					count = StringUtils.countMatches(property, "/");
					if(count > 3){
						for(int i=0; i < splt.length - 1; i++){
							concat += splt[i];
							concat += "/";
						}
						concat = concat.substring(0, concat.length()-1);
						concat += ">";
						where += " ?" + subjectBySource.get(s) + " " + concat + " [ " + property + " ?" + column + " ] .";
					}

					else
						where += " ?" + subjectBySource.get(s) + " " + property + " ?" + column + " .";
				}
			}
			System.out.println("Select: " + select);
			System.out.println("Where: " + where);
			partialResult = queryDB(sources, searchProperty, value, select, where, subjectBySource);
			result = result.concat(partialResult + "\n");
		}
		
		else{
			for(String s : sources){

				select = "";
				where = "";

				propList = propsBySource.get(s.toLowerCase());
				result = result.concat(s + "\n");
				
				oneElement.clear();
				oneElement.add(s);

				for(String pp: propList){

					property = "";
					column = "";
					splt = null;
					concat = "";
					count = 0;

					property = pp;
					splt = pp.split("/");
					column = splt[splt.length - 1];
					column = column.replace(">", "");

					select += " ?" + column;

					count = StringUtils.countMatches(property, "/");
					if(count > 3){
						for(int i=0; i < splt.length - 1; i++){
							concat += splt[i];
							concat += "/";
						}
						concat = concat.substring(0, concat.length()-1);
						concat += ">";
						where += " ?s " + concat + " [ " + property + " ?" + column + " ] .";
					}

					else
						where += " ?s " + property + " ?" + column + " .";
				}
				
				partialResult = queryDB(oneElement, searchProperty, value, select, where, null);
				result = result.concat(partialResult + "\n");
			}
		}

		return result;
	}

	public String queryDB(ArrayList<String> sources, String searchProperty, String value, String select, String where, 
			HashMap<String, String> subjectBySource){
		Query query;
		QueryExecution qe;
		ResultSet results;

		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		boolean multipleSources = false;
		String queryResult = "";
		String propSource = "";
		String subjectId = "";
		String property = null;
		int index = 0;

		String[] byName = {"<http://www.infarmed.pt/Nome_do_Medicamento>", "<http://www.infomed.pt/Nome_do_Medicamento>"};
		String[] bySubstance = {"<http://www.infarmed.pt/Substância_Activa>", "<http://www.infomed.pt/Nome_Genérico>"};

		if(sources.size() == 1){
			switch(sources.get(0)){
				case "infarmed":
					index = 0;
					break;
				case "infomed":
					index = 1;
					break;
				default:
					break;
			}
		}
		else{
			index = 0;
			multipleSources = true;
		}

		switch(searchProperty){
			case "Name":
				property = byName[index];
				break;
			case "Substance":
				property = bySubstance[index];
				break;
			default:
				break;
		}

		String queryString =
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
						"SELECT ?s\n" +
						"WHERE{ ?s " + property + " ?o ."
						+ "FILTER regex(str(?o)," + "\"" + value + "\"" + ")"
						+ "}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();

		if(!results.hasNext()){
			switch(searchProperty){
				case "Name":
					try {
						infarDC.getInfarByName(dbModel, value);
						infoDC.getInfoByName(dbModel, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case "Substance":
					try {
						infarDC.getInfarBySubstance(dbModel, value);
						infoDC.getInfoBySubstance(dbModel, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		}

		qe.close();
		
		if(multipleSources){
			
			propSource = getPropertySource(property);
			subjectId = subjectBySource.get(propSource);
			
			queryString =
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
					"PREFIX Infarmed: <http://www.infarmed.pt/>" +
					"PREFIX Infomed: <http://www.infomed.pt/>" +
					"PREFIX RCM: <http://www.infarmed.pt/RCM/>" +
					"PREFIX FI: <http://www.infarmed.pt/FI/>" +
					"SELECT" + select + "\n" +
					"WHERE{ ?" + subjectId + " " + property + " ?o ."
						+ where
						+ "FILTER regex(str(?o)," + "\"" + value + "\"" + ")"
						+ "}";
		}
		else{
			
			queryString =
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX Infarmed: <http://www.infarmed.pt/>" +
				"PREFIX Infomed: <http://www.infomed.pt/>" +
				"PREFIX RCM: <http://www.infarmed.pt/RCM/>" +
				"PREFIX FI: <http://www.infarmed.pt/FI/>" +
				"SELECT" + select + "\n" +
				"WHERE{ ?s " + property + " ?o ."
					+ where
					+ "FILTER regex(str(?o)," + "\"" + value + "\"" + ")"
					+ "}";
		}


		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, dbModel);
		results = qe.execSelect();

		ResultSetFormatter.out(baos, results, query);

		queryResult = baos.toString();
		queryResult = queryResult.replace("-", "_");

		qe.close();

		return queryResult;
	}

	String getPropertySource(String property){

		String splitProp[] = null;
		String source[] = null;

		splitProp = property.split("/");
		source = splitProp[2].split("\\.");

		return source[1];
	}

	String getPropertyName(String property){

		String column = "";
		String[] splt = null;

		splt = property.split("/");
		column = splt[splt.length - 1];
		column = column.replace(">", "");

		return column;
	}

}
