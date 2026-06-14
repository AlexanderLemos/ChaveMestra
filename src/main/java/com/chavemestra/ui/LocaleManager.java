package com.chavemestra.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LocaleManager {

    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale PORTUGUESE = Locale.of("pt", "BR");

    private static final LocaleManager INSTANCE = new LocaleManager();

    private final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>(ENGLISH);
    private final Map<Locale, Map<String, String>> translations = new HashMap<>();

    private LocaleManager() {
        translations.put(ENGLISH, buildEnglish());
        translations.put(PORTUGUESE, buildPortuguese());
    }

    public static LocaleManager getInstance() {
        return INSTANCE;
    }

    public String get(String key) {
        Map<String, String> bundle = translations.get(currentLocale.get());
        if (bundle == null) {
            bundle = translations.get(ENGLISH);
        }
        return bundle.getOrDefault(key, key);
    }

    public void setLocale(Locale locale) {
        currentLocale.set(locale);
    }

    public Locale getLocale() {
        return currentLocale.get();
    }

    public ObjectProperty<Locale> localeProperty() {
        return currentLocale;
    }

    public void toggleLocale() {
        if (currentLocale.get().equals(ENGLISH)) {
            currentLocale.set(PORTUGUESE);
        } else {
            currentLocale.set(ENGLISH);
        }
    }

    public String getLocaleDisplayCode() {
        return currentLocale.get().equals(ENGLISH) ? "EN" : "PT";
    }

    private Map<String, String> buildEnglish() {
        Map<String, String> m = new HashMap<>();

        m.put("app.name", "ChaveMestra");
        m.put("app.version", "v1.0.0 — AES-256-GCM · PBKDF2-SHA256");

        m.put("login.title", "ChaveMestra");
        m.put("login.subtitle.register", "Create your master password to begin");
        m.put("login.subtitle.login", "Enter your master password to unlock");
        m.put("login.password.prompt", "Master Password");
        m.put("login.password.confirm", "Confirm Master Password");
        m.put("login.button.create", "Create Vault");
        m.put("login.button.unlock", "Unlock");
        m.put("login.error.empty", "Please enter a password");
        m.put("login.error.short", "Password must be at least 8 characters");
        m.put("login.error.mismatch", "Passwords do not match");
        m.put("login.error.no_account", "No account found. Please register first.");
        m.put("login.error.incorrect", "Incorrect password");
        m.put("login.error.auth", "Authentication error");
        m.put("login.success.created", "Vault created successfully!");
        m.put("login.success.granted", "Access granted");
        m.put("login.attempts.remaining", " attempt(s) remaining");
        m.put("login.lockout.prefix", "⏳ Locked — ");
        m.put("login.lockout.suffix", "s remaining");
        m.put("login.lockout.message", "Too many failed attempts. Locked for ");
        m.put("login.lockout.seconds", " seconds.");
        m.put("login.panic.message", "SECURITY ALERT: Maximum attempts exceeded. Vault has been destroyed.");
        m.put("login.failed.save", "Failed to save credentials: ");

        m.put("dash.files", " files");
        m.put("dash.file", " file");
        m.put("dash.add", "+ Add File");
        m.put("dash.open", "Open");
        m.put("dash.delete", "Delete");
        m.put("dash.refresh", "↻ Refresh");
        m.put("dash.lock", "Lock");
        m.put("dash.tooltip.audit", "Audit Log");
        m.put("dash.tooltip.passgen", "Password Generator");
        m.put("dash.status.ready", "Ready");
        m.put("dash.status.encrypting", "Encrypting ");
        m.put("dash.status.decrypting", "Decrypting ");
        m.put("dash.status.encrypted", "Encrypted: ");
        m.put("dash.status.opened", "Opened: ");
        m.put("dash.status.deleted", "Deleted: ");
        m.put("dash.status.error", "Error: ");
        m.put("dash.status.autodelete", " (auto-deletes in 30s)");
        m.put("dash.status.load_error", "Error loading files: ");
        m.put("dash.empty.title", "Your vault is empty");
        m.put("dash.empty.subtitle", "Drag and drop files here or click \"Add File\" to encrypt and store files securely");
        m.put("dash.filechooser.title", "Select File to Encrypt");
        m.put("dash.filechooser.filter", "All Files");
        m.put("dash.delete.title", "Delete File");
        m.put("dash.delete.header", "Permanently delete ");
        m.put("dash.delete.content", "This action cannot be undone. The encrypted file will be securely destroyed.");
        m.put("dash.encrypted", "🔒 Encrypted");
        m.put("dash.statusbar.encryption", "🔒 AES-256-GCM");
        m.put("dash.statusbar.kdf", "PBKDF2-SHA256 · 600K iterations");
        m.put("dash.audit.title", "📋 Audit Log");
        m.put("dash.session.expired", "Session expired. Please re-authenticate.");

        m.put("passgen.title", "Password Generator");
        m.put("passgen.length", "Length");
        m.put("passgen.copy", "📋 Copy");
        m.put("passgen.regenerate", "↻ Regenerate");
        m.put("passgen.copied", "Copied!");
        m.put("passgen.strength", "Strength");
        m.put("passgen.strength.weak", "Weak");
        m.put("passgen.strength.fair", "Fair");
        m.put("passgen.strength.good", "Good");
        m.put("passgen.strength.strong", "Strong");

        m.put("audit.login_success", "LOGIN SUCCESS");
        m.put("audit.login_failed", "LOGIN FAILED");
        m.put("audit.file_encrypted", "FILE ENCRYPTED");
        m.put("audit.file_decrypted", "FILE DECRYPTED");
        m.put("audit.file_deleted", "FILE DELETED");
        m.put("audit.file_opened", "FILE OPENED");
        m.put("audit.vault_locked", "VAULT LOCKED");
        m.put("audit.vault_unlocked", "VAULT UNLOCKED");
        m.put("audit.panic_triggered", "PANIC TRIGGERED");
        m.put("audit.password_generated", "PASSWORD GENERATED");
        m.put("audit.account_created", "ACCOUNT CREATED");

        m.put("lang.toggle", "🌐");
        m.put("lang.tooltip", "Language / Idioma");

        return m;
    }

    private Map<String, String> buildPortuguese() {
        Map<String, String> m = new HashMap<>();

        m.put("app.name", "ChaveMestra");
        m.put("app.version", "v1.0.0 — AES-256-GCM · PBKDF2-SHA256");

        m.put("login.title", "ChaveMestra");
        m.put("login.subtitle.register", "Crie sua senha mestra para começar");
        m.put("login.subtitle.login", "Digite sua senha mestra para desbloquear");
        m.put("login.password.prompt", "Senha Mestra");
        m.put("login.password.confirm", "Confirmar Senha Mestra");
        m.put("login.button.create", "Criar Cofre");
        m.put("login.button.unlock", "Desbloquear");
        m.put("login.error.empty", "Por favor, digite uma senha");
        m.put("login.error.short", "A senha deve ter pelo menos 8 caracteres");
        m.put("login.error.mismatch", "As senhas não coincidem");
        m.put("login.error.no_account", "Nenhuma conta encontrada. Registre-se primeiro.");
        m.put("login.error.incorrect", "Senha incorreta");
        m.put("login.error.auth", "Erro de autenticação");
        m.put("login.success.created", "Cofre criado com sucesso!");
        m.put("login.success.granted", "Acesso concedido");
        m.put("login.attempts.remaining", " tentativa(s) restante(s)");
        m.put("login.lockout.prefix", "⏳ Bloqueado — ");
        m.put("login.lockout.suffix", "s restantes");
        m.put("login.lockout.message", "Muitas tentativas falhas. Bloqueado por ");
        m.put("login.lockout.seconds", " segundos.");
        m.put("login.panic.message", "ALERTA DE SEGURANÇA: Número máximo de tentativas excedido. O cofre foi destruído.");
        m.put("login.failed.save", "Falha ao salvar credenciais: ");

        m.put("dash.files", " arquivos");
        m.put("dash.file", " arquivo");
        m.put("dash.add", "+ Adicionar Arquivo");
        m.put("dash.open", "Abrir");
        m.put("dash.delete", "Excluir");
        m.put("dash.refresh", "↻ Atualizar");
        m.put("dash.lock", "Bloquear");
        m.put("dash.tooltip.audit", "Log de Auditoria");
        m.put("dash.tooltip.passgen", "Gerador de Senhas");
        m.put("dash.status.ready", "Pronto");
        m.put("dash.status.encrypting", "Criptografando ");
        m.put("dash.status.decrypting", "Descriptografando ");
        m.put("dash.status.encrypted", "Criptografado: ");
        m.put("dash.status.opened", "Aberto: ");
        m.put("dash.status.deleted", "Excluído: ");
        m.put("dash.status.error", "Erro: ");
        m.put("dash.status.autodelete", " (exclusão automática em 30s)");
        m.put("dash.status.load_error", "Erro ao carregar arquivos: ");
        m.put("dash.empty.title", "Seu cofre está vazio");
        m.put("dash.empty.subtitle", "Arraste e solte arquivos aqui ou clique em \"Adicionar Arquivo\" para criptografar e armazenar arquivos com segurança");
        m.put("dash.filechooser.title", "Selecionar Arquivo para Criptografar");
        m.put("dash.filechooser.filter", "Todos os Arquivos");
        m.put("dash.delete.title", "Excluir Arquivo");
        m.put("dash.delete.header", "Excluir permanentemente ");
        m.put("dash.delete.content", "Esta ação não pode ser desfeita. O arquivo criptografado será destruído com segurança.");
        m.put("dash.encrypted", "🔒 Criptografado");
        m.put("dash.statusbar.encryption", "🔒 AES-256-GCM");
        m.put("dash.statusbar.kdf", "PBKDF2-SHA256 · 600K iterações");
        m.put("dash.audit.title", "📋 Log de Auditoria");
        m.put("dash.session.expired", "Sessão expirada. Por favor, autentique-se novamente.");

        m.put("passgen.title", "Gerador de Senhas");
        m.put("passgen.length", "Tamanho");
        m.put("passgen.copy", "📋 Copiar");
        m.put("passgen.regenerate", "↻ Regenerar");
        m.put("passgen.copied", "Copiado!");
        m.put("passgen.strength", "Força");
        m.put("passgen.strength.weak", "Fraca");
        m.put("passgen.strength.fair", "Razoável");
        m.put("passgen.strength.good", "Boa");
        m.put("passgen.strength.strong", "Forte");

        m.put("audit.login_success", "LOGIN SUCESSO");
        m.put("audit.login_failed", "LOGIN FALHOU");
        m.put("audit.file_encrypted", "ARQUIVO CRIPTOGRAFADO");
        m.put("audit.file_decrypted", "ARQUIVO DESCRIPTOGRAFADO");
        m.put("audit.file_deleted", "ARQUIVO EXCLUÍDO");
        m.put("audit.file_opened", "ARQUIVO ABERTO");
        m.put("audit.vault_locked", "COFRE BLOQUEADO");
        m.put("audit.vault_unlocked", "COFRE DESBLOQUEADO");
        m.put("audit.panic_triggered", "PÂNICO ATIVADO");
        m.put("audit.password_generated", "SENHA GERADA");
        m.put("audit.account_created", "CONTA CRIADA");

        m.put("lang.toggle", "🌐");
        m.put("lang.tooltip", "Language / Idioma");

        return m;
    }
}
