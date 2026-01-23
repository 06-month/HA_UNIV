#!/bin/bash

# 프론트엔드-백엔드 통신 테스트 스크립트
echo "🔗 프론트엔드-백엔드 통신 테스트 시작..."

# Ingress IP 확인
INGRESS_IP=$(kubectl get ingress univ-ingress -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo "Ingress IP: $INGRESS_IP"

if [ -z "$INGRESS_IP" ]; then
    echo "❌ Ingress IP를 찾을 수 없습니다. LoadBalancer가 준비되지 않았을 수 있습니다."
    exit 1
fi

echo ""
echo "🌐 프론트엔드 접근 테스트..."
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://$INGRESS_IP/

echo ""
echo "🖥️ 백엔드 API 테스트..."
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://$INGRESS_IP/actuator/health

echo ""
echo "📊 백엔드 Pod 목록:"
kubectl get pods -l app=backend -o wide

echo ""
echo "🌍 프론트엔드 Pod 목록:"
kubectl get pods -l app=frontend -o wide

echo ""
echo "🔄 Service 엔드포인트 확인:"
kubectl get endpoints backend-svc
kubectl get endpoints frontend-svc

echo ""
echo "✅ 통신 테스트 완료!"