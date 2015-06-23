package br.ufg.inf.mestrado.hermesinterpreter.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HermesInterpreterLog {

	public static void recordLog(String log) {

		File arquivo = new File("./log_tempos_validacao/log.txt");

		try {

			if (!arquivo.exists()) {
				// cria um arquivo (vazio)
				arquivo.createNewFile();
			}

			// caso seja um diretório, é possível listar seus arquivos e
			// diretórios
			//File[] arquivos = arquivo.listFiles();

			// escreve no arquivo
			FileWriter fw = new FileWriter(arquivo, true);

			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(log);

			bw.newLine();

			bw.close();
			fw.close();

			/*
			 * //faz a leitura do arquivo FileReader fr = new
			 * FileReader(arquivo);
			 * 
			 * BufferedReader br = new BufferedReader(fr);
			 * 
			 * //equanto houver mais linhas while (br.ready()) { //lê a proxima
			 * linha String linha = br.readLine();
			 * 
			 * //faz algo com a linha System.out.println(linha); }
			 * 
			 * br.close(); fr.close();
			 */

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
