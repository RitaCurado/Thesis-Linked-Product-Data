package main;

import java.util.ArrayList;
import com.hp.hpl.jena.rdf.model.*;


public class Medicine {
	
	
	public Model infomedModel(String substancy, String name, String type, String dosage, String holder, String generic){
		
		Model info = ModelFactory.createDefaultModel();
		
		String infomedURI = "http://infomed/";
		info.setNsPrefix("", infomedURI);
		
		Resource r = info.createResource(infomedURI + name);
		
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
		
	public Model infarmedModel(String substancy, String name, String type, String dosage,
								String numUnits, int prescriptionCode, float price, String generic,
								String rcm, ArrayList<String> rcmInfo, String fi, ArrayList<String> fiInfo){

		String infarmedURI = "http://infarmed/";
		
		Model infar = ModelFactory.createDefaultModel();
		
		infar.setNsPrefix("", infarmedURI);
		infar.setNsPrefix("RCM", "http://infarmed/RCM/");
		infar.setNsPrefix("FI", "http://infarmed/FI/");
		
		// create the resource
		Resource m = infar.createResource(infarmedURI + prescriptionCode + "/" + name);
		
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
	
	public Model completeModel(Model complete, Model infar, Model info){
		
		String completeURI = "http://medicine/";
		complete.setNsPrefix("", completeURI);
		complete.setNsPrefix("RCM", completeURI+"RCM/");
		complete.setNsPrefix("FI", completeURI+"FI/");
		
		
		Property NAME = complete.createProperty(completeURI + "NAME");
		Property SUBST = complete.createProperty(completeURI + "SUBSTANCE");
		Property TYPE = complete.createProperty(completeURI + "TYPE");
		Property DOSE = complete.createProperty(completeURI +"DOSAGE");
		Property HOLDER = complete.createProperty(completeURI + "HOLDER");
		Property GENERIC = complete.createProperty(completeURI + "GENERIC");
		
		Property UNITS = complete.createProperty(completeURI + "UNITS");
		Property CODE = complete.createProperty(completeURI + "CODE");
		Property PRICE = complete.createProperty(completeURI + "PRICE");
		Property RCM = complete.createProperty(completeURI + "RCM");
		Property FI = complete.createProperty(completeURI + "FI");
		
		Property UrlRcm = complete.createProperty(completeURI + "RCM/Url");
		Property Composition = complete.createProperty(completeURI + "RCM/Composition");
		Property Indications = complete.createProperty(completeURI + "RCM/Indications");
		Property Posology = complete.createProperty(completeURI + "RCM/Posology");
		Property Contraindications = complete.createProperty(completeURI + "RCM/Contraindications");
		Property Interactions = complete.createProperty(completeURI + "RCM/Interactions");
		
		Property UrlFi = complete.createProperty(completeURI + "FI/Url");
		Property Definition = complete.createProperty(completeURI + "FI/Definition");
		Property DoNotUse = complete.createProperty(completeURI + "FI/DoNotUse");
		Property MedInteractions = complete.createProperty(completeURI + "FI/Interactions");
		Property SideEffects = complete.createProperty(completeURI + "FI/SideEffects");
		Property HowToConserve = complete.createProperty(completeURI + "FI/HowToConserve");
		
		String name = "";
		
		if(info == null || infar != null)
			name = infar.listObjectsOfProperty(infar.getProperty("http://infarmed/NAME")).next().toString();
		else if(infar == null)
			name = info.listObjectsOfProperty(info.getProperty("http://infomed/NAME")).next().toString();
		
		Resource r = complete.createResource(completeURI + name);
		
		r.addProperty(NAME, name);
		
		if(info != null){
			r.addProperty(SUBST, info.listObjectsOfProperty(info.getProperty("http://infomed/SUBSTANCE")).next().toString());
			r.addProperty(TYPE, info.listObjectsOfProperty(info.getProperty("http://infomed/TYPE")).next().toString());
			r.addProperty(DOSE, info.listObjectsOfProperty(info.getProperty("http://infomed/DOSAGE")).next().toString());
			r.addProperty(HOLDER, info.listObjectsOfProperty(info.getProperty("http://infomed/HOLDER")).next().toString());
			r.addProperty(GENERIC, info.listObjectsOfProperty(info.getProperty("http://infomed/GENERIC")).next().toString());
		}
		
		else if(infar != null){
			r.addProperty(SUBST, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/SUBSTANCE")).next().toString());
			r.addProperty(TYPE, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/TYPE")).next().toString());
			r.addProperty(DOSE, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/DOSAGE")).next().toString());
			r.addProperty(HOLDER, "");
			r.addProperty(GENERIC, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/GENERIC")).next().toString());
		}
		
		if(infar != null){
			r.addProperty(UNITS, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/UNITS")).next().toString());
			r.addProperty(CODE, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/CODE")).next().toString());
			r.addProperty(PRICE, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/PRICE")).next().toString());
			r.addProperty(RCM,
					complete.createResource()
								.addProperty(UrlRcm, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/RCM/Url")).next().toString())
								.addProperty(Composition, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/RCM/Composition")).next().toString())
								.addProperty(Indications, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/RCM/Indications")).next().toString())
								.addProperty(Posology, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/RCM/Posology")).next().toString())
								.addProperty(Contraindications, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/RCM/Contraindications")).next().toString())
								.addProperty(Interactions, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/RCM/Interactions")).next().toString())			
					);
			r.addProperty(FI,
					complete.createResource()
								.addProperty(UrlFi, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/FI/Url")).next().toString())
								.addProperty(Definition, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/FI/Definition")).next().toString())
								.addProperty(DoNotUse, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/FI/DoNotUse")).next().toString())
								.addProperty(MedInteractions, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/FI/Interactions")).next().toString())
								.addProperty(SideEffects, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/FI/SideEffects")).next().toString())
								.addProperty(HowToConserve, infar.listObjectsOfProperty(infar.getProperty("http://infarmed/FI/HowToConserve")).next().toString())
					);
		}
		
		else{
			r.addProperty(UNITS, "");
			r.addProperty(CODE, "");
			r.addProperty(PRICE, "");
			r.addProperty(RCM,
					complete.createResource()
								.addProperty(UrlRcm, "")
								.addProperty(Composition, "")
								.addProperty(Indications, "")
								.addProperty(Posology, "")
								.addProperty(Contraindications, "")
								.addProperty(Interactions, "")			
					);
			r.addProperty(FI,
					complete.createResource()
								.addProperty(UrlFi, "")
								.addProperty(Definition, "")
								.addProperty(DoNotUse, "")
								.addProperty(MedInteractions, "")
								.addProperty(SideEffects, "")
								.addProperty(HowToConserve, "")
					);
		}
		
		
		return complete;
	}
}
