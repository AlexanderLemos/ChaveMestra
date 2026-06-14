# 🔒 ChaveMestra

Um cofre digital para guardar seus arquivos de forma segura no computador.

---

## O que é?

ChaveMestra é um programa desktop feito em Java que criptografa seus arquivos com senha. Você coloca seus arquivos no cofre e só consegue acessar com a senha mestra.

## O que ele faz?

- **Protege arquivos com senha** — você importa qualquer arquivo e ele fica criptografado
- **Abre arquivos temporariamente** — quando você precisa ver o arquivo, ele abre e se apaga sozinho depois de 30 segundos
- **Bloqueia após tentativas erradas** — se errar a senha 5 vezes, bloqueia por 1 minuto
- **Modo Pânico** — se errar 10 vezes, o cofre inteiro é apagado
- **Gerador de senhas** — gera senhas fortes aleatórias
- **Log de atividades** — registra tudo que acontece (login, arquivos abertos, etc.)
- **Bloqueio automático** — se ficar 5 min sem usar, bloqueia sozinho
- **Suporte PT-BR e Inglês** — troca o idioma pelo botão 🌐

---

## Tecnologias usadas

- Java 21
- JavaFX (interface gráfica)
- AES-256-GCM (criptografia)
- PBKDF2 (hash de senha)
- Jackson (JSON)
- Maven (build)

---

## Como rodar

### Requisitos
- Java 21 instalado

### Rodando
```bash
cd ChaveMestra
mvnw.cmd javafx:run
```

---

## Estrutura de pastas

```
src/main/java/com/chavemestra/
├── app/           → entrada do programa
├── controller/    → lógica de login e do cofre
├── crypto/        → criptografia e hash
├── model/         → classes de dados (User, VaultFile, etc.)
├── security/      → proteção contra força bruta, sessão, modo pânico
├── storage/       → salvar/carregar dados (JSON e arquivos)
├── ui/            → telas (login, dashboard, gerador de senhas)
└── utils/         → funções auxiliares
```

---

## Como funciona (resumo)

1. Você cria uma senha mestra na primeira vez
2. Importa arquivos → eles são criptografados e salvos
3. Para abrir → o arquivo é descriptografado temporariamente e apagado depois
4. Tudo fica salvo em `C:\Users\<usuario>\.chavemestra\`

---

## Autor

Projeto de portfólio — aplicação de segurança desktop.
