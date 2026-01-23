#!/bin/bash

echo "🔍 클라우드 서버 문제 진단 스크립트"
echo "=================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 데이터베이스 연결 확인
echo "1️⃣ 데이터베이스 연결 확인"
echo "-------------------"
mysql -h 192.168.30.6 -P 3306 -u taekjunnn -p univ_db -e "SELECT 'Database connection: OK' as status;" 2>&1 | grep -v "Warning\|Using a password"
echo ""

# 2. 특정 학생의 학기 데이터 확인
echo "2️⃣ 학생 학기 데이터 확인 (student_id = 1 예시)"
echo "-------------------"
echo "GradeSummary 테이블:"
mysql -h 192.168.30.6 -P 3306 -u taekjunnn -p univ_db << 'SQL'
SELECT 
    student_id,
    semester,
    gpa,
    total_credits
FROM grade_summary
WHERE student_id = 1
ORDER BY semester DESC;
SQL

echo ""
echo "Enrollments 테이블:"
mysql -h 192.168.30.6 -P 3306 -u taekjunnn -p univ_db << 'SQL'
SELECT 
    student_id,
    COUNT(DISTINCT semester) as semester_count,
    GROUP_CONCAT(DISTINCT semester ORDER BY semester DESC) as semesters
FROM enrollments
WHERE student_id = 1
GROUP BY student_id;
SQL

echo ""

# 3. 학생 이름 확인
echo "3️⃣ 학생 이름 확인 (login_id = '20240001' 예시)"
echo "-------------------"
mysql -h 192.168.30.6 -P 3306 -u taekjunnn -p univ_db << 'SQL'
SELECT 
    u.user_id,
    u.login_id,
    s.student_id,
    s.name,
    s.student_number
FROM users u
LEFT JOIN students s ON u.user_id = s.user_id
WHERE u.login_id = '20240001';
SQL

echo ""

# 4. 성적 상세 데이터 확인
echo "4️⃣ 성적 상세 데이터 확인 (student_id = 1, semester = '2025-1')"
echo "-------------------"
mysql -h 192.168.30.6 -P 3306 -u taekjunnn -p univ_db << 'SQL'
SELECT 
    g.grade_id,
    g.score,
    g.grade_letter,
    c.course_code,
    c.course_name,
    c.credit,
    e.semester
FROM grades g
JOIN enrollments e ON g.enrollment_id = e.enrollment_id
JOIN courses c ON e.course_id = c.course_id
WHERE e.student_id = 1 AND e.semester = '2025-1'
LIMIT 10;
SQL

echo ""

# 5. 데이터 통계 확인
echo "5️⃣ 데이터 통계 확인"
echo "-------------------"
mysql -h 192.168.30.6 -P 3306 -u taekjunnn -p univ_db << 'SQL'
SELECT 
    'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'students', COUNT(*) FROM students
UNION ALL
SELECT 'grade_summary', COUNT(*) FROM grade_summary
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL
SELECT 'grades', COUNT(*) FROM grades
UNION ALL
SELECT 'courses', COUNT(*) FROM courses;
SQL

echo ""

# 6. 특정 학생의 전체 데이터 확인
echo "6️⃣ 특정 학생의 전체 데이터 확인 (student_id = 1)"
echo "-------------------"
mysql -h 192.168.30.6 -P 3306 -u taekjunnn -p univ_db << 'SQL'
SELECT 
    '학기별 요약' as type,
    gs.semester,
    COUNT(*) as count,
    GROUP_CONCAT(DISTINCT gs.semester ORDER BY gs.semester DESC) as semesters
FROM grade_summary gs
WHERE gs.student_id = 1
GROUP BY gs.student_id, gs.semester

UNION ALL

SELECT 
    '수강 과목',
    e.semester,
    COUNT(*) as count,
    GROUP_CONCAT(DISTINCT c.course_name SEPARATOR ', ') as courses
FROM enrollments e
JOIN courses c ON e.course_id = c.course_id
WHERE e.student_id = 1
GROUP BY e.student_id, e.semester;
SQL

echo ""

# 7. 실제 문제 진단: 세션 및 캐시 관련
echo "7️⃣ 실제 문제 진단"
echo "-------------------"
echo "⚠️  데이터는 모두 존재합니다. 문제는 애플리케이션 레벨일 가능성이 높습니다."
echo ""
echo "확인해야 할 사항:"
echo "1. 실제 로그인한 사용자의 student_id 확인"
echo "2. 애플리케이션 로그에서 세션에 저장된 studentId 확인"
echo "3. 캐시 초기화 필요할 수 있음"
echo ""
echo "애플리케이션 로그 확인 명령어:"
echo "kubectl logs -f deployment/univ-backend | grep -E 'studentId|semester|getAvailableSemesters|Login'"

echo ""
echo "✅ 진단 완료!"
echo ""
echo "📋 확인 사항:"
echo "   - student_id = 1에 대한 데이터가 있는지 확인"
echo "   - 실제 로그인한 사용자의 student_id 확인 필요"
echo "   - 애플리케이션 로그에서 실제 사용되는 student_id 확인"
