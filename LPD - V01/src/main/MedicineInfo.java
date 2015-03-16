package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
	
	private ArrayList<Model> infarModel;
	private ArrayList<Model> infoModel;
	private Model completeModel;
	
	public MedicineInfo(){
		
		infarModel = new ArrayList<Model>();
		infoModel = new ArrayList<Model>();
		
		completeModel = ModelFactory.createDefaultModel();
	}
	
	public Model getCompleteModel(){
		return this.completeModel;
	}
	
	public void setCompleteModel(Model m){
		this.completeModel = m;
	}
	
	public ArrayList<Model> getInfarModel(){
		return this.infarModel;
	}
	
	public void clearInfarModel(){
		this.infarModel.clear();
	}
	
	public ArrayList<Model> getInfoModel(){
		return this.infoModel;
	}
	
	public void clearInfoModel(){
		this.infoModel.clear();
	}


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

				m = med.infarmedModel(substancyAct, name, type, dosage,
						numUnits, prescriptionCode, price, generic, rcm, rcmInfo, fi, fiInfo);
				
				//setInfarModel(m);
				getInfarModel().add(m);
				
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
		
		Model m;
		
		
		while(numMeds < 4){
			if(permited.equals(table.getCellAt(line, 5).asText())){
				numMeds++;
				
				String s = table.getCellAt(line, 0).asText().toLowerCase();
				length = s.length();
				actSubs = s.substring(0, length-1);
				
				name = table.getCellAt(line, 1).asText();
				type = table.getCellAt(line, 2).asText();
				dosage = table.getCellAt(line, 3).asText();
				holder = table.getCellAt(line, 4).asText();
				generic = table.getCellAt(line, 7).asText();
				
				m = med.infomedModel(actSubs, name, type, dosage, holder, generic);
				
				//setInfoModel(m);
				getInfoModel().add(m);
			}
			
			name = type = dosage = holder = generic = "";			
			line++;
		}
	}
	
	public void linkModels(Medicine med){
		
		String name_infar, name_info;
    	String type_infar, type_info;
    	String dose_infar, dose_info;
    	
    	
    	Model infar, info;
    	ArrayList<Model> rmInfar = new ArrayList<Model>();
    	ArrayList<Model> rmInfo = new ArrayList<Model>();
    	
    	for(int i=0; i< infarModel.size(); i++){
    		infar = infarModel.get(i);
    		
	    	name_infar = infar.listObjectsOfProperty(infar.getProperty("http://infarmed/NAME")).next().toString();
	    	type_infar = infar.listObjectsOfProperty(infar.getProperty("http://infarmed/TYPE")).next().toString();
	    	dose_infar = infar.listObjectsOfProperty(infar.getProperty("http://infarmed/DOSAGE")).next().toString();
	    	
	    	for(int j=0; j<infoModel.size(); j++){
	    		info = infoModel.get(j);
	    		
	    		name_info = info.listObjectsOfProperty(info.getProperty("http://infomed/NAME")).next().toString();
	    		type_info = info.listObjectsOfProperty(info.getProperty("http://infomed/TYPE")).next().toString();
	    		dose_info = info.listObjectsOfProperty(info.getProperty("http://infomed/DOSAGE")).next().toString();
	    		
	    		if(name_infar.equals(name_info) && type_infar.equals(type_info) && dose_infar.equals(dose_info)){
	    				    			
	    			Model m = med.completeModel(getCompleteModel(), infar, info);	    					
	    			
	    			setCompleteModel(m);
	    			rmInfo.add(info);
	    			rmInfar.add(infar);
	    		}
	    	}
    	}
    	
    	for(Model m: rmInfar){
    		infarModel.remove(m);
    	}
    	
    	for(Model m: rmInfo){
    		infoModel.remove(m);
    	}
    	
    	for(Model m: infarModel){
    		m = med.completeModel(getCompleteModel(), m, null);
    		setCompleteModel(m);
    	}
    	
    	for(Model m: infoModel){
    		m = med.completeModel(getCompleteModel(), null, m);
    		setCompleteModel(m);
    	}
		
		clearInfarModel();
		clearInfoModel();
    	    	
    		
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
	public File InfoByName(String name) throws Exception{
		
		Medicine med = new Medicine();
		
		String queryString =
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://medicine/>" +
				"PREFIX RCM: <http://medicine/RCM/>" +
				"PREFIX FI: <http://medicine/FI/>"  +
	    		"SELECT ?x\n" +
	    		"WHERE{ ?x :NAME " + "\"" + name + "\" .}";
	    
	    Query query = QueryFactory.create(queryString);
	    QueryExecution qe = QueryExecutionFactory.create(query, getCompleteModel());
	    ResultSet results =  qe.execSelect();
	    
	    
	    if(!results.hasNext()){
	    	getInfoByName(name, med);
	    	getInfarByName(name, med);
	    	linkModels(med);
	    }
	    
	    qe.close();
		
		File file = new File("getInfo.txt");
		file.createNewFile();		
		FileOutputStream fout = new FileOutputStream(file);
		
		queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://medicine/>" +
				"PREFIX RCM: <http://medicine/RCM/>" +
				"PREFIX FI: <http://medicine/FI/>"  +
				"SELECT ?Substância ?Tipo ?Dose ?Titular ?Genérico ?Unidades ?Código ?Preço\n" +
				"WHERE { ?x :NAME " + "\"" + name + "\" .\n" +
				     "?x :SUBSTANCE ?Substância." +
				     "?x :TYPE ?Tipo." +
				     "?x :DOSAGE ?Dose." +
				     "?x :HOLDER ?Titular." +
				     "?x :GENERIC ?Genérico." +
				     "?x :UNITS ?Unidades." +
				     "?x :CODE ?Código." +
				     "?x :PRICE ?Preço.}";
	    
	    query = QueryFactory.create(queryString);
	    qe = QueryExecutionFactory.create(query, getCompleteModel());
	    results =  qe.execSelect();
	    ResultSetFormatter.out(fout, results, query);
	    qe.close();
	    		
		fout.flush();
		fout.close();
		
		
		return file;
	}
	
	public File InfoBySubstance(String substance) throws Exception{
		
		Medicine med = new Medicine();
		
		String queryString =
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://medicine/>" +
				"PREFIX RCM: <http://medicine/RCM/>" +
				"PREFIX FI: <http://medicine/FI/>"  +
	    		"SELECT ?x\n" +
	    		"WHERE{ ?x :SUBSTANCE " + "\"" + substance + "\" .}";
	    
	    Query query = QueryFactory.create(queryString);
	    QueryExecution qe = QueryExecutionFactory.create(query, getCompleteModel());
	    ResultSet results =  qe.execSelect();
	    
	    
	    if(!results.hasNext()){
	    	getInfoBySubstance(substance, med);
	    	getInfarBySubstance(substance, med);
	    	linkModels(med);
	    }
	    
	    qe.close();

		//getCompleteModel().write(System.out, "TTL");
		
		File file = new File("getInfo.txt");
		file.createNewFile();		
		FileOutputStream fout = new FileOutputStream(file);
		
		queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://medicine/>" +
				"PREFIX RCM: <http://medicine/RCM/>" +
				"PREFIX FI: <http://medicine/FI/>"  +
				"SELECT ?Nome \n" +
				"WHERE { ?x :SUBSTANCE " + "\"" + substance + "\" .\n" +
				     "?x :NAME ?Nome.}";
	    
	    query = QueryFactory.create(queryString);
	    qe = QueryExecutionFactory.create(query, getCompleteModel());
	    results =  qe.execSelect();
	    ResultSetFormatter.out(fout, results, query);
	    qe.close();
	    		
		fout.flush();
		fout.close();
		
		
		return file;
	}

	public File InfoByCode(String code) throws Exception{
		
		Medicine med = new Medicine();
		
		String queryString =
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://medicine/>" +
				"PREFIX RCM: <http://medicine/RCM/>" +
				"PREFIX FI: <http://medicine/FI/>"  +
	    		"SELECT ?x\n" +
	    		"WHERE{ ?x :CODE " + "\"" + code + "\" .}";
	    
	    Query query = QueryFactory.create(queryString);
	    QueryExecution qe = QueryExecutionFactory.create(query, getCompleteModel());
	    ResultSet results =  qe.execSelect();
	    
	    
	    if(!results.hasNext()){
	    	getInfoByCode(code, med);
	    	getInfarByCode(code, med);
	    	linkModels(med);
	    }
	    
	    qe.close();
	
		//getCompleteModel().write(System.out, "TTL");
		
		File file = new File("getInfo.txt");
		file.createNewFile();		
		FileOutputStream fout = new FileOutputStream(file);
		
		queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
	    		"PREFIX : <http://medicine/>" +
				"PREFIX RCM: <http://medicine/RCM/>" +
				"PREFIX FI: <http://medicine/FI/>"  +
				"SELECT ?Nome \n" +
				"WHERE { ?x :CODE " + "\"" + code + "\" .\n" +
				     "?x :NAME ?Nome.}";
	    
	    query = QueryFactory.create(queryString);
	    qe = QueryExecutionFactory.create(query, getCompleteModel());
	    results =  qe.execSelect();
	    ResultSetFormatter.out(fout, results, query);
	    qe.close();
	    		
		fout.flush();
		fout.close();
		
		
		return file;
	}


}
