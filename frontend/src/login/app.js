const $ = (id) => document.getElementById(id);

function setMsg(text, type = "info") {
  const el = $("msg");
  el.textContent = text;

  if (type === "error") el.style.color = "#b91c1c";
  else if (type === "success") el.style.color = "#166534";
  else el.style.color = "#1f4e8c";
}

function isEmpty(v) {
  return !v || !String(v).trim();
}

function redirectIfAlreadyLoggedIn() {
  if (localStorage.getItem("isLoggedIn") === "true") {
    window.location.href = "../main/index.html";
  }
}

function init() {
  redirectIfAlreadyLoggedIn();

  const form = $("loginForm");
  const userId = $("userId");
  const password = $("password");

  form.addEventListener("submit", (e) => {
    e.preventDefault();

    const idVal = userId.value.trim();
    const pwVal = password.value;

    if (isEmpty(idVal)) {
      setMsg("학번/사번을 입력해주세요.", "error");
      userId.focus();
      return;
    }
    if (isEmpty(pwVal)) {
      setMsg("비밀번호를 입력해주세요.", "error");
      password.focus();
      return;
    }
    if (idVal.length < 4) {
      setMsg("학번/사번을 다시 확인해주세요(너무 짧습니다).", "error");
      userId.focus();
      return;
    }

    // ✅ 로컬 로그인 서비스 호출
    fetch("/api/v1/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include", // 쿠키 포함 (JSESSIONID)
      body: JSON.stringify({ userId: idVal, password: pwVal })
    })
      .then(res => {
        if (!res.ok) {
          if (res.status === 401) {
            throw new Error("학번/사번 또는 비밀번호가 올바르지 않습니다.");
          }
          return res.json().then(err => { throw new Error(err.message || "인증에 실패했습니다."); });
        }
        return res.json();
      })
      .then(data => {
        console.log("Login response:", data);
        
        // studentId가 없으면 경고
        if (!data.studentId) {
          console.warn("studentId is null in login response. userId:", data.userId);
          setMsg("경고: 학생 정보를 찾을 수 없습니다. 성적 조회가 제한될 수 있습니다.", "error");
          // 일단 로그인은 진행하되, userId를 사용
        }
        
        localStorage.setItem("isLoggedIn", "true");
        // backend returns studentId as the primary key for grades
        // studentId가 있으면 studentId를, 없으면 userId를 사용 (교직원의 경우)
        const storedUserId = data.studentId ? String(data.studentId) : String(data.userId);
        localStorage.setItem("userId", storedUserId);
        localStorage.setItem("userRole", data.role);
        // 사용자 이름 저장
        localStorage.setItem("userName", data.name || "사용자");
        
        console.log("Stored in localStorage:", {
          isLoggedIn: localStorage.getItem("isLoggedIn"),
          userId: localStorage.getItem("userId"),
          userRole: localStorage.getItem("userRole"),
          userName: localStorage.getItem("userName"),
          originalData: data
        });
        
        setMsg("로그인 성공! 메인 화면으로 이동합니다...", "success");
        setTimeout(() => {
          window.location.href = "../main/index.html";
        }, 500);
      })
      .catch(err => {
        console.error(err);
        setMsg(err.message || "로그인 서버와 연결할 수 없습니다.", "error");
      });
  });

  password.addEventListener("keyup", (e) => {
    if (e.getModifierState && e.getModifierState("CapsLock")) {
      setMsg("Caps Lock이 켜져 있습니다.", "error");
    } else if ($("msg").textContent.includes("Caps Lock")) {
      setMsg("");
    }
  });
}

document.addEventListener("DOMContentLoaded", init);
