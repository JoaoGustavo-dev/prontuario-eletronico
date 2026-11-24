# Matriz de rastreabilidade do PEA (slides 21 a 24)

Projeto `prontuario-eletronico`, pacote base `com.health.prontuario`. Caminhos relativos a `api/src/main/java/com/health/prontuario/` (código) e `api/src/test/java/com/health/prontuario/` (testes).

## Unidade I: fundamentos e objetos

| Conteúdo do PEA | Evidência | Classe/método | Teste |
|---|---|---|---|
| POO e introdução a Java | Objetos colaboram: o serviço recebe o paciente, cria a leitura, aplica as regras e persiste os alertas | `SinaisService.registrar` → `SinaisVitais` → `RegraAlerta.avaliar` → `Alerta` | `AlertaVisibilidadeTest`, `PressaoCriticaRegraTest` |
| Classes e objetos | Tipos próprios do domínio, instanciados nos cenários | `Paciente`, `Exame`, `Receita`, `SinaisVitais`, `Alerta`, `ConfiguracaoAlerta` | `PacienteTest`, `RegistroClinicoTest`, `SinaisVitaisTest`, `AlertaTest` |
| Encapsulamento | Atributos privados, sem setters, estado validado no construtor | `ConfiguracaoAlerta` (campos `final`), `Paciente`, `SinaisVitais` | `ConfiguracaoAlertaTest`, `PacienteTest.falha_*`, `SinaisVitaisTest.falha_*` |
| Construtores | Estado inicial sempre válido; construtores sobrecarregados | `Paciente(nome, nascimento, cpf)`, `SinaisVitais` (4 e 5 parâmetros), `ConfiguracaoAlerta` (4 e 5 parâmetros), `PressaoCriticaRegra()` e `(int, int)` | `PacienteTest.normal_construtorGuardaDadosENormalizaONome`, `SinaisVitaisTest.normal_construtorSobrecarregadoDeQuatroParametrosUsaOMomentoAtual` |
| Pacotes e acesso | Pacotes por responsabilidade: `model`, `model.regra`, `service`, `controller`, `repository`, `dto`, `exception`, `config`, `crypto`, `csv`, `gerador`. Atributos `private`; construtores sem argumentos `protected` (exigência do JPA, impedem uso acidental); `CsvUtil` e `ArquivoConfiguracaoAlerta` são `final` com construtor privado (utilitários só estáticos) | Todos os pacotes citados | `PacienteTest` (só o construtor público cria objeto válido) |

## Unidade I: comportamento e interação

| Conteúdo do PEA | Evidência | Classe/método | Teste |
|---|---|---|---|
| Métodos estáticos | Operações da classe, sem estado compartilhado | `SinaisVitais.pressaoPlausivel`, `ConfiguracaoAlerta.padrao`, `CsvUtil.escapar/linha/separar`, `ArquivoConfiguracaoAlerta.gravar/ler/lerOuCriarPadrao` | `SinaisVitaisTest.limite_pressaoNasFronteirasDaFaixa`, `CsvUtilTest`, `CsvUtilSepararTest`, `ArquivoConfiguracaoAlertaTest` |
| Passagem de parâmetros | Java passa por valor: mutar o objeto é visível, reatribuir o parâmetro não | Classes de apoio dentro do teste | `PassagemParametrosTest` (5 testes) |
| Herança | Classe-base abstrata com duas subclasses | `RegistroClinico` → `Exame`, `Receita` (SINGLE_TABLE) | `RegistroClinicoTest.normal_heranca_subclassesSaoRegistroClinico` |
| Programação por eventos | Tratadores de evento acionam operações: `submit` dos formulários de login, histórico, paciente, sinais e registro; `click` em sair e em atualizar alertas; `change` na alternância automática e no tipo de registro; `keydown` global para os atalhos. As chamadas usam `fetch` assíncrono (`async/await`) e o polling usa `setTimeout` encadeado a cada 2 s | `frontend/app.js`: `chamar`, `executar`, `carregarAlertas`, `agendarPolling`, `pararPolling`, `registrarLatencia`, `entrarNoApp`, `sair` | Demonstração manual (atraso medido de 1,6 s); `AlertaVisibilidadeTest` no servidor. **Equivalência ao Swing pendente de confirmação escrita do professor** |
| Interfaces e TADs | Contrato próprio `RegraAlerta`; `ConfiguracaoAlerta` é TAD (estado encapsulado, operações públicas, igualdade por valor) | `RegraAlerta`, `ConfiguracaoAlerta` | `PressaoCriticaRegraTest`, `FrequenciaCardiacaExtremaRegraTest`, `ConfiguracaoAlertaTest` |

## Unidade II: contratos e robustez

| Conteúdo do PEA | Evidência | Classe/método | Teste |
|---|---|---|---|
| Override | `descricao()` abstrato na base e sobrescrito nas subclasses; `nome()` e `avaliar()` sobrescrevem a interface; `toString`, `equals`, `hashCode` sobrescrevem `Object` | `Exame.descricao`, `Receita.descricao`, `PressaoCriticaRegra.avaliar`, `FrequenciaCardiacaExtremaRegra.avaliar` | `RegistroClinicoTest.normal_polimorfismoCadaSubclasseResponderComSuaDescricao` |
| Polimorfismo | Duas estratégias pelo mesmo contrato: `SinaisService` guarda `List<RegraAlerta>` e aplica cada regra sem saber o tipo concreto; `List<RegistroClinico>` mistura exames e receitas | `SinaisService.salvarEAvaliar`, `RegrasAlertaConfig.regrasAlerta` | `PressaoCriticaRegraTest`, `FrequenciaCardiacaExtremaRegraTest`, `RegistroClinicoTest` |
| Overload | Mesmo nome, parâmetros diferentes, em métodos e construtores | `ImportacaoService.importarPacientes(String)` e `(File)`; `importarSinais(String)` e `(File)`; construtores de `SinaisVitais`, `ConfiguracaoAlerta`, regras | `ImportacaoArquivoTest`, `ImportacaoServiceTest` |
| Classe Object | `toString` em `Paciente`, `RegistroClinico`, `SinaisVitais`, `Alerta`, `ConfiguracaoAlerta`; `equals`/`hashCode` em `Paciente` (por CPF, identificador de negócio) e `ConfiguracaoAlerta` (por valor). Nas demais, igualdade por identidade basta | Classes citadas | `PacienteTest.normal_equalsEHashCodeComparamPeloCpf`, `PacienteTest.normal_setNaoDuplicaPacientesComMesmoCpf`, `ConfiguracaoAlertaTest.equalsEHashCodeComparamPorValor`, `AlertaTest.normal_toStringMostraRegraEJustificativa` |
| Exceções | Exceções próprias com recuperação: `DadosInvalidosException` (400), `PacienteDuplicadoException` (409), `PacienteNaoEncontradoException` (404), `CriptografiaException` (500 genérico, causa só no log), `ArquivoConfiguracaoException`. A importação captura o erro da linha e segue, devolvendo relatório; `lerOuCriarPadrao` recria o `config.bin` ausente | `GlobalExceptionHandler`, `ImportacaoService.processar`, `ArquivoConfiguracaoAlerta.lerOuCriarPadrao` | `CriptografiaHandlerTest`, `ImportacaoServiceTest`, `ImportacaoArquivoTest.falha_*`, `ArquivoConfiguracaoAlertaTest` |

## Unidade II: dados e interface

| Conteúdo do PEA | Evidência | Classe/método | Teste |
|---|---|---|---|
| Classe File | Verifica `isFile()` e `canRead()` antes de ler o CSV | `ImportacaoService.lerArquivo(File)` (privado, usado pelas sobrecargas públicas com `File`) | `ImportacaoArquivoTest` (arquivo válido, vazio, inexistente, diretório) |
| Arquivos texto | CSV documentado (separador `;`, aspas dobradas), exportação, importação com validação de cabeçalho e de cada linha, relatório de linhas rejeitadas | `CsvUtil`, `ExportacaoService`, `ImportacaoService`; formato em `docs/dicionario-de-dados.md` | `CsvUtilTest`, `CsvUtilSepararTest`, `ExportacaoControllerTest`, `ImportacaoServiceTest`, `ImportacaoControllerTest`, `ImportacaoCpfRepetidoTest`, `GeradorDadosImportacaoTest` |
| Arquivos binários | `api/config.bin` versionado: número mágico, versão, 4 limites, instante. Salvar, fechar e recuperar. Rejeita formato inválido, truncado ou com bytes sobrando | `ArquivoConfiguracaoAlerta.gravar/ler`, `RegrasAlertaConfig` | `ArquivoConfiguracaoAlertaTest` (11), `RegrasAlertaConfigTest` (3) |
| Coleções | `List` guarda resultados em ordem (regras, erros de importação, alertas). `Map<Long, List<AlertaResponse>>` agrupa alertas por leitura com busca direta (`HistoricoService`). `LinkedHashMap` mantém a ordem dos campos no erro (`GlobalExceptionHandler`). `HashSet` detecta CPF repetido no arquivo em O(1) (`ImportacaoService`) | Classes citadas | `ImportacaoCpfRepetidoTest`, `PacienteTest.normal_setNaoDuplicaPacientesComMesmoCpf` |
| GUI | Interface web em `frontend/` (HTML, CSS e JS puro): login, cadastro, sinais, registros, histórico e painel de alertas; validação nos campos, mensagens por campo, formulários desabilitados para o leitor, atalhos Option/Alt + A, P, H, N, S, R | `frontend/index.html`, `estilo.css`, `app.js` (`entrarNoApp` desabilita os formulários de escrita para o leitor; `textoDoErro` e `mostrar` exibem a mensagem por campo; `inteiro` valida os números; `renderHistorico` e `itemAlerta` montam a tela com `textContent`) | Demonstração manual e print da medição do atraso. **Aguarda confirmação escrita do professor sobre o uso de web no lugar de Swing** |

## Critérios de aceite do contrato

| Critério | Evidência | Teste |
|---|---|---|
| Leitor não altera registros | `SecurityConfig`: GET para `PROFISSIONAL` e `LEITOR`, qualquer outro método só `PROFISSIONAL` | `SecurityConfigTest` (6) e `AlertaVisibilidadeTest.semCredenciaisNaoEnxergaAlertas` |
| Nota clínica ilegível no banco | AES-256-GCM via `NotaClinicaConverter` e `CifradorNotas` | `CifradorNotasTest` (11), `NotaClinicaPersistenciaTest` (5) |
| Alerta visível em até 5 s | Polling de 2 s; medido 1,6 s | `AlertaVisibilidadeTest.alertaCriticoFicaVisivelEmMenosDeCincoSegundos`, `AlertaVisibilidadeTest.limiteDaPressaoDisparaNoValorExatoENaoAbaixo` (limite 180 exato) e medição manual (local, mesma máquina) |
| 20 pacientes e 100 leituras com regra documentada | `GeradorDados`, semente 42, `dados/` | `GeradorDadosTest` (11), `GeradorDadosImportacaoTest` (1) |

## Os três cenários (normal, limite e falha)

Os testes de domínio e de arquivos usam o prefixo `normal_`, `limite_` e `falha_` no nome. Exemplos: `PacienteTest`, `RegistroClinicoTest`, `SinaisVitaisTest`, `AlertaTest`, `ImportacaoArquivoTest`, `ImportacaoCpfRepetidoTest`, `PassagemParametrosTest`.