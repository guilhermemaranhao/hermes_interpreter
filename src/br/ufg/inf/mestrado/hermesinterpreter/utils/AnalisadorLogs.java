package br.ufg.inf.mestrado.hermesinterpreter.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;

public class AnalisadorLogs {
	
	public static void main (String args[])
	{
		File log = new File("./log_tempos_validacao/"+args[0]);
		
		CSVReader reader = null;
		List<String[]> linhas = null;

		try {
			reader = new CSVReader(new FileReader(log));
			linhas = reader.readAll();
			reader.close();
			
			String tipo_filtro_para_analise = args[1];
			
			if (tipo_filtro_para_analise.equals("fsi"))
			{
				boolean topico_possui_fsi = false;
				int total_tempo_fsi = 0;
				//Percorre linhas
				for (String[] linha : linhas)
				{
					if ((linha[0].equals("fsi")) && Integer.parseInt(linha[2]) > 0)
					{
						topico_possui_fsi = true;
					}
					else if (linha[0].equals("tempo_total_regras") && topico_possui_fsi)
					{
						total_tempo_fsi += Integer.parseInt(linha[1]);
						topico_possui_fsi = false;
					}
				}
				System.out.println("Total de tempo de inferência em tópicos com filtro sem inferência: " + total_tempo_fsi);
			}
			else if (tipo_filtro_para_analise.equals("fci"))
			{
				boolean topico_possui_fci = false;
				int total_tempo_fci = 0;
				//Percorre linhas
				for (String[] linha : linhas)
				{
					if ((linha[0].equals("fci")) && Integer.parseInt(linha[2]) > 0)
					{
						topico_possui_fci = true;
					}
					else if (linha[0].equals("tempo_total_regras") && topico_possui_fci)
					{
						total_tempo_fci += Integer.parseInt(linha[1]);
						topico_possui_fci = false;
					}
				}
				System.out.println("Total de tempo de inferência em tópicos com filtro com inferência: " + total_tempo_fci);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
