#!/bin/bash

# 기존 시스템에 HPA 추가 배포 스크립트 (개선 버전)
echo "🚀 University System HPA 업그레이드 시작..."

# 현재 배포 상태 확인
echo "📊 현재 배포 상태 확인..."
echo "=== 기존 Deployments ==="
kubectl get deployments -o wide
echo ""
echo "=== 기존 Pods ==="
kubectl get pods -o wide
echo ""
echo "=== 기존 HPA (있다면) ==="
kubectl get hpa 2>/dev/null || echo "HPA가 아직 설정되지 않음"

echo ""
echo "⚠️  주의사항:"
echo "- 기존 배포를 HPA 지원 버전으로 업그레이드합니다"
echo "- 리소스 요청/제한이 추가되어 Pod가 재시작됩니다"
echo "- 잠시 서비스 중단이 발생할 수 있습니다"
echo ""
echo "계속하시겠습니까? (y/N)"
read -r response

if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo ""
    echo "🔄 HPA 지원 배포 파일 적용 중..."
    kubectl apply -f univ-system-with-hpa.yaml
    
    if [ $? -eq 0 ]; then
        echo "✅ 배포 파일 적용 완료"
    else
        echo "❌ 배포 파일 적용 실패"
        exit 1
    fi
    
    echo ""
    echo "⏳ 롤링 업데이트 진행 상황 모니터링..."
    
    # 백엔드 롤아웃 대기 (타임아웃 단축)
    echo "🖥️ 백엔드 롤아웃 대기 중..."
    kubectl rollout status deployment/univ-backend --timeout=120s
    
    if [ $? -ne 0 ]; then
        echo "⚠️ 백엔드 롤아웃이 지연되고 있습니다. 강제로 진행합니다..."
        kubectl delete pods -l app=backend --force --grace-period=0
        sleep 30
    fi
    
    # 프론트엔드 롤아웃 대기  
    echo "🌐 프론트엔드 롤아웃 대기 중..."
    kubectl rollout status deployment/univ-frontend --timeout=300s
    
    # HPA 생성 대기
    echo "📊 HPA 생성 대기 중..."
    sleep 30
    
    echo ""
    echo "📋 업그레이드 후 상태 확인..."
    echo "=== Deployments ==="
    kubectl get deployments -o wide
    echo ""
    echo "=== Pods ==="
    kubectl get pods -o wide
    echo ""
    echo "=== HPA ==="
    kubectl get hpa -o wide
    echo ""
    echo "=== Services ==="
    kubectl get services
    
    # HPA 메트릭 수집 확인
    echo ""
    echo "🔍 HPA 메트릭 수집 상태 확인..."
    sleep 10
    kubectl describe hpa univ-backend-hpa | grep -A 5 "Metrics:"
    kubectl describe hpa univ-frontend-hpa | grep -A 5 "Metrics:"
    
    echo ""
    echo "✅ HPA 업그레이드 완료!"
    echo ""
    echo "📊 유용한 모니터링 명령어:"
    echo "kubectl get hpa -w                              # HPA 실시간 모니터링"
    echo "kubectl describe hpa univ-backend-hpa           # 백엔드 HPA 상세 정보"
    echo "kubectl describe hpa univ-frontend-hpa          # 프론트엔드 HPA 상세 정보"
    echo "kubectl top pods                                # Pod 리소스 사용률"
    echo "kubectl get events --sort-by='.lastTimestamp'   # 최근 이벤트 확인"
    echo ""
    echo "📈 실시간 모니터링 스크립트:"
    echo "./monitor-hpa.sh                                # HPA 대시보드"
    echo "./hpa-test.sh                                   # 부하 테스트"
    
else
    echo "❌ 배포가 취소되었습니다."
    exit 0
fi