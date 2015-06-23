package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.backup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;

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

public class HermesInterpreterOntologyService extends HermesInterpreterService {
	
	private Timers temposInferenciaOntologica;
	
	private static String MLT_O = "mlt_o";
	private static String SVT_O = "svt_o";
	private static String CT_O = "ct_o";
	private static String FSI_O = "fsi_o";
	private static String RT_O = "rt_o";
	private static String FCI_O = "fci_o";
	private static String ALT_ONT = "alt_ont";
	private static String CRIA_INF = "cria_inf";

	//private PelletInfGraph pellet = null;
	private InfModel infModel;
	
	public HermesInterpreterOntologyService(InfModel infModel, PelletInfGraph pellet, Timers temposInferenciaOntologica)
	{
		this.infModel = infModel;
		//this.pellet = pellet;
		this.temposInferenciaOntologica = temposInferenciaOntologica;
	}

	@Override
	public synchronized void inferirSituacao(HermesInterpreterTO hermesTO) 
	{
		String texto4 = " --- Inferência Ontológica ---";
		HermesInterpreterLog.recordLog(texto4);
		System.out.println(texto4);
		
		if (!HermesInterpreterConfigurator.isOntologiaAtualizada(hermesTO.getCaminhoOntologia()))
		{
			temposInferenciaOntologica.startTimer(ALT_ONT);
			HermesInterpreterFiltroService.refazerFiltros(hermesTO);
			HermesInterpreterConfigurator.atualizarStatusOntologia(hermesTO.getCaminhoOntologia(), true);
			temposInferenciaOntologica.stopTimer(ALT_ONT);
			
			temposInferenciaOntologica.startTimer(CT_O);
			infModel.prepare();
			temposInferenciaOntologica.stopTimer(CT_O);
		}
		
		infModel = atualizarContexto(infModel, hermesTO);
		
		temposInferenciaOntologica.startTimer(SVT_O);
		ValidityReport relatorios = infModel.validate();
		temposInferenciaOntologica.stopTimer(SVT_O);
		Iterator<Report> relatoriosIter = relatorios.getReports();
		while (relatoriosIter.hasNext())
		{
			Report relatorio = relatoriosIter.next();
			String texto = "Relatorio: " + relatorio.description;
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	
		if (relatorios.isValid())
		{
			//Filtragem pré-inferência
			temposInferenciaOntologica.startTimer(FSI_O);
			filtrarContexto(hermesTO, modeloIndividuoAtual, false);
			temposInferenciaOntologica.stopTimer(FSI_O);
			
			temposInferenciaOntologica.startTimer(RT_O);
			infModel.add(modeloIndividuoAtual);
			temposInferenciaOntologica.stopTimer(RT_O);
				
			//Filtragem pós-inferência
			temposInferenciaOntologica.startTimer(FCI_O);
			filtrarContexto(hermesTO, infModel, true);
			temposInferenciaOntologica.stopTimer(FCI_O);
			
			infModel.remove(modeloIndividuoAtual);
			
			registrarTemposEmArquivo();
			//Próximo passo: Persistir contexto em Base de Conhecimento TDB.
		}
		else
		{
			String texto = "Erro: Contexto não consistente com schema ontológico.";
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	}
	
	private void filtrarContexto(HermesInterpreterTO hermesTO, Model modeloParaFiltragem, boolean nivelInferencia)
	{
		HermesInterpreterFiltroService filtroService = new HermesInterpreterFiltroService();
		Model novoModeloParaContextoOriginal = null;
		novoModeloParaContextoOriginal = modeloIndividuoAtual;
		filtroService.filtrarContexto(hermesTO, novoModeloParaContextoOriginal, modeloParaFiltragem, nivelInferencia);
	}
	
	private void registrarTemposEmArquivo()
	{
		try{
			String arquivoTextoLog = "./log_tempos_validacao/tempo_inferencia_ontologica.txt";
			FileWriter arquivoLog = new FileWriter(arquivoTextoLog, true);
			BufferedWriter out = new BufferedWriter(arquivoLog);
			out.write(MLT_O+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(MLT_O)));
			out.newLine();
			out.write(ALT_ONT+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(ALT_ONT)));
			out.newLine();
			out.write(CRIA_INF+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(CRIA_INF)));
			out.newLine();
			out.write(CT_O+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(CT_O)));
			out.newLine();
			out.write(FSI_O+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(FSI_O)));
			out.newLine();
			out.write(RT_O+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(RT_O)));
			out.newLine();
			out.write(FCI_O+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(FCI_O)));
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
