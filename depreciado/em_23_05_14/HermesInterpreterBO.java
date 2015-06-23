package br.ufg.inf.mestrado.hermesinterpreter.businessobject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import br.ufg.inf.mestrado.hermebase.BaseObject;
import br.ufg.inf.mestrado.hermebase.model.TriplaRDF;
import br.ufg.inf.mestrado.hermebase.persistencia.tdb.HermesGerenciadorTDB;
import br.ufg.inf.mestrado.hermebase.utils.Constantes;
import br.ufg.inf.mestrado.hermesinterpreter.negocio.businessobject.sinalvitalInterpreter.SinalVitalFactory;
import br.ufg.inf.mestrado.hermesinterpreter.negocio.businessobject.sinalvitalInterpreter.SinalVitalInterpreter;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class HermesInterpreterBO{
	
	private String topico;
	
	private BaseObject base;
	
	static Properties configuracoes = new Properties();
	
	public static OntModel ontModel;
	
//	public static final String SOURCE_URL = "http://www.semanticweb.org/ontologies/2013/1/Ontology1361391792831.owl";
//
//	public static final String SOURCE_FILE = "./src/ontologia/msvh.owl";
//
//	public static final String NS = SOURCE_URL + "#";

	
	public HermesInterpreterBO(String topico, BaseObject baseObject)
	{
		this.topico = topico;
		this.base = baseObject;
		
		try{
			configuracoes.load(new FileInputStream("configuracoes/config.properties"));
		}
		catch (IOException ioex)
		{
			
		}
		
		ontModel = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );
		//ontModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
		FileManager.get().getLocationMapper().addAltEntry( Constantes.SOURCE_URL, Constantes.SOURCE_FILE );
		Model baseOntology = FileManager.get().loadModel( Constantes.SOURCE_FILE );
        //ontModel.addSubModel( baseOntology );
		ontModel.add(baseOntology);
		//this.searchAndNavigate();
		this.queryData();
		//this.queryTeste();
		//HermesGerenciadorTDB tdb = new HermesGerenciadorTDB();
		//tdb.conectar();
		//tdb.atualizar(ontModel);
		
		//OntModel modelNovoSinalVital = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );
		//ontModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
		//FileManager.get().getLocationMapper().addAltEntry( Constantes.SOURCE_URL, "./src/ontologia/novoSinalVital.owl" );
		//Model baseOntology_ = FileManager.get().loadModel( "./src/ontologia/novoSinalVital.owl" );
        //ontModel.addSubModel( baseOntology );
		//modelNovoSinalVital.add(baseOntology_);
		
		//tdb.atualizar(modelNovoSinalVital);
		
		//StringBuffer queryStr = new StringBuffer();

		// queryStr.append("PREFIX pessoa: <file:src/ontologia/secom/actor#Person> ");

		//queryStr.append("PREFIX ator: <file:src/ontologia/secom/actor#> ");
		//queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		//queryStr.append("PREFIX actor: <file:src/ontologia/secom/actor#> ");
		//queryStr.append("PREFIX actor: <http://linkserver.icmc.usp.br/ckonto/actor#> ");
		//queryStr.append("PREFIX thing: <http://www.w3.org/2002/07/owl#Thing> ");
		//queryStr.append("select ?nome where{ ?x rdf:type actor:Person . ?x actor:hasName ?nome }");
		//queryStr.append("select ?nome where{ actor:Person actor:hasName ?nome }");
		
		//tdb.consultar(queryStr.toString());
		
		//tdb.fecharConexao();
	}
	
	private void searchAndNavigate()

	{
		Resource personMarcelo = ontModel.getResource("http://www.semanticweb.org/ontologies/2013/1/Ontology1361391792831.owl#person01");

		// Resource personMarcelo =
		// model.getResource("file:src/ontologia/secom/actor#Person");

		StmtIterator iterator = personMarcelo.listProperties();

		int count = 1;

		while (iterator.hasNext())

		{
			System.out.println("Property " + count++ + ": " + iterator.nextStatement().getObject());
		}
	}


	private void queryData()
	{
		OntClass classe = ontModel.createClass("file:src/ontologia/secom/actor#Person");
	    ExtendedIterator iter = classe.listInstances();
	    while (iter.hasNext())
	    {
	    	OntResource pessoa = (OntResource)iter.next();
			System.out.println(pessoa.getURI());
	    }

		String queryStr = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX actor: <file://src/ontologia/secom/actor>"
				+ " SELECT ?s ?p"
				+ " WHERE { ?s ?p ?o }";
				//+ " ?uri actor:hasName ?nome }";


		Query query = QueryFactory.create(queryStr);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		
		try {
			ResultSet response = qexec.execSelect();
			
			ResultSetFormatter.out(System.out, response, query);

			while (response.hasNext())
			{
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?hasName");

				if (name != null) {
					System.out.println("Notificar aplicação sensível a contexto! Medida de sinal de vital de " + name + " atualizada.");
				}
				else
				{
					System.out.println("Medida não é de paciente do filtro.");
				}
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		finally {
			qexec.close();
		}
	}
	
	private void queryData2()
	{
		Resource personMarcelo = ontModel.getResource("http://www.semanticweb.org/ontologies/2013/1/Ontology1361391792831.owl#person01");

		StringBuffer queryStr = new StringBuffer();

		// queryStr.append("PREFIX pessoa: <file:src/ontologia/secom/actor#Person> ");

		//queryStr.append("PREFIX ator: <file:src/ontologia/secom/actor#> ");
		queryStr.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ");
		queryStr.append("PREFIX var: <http://www.semanticweb.org/ontologies/2013/1/Ontology1361391792831.owl#> ");
		queryStr.append("PREFIX actor: <file:src/ontologia/secom/actor#> ");
		queryStr.append("PREFIX acti: <file:src/ontologia/secom/activity#> ");
		queryStr.append("PREFIX tEvent: <file:src/ontologia/secom/tEvent#> ");
		queryStr.append("PREFIX time: <file:src/ontologia/secom/time#> ");
		queryStr.append(" SELECT ?hasName ?valorSistolica ?valorDiastolica ?pressure ?unidade ?valorData "
				+ "WHERE {?monitoring acti:hasParticipant ?person "
				+ "?person var:hasRole var:patient "
				+ "?monitoring var:hasMonitoringBloodPressure ?bloodPressure "
				+ "?monitoring tEvent:startDateTime ?date "
				+ "?pressure var:isMeasurementBloodPressure ?bloodPressure "
				+ "?pressure var:valueSystolicBloodPressure ?valorSistolica "
				+ "?pressure var:valueDiastolicBloodPressure ?valorDiastolica "
				+ "?pressure var:unitBloodPressure ?unidade "
				+ "?date time:instantCalendarClockDataType ?valorData "
				+ "FILTER (?valorData >= '2013-02-19T10:00:00'^^xsd:dateTime && ?valorData <= '2013-02-20T12:00:00'^^xsd:dateTime)");
		
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

		try {
			ResultSet response = qexec.execSelect();

			while (response.hasNext())
			{
				QuerySolution soln = response.nextSolution();
				RDFNode pessoa = soln.get("?hasName");

				if (pessoa != null) {
					System.out.println("Notificar aplicação sensível a contexto! Medida de sinal de vital de " + pessoa + " atualizada.");
				}
				else
				{
					System.out.println("Medida não é de paciente do filtro.");
				}
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		finally {
			qexec.close();
		}
		}
	
	private void queryTeste()
	{
		StringBuffer queryStr = new StringBuffer();

		queryStr.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
		queryStr.append("PREFIX actor: <file:src/ontologia/secom/actor#> ");
		//queryStr.append("PREFIX actor: <http://linkserver.icmc.usp.br/ckonto/actor#> ");
		//queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");	
		queryStr.append("select ?c where{ ?c owl:Class actor:Person .}");
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

		try {
			ResultSet response = qexec.execSelect();

			while (response.hasNext())
			{
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?hasName");

				if (name != null) {
					System.out.println("Notificar aplicação sensível a contexto! Medida de sinal de vital de " + name + " atualizada.");
				}
				else
				{
					System.out.println("Medida não é de paciente do filtro.");
				}
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		finally {
			qexec.close();
		}
	}
	
	
//	public static void criarModeloOntologico()
//	{	    
////		ModelMaker modelMaker = ModelFactory.createFileModelMaker(Constantes.SOURCE_FILE);
////		Model modelTemp = modelMaker.createDefaultModel();
////		ontModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, modelTemp);
//		
//		
//	}
	
	public boolean registrarSinalVital(byte[] rdfSerializado)
	{
		//obterInstanciaSinalVital().registrarSinalVital(sensorId, rdfSerializado);
		//StringReader io = new StringReader(rdfSerializado);
		ByteArrayInputStream bais = new ByteArrayInputStream(rdfSerializado);		
		
		try{
//			File arquivoOntologiaAtual = new File("C:\\Documents and Settings\\guilhermemaranhao\\workspace\\monitorSinalVital\\src\\ontologia\\ontologia_atual.owl");
//			FileOutputStream streamOntologia = new FileOutputStream(arquivoOntologiaAtual);
//			ontModel.write(streamOntologia);
//			
			ontModel.read(bais, Constantes.SOURCE_URL, "RDF/XML");
			bais.close();
//			
//			File novoArquivoOntologia = new File("C:\\Documents and Settings\\guilhermemaranhao\\workspace\\monitorSinalVital\\src\\ontologia\\nova_ontologia.owl");
//			FileOutputStream novoStreamOntologia = new FileOutputStream(novoArquivoOntologia);
//			ontModel.write(novoStreamOntologia);
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		if (ontModel.validate().isValid()){ 
		//	System.out.println(ontModel.toString());
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void inferirInformacoesContexto(TriplaRDF triplaRDF, String tipoSerializacao)
	{
		try{
			obterInstanciaSinalVital( triplaRDF, tipoSerializacao).inferirInformacoesContexto(base);
		}
		catch (FileNotFoundException fnfex)
		{
			
		}
	}
	
	private void concluirCiclo()
	{
		ontModel.removeAll();
		ontModel.close();
	}
	
	private SinalVitalInterpreter obterInstanciaSinalVital(TriplaRDF triplaRDF, String tipoSerializacao)
	{
		return SinalVitalFactory.getSinalVital(ontModel, triplaRDF, tipoSerializacao, topico);
	}

}
