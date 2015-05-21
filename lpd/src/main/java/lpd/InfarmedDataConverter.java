package lpd;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;

import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class InfarmedDataConverter {
	
	private Model infarModel;
	
	public InfarmedDataConverter(){
		this.infarModel = null;
	}
	
	public Model getModel(){
		return this.infarModel;
	}
	
	public void setModel(Model m){
		this.infarModel = m;
	}

	public Model getSchemaModel() throws Exception{
		
		Model model = ModelFactory.createDefaultModel();
		ArrayList<Resource> properties = new ArrayList<Resource>();
		ArrayList<Resource> fiProperties = new ArrayList<Resource>();
		ArrayList<Resource> rcmProperties = new ArrayList<Resource>();
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");
		HtmlTable table = (HtmlTable) page.getElementById("mainResult");
		
		Resource medicine = model.createResource("http://infarmed/Medicine");		
		medicine.addProperty(RDF.type, RDFS.Class);
		
		Resource fiR = model.createResource("http://infarmed/Fi");
		Resource rcmR = model.createResource("http://infarmed/Rcm");
				
		//columns: row=0
		properties.add(model.createResource("http://infarmed/" + table.getCellAt(0, 2).asText().replace(' ', '_')));
		properties.add(model.createResource("http://infarmed/" + table.getCellAt(0, 1).asText().replace(' ', '_')));
		properties.add(model.createResource("http://infarmed/" + table.getCellAt(0, 4).asText().replace(' ', '_')));
		properties.add(model.createResource("http://infarmed/" + table.getCellAt(0, 5).asText().replace(' ', '_')));
		properties.add(model.createResource("http://infarmed/" + table.getCellAt(0, 6).asText().replace(' ', '_')));
		properties.add(model.createResource("http://infarmed/" + table.getCellAt(0, 8).asText().replace(' ', '_')));
		properties.add(model.createResource("http://infarmed/" + table.getCellAt(0, 11).asText().replace(' ', '_')));
		properties.add(model.createResource("http://infarmed/FI"));
		properties.add(model.createResource("http://infarmed/RCM"));
		
		for(Resource p: properties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, medicine.getURI());
		}
				
		properties.get(properties.size()-1).addProperty(RDFS.range, fiR.getURI());
		properties.get(properties.size()-2).addProperty(RDFS.range, rcmR.getURI());		
		
		
		fiProperties.add(model.createResource("http://infarmed/FI/" + "Url"));
		fiProperties.add(model.createResource("http://infarmed/FI/" + "Definição"));
		fiProperties.add(model.createResource("http://infarmed/FI/" + "Não_usar"));
		fiProperties.add(model.createResource("http://infarmed/FI/" + "Efeitos_Secundários"));
		fiProperties.add(model.createResource("http://infarmed/FI/" + "Como_conservar"));
		
		for(Resource p: fiProperties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, fiR.getURI());
		}
		
		rcmProperties.add(model.createResource("http://infarmed/RCM/" + "Url"));
		rcmProperties.add(model.createResource("http://infarmed/RCM/" + "Composição"));
		rcmProperties.add(model.createResource("http://infarmed/RCM/" + "Indicações"));
		rcmProperties.add(model.createResource("http://infarmed/RCM/" + "Posologia"));
		rcmProperties.add(model.createResource("http://infarmed/RCM/" + "Contraindicações"));
		rcmProperties.add(model.createResource("http://infarmed/RCM/" + "Interações"));
		
		for(Resource p: rcmProperties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, rcmR.getURI());
		}
		
		webClient.closeAllWindows();
		setModel(model);
		return model;		
	}
	
	public Model infarmedModel(String substancy, String name, String type, String dosage,
			String numUnits, int prescriptionCode, float price, String generic,
			String rcm, ArrayList<String> rcmInfo, String fi, ArrayList<String> fiInfo){

		
		Model infar = getModel();
		if(infar == null)
			try {
				infar = getSchemaModel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		String infarmedURI = "http://infarmed/";
		
		infar.setNsPrefix("", infarmedURI);
		infar.setNsPrefix("RCM", "http://infarmed/RCM/");
		infar.setNsPrefix("FI", "http://infarmed/FI/");
		
		Resource r = infar.createResource(infarmedURI + name + "_" + prescriptionCode);
		r.addProperty(RDF.type, "http://infomed/Medicine");		
		
		r.addProperty(infar.getProperty("http://infarmed/Nome_do_Medicamento"), name);
		r.addProperty(infar.getProperty("http://infarmed/Substância_Activa"), substancy);
		r.addProperty(infar.getProperty("http://infarmed/Dosagem"), dosage);
		r.addProperty(infar.getProperty("http://infarmed/Tamanho_da_Embalagem"), numUnits);
		r.addProperty(infar.getProperty("http://infarmed/CNPEM"), Integer.toString(prescriptionCode));
		r.addProperty(infar.getProperty("http://infarmed/Preço_(PVP)"), Float.toString(price));
		r.addProperty(infar.getProperty("http://infarmed/Genérico"), generic);
		
		
		Resource rcmR = infar.createResource();		
		rcmR.addProperty(RDF.type, "http://infarmed/Rcm");
		
		rcmR.addProperty(infar.getProperty("http://infarmed/RCM/Url"), rcm);
		rcmR.addProperty(infar.getProperty("http://infarmed/RCM/Composição"), rcmInfo.get(0));
		rcmR.addProperty(infar.getProperty("http://infarmed/RCM/Indicações"), rcmInfo.get(1));
		rcmR.addProperty(infar.getProperty("http://infarmed/RCM/Posologia"), rcmInfo.get(2));
		rcmR.addProperty(infar.getProperty("http://infarmed/RCM/Contraindicações"), rcmInfo.get(3));
		rcmR.addProperty(infar.getProperty("http://infarmed/RCM/Interações"), rcmInfo.get(4));
		
		r.addProperty(infar.getProperty("http://infarmed/RCM"), rcmR);
		
		
		Resource fiR = infar.createResource();		
		fiR.addProperty(RDF.type, "http://infarmed/Fi");
		
		fiR.addProperty(infar.getProperty("http://infarmed/FI/Url"), fi);
		fiR.addProperty(infar.getProperty("http://infarmed/FI/Definição"), fiInfo.get(0));
		fiR.addProperty(infar.getProperty("http://infarmed/FI/Não_usar"), fiInfo.get(1));
		fiR.addProperty(infar.getProperty("http://infarmed/FI/Efeitos_Secundários"), fiInfo.get(8));
		fiR.addProperty(infar.getProperty("http://infarmed/FI/Como_conservar"), fiInfo.get(9));
		
		r.addProperty(infar.getProperty("http://infarmed/FI"), fiR);

		setModel(infar);
		return infar;
	}
	
	public void getInfarmedInfo(HtmlPage page, WebClient webClient) throws Exception{
		
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
		
		Model m = null;		
		
		String marketed = "Comercializado";
		ReadPDF reader = new ReadPDF();
		TopLevelWindow ww;
		String partialHref;
		int numMeds = 0;
		int row = 1;
		
		int numRows = table.getRowCount();
		int maxRows;		
		
		if(numRows > 6)
			maxRows = 6;
		else
			maxRows = numRows - 1;
			
		while(numMeds < maxRows){
			
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

				m = infarmedModel(substancyAct, name, type, dosage,
						numUnits, prescriptionCode, price, generic, rcm, rcmInfo, fi, fiInfo);
				
				setModel(m);
				
				name = type = dosage = numUnits = generic = rcm = fi = "";
				prescriptionCode = 0;
				price = 0;
				rcmInfo.clear();
				fiInfo.clear();
			}			
			
			row++;
		}
	}
	
	public void getInfarByName(String name) throws Exception{
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");

		DomElement nameMed = page.getElementById("i_MarcaCom");		
		nameMed.setAttribute("value", name);

		DomElement button = page.getElementById("pesquisa");
		HtmlButtonInput b = (HtmlButtonInput) button;
		HtmlPage page2 = b.click();
		
		getInfarmedInfo(page2, webClient);
		
		webClient.closeAllWindows();
	}
	
	public void getInfarBySubstance(String substance) throws Exception{
		
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
		
		getInfarmedInfo(page2, webClient);
		
		webClient.closeAllWindows();
	}
	
	public void getInfarByCode(String code) throws Exception{
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");

		DomElement nameMed = page.getElementById("i_cnpem");		
		nameMed.setAttribute("value", code);

		DomElement button = page.getElementById("pesquisa");
		HtmlButtonInput b = (HtmlButtonInput) button;
		HtmlPage page2 = b.click();
		
		getInfarmedInfo(page2, webClient);
		
		webClient.closeAllWindows();
	}
}
