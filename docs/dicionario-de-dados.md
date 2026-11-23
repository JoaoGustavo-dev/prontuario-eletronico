# Dicionário de dados

Projeto `prontuario-eletronico`. Todos os dados são **fictícios e simulados**. Não há dado medido, publicado ou de pessoa real.

## 1. Origem e classificação dos dados

| Dado | Classificação | Origem |
|---|---|---|
| Pacientes (CPF, nome, nascimento) | Simulado | `GeradorDados`, semente fixa 42 |
| Leituras de sinais vitais | Simulado | `GeradorDados`, semente fixa 42 |
| Regras e limites de alerta | Assumido | Valores didáticos em `config.bin` (não são diretriz clínica) |
| Alertas | Derivado | Gerados pela API ao avaliar cada leitura |
| Notas clínicas, exames, receitas | Simulado | Digitados manualmente na demonstração |

CPFs fictícios usam prefixos reservados: `9000000...` (demo, arquivos em `dados/`), `999999999xx` e `9100000...` (testes automatizados) e `8888888880x` (testes manuais).

## 2. Banco de dados (PostgreSQL 17)

Esquema gerado pelo Hibernate (`ddl-auto: update`, apenas desenvolvimento).

### 2.1 Relacionamentos

- `pacientes` 1 — N `registros_clinicos`
- `pacientes` 1 — N `sinais_vitais`
- `sinais_vitais` 1 — N `alertas` (uma leitura pode disparar mais de uma regra)

### 2.2 Tabela `pacientes`

| Coluna | Tipo | Nulo | Unidade / formato | Significado |
|---|---|---|---|---|
| `id` | bigint, identity | não | n/a | Chave primária |
| `cpf` | varchar(11) | não | 11 dígitos numéricos, sem pontuação | Identificador de negócio, **único** (`UNIQUE`) |
| `nome` | varchar(255) | não | texto | Nome completo, sem espaços nas pontas |
| `data_nascimento` | date | não | aaaa-MM-dd | Não pode ser futura |

### 2.3 Tabela `registros_clinicos` (herança SINGLE_TABLE)

Uma única tabela guarda `Exame` e `Receita`. A coluna `tipo` discrimina o subtipo (`CHECK` aceita apenas `EXAME` e `RECEITA`).

| Coluna | Tipo | Nulo | Unidade / formato | Significado |
|---|---|---|---|---|
| `id` | bigint, identity | não | n/a | Chave primária |
| `tipo` | varchar(31) | não | `EXAME` ou `RECEITA` | Discriminador da herança |
| `paciente_id` | bigint | não | FK para `pacientes.id` | Dono do registro |
| `registrado_em` | timestamptz | não | instante UTC | Quando o registro foi criado |
| `nota_clinica` | text | sim | Base64 de IV (12 B) + texto cifrado + tag (16 B) | Nota **cifrada com AES-256-GCM**; nunca legível no banco |
| `tipo_exame` | varchar(255) | sim | texto | Só para `EXAME` |
| `resultado` | varchar(255) | sim | texto | Só para `EXAME` |
| `medicamento` | varchar(255) | sim | texto | Só para `RECEITA` |
| `posologia` | varchar(255) | sim | texto | Só para `RECEITA` |

As colunas específicas são nulas para o outro subtipo. É a consequência esperada de SINGLE_TABLE.

### 2.4 Tabela `sinais_vitais`

| Coluna | Tipo | Nulo | Unidade | Significado |
|---|---|---|---|---|
| `id` | bigint, identity | não | n/a | Chave primária |
| `paciente_id` | bigint | não | FK para `pacientes.id` | Paciente medido |
| `pressao_sistolica_mmhg` | integer | não | **mmHg** | Pressão sistólica |
| `pressao_diastolica_mmhg` | integer | não | **mmHg** | Pressão diastólica |
| `frequencia_cardiaca_bpm` | integer | não | **bpm** | Batimentos por minuto |
| `instante` | timestamptz | não | instante UTC | Quando a leitura foi **feita** (informado pelo cliente ou pelo CSV) |

### 2.5 Tabela `alertas`

| Coluna | Tipo | Nulo | Unidade / formato | Significado |
|---|---|---|---|---|
| `id` | bigint, identity | não | n/a | Chave primária |
| `sinais_id` | bigint | não | FK para `sinais_vitais.id` | Leitura que disparou o alerta |
| `regra` | varchar(255) | não | `PRESSAO_CRITICA` ou `FREQUENCIA_CARDIACA_EXTREMA` | Identificador da regra que disparou |
| `justificativa` | varchar(500) | não | texto | Explicação legível do disparo |
| `gerado_em` | timestamptz | não | instante UTC | Quando o **servidor** gerou o alerta |

### 2.6 Dois instantes diferentes

- `sinais_vitais.instante`: quando a medição aconteceu.
- `alertas.gerado_em`: quando o servidor criou o alerta (`Instant.now()`).

O atraso de 5 s do critério de aceite é medido sobre `gerado_em`.

## 3. Regras de alerta (regra de disparo)

Limites padrão, lidos do `config.bin` na subida da API.

| Regra | Dispara quando | Limites padrão |
|---|---|---|
| Pressão crítica | sistólica >= 180 mmHg ou diastólica >= 120 mmHg | 180 / 120 |
| Frequência cardíaca extrema | frequência >= 150 bpm (taquicardia) ou <= 40 bpm (bradicardia) | 150 / 40 |

Textos de justificativa gerados pela API:

| Regra | Exemplo de justificativa |
|---|---|
| `PRESSAO_CRITICA` | Pressão 190/100 mmHg atingiu o limite crítico (180/120 mmHg). |
| `FREQUENCIA_CARDIACA_EXTREMA` (alta) | Taquicardia: 155 bpm (limite 150 bpm). |
| `FREQUENCIA_CARDIACA_EXTREMA` (baixa) | Bradicardia: 38 bpm (limite 40 bpm). |

Uma leitura pode disparar as duas regras ao mesmo tempo, gerando dois alertas.

## 4. Arquivo binário `api/config.bin`

32 bytes, big-endian (`DataOutputStream`/`DataInputStream`).

| Posição | Tamanho | Tipo | Conteúdo |
|---|---|---|---|
| 0 | 4 B | int | Número mágico `0x50524F4E` ("PRON") |
| 4 | 4 B | int | Versão do formato (1) |
| 8 | 4 B | int | Limite de pressão sistólica (mmHg) |
| 12 | 4 B | int | Limite de pressão diastólica (mmHg) |
| 16 | 4 B | int | Limite superior de frequência (bpm) |
| 20 | 4 B | int | Limite inferior de frequência (bpm) |
| 24 | 8 B | long | Instante da gravação (epoch ms, UTC) |

Rejeitados na leitura: número mágico errado, versão desconhecida, arquivo truncado, bytes sobrando e valores inválidos. O arquivo não guarda prontuários nem segredos. Só é lido na subida da API.

## 5. Arquivos CSV (`dados/`)

Formato: UTF-8, separador `;`, campos com `;`, `"` ou quebra de linha entre aspas (aspas dobradas). Campos multilinha não são suportados.

| Arquivo | Conteúdo |
|---|---|
| `pacientes.csv` | 20 pacientes: `cpf`, `nome`, `data_nascimento` (aaaa-MM-dd) |
| `sinais.csv` | 100 leituras: `cpf`, `pressao_sistolica_mmhg`, `pressao_diastolica_mmhg`, `frequencia_cardiaca_bpm`, `instante_utc` (ISO-8601 UTC, ex.: 2026-11-01T08:00:00Z) |
| `sinais_regras_esperadas.csv` | `linha`, `cpf`, `instante_utc`, `regra_esperada` (`PRESSAO_CRITICA`, `FREQUENCIA_CARDIACA_EXTREMA` ou `NENHUMA`) e `criterio_disparo`. O número da linha corresponde à linha em `sinais.csv`. |

### 5.1 Regras de geração

- Gerador: `com.health.prontuario.gerador.GeradorDados`, Java puro, **semente fixa 42** (saída reproduzível).
- Distribuição das 100 leituras: 70% normais, 12% pressão crítica, 9% taquicardia, 9% bradicardia.
- Comando (a partir da raiz): `java -cp api/target/classes com.health.prontuario.gerador.GeradorDados dados`
- O gerador conhece apenas os limites **padrão** (180/120 e 150/40).
- Os instantes seguem 5 rodadas de 20 leituras, uma por paciente. Cada rodada começa 6 h depois da anterior (a primeira em `2026-11-01T08:00:00Z`) e as leituras dentro dela têm 1 s de intervalo.

### 5.2 Tratamento na importação

| Problema | Tratamento |
|---|---|
| Arquivo vazio ou cabeçalho diferente do esperado | Rejeita tudo (HTTP 400) |
| Número de colunas errado | Linha rejeitada, com motivo |
| CPF fora de 11 dígitos, nome vazio, data inválida ou futura | Linha rejeitada |
| CPF repetido no mesmo arquivo | Segunda ocorrência rejeitada |
| CPF já cadastrado no banco | Linha rejeitada |
| Leitura de CPF inexistente | Linha rejeitada |
| Número ou instante mal formado | Linha rejeitada |

A importação é **parcial**: linhas válidas entram e a resposta traz `linhasLidas`, `importadas` e `erros` (linha e motivo). Leituras importadas passam pelas regras e geram alertas.

## 6. Limitações conhecidas

- Os dados são simulados e não representam população real.
- Não há separação entre dado bruto e tratado: os CSVs são gerados já no formato final.
- Os limites clínicos são didáticos, não orientação médica.
- Hibernate cria o esquema automaticamente; não há migrações versionadas (melhoria futura: Flyway).