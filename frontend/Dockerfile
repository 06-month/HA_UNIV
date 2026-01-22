# 가벼운 Nginx(웹서버) 이미지 사용
FROM nginx:alpine

# 기존 Nginx 기본 파일 삭제 (깨끗하게 시작)
RUN rm -rf /usr/share/nginx/html/*

# 1. 로그인 폴더 복사 -> 접속주소: /login/index.html
COPY src/login /usr/share/nginx/html/login

# 2. 메인(성적조회) 폴더 복사 -> 접속주소: /main/index.html
COPY src/main /usr/share/nginx/html/main

# (팁) 루트(/) 접속 시 로그인 페이지로 자동 이동시키는 파일 생성
RUN echo '<html><head><meta http-equiv="refresh" content="0;url=/login/index.html"></head><body></body></html>' > /usr/share/nginx/html/index.html

# 80번 포트 열기
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
