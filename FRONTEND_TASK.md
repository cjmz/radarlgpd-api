# 📋 Task: Integração Plugin WordPress com API Radar LGPD

## 🎯 Objetivo

Implementar a integração do plugin WordPress **Radar LGPD Scanner** com a API backend para envio de resultados de varredura de dados pessoais sensíveis.

---

## 📝 Contexto

O plugin WordPress deve:
1. Varrer o banco de dados MySQL/MariaDB em busca de dados pessoais (CPF, email, telefone, etc.)
2. Agregar os resultados (apenas contagens, **NUNCA** dados reais)
3. Enviar os resultados para a API via HTTP POST
4. Exibir feedback ao usuário sobre o status do envio

---

## 🔧 Configuração Necessária

### Endpoint da API

```
Produção: https://radarlgpd-api.onrender.com
Desenvolvimento: http://localhost:8080
```

### Credenciais de Acesso

**API Key**: Será fornecida pelo administrador do sistema.

> ⚠️ **IMPORTANTE**: A API Key é sensível e deve ser armazenada de forma segura no WordPress (usar `wp_options` ou similar).

---

## 📡 Especificação da API

### Endpoint: `POST /v1/telemetry/scan-result`

#### Headers Obrigatórios

```http
Authorization: Bearer {API_KEY}
Content-Type: application/json
```

#### Payload Obrigatório (JSON)

```json
{
  "scan_id": "string (UUID v4)",
  "site_id": "string (identificador único do site)",
  "consent_given": true,
  "scan_timestamp_utc": "string (ISO 8601 UTC)",
  "scan_duration_ms": 1500,
  "scanner_version": "string (ex: 1.0.0)",
  "environment": {
    "wordpress_version": "string",
    "php_version": "string",
    "mysql_version": "string (opcional)"
  },
  "results": [
    {
      "data_type": "string (CPF|EMAIL|TELEFONE|RG|CNH|PASSAPORTE)",
      "source_location": "string (tabela.coluna)",
      "count": 0
    }
  ]
}
```

#### Exemplo Completo de Payload

```json
{
  "scan_id": "123e4567-e89b-12d3-a456-426614174000",
  "site_id": "site-12345-hash",
  "consent_given": true,
  "scan_timestamp_utc": "2025-10-20T18:30:00Z",
  "scan_duration_ms": 1500,
  "scanner_version": "1.0.0",
  "environment": {
    "wordpress_version": "6.4.0",
    "php_version": "8.2.0",
    "mysql_version": "8.0.35"
  },
  "results": [
    {
      "data_type": "CPF",
      "source_location": "wp_comments.comment_content",
      "count": 152
    },
    {
      "data_type": "EMAIL",
      "source_location": "wp_users.user_email",
      "count": 310
    },
    {
      "data_type": "TELEFONE",
      "source_location": "wp_postmeta.meta_value",
      "count": 89
    }
  ]
}
```

---

## ✅ Regras de Validação

### 1. **scan_id** (Obrigatório)
- Tipo: String
- Formato: UUID v4 (ex: `123e4567-e89b-12d3-a456-426614174000`)
- Gerar um novo UUID para cada varredura
- PHP: `wp_generate_uuid4()`

### 2. **site_id** (Obrigatório)
- Tipo: String
- Identificador único do site WordPress
- Sugestão: usar hash SHA256 do domínio
- PHP: `hash('sha256', get_site_url())`

### 3. **consent_given** (Obrigatório)
- Tipo: Boolean
- **DEVE ser `true`**
- Se `false`: API retorna **403 Forbidden**
- Usuário deve aceitar termos antes de enviar dados

### 4. **scan_timestamp_utc** (Obrigatório)
- Tipo: String
- Formato: ISO 8601 UTC (ex: `2025-10-20T18:30:00Z`)
- PHP: `gmdate('Y-m-d\TH:i:s\Z')`

### 5. **scan_duration_ms** (Obrigatório)
- Tipo: Integer
- Duração da varredura em milissegundos
- Mínimo: 0

### 6. **scanner_version** (Obrigatório)
- Tipo: String
- Versão do plugin (SemVer recomendado)
- Ex: `1.0.0`, `1.2.3-beta`

### 7. **environment** (Obrigatório)
- `wordpress_version`: Versão do WordPress
- `php_version`: Versão do PHP
- `mysql_version`: Versão do MySQL (opcional)

### 8. **results** (Obrigatório, mínimo 1 item)
- Array de objetos
- Cada objeto deve conter:
  - `data_type`: Tipo de dado encontrado
  - `source_location`: Onde foi encontrado (formato: `tabela.coluna`)
  - `count`: Quantidade encontrada (>= 0)

#### Tipos de Dados Aceitos (`data_type`)

```
CPF
EMAIL
TELEFONE
RG
CNH
PASSAPORTE
CARTAO_CREDITO
ENDERECO
DATA_NASCIMENTO
```

---

## 📥 Respostas da API

### ✅ Sucesso (200 OK)

```json
{
  "scan_id": "123e4567-e89b-12d3-a456-426614174000",
  "status": "SUCCESS",
  "message": "Scan processado com sucesso",
  "received_at": "2025-10-20T18:30:05Z"
}
```

### ❌ Erros Possíveis

#### 400 Bad Request - Payload Inválido

```json
{
  "timestamp": "2025-10-20T18:30:05-03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "scan_id",
      "message": "scan_id deve ser um UUID válido"
    }
  ]
}
```

#### 401 Unauthorized - API Key Inválida

```json
{
  "timestamp": "2025-10-20T18:30:05-03:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "API Key inválida ou ausente"
}
```

#### 403 Forbidden - Consentimento Não Dado

```json
{
  "timestamp": "2025-10-20T18:30:05-03:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Consentimento não foi concedido. É obrigatório consentimento do usuário para processar dados de telemetria."
}
```

#### 429 Too Many Requests - Rate Limit Excedido

```json
{
  "timestamp": "2025-10-20T18:30:05-03:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Limite de 100 requisições por hora excedido"
}
```

#### 500 Internal Server Error

```json
{
  "timestamp": "2025-10-20T18:30:05-03:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Erro ao processar requisição. Tente novamente mais tarde."
}
```

---

## 🔒 Requisitos de Segurança

### ⚠️ CRÍTICO: Privacidade e LGPD

**NUNCA ENVIAR DADOS REAIS!**

❌ **PROIBIDO:**
```json
{
  "results": [
    {
      "data_type": "CPF",
      "values": ["123.456.789-00", "987.654.321-00"]  // ❌ NUNCA!
    }
  ]
}
```

✅ **CORRETO:**
```json
{
  "results": [
    {
      "data_type": "CPF",
      "source_location": "wp_comments.comment_content",
      "count": 152  // ✅ Apenas contagem
    }
  ]
}
```

### Checklist de Segurança

- [ ] API Key armazenada de forma segura (nunca no código)
- [ ] Enviar apenas contagens agregadas
- [ ] Validar consentimento do usuário antes de enviar
- [ ] Usar HTTPS em produção
- [ ] Tratar erros sem expor informações sensíveis
- [ ] Implementar retry com backoff exponencial

---

## 💻 Exemplo de Implementação PHP (WordPress)

### 1. Configuração do Plugin

```php
<?php
// settings-page.php

// Salvar API Key e URL
function radarlgpd_save_settings() {
    if (isset($_POST['radarlgpd_api_key'])) {
        update_option('radarlgpd_api_key', sanitize_text_field($_POST['radarlgpd_api_key']));
    }
    
    if (isset($_POST['radarlgpd_api_url'])) {
        update_option('radarlgpd_api_url', esc_url_raw($_POST['radarlgpd_api_url']));
    }
}

// Formulário de configuração
function radarlgpd_settings_page() {
    ?>
    <div class="wrap">
        <h1>Radar LGPD - Configurações</h1>
        <form method="post" action="">
            <table class="form-table">
                <tr>
                    <th scope="row"><label>API URL</label></th>
                    <td>
                        <input type="url" name="radarlgpd_api_url" 
                               value="<?php echo esc_attr(get_option('radarlgpd_api_url', 'https://radarlgpd-api.onrender.com')); ?>" 
                               class="regular-text" required>
                    </td>
                </tr>
                <tr>
                    <th scope="row"><label>API Key</label></th>
                    <td>
                        <input type="password" name="radarlgpd_api_key" 
                               value="<?php echo esc_attr(get_option('radarlgpd_api_key')); ?>" 
                               class="regular-text" required>
                        <p class="description">Chave fornecida pelo administrador da API</p>
                    </td>
                </tr>
            </table>
            <?php submit_button('Salvar Configurações'); ?>
        </form>
    </div>
    <?php
}
```

### 2. Função de Envio para API

```php
<?php
// api-client.php

/**
 * Envia resultados do scan para a API Radar LGPD
 *
 * @param array $scan_results Resultados do scan
 * @return array|WP_Error Resposta da API ou erro
 */
function radarlgpd_send_scan_results($scan_results) {
    // 1. Obter configurações
    $api_url = get_option('radarlgpd_api_url', 'https://radarlgpd-api.onrender.com');
    $api_key = get_option('radarlgpd_api_key');
    
    if (empty($api_key)) {
        return new WP_Error('no_api_key', 'API Key não configurada');
    }
    
    // 2. Preparar payload
    $payload = [
        'scan_id' => wp_generate_uuid4(),
        'site_id' => hash('sha256', get_site_url()),
        'consent_given' => true, // Deve ser validado antes!
        'scan_timestamp_utc' => gmdate('Y-m-d\TH:i:s\Z'),
        'scan_duration_ms' => $scan_results['duration_ms'],
        'scanner_version' => RADARLGPD_VERSION, // Definir constante
        'environment' => [
            'wordpress_version' => get_bloginfo('version'),
            'php_version' => PHP_VERSION,
            'mysql_version' => radarlgpd_get_mysql_version(),
        ],
        'results' => $scan_results['data_findings']
    ];
    
    // 3. Fazer requisição
    $response = wp_remote_post($api_url . '/v1/telemetry/scan-result', [
        'headers' => [
            'Authorization' => 'Bearer ' . $api_key,
            'Content-Type' => 'application/json',
        ],
        'body' => wp_json_encode($payload),
        'timeout' => 30,
        'sslverify' => true, // IMPORTANTE: verificar SSL em produção
    ]);
    
    // 4. Tratar resposta
    if (is_wp_error($response)) {
        return $response;
    }
    
    $status_code = wp_remote_retrieve_response_code($response);
    $body = json_decode(wp_remote_retrieve_body($response), true);
    
    // 5. Validar resposta
    if ($status_code === 200) {
        return [
            'success' => true,
            'data' => $body
        ];
    }
    
    // Erro da API
    return new WP_Error(
        'api_error',
        $body['message'] ?? 'Erro desconhecido',
        ['status' => $status_code, 'response' => $body]
    );
}

/**
 * Obtém versão do MySQL
 */
function radarlgpd_get_mysql_version() {
    global $wpdb;
    return $wpdb->get_var("SELECT VERSION()");
}
```

### 3. Exemplo de Uso

```php
<?php
// scanner.php

function radarlgpd_run_scan() {
    // 1. Validar consentimento
    if (!radarlgpd_has_user_consent()) {
        wp_die('É necessário consentimento para realizar o scan.');
    }
    
    // 2. Executar scan (implementar lógica de varredura)
    $start_time = microtime(true);
    $findings = radarlgpd_scan_database();
    $duration_ms = (int)((microtime(true) - $start_time) * 1000);
    
    // 3. Preparar resultados
    $scan_results = [
        'duration_ms' => $duration_ms,
        'data_findings' => $findings // Array de objetos {data_type, source_location, count}
    ];
    
    // 4. Enviar para API
    $response = radarlgpd_send_scan_results($scan_results);
    
    // 5. Exibir resultado
    if (is_wp_error($response)) {
        add_settings_error(
            'radarlgpd_messages',
            'radarlgpd_error',
            'Erro ao enviar resultados: ' . $response->get_error_message(),
            'error'
        );
    } else {
        add_settings_error(
            'radarlgpd_messages',
            'radarlgpd_success',
            'Scan enviado com sucesso! ID: ' . $response['data']['scan_id'],
            'success'
        );
    }
}
```

---

## 🧪 Testes

### Teste de Conectividade

```php
<?php
/**
 * Testa conexão com a API
 */
function radarlgpd_test_connection() {
    $api_url = get_option('radarlgpd_api_url');
    $api_key = get_option('radarlgpd_api_key');
    
    // Testar endpoint de health
    $response = wp_remote_get($api_url . '/health', [
        'timeout' => 10,
        'sslverify' => true
    ]);
    
    if (is_wp_error($response)) {
        return [
            'success' => false,
            'message' => 'Erro de conexão: ' . $response->get_error_message()
        ];
    }
    
    $status = wp_remote_retrieve_response_code($response);
    
    if ($status === 200) {
        return [
            'success' => true,
            'message' => 'Conexão com a API estabelecida com sucesso!'
        ];
    }
    
    return [
        'success' => false,
        'message' => 'API retornou status: ' . $status
    ];
}
```

### Payload de Teste (Mínimo)

```php
<?php
$test_payload = [
    'scan_id' => wp_generate_uuid4(),
    'site_id' => 'test-site-id',
    'consent_given' => true,
    'scan_timestamp_utc' => gmdate('Y-m-d\TH:i:s\Z'),
    'scan_duration_ms' => 100,
    'scanner_version' => '1.0.0-test',
    'environment' => [
        'wordpress_version' => get_bloginfo('version'),
        'php_version' => PHP_VERSION
    ],
    'results' => [
        [
            'data_type' => 'EMAIL',
            'source_location' => 'wp_users.user_email',
            'count' => 1
        ]
    ]
];
```

---

## 📚 Recursos Adicionais

### Documentação da API

- Endpoint Health Check: `GET /health`
- Endpoint principal: `POST /v1/telemetry/scan-result`

### URLs Importantes

- **Produção**: https://radarlgpd-api.onrender.com
- **Documentação**: (incluir link quando disponível)
- **Repositório**: https://github.com/cjmz/radarlgpd-api

---

## ✅ Critérios de Aceitação

- [ ] Plugin tem página de configuração para API URL e API Key
- [ ] API Key é armazenada de forma segura
- [ ] Consentimento do usuário é validado antes de enviar dados
- [ ] Payload é montado corretamente conforme especificação
- [ ] Erros da API são tratados e exibidos ao usuário
- [ ] Apenas contagens são enviadas (NUNCA dados reais)
- [ ] Implementado retry com backoff para erros temporários
- [ ] Logs de debug são criados (opcional, mas recomendado)
- [ ] Botão "Test Connection" funciona
- [ ] Interface exibe resultado do scan após envio

---

## 🐛 Troubleshooting

### Erro 401 - API Key Inválida

**Causa**: API Key incorreta ou não configurada.

**Solução**:
- Verificar se a key foi copiada corretamente (sem espaços extras)
- Confirmar que a key é a mesma configurada no backend

### Erro 403 - Consentimento Não Dado

**Causa**: Campo `consent_given` está `false`.

**Solução**:
- Implementar checkbox de consentimento na UI
- Validar consentimento antes de enviar

### Erro 429 - Rate Limit

**Causa**: Mais de 100 requisições por hora.

**Solução**:
- Implementar debounce no botão de scan
- Alertar usuário sobre o limite
- Aguardar 1 hora ou contatar suporte

### Erro 500 - Erro Interno

**Causa**: Problema no servidor da API.

**Solução**:
- Implementar retry automático (3 tentativas com backoff)
- Se persistir, reportar ao time de backend

---

## 📞 Contato

**Dúvidas técnicas**: Entre em contato com o time de backend.

**API Key**: Solicitar ao administrador do sistema.

---

**Versão da Especificação**: 1.0.0  
**Última Atualização**: 20/10/2025  
**Autor**: Time Radar LGPD Backend
