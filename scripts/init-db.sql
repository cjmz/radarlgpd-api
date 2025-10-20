-- Script de inicialização do banco de dados Radar LGPD
-- Este script é executado automaticamente na primeira vez que o container é criado

-- Garantir que estamos usando o banco correto
\c radarlgpd;

-- Criar extensão para UUID (caso precise no futuro)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Criar extensão para funções de criptografia (útil para LGPD)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Mensagem de sucesso
DO $$
BEGIN
    RAISE NOTICE '✅ Banco de dados Radar LGPD inicializado com sucesso!';
    RAISE NOTICE '📊 Database: radarlgpd';
    RAISE NOTICE '👤 User: radarlgpd_user';
    RAISE NOTICE '🔧 Extensões instaladas: uuid-ossp, pgcrypto';
END $$;
