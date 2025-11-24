# 🛡️ API - VALIDADOR DE FRAUDE PIX (Backend)

## 📝 Visão Geral do Projeto

Este projeto é o **Backend** de um sistema de validação de transações PIX em tempo real, desenvolvido em **Spring Boot 3.x**. Ele é responsável pela **lógica de validação de fraude**, persistência de dados e exposição dos endpoints da API REST utilizados pelo Frontend.

O foco principal deste projeto foi garantir a estabilidade transacional, aplicando a arquitetura robusta **Spring Data JPA** para resolver o problema de persistência (LazyInitializationException) de forma definitiva.

---

## 🛠️ Stack Tecnológica

| Componente | Tecnologia | Detalhe |
| :--- | :--- | :--- |
| **Framework** | **Spring Boot 3.x** | Construção da API REST. |
| **Linguagem** | **Java** | Versão **21** (ou superior). |
| **Gerenciador de Build** | **Apache Maven** | Utilizado para gerenciamento de dependências. |
| **Persistência** | **Spring Data JPA** | Utilizado para o mapeamento objeto-relacional (ORM), com implementação de `FETCH JOIN` e `@Modifying` para correções críticas. |
| **Banco de Dados**| **PostgreSQL** | Servidor de banco de dados relacional. |
| **Utilitários** | **Lombok** | Geração automática de *getters*, *setters* e construtores. |

---

## 💡 Regras de Validação e Fluxo

O serviço central, `TransactionValidator`, aplica regras para classificar as transações em **3 estados** principais:

1.  **SUCCESS (Aprovada):** Transação limpa, aprovada automaticamente.
2.  **PENDING\_REVIEW (Em Análise):** Risco moderado (ex: descrição suspeita). O item é enviado ao Painel do Analista para decisão manual.
3.  **FAILED (Rejeitada):** Alto risco (ex: valor acima do limite ou na blacklist). Rejeitada imediatamente.

---

## ⚙️ Instalação e Execução

### Pré-requisitos
* **JDK 21** (ou superior)
* **Apache Maven**
* Servidor **PostgreSQL** rodando localmente (porta padrão: 5432)

### 1. Configuração do Banco de Dados

1.  **Crie o Banco:** Crie o banco de dados PostgreSQL (via terminal ou DBeaver). O nome do banco de dados esperado é `pix_validator_db`.
    ```sql
    CREATE DATABASE pix_validator_db;
    ```
2.  **Ajuste o `application.properties`:** Verifique se as credenciais de `spring.datasource.*` estão configuradas para seu usuário PostgreSQL.

### 2. Compilação e Início do Servidor

1.  **Navegue para a pasta do projeto:** Abra o terminal e certifique-se de estar no diretório que contém o `pom.xml` (o subdiretório principal).
2.  **Compile o Projeto:** Use o Maven para limpar o cache de compilação e gerar o pacote JAR.
    ```bash
    mvn clean install
    ```
3.  **Inicie o Servidor:** Após a mensagem **`BUILD SUCCESS`**, execute a classe principal `DemoApplication.java` pela sua IDE (VS Code ou IntelliJ).

O backend estará rodando em `http://localhost:8080`.

---

## 🌐 Endpoints da API

| Endpoint | Método | Descrição |
| :--- | :--- | :--- |
| `/api/transactions` | `POST` | Cria uma nova transação PIX e executa a validação de risco. |
| `/api/transactions/{id}/approve` | `POST` | **Ação:** Altera o status da transação para `SUCCESS`. |
| `/api/transactions/{id}/reject` | `POST` | **Ação:** Altera o status da transação para `FAILED`. |
| `/api/transactions/status/{status}`| `GET` | Lista transações por status (`PENDING_REVIEW`, `SUCCESS`, `FAILED`). |
