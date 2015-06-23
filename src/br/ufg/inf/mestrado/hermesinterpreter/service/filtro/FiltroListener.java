package br.ufg.inf.mestrado.hermesinterpreter.service.filtro;

import java.util.Map;

import br.ufg.inf.mestrado.hermesinterpreter.service.interpretacao.TipoInferencia;

import com.hp.hpl.jena.rdf.model.Model;

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
	public void setIdListener(String idListener) {
		this.idListener = idListener;
	}
	public String getConsultaFiltro() {
		return consultaFiltro;
	}
	public void setConsultaFiltro(String consultaFiltro) {
		this.consultaFiltro = consultaFiltro;
	}
	public Model getContextoFiltrado() {
		return contextoFiltrado;
	}
	public void setContextoFiltrado(Model contextoFiltrado) {
		this.contextoFiltrado = contextoFiltrado;
	}
	public TipoInferencia getTipoInferencia() {
		return tipoInferencia;
	}
	public void setTipoInferencia(TipoInferencia tipoInferencia) {
		this.tipoInferencia = tipoInferencia;
	}
	public Map<String, String> getFiltroHash() {
		return filtroHash;
	}
	public void setFiltroHash(Map<String, String> filtroHash) {
		this.filtroHash = filtroHash;
	}
}
