package br.ufg.inf.mestrado.hermesinterpreter.facade;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.mindswap.pellet.utils.Timer;

import br.ufg.inf.mestrado.hermesinterpreter.service.HermesInterpreterServiceFactory;
import br.ufg.inf.mestrado.hermesinterpreter.service.comunicacao.HermesInterpreterComunicationService;
import br.ufg.inf.mestrado.hermesinterpreter.service.filtro.HermesInterpreterFiltroService;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesInterpreterService;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;

public class HermesInterpreterFacade {
	
	public static Timer tempo;
	//public static Timer tempoInicioHI;
	//public static Timer tempoFinalHI;
	public static FileWriter arquivoLog;
	
	public HermesInterpreterFacade()
	{
		
	}
	
	public void inicializarInterpreter()
	{
		HermesInterpreterComunicationService comunicacaoService = HermesInterpreterServiceFactory.getComunicacaoService();
		comunicacaoService.criarTopicos();
		comunicacaoService.assinarTopicos();
	}
	
	public void criarFiltro(HermesInterpreterTO hermesTO)
	{
		HermesInterpreterFiltroService filtroService = new HermesInterpreterFiltroService();
		Timer tempoFiltro = new Timer("criar_filtro");
		tempoFiltro.start();
		filtroService.criarFiltro(hermesTO);
		tempoFiltro.stop();
		
		try{
			String arquivoTextoLog = "./log_tempos_validacao/tempo_criar_filtro.txt";
			FileWriter arquivoFiltroLog = new FileWriter(arquivoTextoLog, true);
			BufferedWriter out = new BufferedWriter(arquivoFiltroLog);
			out.write(hermesTO.getNomeTopico() + "," + hermesTO.getIdListener() + "," + Long.toString(tempoFiltro.getLast()));
			out.newLine();
			out.close();
			arquivoFiltroLog.close();
		}
		catch (Exception ex)
		{
			
		}
		tempoFiltro.reset();
	}

	public void inferirInformacoesContexto (HermesInterpreterTO hermesTO)
	{
		HermesInterpreterService hermesInterpreterService = HermesInterpreterServiceFactory.getInterpretacaoServiceInstance(hermesTO.getNomeTopico(), hermesTO.getCaminhoOntologia());
		hermesInterpreterService.inferirSituacao(hermesTO);
		//tempoFinalHI = System.currentTimeMillis();
		//long tempoTotal = tempoFinalHI-tempoInicioHI;
		tempo.stop();
		try{
			String arquivoTextoLog = "./log_tempos_validacao/tempo_total.txt";
			arquivoLog = new FileWriter(arquivoTextoLog, true);
			BufferedWriter out = new BufferedWriter(arquivoLog);
			out.write(Long.toString(tempo.getLast()));
			out.newLine();
			out.close();
			arquivoLog.close();
		}
		catch (Exception ex)
		{
			
		}
		tempo.reset();
	}
}
