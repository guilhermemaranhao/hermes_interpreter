package br.ufg.inf.mestrado.hermesinterpreter.listener;

import java.util.HashMap;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteFilteringListener;
import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

/**
 * Listener que recebe as notifica��es de contexto relativas aos t�picos do tipo "filtragem". Implementa a interface ComponenteFilteringListener, a qual
 * � utilizada pelo Hermes Base para notificar a inst�ncia apropriada do listener dos assinantes.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterFilteringListener implements ComponenteFilteringListener {

	HermesInterpreterFacade facade = new HermesInterpreterFacade();
	
	/**
	 * M�todo definido na interface ComponenteFilteringListener que � invocado por Hermes Base para notificar os assinantes.
	 * @param idListener Identificador do listener na arquitetura Hermes, o qual est� associado a um determinado assinante, para que, no devido momento em que
	 * se respectivo contexto for publicado, ele seja notificado.
	 * @param nomeTopico nome do t�pico em que se est� solicitando a cria��o de filtros.
	 * @param filterJson stream de bytes em que cont�m um objeto do tipo {@link HashMap} serializado, o qual descreve os par�metros de filtros com os respectivos valores, exemplo: idade > 65, :temperatura rdf:type :HighFever
	 * @param consultaFiltro query a ser gerada para o filtro. No momento da solicita��o de cria��o de filtro, possui valor null. 
	 */
	@Override
	public void handleContext(String idListener, String nomeTopico, byte[] filterJson, String consultaFiltro) {
		if (consultaFiltro == null || consultaFiltro.equals(""))
		{
			HermesInterpreterTO hermesTO = new HermesInterpreterTO();
			hermesTO.setNomeTopico(nomeTopico);
			hermesTO.setFiltroByte(filterJson);
			hermesTO.setIdListener(idListener);
			facade.criarFiltro(hermesTO);
		}
		else
		{
			String texto = "T�pico de execu��o de Filtro notificado.";
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	}

}
