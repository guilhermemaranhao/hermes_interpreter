package br.ufg.inf.mestrado.hermesinterpreter.transferObject;

import java.util.List;
import java.util.Map;

import br.ufg.inf.mestrado.hermesinterpreter.service.filtro.FiltroListener;

import com.hp.hpl.jena.rdf.model.Model;


public class HermesInterpreterTO {

	private String nomeTopico;
	private String entidade;
	private List<String> subEntidades;
	private byte[] contexto;
	private Model modelContexto;
	private String idEntidade;
	private String caminhoOntologia;
	private String tipoSerializacao;
	private String tipoInferencia;
	private byte[] filtroByte;
	private Map<String, String> filtroHash;
	private String idListener;
	private FiltroListener filtroParaNotificacao;
	
	public String getNomeTopico() {
		return nomeTopico;
	}
	public void setNomeTopico(String nomeTopico) {
		this.nomeTopico = nomeTopico;
	}
	public String getEntidade() {
		return entidade;
	}
	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}
	public List<String> getSubEntidades() {
		return subEntidades;
	}
	public void setSubEntidades(List<String> subEntidades) {
		this.subEntidades = subEntidades;
	}
	public byte[] getContexto() {
		return contexto;
	}
	public void setContexto(byte[] rdfSerializado) {
		this.contexto = rdfSerializado;
	}
	public String getIdEntidade() {
		return idEntidade;
	}
	public void setIdEntidade(String idEntidade) {
		this.idEntidade = idEntidade;
	}
	public String getCaminhoOntologia() {
		return caminhoOntologia;
	}
	public void setCaminhoOntologia(String caminhoOntologia) {
		this.caminhoOntologia = caminhoOntologia;
	}
	public String getTipoSerializacao() {
		return tipoSerializacao;
	}
	public void setTipoSerializacao(String tipoSerializacao) {
		this.tipoSerializacao = tipoSerializacao;
	}
	public String getTipoInferencia() {
		return tipoInferencia;
	}
	public void setTipoInferencia(String tipoInferencia) {
		this.tipoInferencia = tipoInferencia;
	}
	public byte[] getFiltroByte() {
		return filtroByte;
	}
	public void setFiltroByte(byte[] filtro) {
		this.filtroByte = filtro;
	}
	public String getIdListener() {
		return idListener;
	}
	public void setIdListener(String idListener) {
		this.idListener = idListener;
	}
	public Map<String, String> getFiltroHash() {
		return filtroHash;
	}
	public void setFiltroHash(Map<String, String> filtroHash) {
		this.filtroHash = filtroHash;
	}
	public Model getModelContexto() {
		return modelContexto;
	}
	public void setModelContexto(Model modelContexto) {
		this.modelContexto = modelContexto;
	}
	public FiltroListener getFiltroParaNotificacao() {
		return filtroParaNotificacao;
	}
	public void setFiltroParaNotificacao(FiltroListener filtroParaNotificacao) {
		this.filtroParaNotificacao = filtroParaNotificacao;
	}
	
	
	
}
