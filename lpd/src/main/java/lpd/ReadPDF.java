package lpd;

import java.io.InputStream;
import java.util.ArrayList;

import com.snowtide.pdf.Document;
import com.snowtide.PDF;
import com.snowtide.pdf.OutputTarget;

public class ReadPDF {

	public ArrayList<String> readRCM(InputStream is, ArrayList<String> information) throws java.io.IOException{
		
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
        
        //ArrayList<String> information = new ArrayList<String>();        
        
        while(l < line.length){
        	s = line[l];
        	
//      -------------------------------------------------------------------------
        	if(s.contains("2. COMPOSIÇÃO QUALITATIVA E QUANTITATIVA") 
        			|| s.contains("COMPOSIÇÃO QUALITATIVA E QUANTITATIVA") && !s.contains("ver")){
        		l += 2;
        		s = line[l];
        		char[] c;
        		int pos;
        		while(s.contains("Cada comprimido contém") || s.contains("1 comprimido contém")){
        			
        			if(s.contains("Cada comprimido contém\r") || s.contains("Cada comprimido contém:")){
        				while(!s.contains("Excipientes:") && !s.contains("Excipiente(s):")
        						&& !s.contains("Lista completa de excipientes")){
        					l++;
        					s = line[l];
        					composition += s;
        					composition += "; ";
        				}
        			}
        			else{        				
        				c = s.toCharArray();
        				if(s.contains("1 comprimido contém"))
        					pos = 20;
        				else
        					pos = 23;
        				for (; c[pos] != ',' && c[pos] != '.' && c[pos] != '\r'; pos++)
        					composition += c[pos];
        				composition += '\n';
        				l+= 2;
        				s = line[l];
        				
        			}
        			
        			if(s.contains("Excipientes:") || s.contains("Excipiente(s):")){
        				l++;
        				s = line[l];
        			}
        		}
        	}
//      -------------------------------------------------------------------------
        	
        	if((s.contains("4.1 Indicações terapêuticas\r") || s.contains("4.1. Indicações terapêuticas\r") ||
        			s.contains("4.1. Indicações Terapêuticas\r") || s.contains("4.1Indicações terapêuticas\r")
        			|| s.contains("4.1.	Indicações terapêuticas\r") || s.contains("Indicações terapêuticas\r")) && !s.contains("ver")){
        		l+= 2;
        		s = line[l];
        		while(!(s.toLowerCase().contains("4.2 posologia e modo de administração")) &&
        				!(s.toLowerCase().contains("4.2. posologia e modo de administração")) &&
        				!(s.toLowerCase().contains("4.2.posologia e modo de administração")) &&
        				!(s.toLowerCase().contains("4.2posologia e modo de administração")) &&
        				!(s.toLowerCase().contains("posologia e modo de administração"))){
        			indications = indications.concat(s);
        			indications += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//       -------------------------------------------------------------------------        	
        	
        	if((s.toLowerCase().contains("4.2 posologia e modo de administração") || s.toLowerCase().contains("4.2. posologia e modo de administração")
        			|| s.toLowerCase().contains("4.2.posologia e modo de administração") || s.toLowerCase().contains("4.2posologia e modo de administração")
        			|| s.toLowerCase().contains("posologia e modo de administração")) && !s.contains("ver") && !s.contains(")")){
        		l += 2;
        		s = line[l];
        		while(!(s.toLowerCase().contains("4.3 contra-indicações")) && !(s.toLowerCase().contains("4.3. contra-indicações"))
        				 && !(s.toLowerCase().contains("4.3contra-indicações")) && !(s.toLowerCase().contains("4.3.contra-indicações"))
        				 && !(s.toLowerCase().contains("4.3 contraindicações")) && !(s.toLowerCase().contains("4.3contraindicações"))
        				 && !(s.toLowerCase().contains("contraindicações") && !s.contains("ver"))
        				 && !(s.toLowerCase().contains("contra-indicações") && !s.contains("ver"))){
        			if(s.contains("por dia") || s.contains("diária máxima")){
        				posology = posology.concat(s);
        				break;
        			}
        			l++;
        			s = line[l];
        		}
        	}
//       --------------------------------------------------------------------------
        	
        	if((s.contains("4.3 Contra-indicações") || s.contains("4.3Contra-indicações") || s.contains("4.3. Contra-indicações")
        			|| s.contains("4.3.Contra-indicações") || s.contains("4.3 Contraindicações") || s.contains("4.3Contraindicações"))
        			&& !s.contains("ver")){
        		l += 2;
        		s = line[l];
        		while(!(s.toLowerCase().contains("4.4 advertências e precauções especiais de utilização")) &&
        				!(s.toLowerCase().contains("4.4. advertências e precauções especiais de utilização")) &&
        				!(s.toLowerCase().contains("4.4.advertências e precauções especiais de utilização")) &&
        				!(s.toLowerCase().contains("4.4advertências e precauções especiais de utilização")) &&
        				!(s.toLowerCase().contains("advertências e precauções especiais de utilização"))){
        			contraindications = contraindications.concat(s);
        			contraindications += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//        --------------------------------------------------------------------------
        	if((s.toLowerCase().contains("4.5 interacções medicamentosas e outras formas de interacção") || 
        			s.toLowerCase().contains("4.5. interaccções medicamentosas e outras formas de interacção") ||
        			s.toLowerCase().contains("4.5 interações medicamentosas e outras formas de interação") ||
        			s.toLowerCase().contains("4.5interações medicamentosas e outras") ||
        			s.contains("Interaccções medicamentosas e outras formas de interacção")) && !s.contains("ver") && !s.contains(")")){
        		l += 2;
        		s = line[l];
        		while(!(s.toLowerCase().contains("4.6 gravidez e aleitamento")) &&
        				!(s.toLowerCase().contains("4.6 gravidez e de aleitamento")) &&
        				!(s.toLowerCase().contains("4.6. gravidez e aleitamento")) &&
        				!(s.toLowerCase().contains("4.6 fertilidade, gravidez e aleitamento")) &&
        				!(s.toLowerCase().contains("4.6fertilidade, gravidez e aleitamento")) &&
        				!(s.toLowerCase().contains("4.6 fertilidade gravidez e aleitamento")) &&
        				!(s.toLowerCase().contains("4.6gravidez e aleitamento")) &&
        				!(s.toLowerCase().contains("4.6.gravidez e aleitamento"))){
        			
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
        	if((s.toLowerCase().contains("6.1 lista dos excipientes") || s.toLowerCase().contains("6.1. lista dos excipientes") ||
        			s.toLowerCase().contains("6.1.lista dos excipientes") || s.toLowerCase().contains("6.1 lista de excipientes") ||
        			s.toLowerCase().contains("6.1lista de excipientes")) && !s.contains("ver")){
        		l += 2;
        		s = line[l];
        		while(!(s.contains("6.2 Incompatibilidades")) && !(s.contains("6.2. Incompatibilidades"))
        				&& !(s.contains("6.2.Incompatibilidades")) && !(s.contains("6.2Incompatibilidades"))
        				&& !(s.contains("Incompatibilidades"))){
        			composition = composition.concat(s);
        			composition += '\n';
        			l++;
        			s = line[l];
        		}
        	}
//         --------------------------------------------------------------------------
        	
        	
        	l++;
        }
        
        information.set(0, composition);
        information.set(1, indications);
        information.set(2, posology);
        information.set(3, contraindications);
        information.set(4, interactions);
        
        return information;
	}
	
	
	
	public ArrayList<String> readFI(InputStream is, ArrayList<String> information) throws java.io.IOException {
		
		Document pdf = PDF.open(is, "pdfName");
		StringBuffer text = new StringBuffer(1024);        
		OutputTarget ot =  new OutputTarget(text);
        
        pdf.pipe(ot);
        pdf.close();
        
        String line[] = text.toString().split("\n");
        
        //ArrayList<String> information = new ArrayList<String>();
        int l = 0;
        String s;
        boolean structure = false;
        
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
        	
        	if(l > 30 && structure == false){
        		break;
        	}
        	
//        	-----------------------------------------------------------------------------
        	if(s.contains("O que contém este folheto") || s.contains("Neste folheto") || 
        			s.contains("Este folheto contém")){
        		structure = true;
        		l += 7;
        		s = line[l];
        		if(!(s.contains("")) && !(s.contains("\r"))){
        			l++;
        			s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("1. o que é") || s.toLowerCase().contains("o que é")){
        		l += 1;
        		s = line[l];
        		while(!(s.toLowerCase().contains("2. antes de tomar")) && !s.toLowerCase().contains("antes de tomar")
        				&& !s.toLowerCase().contains("2. antes de utilizar") && !s.toLowerCase().contains("2. o que precisa de saber antes de tomar")
        				&& !s.toLowerCase().contains("2. o que precisa de saber antes de utilizar")){
        			definition = definition.concat(s);
        			definition += ' ';
        			l++;
        			s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("2. antes de tomar") || s.toLowerCase().contains("o que é preciso saber antes de tomar")
        			|| s.toLowerCase().contains("2. antes de utilizar")
        			|| (s.toLowerCase().contains("antes de tomar") && !s.toLowerCase().contains("leia atentamente")
        					&& !s.toLowerCase().contains("fale com o seu médico") && !s.toLowerCase().contains("consulte o seu médico"))
        			|| s.toLowerCase().contains("2. o que precisa de saber antes de tomar")
        			|| s.toLowerCase().contains("2.o que precisa de saber antes de tomar")
        			|| s.toLowerCase().contains("2. o que precisa de saber antes de utilizar")){
        		
        		if(l > 30 && structure == false){
            		break;
            	}
        		
        		while(!s.contains("Não tome") && !s.contains("não deve usar") && !s.contains("Não utilize")
        				&& !s.contains("NÃO tome") && !s.contains("nunca deverei tomar")){
        			l++;
        			s = line[l];
        		}
        		
        		l++;
    			s = line[l];
    			
        		while(!(s.contains("Tome especial cuidado")) && !(s.contains("com precaução"))
        				&& !(s.contains("Advertências e precauções")) && !(s.contains("Advertências e Precauções"))
        				&& !(s.contains("Gravidez e aleitamento"))){
        			notUse = notUse.concat(s);
        			l++;
        			s = line[l];
        		}
        		
        		while((!s.toLowerCase().contains("tomar") || !s.toLowerCase().contains("com outros medicamentos"))
        				&& (!s.toLowerCase().contains("tomar") || !s.toLowerCase().contains("com outros"))
        				&& (!s.toLowerCase().contains("utilizar") || !s.toLowerCase().contains("com outros medicamentos"))
        				&& !s.contains("Tomar outros medicamentos")
        				&& !s.contains("interações medicamentosas") && !s.contains("Outros medicamentos e")
        				&& !(s.contains("Gravidez e amamentação") && !s.contains("ver") && !s.contains("secção") && !s.contains(")") && !s.contains("-"))
        				&& !(s.contains("Gravidez e aleitamento") && !s.contains("ver") && !s.contains("secção") && !s.contains(")") && !s.contains("-"))
        				&& !(s.contains("Gravidez") && !s.contains(":") && !s.contains("ver") && !s.contains("secção") && !s.contains(")") && !s.contains("-"))){
        			l++;
        			s = line[l];
        		}
        		
        		if(s.contains("Gravidez")){
        			l += 2;
        			s = line[l];
        			while(!s.contains("Crianças e idosos") && !s.contains("Condução de veículos e utilização de máquinas")
        					&& !s.contains("Condução e uso de máquinas")){
        				pregnancy = pregnancy.concat(s);
            			pregnancy += ' ';
            			l++;
                		s = line[l];
        			}
        			while(!s.contains("Condução de veículos e utilização de máquinas") && !s.contains("Condução e uso de máquinas")){
        				l++;
        				s = line[l];
        			}
        			while(!s.contains("com outros medicamentos") && !s.toLowerCase().contains("3. como utilizar")){
        				driving = driving.concat(s);
            			driving += ' ';
            			l++;
                		s = line[l];
        			}
        			while(!s.toLowerCase().contains("4. como tomar") && !s.toLowerCase().contains("3. como utilizar")
        					&& !s.toLowerCase().contains("3. como tomar") && !s.toLowerCase().contains("como tomar")){
        				interactions = interactions.concat(s);
            			interactions += ' ';
            			l++;
            			s = line[l];
        			}
        		}
        		
        		else{
        			while(!(s.contains("Gravidez")) && !(s.contains("Gravidez e amamentação")) &&
            				!(s.contains("Gravidez e aleitamento")) && !(s.contains("Gravidez e Aleitamento"))){
            			interactions = interactions.concat(s);
            			interactions += ' ';
            			l++;
            			s = line[l];
            		}
            		
            		l++;
            		s = line[l];
            		
            		while(!s.toLowerCase().contains("condução de veículos e utilização de máquinas")
            				&& !s.contains("Condução e veículos e utilização de máquinas")
            				&& !s.contains("Condução veículos e utilização de máquinas")
            				&& !s.contains("capacidade de conduzir") && !s.toLowerCase().contains("3. como tomar")){
            			pregnancy = pregnancy.concat(s);
            			pregnancy += ' ';
            			l++;
                		s = line[l];
            		}
            		
            		while(!(s.toLowerCase().contains("3. como tomar")) && !(s.toLowerCase().contains("como tomar"))
            				&& !(s.toLowerCase().contains("3. como utilizar"))){
            			driving = driving.concat(s);
            			driving += ' ';
            			l++;
                		s = line[l];
            		}
        		}        		
        	}
        	
//        	--------------------------------------------------------------------------
        	if(s.toLowerCase().contains("3. como tomar") || s.toLowerCase().contains("4. como tomar")
        			|| s.toLowerCase().contains("3. como utilizar") || s.toLowerCase().contains("como tomar")
        			|| s.toLowerCase().contains("3.como tomar")){
        		l += 1;
        		s = line[l];
        		
        		while(!s.toLowerCase().contains("se tomou mais") && !s.toLowerCase().contains("se tomar mais")
        				&& !s.toLowerCase().contains("se utilizar mais") && !s.contains("mais do que deveria")
        				&& !s.toLowerCase().contains("se lhe foi administrado mais") && !s.contains("Caso se tenha esquecido de utilizar")
        				&& !s.toLowerCase().contains("efeitos secundários")){
        			howToTake = howToTake.concat(s);
        			howToTake += ' ';
        			l++;
        			s = line[l];
        		}
        		
        		if(howToTake.isEmpty() && !s.toLowerCase().contains("efeitos secundários")){
        			while(!s.contains("Caso se tenha esquecido de tomar") && !s.toLowerCase().contains("se tomou mais")){
        				howToTake = howToTake.concat(s);
            			howToTake += ' ';
            			l++;
            			s = line[l];
        			}
        		}
        	}
//        	--------------------------------------------------------------------------
        	if(s.contains("Se tomar mais") || s.contains("em caso de sobredosagem") || s.toLowerCase().contains("se tomou mais")
        			|| s.contains("Se utilizar mais") || s.contains("mais do que deveria")){
        		l += 2;
        		s = line[l];
        		
        		while(!s.contains("Tratamento") && !(s.contains("Caso se tenha esquecido")) && !s.toLowerCase().contains("se se esquecer")
        				&& !s.toLowerCase().contains("4. efeitos secundários") && !s.toLowerCase().contains("5. efeitos secundários")
        				&& !s.toLowerCase().contains("4. efeitos secundàrios") && !s.contains("Caso de tenha esquecido")
        				&& !s.toLowerCase().contains("efeitos secundários")){
        			overdosage = overdosage.concat(s);
        			overdosage += ' ';
        			l++;
        			s = line[l];
        		}
        	}
//        	---------------------------------------------------------------------------
        	if(s.contains("Caso se tenha esquecido") || s.contains("Caso de tenha esquecido")){
        		l += 2;
        		s = line[l];
        		//System.out.println(s);
        		while(!(s.equals("")) && !(s.equals("\r")) && !(s.toLowerCase().contains("efeitos secundários"))){
        			ifForgotten = ifForgotten.concat(s);
        			ifForgotten += ' ';
        			l++;
        			s = line[l];
        			//System.out.println(s);
        		}
        	}
//        	----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("4. efeitos secundários") || s.toLowerCase().contains("5. efeitos secundários")
        			|| s.toLowerCase().contains("efeitos secundários possíveis")){
        		l += 2;
        		s = line[l];
        		
        		while(!s.toLowerCase().contains("como conservar") && !s.toLowerCase().contains("5. como conservar")
        				&& !s.toLowerCase().contains("5. conservação") && !s.toLowerCase().contains("6. como conservar")
        				&& !s.toLowerCase().contains("conservação")){
        			
        			if(s.contains("mais frequentemente observados") || s.contains("efeitos secundários frequentes")
        					|| s.contains("efeitos secundários mais frequentes")
        					|| s.contains("seguintes sintomas") || s.contains("com maior frequência")
        					|| s.contains("mais frequentemente associados à utilização")){
            			sideEffects = "";
            			while(!(s.equals("")) && !(s.equals("\r"))){
            				sideEffects = sideEffects.concat(s);
            				sideEffects += ' ';
                			l++;
                			s = line[l];
            			}        			
            		}
            		
            		if( (s.contains(":") && sideEffects.isEmpty()) || (s.contains(":") && sideEffects.contains(":")) ){
            			//System.out.println("DOIS PONTOS: " + s);
            			while(!(s.equals("")) && !(s.equals("\r")) && !(s.toLowerCase().contains("5. como conservar"))
            					&& !(s.toLowerCase().contains("como conservar")) && !s.toLowerCase().contains("5. conservação")){
            				sideEffects = sideEffects.concat(s);
            				sideEffects += ' ';
                			l++;
                			s = line[l];
                			//System.out.println(s);
            			}
            		}
            		if(s.toLowerCase().contains("5. como conservar") || s.toLowerCase().contains("como conservar")
            				|| s.toLowerCase().contains("5. conservação") || s.toLowerCase().contains("5.como conservar")
            				|| s.toLowerCase().contains("conservação"))
            			break;
            		l++;
            		s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------
        	if(s.toLowerCase().contains("5. como conservar") || s.toLowerCase().contains("6. como conservar")
        			|| s.toLowerCase().contains("5. conservação") || s.toLowerCase().contains("conservação")
        			|| s.toLowerCase().contains("como conservar") || s.toLowerCase().contains("5.como conservar")){
        		l += 2;
        		s = line[l];        		
        		while(!s.toLowerCase().contains("6. outras informações") && !s.toLowerCase().contains("recomendações gerais")
        				&& !s.toLowerCase().contains("e outras informações") && !s.toLowerCase().contains("outras informações")
        				&& !s.toLowerCase().contains("e outras infomações") && !s.contains("Data da última aprovação deste folheto")
        				&& !s.toLowerCase().contains("texto revisto em") && !s.contains("Este folheto foi aprovado pela última vez")
        				&& !s.contains("Este folheto informativo foi")){
        			howToConserve = howToConserve.concat(s);
        			howToConserve += ' ';
        			l++;
        			s = line[l];
        		}
        	}
//        	-----------------------------------------------------------------------------        	
        	
        	l++;
        }
        
        information.set(0, definition);
        information.set(1, notUse);
        information.set(2, interactions);
        information.set(3, pregnancy);
        information.set(4, driving);
        information.set(5, howToTake);
        information.set(6, overdosage);
        information.set(7, ifForgotten);
        information.set(8, sideEffects);
        information.set(9, howToConserve);
        
                
        return information;
	}
	
	
}

