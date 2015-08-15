package br.ufg.inf.mestrado.hermesinterpreter.listener;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteConfiguracaoListener;
import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

/**
 * Listener que recebe as notifica��es de contexto relativas aos t�picos do tipo "configura��o". Implementa a interface ComponenteConfiguracaoListener, a qual
 * � utilizada pelo Hermes Base para notificar a inst�ncia apropriada do listener dos assinantes.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterConfigurationListener implements ComponenteConfiguracaoListener{

	/**
	 * M�todo definido na interface ComponenteConfiguracaoListener que � invocado por Hermes Base para notificar os assinantes.
	 * @param tipoInferencia Novo tipo de infer�ncia que deve ser configurado para a lista de t�picos
	 * @param topicos lista de t�picos que ter� seu tipo de infer�ncia alterado para o informado no outro par�metro
	 */
	public void handleContext(String tipoInferencia, String[] topicos)
	{
		String texto = "Hermes Interpreter notificado sobre altera��o de configura��o";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
		HermesInterpreterConfigurator.alterarTipoInferenciaParaTopico(TipoInferencia.valueOf(tipoInferencia), topicos);
	}
}
