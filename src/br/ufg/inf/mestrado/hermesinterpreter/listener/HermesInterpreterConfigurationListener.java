package br.ufg.inf.mestrado.hermesinterpreter.listener;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteConfiguracaoListener;
import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

public class HermesInterpreterConfigurationListener implements ComponenteConfiguracaoListener{

	public void handleContext(String tipoInferencia, String[] topicos)
	{
		String texto = "Hermes Interpreter notificado sobre alteração de configuração";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
		HermesInterpreterConfigurator.alterarTipoInferenciaParaTopico(TipoInferencia.valueOf(tipoInferencia), topicos);
	}
}
