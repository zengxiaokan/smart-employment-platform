import { reactive } from "vue";
import { clearChatCache } from "@/services/chatCache.js";

const userInfo = reactive({
  userId: null,
  role: null,
  username: "",
  nickname: "",
  companyId: null,
  avatarUrl: "",
  companyName: "",
  token: "",
});

export function useUser() {
  const loadUserInfo = () => {
    try {
      const raw = localStorage.getItem("loginUser");
      if (raw) {
        const u = JSON.parse(raw);
        userInfo.userId = u.userId ?? null;
        userInfo.role = u.role ?? null;
        userInfo.username = u.username || "";
        userInfo.nickname = u.nickname || "";
        userInfo.companyId = u.companyId ?? null;
        userInfo.avatarUrl = u.avatarUrl || "";
        userInfo.companyName = u.companyName || "";
        userInfo.token = u.token || "";
      }
    } catch {}
  };

  const updateAvatar = (url) => {
    userInfo.avatarUrl = url;
    syncToStorage();
  };

  const updateNickname = (name) => {
    userInfo.nickname = name;
    syncToStorage();
  };

  const syncToStorage = () => {
    try {
      const raw = localStorage.getItem("loginUser");
      if (raw) {
        const u = JSON.parse(raw);
        u.avatarUrl = userInfo.avatarUrl;
        u.nickname = userInfo.nickname;
        localStorage.setItem("loginUser", JSON.stringify(u));
      }
    } catch {}
  };

  const clearUser = () => {
    clearChatCache();
    userInfo.userId = null;
    userInfo.role = null;
    userInfo.username = "";
    userInfo.nickname = "";
    userInfo.companyId = null;
    userInfo.avatarUrl = "";
    userInfo.companyName = "";
    userInfo.token = "";
  };

  loadUserInfo();

  return { userInfo, loadUserInfo, updateAvatar, updateNickname, clearUser };
}
