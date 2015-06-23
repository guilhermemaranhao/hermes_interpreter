package br.ufg.inf.mestrado.hermesinterpreter.businessobject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import br.ufg.inf.mestrado.hermebase.BaseObject;
import br.ufg.inf.mestrado.hermebase.model.TriplaRDF;
import br.ufg.inf.mestrado.hermebase.persistencia.tdb.HermesGerenciadorTDB;
import br.ufg.inf.mestrado.hermebase.utils.Constantes;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class PressaoSanguineaInterpreter extends SinalVitalInterpreter {
	
	private static final String anormalidadeStage3Hypertension = "Stage3HypertensionAlarm";
	
	private Property isMeasurementBloodPressure = ontModel.createProperty(Constantes.defaultNamespace + "isMeasurementBloodPressure");
	
	private Property isMonitoredBloodPressureOf = ontModel.createProperty(Constantes.defaultNamespace + "isMonitoredBloodPressureOf");
	
	private Property hasParticipant = ontModel.createProperty(Constantes.defaultNamespace + "hasParticipant");
	
	private Property hasRole = ontModel.createProperty(Constantes.defaultNamespace + "hasRole"); 
	
//	private AdministradorMiddleware gen = AdministradorMiddlewareFactory.getInstance(Constantes.DOMINIO_SINAIS_VITAIS, false);
	
	public PressaoSanguineaInterpreter(OntModel model, TriplaRDF triplaRDF, String tipoSerializacao)
	{
		super(model, triplaRDF, tipoSerializacao);
	}

//	@Override
//	public void registrarSinalVital(int sensorId, float valor, long dataHora) {
//		
//		
//	}

	@Override
	public void inferirInformacoesContexto(BaseObject base) throws FileNotFoundException{
		
		//Infere se há estgio 3 de Hipertens‹o.
		HermesGerenciadorTDB tdb = new HermesGerenciadorTDB();
		tdb.conectar();
		
		StringBuffer queryStr = new StringBuffer();

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
		
        tdb.consultar(queryStr.toString());
		
		tdb.fecharConexao();
		
		
		OntClass stage3Hypertension = ontModel.getOntClass( Constantes.defaultNamespace + anormalidadeStage3Hypertension );
       
        ExtendedIterator ocorrenciasDeEstagio3Hipertensao = stage3Hypertension.listInstances();
		while (ocorrenciasDeEstagio3Hipertensao.hasNext())
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
	        ontModel.write(os);
	        
	        try{
	        	os.close();
	        }
	        catch (Exception ex)
	        {
	        	System.out.println(ex.getMessage());
	        }
	        
	        byte[] rdfSerializado = os.toByteArray();
	        
	        triplaRDF.setTipo_sujeito("http://www.semanticweb.org/ontologies/2013/1/Ontology1361391792831.owl#mBloodPressure01");
	        triplaRDF.setTipo_predicado("rdf:type");
	        triplaRDF.setTipo_objeto("http://www.semanticweb.org/ontologies/2013/1/Ontology1361391792831.owl#Stage3HypertensionAlarm");
	        System.out.println("paciente notificado -> " + triplaRDF.getInstancia_objeto());
			base.publicar(Constantes.PRESSAO_SANGUINEA, triplaRDF, rdfSerializado, tipoSerializacao, Constantes.PARTITION_ALARME_PRESSAO_SANGUINEA_HIPERTENSAO_ESTAGIO_3);

			System.out.println("Alarme Notificado pelo Interpreter: " + "http://www.semanticweb.org/ontologies/2013/1/Ontology1361391792831.owl#Stage3HypertensionAlarm");
//			OntResource medHipertensao = (OntResource)ocorrenciasDeEstagio3Hipertensao.next();
//			System.out.println(medHipertensao.getURI());
//			
//			Statement valorMedidaPressaoSanguinea = medHipertensao.getProperty(isMeasurementBloodPressure);
//			System.out.println(valorMedidaPressaoSanguinea.getObject().toString());
//			
//			Statement valorPacienteMedido = valorMedidaPressaoSanguinea.getResource().getProperty(isMonitoredBloodPressureOf);
//			System.out.println(valorPacienteMedido.getObject().toString());
			
			//Publica anormalidade inferida.
			//gen.registrarSinalVital(AdministradorMiddleware.PRESSAO_SANGUINEA, 0, null, Constantes.PARTITION_ALARME_PRESSAO_SANGUINEA_HIPERTENSAO_ESTAGIO_3);
			
//			System.out.println(valorPacienteMedido.getObject().asResource().toString());
//			
//			StmtIterator iterProperties = valorPacienteMedido.getObject().asResource().listProperties();
//			while (iterProperties.hasNext())
//			{
//				Statement stmt = iterProperties.next();
//				System.out.println(stmt.getPredicate().getURI());
//			}
//			
//			RSIterator participantes = valorPacienteMedido.getObject().asResource().getProperty(hasParticipant).listReifiedStatements();
//			while (participantes.hasNext())
//			{ 
//				System.out.println(participantes.next().getStatement().getObject().toString());
//			}
			
//			Resource padronizadorPressaoSanguinea = medHipertensao.getPropertyResourceValue(isMeasurementBloodPressure);
//			Resource monitorizadorPressaoSanguinea = padronizadorPressaoSanguinea.getPropertyResourceValue(isMonitoredBloodPressureOf);
//			Resource pessoaParticipante = monitorizadorPressaoSanguinea.getPropertyResourceValue(hasParticipant);
//			System.out.println(pessoaParticipante.getURI());
//			Resource papel = pessoaParticipante.getPropertyResourceValue(hasRole);
//			System.out.println(papel.getURI());
		}
		
	}
	
	public 

}
