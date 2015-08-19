package br.ufg.inf.mestrado.hermesinterpreter.situations;

import java.util.ArrayList;
import java.util.List;

import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;

/**
 * F�brica para cria��o de objetos HermesInterpreterSituation de acordo com o t�pico do assinante e a ontologia.
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterSituationFactory {
	
//	private static JSONObject jsonTopicSettings;
//	
//	static
//	{
//		File arquivo = new File("./settings/topics.json");
//		try{
//			FileInputStream finputs = new FileInputStream(arquivo);
//			JSONTokener jsonTocken = new JSONTokener(finputs);
//			jsonTopicSettings = new JSONObject(jsonTocken);
//		}
//		catch (FileNotFoundException fnoex)
//		{
//		}
//	}
	
	private HermesInterpreterSituationFactory(){}
	
	private static List<HermesInterpreterSituation> situations = new ArrayList<>();
	
	/**
	 * Obt�m uma inst�ncia de HermesInterpreterSituation baseado na ontologia e/ou t�pico. As inst�ncias de HermesInterpreterSituation s�o Singletons em HI.
	 * @param ontologia modelo ontol�gico que descreve o dom�nio do filtro requerido.
	 * @param topico t�pico assinado
	 * @return inst�ncia de HermesInterpreterSituation
	 */
	public static HermesInterpreterSituation getInstance(String ontologia, String topico)
	{
		String nomeJson = HermesInterpreterConfigurator.getSituationTopico(topico);
		for (HermesInterpreterSituation hiSituation : situations)
		{
			if (hiSituation.situationJsonPath.equals(nomeJson))
			{
				return hiSituation;
			}
		}
		HermesInterpreterSituation novoSituation = new HermesInterpreterSituation();
		if (ontologia == null)
		{
			ontologia = HermesInterpreterConfigurator.getOntologiaTopico(topico);
		}
		novoSituation.ontologia = ontologia;
		novoSituation.situationJsonPath = nomeJson;
		situations.add(novoSituation);
		return novoSituation;
		
	}
	
	/**
	 * Retorna todas as inst�ncias de HermesInterpreterSituation j� iniciadas. Utilizado quando o HI necessita refazer os filtros previamente criados em decorr�ncia
	 * de altera��o no modelo ontol�gico.
	 * @param ontologia modelo ontol�gico do qual se deseja obter os objetos HermesInterpreterSituation
	 * @return lista de HermesInterpreterSituation j� instanciadas.
	 */
	public static List<HermesInterpreterSituation> getSituationsPorOntologia(String ontologia)
	{
		List<HermesInterpreterSituation> situationsOntologia = new ArrayList<>();
		for (HermesInterpreterSituation hiSituation : situations)
		{
			if (hiSituation.ontologia.equals(ontologia))
			{
				situationsOntologia.add(hiSituation);
			}
		}
		return situationsOntologia;
	}
	
//	public static HermesInterpreterSituation getInstance(String topico)
//	{
//		String nomeClasse = HermesInterpreterConfigurator.getSituationTopico(topico);
//		for (HermesInterpreterSituation hiSituation : situations)
//		{
//			if (hiSituation.getClass().getName().equals(nomeClasse))
//			{
//				return hiSituation;
//			}
//		}
//		try{
//			Class<?> classeBO = Class.forName(nomeClasse);
//			HermesInterpreterSituation topicoBO = (HermesInterpreterSituation)classeBO.newInstance();
//			situations.add(topicoBO);
//			return topicoBO;
//		}
//		catch(ClassNotFoundException cnfex)
//		{
//			
//		}
//		catch(IllegalAccessException iaex)
//		{
//			
//		}
//		catch (InstantiationException iex)
//		{
//			
//		}
//		return null;
//	}
}
