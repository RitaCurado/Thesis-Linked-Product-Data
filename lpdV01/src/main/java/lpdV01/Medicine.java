package lpdV01;

import java.util.ArrayList;
import com.hp.hpl.jena.rdf.model.*;


public class Medicine {
	
	
	public Model infomedModel(Model info, String substancy, String name, String type, String dosage, String holder, String generic){
		
		//Model info = ModelFactory.createDefaultModel();
		
		String infomedURI = "http://infomed/";
		info.setNsPrefix("", infomedURI);
		
		Resource r = info.createResource(infomedURI + name + "_" + dosage);
		
		Property NAME = info.createProperty("http://infomed/NAME");
		Property SUBST = info.createProperty("http://infomed/SUBSTANCE");
		Property TYPE = info.createProperty("http://infomed/TYPE");
		Property DOSE = info.createProperty("http://infomed/DOSAGE");
		Property HOLDER = info.createProperty("http://infomed/HOLDER");
		Property GENERIC = info.createProperty("http://infomed/GENERIC");
		
		r.addProperty(NAME, name);
		r.addProperty(SUBST, substancy);
		r.addProperty(TYPE, type);
		r.addProperty(DOSE, dosage);
		r.addProperty(HOLDER, holder);
		r.addProperty(GENERIC, generic);
		
		return info;
	}
		
	public Model infarmedModel(Model infar, String substancy, String name, String type, String dosage,
								String numUnits, int prescriptionCode, float price, String generic,
								String rcm, ArrayList<String> rcmInfo, String fi, ArrayList<String> fiInfo){

		String infarmedURI = "http://infarmed/";
		
		//Model infar = ModelFactory.createDefaultModel();
		
		infar.setNsPrefix("", infarmedURI);
		infar.setNsPrefix("RCM", "http://infarmed/RCM/");
		infar.setNsPrefix("FI", "http://infarmed/FI/");
		
		// create the resource
		Resource m = infar.createResource(infarmedURI + "/" + name + "_" + prescriptionCode);
		
		Property NAME = infar.createProperty("http://infarmed/NAME");
		Property SUBST = infar.createProperty("http://infarmed/SUBSTANCE");
		Property TYPE = infar.createProperty("http://infarmed/TYPE");
		Property DOSE = infar.createProperty("http://infarmed/DOSAGE");
		Property UNITS = infar.createProperty("http://infarmed/UNITS");
		Property CODE = infar.createProperty("http://infarmed/CODE");
		Property PRICE = infar.createProperty("http://infarmed/PRICE");
		Property GEN = infar.createProperty("http://infarmed/GENERIC");
		Property RCM = infar.createProperty("http://infarmed/RCM");
		Property FI = infar.createProperty("http://infarmed/FI");
		
		Property UrlRcm = infar.createProperty("http://infarmed/RCM/Url");
		Property Composition = infar.createProperty("http://infarmed/RCM/Composition");
		Property Indications = infar.createProperty("http://infarmed/RCM/Indications");
		Property Posology = infar.createProperty("http://infarmed/RCM/Posology");
		Property Contraindications = infar.createProperty("http://infarmed/RCM/Contraindications");
		Property Interactions = infar.createProperty("http://infarmed/RCM/Interactions");
		
		Property UrlFi = infar.createProperty("http://infarmed/FI/Url");
		Property Definition = infar.createProperty("http://infarmed/FI/Definition");
		Property DoNotUse = infar.createProperty("http://infarmed/FI/DoNotUse");
		Property MedInteractions = infar.createProperty("http://infarmed/FI/Interactions");
		
//		Property OnPregnancy = infar.createProperty("http://infarmed/FI/OnPregnancy");
//		Property Driving = infar.createProperty("http://infarmed/FI/Driving");
//		Property HowToTake = infar.createProperty("http://infarmed/FI/HowToTake");
//		Property Overdosage = infar.createProperty("http://infarmed/FI/Overdosage");
//		Property IfForgotten = infar.createProperty("http://infarmed/FI/IfForgotten");
		
		Property SideEffects = infar.createProperty("http://infarmed/FI/SideEffects");
		Property HowToConserve = infar.createProperty("http://infarmed/FI/HowToConserve");
		

		// add the property
		m.addProperty(NAME, name);
		m.addProperty(SUBST, substancy);
		m.addProperty(TYPE, type);
		m.addProperty(DOSE, dosage);
		m.addProperty(UNITS, numUnits);
		m.addProperty(CODE, Integer.toString(prescriptionCode));
		m.addProperty(PRICE, Float.toString(price));
		m.addProperty(GEN, generic);
		m.addProperty(RCM, 
				infar.createResource().addProperty(UrlRcm, rcm)
										.addProperty(Composition, rcmInfo.get(0))
										.addProperty(Indications, rcmInfo.get(1))
										.addProperty(Posology, rcmInfo.get(2))
										.addProperty(Contraindications, rcmInfo.get(3))
										.addProperty(Interactions, rcmInfo.get(4)));
		m.addProperty(FI,
				infar.createResource().addProperty(UrlFi, fi)
										.addProperty(Definition, fiInfo.get(0))
										.addProperty(DoNotUse, fiInfo.get(1))
										.addProperty(MedInteractions, fiInfo.get(2))
										.addProperty(SideEffects, fiInfo.get(8))
										.addProperty(HowToConserve, fiInfo.get(9)));
				
		
		return infar;		
	}

}

