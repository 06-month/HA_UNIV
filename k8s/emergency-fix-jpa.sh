#!/bin/bash

echo "🚨 긴급 JPA 설정 수정 중..."

# 현재 실패한 Pod들 강제 삭제
echo "❌ 실패한 Pod들 삭제..."
kubectl delete pods -l app=backend --force --grace-period=0

# JPA DDL을 update로 변경
echo "📝 JPA DDL을 update로 변경..."
kubectl patch deployment univ-backend --type='json' -p='[
  {
    "op": "replace",
    "path": "/spec/template/spec/containers/0/env",
    "value": [
      {
        "name": "SPRING_DATASOURCE_URL",
        "value": "jdbc:mysql://192.168.30.6:3306/univ_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
      },
      {
        "name": "SPRING_DATASOURCE_USERNAME", 
        "value": "taekjunnn"
      },
      {
        "name": "SPRING_DATASOURCE_PASSWORD",
        "value": "Melontype123!"
      }
    ]
  }
]'

echo "⏳ 새로운 Pod 시작 대기..."
sleep 45

echo "📊 Pod 상태 확인..."
kubectl get pods -l app=backend

echo "📋 새로운 Pod 로그 확인..."
NEW_POD=$(kubectl get pods -l app=backend --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ ! -z "$NEW_POD" ]; then
    echo "✅ 실행 중인 Pod: $NEW_POD"
    kubectl logs $NEW_POD | grep -E "(Started Application|HikariPool|ERROR)" | tail -10
    
    echo ""
    echo "🧪 헬스체크 테스트:"
    sleep 10
    kubectl exec $NEW_POD -- curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "아직 준비 중..."
else
    echo "⚠️ 아직 실행 중인 Pod가 없습니다."
    LATEST_POD=$(kubectl get pods -l app=backend --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}')
    echo "최신 Pod 로그:"
    kubectl logs $LATEST_POD | tail -15
fi

echo ""
echo "🔍 HPA 상태 확인..."
kubectl get hpa univ-backend-hpa

echo ""
echo "✅ 긴급 수정 완료!"