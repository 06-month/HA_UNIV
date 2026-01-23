#!/bin/bash

# HPA 모니터링 스크립트
echo "📊 HPA 실시간 모니터링 시작..."
echo "Ctrl+C로 중단하세요"
echo ""

# 터미널 화면 클리어 함수
clear_screen() {
    clear
    echo "📊 University System HPA 모니터링"
    echo "=================================="
    date
    echo ""
}

# 무한 루프로 모니터링
while true; do
    clear_screen
    
    echo "🎯 HPA 상태:"
    kubectl get hpa
    echo ""
    
    echo "📦 Pod 상태:"
    kubectl get pods -o wide
    echo ""
    
    echo "💻 리소스 사용률:"
    kubectl top pods 2>/dev/null || echo "메트릭 서버 데이터 수집 중..."
    echo ""
    
    echo "📈 백엔드 HPA 상세 정보:"
    kubectl describe hpa univ-backend-hpa | grep -E "(Current|Target|Min|Max|Conditions)" | head -10
    echo ""
    
    echo "🌐 프론트엔드 HPA 상세 정보:"
    kubectl describe hpa univ-frontend-hpa | grep -E "(Current|Target|Min|Max|Conditions)" | head -10
    echo ""
    
    echo "다음 업데이트: 10초 후..."
    sleep 10
done