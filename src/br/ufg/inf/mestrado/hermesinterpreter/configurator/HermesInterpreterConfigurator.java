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
 * qual interage com o arquivo topics.json. Esse arquivo contém registros sobre as ontologias acessadas pelo HI
 * @author Guilherme Maranhão
 *
 */
public class HermesInterpreterConfigurator {

	//private static JSONObject jsonObject = null;
	private static File arquivo = new File("./settings/topics.json");
	
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
