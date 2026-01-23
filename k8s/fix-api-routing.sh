#!/bin/bash

echo "🔧 API 라우팅 문제 해결 중..."

ALB_URL="http://ing-default-univingress-eeff7-123127578-e18b2d7ab2a9.kr.lb.naverncp.com"

echo "🧪 현재 라우팅 테스트..."
echo "1. 프론트엔드 루트:"
curl -I "$ALB_URL"

echo ""
echo "2. /api 경로 테스트:"
curl -I "$ALB_URL/api"

echo ""
echo "3. /actuator 경로 테스트:"
curl -I "$ALB_URL/actuator"

echo ""
echo "4. /actuator/health 경로 테스트:"
curl -I "$ALB_URL/actuator/health"

echo ""
echo "🔍 백엔드 Pod 직접 테스트..."
BACKEND_POD=$(kubectl get pods -l app=backend -o jsonpath='{.items[0].metadata.name}')
echo "백엔드 Pod: $BACKEND_POD"

echo "Pod 내부 헬스체크:"
kubectl exec $BACKEND_POD -- curl -I http://localhost:8080/actuator/health

echo ""
echo "🔧 Ingress 라우팅 규칙 수정..."
# /actuator 경로를 명시적으로 추가
kubectl patch ingress univ-ingress --type='json' -p='[
  {
    "op": "replace",
    "path": "/spec/rules/0/http/paths",
    "value": [
      {
        "path": "/api",
        "pathType": "Prefix",
        "backend": {
          "service": {
            "name": "backend-svc",
            "port": {
              "number": 80
            }
          }
        }
      },
      {
        "path": "/actuator",
        "pathType": "Prefix", 
        "backend": {
          "service": {
            "name": "backend-svc",
            "port": {
              "number": 80
            }
          }
        }
      },
      {
        "path": "/",
        "pathType": "Prefix",
        "backend": {
          "service": {
            "name": "frontend-svc",
            "port": {
              "number": 80
            }
          }
        }
      }
    ]
  }
]'

echo "⏳ Ingress 업데이트 대기..."
sleep 30

echo ""
echo "🧪 수정 후 테스트..."
echo "1. /actuator/health 재테스트:"
curl -I "$ALB_URL/actuator/health"

echo ""
echo "2. /api 테스트 (로그인 API):"
curl -I "$ALB_URL/api/v1/auth/login"

echo ""
echo "✅ API 라우팅 수정 완료!"
echo "🌐 브라우저 접속: $ALB_URL"