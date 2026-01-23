#!/bin/bash

# HPA 테스트 스크립트
echo "🧪 HPA 부하 테스트 시작..."

# 백엔드 서비스 엔드포인트 확인
BACKEND_SERVICE=$(kubectl get svc backend-svc -o jsonpath='{.spec.clusterIP}')
echo "백엔드 서비스 IP: $BACKEND_SERVICE"

# 부하 테스트 Pod 생성
echo "🚀 부하 테스트 Pod 생성 중..."
kubectl run load-test --image=busybox --rm -i --tty --restart=Never -- /bin/sh -c "
echo '부하 테스트 시작...'
echo '백엔드 엔드포인트: http://$BACKEND_SERVICE/actuator/health'

# 10개의 동시 요청을 계속 보내기
for i in \$(seq 1 10); do
  (
    while true; do
      wget -q -O- http://$BACKEND_SERVICE/actuator/health > /dev/null 2>&1
      sleep 0.1
    done
  ) &
done

echo '부하 테스트 실행 중... (Ctrl+C로 중단)'
wait
"

echo "🔍 HPA 상태 확인..."
kubectl get hpa
kubectl describe hpa univ-backend-hpa