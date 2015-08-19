package br.ufg.inf.mestrado.hermesinterpreter.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.utils.Timers;

import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.service.comunicacao.HermesInterpreterComunicationService;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesInterpreterOntologyService;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesInterpreterRulesService;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesInterpreterService;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesServiceOntologicaPOJO;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesServiceRegrasPOJO;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.util.FileManager;

/**
 * Fábrica de serviços de inferência que instancia o serviço apropriado para o tópico corrente.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterServiceFactory {
	
	private static HermesInterpreterComunicationService hermesComunicacao;
	private static List<HermesServiceRegrasPOJO> listaHermesInferenciaRegras = new ArrayList<>();
	private static List<HermesServiceOntologicaPOJO> listaHermesInferenciaOntologica = new ArrayList<>();
	
	private static String MLT = "mlt";
	private static String SVT = "svt";
	private static String CT = "ct";
	private static String INICIAR_PELLET = "iniciar_pellet";
	
	private static String MLT_O = "mlt_o";
	private static String SVT_O = "svt_o";
	private static String CT_O = "ct_o";
	private static String CRIA_INF = "cria_inf";
	private static String INIC_JENA_OWL_MICRO = "inic_jena_owl_micro";
	
	private static String TEMPO_TOTAL_ONT = "tempo_total_ont";
	private static String TEMPO_TOTAL_REGRAS = "tempo_total_regras";
	
	private static Timers temposInferenciaOntologica = new Timers();
	private static Timers temposInferenciaRegras = new Timers();
	
	private HermesInterpreterServiceFactory()
	{
		
	}
	
	/**
	 * Retorna a instância do serviço de inferência para o tópico, com base na ontologia de contexto. HI contém uma instância de serviço de inferência
	 * por schema ontológico suportado. Baseado no caminhoOntologia e nomeTopico, o serviço é instanciado ou obtido, se já existente.
	 * @param nomeTopico
	 * @param caminhoOntologia
	 * @return
	 */
	public static HermesInterpreterService getInterpretacaoServiceInstance(String nomeTopico, String caminhoOntologia)
	{
		TipoInferencia tipoInferencia = HermesInterpreterConfigurator.getTipoInferenciaTopico(nomeTopico);
		boolean contemServiceParaOntologia = false;
		if (tipoInferencia.equals(TipoInferencia.ontologica))
		{		
			temposInferenciaOntologica.startTimer(TEMPO_TOTAL_ONT);
			for (HermesServiceOntologicaPOJO hermesOntologia : listaHermesInferenciaOntologica)
			{
				if (hermesOntologia.getCaminhoOntologia().equals(caminhoOntologia))
				{
					contemServiceParaOntologia = true;
					return hermesOntologia.getHermesOntology();
				}
			}
			if (!contemServiceParaOntologia)
			{
				//*** Inferência ontológica com Jena ****
//				Timers temposInferenciaOntologica = new Timers();
//				temposInferenciaOntologica.startTimer(MLT_O);
//				Model ontologiaBase = FileManager.get().loadModel(caminhoOntologia);
//				temposInferenciaOntologica.stopTimer(MLT_O);
//				
//				Reasoner motorInferencia = PelletReasonerFactory.theInstance().create();
//				PelletOptions.DL_SAFE_RULES = false;
//				InfModel infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
//				PelletInfGraph pellet = (PelletInfGraph) infModel.getGraph();
//				
//				temposInferenciaOntologica.startTimer(SVT_O);
//				ValidityReport relatorios = pellet.validate();
//				temposInferenciaOntologica.stopTimer(SVT_O);
//				Iterator<Report> relatoriosIter = relatorios.getReports();
//				while (relatoriosIter.hasNext())
//				{
//					Report relatorio = relatoriosIter.next();
//					String texto = "Relatorio de validação: " + relatorio.description;
//					HermesInterpreterLog.recordLog(texto);
//					System.out.println(texto);
//				}
//				
//				if (relatorios.isValid())
//				//if (pellet.isConsistent())
//				{
//					temposInferenciaOntologica.startTimer(CT_O);
//					pellet.classify();
//					temposInferenciaOntologica.stopTimer(CT_O);
//				}
//					
//				HermesServiceOntologicaPOJO novoServiceOntologicaParaOntologia = new HermesServiceOntologicaPOJO();
//				novoServiceOntologicaParaOntologia.setCaminhoOntologia(caminhoOntologia);
//				novoServiceOntologicaParaOntologia.setHermesOntology(new HermesInterpreterOntologyService(infModel, pellet, temposInferenciaOntologica));
//				novoServiceOntologicaParaOntologia.setInfModel(infModel);
//				novoServiceOntologicaParaOntologia.setPellet(pellet);
//				listaHermesInferenciaOntologica.add(novoServiceOntologicaParaOntologia);
//					
//				return novoServiceOntologicaParaOntologia.getHermesOntology();
				temposInferenciaOntologica.startTimer(MLT_O);
				Model ontologiaBase = FileManager.get().loadModel(caminhoOntologia);
				temposInferenciaOntologica.stopTimer(MLT_O);
				
				temposInferenciaOntologica.startTimer(INIC_JENA_OWL_MICRO);
				Reasoner motorInferencia = ReasonerRegistry.getOWLMicroReasoner();
				temposInferenciaOntologica.stopTimer(INIC_JENA_OWL_MICRO);
				
				motorInferencia = motorInferencia.bindSchema(ontologiaBase);
				temposInferenciaOntologica.startTimer(CRIA_INF);
				InfModel infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
				temposInferenciaOntologica.stopTimer(CRIA_INF);
				
				temposInferenciaOntologica.startTimer(SVT_O);
				ValidityReport relatorios = infModel.validate();
				temposInferenciaOntologica.stopTimer(SVT_O);
				Iterator<Report> relatoriosIter = relatorios.getReports();
				while (relatoriosIter.hasNext())
				{
					Report relatorio = relatoriosIter.next();
					String texto = "Relatorio de validação: " + relatorio.description;
					HermesInterpreterLog.recordLog(texto);
					System.out.println(texto);
				}
				
				if (relatorios.isValid())
				//if (pellet.isConsistent())
				{
					temposInferenciaOntologica.startTimer(CT_O);
					infModel.prepare();
					temposInferenciaOntologica.stopTimer(CT_O);
				}
				
				HermesServiceOntologicaPOJO novoServiceOntologicaParaOntologia = new HermesServiceOntologicaPOJO();
				novoServiceOntologicaParaOntologia.setCaminhoOntologia(caminhoOntologia);
				novoServiceOntologicaParaOntologia.setHermesOntology(new HermesInterpreterOntologyService(infModel, null, temposInferenciaOntologica));
				novoServiceOntologicaParaOntologia.setInfModel(infModel);
				listaHermesInferenciaOntologica.add(novoServiceOntologicaParaOntologia);
					
				return novoServiceOntologicaParaOntologia.getHermesOntology();
			}
		}
		else if (tipoInferencia.equals(TipoInferencia.regras))
		{
			temposInferenciaRegras.startTimer(TEMPO_TOTAL_REGRAS);
			for (HermesServiceRegrasPOJO hermesRules : listaHermesInferenciaRegras)
			{
				if (hermesRules.getCaminhoOntologia().equals(caminhoOntologia))
				{
					contemServiceParaOntologia = true;
					return hermesRules.getHermesRule();
				}
			}
			if (!contemServiceParaOntologia)
			{
				temposInferenciaRegras.startTimer(MLT);
				Model ontologiaBase = FileManager.get().loadModel(caminhoOntologia);
				temposInferenciaRegras.stopTimer(MLT);
				
				temposInferenciaRegras.startTimer(INICIAR_PELLET);
				Reasoner motorInferencia = PelletReasonerFactory.theInstance().create();
				temposInferenciaRegras.stopTimer(INICIAR_PELLET);
				PelletOptions.DL_SAFE_RULES = true;

				temposInferenciaRegras.startTimer(CRIA_INF);
				InfModel infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
				temposInferenciaRegras.stopTimer(CRIA_INF);
				PelletInfGraph pellet = (PelletInfGraph) infModel.getGraph();
				
				temposInferenciaRegras.startTimer(SVT);
				ValidityReport relatorios = pellet.validate();
				temposInferenciaRegras.stopTimer(SVT);
				Iterator<Report> relatoriosIter = relatorios.getReports();
				while (relatoriosIter.hasNext())
				{
					Report relatorio = relatoriosIter.next();
					String texto = "Relatorio de validação: " + relatorio.description;
					HermesInterpreterLog.recordLog(texto);
					System.out.println(texto);
				}
				
				if (relatorios.isValid())
				//if (pellet.isConsistent())
				{
					temposInferenciaRegras.startTimer(CT);
					pellet.classify();
					temposInferenciaRegras.stopTimer(CT);
				}
					
				HermesServiceRegrasPOJO novoServiceRulesParaOntologia = new HermesServiceRegrasPOJO();
				novoServiceRulesParaOntologia.setCaminhoOntologia(caminhoOntologia);
				novoServiceRulesParaOntologia.setHermesRule(new HermesInterpreterRulesService(infModel, pellet, temposInferenciaRegras));
				novoServiceRulesParaOntologia.setInfModel(infModel);
				novoServiceRulesParaOntologia.setPellet(pellet);
				listaHermesInferenciaRegras.add(novoServiceRulesParaOntologia);
					
				return novoServiceRulesParaOntologia.getHermesRule();
			}
		}
		
		return null;
	}
	
	/**
	 * Método que retorna a única instância existente do serviço de comunicação.
	 * @return
	 */
	public static HermesInterpreterComunicationService getComunicacaoService()
	{
		if (hermesComunicacao == null)
		{
			hermesComunicacao = new HermesInterpreterComunicationService();
		}
		return hermesComunicacao;
	}
}
