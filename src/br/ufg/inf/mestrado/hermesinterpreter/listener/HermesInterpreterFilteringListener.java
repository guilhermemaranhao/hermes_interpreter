package br.ufg.inf.mestrado.hermesinterpreter.listener;

import br.ufg.inf.mestrado.hermesbase.listeners.ComponenteFilteringListener;
import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

public class HermesInterpreterFilteringListener implements ComponenteFilteringListener {

	HermesInterpreterFacade facade = new HermesInterpreterFacade();
	
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
