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

public class HermesInterpreterSituation{
	
	protected String ontologia = null;
	protected String situationJsonPath = null;
	
	public List<FiltroListener> filtrosListenerSemInferencia = new ArrayList<FiltroListener>();
	public List<FiltroListener> filtrosListenerComInferencia = new ArrayList<FiltroListener>();
	
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
	
	//Chaves de ID=1 devem anteceder as chaves de ID=2, para a correta montagem das cláusulas.
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
