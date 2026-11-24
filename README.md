# Prontuário eletrônico e triagem simulada

Projeto de Programação (UNIT, Ciência da Computação, 4º período). Tema 1: **Saúde, prontuário e triagem simulada**. Módulo `prontuario-eletronico`, pacote base `com.health.prontuario`.

API REST que cadastra pacientes fictícios, guarda exames e receitas (com notas clínicas cifradas), registra sinais vitais e gera alertas explicados por regras. Uma interface web simples consome a API.

Todos os dados são **fictícios**. Os limites de alerta são didáticos e não são orientação médica.

## O que o projeto entrega

- 5 endpoints mínimos do contrato, mais exportação e importação de CSV.
- `RegistroClinico` com `Exame` e `Receita`; interface `RegraAlerta` com duas regras (pressão crítica e frequência cardíaca extrema).
- Spring Security com dois perfis: `PROFISSIONAL` (lê e escreve) e `LEITOR` (só lê).
- Notas clínicas cifradas com AES-256-GCM no banco.
- Alerta visível na interface em menos de 5 s (polling de 2 s; medido 1,6 s em teste local).
- 20 pacientes e 100 leituras sintéticas (semente 42) com a regra esperada documentada.
- Configuração dos limites em arquivo binário versionado (`api/config.bin`).

## Tecnologias

Java 21, Spring Boot 4.1.1, Maven (wrapper incluso), PostgreSQL 17 (Docker), JUnit 5, HTML/CSS/JavaScript puro no front. Sem Lombok, de propósito.

## Pré-requisitos

- JDK 21 ou superior (o `pom.xml` compila com release 21)
- Docker Desktop
- Python 3 (apenas para servir o front)
- `openssl` (para gerar a chave de criptografia)

## Executar do zero

Todos os comandos partem da raiz do repositório.

**1. Clonar**

```
git clone https://github.com/JoaoGustavo-dev/prontuario-eletronico.git
```
```
cd prontuario-eletronico
```

**2. Criar o `.env`** (nunca é versionado)

```
cp .env.example .env
```

Edite o `.env` e troque todos os valores `troque_esta_senha`. Gere a chave de criptografia (Base64 de 32 bytes) com:

```
openssl rand -base64 32
```

Cole o resultado em `APP_CRYPTO_CHAVE`.

| Variável | Uso |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Banco PostgreSQL (também lidas pelo `compose.yaml`) |
| `AUTH_PROFISSIONAL_USUARIO`, `AUTH_PROFISSIONAL_SENHA` | Login do perfil `PROFISSIONAL` |
| `AUTH_LEITOR_USUARIO`, `AUTH_LEITOR_SENHA` | Login do perfil `LEITOR` |
| `APP_CRYPTO_CHAVE` | Chave AES-256 das notas clínicas |

Nenhuma variável tem valor padrão no `application.yaml`, exceto `DB_HOST` (`localhost`) e `DB_PORT` (`5432`). Se faltar outra, a API não sobe.

**3. Subir o banco**

```
docker compose up -d
```
```
docker compose ps
```

Espere o serviço `db` ficar `healthy`.

**4. Subir a API** (porta 8080)

```
(cd api && ./mvnw spring-boot:run)
```

Na primeira subida o Hibernate cria as tabelas (`ddl-auto: update`, só para desenvolvimento).

**5. Subir o front** (em outro terminal, porta 5500)

```
(cd frontend && python3 -m http.server 5500)
```

Abra `http://localhost:5500` e entre com um dos usuários do `.env`. **Não abra o `index.html` por `file://`**: a origem `null` quebra o CORS. As origens permitidas são `http://localhost:5500` e `http://127.0.0.1:5500`.

Atalhos de teclado (Option/Alt + tecla): A atualiza os alertas, P liga ou desliga a atualização automática, H foca o histórico, N foca o nome no cadastro de paciente, S foca os sinais vitais, R foca o registro clínico.

## Carregar os dados de demonstração

Importe os pacientes **antes** dos sinais, porque cada leitura aponta para um CPF já cadastrado. Carregue as credenciais no terminal sem digitar a senha:

```
set -a && source .env && set +a
```
```
curl -s -u "$AUTH_PROFISSIONAL_USUARIO:$AUTH_PROFISSIONAL_SENHA" -F "arquivo=@dados/pacientes.csv" http://localhost:8080/importacao/pacientes
```
```
curl -s -u "$AUTH_PROFISSIONAL_USUARIO:$AUTH_PROFISSIONAL_SENHA" -F "arquivo=@dados/sinais.csv" http://localhost:8080/importacao/sinais
```

A resposta traz `linhasLidas`, `importadas` e `erros`. As leituras passam pelas regras e geram alertas, que aparecem no painel. Reimportar os mesmos pacientes rejeita as 20 linhas como "CPF já cadastrado" (comportamento esperado).

Para regenerar os CSVs (saída idêntica, semente fixa 42):

```
(cd api && ./mvnw -q compile)
```
```
java -cp api/target/classes com.health.prontuario.gerador.GeradorDados dados
```

## Perfis de acesso

| Perfil | Leitura (GET) | Escrita (POST) |
|---|---|---|
| `PROFISSIONAL` | sim | sim |
| `LEITOR` | sim | não (403) |

Detalhes, corpos e códigos de erro em [`docs/contrato-rest.md`](docs/contrato-rest.md).

## Testes

O Postgres precisa estar no ar e o `.env` completo, porque os testes com `@SpringBootTest` usam o banco.

```
(cd api && ./mvnw test | grep "Tests run:" | tail -n 1)
```

Esperado: `Tests run: 144, Failures: 0, Errors: 0`. Os testes seguem três cenários (prefixos `normal_`, `limite_`, `falha_` nos nomes).

## Estrutura

```
prontuario-eletronico/
  api/            projeto Spring Boot (código, testes e config.bin)
  frontend/       interface web (index.html, estilo.css, app.js)
  dados/          CSVs sintéticos versionados
  docs/           documentação
  compose.yaml    PostgreSQL 17 com volume e healthcheck
  .env.example    modelo das variáveis (sem segredos)
```

Pacotes em `api/src/main/java/com/health/prontuario/`: `model`, `model.regra`, `service`, `controller`, `repository`, `dto`, `exception`, `config`, `crypto`, `csv`, `gerador`. O guia sugere `integration/` e `files/`; aqui o código de arquivos ficou em `csv`, `config` e `gerador`, e a criptografia em `crypto`.

## Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/dicionario-de-dados.md`](docs/dicionario-de-dados.md) | Tabelas, colunas, unidades, timestamps, regras de disparo, `config.bin` e CSVs |
| [`docs/contrato-rest.md`](docs/contrato-rest.md) | Endpoints, perfis, corpos e erros |
| [`docs/diagrama-de-classes.md`](docs/diagrama-de-classes.md) | Diagrama de classes em Mermaid |
| [`docs/matriz-pea.md`](docs/matriz-pea.md) | Matriz de rastreabilidade do PEA (slides 21 a 24) |

## Observações importantes

- **Interface web no lugar de Swing.** O front é HTML, CSS e JavaScript puro, e os itens de GUI e eventos da matriz são provados em JavaScript. A equivalência ainda aguarda confirmação escrita do professor.
- **`config.bin`** guarda os limites das regras e é lido só na subida da API. Se for apagado, a API o recria com os limites padrão, e o Git passa a mostrar o arquivo como modificado (`git checkout api/config.bin` restaura). Se a API subir de outro diretório, cria um `config.bin` lá.
- O gerador e o teste `GeradorDadosImportacaoTest` assumem os limites padrão (180/120 mmHg e 150/40 bpm). Se editar o `config.bin`, esse teste falha por causa dos limites, não por bug.
- O atraso medido no front compara o relógio do servidor com o do navegador, então só vale em teste local, na mesma máquina.
- Importação CSV: campos entre aspas com quebra de linha não são suportados, e o limite de upload é o padrão do Spring (1 MB).

## Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| Vários testes com erro, o primeiro é `Connection refused` em `localhost:5432` | Banco desligado: `docker compose up -d` |
| A API não sobe reclamando de placeholder sem valor | Falta variável no `.env` |
| Mudei a senha do banco no `.env` e a API não conecta | O volume guarda a senha antiga: `docker compose down -v` recria o banco (apaga os dados) |
| Front carrega mas as chamadas falham com erro de CORS | Abra por `http://localhost:5500` ou `http://127.0.0.1:5500`, nunca por `file://` |
| `nota_clinica` com tipo errado em banco antigo | Num banco criado antes da cifragem: `alter table registros_clinicos alter column nota_clinica type text` |

## Evolução futura

Microserviços (separar pacientes e prontuário, sinais vitais e alertas), WebSocket ou SSE no lugar do polling, JWT, Flyway no lugar de `ddl-auto: update`, novas regras (temperatura, saturação), paginação de `GET /alertas`, `GET /pacientes`, endpoint de identidade do usuário, recarga do `config.bin` sem reiniciar e deploy completo com Docker.

## Autoria

Grupo: Arthur Vieira, Álvaro Fraga, José Gabriel Fontes, João Gustavo e Pedro Simões. Ciência da Computação (UNIT), 4º período. Disciplina Projeto de Programação, profs. Walisson Souza e Jonnathan Souza.
