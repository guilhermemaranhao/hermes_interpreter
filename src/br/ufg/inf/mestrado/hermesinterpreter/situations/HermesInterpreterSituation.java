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
 * Classe que manipula os arquivos JSON relativos aos filtros de contexto. Det�m, portanto, o conhecimento da estrutura do arquivo, bem como das chaves existentes, a saber:<br>
 * <b>construct</b>: tipo de dado textual que armazena o template SPARQL relativo � cl�usula CONSTRUCT. Cont�m os prefixos que poder�o ser utilizados e a estrutura inicial da consulta<br>
 * <b>where</b>: tipo de dado textual que descreve um template b�sico SPARQL para a cl�usula WHERE<br>
 * <b>conjuntos_filtros</b>: objeto do tipo array define dois objetos arrays, uma para filtros que n�o necessitam de cl�usula UNION e outra que sim<br>
 * <b>id</b>:<br> Identifica os tipos de par�metros de filtros suportados pelo JSON do t�pico. Foram definidos 2 tipos: par�metros que n�o necessitam de cl�usula UNION, pois s�o par�metros que n�o
 * s�o disjuntos entre si, e par�metros que s�o disjuntos entre si, para os quais � necess�ria a inclus�o da cl�usula UNION entre suas ocorr�ncias na consulta. Por essa chave, � poss�vel ordenar os par�metros
 * para que fiquem apropriadamente estruturados na query e possam retornar o modelo RDF desejado pelo assinante.
 * <b>clausulas</b>: Objeto array que cont�m objetos que definem os par�metros de filtros, as respectivas cl�usulas SPARQL e outras chaves que definem o par�metro<br>
 * <b>filtro</b>: nome do par�metro propriamente dito, tal como idade limite inferior ou alguma anormalidade de sinal vital. <br>
 * <b>tipo_dado</b>: utilizado pelo HI para informar o tipo de dado correto do par�metro na consulta SPARQL. Para o par�metro idade, por exemplo, informa ao HI que � necess�rio calcular
 * a idade especificada com a data corrente para se filtrar pela data de anivers�rio do paciente, por exemplo.<br>
 * <b>possui_classe_disjunta_no_filtro</b>: Informa��o relevante para direcionar o HI na inclus�o ou n�o das cl�usulas UNION na consulta.<br>
 * <b>possui_parametro</b>: Se possuir, deve incluir a cl�usula FILTER para especificar um valor e um operador.<br>
 * <b>construct</b>: O respectivo template CONSTRUCT para o par�metro em quest�o.<br>
 * <b>clausula</b>: O respectivo template WHERE para o par�metro em quest�o.<br>
 * <b>tipo_inferencia</b>: O tipo de infer�ncia que deve ser executado pelo HI anteriormente � execu��o da query para que aquele par�metro de filtro seja identificado. Orienta o HI na configura��o
 * adequada do tipo de infer�ncia para um t�pico. Confere flexibilidade ao componente, pois a complexidade do filtro do assinante determinar� qual o tipo de infer�ncia a ser instanciado.<br> 
 * @author guilhermemaranhao
 *
 */
public class HermesInterpreterSituation{
	
	protected String ontologia = null;
	protected String situationJsonPath = null;
	
	public List<FiltroListener> filtrosListenerSemInferencia = new ArrayList<FiltroListener>();
	public List<FiltroListener> filtrosListenerComInferencia = new ArrayList<FiltroListener>();
	
	/**
	* Cria o filtro do assinante e o inclui na respectiva lista de filtros, que exigem ou n�o etapa de infer�ncia. 
	* @param hermesTO Encapsula as informa��es necess�rias para cria��o do filtro, como o hash de par�metros e valores, nome do t�pico e o id do assinante.
	*/
	public void createFilter(HermesInterpreterTO hermesTO)
	{
		FiltroListener novoFiltro = new FiltroListener();
		novoFiltro.setFiltroHash(hermesTO.getFiltroHash());
		montarConsultaSparql(novoFiltro);	
		novoFiltro.setNomeTopico(hermesTO.getNomeTopico());
		novoFiltro.setIdListener(hermesTO.getIdListener());
		hermesTO.setFiltroParaNotificacao(novoFiltro);
			
		//Adicionar a lista correspondente: se � exigida alguma infer�ncia, filtro � adicionado na lista de filtros com infer�ncia. Se n�o � exigido, � adicionado na lista de filtros
		//que n�o exigem infer�ncia.
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
	* Monta a consulta SPARQL do filtro, a partir do arquivo JSON apontado por situationJsonPath. Para isso, invoca os m�todos montarClausulaConstruct() e
	* montarClausulaWhere() que det�m o conhecimento necess�rio para percurso do arquivo de situa��es JSON do respectivo t�pico. Acessado quando � preciso
	* recriar os filtros quando a ontologia � atualizada.
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
			String texto = "Arquivo " + situationJsonPath + " n�o encontrado.";
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	}
	
	/**
	 * Organiza os par�metros de filtros de acordo as chaves existentes no JSON. Essas chaves identificam par�metros que n�o possuem classes disjuntas entre si (chave = 1),
	 * e portanto n�o necessitam de cl�usula UNION e par�metros que possuem classes disjuntas entre si (chave = 2), e assim, precisam que sejam inclu�das cl�usulas UNION 
	 * entre as suas ocorr�ncias. Esse m�todo ent�o ordena os par�metros para facilitar a constru��o da consulta SPARQL.
	 * @param jsonObject Objeto JSON da API que se refere ao arquivo JSON do referido t�pico.
	 * @param filtroSemantico filtro com os par�metros
	 * @return filtro com os par�metros ordenados pelas chaves (IDs)
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
	 * Monta a cl�usula CONSTRUCT da consulta SPARQL de acordo com os par�metros que o assinante deseja filtrar. Esses par�metros ser�o especificados na cl�usula para serem
	 * retornados no modelo RDF resultante para o usu�rio, juntamente com os dados originais publicados pelos HWs.
	 * @param jsonObject Objeto JSON da API que se refere ao arquivo JSON do referido t�pico.
	 * @param filtroSemantico filtro com os par�metros
	 * @param novoFiltro objeto do HI que representa um filtro de assinante
	 * @return objeto String com a por��o CONSTRUCT da query SPARQL
	 */
	private String montarClausulaConstruct (JSONObject jsonObject, Map<String, String> filtroSemantico, FiltroListener novoFiltro)
	{
		String clausula_construct = jsonObject.getString("construct");
		Iterator<String> iter = filtroSemantico.keySet().iterator();
		
		//Necess�rio para n�o permitir que cl�usulas j� inclu�das sejam repetidas.
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
								//A infer�ncia ontol�gica s� � atribuida ao filtro se a atual for nula. A prioridade � infer�ncia por Regras, por ser mais abrangente.
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
	 * M�todo que constr�i a por��o WHERE da query SPARQL. Percorre o JSON de situa��o do t�pico, incluindo os respectivos tipos de dados, cl�usulas UNION, AND e FILTER quando 
	 * necess�rias, al�m de estruturar a consulta com {}.
	 * @param sparqlParametrizado objeto que permite a inclus�o de par�metros para a execu��o de uma consulta SPARQL. Como o m�todo se prop�e a montar a por��o WHERE
	 * da consulta, esses par�metros ser�o os valores especificados pelo assinante, como o valor de uma medida de sinal vital, a idade de um paciente ou outro valor 
	 * qualquer de alguma entidade de contexto. 
	 * @param jsonObject Objeto JSON da API que se refere ao arquivo JSON do referido t�pico.
	 * @param filtroSemantico filtro com os par�metros
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
						//Se n�o possui classe disjunta, armazena cl�usula em string para, caso exista mais de uma combina��o de disjun��o no filtro, a cl�usula possa ser repetida. 
						if (!possuiClasseDisjuntaNoFiltro)
						{
							clausulasNaoDisjuntas.append(clausulas.getJSONObject(j).getString("clausula"));
						}
						else
						{
							//Se for, incluir o operador 'union', pois trata-se de ocorr�ncia de mais de uma classe disjunta no filtro.
							if (possuiMultiplosFiltrosParaAlarmes)
							{
								sparqlParametrizado.append(" union ");
								
								sparqlParametrizado.append(" { ");
								
								//Repete cl�usulas n�o disjuntas no novo union.
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
