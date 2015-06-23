package br.ufg.inf.mestrado.hermesinterpreter.service.comunicacao;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufg.inf.mestrado.hermesbase.HermesBaseManager;
import br.ufg.inf.mestrado.hermesinterpreter.configurator.HermesInterpreterConfigurator;
import br.ufg.inf.mestrado.hermesinterpreter.listener.HermesInterpreterConfigurationListener;
import br.ufg.inf.mestrado.hermesinterpreter.listener.HermesInterpreterFilteringListener;
import br.ufg.inf.mestrado.hermesinterpreter.listener.HermesInterpreterNotificationListener;
import br.ufg.inf.mestrado.hermesinterpreter.service.filtro.FiltroListener;
import br.ufg.inf.mestrado.hermesinterpreter.transferObject.HermesInterpreterTO;
import br.ufg.inf.mestrado.hermesinterpreter.utils.HermesInterpreterLog;


public class HermesInterpreterComunicationService{
	
	private HermesBaseManager hermesBaseManager = new HermesBaseManager();

	public void criarTopicos()
	{
		List<String> topicosParaNotificacao = HermesInterpreterConfigurator.getTopicosParaRegistroPorTipo("notificacao");
		for (int i = 0; i < topicosParaNotificacao.size(); i++)
		{
			String nomeTopicoNotificacao = topicosParaNotificacao.get(i);
			hermesBaseManager.createNotificationTopic(nomeTopicoNotificacao);
			String texto = "Tópico criado: " + nomeTopicoNotificacao;
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
		List<String> topicosParaConfiguracao = HermesInterpreterConfigurator.getTopicosParaRegistroPorTipo("configuracao");
		for (int i = 0; i < topicosParaConfiguracao.size(); i++)
		{
			String nomeTopicoConfiguracao = topicosParaConfiguracao.get(i);
			hermesBaseManager.createConfigurationTopic(nomeTopicoConfiguracao);
			String texto = "Tópico criado: " + nomeTopicoConfiguracao;
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}
	}
	
	public void assinarTopicos()
	{
		Map<String, String> topicosNotificacaoParaAssinatura = HermesInterpreterConfigurator.getTopicosParaAssinaturaPorTipo("notificacao");
		Set<String> listaChavesNotificacao = topicosNotificacaoParaAssinatura.keySet();
		Iterator<String> iter = listaChavesNotificacao.iterator();
		while (iter.hasNext())
		{
			String nomeTopico = iter.next();
			String complementoTopico = topicosNotificacaoParaAssinatura.get(nomeTopico);
			hermesBaseManager.subscribeNotificationTopic(nomeTopico, complementoTopico, new HermesInterpreterNotificationListener(), null);
			String texto = "Tópico assinado: " + nomeTopico + " - " + complementoTopico;
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}	
		
		Map<String, String> topicosConfiguracaoParaAssinatura = HermesInterpreterConfigurator.getTopicosParaAssinaturaPorTipo("configuracao");
		Set<String> listaChaves = topicosConfiguracaoParaAssinatura.keySet();
		Iterator<String> iterConf = listaChaves.iterator();
		while (iterConf.hasNext())
		{
			String nomeTopico = iterConf.next();
			String complementoTopico = topicosConfiguracaoParaAssinatura.get(nomeTopico);
			hermesBaseManager.subscribeConfigurationTopic(nomeTopico, complementoTopico, new HermesInterpreterConfigurationListener());
			String texto = "Tópico assinado: " + nomeTopico + " - " + complementoTopico;
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}	
		
		Map<String, String> topicosFiltragemParaAssinatura = HermesInterpreterConfigurator.getTopicosParaAssinaturaPorTipo("filtragem");
		Set<String> listaChavesFiltragem = topicosFiltragemParaAssinatura.keySet();
		Iterator<String> iterFiltragem = listaChavesFiltragem.iterator();
		while (iterFiltragem.hasNext())
		{
			String nomeTopico = iterFiltragem.next();
			String complementoTopico = topicosFiltragemParaAssinatura.get(nomeTopico);
			hermesBaseManager.subscribeFilteringTopico(nomeTopico, complementoTopico, new HermesInterpreterFilteringListener());
			String texto = "Tópico assinado: " + nomeTopico + " - " + complementoTopico;
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
		}	
	}
	
	public void notificarFiltro(HermesInterpreterTO hermesTO)
	{
		
//		for (FiltroListener filtroListener : hermesTO.getFiltrosNotificacao())
//		{
		FiltroListener filtroListener = hermesTO.getFiltroParaNotificacao();
			ByteArrayOutputStream baosContextoFiltrado = new ByteArrayOutputStream();
			filtroListener.getContextoFiltrado().write(baosContextoFiltrado, hermesTO.getTipoSerializacao(), hermesTO.getCaminhoOntologia());
			
			String texto = "Tópico notificado ---> " + hermesTO.getNomeTopico() + "\n" +
					"Filtro aceito ---> " + filtroListener.getConsultaFiltro() + "\n" +
					"IdEntidade ---> " + hermesTO.getEntidade() + "\n" +
					"ID Listener ---> " + filtroListener.getIdListener();
			HermesInterpreterLog.recordLog(texto);
			System.out.println(texto);
			
			//Esse método para publicação cria um publicador no DDS para o idListener específico, desconhecido no momento da inicialização do HI.
			hermesBaseManager.publishNotification(hermesTO.getEntidade(), hermesTO.getNomeTopico(), filtroListener.getIdListener(), hermesTO.getCaminhoOntologia(), baosContextoFiltrado.toByteArray(), hermesTO.getTipoSerializacao());
		//}
			String texto2 = "*******************************************************************\n\n";
			HermesInterpreterLog.recordLog(texto2);
			System.out.println(texto2);
	}
}
