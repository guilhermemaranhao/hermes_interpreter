package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.backup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.utils.Timers;

import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.service.filtro.HermesInterpreterFiltroService;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;

public class HermesInterpreterRulesService extends HermesInterpreterService{

	private static String MLT = "mlt";
	private static String SVT = "svt";
	private static String CT = "ct";
	private static String RT = "rt";
	private static String FCI = "fci";
	private static String FSI = "fsi";
	private static String ALT_ONT = "alt-ont";
	
	private Timers temposInferencia = null;
	
	private Model ontologiaBase = null;
	
	private boolean modeloClassificado = false;
	
	public HermesInterpreterRulesService(Model ontologiaBase, Timers temposInferenciaOntologia)
	{
		this.ontologiaBase = ontologiaBase;
		this.temposInferencia = temposInferenciaOntologia;
		temposInferencia.createTimer(RT);
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
		
		Reasoner motorInferencia = PelletReasonerFactory.theInstance().create();
		PelletOptions.DL_SAFE_RULES = true;
		InfModel infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
		PelletInfGraph pellet = (PelletInfGraph) infModel.getGraph();
		
		temposInferencia.startTimer(SVT);
		ValidityReport relatorios = pellet.validate();
		temposInferencia.stopTimer(SVT);
		Iterator<Report> relatoriosIter = relatorios.getReports();
		while (relatoriosIter.hasNext())
		{
			Report relatorio = relatoriosIter.next();
			texto = "Relatorio de validação: " + relatorio.description;
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
		
		if (relatorios.isValid())
		//if (pellet.isConsistent())
		{
//			temposInferencia.startTimer(CT);
//			pellet.classify();
//			temposInferencia.stopTimer(CT);
			classificar(pellet);
		
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
				
			if (!HermesInterpreterConfigurator.isOntologiaAtualizada(hermesTO.getCaminhoOntologia()))
			{
				temposInferencia.startTimer(ALT_ONT);
				HermesInterpreterFiltroService.refazerFiltros(hermesTO);
				HermesInterpreterConfigurator.atualizarStatusOntologia(hermesTO.getCaminhoOntologia(), true);
				temposInferencia.stopTimer(ALT_ONT);
				
				modeloClassificado = false;
				classificar(pellet);
//				temposInferencia.startTimer(CT);
//				pellet.classify();
//				temposInferencia.stopTimer(CT);
			}
				
			infModel = atualizarContexto(infModel,hermesTO);
		
			//Filtragem pré-inferência
			temposInferencia.startTimer(FSI);
			filtrarContexto(hermesTO, modeloIndividuoAtual, false);
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
			filtrarContexto(hermesTO, infModel, true);
			temposInferencia.stopTimer(FCI);
			
			infModel.remove(modeloIndividuoAtual);
			
			registrarTemposEmArquivo();
		}
		else
		{
			String texto3 = "Erro: Contexto não consistente com schema ontológico.";
			HermesInterpreterLog.recordLog(texto3);
			System.out.println(texto3);
		}
	}
	
	private void filtrarContexto(HermesInterpreterTO hermesTO, Model modeloParaFiltragem, boolean nivelInferencia)
	{
		HermesInterpreterFiltroService filtroService = new HermesInterpreterFiltroService();
		Model novoModeloContextoOriginal = null;
		novoModeloContextoOriginal = modeloIndividuoAtual;
		filtroService.filtrarContexto(hermesTO, novoModeloContextoOriginal, modeloParaFiltragem, nivelInferencia);
	}
	
	private void registrarTemposEmArquivo()
	{
		try{
			String arquivoTextoLog = "./log_tempos_validacao/tempo_inferencia_regras.txt";
			FileWriter arquivoLog = new FileWriter(arquivoTextoLog, true);
			BufferedWriter out = new BufferedWriter(arquivoLog);
			out.write(MLT+","+String.valueOf(temposInferencia.getTimerAverage(MLT)));
			out.newLine();
			out.write(SVT+","+String.valueOf(temposInferencia.getTimerAverage(SVT)));
			out.newLine();
			out.write(ALT_ONT+","+String.valueOf(temposInferencia.getTimerAverage(ALT_ONT)));
			out.newLine();
			out.write(CT+","+String.valueOf(temposInferencia.getTimerAverage(CT)));
			out.newLine();
			out.write(FSI+","+String.valueOf(temposInferencia.getTimerAverage(FSI)));
			out.newLine();
			out.write(RT+","+String.valueOf(temposInferencia.getTimerAverage(RT)));
			out.newLine();
			out.write(FCI+","+String.valueOf(temposInferencia.getTimerAverage(FCI)));
			out.newLine();
			out.newLine();
			out.close();
			arquivoLog.close();
		}
		catch (Exception ex)
		{
			
		}
	}
	
	private void classificar(PelletInfGraph pellet)
	{
		if (!modeloClassificado)
		{
			temposInferencia.startTimer(CT);
			pellet.classify();
			temposInferencia.stopTimer(CT);
			modeloClassificado = true;
		}
	}
}
