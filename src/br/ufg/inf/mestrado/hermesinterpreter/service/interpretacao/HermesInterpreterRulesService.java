package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.utils.Timers;

import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.service.filtro.HermesInterpreterFiltroService;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;

public class HermesInterpreterRulesService extends HermesInterpreterService{

	private static String MLT = "mlt";
	private static String SVT = "svt";
	private static String CT = "ct";
	private static String CRIA_INF = "cria_inf";
	private static String RT = "rt";
	private static String FCI = "fci";
	private static String FSI = "fsi";
	private static String REFAZER_FILTROS = "refazer_filtros";
	private static String TEMPO_TOTAL_REGRAS = "tempo_total_regras";
	private static String INICIAR_PELLET = "iniciar_pellet";
	
	private PelletInfGraph pellet = null;
	private Timers temposInferencia = null;
	
	private String arquivoTextoLog = "./log_tempos_validacao/tempo_inferencia_regras.txt";

	
	public HermesInterpreterRulesService(InfModel infModel, PelletInfGraph pellet, Timers temposInferenciaOntologia)
	{
		this.infModel = infModel;
		this.pellet = pellet;
		this.temposInferencia = temposInferenciaOntologia;
		temposInferencia.createTimer(RT);
		temposInferencia.createTimer(REFAZER_FILTROS);
		temposInferencia.createTimer(FSI);
		temposInferencia.createTimer(FCI);
	}
	
//	public HermesInterpreterRulesService(String caminhoOntologia)
//	{
//		Model ontologiaBase = FileManager.get().loadModel(caminhoOntologia);
//		Reasoner motorInferencia = PelletReasonerFactory.theInstance().create();
//		PelletOptions.DL_SAFE_RULES = true;
//		infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
//		pellet = (PelletInfGraph) infModel.getGraph();
//		
//		if (pellet.isConsistent())
//		{
//			pellet.classify();
//		}
//	}
	
	@Override
	public synchronized void inferirSituacao(HermesInterpreterTO hermesTO) 
	{
		String texto = " --- Inferência baseada em Regras ---";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
			
		if (!HermesInterpreterConfigurator.isOntologiaAtualizada(hermesTO.getCaminhoOntologia()))
		{
			temposInferencia.startTimer(REFAZER_FILTROS);
			HermesInterpreterFiltroService.refazerFiltros(hermesTO);
			temposInferencia.stopTimer(REFAZER_FILTROS);
			HermesInterpreterConfigurator.atualizarStatusOntologia(hermesTO.getCaminhoOntologia(), true);
			
			temposInferencia.startTimer(CT);
			pellet.classify();
			temposInferencia.stopTimer(CT);
		}
			
		atualizarContexto(hermesTO);
		
		temposInferencia.startTimer(SVT);
		ValidityReport relatorios = pellet.validate();
		temposInferencia.stopTimer(SVT);
		Iterator<Report> relatoriosIter = relatorios.getReports();
		while (relatoriosIter.hasNext())
		{
			Report relatorio = relatoriosIter.next();
			String texto2 = "Relatorio: " + relatorio.description;
			HermesInterpreterLog.recordLog(texto2);
			System.out.println(texto2);
		}
		
		if (relatorios.isValid())
		//if (pellet.isConsistent())
		{
			//Filtragem pré-inferência
			temposInferencia.startTimer(FSI);
			int quantidadeFiltrosSemInferencia = filtrarContexto(hermesTO, modeloIndividuoAtual, false);
			temposInferencia.stopTimer(FSI);
			
			if (!PelletOptions.DL_SAFE_RULES)
			{
				PelletOptions.DL_SAFE_RULES = true;
			}
			
			temposInferencia.startTimer(RT);
			infModel.add(modeloIndividuoAtual);
			temposInferencia.stopTimer(RT);
			
//			temposInferencia.resetTimer(RT);
//			temposInferencia.startTimer(RT);
//			pellet.realize();
//			temposInferencia.stopTimer(RT);
//			System.out.println(" -- Tempo para realização com Pellet (RT): " + temposInferencia.getTimerTotal(RT));
//			temposInferencia.resetTimer(RT);
			
			//Filtragem pós-inferência
			temposInferencia.startTimer(FCI);
			int quantidadeFiltrosComInferencia = filtrarContexto(hermesTO, infModel, true);
			temposInferencia.stopTimer(FCI);
			
			infModel.remove(modeloIndividuoAtual);
			
			temposInferencia.stopTimer(TEMPO_TOTAL_REGRAS);
			
			registrarTemposEmArquivo(hermesTO, quantidadeFiltrosSemInferencia, quantidadeFiltrosComInferencia);
			
			temposInferencia.resetTimer(MLT);
			temposInferencia.resetTimer(CT);
			temposInferencia.resetTimer(CRIA_INF);
			temposInferencia.resetTimer(REFAZER_FILTROS);
			temposInferencia.resetTimer(INICIAR_PELLET);
		}
		else
		{
			String texto3 = "Erro: Contexto não consistente com schema ontológico.";
			HermesInterpreterLog.recordLog(texto3);
			System.out.println(texto3);
		}
	}
	
	private int filtrarContexto(HermesInterpreterTO hermesTO, Model modeloParaFiltragem, boolean nivelInferencia)
	{
		HermesInterpreterFiltroService filtroService = new HermesInterpreterFiltroService();
		Model novoModeloContextoOriginal = null;
		novoModeloContextoOriginal = modeloIndividuoAtual;
		return filtroService.filtrarContexto(hermesTO, novoModeloContextoOriginal, modeloParaFiltragem, nivelInferencia);
	}
	
	private void registrarTemposEmArquivo(HermesInterpreterTO hermesTO, int quantidadeFiltrosSemInferencia, int quantidadeFiltrosComInferencia)
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dataHoraMedida = format.format(new Date());
		try{
			FileWriter arquivoLog = new FileWriter(arquivoTextoLog, true);
			BufferedWriter out = new BufferedWriter(arquivoLog);
			out.write("data_hora,"+dataHoraMedida);
			out.newLine();
			out.write("topico,"+hermesTO.getNomeTopico());
			out.newLine();
			out.write("paciente,"+hermesTO.getIdEntidade());
			out.newLine();
			out.write(MLT+","+String.valueOf(temposInferencia.getTimerAverage(MLT)));
			out.newLine();
			out.write(INICIAR_PELLET+","+String.valueOf(temposInferencia.getTimerAverage(INICIAR_PELLET)));
			out.newLine();
			out.write(SVT+","+String.valueOf(temposInferencia.getTimer(SVT).getLast()));
			out.newLine();
			out.write(SVT+"_media,"+String.valueOf(temposInferencia.getTimerAverage(SVT)));
			out.newLine();
			out.write(REFAZER_FILTROS+","+String.valueOf(temposInferencia.getTimer(REFAZER_FILTROS).getLast()));
			out.newLine();
			out.write(CRIA_INF+","+String.valueOf(temposInferencia.getTimerAverage(CRIA_INF)));
			out.newLine();
			out.write(CT+","+String.valueOf(temposInferencia.getTimer(CT).getLast()));
			out.newLine();
			out.write(CT+"_media,"+String.valueOf(temposInferencia.getTimerAverage(CT)));
			out.newLine();
			out.write(FSI+","+String.valueOf(temposInferencia.getTimer(FSI).getLast())+","+hermesTO.getNomeTopico()+","+quantidadeFiltrosSemInferencia);
			out.newLine();
			out.write(FSI+"_media,"+String.valueOf(temposInferencia.getTimerAverage(FSI)));
			out.newLine();
			out.write(RT+","+String.valueOf(temposInferencia.getTimer(RT).getLast()));
			out.newLine();
			out.write(RT+"_media,"+String.valueOf(temposInferencia.getTimerAverage(RT)));
			out.newLine();
			out.write(FCI+","+String.valueOf(temposInferencia.getTimer(FCI).getLast())+","+hermesTO.getNomeTopico()+","+quantidadeFiltrosComInferencia);
			out.newLine();
			out.write(FCI+"_media,"+String.valueOf(temposInferencia.getTimerAverage(FCI)));
			out.newLine();
			out.write(TEMPO_TOTAL_REGRAS+","+String.valueOf(temposInferencia.getTimer(TEMPO_TOTAL_REGRAS).getLast()));
			out.newLine();
			out.write(TEMPO_TOTAL_REGRAS+"_media,"+String.valueOf(temposInferencia.getTimerAverage(TEMPO_TOTAL_REGRAS)));
			out.newLine();
			out.newLine();
			out.close();
			arquivoLog.close();
		}
		catch (Exception ex)
		{
			
		}
	}
}
