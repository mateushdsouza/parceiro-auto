# CRON de movimentacoes recorrentes

## Objetivo

Criar uma rotina diaria para verificar regras de recorrencia e gerar as movimentacoes pendentes automaticamente.

## Onde foi implementado

- Arquivo principal: `src/main/java/br/com/parceiroauto/Main.java`
- Servico usado: `RecurrenceRuleService`
- Metodo de negocio chamado: `processPendingRecurrenceRules()`

## Como funciona

- A `Main` cria um `ScheduledExecutorService` ao iniciar a aplicacao.
- O agendador executa todos os dias as `00:05`.
- A rotina tambem executa uma vez na inicializacao para processar pendencias caso a aplicacao tenha ficado fechada.
- Cada execucao abre um `EntityManager` proprio, monta os repositories/services necessarios e fecha o `EntityManager` no final.
- Em caso de erro, a mensagem e registrada no console sem derrubar a interface Swing.

## Fluxo executado

1. Buscar todas as regras de recorrencia.
2. Calcular a proxima execucao de cada regra.
3. Gerar movimentacoes pendentes ate a data atual.
4. Atualizar `ultimaExecucao` da regra processada.
5. Repetir o processo diariamente.

## Cuidados tecnicos

- O agendador roda em thread daemon chamada `recurrence-rule-cron`.
- Foi adicionado `shutdownHook` para encerrar o agendador quando a aplicacao fechar.
- O CRON nao usa o mesmo `EntityManager` da interface, evitando compartilhamento de contexto JPA entre threads.
