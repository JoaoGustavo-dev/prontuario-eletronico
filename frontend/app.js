const API = "http://localhost:8080";

let credenciais = null;

const $ = (id) => document.getElementById(id);

function criar(tag, texto, classe) {
  const el = document.createElement(tag);
  if (texto !== undefined) el.textContent = texto;
  if (classe) el.className = classe;
  return el;
}

function mostrar(idMsg, texto, ok) {
  const el = $(idMsg);
  el.textContent = texto;
  el.className = "msg " + (ok ? "ok" : "erro");
}

function limpar(idMsg) {
  const el = $(idMsg);
  el.textContent = "";
  el.className = "msg";
}

function formatarData(iso) {
  return new Date(iso).toLocaleString("pt-BR");
}

function basic(usuario, senha) {
  const bytes = new TextEncoder().encode(usuario + ":" + senha);
  return "Basic " + btoa(String.fromCharCode(...bytes));
}

async function chamar(caminho, metodo = "GET", corpo) {
  const headers = { Authorization: credenciais };
  if (corpo !== undefined) headers["Content-Type"] = "application/json";
  const resp = await fetch(API + caminho, {
    method: metodo,
    headers,
    body: corpo !== undefined ? JSON.stringify(corpo) : undefined
  });
  const texto = await resp.text();
  let dados = null;
  if (texto) {
    try { dados = JSON.parse(texto); } catch { dados = null; }
  }
  return { status: resp.status, ok: resp.ok, dados };
}

function textoDoErro(r) {
  if (r.status === 401) return "Sessão inválida. Entre novamente.";
  if (r.status === 403) return "Seu perfil (leitor) não pode alterar dados.";
  const d = r.dados;
  if (!d) return "Erro inesperado (HTTP " + r.status + ").";
  const linhas = [d.mensagem];
  if (d.campos) {
    for (const [campo, msg] of Object.entries(d.campos)) {
      linhas.push(campo + ": " + msg);
    }
  }
  return linhas.join("\n");
}

async function executar(idMsg, acao) {
  limpar(idMsg);
  try {
    await acao();
  } catch (e) {
    mostrar(idMsg, "Falha ao falar com a API em " + API + " (" + e.message + ").", false);
  }
}

function inteiro(texto) {
  const t = texto.trim();
  return /^\d+$/.test(t) ? Number(t) : null;
}

/* ---------- Sessão ---------- */

function entrarNoApp(profissional) {
  $("tela-login").hidden = true;
  $("tela-app").hidden = false;
  $("sessao").hidden = false;
  $("perfil").textContent = profissional ? "Perfil: profissional" : "Perfil: leitor (somente leitura)";
  document.querySelectorAll("fieldset.escrita").forEach((f) => { f.disabled = !profissional; });
  carregarAlertas();
}

function sair() {
  credenciais = null;
  $("tela-app").hidden = true;
  $("sessao").hidden = true;
  $("tela-login").hidden = false;
  $("l-senha").value = "";
  $("lista-alertas").replaceChildren();
  $("resultado-historico").replaceChildren();
  document.querySelectorAll(".msg").forEach((m) => { m.textContent = ""; });
}

$("form-login").addEventListener("submit", (ev) => {
  ev.preventDefault();
  executar("msg-login", async () => {
    const usuario = $("l-usuario").value.trim();
    const senha = $("l-senha").value;
    if (!usuario || !senha) {
      mostrar("msg-login", "Informe usuário e senha.", false);
      return;
    }
    credenciais = basic(usuario, senha);

    const leitura = await chamar("/alertas");
    if (!leitura.ok) {
      credenciais = null;
      mostrar("msg-login", leitura.status === 401 ? "Usuário ou senha inválidos." : textoDoErro(leitura), false);
      return;
    }

    const sonda = await chamar("/pacientes", "POST", {});
    entrarNoApp(sonda.status !== 403);
  });
});

$("btn-sair").addEventListener("click", sair);

/* ---------- Alertas ---------- */

async function carregarAlertas() {
  await executar("msg-alertas", async () => {
    const r = await chamar("/alertas");
    if (!r.ok) {
      mostrar("msg-alertas", textoDoErro(r), false);
      return;
    }
    if (!Array.isArray(r.dados)) {
      mostrar("msg-alertas", "Formato inesperado em GET /alertas (esperava uma lista).", false);
      return;
    }
    const lista = $("lista-alertas");
    lista.replaceChildren();
    if (r.dados.length === 0) {
      mostrar("msg-alertas", "Nenhum alerta registrado.", true);
      return;
    }
    r.dados.forEach((a) => lista.appendChild(itemAlerta(a)));
  });
}

function itemAlerta(a) {
  const li = criar("li", undefined, "alerta");
  li.appendChild(criar("strong", a.regra));
  li.appendChild(criar("span", a.justificativa));
  li.appendChild(criar("small", formatarData(a.geradoEm)));
  return li;
}

$("btn-alertas").addEventListener("click", carregarAlertas);

/* ---------- Histórico ---------- */

$("form-historico").addEventListener("submit", (ev) => {
  ev.preventDefault();
  executar("msg-historico", async () => {
    const id = inteiro($("h-id").value);
    if (id === null || id < 1) {
      mostrar("msg-historico", "Informe um ID numérico válido.", false);
      return;
    }
    const r = await chamar("/pacientes/" + id + "/historico");
    const destino = $("resultado-historico");
    destino.replaceChildren();
    if (!r.ok) {
      mostrar("msg-historico", textoDoErro(r), false);
      return;
    }
    renderHistorico(destino, r.dados);
  });
});

function renderHistorico(destino, h) {
  const p = h.paciente;
  destino.appendChild(criar("h3", p.nome + " (ID " + p.id + ")"));
  destino.appendChild(criar("small", "CPF " + p.cpf + " | Nascimento " + p.dataNascimento));

  destino.appendChild(criar("h3", "Registros clínicos"));
  const registros = criar("ul", undefined, "lista");
  if (h.registros.length === 0) registros.appendChild(criar("li", "Nenhum registro."));
  h.registros.forEach((r) => {
    const li = criar("li");
    li.appendChild(criar("strong", r.tipo + ": " + r.descricao));
    li.appendChild(criar("span", r.notaClinica ? "Nota: " + r.notaClinica : "Sem nota clínica."));
    li.appendChild(criar("small", formatarData(r.registradoEm)));
    registros.appendChild(li);
  });
  destino.appendChild(registros);

  destino.appendChild(criar("h3", "Sinais vitais"));
  const sinais = criar("ul", undefined, "lista");
  if (h.sinais.length === 0) sinais.appendChild(criar("li", "Nenhuma leitura."));
  h.sinais.forEach((s) => {
    const li = criar("li");
    li.appendChild(criar("strong", s.pressaoSistolica + "/" + s.pressaoDiastolica + " mmHg, " + s.frequenciaCardiaca + " bpm"));
    li.appendChild(criar("small", formatarData(s.instante)));
    sinais.appendChild(li);
  });
  destino.appendChild(sinais);
}

/* ---------- Cadastro de paciente ---------- */

$("form-paciente").addEventListener("submit", (ev) => {
  ev.preventDefault();
  executar("msg-paciente", async () => {
    const nome = $("p-nome").value.trim();
    const data = $("p-data").value;
    const cpf = $("p-cpf").value.trim();
    const erros = [];
    if (!nome) erros.push("Nome é obrigatório.");
    if (!data) erros.push("Data de nascimento é obrigatória.");
    else if (data >= new Date().toISOString().slice(0, 10)) erros.push("Data de nascimento deve estar no passado.");
    if (!/^\d{11}$/.test(cpf)) erros.push("CPF deve conter 11 dígitos numéricos.");
    if (erros.length > 0) {
      mostrar("msg-paciente", erros.join("\n"), false);
      return;
    }
    const r = await chamar("/pacientes", "POST", { nome, dataNascimento: data, cpf });
    if (!r.ok) {
      mostrar("msg-paciente", textoDoErro(r), false);
      return;
    }
    mostrar("msg-paciente", "Paciente cadastrado com ID " + r.dados.id + ".", true);
    $("form-paciente").reset();
  });
});

/* ---------- Sinais vitais ---------- */

$("form-sinais").addEventListener("submit", (ev) => {
  ev.preventDefault();
  executar("msg-sinais", async () => {
    const id = inteiro($("s-id").value);
    const sis = inteiro($("s-sis").value);
    const dia = inteiro($("s-dia").value);
    const fc = inteiro($("s-fc").value);
    const erros = [];
    if (id === null || id < 1) erros.push("ID do paciente inválido.");
    if (sis === null || sis < 40 || sis > 300) erros.push("Sistólica deve estar entre 40 e 300 mmHg.");
    if (dia === null || dia < 20 || dia > 200) erros.push("Diastólica deve estar entre 20 e 200 mmHg.");
    if (sis !== null && dia !== null && sis <= dia) erros.push("Sistólica deve ser maior que a diastólica.");
    if (fc === null || fc < 20 || fc > 300) erros.push("Frequência cardíaca deve estar entre 20 e 300 bpm.");
    if (erros.length > 0) {
      mostrar("msg-sinais", erros.join("\n"), false);
      return;
    }
    const r = await chamar("/pacientes/" + id + "/sinais", "POST", {
      pressaoSistolica: sis,
      pressaoDiastolica: dia,
      frequenciaCardiaca: fc
    });
    if (!r.ok) {
      mostrar("msg-sinais", textoDoErro(r), false);
      return;
    }
    const alertas = r.dados.alertas;
    const texto = alertas.length === 0
      ? "Leitura registrada. Nenhum alerta disparado."
      : "Leitura registrada. ALERTA: " + alertas.map((a) => a.justificativa).join(" | ");
    mostrar("msg-sinais", texto, alertas.length === 0);
    carregarAlertas();
  });
});

/* ---------- Registro clínico ---------- */

$("r-tipo").addEventListener("change", () => {
  const exame = $("r-tipo").value === "EXAME";
  $("campos-exame").hidden = !exame;
  $("campos-receita").hidden = exame;
});

$("form-registro").addEventListener("submit", (ev) => {
  ev.preventDefault();
  executar("msg-registro", async () => {
    const id = inteiro($("r-id").value);
    const tipo = $("r-tipo").value;
    const nota = $("r-nota").value.trim();
    const corpo = { tipo, notaClinica: nota === "" ? null : nota };
    const erros = [];
    if (id === null || id < 1) erros.push("ID do paciente inválido.");
    if (tipo === "EXAME") {
      corpo.tipoExame = $("r-tipo-exame").value.trim();
      corpo.resultado = $("r-resultado").value.trim();
      if (!corpo.tipoExame) erros.push("Tipo de exame é obrigatório.");
    } else {
      corpo.medicamento = $("r-medicamento").value.trim();
      corpo.posologia = $("r-posologia").value.trim();
      if (!corpo.medicamento) erros.push("Medicamento é obrigatório.");
    }
    if (erros.length > 0) {
      mostrar("msg-registro", erros.join("\n"), false);
      return;
    }
    const r = await chamar("/pacientes/" + id + "/registros", "POST", corpo);
    if (!r.ok) {
      mostrar("msg-registro", textoDoErro(r), false);
      return;
    }
    mostrar("msg-registro", "Registro criado: " + r.dados.descricao + " (ID " + r.dados.id + ").", true);
    $("form-registro").reset();
    $("campos-exame").hidden = false;
    $("campos-receita").hidden = true;
  });
});