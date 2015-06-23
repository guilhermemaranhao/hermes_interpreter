package br.ufg.inf.mestrado.hermesinterpreter.businessobject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.semanticweb.owlapi.io.OWLObjectRenderer;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;
import br.ufg.inf.mestrado.hermebase.BaseObject;
import br.ufg.inf.mestrado.hermebase.model.TriplaRDF;

import com.hp.hpl.jena.ontology.OntModel;

public abstract class SinalVitalInterpreter {
	
	public OntModel ontModel;
	
	public TriplaRDF triplaRDF;
	
	public String tipoSerializacao;
	
//	public static String NS;
	
	static Properties configuracoes = new Properties();
	
	public static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer(); 
	
	public SinalVitalInterpreter(OntModel model, TriplaRDF triplaRDF, String tipoSerializacao)
	{
		this.ontModel = model;
		this.triplaRDF = triplaRDF;
		this.tipoSerializacao = tipoSerializacao;
	//	this.NS = NS;
		
		try{
			configuracoes.load(new FileInputStream("configuracoes/config.properties"));
		}
		catch (IOException ioex)
		{
			
		}
	}
	
//	public abstract void registrarSinalVital(int sensorId, float valor, long dataHora);
	
	public abstract void inferirInformacoesContexto(BaseObject base) throws FileNotFoundException;

}
