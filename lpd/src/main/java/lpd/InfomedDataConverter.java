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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class InfomedDataConverter {
	
	private int drugID;
	
	public InfomedDataConverter(Model dbModel){
		this.drugID = 0;
		
		try{
			setSchemaModel(dbModel);
			getInfoBySubstance(dbModel, "ácido acetilsalicílico");
			getInfoBySubstance(dbModel, "amoxicilina");
			getInfoBySubstance(dbModel, "Ibuprofeno");
			getInfoBySubstance(dbModel, "Paracetamol");
			getInfoBySubstance(dbModel, "Iodopovidona");
			getInfoBySubstance(dbModel, "Levotiroxina sódica");
			getInfoBySubstance(dbModel, "Perindopril");
			getInfoBySubstance(dbModel, "Atorvastatina");
			getInfoBySubstance(dbModel, "Fluvastatina");
			getInfoBySubstance(dbModel, "Pravastatina");
			getInfoBySubstance(dbModel, "midodrina");
			getInfoBySubstance(dbModel, "captopril");
			getInfoBySubstance(dbModel, "Furosemida");
			getInfoBySubstance(dbModel, "Espironolactona");
			getInfoBySubstance(dbModel, "Digoxina");
			getInfoBySubstance(dbModel, "Metoprolol");
			getInfoBySubstance(dbModel, "Carbonato de cálcio");
			getInfoBySubstance(dbModel, "cloreto de magnésio");
			getInfoBySubstance(dbModel, "metformina");
			getInfoBySubstance(dbModel, "Nitrofurantoína");
			getInfoBySubstance(dbModel, "Glibenclamida");
			getInfoBySubstance(dbModel, "Glipizida");
			getInfoBySubstance(dbModel, "Glimepirida");
			getInfoBySubstance(dbModel, "Nateglinida");//
			getInfoBySubstance(dbModel, "Sitagliptina");//
			getInfoBySubstance(dbModel, "Pioglitazona");
			getInfoBySubstance(dbModel, "Acarbose");
			getInfoBySubstance(dbModel, "ciclosporina");
			getInfoBySubstance(dbModel, "metotrexato");
			getInfoBySubstance(dbModel, "Prednisona");
			getInfoBySubstance(dbModel, "Metilprednisolona");
			getInfoBySubstance(dbModel, "Leflunomida");
			getInfoBySubstance(dbModel, "Azatioprina");
			getInfoBySubstance(dbModel, "Cloreto de sódio");
			getInfoBySubstance(dbModel, "ETINILESTRADIOL GESTODENO");
			getInfoBySubstance(dbModel, "SACCHAROMYCES BOULARDII");
			getInfoBySubstance(dbModel, "Bromexina");
			getInfoBySubstance(dbModel, "dextrometorfano");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public Model setSchemaModel(Model model) throws IOException{
		
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
		
		String baseURI = "http://www.infomed.pt/";
		
		Resource medicine = model.createResource(baseURI + "Medicine");		
		medicine.addProperty(RDF.type, RDFS.Class);
		
		String column0 = table.getCellAt(0, 0).getElementsByTagName("b").item(0).getTextContent();
		String[] splited = column0.split("/");
		
		properties.add(model.createResource(baseURI + splited[1].substring(1).replace(" ", "_")));
		properties.add(model.createResource(baseURI + table.getCellAt(0,1).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		properties.add(model.createResource(baseURI + table.getCellAt(0,3).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		properties.add(model.createResource(baseURI + table.getCellAt(0,4).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		properties.add(model.createResource(baseURI + table.getCellAt(0,7).getElementsByTagName("b").item(0).getTextContent().replace(" ", "_")));
		
		for(Resource p: properties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, medicine.getURI());
		}
		
		webClient.closeAllWindows();
		return model;
	}
	
	public void getInfomedInfo(Model m, HtmlPage pageResult) {		
		
		HtmlTable table = (HtmlTable)pageResult.getElementsByTagName("table").item(2);
		
		int length;
		String permited = "Autorizado";
		
		String actSubs="";
		String name;
		String dosage;
		String type;
		String generic;
		String holder;
		
		int i = 0;		
		int line = 1;
		int numRows = table.getRowCount() - 1;
//		int numColumns = table.getRow(1).getCells().size();
//		
//		System.out.println("num rows: " + numRows);
//		System.out.println("num cols: " + numColumns);
//		System.out.println(table.getCellAt(line, 5).asText());
			
		while(i < numRows){
			String coluna5 = table.getCellAt(line, 5).asText();
			if(permited.equals(coluna5)){				
				
				String s = table.getCellAt(line, 0).asText().toLowerCase();
				length = s.length();
				actSubs = s.substring(0, length-1);
				
				name = table.getCellAt(line, 1).asText();
				type = table.getCellAt(line, 2).asText();
				dosage = table.getCellAt(line, 3).asText();
				holder = table.getCellAt(line, 4).asText();
				generic = table.getCellAt(line, 7).asText();
				
				infomedModel(m, actSubs, name, type, dosage, holder, generic);
			}
			
			name = type = dosage = holder = generic = "";	
			
			i++;
			line++;
		}
	}

	public void infomedModel(Model info, String substancy, String name, String type, String dosage, String holder, String generic){
		
		String baseURI = "http://www.infomed.pt/";
		this.drugID++;
		
		Resource r = info.createResource(baseURI + name + "_" + drugID);
		r.addProperty(RDF.type, baseURI + "Medicine");
		
		r.addProperty(info.getProperty(baseURI + "Nome_do_Medicamento"), name);
		r.addProperty(info.getProperty(baseURI + "Nome_Genérico"), substancy);
		r.addProperty(info.getProperty(baseURI + "Dosagem"), dosage);
		r.addProperty(info.getProperty(baseURI + "Titular"), holder);
		r.addProperty(info.getProperty(baseURI + "Genérico"), generic);
		
	}
	
	public void getInfoByName(Model m, String medicine) throws Exception{
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
		
		getInfomedInfo(m, pageResult);
		
		webClient.closeAllWindows();
	}
	
	public void getInfoBySubstance(Model m, String substance) throws Exception{
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
		
		getInfomedInfo(m, pageResult);
		
		webClient.closeAllWindows();
	}

}
