package pt.ulisboa.tecnico.core;

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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class InfarmedDataConverter {
	
	private int drugID;
	
	public InfarmedDataConverter(){
		
	}
	
	public InfarmedDataConverter(Model dbModel){
		
		this.drugID = 0;
		
		try {
			setSchemaModel(dbModel);
			getInfarBySubstance(dbModel, "ácido acetilsalicílico");
			getInfarBySubstance(dbModel, "amoxicilina");
			getInfarBySubstance(dbModel, "Ibuprofeno");
			getInfarBySubstance(dbModel, "Paracetamol");
			getInfarBySubstance(dbModel, "Iodopovidona");
			getInfarBySubstance(dbModel, "Levotiroxina sódica");
			getInfarBySubstance(dbModel, "Perindopril");
			getInfarBySubstance(dbModel, "Atorvastatina");
			getInfarBySubstance(dbModel, "Fluvastatina");
			getInfarBySubstance(dbModel, "Pravastatina");
			getInfarBySubstance(dbModel, "midodrina");
			getInfarBySubstance(dbModel, "captopril");
			getInfarBySubstance(dbModel, "Furosemida");
			getInfarBySubstance(dbModel, "Espironolactona");
			getInfarBySubstance(dbModel, "Digoxina");
			getInfarBySubstance(dbModel, "Carbonato de cálcio");
			getInfarBySubstance(dbModel, "cloreto de magnésio");			
			getInfarBySubstance(dbModel, "metformina");
			getInfarBySubstance(dbModel, "Nitrofurantoína");
			getInfarBySubstance(dbModel, "Glibenclamida");
			getInfarBySubstance(dbModel, "Glipizida");
			getInfarBySubstance(dbModel, "Glimepirida");
			getInfarBySubstance(dbModel, "Pioglitazona");
			getInfarBySubstance(dbModel, "Acarbose");
			
//			getInfarBySubstance(dbModel, "Nateglinida");
//			getInfarBySubstance(dbModel, "Sitagliptina");
			
			getInfarBySubstance(dbModel, "ciclosporina");
			getInfarBySubstance(dbModel, "metotrexato");
			getInfarBySubstance(dbModel, "Prednisona");
			getInfarBySubstance(dbModel, "Metilprednisolona");
			getInfarBySubstance(dbModel, "Leflunomida");
			getInfarBySubstance(dbModel, "Azatioprina");
			getInfarBySubstance(dbModel, "Cloreto de sódio");
			getInfarBySubstance(dbModel, "ETINILESTRADIOL GESTODENO");
			getInfarBySubstance(dbModel, "SACCHAROMYCES BOULARDII");
			getInfarBySubstance(dbModel, "Bromexina");
			getInfarBySubstance(dbModel, "dextrometorfano");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setSchemaModel(Model model) throws Exception{
		
		//Model model = ModelFactory.createDefaultModel();
		ArrayList<Resource> properties = new ArrayList<Resource>();
		ArrayList<Resource> fiProperties = new ArrayList<Resource>();
		ArrayList<Resource> rcmProperties = new ArrayList<Resource>();
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");
		HtmlTable table = (HtmlTable) page.getElementById("mainResult");
		
		String baseURI = "http://www.infarmed.pt/";
		
		Resource medicine = model.createResource(baseURI + "Medicine");		
		medicine.addProperty(RDF.type, RDFS.Class);
		
		Resource fiR = model.createResource(baseURI + "FiNode");
		Resource rcmR = model.createResource(baseURI + "RcmNode");
		
		//Infarmed properties
		properties.add(model.createResource(baseURI + table.getCellAt(0, 2).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + table.getCellAt(0, 1).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + table.getCellAt(0, 3).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + table.getCellAt(0, 4).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + table.getCellAt(0, 5).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + table.getCellAt(0, 6).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + table.getCellAt(0, 8).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + table.getCellAt(0, 11).asText().replace(' ', '_')));
		properties.add(model.createResource(baseURI + "FI"));
		properties.add(model.createResource(baseURI + "RCM"));
		
		for(Resource p: properties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, medicine.getURI());
		}
				
		properties.get(properties.size()-1).addProperty(RDFS.range, fiR.getURI());
		properties.get(properties.size()-2).addProperty(RDFS.range, rcmR.getURI());		
		
		
		fiProperties.add(model.createResource(baseURI + "FI/" + "Url"));
		fiProperties.add(model.createResource(baseURI + "FI/" + "Definição"));
		fiProperties.add(model.createResource(baseURI + "FI/" + "Não_usar"));
		fiProperties.add(model.createResource(baseURI + "FI/" + "Efeitos_Secundários"));
		fiProperties.add(model.createResource(baseURI + "FI/" + "Como_conservar"));
		
		for(Resource p: fiProperties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, fiR.getURI());
		}
		
		rcmProperties.add(model.createResource(baseURI + "RCM/" + "Url"));
		rcmProperties.add(model.createResource(baseURI + "RCM/" + "Composição"));
		rcmProperties.add(model.createResource(baseURI + "RCM/" + "Indicações"));
		rcmProperties.add(model.createResource(baseURI + "RCM/" + "Posologia"));
		rcmProperties.add(model.createResource(baseURI + "RCM/" + "Contraindicações"));
		rcmProperties.add(model.createResource(baseURI + "RCM/" + "Interações"));
		
		for(Resource p: rcmProperties){
			p.addProperty(RDF.type, RDF.Property);
			p.addProperty(RDFS.domain, rcmR.getURI());
		}
		
		webClient.closeAllWindows();
	}
	
	public void infarmedModel(Model infar, String substancy, String name, String type, String dosage,
			String numUnits, int prescriptionCode, float price, String generic,
			String rcm, ArrayList<String> rcmInfo, String fi, ArrayList<String> fiInfo){

		this.drugID++;
		String baseURI = "http://www.infarmed.pt/";
		
		infar.setNsPrefix("", baseURI);
		infar.setNsPrefix("RCM", baseURI + "RCM/");
		infar.setNsPrefix("FI", baseURI + "FI/");
		
		Resource r = infar.createResource(baseURI + name + "_" + drugID);
		r.addProperty(RDF.type, baseURI + "Medicine");
		
		r.addProperty(infar.getProperty(baseURI + "Nome_do_Medicamento"), name);
		r.addProperty(infar.getProperty(baseURI + "Substância_Activa"), substancy);
		r.addProperty(infar.getProperty(baseURI + "Forma_Farmacêutica"), type);
		r.addProperty(infar.getProperty(baseURI + "Dosagem"), dosage);
		r.addProperty(infar.getProperty(baseURI + "Tamanho_da_Embalagem"), numUnits);
		r.addProperty(infar.getProperty(baseURI + "CNPEM"), Integer.toString(prescriptionCode));
		r.addProperty(infar.getProperty(baseURI + "Preço_(PVP)"), Float.toString(price));
		r.addProperty(infar.getProperty(baseURI + "Genérico"), generic);
		
		
		Resource rcmR = infar.createResource();		
		rcmR.addProperty(RDF.type, baseURI + "RcmNode");
		
		rcmR.addProperty(infar.getProperty(baseURI + "RCM/Url"), rcm);
		rcmR.addProperty(infar.getProperty(baseURI + "RCM/Composição"), rcmInfo.get(0));
		rcmR.addProperty(infar.getProperty(baseURI + "RCM/Indicações"), rcmInfo.get(1));
		rcmR.addProperty(infar.getProperty(baseURI + "RCM/Posologia"), rcmInfo.get(2));
		rcmR.addProperty(infar.getProperty(baseURI + "RCM/Contraindicações"), rcmInfo.get(3));
		rcmR.addProperty(infar.getProperty(baseURI + "RCM/Interações"), rcmInfo.get(4));
		
		r.addProperty(infar.getProperty(baseURI + "RCM"), rcmR);
		
		
		Resource fiR = infar.createResource();		
		fiR.addProperty(RDF.type, baseURI + "FiNode");
		
		fiR.addProperty(infar.getProperty(baseURI + "FI/Url"), fi);
		fiR.addProperty(infar.getProperty(baseURI + "FI/Definição"), fiInfo.get(0));
		fiR.addProperty(infar.getProperty(baseURI + "FI/Não_usar"), fiInfo.get(1));
		fiR.addProperty(infar.getProperty(baseURI + "FI/Efeitos_Secundários"), fiInfo.get(8));
		fiR.addProperty(infar.getProperty(baseURI + "FI/Como_conservar"), fiInfo.get(9));
		
		r.addProperty(infar.getProperty(baseURI + "FI"), fiR);
		
	}
	
	public void getInfarmedInfo(HtmlPage page, WebClient webClient, Model infar) throws Exception{
		
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
		String rcm = "";
		String fi = "";
		ArrayList<String> rcmInfo = new ArrayList<String>();
		ArrayList<String> fiInfo = new ArrayList<String>();
		
		int i;
		for(i=0; i<5; i++){
			rcmInfo.add("");
		}
		for(i=0; i <10; i++){
			fiInfo.add("");
		}
		
		HtmlAnchor ha;
		TopLevelWindow ww;
		String partialHref;
		ReadPDF reader = new ReadPDF();
		String marketed = "Comercializado";
		
		i = 0;		
		int row = 1;
		int numRows = table.getRowCount() - 1;
		
			
		while(i < numRows){
			
			if(marketed.equals(table.getCellAt(row, 12).asText())){
				
				name = table.getCellAt(row, 2).asText();
				if(name.equals("Juliperla") || name.equals("Sofiperla")){
					row++;
					i++;
					continue;
				}
				
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
				InputStream is;
				
				if(anchors.hasNext()){					
					ha = (HtmlAnchor)anchors.next();
					partialHref = ha.getAttribute("href");
					
					if(partialHref.contains("tipo_doc=rcm")){						
						rcm = partialHref.replace("../../", "http://www.infarmed.pt/");					
						ha.setAttribute("href", rcm);
						
						is = ha.openLinkInNewWindow().getWebResponse().getContentAsStream();
						rcmInfo = reader.readRCM(is, rcmInfo);
						
						ww = (TopLevelWindow) webClient.getCurrentWindow().getParentWindow();
						ww.close();
					}
				}
				
//		        --------------------------------------------------------------------------------
				
				if(anchors.hasNext()){					
					ha = (HtmlAnchor)anchors.next();
					partialHref = ha.getAttribute("href");
					
					if(partialHref.contains("tipo_doc=fi")){						
						fi = partialHref.replace("../../", "http://www.infarmed.pt/");
						ha.setAttribute("href", fi);
						
						is = ha.openLinkInNewWindow().getWebResponse().getContentAsStream();
						fiInfo = reader.readFI(is, fiInfo);
						
						ww = (TopLevelWindow) webClient.getCurrentWindow().getParentWindow();
						ww.close();
					}
				}
//				--------------------------------------------------------------------------------

				infarmedModel(infar, substancyAct, name, type, dosage, numUnits, prescriptionCode,
								price, generic, rcm, rcmInfo, fi, fiInfo);
				
				name = type = dosage = numUnits = generic = rcm = fi = "";
				prescriptionCode = 0;
				price = 0;
				//rcmInfo.clear();
				//fiInfo.clear();
			}			
			
			i++;
			row++;
		}
	}
	
	public void getInfarByName(Model m, String name) throws Exception{
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");

		DomElement nameMed = page.getElementById("i_MarcaCom");		
		nameMed.setAttribute("value", name);

		DomElement button = page.getElementById("pesquisa");
		HtmlButtonInput b = (HtmlButtonInput) button;
		HtmlPage page2 = b.click();
		
		getInfarmedInfo(page2, webClient, m);
		
		webClient.closeAllWindows();
	}
	
	public void getInfarBySubstance(Model m, String substance) throws Exception{
		
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
		
		getInfarmedInfo(page2, webClient, m);
		
		webClient.closeAllWindows();
	}
	
	public void getInfarByCode(Model m, String code) throws Exception{
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://www.infarmed.pt/genericos/pesquisamg/pesquisaMG.php");

		DomElement nameMed = page.getElementById("i_cnpem");		
		nameMed.setAttribute("value", code);

		DomElement button = page.getElementById("pesquisa");
		HtmlButtonInput b = (HtmlButtonInput) button;
		HtmlPage page2 = b.click();
		
		getInfarmedInfo(page2, webClient, m);
		
		webClient.closeAllWindows();
	}
}
