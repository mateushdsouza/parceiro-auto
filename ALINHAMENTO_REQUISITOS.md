# Alinhamento dos requisitos

## Organizacao da View

- Mantida a separacao entre tela, controller, service e repository.
- As telas Swing usam `AppContext`, controllers e services ja criados na aplicacao.
- A busca nos arquivos de `src/main/java/br/com/parceiroauto/view/swing` nao encontrou acesso direto a `Repository`, `EntityManager` ou `JPAUtil`.
- As telas de login, cadastro de usuario, selecao de empresa e cadastro de empresa foram ajustadas para remover `setLayout(null)`.

## Uso de layouts

- `LoginFrame`, `RegisterFrame`, `LoginCompanyFrame` e `RegisterCompanyFrame` passaram a usar `GridBagLayout`.
- Os botoes das telas simples passaram a ficar em `GridLayout`, mantendo alinhamento consistente.
- Os campos agora respeitam o layout do Swing em vez de coordenadas fixas.

## Customizacao da interface

- Padronizado o uso de fonte `Segoe UI` nas telas ajustadas.
- Mantido o fundo branco e espacamento por `EmptyBorder` para preservar a identidade simples ja usada no projeto.
- Os botoes foram padronizados com fonte em negrito e sem pintura de foco.

## Integracao com backend

- A interface continua chamando controllers e services, preservando o fluxo `View -> Controller/Service -> Repository`.
- A regra de recorrencia continua centralizada em `RecurrenceRuleService`.
- O processamento automatico de recorrencias foi ligado na inicializacao da aplicacao sem colocar regra de negocio dentro das telas.

## Estrutura Maven

- O projeto ja estava estruturado em `src/main/java` e `src/main/resources`.
- O `pom.xml` foi ajustado para usar identificadores do projeto:
  - `groupId`: `br.com.parceiroauto`
  - `artifactId`: `parceiro-auto`

## Observacao

- A classe `br.com.parceiroauto.view.View` e uma interface de console legada. A aplicacao iniciada pela `Main` usa a interface Swing.
