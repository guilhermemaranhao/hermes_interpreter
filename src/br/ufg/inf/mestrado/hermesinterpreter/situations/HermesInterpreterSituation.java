package br.ufg.inf.mestrado.hermesinterpreter.situations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import br.ufg.inf.mestrado.hermesinterpreter.service.filtro.FiltroListener;
import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Classe que manipula os arquivos JSON relativos aos filtros de contexto. Detém, portanto, o conhecimento da estrutura do arquivo, bem como das chaves existentes, a saber:<br>
 * <b>construct</b>: tipo de dado textual que armazena o template SPARQL relativo à cláusula CONSTRUCT. Contém os prefixos que poderão ser utilizados e a estrutura inicial da consulta<br>
 * <b>where</b>: tipo de dado textual que descreve um template básico SPARQL para a cláusula WHERE<br>
 * <b>conjuntos_filtros</b>: objeto do tipo array define dois objetos arrays, uma para filtros que não necessitam de cláusula UNION e outra que sim<br>
 * <b>id</b>:<br> Identifica os tipos de parâmetros de filtros suportados pelo JSON do tópico. Foram definidos 2 tipos: parâmetros que não necessitam de cláusula UNION, pois são parâmetros que não
 * são disjuntos entre si, e parâmetros que são disjuntos entre si, para os quais é necessária a inclusão da cláusula UNION entre suas ocorrências na consulta. Por essa chave, é possível ordenar os parâmetros
 * para que fiquem apropriadamente estruturados na query e possam retornar o modelo RDF desejado pelo assinante.
 * <b>clausulas</b>: Objeto array que contém objetos que definem os parâmetros de filtros, as respectivas cláusulas SPARQL e outras chaves que definem o parâmetro<br>
 * <b>filtro</b>: nome do parâmetro propriamente dito, tal como idade limite inferior ou alguma anormalidade de sinal vital. <br>
 * <b>tipo_dado</b>: utilizado pelo HI para informar o tipo de dado correto do parâmetro na consulta SPARQL. Para o parâmetro idade, por exemplo, informa ao HI que é necessário calcular
 * a idade especificada com a data corrente para se filtrar pela data de aniversário do paciente, por exemplo.<br>
 * <b>possui_classe_disjunta_no_filtro</b>: Informação relevante para direcionar o HI na inclusão ou não das cláusulas UNION na consulta.<br>
 * <b>possui_parametro</b>: Se possuir, deve incluir a cláusula FILTER para especificar um valor e um operador.<br>
 * <b>construct</b>: O respectivo template CONSTRUCT para o parâmetro em questão.<br>
 * <b>clausula</b>: O respectivo template WHERE para o parâmetro em questão.<br>
 * <b>tipo_inferencia</b>: O tipo de inferência que deve ser executado pelo HI anteriormente à execução da query para que aquele parâmetro de filtro seja identificado. Orienta o HI na configuração
 * adequada do tipo de inferência para um tópico. Confere flexibilidade ao componente, pois a complexidade do filtro do assinante determinará qual o tipo de inferência a ser instanciado.<br> 
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterSituation{
	
	protected String ontologia = null;
	protected String situationJsonPath = null;
	
	public List<FiltroListener> filtrosListenerSemInferencia = new ArrayList<FiltroListener>();
	public List<FiltroListener> filtrosListenerComInferencia = new ArrayList<FiltroListener>();
	
	/**
	* Cria o filtro do assinante e o inclui na respectiva lista de filtros, que exigem ou não etapa de inferência. 
	* @param hermesTO Encapsula as informações necessárias para criação do filtro, como o hash de parâmetros e valores, nome do tópico e o id do assinante.
	*/
	public void createFilter(HermesInterpreterTO hermesTO)
	{
		FiltroListener novoFiltro = new FiltroListener();
		novoFiltro.setFiltroHash(hermesTO.getFiltroHash());
		montarConsultaSparql(novoFiltro);	
		novoFiltro.setNomeTopico(hermesTO.getNomeTopico());
		novoFiltro.setIdListener(hermesTO.getIdListener());
		hermesTO.setFiltroParaNotificacao(novoFiltro);
			
		//Adicionar a lista correspondente: se é exigida alguma inferência, filtro é adicionado na lista de filtros com inferência. Se não é exigido, é adicionado na lista de filtros
		//que não exigem inferência.
		if (novoFiltro.getTipoInferencia() != null)
		{
			filtrosListenerComInferencia.add(novoFiltro);
		}
		else
		{
			filtrosListenerSemInferencia.add(novoFiltro);	
		}
	}
	
	/**
	* Monta a consulta SPARQL do filtro, a partir do arquivo JSON apontado por situationJsonPath. Para isso, invoca os métodos montarClausulaConstruct() e
	* montarClausulaWhere() que detêm o conhecimento necessário para percurso do arquivo de situações JSON do respectivo tópico. Acessado quando é preciso
	* recriar os filtros quando a ontologia é atualizada.
	*/
	public void montarConsultaSparql(FiltroListener novoFiltro)
	{
		File arquivo = new File(situationJsonPath);
		ParameterizedSparqlString sparqlParametrizado = null;
		try{
			FileInputStream finput = new FileInputStream(arquivo);
			JSONTokener jsonTokener = new JSONTokener(finput);
			JSONObject jsonObject = new JSONObject(jsonTokener);
			novoFiltro.setFiltroHash(ordenarChavesFiltro(jsonObject, novoFiltro.getFiltroHash()));
			
			sparqlParametrizado = new ParameterizedSparqlString(montarClausulaConstruct(jsonObject, novoFiltro.getFiltroHash(), novoFiltro) + jsonObject.getString("where"));
			montarClausulaWhere(sparqlParametrizado, jsonObject, novoFiltro.getFiltroHash());

			String sparql = sparqlParametrizado.toString();
			sparql = sparql.substring(0, sparql.length()-1);
			sparql+=" }";
			sparqlParametrizado = new ParameterizedSparqlString(sparql);
			novoFiltro.setConsultaFiltro(sparqlParametrizado.toString());
			
			String texto = sparqlParametrizado.toString();
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
		catch (FileNotFoundException foex)
		{
			String texto = "Arquivo " + situationJsonPath + " não encontrado.";
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	}
	
	/**
	 * Organiza os parâmetros de filtros de acordo as chaves existentes no JSON. Essas chaves identificam parâmetros que não possuem classes disjuntas entre si (chave = 1),
	 * e portanto não necessitam de cláusula UNION e parâmetros que possuem classes disjuntas entre si (chave = 2), e assim, precisam que sejam incluídas cláusulas UNION 
	 * entre as suas ocorrências. Esse método então ordena os parâmetros para facilitar a construção da consulta SPARQL.
	 * @param jsonObject Objeto JSON da API que se refere ao arquivo JSON do referido tópico.
	 * @param filtroSemantico filtro com os parâmetros
	 * @return filtro com os parâmetros ordenados pelas chaves (IDs)
	 */
	private Map<String, String> ordenarChavesFiltro(JSONObject jsonObject, Map<String, String> filtroSemantico)
	{
		Map<String, String> filtroOrdenado = new LinkedHashMap<>();
		
		JSONArray conjuntosFiltro = jsonObject.getJSONArray("conjuntos_filtro");
		for (int i = 0; i < conjuntosFiltro.length(); i++)
		{
			JSONArray clausulas = conjuntosFiltro.getJSONObject(i).getJSONArray("clausulas");
			for (int j = 0; j < clausulas.length(); j++)
			{
				String filtroClausula = clausulas.getJSONObject(j).getString("filtro");
				Iterator<String> iterFiltro = filtroSemantico.keySet().iterator();
				while (iterFiltro.hasNext())
				{
					String chaveFiltroEscolhido = iterFiltro.next();
					String valorFiltroEscolhido = filtroSemantico.get(chaveFiltroEscolhido);
					if (filtroClausula.equals(chaveFiltroEscolhido))
					{
						filtroOrdenado.put(chaveFiltroEscolhido, valorFiltroEscolhido);
						iterFiltro.remove();
						break;
					}
				}
			}
		}
		
		return filtroOrdenado;
	}
	
	/**
	 * Monta a cláusula CONSTRUCT da consulta SPARQL de acordo com os parâmetros que o assinante deseja filtrar. Esses parâmetros serão especificados na cláusula para serem
	 * retornados no modelo RDF resultante para o usuário, juntamente com os dados originais publicados pelos HWs.
	 * @param jsonObject Objeto JSON da API que se refere ao arquivo JSON do referido tópico.
	 * @param filtroSemantico filtro com os parâmetros
	 * @param novoFiltro objeto do HI que representa um filtro de assinante
	 * @return objeto String com a porção CONSTRUCT da query SPARQL
	 */
	private String montarClausulaConstruct (JSONObject jsonObject, Map<String, String> filtroSemantico, FiltroListener novoFiltro)
	{
		String clausula_construct = jsonObject.getString("construct");
		Iterator<String> iter = filtroSemantico.keySet().iterator();
		
		//Necessário para não permitir que cláusulas já incluídas sejam repetidas.
		List<String> clausulasIncluidas = new ArrayList<>();
		while (iter.hasNext())
		{
			String filtroEscolhido = iter.next();
			JSONArray conjuntosFiltro = jsonObject.getJSONArray("conjuntos_filtro");
			for (int i = 0; i < conjuntosFiltro.length(); i++)
			{
				JSONArray clausulas = conjuntosFiltro.getJSONObject(i).getJSONArray("clausulas");
				for (int j = 0; j < clausulas.length(); j++)
				{
					String filtroClausula = clausulas.getJSONObject(j).getString("filtro");
					if (filtroClausula.equals(filtroEscolhido))
					{
						String clausulaAtual = clausulas.getJSONObject(j).getString("construct");
						if (!clausulasIncluidas.contains(clausulaAtual))
						{
							clausula_construct += clausulaAtual;
							clausulasIncluidas.add(clausulaAtual);
							String tipoInferencia = clausulas.getJSONObject(j).getString("tipo_inferencia");
							if (tipoInferencia.equals(TipoInferencia.regras.name()))
							{
								novoFiltro.setTipoInferencia(TipoInferencia.regras);
							}
							else if (tipoInferencia.equals(TipoInferencia.ontologica.name()))
							{
								//A inferência ontológica só é atribuida ao filtro se a atual for nula. A prioridade é inferência por Regras, por ser mais abrangente.
								if (novoFiltro.getTipoInferencia() == null)
								{
									novoFiltro.setTipoInferencia(TipoInferencia.ontologica);
								}
							}
						}
					}
				}
			}
		}
		return clausula_construct += " }";
	}
	
	/**
	 * Método que constrói a porção WHERE da query SPARQL. Percorre o JSON de situação do tópico, incluindo os respectivos tipos de dados, cláusulas UNION, AND e FILTER quando 
	 * necessárias, além de estruturar a consulta com {}.
	 * @param sparqlParametrizado objeto que permite a inclusão de parâmetros para a execução de uma consulta SPARQL. Como o método se propõe a montar a porção WHERE
	 * da consulta, esses parâmetros serão os valores especificados pelo assinante, como o valor de uma medida de sinal vital, a idade de um paciente ou outro valor 
	 * qualquer de alguma entidade de contexto. 
	 * @param jsonObject Objeto JSON da API que se refere ao arquivo JSON do referido tópico.
	 * @param filtroSemantico filtro com os parâmetros
	 */
	private void montarClausulaWhere(ParameterizedSparqlString sparqlParametrizado, JSONObject jsonObject, Map<String, String> filtroSemantico)
	{
		Iterator<String> iter = filtroSemantico.keySet().iterator();
		boolean possuiMultiplosFiltrosParaAlarmes = false;
		ParameterizedSparqlString clausulasNaoDisjuntas = new ParameterizedSparqlString();
		String clausula_base = jsonObject.getString("clausula_base");
		while (iter.hasNext())
		{
			String filtroEscolhido = iter.next();
			JSONArray conjuntosFiltro = jsonObject.getJSONArray("conjuntos_filtro");
			for (int i = 0; i < conjuntosFiltro.length(); i++)
			{
				JSONArray clausulas = conjuntosFiltro.getJSONObject(i).getJSONArray("clausulas");
				for (int j = 0; j < clausulas.length(); j++)
				{
					boolean possuiClasseDisjuntaNoFiltro = clausulas.getJSONObject(j).getBoolean("possui_classe_disjunta_no_filtro");
					String filtroClausula = clausulas.getJSONObject(j).getString("filtro");
					if (filtroClausula.equals(filtroEscolhido))
					{
						//Se não possui classe disjunta, armazena cláusula em string para, caso exista mais de uma combinação de disjunção no filtro, a cláusula possa ser repetida. 
						if (!possuiClasseDisjuntaNoFiltro)
						{
							clausulasNaoDisjuntas.append(clausulas.getJSONObject(j).getString("clausula"));
						}
						else
						{
							//Se for, incluir o operador 'union', pois trata-se de ocorrência de mais de uma classe disjunta no filtro.
							if (possuiMultiplosFiltrosParaAlarmes)
							{
								sparqlParametrizado.append(" union ");
								
								sparqlParametrizado.append(" { ");
								
								//Repete cláusulas não disjuntas no novo union.
								sparqlParametrizado.append(clausula_base);
								sparqlParametrizado.append(clausulasNaoDisjuntas);
							}
							else
							{
								sparqlParametrizado.append(" { ");
								sparqlParametrizado.append(clausula_base);
							}
							
						}
						sparqlParametrizado.append(clausulas.getJSONObject(j).getString("clausula"));	
						
						if (clausulas.getJSONObject(j).getString("clausula_filtro") != "")
						{
							sparqlParametrizado.append(clausulas.getJSONObject(j).getString("clausula_filtro"));
							if (!possuiClasseDisjuntaNoFiltro)
							{
								clausulasNaoDisjuntas.append(clausulas.getJSONObject(j).getString("clausula_filtro"));
							}
						}
						
						if (clausulas.getJSONObject(j).getBoolean("possui_parametro"))
						{
							String tipoDadoFiltro = clausulas.getJSONObject(j).getString("tipo_dado");
							Literal novoParametro = null;
							if (tipoDadoFiltro.equals("idade"))
							{
								int idadeInt = Integer.parseInt(filtroSemantico.get(filtroEscolhido));
								Calendar hoje = Calendar.getInstance();
								hoje.add(Calendar.YEAR, -idadeInt);
								Date data = hoje.getTime();
								SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
								XSDDatatype xsd = new XSDDatatype("date");
								novoParametro = ResourceFactory.createTypedLiteral(formatter.format(data), xsd);
							}
							else if (tipoDadoFiltro.equals("inteiro"))
							{
								novoParametro = ResourceFactory.createTypedLiteral(Integer.parseInt(filtroSemantico.get(filtroEscolhido)));
							}
							else if (tipoDadoFiltro.equals("float"))
							{
								novoParametro = ResourceFactory.createTypedLiteral(Float.parseFloat(filtroSemantico.get(filtroEscolhido)));
							}
							else if (tipoDadoFiltro.equals("data_hora"))
							{
								XSDDatatype xsdData = new XSDDatatype("dateTime");
								novoParametro = ResourceFactory.createTypedLiteral(filtroSemantico.get(filtroEscolhido), xsdData);
							}
							else
							{
								novoParametro = ResourceFactory.createTypedLiteral(filtroSemantico.get(filtroEscolhido));
							}	
							sparqlParametrizado.setLiteral(filtroEscolhido, novoParametro);
							if (!possuiClasseDisjuntaNoFiltro)
							{
								clausulasNaoDisjuntas.setLiteral(filtroEscolhido, novoParametro);
							}
						}
						if (possuiClasseDisjuntaNoFiltro)
						{
							sparqlParametrizado.append(" } ");
							possuiMultiplosFiltrosParaAlarmes = true;
						}
					}
				}
			}
		}
	}

}
