# Diagrama de classes

Pacote base `com.health.prontuario`. Os métodos de acesso (getters) foram omitidos para manter o diagrama legível.

```mermaid
classDiagram
    direction LR

    class Paciente {
        -Long id
        -String nome
        -LocalDate dataNascimento
        -String cpf
        +equals(Object) boolean
        +hashCode() int
        +toString() String
    }

    class RegistroClinico {
        <<abstract>>
        -Long id
        -Instant registradoEm
        -String notaClinica
        +descricao()* String
        +toString() String
    }

    class Exame {
        -String tipoExame
        -String resultado
        +descricao() String
    }

    class Receita {
        -String medicamento
        -String posologia
        +descricao() String
    }

    class SinaisVitais {
        -Long id
        -int pressaoSistolica
        -int pressaoDiastolica
        -int frequenciaCardiaca
        -Instant instante
        +pressaoPlausivel(int, int)$ boolean
        +toString() String
    }

    class Alerta {
        -Long id
        -String regra
        -String justificativa
        -Instant geradoEm
        +toString() String
    }

    class RegraAlerta {
        <<interface>>
        +nome() String
        +avaliar(SinaisVitais) Optional~Alerta~
    }

    class PressaoCriticaRegra {
        -int limiteSistolica
        -int limiteDiastolica
    }

    class FrequenciaCardiacaExtremaRegra {
        -int limiteSuperior
        -int limiteInferior
    }

    class ConfiguracaoAlerta {
        -int limiteSistolicaMmhg
        -int limiteDiastolicaMmhg
        -int limiteFcSuperiorBpm
        -int limiteFcInferiorBpm
        -Instant gravadoEm
        +padrao()$ ConfiguracaoAlerta
        +equals(Object) boolean
        +hashCode() int
        +toString() String
    }

    class ArquivoConfiguracaoAlerta {
        +gravar(Path, ConfiguracaoAlerta)$ void
        +ler(Path)$ ConfiguracaoAlerta
        +lerOuCriarPadrao(Path)$ ConfiguracaoAlerta
    }

    class RegrasAlertaConfig {
        +regrasAlerta(String) List~RegraAlerta~
    }

    class SinaisService {
        +registrar(Long, SinaisRequest) SinaisResponse
        +registrarLeitura(SinaisVitais) SinaisResponse
    }

    class ImportacaoService {
        +importarPacientes(String) ImportacaoResponse
        +importarPacientes(File) ImportacaoResponse
        +importarSinais(String) ImportacaoResponse
        +importarSinais(File) ImportacaoResponse
    }

    RegistroClinico <|-- Exame
    RegistroClinico <|-- Receita
    RegraAlerta <|.. PressaoCriticaRegra
    RegraAlerta <|.. FrequenciaCardiacaExtremaRegra

    Paciente "1" --> "*" RegistroClinico : possui
    Paciente "1" --> "*" SinaisVitais : possui
    SinaisVitais "1" --> "*" Alerta : gera

    RegraAlerta ..> SinaisVitais : avalia
    RegraAlerta ..> Alerta : produz
    SinaisService --> "*" RegraAlerta : aplica
    RegrasAlertaConfig ..> ArquivoConfiguracaoAlerta : lê
    ArquivoConfiguracaoAlerta ..> ConfiguracaoAlerta : grava e recupera
    RegrasAlertaConfig ..> RegraAlerta : cria
    ImportacaoService --> SinaisService : usa
```

## Leitura rápida

- **Herança:** `Exame` e `Receita` especializam `RegistroClinico`, que declara `descricao()` abstrato; cada subclasse sobrescreve.
- **Polimorfismo:** `SinaisService` recebe uma `List<RegraAlerta>` e aplica cada regra pelo mesmo contrato, sem saber qual é.
- **TAD:** `ConfiguracaoAlerta` tem estado privado, validado no construtor, sem setters.
- **Sobrecarga:** `ImportacaoService` oferece `importarPacientes` e `importarSinais` com `String` e com `File`.
- **Omitidos:** controllers, repositórios, DTOs e exceções. Seguem o padrão controller → service → repository.