package lpdV01;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.lucene.document.Document;

import com.snowtide.pdf.OutputTarget;
import com.snowtide.pdf.PDFTextStream;

public class ReadPDF {

	public ArrayList<String> readRCM(InputStream is){
		
		
		
		PDFTextStream pdf = new PDFTextStream(is, "pdfName");
		StringBuffer text = new StringBuffer(1024);
        
		OutputTarget ot =  new OutputTarget(text);
        
        pdf.pipe(ot);
        pdf.close();
        
        String line[] = text.toString().split("\n");
        //System.out.println("File legth: " + line.length);
        
        int l = 0;
        String s;
        String composition = "";
        String indications = "";
        String posology = "";
        String contraindications = "";
        String interactions = "";
        
        ArrayList<String> information = new ArrayList<String>();
        
        
        while(l < line.length){
        	s = line[l];
        	
//      -------------------------------------------------------------------------
        	if(s.equals("2. COMPOSIÇÃO QUALITATIVA E QUANTITATIVA")){
        		l += 2;
        		s = line[l];
        		char[] c;
        		int pos;
        		while(s.contains("Cada comprimido contém") || s.contains("1 comprimido contém")){
        			c = s.toCharArray();
        			if(s.contains("1 comprimido contém"))
        				pos = 20;
        			else
        				pos = 23;
        			for (; c[pos] != ',' && c[pos] != '.'; pos++)
        				composition += c[pos];
        			composition += '\n';
        			l+= 2;
        			s = line[l];
        			
        			if(s.equals("Excipientes:")){
        				l++;
        				s = line[l];
        			}
        		}
        	}
//      -------------------------------------------------------------------------
        	
        	if(s.equals("4.1 Indicações terapêuticas") || s.equals("4.1. Indicações terapêuticas") ||s.equals("4.1. Indicações Terapêuticas")){
        		l+= 2;
        		s = line[l];
        		while(!(s.equals("4.2 Posologia e modo de administração")) &&
        				!(s.equals("4.2. Posologia e modo de administração"))){
        			indications = indications.concat(s);
        			indications += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//       -------------------------------------------------------------------------        	
        	
        	if(s.equals("4.2 Posologia e modo de administração") || s.equals("4.2. Posologia e modo de administração")){
        		l += 2;
        		s = line[l];
        		while(!(s.equals("4.3 Contra-indicações"))){
        			if(s.contains("por dia") || s.contains("diária máxima")){
        				posology = posology.concat(s);
        				break;
        			}
        			l++;
        			s = line[l];
        		}
        	}
//       --------------------------------------------------------------------------
        	
        	if(s.equals("4.3 Contra-indicações") || s.equals("4.3. Contra-indicações") || s.equals("4.3 Contraindicações")){
        		l += 2;
        		s = line[l];
        		while(!(s.equals("4.4 Advertências e precauções especiais de utilização")) &&
        				!(s.equals("4.4. Advertências e precauções especiais de utilização"))){
        			contraindications = contraindications.concat(s);
        			contraindications += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//        --------------------------------------------------------------------------
        	if(s.equals("4.5 Interacções medicamentosas e outras formas de interacção") || 
        			s.equals("4.5. Interacções medicamentosas e outras formas de interacção") ||
        			s.equals("4.5 Interações medicamentosas e outras formas de interação")){
        		l += 2;
        		s = line[l];
        		while(!(s.equals("4.6 Gravidez e aleitamento")) &&
        				!(s.equals("4.6. Gravidez e aleitamento")) &&
        				!(s.equals("4.6 Fertilidade, gravidez e aleitamento"))){
        			if(! (s.equals(""))){
	        			if(s.charAt(0) == '-'){
	            			interactions = interactions.concat(s);
	            			interactions += '\n';
	            		}
        			}
        			l++;
        			s = line[l];
        		}        		
        	}
//         --------------------------------------------------------------------------
        	if(s.equals("6.1 Lista dos excipientes") || s.equals("6.1. Lista dos excipientes") || 
        			s.equals("6.1 Lista de excipientes")){
        		l += 2;
        		s = line[l];
        		while(!(s.equals("6.2 Incompatibilidades")) && !(s.equals("6.2. Incompatibilidades"))){
        			composition = composition.concat(s);
        			composition += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//         --------------------------------------------------------------------------
        	
        	
        	l++;
        }
        
        information.add(composition);
        information.add(indications);
        information.add(posology);
        information.add(contraindications);
        information.add(interactions);
        
        /*
        System.out.println("COMPOSITION:");
        System.out.println(composition);
        System.out.println("INDICATIONS:");
        System.out.println(indications);
        System.out.println("POSOLOGY:");
        System.out.println(posology);
        System.out.println("CONTRAINDICATIONS:");
        System.out.println(contraindications);
        System.out.println("INTERACTIONS:");
        System.out.println(interactions);
        */
        
        return information;
	}
	
	
	
	public ArrayList<String> readFI(InputStream is){
		
		PDFTextStream pdf = new PDFTextStream(is, "pdfName");
		StringBuffer text = new StringBuffer(1024);
        
		OutputTarget ot =  new OutputTarget(text);
        
        pdf.pipe(ot);
        pdf.close();
        
        String line[] = text.toString().split("\n");
        //System.out.println("File length: " + line.length);
        
        ArrayList<String> information = new ArrayList<String>();
        int l = 0;
        String s;
        
        String definition = "";
        String notUse = "";
        String interactions = "";
        String pregnancy = "";
        String driving = "";
        String howToTake = "";
        String overdosage = "";
        String ifForgotten = "";
        String sideEffects = "";
        String howToConserve = "";
        
        while(l < line.length){
        	
        	s = line[l];
        	
//        	-----------------------------------------------------------------------------
        	if(s.contains("O que contém este folheto") || s.contains("Neste folheto") || 
        			s.contains("Este folheto contém")){
        		l += 7;
        		s = line[l];
        		if(!s.equals("")){
        			l++;
        			s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("1. o que é")){
        		l += 1;
        		s = line[l];
        		while(!(s.toLowerCase().contains("2. antes de tomar")) && !s.toLowerCase().contains("antes de tomar")){
        			definition = definition.concat(s);
        			definition += ' ';
        			l++;
        			s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("2. antes de tomar") || s.toLowerCase().contains("o que é preciso saber antes de tomar")){
        		while(!(s.contains("Não tome")) && !(s.contains("não deve usar"))){
        			l++;
        			s = line[l];
        		}
        		
        		l++;
    			s = line[l];
        		
        		while(!(s.contains("Tome especial cuidado")) && !(s.contains("com precaução"))
        				&& !(s.contains("Advertências e precauções")) ){
        			notUse = notUse.concat(s);
        			l++;
        			s = line[l];
        		}
        		
        		while((!s.toLowerCase().contains("tomar") || !s.toLowerCase().contains("com outros medicamentos")) && !(s.contains("interações medicamentosas")) && 
        				!(s.contains("Outros medicamentos e")) && !(s.contains("Gravidez e aleitamento"))){
        			l++;
        			s = line[l];
        		}
        		
        		if(s.contains("Gravidez e aleitamento")){
        			l += 2;
        			s = line[l];
        			while(!s.contains("Crianças e idosos")){
        				pregnancy = pregnancy.concat(s);
            			pregnancy += ' ';
            			l++;
                		s = line[l];
        			}
        			l += 2;
        			s = line[l];
        			while(!s.contains("Condução de veículos e utilização de máquinas")){
        				l++;
        				s = line[l];
        			}
        			l+=2;
        			s=line[l];
        			while(!s.contains("com outros medicamentos")){
        				driving = driving.concat(s);
            			driving += ' ';
            			l++;
                		s = line[l];
        			}
        			l+=2;
        			s=line[l];
        			while(!s.toLowerCase().contains("4. como tomar")){
        				interactions = interactions.concat(s);
            			interactions += ' ';
            			l++;
            			s = line[l];
        			}
        		}
        		
        		else{
        			while(!(s.contains("Gravidez")) && !(s.contains("Gravidez e amamentação")) &&
            				!(s.contains("Gravidez e aleitamento"))){
            			interactions = interactions.concat(s);
            			interactions += ' ';
            			l++;
            			s = line[l];
            		}
            		
            		l++;
            		s = line[l];
            		
            		while(!(s.contains("Condução de veículos e utilização de máquinas"))){
            			pregnancy = pregnancy.concat(s);
            			pregnancy += ' ';
            			l++;
                		s = line[l];
            		}
            		
            		l++;
            		s = line[l];
            		
            		while(!(s.toLowerCase().contains("3. como tomar"))){
            			driving = driving.concat(s);
            			driving += ' ';
            			l++;
                		s = line[l];
            		}
        		}        		
        	}
        	
//        	--------------------------------------------------------------------------
        	if(s.toLowerCase().contains("3. como tomar") || s.toLowerCase().contains("4. como tomar")){
        		l += 1;
        		s = line[l];
        		
        		while(!s.toLowerCase().contains("se tomou mais") && !s.toLowerCase().contains("se tomar mais")
        				&& !s.toLowerCase().contains("se utilizar mais")){
        			howToTake = howToTake.concat(s);
        			howToTake += ' ';
        			l++;
        			s = line[l];
        		}
        		
        		if(howToTake.isEmpty()){
        			while(!s.contains("Caso se tenha esquecido de tomar") && !s.toLowerCase().contains("se tomou mais")){
        				howToTake = howToTake.concat(s);
            			howToTake += ' ';
            			l++;
            			s = line[l];
        			}
        		}
        	}
//        	--------------------------------------------------------------------------
        	if(s.contains("Se tomar mais") || s.contains("em caso de sobredosagem") || s.toLowerCase().contains("se tomou mais")){
        		l += 2;
        		s = line[l];
        		
        		while(!s.contains("Tratamento") && !(s.contains("Caso se tenha esquecido")) && !s.toLowerCase().contains("se se esquecer")
        				&& !s.contains("4. Efeitos secundários")){
        			overdosage = overdosage.concat(s);
        			overdosage += ' ';
        			l++;
        			s = line[l];
        		}
        	}
//        	---------------------------------------------------------------------------
        	if(s.contains("Caso se tenha esquecido")){
        		l += 2;
        		s = line[l];
        		while(!s.equals("")){
        			ifForgotten = ifForgotten.concat(s);
        			ifForgotten += ' ';
        			l++;
        			s = line[l];
        		}
        	}
//        	----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("4. efeitos secundários") || s.toLowerCase().contains("5. efeitos secundários")){
        		l += 2;
        		s = line[l];
        		
        		while(!s.toLowerCase().contains("5. como conservar") && !s.toLowerCase().contains("6. como conservar")){
        			
        			if(s.contains("mais frequentemente observados") || s.contains("efeitos secundários frequentes")
        					|| s.contains("efeitos secundários mais frequentes")
        					|| s.contains("seguintes sintomas") || s.contains("com maior frequência")
        					|| s.contains("mais frequentemente associados à utilização")){
            			sideEffects = "";
            			while(!s.equals("")){
            				sideEffects = sideEffects.concat(s);
            				sideEffects += ' ';
                			l++;
                			s = line[l];
            			}        			
            		}
            		
            		if( (s.contains(":") && sideEffects.isEmpty()) || (s.contains(":") && sideEffects.contains(":")) ){
            			while(!s.equals("")){
            				sideEffects = sideEffects.concat(s);
            				sideEffects += ' ';
                			l++;
                			s = line[l];
            			}
            		}
            		l++;
            		s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("5. como conservar") || s.toLowerCase().contains("6. como conservar")){
        		l += 2;
        		s = line[l];        		
        		while(!s.toLowerCase().contains("6. outras informações") && !s.toLowerCase().contains("recomendações gerais")
        				&& !s.contains("e outras informações")){
        			howToConserve = howToConserve.concat(s);
        			howToConserve += ' ';
        			l++;
        			s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------        	
        	
        	l++;
        }
        
        information.add(definition);
        information.add(notUse);
        information.add(interactions);
        information.add(pregnancy);
        information.add(driving);
        information.add(howToTake);
        information.add(overdosage);
        information.add(ifForgotten);
        information.add(sideEffects);
        information.add(howToConserve);
        
        /*
        System.out.println("DEFINITION:");
        System.out.println(definition);
        System.out.println("NOT USE:");
        System.out.println(notUse);
        System.out.println("INTERACTIONS:");
        System.out.println(interactions);
        System.out.println("PREGNANCY:");
        System.out.println(pregnancy);
        System.out.println("DRIVING:");
        System.out.println(driving);
        System.out.println("HOW TO TAKE:");
        System.out.println(howToTake);
        System.out.println("OVERDOSAGE:");
        System.out.println(overdosage);
        System.out.println("IF FORGOTTEN:");
        System.out.println(ifForgotten);
        System.out.println("SIDE EFFECTS:");
        System.out.println(sideEffects);
        System.out.println("HOW TO CONSERVE:");
        System.out.println(howToConserve);
        */
        
        return information;
	}
	
	
	
	
	
	
	
}

