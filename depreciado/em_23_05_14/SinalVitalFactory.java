package br.ufg.inf.mestrado.hermesinterpreter.businessobject;

import br.ufg.inf.mestrado.hermebase.model.TriplaRDF;
import br.ufg.inf.mestrado.hermebase.utils.Constantes;

import com.hp.hpl.jena.ontology.OntModel;

public class SinalVitalFactory {

	private static FrequenciaPulsoInterpreter frequenciaPulso;
	private static PressaoSanguineaInterpreter pressaoSanguinea;
	
	public static SinalVitalInterpreter getSinalVital(OntModel ontModel, TriplaRDF triplaRDF, String tipoSerializacao, String topico)
	{
		switch (topico) {
		case Constantes.FREQUENCIA_PULSO:
			if (frequenciaPulso == null)
				frequenciaPulso = new FrequenciaPulsoInterpreter(ontModel, triplaRDF, tipoSerializacao);
		
			return frequenciaPulso;
		case Constantes.PRESSAO_SANGUINEA:
			if (pressaoSanguinea == null)
				pressaoSanguinea = new PressaoSanguineaInterpreter(ontModel, triplaRDF, tipoSerializacao);
			
			return pressaoSanguinea;
		default:
			return null;
		}
		
	}
}
