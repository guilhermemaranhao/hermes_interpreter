package br.ufg.inf.mestrado.hermesinterpreter.listener;

import org.mindswap.pellet.utils.Timer;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteNotificacaoListener;
import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

/**
 * Listener que recebe as notificações de contexto relativas aos tópicos do tipo "notifação". Implementa a interface ComponenteNotificacaoListener, a qual
 * é utilizada pelo Hermes Base para notificar a instância apropriada do listener dos assinantes.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterNotificationListener implements ComponenteNotificacaoListener{

	HermesInterpreterFacade facade = new HermesInterpreterFacade();
	
	/**
	 * Método definido na interface ComponenteNotificacaoListener que é invocado por Hermes Base para notificar os assinantes.
	 * @param idEntidade identifica um indivíduo referência do contexto, por exemplo, no cenário de monitoramento de sinais vitais, refere-se ao nome do paciente. Objetiva ser uma forma mais direta
	 * de acessar essa informação do que ter que percorrer o modelo RDF de contexto notificado
	 * @param nomeTopico nome do tópico que está sendo notificado.
	 * @param complementTopico utilizado para especificar um determinado tipo de contexto, por exemplo, guarda o valor de um identificador de assinante
	 * @param caminhoOntologia descreve o caminho da ontologia que descreve o conhecimento do contexto notificado. Essa informação é redundante, pois também se encontra no arquivo de configuração do componente.
	 * @param contexto stream de bytes que contém um modelo RDF serializado com os dados de contexto publicado. É a informação mais relevante dentre os argumentos do método.
	 * @param tipoSerializacao tipo de serialização do modelo RDF. Utilizado pelo HI para fazer a correta deserialização do componente. Pode ser RDF/XML, Turtle ou JSON.
	 */
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
