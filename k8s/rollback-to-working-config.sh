#!/bin/bash

echo "🔄 기존 작동하던 설정으로 롤백 중..."

# 기존 작동하던 Pod들의 설정으로 롤백
echo "📝 환경 변수를 기존 설정으로 복원..."
kubectl patch deployment univ-backend -p '{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "backend",
          "env": [
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
        }]
      }
    }
  }
}'

# JPA 관련 환경 변수 제거 (기본 설정 사용)
echo "🗑️ 추가된 JPA 환경 변수 제거..."
kubectl patch deployment univ-backend --type='json' -p='[
  {
    "op": "remove",
    "path": "/spec/template/spec/containers/0/env/3"
  }
]' 2>/dev/null || echo "JPA 환경 변수가 이미 제거되었거나 존재하지 않음"

echo "⏳ 롤백 완료 대기..."
sleep 30

echo "📊 롤백 후 Pod 상태..."
kubectl get pods -l app=backend

echo "📋 롤백 후 로그 확인..."
BACKEND_POD=$(kubectl get pods -l app=backend --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ ! -z "$BACKEND_POD" ]; then
    kubectl logs $BACKEND_POD --tail=20
    
    echo ""
    echo "🧪 헬스체크 테스트:"
    kubectl exec $BACKEND_POD -- curl -s http://localhost:8080/actuator/health
fi

echo ""
echo "✅ 기존 설정으로 롤백 완료!"