package br.ufg.inf.mestrado.hermesinterpreter.listener;

import org.mindswap.pellet.utils.Timer;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteNotificacaoListener;
import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

/**
 * Listener que recebe as notifica��es de contexto relativas aos t�picos do tipo "notifa��o". Implementa a interface ComponenteNotificacaoListener, a qual
 * � utilizada pelo Hermes Base para notificar a inst�ncia apropriada do listener dos assinantes.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterNotificationListener implements ComponenteNotificacaoListener{

	HermesInterpreterFacade facade = new HermesInterpreterFacade();
	
	/**
	 * M�todo definido na interface ComponenteNotificacaoListener que � invocado por Hermes Base para notificar os assinantes.
	 * @param idEntidade identifica um indiv�duo refer�ncia do contexto, por exemplo, no cen�rio de monitoramento de sinais vitais, refere-se ao nome do paciente. Objetiva ser uma forma mais direta
	 * de acessar essa informa��o do que ter que percorrer o modelo RDF de contexto notificado
	 * @param nomeTopico nome do t�pico que est� sendo notificado.
	 * @param complementTopico utilizado para especificar um determinado tipo de contexto, por exemplo, guarda o valor de um identificador de assinante
	 * @param caminhoOntologia descreve o caminho da ontologia que descreve o conhecimento do contexto notificado. Essa informa��o � redundante, pois tamb�m se encontra no arquivo de configura��o do componente.
	 * @param contexto stream de bytes que cont�m um modelo RDF serializado com os dados de contexto publicado. � a informa��o mais relevante dentre os argumentos do m�todo.
	 * @param tipoSerializacao tipo de serializa��o do modelo RDF. Utilizado pelo HI para fazer a correta deserializa��o do componente. Pode ser RDF/XML, Turtle ou JSON.
	 */
	public void handleContext (String idEntidade, String nomeTopico, String complementoTopico, String caminhoOntologia, byte[] contexto, String tipoSerializacao)
	{
		//facade.tempoInicioHI = System.currentTimeMillis();
		Timer tempo = new Timer("TOTAL");
		tempo.start();
		facade.tempo = tempo;
		String texto = "T�pico " + nomeTopico + " - " + complementoTopico + " recebido!";
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
