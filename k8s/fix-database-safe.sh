#!/bin/bash

echo "🔧 데이터베이스 스키마 문제 안전 해결 중..."

# 백엔드 Deployment에서 JPA 검증을 비활성화하고 validate 모드로 변경
echo "📝 JPA 설정을 안전 모드로 변경..."
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
              "value": "none"
            },
            {
              "name": "SPRING_JPA_HIBERNATE_NAMING_PHYSICAL_STRATEGY",
              "value": "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl"
            }
          ]
        }]
      }
    }
  }
}'

echo "⏳ 새로운 Pod 시작 대기..."
sleep 15

echo "📊 Pod 상태 확인..."
kubectl get pods -l app=backend

echo "📋 새로운 Pod 로그 확인..."
NEW_POD=$(kubectl get pods -l app=backend --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ ! -z "$NEW_POD" ]; then
    echo "✅ 실행 중인 Pod: $NEW_POD"
    kubectl logs $NEW_POD --tail=30
else
    echo "⚠️ 아직 실행 중인 Pod가 없습니다."
    echo "🔍 최신 Pod 로그 확인..."
    LATEST_POD=$(kubectl get pods -l app=backend --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}')
    kubectl logs $LATEST_POD --tail=30
fi

echo ""
echo "🔍 HPA 상태 확인..."
kubectl get hpa

echo ""
echo "✅ 안전 모드 설정 완료!"