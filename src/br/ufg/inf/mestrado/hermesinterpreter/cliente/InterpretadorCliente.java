package br.ufg.inf.mestrado.hermesinterpreter.cliente;

import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;

/**
* Classe de inicializa��o do componente. Por meio do m�todo inicializarInterpreter(), solicita ao HermesInterpreterFacade que realize a cria��o e assinatura dos t�picos
* com os quais o componente ir� interagir durante sua execu��o.
* @author Guilherme Maranh�o
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
