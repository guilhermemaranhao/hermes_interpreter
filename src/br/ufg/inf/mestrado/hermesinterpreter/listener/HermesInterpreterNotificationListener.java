package br.ufg.inf.mestrado.hermesinterpreter.listener;

import org.mindswap.pellet.utils.Timer;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteNotificacaoListener;
import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

public class HermesInterpreterNotificationListener implements ComponenteNotificacaoListener{

	HermesInterpreterFacade facade = new HermesInterpreterFacade();
	
	public void handleContext (String idEntidade, String nomeTopico, String complementoTopico, String caminhoOntologia, byte[] contexto, String tipoSerializacao)
	{
		//facade.tempoInicioHI = System.currentTimeMillis();
		Timer tempo = new Timer("TOTAL");
		tempo.start();
		facade.tempo = tempo;
		String texto = "Tópico " + nomeTopico + " - " + complementoTopico + " recebido!";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
		
		HermesInterpreterTO to = new HermesInterpreterTO();
		to.setNomeTopico(nomeTopico);
		to.setEntidade(idEntidade);
		to.setIdEntidade(idEntidade);
		to.setCaminhoOntologia(caminhoOntologia);
		to.setContexto(contexto);
		to.setTipoSerializacao(tipoSerializacao);
		facade.inferirInformacoesContexto(to);
	}
}
