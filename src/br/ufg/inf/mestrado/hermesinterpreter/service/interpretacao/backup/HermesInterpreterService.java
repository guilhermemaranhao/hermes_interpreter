package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.backup;

import java.io.ByteArrayInputStream;

import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public abstract class HermesInterpreterService {

	protected Model modeloIndividuoAtual = null;
	//protected InfModel infModel = null;
	
	public abstract void inferirSituacao(HermesInterpreterTO to);
	
	protected InfModel atualizarContexto(InfModel infModel, HermesInterpreterTO hermesTO)
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
		return infModel;
	}
}
