package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao;

import org.mindswap.pellet.jena.PelletInfGraph;

import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesInterpreterOntologyService;

import com.hp.hpl.jena.rdf.model.InfModel;

public class HermesServiceOntologicaPOJO {

	private String caminhoOntologia;
	private HermesInterpreterOntologyService hermesOntology;
	private InfModel infModel;
	private PelletInfGraph pellet;
	
	public String getCaminhoOntologia() {
		return caminhoOntologia;
	}
	public void setCaminhoOntologia(String caminhoOntologia) {
		this.caminhoOntologia = caminhoOntologia;
	}
	public HermesInterpreterOntologyService getHermesOntology() {
		return hermesOntology;
	}
	public void setHermesOntology(HermesInterpreterOntologyService hermesOntology) {
		this.hermesOntology = hermesOntology;
	}
	public InfModel getInfModel() {
		return infModel;
	}
	public void setInfModel(InfModel infModel) {
		this.infModel = infModel;
	}
	public PelletInfGraph getPellet() {
		return pellet;
	}
	public void setPellet(PelletInfGraph pellet) {
		this.pellet = pellet;
	}
	
}
