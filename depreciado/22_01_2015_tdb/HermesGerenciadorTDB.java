package br.ufg.inf.mestrado.hermebase.persistencia.tdb;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class HermesGerenciadorTDB {
	
	Dataset dataset = null;

	public HermesGerenciadorTDB()
	{
		
	}
	
	public void conectar()
	{
		String diretorio = "HermesDatabases/Dataset1";
		dataset = TDBFactory.createDataset(diretorio);
//		String assemblerFile = "Store/tdb-assembler.ttl";
//		dataset = TDBFactory.assembleDataset(assemblerFile);
	}
	
	public void atualizar(OntModel model)
	{
		try{
			dataset.begin(ReadWrite.WRITE);
			Model modelTDB = dataset.getDefaultModel();
			modelTDB.add(model);
			dataset.commit();
		}
		finally
		{
			dataset.end();
		}
	}
	
	public void consultar(String query)
	{
		dataset.begin(ReadWrite.READ);
		try{
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			ResultSet rs = qexec.execSelect();
			try{
				ResultSetFormatter.out(rs);
			}finally{qexec.close();}
			//Model model = dataset.getDefaultModel();
		}
		finally
		{
			dataset.end();
		}
	}
	
	public void fecharConexao()
	{
		dataset.close();
	}
}
