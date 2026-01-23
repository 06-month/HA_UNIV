#!/bin/bash

echo "🔍 애플리케이션 로그 확인 스크립트"
echo "=================================="
echo ""

# Pod 이름 가져오기
POD_NAME=$(kubectl get pods -l app=backend --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)

if [ -z "$POD_NAME" ]; then
    echo "❌ 실행 중인 백엔드 Pod를 찾을 수 없습니다."
    echo "Pod 상태 확인:"
    kubectl get pods -l app=backend
    exit 1
fi

echo "✅ 백엔드 Pod: $POD_NAME"
echo ""

# 1. 로그인 관련 로그
echo "1️⃣ 로그인 관련 로그 (최근 50줄)"
echo "-------------------"
kubectl logs $POD_NAME --tail=200 | grep -E "Login|studentId|userId" | tail -20
echo ""

# 2. 학기 조회 관련 로그
echo "2️⃣ 학기 조회 관련 로그"
echo "-------------------"
kubectl logs $POD_NAME --tail=200 | grep -E "getAvailableSemesters|semester|Found.*grade summaries" | tail -20
echo ""

# 3. 세션 관련 로그
echo "3️⃣ 세션 관련 로그"
echo "-------------------"
kubectl logs $POD_NAME --tail=200 | grep -E "Session|sessionId|studentId from session" | tail -20
echo ""

# 4. 에러 로그
echo "4️⃣ 에러 로그"
echo "-------------------"
kubectl logs $POD_NAME --tail=200 | grep -E "ERROR|Exception|Failed" | tail -20
echo ""

# 5. 최근 로그 전체 (마지막 30줄)
echo "5️⃣ 최근 로그 전체 (마지막 30줄)"
echo "-------------------"
kubectl logs $POD_NAME --tail=30
echo ""

echo "✅ 로그 확인 완료!"
echo ""
echo "📋 확인 포인트:"
echo "   - 로그인 시 studentId가 제대로 저장되었는지"
echo "   - 학기 조회 시 사용된 studentId가 무엇인지"
echo "   - 세션에서 studentId를 가져왔는지"
echo "   - 에러 메시지가 있는지"
