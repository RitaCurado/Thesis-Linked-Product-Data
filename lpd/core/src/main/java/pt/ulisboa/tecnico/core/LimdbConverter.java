package pt.ulisboa.tecnico.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class LimdbConverter {

	private String[] movies = {"Man on Fire", "Babel", "Blood Diamond", "Titanic"};

	public LimdbConverter(Model model){
		this.setSchemaModel(model);
		this.populateDB(model);

		String q = "select * where {?s ?p ?o}";
		Query query = QueryFactory.create(q);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File("initialLMDB.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ResultSetFormatter.out(fos, results);
		qexec.close();
	}

	public void setSchemaModel(Model model){
		String baseURI = "http://data.linkedmdb.org/resource/movie/film";
		ArrayList<Resource> properties = new ArrayList<Resource>();

		Resource movie = model.createResource(baseURI);		
		movie.addProperty(RDF.type, RDFS.Class);

		properties.add(model.createResource("http://purl.org/dc/terms/title"));
		properties.add(model.createResource("http://data.linkedmdb.org/resource/movie/initial_release_date"));
		properties.add(model.createResource("http://data.linkedmdb.org/resource/movie/runtime"));
		properties.add(model.createResource("http://data.linkedmdb.org/resource/movie/actor"));

		for(Resource p: properties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, movie.getURI());
		}

		properties.get(properties.size()-1).addProperty(RDFS.range, "http://data.linkedmdb.org/resource/movie/actor_name");

		Resource p; 
		p = model.createResource("http://data.linkedmdb.org/resource/movie/actor_name");
		p.addProperty(RDF.type, RDF.Property);
		p.addProperty(RDFS.domain, "http://data.linkedmdb.org/resource/movie/actor");

	}

	public void populateDB(Model model){
		String q;
		Query query;
		QueryExecution qexec;
		QuerySolution qs;
		Resource r;

		for(String movie: movies){
			q = "SELECT * "
					+"WHERE {"
					+ " ?s a <http://data.linkedmdb.org/resource/movie/film> ."
					+ " ?s <http://purl.org/dc/terms/title> '" + movie + "' ."
					+ " OPTIONAL {"
					+ " ?s <http://purl.org/dc/terms/title> ?title ."
					+ " ?s <http://data.linkedmdb.org/resource/movie/initial_release_date> ?date ."
					+ " ?s <http://data.linkedmdb.org/resource/movie/runtime> ?runtime ."
					+ " ?s <http://data.linkedmdb.org/resource/movie/actor> ?actor ."
					+ " ?actor <http://data.linkedmdb.org/resource/movie/actor_name> ?actorName ."
					+ "}"
					+ "}";

			//System.out.println(q);
			query = QueryFactory.create(q);
			qexec = QueryExecutionFactory.sparqlService( "http://data.linkedmdb.org/sparql", query );
			ResultSet results = qexec.execSelect();

			while(results.hasNext()){
				qs = results.next();
				
				r = model.createResource(qs.get("s").toString());
				r.addProperty(RDF.type, "http://data.linkedmdb.org/resource/movie/film");

				if(qs.contains("title")){
					r = model.createResource(qs.get("s").toString());
					r.addProperty(model.getProperty("http://purl.org/dc/terms/title"), qs.get("title"));
				}

				if(qs.contains("date")){
				r = model.createResource(qs.get("s").toString());
				r.addProperty(model.getProperty("http://data.linkedmdb.org/resource/movie/initial_release_date"),
						qs.get("date"));
				}

				if(qs.contains("runtime")){
				r = model.createResource(qs.get("s").toString());
				r.addProperty(model.getProperty("http://data.linkedmdb.org/resource/movie/runtime"),
						qs.get("runtime"));
				}

				if(qs.contains("actor")){
				r = model.createResource(qs.get("s").toString());
				r.addProperty(model.getProperty("http://data.linkedmdb.org/resource/movie/actor"),
						qs.get("actor"));
				}

				if(qs.contains("actorName")){
				r = model.createResource(qs.get("s").toString());
				r.addProperty(model.getProperty("http://data.linkedmdb.org/resource/movie/actor_name"),
						qs.get("actorName"));
				}
			}

			qexec.close();
		}
	}
}
