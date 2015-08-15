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

/**
 * Classe central na manipula��o dos filtros do componente. Cont�m a l�gica para cria��o e execu��o dos mesmos.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterFiltroService {
	
	private String arquivoLogFreqResp = "./log_tempos_validacao/tempo_freq_resp.txt";
	private String arquivoLogFreqPulso = "./log_tempos_validacao/tempo_freq_pulso.txt";
	private String arquivoLogPressaoSanguinea = "./log_tempos_validacao/tempo_pressao_sanguinea.txt";
	
	/**
	 * Invocado para criar um filtro para um determinado assinante. Ao final do m�todo, o filtro � adicionado em uma lista de filtros a serem
	 * processados para o referente t�pico. Pode ser em lista de filtros que n�o necessitam de etapa de infer�ncia, quando os par�metros s�o informa��es j� existentes no contexto original publicado. Ou 
	 * pode ser em lista de filtros que necessitam de etapa de infer�ncia, quando cont�m par�metros desconhecidos no momento da notifica��o de contexto.
	 * Caso a t�cnica de infer�ncia exigida seja diferente da corrente para o t�pico, essa t�cnica � adicionada no arquivo de configura��o.
	 * @param hermesTO Encapsula os dados necess�rio para cria��o do filtro para um assinante para um determinado t�pico. 
	 */
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
		String texto = "Criando filtro para t�pico " + hermesTO.getNomeTopico() + " idListener: " + hermesTO.getIdListener() + " ......";
		HermesInterpreterLog.recordLog(texto);
		System.out.println(texto);
		interpreterBO.createFilter(hermesTO);
		String texto2 = "Filtro criado para t�pico " + hermesTO.getNomeTopico() + " idListener: " + hermesTO.getIdListener();
		HermesInterpreterLog.recordLog(texto2);
		System.out.println(texto2);
		
		//Ap�s a cria��o do filtro, verifica-se o tipo de infer�ncia exigido para alterar o arquivo topics.json do respectivo t�pico. Se for semelhante � atualmente configurada 
		//ou n�o for exigida infer�ncia, nada � feito. Se for diferente, solicita ao Hermes Configurator que a altere, mantendo a prioridade Regras -> Ontol�gica.
		TipoInferencia tipoInferenciaAtual = HermesInterpreterConfigurator.getTipoInferenciaTopico(hermesTO.getNomeTopico());
		TipoInferencia novoTipoInferencia = hermesTO.getFiltroParaNotificacao().getTipoInferencia();
		if (tipoInferenciaAtual == null || (tipoInferenciaAtual == TipoInferencia.ontologica && novoTipoInferencia == TipoInferencia.regras))
		{
			String[] listaTopicos = new String[1];
			listaTopicos[0] = hermesTO.getNomeTopico();
			HermesInterpreterConfigurator.alterarTipoInferenciaParaTopico(novoTipoInferencia, listaTopicos);
		}
	}
	
	/**
	 * Obt�m os filtros pr�-criados para um determinado t�pico. Diferencia entre filtros que n�o necessitam de etapa de infer�ncia e filtros que necessitam de etapa de infer�ncia.
	 * @param hermesTO Encapsula os dados sobre o filtro que est� sendo requisitado
	 * @param comInferencia especifica se deseja listagem de filtros que necessitam ou n�o de infer�ncia
	 * @return lista de filtros
	 */
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
	
	/**
	 * M�todo central em HI, o qual realiza o processamento dos filtros para um t�pico.
	 * @param hermesTO encapsula os dados de contexto que ser�o utilizados para filtragem.
	 * @param modeloComContextoOriginal modelo de contexto original, publicado pelos Hermes Widgets.
	 * @param modeloParaFiltragem modelo para filtragem, caso este tenha passado por etapa de infer�ncia anteriormente.
	 * @param nivelInferencia Informa ao m�todo se a filtragem deve ser realizada sobre filtros que n�o demandam infer�ncia ou o contr�rio.
	 * @return quantidade de filtros executados.
	 */
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
				String tipoInferenciaRealizada = filtro.getTipoInferencia() == null ? "Sem infer�ncia" : filtro.getTipoInferencia().name();
				if (!modeloConstruido.isEmpty())
				{
					String texto = "**** Filtro ACEITO para paciente " + hermesTO.getEntidade() + ", t�pico " + hermesTO.getNomeTopico() + " �s " + new Date().toString() + " ****\nInfer�ncia: " + tipoInferenciaRealizada + "\n";
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
					String texto = "Filtro n�o aceito para paciente " + hermesTO.getEntidade() + ", t�pico " + hermesTO.getNomeTopico() + " �s " + new Date().toString() + "\nInfer�ncia: " + tipoInferenciaRealizada + "\n";
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
	
	/**
	 * Invocado quando o schema ontol�gico � alterado e assim necessita-se que todos os filtros previamente criados de acordo com o schema antigo sejam refeitos.
	 * @param hermesTO encapsula os atributos para que os filtros sejam refeitos
	 */
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
