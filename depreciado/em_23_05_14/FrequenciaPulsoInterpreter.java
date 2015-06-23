package br.ufg.inf.mestrado.hermesinterpreter.businessobject;

import br.ufg.inf.mestrado.hermebase.BaseObject;
import br.ufg.inf.mestrado.hermebase.model.TriplaRDF;

import com.hp.hpl.jena.ontology.OntModel;


public class FrequenciaPulsoInterpreter extends SinalVitalInterpreter {

	
	public FrequenciaPulsoInterpreter(OntModel ontModel, TriplaRDF triplaRDF, String tipoSerializacao) {
		super(ontModel, triplaRDF, tipoSerializacao);
	}

//	@Override
//	public void registrarSinalVital(int sensorId, float valor, long dataHora) {
//		// TODO Auto-generated method stub
//		
//	}
	
	@Override
	public void inferirInformacoesContexto(BaseObject base) {
		// TODO Auto-generated method stub

	}

}
