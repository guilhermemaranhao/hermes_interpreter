package br.ufg.inf.mestrado.hermesinterpreter.situations;

import java.util.ArrayList;
import java.util.List;

import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;

/**
 * Fábrica para criação de objetos HermesInterpreterSituation de acordo com o tópico do assinante e a ontologia.
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
	 * Obtém uma instância de HermesInterpreterSituation baseado na ontologia e/ou tópico. As instâncias de HermesInterpreterSituation são Singletons em HI.
	 * @param ontologia modelo ontológico que descreve o domínio do filtro requerido.
	 * @param topico tópico assinado
	 * @return instância de HermesInterpreterSituation
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
	 * Retorna todas as instâncias de HermesInterpreterSituation já iniciadas. Utilizado quando o HI necessita refazer os filtros previamente criados em decorrência
	 * de alteração no modelo ontológico.
	 * @param ontologia modelo ontológico do qual se deseja obter os objetos HermesInterpreterSituation
	 * @return lista de HermesInterpreterSituation já instanciadas.
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
