package br.ufg.inf.mestrado.hermesinterpreter.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

/**
 * Classe responsável por manter os itens de configuração do componente, os quais se encontram no arquivo './settings/topics.json'. Possui dependência com a API JSON.org por meio da
 * qual interage com o arquivo topics.json. O significado de cada item de configuração é: <br>
 * <b>ontologias</b>: ontologias acessadas pelo componente <br>
 * <b>atualizada</b>: boolean que indica se ontologia está atualizada. Baseado nessa informação, o HI define se deve refazer os filtros para se adequarem ao novo schema ontológico <br>
 * <b>nome</b>: caminho onde se localiza o schema ontológico <br>
 * <b>tópicos</b>: coleção dos tópicos com os quais o HI interage <br>
 * <b>ontologia</b>: identifica qual ontologia descreve os conceitos do contexto publicado no respectivo tópico <br>
 * <b>tipo_inferencia</b>: serviço de inferência que deve ser instanciado pelo componente para inferir o contexto notificado no respectivo tópico <br>
 * <b>registrar</b>: nome do tópico em questão que deve ser registrado no DDS local. No DDS, cada tópico é mantido localmente pelos publicadores e assinantes de contexto <br>
 * <b>assinar</b>: para o tópico corrente, identifica se ele deve ser assinado pelo componente. Trata-se de uma listagem pois podem ser realizadas várias assinaturas daquele tópico com complemento de tópico (partition) diferentes <br>
 * <b>tipo</b>: tipo DDL do tópico assinado. Cada tópico no DDS está associado a um DDL específico, o qual descreve os dados suportados pelo tópico <br>
 * <b>situation</b>: Objeto JSON que descreve os arquivos de situação de contexto que descrevem os filtros suportados para o referido tópico <br>
 * @author Guilherme Maranhão
 *
 */
public class HermesInterpreterConfigurator {

	//private static JSONObject jsonObject = null;
	private static File arquivo = new File("./settings/topics.json");
	
	/**
	 * Carrega o objeto JSON especificado na variável 'arquivo'. Esse objeto será reutilizado ao longo da execução do HI.
	 * @return
	 */
	private static JSONObject carregarObjetoJson()
	{
		try{
			FileInputStream finput = new FileInputStream(arquivo);
			JSONTokener jsonTokener = new JSONTokener(finput);
			return new JSONObject(jsonTokener);
		}
		catch (FileNotFoundException foex)
		{
			
		}
		return null;
	}	
	
	/**
	 * Utilizado na inicialização do componente. Lista os tópicos configurados para registro.
	 * @param tipo do tópico associado aos DDLs existentes no DDS
	 * @return lista de tópicos para registro
	 */
	public static ArrayList<String> getTopicosParaRegistroPorTipo(String tipo)
	{
		ArrayList<String> topicosParaNotificacao = new ArrayList<String>();
		JSONArray topicos = carregarObjetoJson().getJSONArray("topicos");
		for (int indiceTopico = 0; indiceTopico < topicos.length(); indiceTopico++)
		{
			String nomeTopico = topicos.getJSONObject(indiceTopico).getString("registrar");
			//String tipoTopico = topicos.getJSONObject(indiceTopico).getString("tipo");
			JSONArray tiposTopico = topicos.getJSONObject(indiceTopico).getJSONArray("tipos");
			for (int indiceTipoTopico = 0; indiceTipoTopico < tiposTopico.length(); indiceTipoTopico++)
			{
				String tipoTopico = tiposTopico.getJSONObject(indiceTipoTopico).getString("tipo");
				if (tipoTopico.equals(tipo))
				{
					topicosParaNotificacao.add(nomeTopico);
				}
			}
		}
		return topicosParaNotificacao;
	}
	
	/**
	 * Utilizado na inicialização do componente. Lista os tópicos configurados para assinatura.
	 * @param tipo do tópico associado aos DDLs existentes no DDS
	 * @return lista de tópicos para assinatura
	 */
	public static HashMap<String, String> getTopicosParaAssinaturaPorTipo(String tipo)
	{
		HashMap<String, String> topicosFiltragemParaAssinatura = new HashMap<>();
		JSONArray topicos = carregarObjetoJson().getJSONArray("topicos");
		for (int indiceTopico = 0; indiceTopico < topicos.length(); indiceTopico++)
		{
			JSONArray topicosParaAssinar = topicos.getJSONObject(indiceTopico).getJSONArray("assinar");
			//String tipoTopico = topicos.getJSONObject(indiceTopico).getString("tipo");
			for (int indice = 0; indice < topicosParaAssinar.length(); indice++)
			{
				if (topicosParaAssinar.getJSONObject(indice).getString("tipo").equals(tipo))
				{
					String nomeTopico = topicosParaAssinar.getJSONObject(indice).getString("nome");
					String complementoTopico = topicosParaAssinar.getJSONObject(indice).getString("complementoTopico");
					topicosFiltragemParaAssinatura.put(nomeTopico, complementoTopico);
				}
			}
		}
		return topicosFiltragemParaAssinatura;
	}
	
	/**
	 * Utilizado para inferência de contexto, retorna o tipo de inferência que deve ser instanciado para o tópico recebido
	 * @param nomeTopico nome do tópico publicado
	 * @return tipo de inferência associado
	 */
	public static TipoInferencia getTipoInferenciaTopico (String nomeTopico)
	{
		File arquivo = new File("./settings/topics.json");
		try{
			FileInputStream finput = new FileInputStream(arquivo);
			JSONTokener jsonTokener = new JSONTokener(finput);
			JSONObject objetoJson = new JSONObject(jsonTokener);
			JSONArray jsonTopicos = objetoJson.getJSONArray("topicos");
		
			for (int indice = 0; indice < jsonTopicos.length(); indice++)
			{
				String jsonTopico = jsonTopicos.getJSONObject(indice).getString("registrar");
				String jsonNomeTopicoCompleto = jsonTopico;
				if (nomeTopico.equals(jsonNomeTopicoCompleto))
				{
					JSONArray tipos_inferencia = jsonTopicos.getJSONObject(indice).getJSONArray("tipo_inferencia");
					return TipoInferencia.valueOf(tipos_inferencia.getJSONObject(0).getString("tipo"));
				}
			}
		}
		catch (FileNotFoundException foex)
		{
			
		}
		
		return null;
	}
	
	/**
	 * Lista o caminho do arquivo que contém os filtros especificados para o tópico
	 * @param topico tópico do qual se deseja obter o respectivo json de situação de contexto para filtragem.
	 * @return caminho do arquivo situation
	 */
	public static String getSituationTopico(String topico)
	{
		JSONArray topicos = carregarObjetoJson().getJSONArray("topicos");
		for (int indiceTopico = 0; indiceTopico < topicos.length(); indiceTopico++)
		{
			JSONArray objetosNegocio = topicos.getJSONObject(indiceTopico).getJSONArray("situation");
			for (int indice = 0; indice < objetosNegocio.length(); indice++)
			{
				String nomeTopico = objetosNegocio.getJSONObject(indice).getString("nomeTopico");
				if (nomeTopico.equals(topico)) 
				{
					String nomeClasse = objetosNegocio.getJSONObject(indice).getString("nomeSituation");
					return nomeClasse;
				}
			}
		}
		
		return null;
	}
	
	 /**
	  * Se o filtro recém-criado demanda por um tipo de inferência diferente do configurado para o(s) tópico(s), esse método deve ser usada para reconfiguração.
	  * @param tipoInferencia novo tipo de inferência
	  * @param topicos lista de tópicos que sofrerão alteração
	  */
	public static void alterarTipoInferenciaParaTopico(TipoInferencia tipoInferencia, String[] topicos)
	{
		JSONObject novoJsonObject = carregarObjetoJson();
		JSONArray jsonTopicos = novoJsonObject.getJSONArray("topicos");
		for (int indice = 0; indice < jsonTopicos.length(); indice++)
		{
			String nomeTopico = jsonTopicos.getJSONObject(indice).getString("registrar");
			for (String topicoInferencia : topicos)
			{
				if (topicoInferencia.equals(nomeTopico))
				{
					JSONArray tipos_inferencia = jsonTopicos.getJSONObject(indice).getJSONArray("tipo_inferencia");
					HashMap<String, String> novoTipoInferencia = new HashMap<>();
					novoTipoInferencia.put("tipo", tipoInferencia.name());
					tipos_inferencia.remove(0);
					tipos_inferencia.put(0, novoTipoInferencia);
					String texto = "Tipo de inferência " + tipoInferencia + " configurada para tópico " + nomeTopico;
					HermesInterpreterLog.recordLog(texto);
					System.out.println(texto);
				}
			}
		}
		atualizarArquivoConfiguracao(novoJsonObject);
	}
	
	/**
	 * obtem schema ontológico que descreve os conceitos do contexto publicado para o tópico em questão
	 * @param nomeTopico tópico do qual se deseja obter o schema ontológico
	 * @return caminho de localização do schema ontológico
	 */
	public static String getOntologiaTopico(String nomeTopico)
	{
		JSONArray topicos = carregarObjetoJson().getJSONArray("topicos");
		for (int indiceTopico = 0; indiceTopico < topicos.length(); indiceTopico++)
		{
			String topicoRegistrar = topicos.getJSONObject(indiceTopico).getString("registrar");
			if (topicoRegistrar.equals(nomeTopico))
			{
				return topicos.getJSONObject(indiceTopico).getString("ontologia");
			}
		}
		return null;
	}

	/**
	 * Verifica se ontologia está atualizada
	 * @param ontologia ontologia a qual se deseja verificar se está atualizada
	 * @return flag que informa sobre atualização da ontologia
	 */
	public static boolean isOntologiaAtualizada(String ontologia)
	{
		JSONArray jsonOntologias = carregarObjetoJson().getJSONArray("ontologias");
		for (int indice = 0; indice < jsonOntologias.length(); indice++)
		{
			JSONObject jsonOntologia = jsonOntologias.getJSONObject(indice);
			String nomeOntologia = jsonOntologia.getString("nome");
			if (nomeOntologia.equals(ontologia))
			{
				return jsonOntologia.getBoolean("atualizada");
			}
		}
		return true;
	}
	
	/**
	 * Após atualização da ontologia, seu status deve ser alterado por esse método no arquivo de configuração
	 * @param ontologia ontologia a qual deseja ser atualizada
	 * @param novoStatus novo status da ontologia
	 */
	public static void atualizarStatusOntologia(String ontologia, boolean novoStatus)
	{
		JSONObject novoJsonObject = carregarObjetoJson();
		JSONArray jsonOntologias = novoJsonObject.getJSONArray("ontologias");
		for (int indice = 0; indice < jsonOntologias.length(); indice++)
		{
			JSONObject jsonOntologia = jsonOntologias.getJSONObject(indice);
			String nomeOntologia = jsonOntologia.getString("nome");
			if (nomeOntologia.equals(ontologia))
			{
				jsonOntologia.put("atualizada", novoStatus);
			}
		}
		atualizarArquivoConfiguracao(novoJsonObject);
	}
	
	/**
	 * Solicitado para gerar outro arquivo de configuração após quaisquer das alterações terem sido efetuadas.
	 * @param novoJsonObject
	 */
	private static void atualizarArquivoConfiguracao(JSONObject novoJsonObject)
	{
		arquivo.delete();
		try{
			File novoArquivoJson = new File("./settings/topics.json");
			FileWriter novoFw = new FileWriter(novoArquivoJson, false);
			novoFw.write(novoJsonObject.toString(1));
			novoFw.close();
			String texto = "Arquivo topicos.json atualizado com conteúdo: " + novoJsonObject.toString();
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
		catch (IOException ioex)
		{
			ioex.printStackTrace();
		}
	}
}
