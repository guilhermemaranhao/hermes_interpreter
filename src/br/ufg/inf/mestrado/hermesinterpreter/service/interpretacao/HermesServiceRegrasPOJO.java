package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao;

import org.mindswap.pellet.jena.PelletInfGraph;

import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesInterpreterRulesService;

import com.hp.hpl.jena.rdf.model.InfModel;

/**
 * Encapsula os atributos básicos para inferência baseada em regras, que são reutilizados a cada notificação de contexto.
 * @author guilhermemaranhao
 *
 */
public class HermesServiceRegrasPOJO {

	private String caminhoOntologia;
	private HermesInterpreterRulesService hermesRule;
	private InfModel infModel;
	private PelletInfGraph pellet;
	
	public String getCaminhoOntologia() {
		return caminhoOntologia;
	}
	public void setCaminhoOntologia(String caminhoOntologia) {
		this.caminhoOntologia = caminhoOntologia;
	}
	public HermesInterpreterRulesService getHermesRule() {
		return hermesRule;
	}
	public void setHermesRule(HermesInterpreterRulesService hermesRule) {
		this.hermesRule = hermesRule;
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
