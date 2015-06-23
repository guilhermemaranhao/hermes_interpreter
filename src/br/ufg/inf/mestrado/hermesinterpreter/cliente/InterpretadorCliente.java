package br.ufg.inf.mestrado.hermesinterpreter.cliente;

import br.ufg.inf.mestrado.hermesinterpreter.facade.HermesInterpreterFacade;

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
