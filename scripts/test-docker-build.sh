#!/bin/bash

# Script para testar o build Docker localmente antes do deploy no Render
# Uso: ./scripts/test-docker-build.sh

set -e

echo "🐳 Radar LGPD - Teste de Build Docker"
echo "======================================"
echo ""

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configurações
IMAGE_NAME="radarlgpd-api"
CONTAINER_NAME="radarlgpd-test"
PORT=8080

echo "${YELLOW}📦 Step 1: Limpando builds anteriores...${NC}"
docker rm -f $CONTAINER_NAME 2>/dev/null || true
docker rmi $IMAGE_NAME 2>/dev/null || true
echo "${GREEN}✓ Limpeza concluída${NC}"
echo ""

echo "${YELLOW}🔨 Step 2: Building Docker image...${NC}"
echo "Isso pode levar alguns minutos..."
docker build -t $IMAGE_NAME .
echo "${GREEN}✓ Build concluído com sucesso!${NC}"
echo ""

echo "${YELLOW}🚀 Step 3: Iniciando container...${NC}"
docker run -d \
  --name $CONTAINER_NAME \
  -p $PORT:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e PORT=8080 \
  -e RADARLGPD_API_KEY=test-api-key-local \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/radarlgpd \
  -e DB_USERNAME=radarlgpd_user \
  -e DB_PASSWORD=radarlgpd_dev_password \
  $IMAGE_NAME

echo "${GREEN}✓ Container iniciado!${NC}"
echo ""

echo "${YELLOW}⏳ Step 4: Aguardando aplicação iniciar...${NC}"
sleep 15

echo ""
echo "${YELLOW}🔍 Step 5: Testando endpoints...${NC}"
echo ""

# Test 1: Health Check
echo "Test 1: Health Check"
echo "GET http://localhost:$PORT/health"
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:$PORT/health)
HTTP_CODE=$(echo "$HEALTH_RESPONSE" | tail -n1)
BODY=$(echo "$HEALTH_RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "${GREEN}✓ Health check OK (200)${NC}"
    echo "Response: $BODY"
else
    echo "${RED}✗ Health check FAILED (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
fi
echo ""

# Test 2: Root endpoint
echo "Test 2: Root Endpoint"
echo "GET http://localhost:$PORT/"
ROOT_RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:$PORT/)
HTTP_CODE=$(echo "$ROOT_RESPONSE" | tail -n1)
BODY=$(echo "$ROOT_RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "${GREEN}✓ Root endpoint OK (200)${NC}"
    echo "Response: $BODY"
else
    echo "${RED}✗ Root endpoint FAILED (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
fi
echo ""

# Test 3: Telemetry endpoint (sem auth - deve falhar)
echo "Test 3: Telemetry Endpoint (sem autenticação - deve retornar 401)"
echo "POST http://localhost:$PORT/v1/telemetry/scan-result"
UNAUTH_RESPONSE=$(curl -s -w "\n%{http_code}" \
  -X POST http://localhost:$PORT/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -d '{}')
HTTP_CODE=$(echo "$UNAUTH_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ]; then
    echo "${GREEN}✓ Autenticação funcionando corretamente (401)${NC}"
else
    echo "${YELLOW}⚠ Esperado 401, recebido HTTP $HTTP_CODE${NC}"
fi
echo ""

# Test 4: Telemetry endpoint (com auth)
echo "Test 4: Telemetry Endpoint (com autenticação)"
echo "POST http://localhost:$PORT/v1/telemetry/scan-result"
if [ -f "test-payload.json" ]; then
    AUTH_RESPONSE=$(curl -s -w "\n%{http_code}" \
      -X POST http://localhost:$PORT/v1/telemetry/scan-result \
      -H "Authorization: Bearer test-api-key-local" \
      -H "Content-Type: application/json" \
      -d @test-payload.json)
    HTTP_CODE=$(echo "$AUTH_RESPONSE" | tail -n1)
    BODY=$(echo "$AUTH_RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "${GREEN}✓ Endpoint funcionando corretamente (200)${NC}"
        echo "Response: $BODY"
    else
        echo "${RED}✗ Endpoint FAILED (HTTP $HTTP_CODE)${NC}"
        echo "Response: $BODY"
    fi
else
    echo "${YELLOW}⚠ Arquivo test-payload.json não encontrado, pulando teste${NC}"
fi
echo ""

echo "======================================"
echo "${YELLOW}📋 Logs do container:${NC}"
echo "======================================"
docker logs --tail 50 $CONTAINER_NAME
echo ""

echo "======================================"
echo "${GREEN}✅ Testes concluídos!${NC}"
echo "======================================"
echo ""
echo "Container está rodando. Comandos úteis:"
echo ""
echo "  Ver logs em tempo real:"
echo "  ${YELLOW}docker logs -f $CONTAINER_NAME${NC}"
echo ""
echo "  Entrar no container:"
echo "  ${YELLOW}docker exec -it $CONTAINER_NAME sh${NC}"
echo ""
echo "  Parar e remover container:"
echo "  ${YELLOW}docker rm -f $CONTAINER_NAME${NC}"
echo ""
echo "  Testar manualmente:"
echo "  ${YELLOW}curl http://localhost:$PORT/health${NC}"
echo ""
