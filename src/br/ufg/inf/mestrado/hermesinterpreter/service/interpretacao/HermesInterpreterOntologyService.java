package br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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

/**
 * Servi�o de infer�ncia ontol�gica. Estende a classe abstrata {@link HermesInterpreterService} e implementa o m�todo inferirSituacao para o mecanismo de infer�ncia oferecido.
 * @author guilhermemaranhao
 *
 */
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
	private static String TEMPO_TOTAL_ONT = "tempo_total_ont";
	private static String INIC_JENA_OWL_MICRO = "inic_jena_owl_micro";

	//private PelletInfGraph pellet = null;
	
	/**
	 * Construtor que atribui os objetos que ser�o utilizados pelo servi�o para as infer�ncias ontol�gicas de contexto.
	 * @param infModel objeto que armazenar� o contexto inferido
	 * @param pellet objeto que efetua a infer�ncia ontol�gica
	 * @param temposInferenciaOntologica
	 */
	public HermesInterpreterOntologyService(InfModel infModel, PelletInfGraph pellet, Timers temposInferenciaOntologica)
	{
		this.infModel = infModel;
		//this.pellet = pellet;
		this.temposInferenciaOntologica = temposInferenciaOntologica;
	}

	/**
	 * Invocado para infer�ncia de contexto.
	 * @param hermesTO encapsula os dados de contexto necess�rios para infer�ncia.
	 */
	@Override
	public synchronized void inferirSituacao(HermesInterpreterTO hermesTO) 
	{
		String texto4 = " --- Infer�ncia Ontol�gica ---";
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
		
		atualizarContexto(hermesTO);
		
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
			//Filtragem pr�-infer�ncia
			temposInferenciaOntologica.startTimer(FSI_O);
			int quantidadeFiltrosSemInferencia = filtrarContexto(hermesTO, modeloIndividuoAtual, false);
			temposInferenciaOntologica.stopTimer(FSI_O);
			
			temposInferenciaOntologica.startTimer(RT_O);
			infModel.add(modeloIndividuoAtual);
			temposInferenciaOntologica.stopTimer(RT_O);
				
			//Filtragem p�s-infer�ncia
			temposInferenciaOntologica.startTimer(FCI_O);
			int quantidadeFiltrosComInferencia = filtrarContexto(hermesTO, infModel, true);
			temposInferenciaOntologica.stopTimer(FCI_O);
			
			infModel.remove(modeloIndividuoAtual);
			
			temposInferenciaOntologica.stopTimer(TEMPO_TOTAL_ONT);
			
			registrarTemposEmArquivo(hermesTO, quantidadeFiltrosSemInferencia, quantidadeFiltrosComInferencia);

			temposInferenciaOntologica.resetTimer(MLT_O);
			temposInferenciaOntologica.resetTimer(CT_O);
			temposInferenciaOntologica.resetTimer(CRIA_INF);
			temposInferenciaOntologica.resetTimer(INIC_JENA_OWL_MICRO);
			
			//Pr�ximo passo: Persistir contexto em Base de Conhecimento TDB.
		}
		else
		{
			String texto = "Erro: Contexto n�o consistente com schema ontol�gico.";
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	}
	
	/**
	 * Invoca o servi�o de filtragem de contexto
	 * @param hermesTO
	 * @param modeloParaFiltragem
	 * @param nivelInferencia
	 * @return
	 */
	private int filtrarContexto(HermesInterpreterTO hermesTO, Model modeloParaFiltragem, boolean nivelInferencia)
	{
		HermesInterpreterFiltroService filtroService = new HermesInterpreterFiltroService();
		Model novoModeloParaContextoOriginal = null;
		novoModeloParaContextoOriginal = modeloIndividuoAtual;
		return filtroService.filtrarContexto(hermesTO, novoModeloParaContextoOriginal, modeloParaFiltragem, nivelInferencia);
	}
	
	/**
	 * Registra tempo demandado para infer�ncia e filtragem.
	 * @param hermesTO
	 * @param quantidadeFiltrosSemInferencia
	 * @param quantidadeFiltrosComInferencia
	 */
	private void registrarTemposEmArquivo(HermesInterpreterTO hermesTO, int quantidadeFiltrosSemInferencia, int quantidadeFiltrosComInferencia)
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dataHoraMedida = format.format(new Date());
		try{
			String arquivoTextoLog = "./log_tempos_validacao/tempo_inferencia_ontologica.txt";
			FileWriter arquivoLog = new FileWriter(arquivoTextoLog, true);
			BufferedWriter out = new BufferedWriter(arquivoLog);
			out.write("data_hora,"+dataHoraMedida);
			out.newLine();
			out.write("topico,"+hermesTO.getNomeTopico());
			out.newLine();
			out.write("paciente,"+hermesTO.getIdEntidade());
			out.newLine();
			out.write(MLT_O+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(MLT_O)));
			out.newLine();
			out.write(INIC_JENA_OWL_MICRO+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(INIC_JENA_OWL_MICRO)));
			out.newLine();
			out.write(SVT_O+","+String.valueOf(temposInferenciaOntologica.getTimer(SVT_O).getLast()));
			out.newLine();
			out.write(SVT_O+"_media,"+String.valueOf(temposInferenciaOntologica.getTimerAverage(SVT_O)));
			out.newLine();
			out.write(ALT_ONT+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(ALT_ONT)));
			out.newLine();
			out.write(CRIA_INF+","+String.valueOf(temposInferenciaOntologica.getTimerAverage(CRIA_INF)));
			out.newLine();
			out.write(CT_O+","+String.valueOf(temposInferenciaOntologica.getTimer(CT_O).getLast()));
			out.newLine();
			out.write(CT_O+"_media,"+String.valueOf(temposInferenciaOntologica.getTimerAverage(CT_O)));
			out.newLine();
			out.write(FSI_O+","+String.valueOf(temposInferenciaOntologica.getTimer(FSI_O).getLast())+","+quantidadeFiltrosSemInferencia);
			out.newLine();
			out.write(FSI_O+"_media,"+String.valueOf(temposInferenciaOntologica.getTimerAverage(FSI_O)));
			out.newLine();
			out.write(RT_O+","+String.valueOf(temposInferenciaOntologica.getTimer(RT_O).getLast()));
			out.newLine();
			out.write(RT_O+"_media,"+String.valueOf(temposInferenciaOntologica.getTimerAverage(RT_O)));
			out.newLine();
			out.write(FCI_O+","+String.valueOf(temposInferenciaOntologica.getTimer(FCI_O).getLast())+","+quantidadeFiltrosComInferencia);
			out.newLine();
			out.write(FCI_O+"_media,"+String.valueOf(temposInferenciaOntologica.getTimerAverage(FCI_O)));
			out.newLine();
			out.write(TEMPO_TOTAL_ONT+","+String.valueOf(temposInferenciaOntologica.getTimer(TEMPO_TOTAL_ONT).getLast()));
			out.newLine();
			out.write(TEMPO_TOTAL_ONT+"_media,"+String.valueOf(temposInferenciaOntologica.getTimerAverage(TEMPO_TOTAL_ONT)));
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
