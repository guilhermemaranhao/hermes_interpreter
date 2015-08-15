package br.ufg.inf.mestrado.hermesinterpreter.facade;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.mindswap.pellet.utils.Timer;

import br.ufg.inf.mestrado.hermesinterpreter.service.HermesInterpreterServiceFactory;
import br.ufg.inf.mestrado.hermesinterpreter.service.comunicacao.HermesInterpreterComunicationService;
import br.ufg.inf.mestrado.hermesinterpreter.service.filtro.HermesInterpreterFiltroService;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesInterpreterService;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;

/**
* Classe que implementa o padrão de projeto Facade, oferecendo aos seus clientes uma interface de alta granulosidade para os serviços oferecidos
* pelo componente.
*/

public class HermesInterpreterFacade {
	
	public static Timer tempo;
	//public static Timer tempoInicioHI;
	//public static Timer tempoFinalHI;
	public static FileWriter arquivoLog;
	
	public HermesInterpreterFacade()
	{
		
	}
	
	/**
	* Inicializa o serviço de comunicação do componente e prepara o HI para o recebimento das notificações de contexto, por meio da criação de tópicos localmente, com os quais o componente irá interagir
	* tanto para publicação quanto assinatura. Além disso, também assina os tópicos de contexto sobre os quais irá realizar inferência e filtragem de contexto.
	*/
	public void inicializarInterpreter()
	{
		HermesInterpreterComunicationService comunicacaoService = HermesInterpreterServiceFactory.getComunicacaoService();
		comunicacaoService.criarTopicos();
		comunicacaoService.assinarTopicos();
	}
	
	/**
	* Invoca o serviço de filtro para criação de filtro para o assinante referente a um tópico.
	* @param hermesTO Encapsula os parâmetros dos filtros e o tópico do assinante.
	*/
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

	/**
	* Invoca o serviço de inferência e filtragem de contexto a cada notificação de contexto.
	* @param hermesTO Encapsula o modelo RDF de contexto serializao, o tópico notificado e outras informações relevantes para o desempenho das atividades do HermesInterpreterService.
	*/
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
