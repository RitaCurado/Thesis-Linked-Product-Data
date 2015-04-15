package lpdV01;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;

import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class MedicineInfo {
	
	private Model infarModel;
	private Model infoModel;
	
	//private ArrayList<Model> infarModel;
	//private ArrayList<Model> infoModel;
	//private Model completeModel;
	
	public MedicineInfo(){
		
		infarModel = ModelFactory.createDefaultModel();
		infoModel = ModelFactory.createDefaultModel();
		
		//infarModel = new ArrayList<Model>();
		//infoModel = new ArrayList<Model>();
		//completeModel = ModelFactory.createDefaultModel();
	}
	
	public Model getInfarModel(){
		return this.infarModel;
	}
	
	public void setInfarModel(Model m){
		this.infarModel = m;
	}
	
	public Model getInfoModel(){
		return this.infoModel;
	}
	
	public void setInfoModel(Model m){
		this.infoModel = m;
	}
	
//	public Model getCompleteModel(){
//		return this.completeModel;
//	}
//	
//	public void setCompleteModel(Model m){
//		this.completeModel = m;
//	}
	
//	public ArrayList<Model> getInfarModel(){
//		return this.infarModel;
//	}
//	
//	public void clearInfarModel(){
//		this.infarModel.clear();
//	}
//	
//	public ArrayList<Model> getInfoModel(){
//		return this.infoModel;
//	}
//	
//	public void clearInfoModel(){
//		this.infoModel.clear();
//	}


	public void getInfarmedInfo(HtmlPage page, Medicine med, WebClient webClient) throws Exception{
		
		HtmlTable table = (HtmlTable) page.getElementById("mainResult");

		String substancyAct="";	    
		String name;
		String type;
		String dosage;
		String numUnits;
		String generic;
		String priceTemp;
		int prescriptionCode;
		float price;
		HtmlAnchor ha;
		String rcm;
		String fi;
		ArrayList<String> rcmInfo;
		ArrayList<String> fiInfo;
		
		Model m;		
		
		String marketed = "Comercializado";
		ReadPDF reader = new ReadPDF();
		TopLevelWindow ww;
		String partialHref;
		int numMeds = 0;
		int row = 1;

		while (numMeds < 6){
			
			if(marketed.equals(table.getCellAt(row, 12).asText())){
				
				numMeds++;
				
				substancyAct = table.getCellAt(row, 1).asText().toLowerCase();	    
				name = table.getCellAt(row, 2).asText();
				type = table.getCellAt(row, 3).asText();
				dosage = table.getCellAt(row, 4).asText();
				numUnits = table.getCellAt(row, 5).asText();
				prescriptionCode =  Integer.parseInt(table.getCellAt(row, 6).asText());

				priceTemp = table.getCellAt(row, 8).asText();
				if(priceTemp.equals(""))
					price = 0;
				else{
					priceTemp = priceTemp.replace(',', '.');
					price = Float.parseFloat(priceTemp.toString());
				}
				
				generic = table.getCellAt(row, 11).asText();
				
				
				HtmlTableCell cell = table.getCellAt(row, 13);
				Iterator<DomElement> anchors = cell.getChildElements().iterator();
				
//				-------------------------------------------------------------------------------
				ha = (HtmlAnchor)anchors.next();
				ha = (HtmlAnchor)anchors.next();
				partialHref = ha.getAttribute("href");
				rcm = partialHref.replace("../../", "http://www.infarmed.pt/");
				ha.setAttribute("href", rcm);
				
				InputStream is = ha.openLinkInNewWindow().getWebResponse().getContentAsStream();
				rcmInfo = reader.readRCM(is);
				
//		        --------------------------------------------------------------------------------
				ww = (TopLevelWindow) webClient.getCurrentWindow().getParentWindow();
				ww.close();
				
				ha = (HtmlAnchor)anchors.next();
				partialHref = ha.getAttribute("href");
				fi = partialHref.replace("../../", "http://www.infarmed.pt/");
				ha.setAttribute("href", fi);
				
				is = ha.openLinkInNewWindow().getWebResponse().getContentAsStream();
				fiInfo = reader.readFI(is);
				
				ww = (TopLevelWindow) webClient.getCurrentWindow().getParentWindow();
				ww.close();
//				--------------------------------------------------------------------------------

				m = med.infarmedModel(getInfarModel(), substancyAct, name, type, dosage,
						numUnits, prescriptionCode, price, generic, rcm, rcmInfo, fi, fiInfo);
				
				setInfarModel(m);
				//getInfarModel().add(m);
				
				name = type = dosage = numUnits = generic = rcm = fi = "";
				prescriptionCode = 0;
				price = 0;
				rcmInfo.clear();
				fiInfo.clear();
			}			
			
			row++;
		}
	}
	
	public void getInfomedInfo(HtmlPage pageResult, Medicine med) {		
		
		HtmlTable table = (HtmlTable)pageResult.getElementsByTagName("table").item(2);
		
		//System.out.println(table.getRowCount());
		
		int line = 1;
		int numMeds = 0;
		int length;
		String permited = "Autorizado";
		
		String actSubs="";
		String name;
		String dosage;
		String type;
		String generic;
		String holder;
		
		Model m = null;
		int numRows = table.getRowCount();
		int maxRows;		
		
		if(numRows > 4)
			maxRows = 4;
		else
			maxRows = numRows - 1;
			
		while(numMeds < maxRows){
			String coluna5 = table.getCellAt(line, 5).asText();
			if(permited.equals(coluna5)){
				numMeds++;
				
				String s = table.getCellAt(line, 0).asText().toLowerCase();
				length = s.length();
				actSubs = s.substring(0, length-1);
				
				name = table.getCellAt(line, 1).asText();
				type = table.getCellAt(line, 2).asText();
				dosage = table.getCellAt(line, 3).asText();
				holder = table.getCellAt(line, 4).asText();
				generic = table.getCellAt(line, 7).asText();
				
				m = med.infomedModel(getInfoModel(), actSubs, name, type, dosage, holder, generic);
				
				setInfoModel(m);
				//getInfoModel().add(m);
			}
			
			name = type = dosage = holder = generic = "";			
			line++;
		}
		//m.write(System.out, "TTL");
	}
	
	
//	---------------------------------------------- By Name ---------------------------------------------------------------
	public void getInfoByName(String medicine, Medicine med) throws Exception{
		WebClient webClient = new WebClient();

		HtmlPage page = webClient.getPage("http://www.infarmed.pt/infomed/inicio.php");
		
		DomElement button = page.getElementById("button_");
		HtmlButton b = (HtmlButton) button;
		HtmlPage page2 = b.click();
		
		HtmlTable tablePesquisa = (HtmlTable) page2.getElementById("tabela_pesquisa");
		HtmlTextInput input = (HtmlTextInput) tablePesquisa.getCellAt(2, 1).getElementsByTagName("input").item(0);
		input.setAttribute("value", medicine);
		
		HtmlTable tableButtons = (HtmlTable)page2.getElementsByTagName("table").item(2);		
		HtmlImageInput pesquisar = (HtmlImageInput)tableButtons.getCellAt(0, 0).getElementsByTagName("input").item(0);		
		HtmlPage pageResult = (HtmlPage) pesquisar.click();
		
		getInfomedInfo(pageResult, med);
		
		webClient.closeAllWindows();
	}
	
	
	public void getInfarByName(String name, Medicine med) throws Exception{
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");

		DomElement nameMed = page.getElementById("i_MarcaCom");		
		nameMed.setAttribute("value", name);

		DomElement button = page.getElementById("pesquisa");
		HtmlButtonInput b = (HtmlButtonInput) button;
		HtmlPage page2 = b.click();
		
		getInfarmedInfo(page2, med, webClient);
		
		webClient.closeAllWindows();
	}
//	------------------------------------------------------------------------------------------------------------------------
	
	
//	---------------------------------------------- By Substance ---------------------------------------------------------------
	public void getInfoBySubstance(String substance, Medicine med) throws Exception{
		WebClient webClient = new WebClient();

		HtmlPage page = webClient.getPage("http://www.infarmed.pt/infomed/inicio.php");
		
		DomElement button = page.getElementById("button_");
		HtmlButton b = (HtmlButton) button;
		HtmlPage page2 = b.click();
		
		HtmlTable tablePesquisa = (HtmlTable) page2.getElementById("tabela_pesquisa");
		HtmlTextInput input = (HtmlTextInput) tablePesquisa.getCellAt(1, 1).getElementsByTagName("input").item(0);		
		input.setAttribute("value", substance);
		
		HtmlTable tableButtons = (HtmlTable)page2.getElementsByTagName("table").item(2);		
		HtmlImageInput pesquisar = (HtmlImageInput)tableButtons.getCellAt(0, 0).getElementsByTagName("input").item(0);		
		HtmlPage pageResult = (HtmlPage) pesquisar.click();
		
		getInfomedInfo(pageResult, med);
		
		webClient.closeAllWindows();
	}
	
	
	public void getInfarBySubstance(String substance, Medicine med) throws Exception{
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");

		DomElement nameMed = page.getElementById("i_dci");
		
		//remove accents
		substance = Normalizer.normalize(substance, Normalizer.Form.NFD);
		substance = substance.replaceAll("[^\\p{ASCII}]", "");
		
		nameMed.setAttribute("value", substance.toUpperCase());

		DomElement button = page.getElementById("pesquisa");
		HtmlButtonInput b = (HtmlButtonInput) button;
		HtmlPage page2 = b.click();
		
		getInfarmedInfo(page2, med, webClient);
		
		webClient.closeAllWindows();
	}
//	------------------------------------------------------------------------------------------------------------------------
	
	
//	---------------------------------------------- By Code ---------------------------------------------------------------
	public void getInfoByCode(String code, Medicine med) throws Exception{
		WebClient webClient = new WebClient();

		HtmlPage page = webClient.getPage("http://www.infarmed.pt/infomed/inicio.php");
		
		DomElement button = page.getElementById("button_");
		HtmlButton b = (HtmlButton) button;
		HtmlPage page2 = b.click();
		
		HtmlTable tablePesquisa = (HtmlTable) page2.getElementById("tabela_pesquisa");
		HtmlTextInput input = (HtmlTextInput) tablePesquisa.getCellAt(1, 1).getElementsByTagName("input").item(0);
		input.setAttribute("value", code);
		
		HtmlTable tableButtons = (HtmlTable)page2.getElementsByTagName("table").item(2);		
		HtmlImageInput pesquisar = (HtmlImageInput)tableButtons.getCellAt(0, 0).getElementsByTagName("input").item(0);		
		HtmlPage pageResult = (HtmlPage) pesquisar.click();
		
		getInfomedInfo(pageResult, med);
		
		webClient.closeAllWindows();
	}
	
	
	public void getInfarByCode(String code, Medicine med) throws Exception{
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");

		DomElement nameMed = page.getElementById("i_cnpem");		
		nameMed.setAttribute("value", code);

		DomElement button = page.getElementById("pesquisa");
		HtmlButtonInput b = (HtmlButtonInput) button;
		HtmlPage page2 = b.click();
		
		getInfarmedInfo(page2, med, webClient);
		
		webClient.closeAllWindows();
	}
//	------------------------------------------------------------------------------------------------------------------------
	
	
	
//	------------------------------------------------------------------------------------------------------------------------
	public File InfoByName(String name, String source, String select, String where) throws Exception{
		
		Medicine med = new Medicine();
		
		Query query;
		QueryExecution qe;
		ResultSet results;
		
		File file = new File("getInfo.txt");
		file.createNewFile();
		FileOutputStream fout = new FileOutputStream(file);
		
		
		if(source.equals("Infarmed")){
			
			String queryString =
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infarmed/>" +
		    		"SELECT ?x\n" +
		    		"WHERE{ ?x :NAME ?s ."
		    				+ "FILTER regex(str(?s)," + "\"" + name + "\"" + ")"
		    				+ "}";
			
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, getInfarModel());
			results =  qe.execSelect();
			
			if(!results.hasNext())
		    	getInfarByName(name, med);
			qe.close();
			
			queryString = 
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infarmed/>" +
					"PREFIX RCM: <http://infarmed/RCM/>" +
					"PREFIX FI: <http://infarmed/FI/>"  +
					"SELECT" + select + "\n" +
					"WHERE { ?x :NAME ?s ."
					     + where 
					     + "FILTER regex(str(?s)," + "\"" + name + "\"" + ")"
				    	 + "}";
			
			query = QueryFactory.create(queryString);
		    qe = QueryExecutionFactory.create(query, getInfarModel());
		    results =  qe.execSelect();
		    ResultSetFormatter.out(fout, results, query);
		    qe.close();
		    		
			fout.flush();
			fout.close();
		}
		    
		else{
			String queryString =
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infomed/>" +
		    		"SELECT ?x\n" +
		    		"WHERE{ ?x :NAME ?s ."
		    				+ "FILTER regex(str(?s)," + "\"" + name + "\"" + ")"
		    				+ "}";
			
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, getInfoModel());
		    results =  qe.execSelect();
		    
		    if(!results.hasNext())
		    	getInfoByName(name, med);
		    qe.close();

			queryString = 
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infomed/>" +
					"SELECT" + select + "\n" +
					"WHERE { ?x :NAME ?s ."
					     + where 
					     + "FILTER regex(str(?s)," + "\"" + name + "\"" + ")"
				    	 + "}";
		    
		    query = QueryFactory.create(queryString);
		    qe = QueryExecutionFactory.create(query, getInfoModel());
		    results =  qe.execSelect();
		    
		    ResultSetFormatter.out(fout, results, query);
		    qe.close();
		    		
			fout.flush();
			fout.close();
			
		}		
		
		return file;
	}
	
	public File InfoBySubstance(String substance, String source, String select, String where) throws Exception{
		
		Medicine med = new Medicine();
		
		Query query;
	    QueryExecution qe;
	    ResultSet results;
	    
	    File file = new File("getInfo.txt");
		file.createNewFile();		
		FileOutputStream fout = new FileOutputStream(file);
		
		if(source.equals("Infarmed")){
			
			String queryString =
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infarmed/>" +
		    		"SELECT ?x\n" +
		    		"WHERE{ ?x :SUBSTANCE ?s ."
		    				+ "FILTER regex(str(?s)," + "\"" + substance + "\"" + ")"
		    				+ "}";
			
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, getInfarModel());
			results =  qe.execSelect();
			
			if(!results.hasNext())
		    	getInfarBySubstance(substance, med);
			qe.close();
			
			queryString = 
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infarmed/>" +
					"PREFIX RCM: <http://infarmed/RCM/>" +
					"PREFIX FI: <http://infarmed/FI/>"  +
					"SELECT" + select + "\n" +
					"WHERE { ?x :SUBSTANCE ?s ." //+ "\"" + substance + "\" .\n" +
							+ where 
						    + "FILTER regex(str(?s)," + "\"" + substance + "\"" + ")"
					    	+ "}";
			
			query = QueryFactory.create(queryString);
		    qe = QueryExecutionFactory.create(query, getInfarModel());
		    results =  qe.execSelect();
		    ResultSetFormatter.out(fout, results, query);
		    qe.close();
		    		
			fout.flush();
			fout.close();
		}
		    
		else{
			getInfoModel().write(System.out, "TTL");
			
			String queryString =
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infomed/>" +
		    		"SELECT ?x\n" +
		    		"WHERE{ ?x :SUBSTANCE ?s ."
		    				+ "FILTER regex(str(?s)," + "\"" + substance + "\"" + ")"
		    				+ "}";
			
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, getInfoModel());
		    results =  qe.execSelect();
		    
		    if(!results.hasNext())
		    	getInfoBySubstance(substance, med);
		    qe.close();

			queryString = 
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infomed/>" +
					"SELECT" + select + "\n" +
					"WHERE { ?x :SUBSTANCE ?s ." //+ "\"" + substance + "\" .\n" +
							+ where 
						    + "FILTER regex(str(?s)," + "\"" + substance + "\"" + ")"
					    	+ "}";
		    
		    query = QueryFactory.create(queryString);
		    qe = QueryExecutionFactory.create(query, getInfoModel());
		    results =  qe.execSelect();
		    ResultSetFormatter.out(fout, results, query);
		    qe.close();
		    		
			fout.flush();
			fout.close();
			
		}
		
		return file;
	}

	public File InfoByCode(String code, String source, String select, String where) throws Exception{
		
		Medicine med = new Medicine();
		
		Query query;
	    QueryExecution qe;
	    ResultSet results;
	    
	    File file = new File("getInfo.txt");
		file.createNewFile();		
		FileOutputStream fout = new FileOutputStream(file);
		
		
		if(source.equals("Infarmed")){
			
			String queryString =
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infarmed/>" +
		    		"SELECT ?x\n" +
		    		"WHERE{ ?x :CODE " + "\"" + code + "\" .}";
			
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, getInfarModel());
			results =  qe.execSelect();
			
			if(!results.hasNext())
		    	getInfarByCode(code, med);
			qe.close();
			
			queryString = 
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infarmed/>" +
					"PREFIX RCM: <http://infarmed/RCM/>" +
					"PREFIX FI: <http://infarmed/FI/>"  +
					"SELECT" + select + "\n" +
					"WHERE { ?x :CODE " + "\"" + code + "\" .\n" +
					     where + "}";
			
			query = QueryFactory.create(queryString);
		    qe = QueryExecutionFactory.create(query, getInfarModel());
		    results =  qe.execSelect();
		    ResultSetFormatter.out(fout, results, query);
		    qe.close();
		    		
			fout.flush();
			fout.close();
		}
		    
		else{
			String queryString =
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infomed/>" +
		    		"SELECT ?x\n" +
		    		"WHERE{ ?x :CODE " + "\"" + code + "\" .}";
			
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, getInfoModel());
		    results =  qe.execSelect();
		    
		    if(!results.hasNext())
		    	getInfoByCode(code, med);
		    qe.close();

			queryString = 
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		    		"PREFIX : <http://infomed/>" +
					"SELECT" + select + "\n" +
					"WHERE { ?x :CODE " + "\"" + code + "\" .\n" +
					     where + "}";
		    
		    query = QueryFactory.create(queryString);
		    qe = QueryExecutionFactory.create(query, getInfoModel());
		    results =  qe.execSelect();
		    ResultSetFormatter.out(fout, results, query);
		    qe.close();
		    		
			fout.flush();
			fout.close();
			
		}
		
		return file;
	}


}

