package br.ufg.inf.mestrado.hermesinterpreter.listener;

import java.util.HashMap;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteFilteringListener;
import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

/**
 * Listener que recebe as notificações de contexto relativas aos tópicos do tipo "filtragem". Implementa a interface ComponenteFilteringListener, a qual
 * é utilizada pelo Hermes Base para notificar a instância apropriada do listener dos assinantes.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterFilteringListener implements ComponenteFilteringListener {

	HermesInterpreterFacade facade = new HermesInterpreterFacade();
	
	/**
	 * Método definido na interface ComponenteFilteringListener que é invocado por Hermes Base para notificar os assinantes.
	 * @param idListener Identificador do listener na arquitetura Hermes, o qual está associado a um determinado assinante, para que, no devido momento em que
	 * se respectivo contexto for publicado, ele seja notificado.
	 * @param nomeTopico nome do tópico em que se está solicitando a criação de filtros.
	 * @param filterJson stream de bytes em que contém um objeto do tipo {@link HashMap} serializado, o qual descreve os parâmetros de filtros com os respectivos valores, exemplo: idade > 65, :temperatura rdf:type :HighFever
	 * @param consultaFiltro query a ser gerada para o filtro. No momento da solicitação de criação de filtro, possui valor null. 
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
			String texto = "Tópico de execução de Filtro notificado.";
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	}

}
