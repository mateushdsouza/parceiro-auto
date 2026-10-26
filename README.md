# ParceiroAuto — Desktop

Aplicação acadêmica de gestão financeira em Java Swing. Esta é a **versão desktop**; o [ParceiroAuto web](https://github.com/Adriannoo/ParceiroAuto), desenvolvido em grupo com Spring Boot e Angular, está em outro repositório.

## Funcionalidades presentes no código

- Cadastro e acesso de usuários e empresas.
- Contas bancárias, funcionários e categorias de movimentação.
- Entradas, saídas e regras de recorrência.
- Telas de relatórios e gráficos.
- Persistência em PostgreSQL com JPA/Hibernate e migrations com Flyway.

## Tecnologias

Java 17 (configurado no pom.xml), Swing, Maven, PostgreSQL, Jakarta Persistence, Hibernate, Flyway, JFreeChart e Apache POI.

## Como executar

1. Instale um JDK compatível com Java 17, Maven e PostgreSQL.
2. Clone o repositório e abra a pasta que contém [pom.xml](pom.xml) em uma IDE Java.
3. Crie um banco de desenvolvimento separado. Confira a configuração local em [persistence.xml](src/main/resources/META-INF/persistence.xml) e [FlyWayconfg.java](src/main/java/br/com/parceiroauto/confg/FlyWayconfg.java), mantendo ambas apontadas para o mesmo banco. Não publique credenciais pessoais.
4. Recarregue as dependências Maven e execute [Main.java](src/main/java/br/com/parceiroauto/Main.java), classe `br.com.parceiroauto.Main`.

Para conferir a compilação, na raiz:

```sh
mvn compile
```

As migrations ficam em [src/main/resources/db/migration/](src/main/resources/db/migration/). A inicialização pode modificar o esquema e os dados do banco configurado; use um ambiente de desenvolvimento.

## Organização

- `controller`: operações expostas às telas.
- `model/service`: regras de negócio.
- `model/repository` e `model/entity`: persistência e entidades.
- `view/swing`: interface desktop.
- `confg`: configuração de persistência e migrations.

Todos esses pacotes ficam em [src/main/java/br/com/parceiroauto/](src/main/java/br/com/parceiroauto/).

## Estado e autoria

Projeto acadêmico. A execução depende do ambiente Java e do banco local. Não foi executada uma validação funcional completa nesta revisão de documentação. A autoria original permanece no histórico Git; não se atribui a uma única pessoa o trabalho coletivo.
