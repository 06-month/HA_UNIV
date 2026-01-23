#!/bin/bash

echo "🔍 데이터베이스 연결 문제 진단 중..."

# 백엔드 Pod 로그 확인
echo "📋 백엔드 Pod 로그 (최근 50줄):"
BACKEND_POD=$(kubectl get pods -l app=backend -o jsonpath='{.items[0].metadata.name}')
kubectl logs $BACKEND_POD --tail=50

echo ""
echo "🔧 데이터베이스 연결 테스트..."

# Pod 내부에서 데이터베이스 연결 테스트
echo "📊 MySQL 연결 테스트:"
kubectl exec $BACKEND_POD -- sh -c "
echo 'Testing MySQL connection...'
nc -zv 192.168.30.6 3306 2>&1 || echo 'MySQL connection failed'
"

echo ""
echo "🔍 환경 변수 확인:"
kubectl exec $BACKEND_POD -- env | grep -E "(SPRING_|DB_|MYSQL_)"

echo ""
echo "📋 현재 데이터베이스 설정:"
kubectl describe pod $BACKEND_POD | grep -A 10 "Environment:"

echo ""
echo "🧪 헬스체크 엔드포인트 테스트:"
kubectl exec $BACKEND_POD -- curl -s http://localhost:8080/actuator/health | head -20

echo ""
echo "✅ 데이터베이스 진단 완료!"