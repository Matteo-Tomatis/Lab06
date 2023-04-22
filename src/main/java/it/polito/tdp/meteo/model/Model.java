package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private MeteoDAO database = new MeteoDAO();
	private List<Citta> listaCitta = new ArrayList<>();
	private List<Citta> soluzione = new ArrayList<>();
	private int costoSoluzione = 100000;
 
	public Model() {
	}

	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		
		List<Rilevamento> listaRilevamenti = database.getAllRilevamenti(mese);
		
		int totMilano = 0;
		int totGenova = 0;
		int totTorino = 0;
		
		for(Rilevamento r : listaRilevamenti) {
			if(r.getLocalita().toLowerCase().contains("milano"))
				totMilano += r.getUmidita();
			if(r.getLocalita().toLowerCase().contains("torino"))
				totTorino += r.getUmidita();
			if(r.getLocalita().toLowerCase().contains("genova"))
				totGenova += r.getUmidita();
		}
		
		String stampa ="A Milano l'umidità media è stata di " + totMilano + "% /n A Torino l'umidità media è stata di " + totTorino + 
				"% /n A Genova l'umidità media è stata di " + totGenova;
		
		return stampa;
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		
		//crea le città con i dati necessari dal db
		Citta genova = new Citta("Genova", database.getRilevamentiCitta("Genova", mese));
		Citta torino = new Citta("Torino", database.getRilevamentiCitta("Torino", mese));
		Citta milano = new Citta("Milano", database.getRilevamentiCitta("Milano", mese));
		
		//salva le città nella lista
		listaCitta.clear();
		listaCitta.add(genova);
		listaCitta.add(torino);
		listaCitta.add(milano);
		
		//avvia la ricorsione
		List<Citta> parziale = new ArrayList<>();
		ricorsione(parziale, mese, 1);
		
		String risultato = "";
		for(Citta citta : soluzione) {
			risultato += citta.getNome() + "\n";
		}
		return risultato;
	}
	
	private void ricorsione(List<Citta> parziale , int mese, int giorno) {
		
		int costoParziale = 0;
		Citta ultimaCittaVisitata = null;
		
		if(giorno<=16) {
			costoParziale = calcolaCosto(parziale);
		}
		else {
			return;
		}
		
		if(giorno>1) {
			ultimaCittaVisitata = parziale.get(parziale.size()-1);
		}
		
		if(parziale.size()>0 && ultimaCittaVisitata.getCounter()>6) {
			return;
		}
		
		if(giorno==16) {
				if(costoParziale<costoSoluzione) {
				costoSoluzione=costoParziale;
				soluzione = new ArrayList<>(parziale);
				}
			return;
		}
		
		
		for(Citta c : listaCitta) {
			
			if(costoParziale<costoSoluzione) {
				
				//primo giorno
				if(giorno==1) {
					parziale.add(c);
					parziale.add(c);
					parziale.add(c);
					c.aggiornaCounter(3);
					ricorsione(parziale , mese, giorno+3);
					parziale.remove(parziale.size()-1);
					parziale.remove(parziale.size()-1);
					parziale.remove(parziale.size()-1);
					c.aggiornaCounter(-3);
				}
				
				//ho scelto una città uguale alla precedente
				else if(c.equals(parziale.get(parziale.size()-1))) {
					parziale.add(c);
					c.aggiornaCounter(1);
					ricorsione(parziale, mese, giorno+1);
					parziale.remove(parziale.size()-1);
					c.aggiornaCounter(-1);
				}
				
				else {
					parziale.add(c);
					parziale.add(c);
					parziale.add(c);
					c.aggiornaCounter(3);
					ricorsione(parziale , mese, giorno+3);
					parziale.remove(parziale.size()-1);
					parziale.remove(parziale.size()-1);
					parziale.remove(parziale.size()-1);
					c.aggiornaCounter(-3);
				}
			}
		}
		
	}

	private int calcolaCosto(List<Citta> parziale) {
		int costo = 0; 
		Citta cittaPrecedente = null;
		//ciclo su ogni elemento nella soluzione parziale
		for(int i=0; i<parziale.size()-1; i++) {
			
			//ciclo su ogni citta
			for(Citta c : listaCitta) {
				
				//confronto citta in parziale con la città nella lista
				if(c.equals(parziale.get(i))){
					costo += c.getRilevamenti().get(i).getUmidita();
					
					//verifico se ha cambiato città
					if(cittaPrecedente!=null && !cittaPrecedente.equals(c)) {
						costo += 100;
					}
					
					cittaPrecedente=c;
				}
			}
		}
		
		return costo;
	}

}
