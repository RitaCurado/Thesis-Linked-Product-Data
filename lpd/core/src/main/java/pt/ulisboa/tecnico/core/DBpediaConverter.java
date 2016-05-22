package pt.ulisboa.tecnico.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class DBpediaConverter {
	
	private String[] actors = {
			"Johnny[a-z ]*Depp", "Al[a-z ]*Pacino", "Mel[a-z ]*Gibson", "Denzel[a-z ]*Washington", "Brad[a-z ]*Pitt",
			"Angelina[a-z ]*Jolie", "Leonardo[a-z ]*DiCaprio", "Kate[a-z ]*Winslet", "Meg[a-z ]*Ryan", "Nicole[a-z ]*Kidman"
			};

	public DBpediaConverter(Model model){
		this.setSchemaModel(model);
		this.populateDB(model);
		
		String q = "select * where {?s ?p ?o}";
		Query query = QueryFactory.create(q);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File("initialDBpedia.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ResultSetFormatter.out(fos, results);
		qexec.close();
	}
	
	public void setSchemaModel(Model model){
		
		String baseURI = "http://dbpedia.org/class/yago/Actor109765278";
		ArrayList<Resource> properties = new ArrayList<Resource>();
		
		Resource actor = model.createResource(baseURI);		
		actor.addProperty(RDF.type, RDFS.Class);
		
		properties.add(model.createResource("http://dbpedia.org/property/birthDate"));
		properties.add(model.createResource("http://dbpedia.org/property/birthName"));
		properties.add(model.createResource("http://dbpedia.org/property/birthPlace"));
		properties.add(model.createResource("http://dbpedia.org/property/deathDate"));
		properties.add(model.createResource("http://dbpedia.org/property/nationality"));
		properties.add(model.createResource("http://dbpedia.org/property/occupation"));
		properties.add(model.createResource("http://dbpedia.org/property/children"));
		properties.add(model.createResource("http://dbpedia.org/property/parents"));
		properties.add(model.createResource("http://dbpedia.org/property/spouse"));
		properties.add(model.createResource("http://dbpedia.org/property/name"));
		properties.add(model.createResource("http://dbpedia.org/ontology/abstract"));
		properties.add(model.createResource("http://dbpedia.org/ontology/imdbid"));

		for(Resource p: properties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, actor.getURI());
		}
	}
	
	public void populateDB(Model model){
		
		String q;
		Query query;
		QueryExecution qexec;
		
		for(String actor: actors){
			q = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
				+"PREFIX dbp: <http://dbpedia.org/property/>"
				+"CONSTRUCT {"
					+ "?s a <http://dbpedia.org/class/yago/Actor109765278> ."
					+ "?s dbp:name ?name ."
					+ "?s dbp:birthName ?birthName ."
					+ "?s dbp:birthDate ?birthDate ."
					+ "?s dbp:birthPlace ?birthPlace ."
					+ "?s dbp:childen ?children ."
					+ "?s dbp:spouse ?spouse ."
					+ "?s dbp:parents ?parents ."
					+ "?s dbp:occupation ?occupation ."
					+ "?s dbp:imdbid ?imdbID ."
					+ "?s dbo:abstract ?abstract ."
				+ "}"
				+"WHERE {"
					+ " ?s a <http://dbpedia.org/class/yago/Actor109765278> ."
					+ " ?s dbp:name ?name ."
					+ " ?s dbp:birthName ?birthName ."
					+ " ?s dbp:birthDate ?birthDate ."
					+ " OPTIONAL {?s dbp:birthPlace ?birthPlace}"
					+ " OPTIONAL {?s dbp:childen ?children}"
					+ " OPTIONAL {?s dbp:spouse ?spouse}"
					+ " OPTIONAL {?s dbp:parents ?parents}"
					+ " OPTIONAL {?s dbp:occupation ?occupation}"
					+ " OPTIONAL {?s dbp:imdbid ?imdbID}"
					+ " OPTIONAL {?s dbo:abstarct ?abstract}"
					+ "FILTER (regex(str(?birthName), '" + actor + "', 'i') || regex(str(?name), '" + actor + "', 'i'))"
				+ "}";
			
			query = QueryFactory.create(q);
	        qexec = QueryExecutionFactory.sparqlService( "http://dbpedia.org/sparql", query );
	        Model newModel = qexec.execConstruct();
	        qexec.close();
	        
	        model.add(newModel);
		}        
	}
}
