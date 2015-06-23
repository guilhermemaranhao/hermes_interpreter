package br.ufg.inf.mestrado.hermesinterpreter.service.filtro;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.mindswap.pellet.utils.Timer;

import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.service.HermesInterpreterServiceFactory;
import br.ufg.inf.mestrado.hermesinterpreter.service.comunicacao.HermesInterpreterComunicationService;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
import br.ufg.inf.mestrado.hermesinterpreter.situations.HermesInterpreterSituation;
import br.ufg.inf.mestrado.hermesinterpreter.situations.HermesInterpreterSituationFactory;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class HermesInterpreterFiltroService {
	
	private String arquivoLogFreqResp = "./log_tempos_validacao/tempo_freq_resp.txt";
	private String arquivoLogFreqPulso = "./log_tempos_validacao/tempo_freq_pulso.txt";
	private String arquivoLogPressaoSanguinea = "./log_tempos_validacao/tempo_pressao_sanguinea.txt";
	
	public void criarFiltro (HermesInterpreterTO hermesTO)
	{
		HermesInterpreterSituation interpreterBO = HermesInterpreterSituationFactory.getInstance(hermesTO.getCaminhoOntologia(), hermesTO.getNomeTopico());
		
		HashMap<String, String> filtroSemantico = new HashMap<>();
		ByteArrayInputStream bais = new ByteArrayInputStream(hermesTO.getFiltroByte());
		ObjectInputStream retorno = null;
		try{
			retorno = new ObjectInputStream(bais);
			filtroSemantico = (HashMap<String, String>)retorno.readObject();
		}catch (IOException ioex)
		{}
		catch(ClassNotFoundException cex)
		{}
		
		hermesTO.setFiltroHash(filtroSemantico);
		String texto = "Criando filtro para tópico " + hermesTO.getNomeTopico() + " idListener: " + hermesTO.getIdListener() + " ......";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
		interpreterBO.createFilter(hermesTO);
		String texto2 = "Filtro criado para tópico " + hermesTO.getNomeTopico() + " idListener: " + hermesTO.getIdListener();
		HermesInterpreterLog.recordLog(texto2);
		System.out.println(texto2);
		
		//Após a criação do filtro, verifica-se o tipo de inferência exigido para alterar o arquivo topics.json do respectivo tópico. Se for semelhante à atualmente configurada 
		//ou não for exigida inferência, nada é feito. Se for diferente, solicita ao Hermes Configurator que a altere, mantendo a prioridade Regras -> Ontológica.
		TipoInferencia tipoInferenciaAtual = HermesInterpreterConfigurator.getTipoInferenciaTopico(hermesTO.getNomeTopico());
		TipoInferencia novoTipoInferencia = hermesTO.getFiltroParaNotificacao().getTipoInferencia();
		if (tipoInferenciaAtual == null || (tipoInferenciaAtual == TipoInferencia.ontologica && novoTipoInferencia == TipoInferencia.regras))
		{
			String[] listaTopicos = new String[1];
			listaTopicos[0] = hermesTO.getNomeTopico();
			HermesInterpreterConfigurator.alterarTipoInferenciaParaTopico(novoTipoInferencia, listaTopicos);
		}
	}
	
	public List<FiltroListener> obterFiltrosPorTopico(HermesInterpreterTO hermesTO, boolean comInferencia)
	{
		HermesInterpreterSituation situacao = HermesInterpreterSituationFactory.getInstance(hermesTO.getCaminhoOntologia(), hermesTO.getNomeTopico());
		if (comInferencia)
		{
			return situacao.filtrosListenerComInferencia;
		}
		else
		{
			return situacao.filtrosListenerSemInferencia;
		}
	}
	
	public int filtrarContexto(HermesInterpreterTO hermesTO, Model modeloComContextoOriginal, Model modeloParaFiltragem, boolean nivelInferencia)
	{	
		List<FiltroListener> filtrosListenersTopico = this.obterFiltrosPorTopico(hermesTO, nivelInferencia);
		for (FiltroListener filtro : filtrosListenersTopico)
		{
			try{
				Query query = QueryFactory.create(filtro.getConsultaFiltro());
				//System.out.println("Consulta com parametros:\n" + query.toString());
				QueryExecution qexec = QueryExecutionFactory.create(query, modeloParaFiltragem);
				Timer tempoFiltro = new Timer();
				tempoFiltro.start();
				Model modeloConstruido = qexec.execConstruct();
				tempoFiltro.stop();
				try{
					FileWriter arquivoLog = null;
					if (hermesTO.getNomeTopico().equals("VSO_0000005"))
					{
						arquivoLog = new FileWriter(arquivoLogPressaoSanguinea, true);
					}
					else if (hermesTO.getNomeTopico().equals("VSO_0000030"))
					{
						arquivoLog = new FileWriter(arquivoLogFreqPulso, true);
					}
					else if (hermesTO.getNomeTopico().equals("VSO_0000035"))
					{
						arquivoLog = new FileWriter(arquivoLogFreqResp, true);
					}
					if (arquivoLog != null)
					{
						BufferedWriter out = new BufferedWriter(arquivoLog);
						out.write(Long.toString(tempoFiltro.getLast()));
						out.newLine();
						out.close();
						arquivoLog.close();
					}
					tempoFiltro.reset();
				}
				catch (Exception ex)
				{
					
				}
				String tipoInferenciaRealizada = filtro.getTipoInferencia() == null ? "Sem inferência" : filtro.getTipoInferencia().name();
				if (!modeloConstruido.isEmpty())
				{
					String texto = "**** Filtro ACEITO para paciente " + hermesTO.getEntidade() + ", tópico " + hermesTO.getNomeTopico() + " às " + new Date().toString() + " ****\nInferência: " + tipoInferenciaRealizada + "\n";
					HermesInterpreterLog.recordLog(texto);
					System.out.println(texto);
					//Adiciona modelo de contexto original ao modelo inferido.
					modeloComContextoOriginal.add(modeloConstruido);
					filtro.setContextoFiltrado(modeloComContextoOriginal);
					hermesTO.setFiltroParaNotificacao(filtro);
						
					//Notifica contexto.
					HermesInterpreterComunicationService comunicacaoService = HermesInterpreterServiceFactory.getComunicacaoService();
					comunicacaoService.notificarFiltro(hermesTO);
					//modeloComContextoOriginal = null;
				}
				else
				{
					String texto = "Filtro não aceito para paciente " + hermesTO.getEntidade() + ", tópico " + hermesTO.getNomeTopico() + " às " + new Date().toString() + "\nInferência: " + tipoInferenciaRealizada + "\n";
					HermesInterpreterLog.recordLog(texto);
					System.out.println(texto);
				}
				modeloConstruido = null;
			}
			catch (Exception ex){
				HermesInterpreterLog.recordLog("ERRO!!!!! -----> " + ex.getStackTrace());
				ex.printStackTrace();
				//System.exit(0);
			}
		}
		return filtrosListenersTopico.size();
	}
	
	public static void refazerFiltros(HermesInterpreterTO hermesTO)
	{
		String texto = "Refazendo filtros da ontologia " + hermesTO.getCaminhoOntologia() + " .....";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
		List<HermesInterpreterSituation> situationsOntologia = HermesInterpreterSituationFactory.getSituationsPorOntologia(hermesTO.getCaminhoOntologia());
		for (HermesInterpreterSituation hiSituation : situationsOntologia)
		{
			for (FiltroListener filtroSemInferencia: hiSituation.filtrosListenerSemInferencia)
			{
				hiSituation.montarConsultaSparql(filtroSemInferencia);
			}
			for (FiltroListener filtroComInferencia: hiSituation.filtrosListenerComInferencia)
			{
				hiSituation.montarConsultaSparql(filtroComInferencia);
			}
		}
		String texto2 = "Filtros refeitos!";
		HermesInterpreterLog.recordLog(texto2);
		System.out.println(texto2);
	}
}
