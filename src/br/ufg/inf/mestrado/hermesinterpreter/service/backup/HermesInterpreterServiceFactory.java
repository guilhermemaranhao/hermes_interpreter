//package br.ufg.inf.mestrado.hermesinterpreter.service.backup;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import org.mindswap.pellet.utils.Timers;
//
//import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
//import br.ufg.inf.mestrado.hermesinterpreter.service.comunicacao.HermesInterpreterComunicationService;
//import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesServiceOntologicaPOJO;
//import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.HermesServiceRegrasPOJO;
//import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
//import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.backup.HermesInterpreterOntologyService;
//import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.backup.HermesInterpreterRulesService;
//import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.backup.HermesInterpreterService;
//import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;
//
//import com.hp.hpl.jena.rdf.model.InfModel;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.reasoner.Reasoner;
//import com.hp.hpl.jena.reasoner.ReasonerRegistry;
//import com.hp.hpl.jena.reasoner.ValidityReport;
//import com.hp.hpl.jena.reasoner.ValidityReport.Report;
//import com.hp.hpl.jena.util.FileManager;
//
//public class HermesInterpreterServiceFactory {
//	
//	private static HermesInterpreterComunicationService hermesComunicacao;
//	private static List<HermesServiceRegrasPOJO> listaHermesInferenciaRegras = new ArrayList<>();
//	private static List<HermesServiceOntologicaPOJO> listaHermesInferenciaOntologica = new ArrayList<>();
//	
//	private static String MLT = "mlt";
//	private static String SVT = "svt";
//	private static String CT = "ct";
//	
//	private static String MLT_O = "mlt_o";
//	private static String SVT_O = "svt_o";
//	private static String CT_O = "ct_o";
//	private static String CRIA_INF = "cria_inf";
//	
//	private HermesInterpreterServiceFactory()
//	{
//		
//	}
//	
//	public static HermesInterpreterService getInterpretacaoServiceInstance(String nomeTopico, String caminhoOntologia)
//	{
//		TipoInferencia tipoInferencia = HermesInterpreterConfigurator.getTipoInferenciaTopico(nomeTopico);
//		boolean contemServiceParaOntologia = false;
//		if (tipoInferencia.equals(TipoInferencia.ontologica))
//		{		
//			for (HermesServiceOntologicaPOJO hermesOntologia : listaHermesInferenciaOntologica)
//			{
//				if (hermesOntologia.getCaminhoOntologia().equals(caminhoOntologia))
//				{
//					contemServiceParaOntologia = true;
//					return hermesOntologia.getHermesOntology();
//				}
//			}
//			if (!contemServiceParaOntologia)
//			{
//				//*** Inferência ontológica com Jena ****
////				Timers temposInferenciaOntologica = new Timers();
////				temposInferenciaOntologica.startTimer(MLT_O);
////				Model ontologiaBase = FileManager.get().loadModel(caminhoOntologia);
////				temposInferenciaOntologica.stopTimer(MLT_O);
////				
////				Reasoner motorInferencia = PelletReasonerFactory.theInstance().create();
////				PelletOptions.DL_SAFE_RULES = false;
////				InfModel infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
////				PelletInfGraph pellet = (PelletInfGraph) infModel.getGraph();
////				
////				temposInferenciaOntologica.startTimer(SVT_O);
////				ValidityReport relatorios = pellet.validate();
////				temposInferenciaOntologica.stopTimer(SVT_O);
////				Iterator<Report> relatoriosIter = relatorios.getReports();
////				while (relatoriosIter.hasNext())
////				{
////					Report relatorio = relatoriosIter.next();
////					String texto = "Relatorio de validação: " + relatorio.description;
////					HermesInterpreterLog.recordLog(texto);
////					System.out.println(texto);
////				}
////				
////				if (relatorios.isValid())
////				//if (pellet.isConsistent())
////				{
////					temposInferenciaOntologica.startTimer(CT_O);
////					pellet.classify();
////					temposInferenciaOntologica.stopTimer(CT_O);
////				}
////					
////				HermesServiceOntologicaPOJO novoServiceOntologicaParaOntologia = new HermesServiceOntologicaPOJO();
////				novoServiceOntologicaParaOntologia.setCaminhoOntologia(caminhoOntologia);
////				novoServiceOntologicaParaOntologia.setHermesOntology(new HermesInterpreterOntologyService(infModel, pellet, temposInferenciaOntologica));
////				novoServiceOntologicaParaOntologia.setInfModel(infModel);
////				novoServiceOntologicaParaOntologia.setPellet(pellet);
////				listaHermesInferenciaOntologica.add(novoServiceOntologicaParaOntologia);
////					
////				return novoServiceOntologicaParaOntologia.getHermesOntology();
//				Timers temposInferenciaOntologica = new Timers();
//				temposInferenciaOntologica.startTimer(MLT_O);
//				Model ontologiaBase = FileManager.get().loadModel(caminhoOntologia);
//				temposInferenciaOntologica.stopTimer(MLT_O);
//				Reasoner motorInferencia = ReasonerRegistry.getOWLMicroReasoner();
//				
//				temposInferenciaOntologica.startTimer(CRIA_INF);
//				motorInferencia = motorInferencia.bindSchema(ontologiaBase);
//				InfModel infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
//				temposInferenciaOntologica.stopTimer(CRIA_INF);
//				
//				temposInferenciaOntologica.startTimer(SVT_O);
//				ValidityReport relatorios = infModel.validate();
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
//					infModel.prepare();
//					temposInferenciaOntologica.stopTimer(CT_O);
//				}
//				
//				HermesServiceOntologicaPOJO novoServiceOntologicaParaOntologia = new HermesServiceOntologicaPOJO();
//				novoServiceOntologicaParaOntologia.setCaminhoOntologia(caminhoOntologia);
//				novoServiceOntologicaParaOntologia.setHermesOntology(new HermesInterpreterOntologyService(infModel, null, temposInferenciaOntologica));
//				novoServiceOntologicaParaOntologia.setInfModel(infModel);
//				listaHermesInferenciaOntologica.add(novoServiceOntologicaParaOntologia);
//					
//				return novoServiceOntologicaParaOntologia.getHermesOntology();
//			}
//		}
//		else if (tipoInferencia.equals(TipoInferencia.regras))
//		{
//			for (HermesServiceRegrasPOJO hermesRules : listaHermesInferenciaRegras)
//			{
//				if (hermesRules.getCaminhoOntologia().equals(caminhoOntologia))
//				{
//					contemServiceParaOntologia = true;
//					return hermesRules.getHermesRule();
//				}
//			}
//			if (!contemServiceParaOntologia)
//			{
//				Timers temposInferenciaRegras = new Timers();
//				temposInferenciaRegras.startTimer(MLT);
//				Model ontologiaBase = FileManager.get().loadModel(caminhoOntologia);
//				temposInferenciaRegras.stopTimer(MLT);
//				
////				Reasoner motorInferencia = PelletReasonerFactory.theInstance().create();
////				PelletOptions.DL_SAFE_RULES = true;
////				InfModel infModel = ModelFactory.createInfModel(motorInferencia, ontologiaBase);
////				PelletInfGraph pellet = (PelletInfGraph) infModel.getGraph();
////				
////				temposInferenciaRegras.startTimer(SVT);
////				ValidityReport relatorios = pellet.validate();
////				temposInferenciaRegras.stopTimer(SVT);
////				Iterator<Report> relatoriosIter = relatorios.getReports();
////				while (relatoriosIter.hasNext())
////				{
////					Report relatorio = relatoriosIter.next();
////					String texto = "Relatorio de validação: " + relatorio.description;
////					HermesInterpreterLog.recordLog(texto);
////					System.out.println(texto);
////				}
////				
////				if (relatorios.isValid())
////				//if (pellet.isConsistent())
////				{
////					temposInferenciaRegras.startTimer(CT);
////					pellet.classify();
////					temposInferenciaRegras.stopTimer(CT);
////				}
//					
//				HermesServiceRegrasPOJO novoServiceRulesParaOntologia = new HermesServiceRegrasPOJO();
//				novoServiceRulesParaOntologia.setCaminhoOntologia(caminhoOntologia);
//				novoServiceRulesParaOntologia.setHermesRule(new HermesInterpreterRulesService(ontologiaBase, temposInferenciaRegras));
////				novoServiceRulesParaOntologia.setInfModel(infModel);
////				novoServiceRulesParaOntologia.setPellet(pellet);
//				listaHermesInferenciaRegras.add(novoServiceRulesParaOntologia);
//					
//				return novoServiceRulesParaOntologia.getHermesRule();
//			}
//		}
//		
//		return null;
//	}
//	
//	public static HermesInterpreterComunicationService getComunicacaoService()
//	{
//		if (hermesComunicacao == null)
//		{
//			hermesComunicacao = new HermesInterpreterComunicationService();
//		}
//		return hermesComunicacao;
//	}
//}
