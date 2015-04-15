package lpdV01;

import java.io.InputStream;
import java.util.ArrayList;

import com.snowtide.pdf.Document;
import com.snowtide.PDF;
import com.snowtide.pdf.OutputTarget;

public class ReadPDF {

	public ArrayList<String> readRCM(InputStream is) throws java.io.IOException{
		
		Document pdf = PDF.open(is, "pdfName");		
		StringBuffer text = new StringBuffer(1024);        
		OutputTarget ot =  new OutputTarget(text);
        
        pdf.pipe(ot);
		pdf.close();
        
        String line[] = text.toString().split("\n");
        
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
        	if(s.contains("2. COMPOSIÇÃO QUALITATIVA E QUANTITATIVA") && !s.contains("ver")){
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
        			
        			if(s.contains("Excipientes:")){
        				l++;
        				s = line[l];
        			}
        		}
        	}
//      -------------------------------------------------------------------------
        	
        	if((s.contains("4.1 Indicações terapêuticas") || s.contains("4.1. Indicações terapêuticas") ||
        			s.contains("4.1. Indicações Terapêuticas")) && !s.contains("ver")){
        		l+= 2;
        		s = line[l];
        		while(!(s.contains("4.2 Posologia e modo de administração")) &&
        				!(s.contains("4.2. Posologia e modo de administração"))){
        			indications = indications.concat(s);
        			indications += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//       -------------------------------------------------------------------------        	
        	
        	if((s.contains("4.2 Posologia e modo de administração") || s.contains("4.2. Posologia e modo de administração")) &&
        			!s.contains("ver")){
        		l += 2;
        		s = line[l];
        		while(!(s.contains("4.3 Contra-indicações"))){
        			if(s.contains("por dia") || s.contains("diária máxima")){
        				posology = posology.concat(s);
        				break;
        			}
        			l++;
        			s = line[l];
        		}
        	}
//       --------------------------------------------------------------------------
        	
        	if((s.contains("4.3 Contra-indicações") || s.contains("4.3. Contra-indicações") || s.contains("4.3 Contraindicações")) &&
        			!s.contains("ver")){
        		l += 2;
        		s = line[l];
        		while(!(s.contains("4.4 Advertências e precauções especiais de utilização")) &&
        				!(s.contains("4.4. Advertências e precauções especiais de utilização"))){
        			contraindications = contraindications.concat(s);
        			contraindications += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//        --------------------------------------------------------------------------
        	if((s.contains("4.5 Interacções medicamentosas e outras formas de interacção") || 
        			s.contains("4.5. Interaccções medicamentosas e outras formas de interacção") ||
        			s.contains("4.5 Interações medicamentosas e outras formas de interação")) &&
        			!s.contains("ver")){
        		l += 2;
        		s = line[l];
        		while(!(s.contains("4.6 Gravidez e aleitamento")) &&
        				!(s.contains("4.6. Gravidez e aleitamento")) &&
        				!(s.contains("4.6 Fertilidade, gravidez e aleitamento"))){
        			if(!(s.equals("")) && !(s.equals("\r"))){
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
        	if((s.contains("6.1 Lista dos excipientes") || s.contains("6.1. Lista dos excipientes") || 
        			s.contains("6.1 Lista de excipientes")) &&
        			!s.contains("ver")){
        		l += 2;
        		s = line[l];
        		while(!(s.contains("6.2 Incompatibilidades")) && !(s.contains("6.2. Incompatibilidades"))){
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
        
        return information;
	}
	
	
	
	public ArrayList<String> readFI(InputStream is) throws java.io.IOException {
		
		Document pdf = PDF.open(is, "pdfName");
		StringBuffer text = new StringBuffer(1024);        
		OutputTarget ot =  new OutputTarget(text);
        
        pdf.pipe(ot);
        pdf.close();
        
        String line[] = text.toString().split("\n");
        
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
        		if(!(s.contains("")) && !(s.contains("\r"))){
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
        		while(!(s.equals("")) && !(s.equals("\r"))){
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
        					|| s.contains("mais frequentemente associados � utilização")){
            			sideEffects = "";
            			while(!(s.equals("")) && !(s.equals("\r"))){
            				sideEffects = sideEffects.concat(s);
            				sideEffects += ' ';
                			l++;
                			s = line[l];
            			}        			
            		}
            		
            		if( (s.contains(":") && sideEffects.isEmpty()) || (s.contains(":") && sideEffects.contains(":")) ){
            			while(!(s.equals("")) && !(s.equals("\r"))){
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
        
                
        return information;
	}
	
	
}

