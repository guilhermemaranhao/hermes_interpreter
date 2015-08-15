package br.ufg.inf.mestrado.hermesinterpreter.listener;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteConfiguracaoListener;
import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

/**
 * Listener que recebe as notificações de contexto relativas aos tópicos do tipo "configuração". Implementa a interface ComponenteConfiguracaoListener, a qual
 * é utilizada pelo Hermes Base para notificar a instância apropriada do listener dos assinantes.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterConfigurationListener implements ComponenteConfiguracaoListener{

	/**
	 * Método definido na interface ComponenteConfiguracaoListener que é invocado por Hermes Base para notificar os assinantes.
	 * @param tipoInferencia Novo tipo de inferência que deve ser configurado para a lista de tópicos
	 * @param topicos lista de tópicos que terá seu tipo de inferência alterado para o informado no outro parâmetro
	 */
	public void handleContext(String tipoInferencia, String[] topicos)
	{
		String texto = "Hermes Interpreter notificado sobre alteração de configuração";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
		HermesInterpreterConfigurator.alterarTipoInferenciaParaTopico(TipoInferencia.valueOf(tipoInferencia), topicos);
	}
}
