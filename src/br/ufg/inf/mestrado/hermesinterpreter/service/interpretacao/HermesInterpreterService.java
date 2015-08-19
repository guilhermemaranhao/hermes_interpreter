package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao;

import java.io.ByteArrayInputStream;

import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Classe abstrata que implementa o padrão de projeto Strategy ao expor um método que é acessado de forma transparente pelo cliente, que desconhece
 * o mecanismo de inferência acessado para o tópico.
 * @author guilhermemaranhao
 *
 */
public abstract class HermesInterpreterService {

	protected Model modeloIndividuoAtual = null;
	protected InfModel infModel = null;
	
	/**
	 * Método exposto que deve ser implementado pelos serviços de inferência.
	 * @param to
	 */
	public abstract void inferirSituacao(HermesInterpreterTO to);
	
	/**
	 * Atualizado o objeto {@link InfModel} conforme o contexto é notificado.
	 * @param hermesTO
	 */
	protected void atualizarContexto(HermesInterpreterTO hermesTO)
	{
		// Cria modelo ontológico para instância de dados RDF.
		ByteArrayInputStream bais = new ByteArrayInputStream(hermesTO.getContexto());
		Model novoContexto = ModelFactory.createDefaultModel();

		try {
			novoContexto.read(bais, hermesTO.getCaminhoOntologia(), hermesTO.getTipoSerializacao());
			if (modeloIndividuoAtual != null)
			{
				infModel.remove(modeloIndividuoAtual);
			}
			modeloIndividuoAtual = novoContexto;
			bais.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
