package br.ufg.inf.mestrado.hermesinterpreter.service.filtro;

import java.util.Map;

import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Classe POJO utilizada para persistir, em memória, um filtro de um determinado assinante para um tópico específico
 * @author guilhermemaranhao
 *
 */
public class FiltroListener {

	private String nomeTopico;
	private String idListener;
	private Map<String, String> filtroHash;
	private String consultaFiltro;
	private Model contextoFiltrado;
	private TipoInferencia tipoInferencia = null;
	
	public String getNomeTopico() {
		return nomeTopico;
	}
	
	public void setNomeTopico(String nomeTopico) {
		this.nomeTopico = nomeTopico;
	}
	
	public String getIdListener() {
		return idListener;
	}
	
	/**
	 * Identificador do assinante. Atributo é utlizado para publicação de contexto e identifação do assinante em Hermes.
	 */
	public void setIdListener(String idListener) {
		this.idListener = idListener;
	}
	
	public String getConsultaFiltro() {
		return consultaFiltro;
	}
	
	/**
	 * Query gerada pelo HI relativa ao filtro do assinante que é utilizada a cada notificação no referido tópico.
	 */
	public void setConsultaFiltro(String consultaFiltro) {
		this.consultaFiltro = consultaFiltro;
	}
	
	public Model getContextoFiltrado() {
		return contextoFiltrado;
	}
	
	/**
	 * Modelo filtrado a partir da consulta do atributo consultaFiltro no contexto original 
	 */
	public void setContextoFiltrado(Model contextoFiltrado) {
		this.contextoFiltrado = contextoFiltrado;
	}
	
	public TipoInferencia getTipoInferencia() {
		return tipoInferencia;
	}
	
	/**
	 * Tipo de inferência que deve ser utilizado para suportar o filtro do assinante. Essa informação será utilizada para reconfigurar o mecanismo de 
	 * inferência para o tópico caso este seja diferente.
	 */
	public void setTipoInferencia(TipoInferencia tipoInferencia) {
		this.tipoInferencia = tipoInferencia;
	}
	
	public Map<String, String> getFiltroHash() {
		return filtroHash;
	}
	
	/**
	 * Parâmetros do filtro do assinante
	 */
	public void setFiltroHash(Map<String, String> filtroHash) {
		this.filtroHash = filtroHash;
	}
}
