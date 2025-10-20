#!/bin/bash

# Script de ajuda para gerenciar o banco de dados PostgreSQL
# Uso: ./scripts/db-helper.sh [comando]

set -e

CONTAINER_NAME="radarlgpd-postgres"
DB_NAME="radarlgpd"
DB_USER="radarlgpd_user"

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

print_help() {
    echo -e "${GREEN}Radar LGPD - Database Helper${NC}"
    echo ""
    echo "Comandos disponíveis:"
    echo "  start       - Inicia o container PostgreSQL"
    echo "  stop        - Para o container PostgreSQL"
    echo "  restart     - Reinicia o container PostgreSQL"
    echo "  status      - Mostra o status do container"
    echo "  logs        - Mostra os logs do PostgreSQL"
    echo "  connect     - Conecta ao PostgreSQL via psql"
    echo "  reset       - Para e remove o container e volumes (CUIDADO!)"
    echo "  backup      - Cria um backup do banco de dados"
    echo "  restore     - Restaura um backup (requer caminho do arquivo)"
    echo ""
    echo "Exemplos:"
    echo "  ./scripts/db-helper.sh start"
    echo "  ./scripts/db-helper.sh logs"
    echo "  ./scripts/db-helper.sh backup"
}

start_db() {
    echo -e "${GREEN}🚀 Iniciando PostgreSQL...${NC}"
    docker compose up -d postgres
    echo -e "${GREEN}✅ PostgreSQL iniciado!${NC}"
    echo -e "${YELLOW}📊 Aguardando banco ficar pronto...${NC}"
    sleep 5
    docker compose ps
}

stop_db() {
    echo -e "${YELLOW}⏹️  Parando PostgreSQL...${NC}"
    docker compose stop postgres
    echo -e "${GREEN}✅ PostgreSQL parado!${NC}"
}

restart_db() {
    echo -e "${YELLOW}🔄 Reiniciando PostgreSQL...${NC}"
    docker compose restart postgres
    echo -e "${GREEN}✅ PostgreSQL reiniciado!${NC}"
}

status_db() {
    echo -e "${GREEN}📊 Status dos containers:${NC}"
    docker compose ps
}

logs_db() {
    echo -e "${GREEN}📝 Logs do PostgreSQL:${NC}"
    docker compose logs -f postgres
}

connect_db() {
    echo -e "${GREEN}🔌 Conectando ao PostgreSQL...${NC}"
    docker exec -it $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME
}

reset_db() {
    echo -e "${RED}⚠️  ATENÇÃO: Isso irá DELETAR todos os dados do banco!${NC}"
    read -p "Tem certeza? (digite 'sim' para confirmar): " confirm
    if [ "$confirm" = "sim" ]; then
        echo -e "${YELLOW}🗑️  Removendo containers e volumes...${NC}"
        docker compose down -v
        echo -e "${GREEN}✅ Banco de dados resetado!${NC}"
    else
        echo -e "${YELLOW}❌ Operação cancelada.${NC}"
    fi
}

backup_db() {
    BACKUP_DIR="./backups"
    mkdir -p $BACKUP_DIR
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    BACKUP_FILE="$BACKUP_DIR/radarlgpd_backup_$TIMESTAMP.sql"
    
    echo -e "${GREEN}💾 Criando backup...${NC}"
    docker exec -t $CONTAINER_NAME pg_dump -U $DB_USER -d $DB_NAME > $BACKUP_FILE
    echo -e "${GREEN}✅ Backup criado: $BACKUP_FILE${NC}"
}

restore_db() {
    if [ -z "$2" ]; then
        echo -e "${RED}❌ Erro: Especifique o caminho do arquivo de backup${NC}"
        echo "Uso: ./scripts/db-helper.sh restore <caminho-do-backup.sql>"
        exit 1
    fi
    
    BACKUP_FILE=$2
    if [ ! -f "$BACKUP_FILE" ]; then
        echo -e "${RED}❌ Arquivo não encontrado: $BACKUP_FILE${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}📥 Restaurando backup: $BACKUP_FILE${NC}"
    docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < $BACKUP_FILE
    echo -e "${GREEN}✅ Backup restaurado!${NC}"
}

# Main
case "$1" in
    start)
        start_db
        ;;
    stop)
        stop_db
        ;;
    restart)
        restart_db
        ;;
    status)
        status_db
        ;;
    logs)
        logs_db
        ;;
    connect)
        connect_db
        ;;
    reset)
        reset_db
        ;;
    backup)
        backup_db
        ;;
    restore)
        restore_db "$@"
        ;;
    *)
        print_help
        ;;
esac
