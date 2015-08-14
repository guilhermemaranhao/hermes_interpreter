package br.ufg.inf.mestrado.hermesinterpreter.cliente;

import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;

/**
* Classe de inicialização do componente. Por meio do método inicializarInterpreter(), solicita ao HermesInterpreterFacade que realize a criação e assinatura dos tópicos
* com os quais o componente irá interagir durante sua execução.
* @author Guilherme Maranhão
* 
*/

public class InterpretadorCliente{
	
	private static HermesInterpreterFacade facade = new HermesInterpreterFacade();
	
	private InterpretadorCliente(){}
	
	public static void main(String[] args) 
	{		
		facade.inicializarInterpreter();
		
		while (true)
		{
			
		}
	}
}
