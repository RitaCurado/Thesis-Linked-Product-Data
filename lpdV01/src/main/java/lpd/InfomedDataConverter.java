package lpd;

import java.io.IOException;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class InfomedDataConverter {
	
	private Model infoModel;
	
	public InfomedDataConverter(){
		this.infoModel = null;
	}
	
	public Model getModel(){
		return this.infoModel;
	}
	
	public void setModel(Model m){
		this.infoModel = m;
	}
	
	public Model getSchemaModel() throws IOException{
		
		Model model = ModelFactory.createDefaultModel();
		ArrayList<Resource> properties = new ArrayList<Resource>();
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/infomed/inicio.php");
		
		DomElement button = page.getElementById("button_");
		HtmlButton b = (HtmlButton) button;
		HtmlPage page2 = b.click();
		
		HtmlTable tablePesquisa = (HtmlTable) page2.getElementById("tabela_pesquisa");
		HtmlTextInput input = (HtmlTextInput) tablePesquisa.getCellAt(2, 1).getElementsByTagName("input").item(0);
		input.setAttribute("value", "ASP");
		
		HtmlTable tableButtons = (HtmlTable)page2.getElementsByTagName("table").item(2);		
		HtmlImageInput pesquisar = (HtmlImageInput)tableButtons.getCellAt(0, 0).getElementsByTagName("input").item(0);		
		HtmlPage pageResult = (HtmlPage) pesquisar.click();
		HtmlTable table = (HtmlTable)pageResult.getElementsByTagName("table").item(2);
		
		Resource medicine = model.createResource("http://infomed/Medicine");		
		medicine.addProperty(RDF.type, RDFS.Class);
		
		String column0 = table.getCellAt(0, 0).getElementsByTagName("b").item(0).getTextContent();
		String[] splited = column0.split("/");
		
		properties.add(model.createResource("http://infomed/" + splited[1].substring(1).replace(" ", "_")));
		properties.add(model.createResource("http://infomed/" + table.getCellAt(0,1).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		properties.add(model.createResource("http://infomed/" + table.getCellAt(0,3).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		properties.add(model.createResource("http://infomed/" + table.getCellAt(0,4).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		properties.add(model.createResource("http://infomed/" + table.getCellAt(0,7).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		
		for(Resource p: properties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, medicine.getURI());
		}
		
		webClient.closeAllWindows();
		setModel(model);
		return model;
	}
	
	public void getInfomedInfo(HtmlPage pageResult) {		
		
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
				
				m = infomedModel(actSubs, name, type, dosage, holder, generic);
				
				setModel(m);
			}
			
			name = type = dosage = holder = generic = "";			
			line++;
		}
	}

	public Model infomedModel(String substancy, String name, String type, String dosage, String holder, String generic){
		
		Model info = getModel();
		if(info == null)
			try {
				info = getSchemaModel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		String infomedURI = "http://infomed/";
		
		Resource r = info.createResource(infomedURI + name + "_" + dosage);
		r.addProperty(RDF.type, "http://infomed/Medicine");
		
		r.addProperty(info.getProperty("http://infomed/Nome_do_Medicamento"), name);
		r.addProperty(info.getProperty("http://infomed/Nome_Genérico"), substancy);
		r.addProperty(info.getProperty("http://infomed/Dosagem"), dosage);
		r.addProperty(info.getProperty("http://infomed/Titular"), holder);
		r.addProperty(info.getProperty("http://infomed/Genérico"), generic);
		
		setModel(info);		
		return info;
	}
	
	public void getInfoByName(String medicine) throws Exception{
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
		
		getInfomedInfo(pageResult);
		
		webClient.closeAllWindows();
	}
	
	public void getInfoBySubstance(String substance) throws Exception{
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
		
		getInfomedInfo(pageResult);
		
		webClient.closeAllWindows();
	}

}
