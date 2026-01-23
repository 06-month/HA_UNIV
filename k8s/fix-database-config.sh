#!/bin/bash

echo "🔧 데이터베이스 설정 수정 중..."

# JPA 설정을 validate로 변경 (기존 테이블 구조 유지)
echo "📝 JPA DDL 설정을 validate로 변경..."
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
            },
            {
              "name": "SPRING_JPA_HIBERNATE_DDL_AUTO",
              "value": "validate"
            },
            {
              "name": "SPRING_JPA_SHOW_SQL",
              "value": "false"
            }
          ]
        }]
      }
    }
  }
}'

echo "⏳ Pod 재시작 대기..."
sleep 30

echo "📊 새로운 Pod 상태 확인..."
kubectl get pods -l app=backend

echo "📋 새로운 Pod 로그 확인..."
NEW_POD=$(kubectl get pods -l app=backend --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ ! -z "$NEW_POD" ]; then
    echo "✅ 실행 중인 Pod: $NEW_POD"
    kubectl logs $NEW_POD --tail=30
    
    echo ""
    echo "🧪 헬스체크 테스트:"
    kubectl exec $NEW_POD -- curl -s http://localhost:8080/actuator/health
else
    echo "⚠️ 아직 실행 중인 Pod가 없습니다."
fi

echo ""
echo "✅ 데이터베이스 설정 수정 완료!"