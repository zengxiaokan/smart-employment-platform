<template>
  <div class="login-page" @mousemove="onMouseMove">
    <div class="ocean-bg">
      <div class="deep-layer"></div>
      <div class="mid-layer"></div>
      <div class="light-layer"></div>
      <div class="bubbles">
        <span v-for="i in 10" :key="i" class="bubble" :style="{ left: (i * 89 + 47) % 90 + '%', animationDelay: (i * 1.6) + 's', animationDuration: (6 + i * 1.4) + 's', width: (6 + i * 3) + 'px', height: (6 + i * 3) + 'px' }"></span>
      </div>
    </div>

    <div class="login-layout">
      <div class="hero-side">
        <div class="whale-layer">
          <div class="depth-shadow"></div>
          <div class="whale" ref="whaleRef">
            <div class="water-splash">
              <span v-for="i in 8" :key="i" class="splash-drop"></span>
            </div>
            <div class="whale-body">
              <div class="body-grad"></div>
              <div class="body-spots">
                <span v-for="i in 4" :key="i" class="spot"></span>
              </div>
              <div class="whale-belly">
                <div class="belly-ridge" v-for="i in 5" :key="i"></div>
              </div>
              <div class="whale-fin tail-fin"></div>
              <div class="whale-fin side-fin-left"></div>
              <div class="eye-group">
                <div class="eye">
                  <div class="pupil" :style="pupilStyle"></div>
                </div>
                <div class="eye">
                  <div class="pupil" :style="pupilStyle"></div>
                </div>
              </div>
              <div class="whale-smile"></div>
              <div class="whale-blush left-blush"></div>
              <div class="whale-blush right-blush"></div>
            </div>
          </div>
        </div>

        <div class="hero-text">
          <div class="title-group">
            <span class="t1">让每一次相遇</span>
            <span class="t2">都值得期待</span>
          </div>
          <div class="desc-group">
            <span class="d1">🎯 AI 精准匹配</span>
            <span class="d2">💬 实时沟通</span>
            <span class="d3">📊 数据驱动</span>
          </div>
        </div>
      </div>

      <div class="form-side">
        <div class="login-panel">
          <div class="panel-brand">
            <div class="brand-icon">🐋</div>
            <h2 class="brand-name">智聘</h2>
            <p class="brand-sub">智能就业服务平台</p>
          </div>

          <div class="mode-switch">
            <button :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
            <button :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
          </div>

          <template v-if="mode === 'login'">
            <form @submit.prevent="handleLogin">
            <div class="login-method" v-if="loginMethod === 'username'">
              <el-input v-model="loginForm.username" placeholder="用户名" size="large" class="custom-input" @input="loginForm.username = filterChinese(loginForm.username)">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password class="custom-input">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </div>
            <div class="login-method" v-else>
              <el-input v-model="loginForm.phone" placeholder="手机号" size="large" maxlength="11" class="custom-input">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
              <div class="sms-row">
                <el-input v-model="loginForm.catchCode" placeholder="验证码" size="large" maxlength="6" class="custom-input sms-input">
                  <template #prefix><el-icon><Key /></el-icon></template>
                </el-input>
                <button class="sms-btn" :disabled="codeCountdown > 0 || sending" @click="sendCode">
                  {{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}
                </button>
              </div>
            </div>
            <div class="login-switch">
              <a :class="{ on: loginMethod === 'username' }" @click="switchLoginMethod('username')">密码登录</a>
              <span>·</span>
              <a :class="{ on: loginMethod === 'phone' }" @click="switchLoginMethod('phone')">验证码登录</a>
            </div>
            <button class="submit-btn" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              <span v-else>登 录</span>
            </button>
            </form>
            <p class="tip">还没有账号？<a @click="switchMode('register')">立即注册</a> · <a @click="openForgotDialog">忘记密码</a></p>
          </template>

          <!-- 忘记密码弹窗 -->
          <el-dialog v-model="showForgotDialog" title="忘记密码" width="420px" :close-on-click-modal="false" center @closed="onForgotClosed">
            <div class="forgot-steps">
              <div class="step-dot" :class="{ active: forgotStep === 0, done: forgotStep > 0 }">1</div>
              <span class="step-line" :class="{ done: forgotStep > 0 }"></span>
              <div class="step-dot" :class="{ active: forgotStep === 1, done: forgotStep > 1 }">2</div>
              <span class="step-line" :class="{ done: forgotStep > 1 }"></span>
              <div class="step-dot" :class="{ active: forgotStep === 2, done: forgotStep > 2 }">3</div>
            </div>
            <div class="forgot-body">
              <el-input v-model="forgotForm.username" placeholder="用户名" size="large" @input="forgotForm.username = filterChinese(forgotForm.username)">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
              <el-input v-model="forgotForm.phone" placeholder="注册手机号" size="large" maxlength="11">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
              <div v-if="forgotStep === 0" style="margin-top:12px">
                <button class="submit-btn" :disabled="forgotCodeCountdown > 0 || forgotSending" @click="handleForgotSendCode" style="height:42px;font-size:14px">
                  <span v-if="forgotSending" class="spinner"></span>
                  <span v-else>{{ forgotCodeCountdown > 0 ? forgotCodeCountdown + 's 后重发' : '发送验证码' }}</span>
                </button>
              </div>
              <template v-if="forgotStep >= 1">
                <el-input v-model="forgotForm.code" placeholder="验证码" size="large" maxlength="6">
                  <template #prefix><el-icon><Key /></el-icon></template>
                </el-input>
                <div v-if="forgotStep === 1" style="margin-top:12px">
                  <button class="submit-btn" :disabled="forgotLoading" @click="handleForgotVerifyCode" style="height:42px;font-size:14px">
                    <span v-if="forgotLoading" class="spinner"></span>
                    <span v-else>验证验证码</span>
                  </button>
                </div>
              </template>
              <template v-if="forgotStep >= 2">
                <el-input v-model="forgotForm.newPassword" type="password" placeholder="新密码（6-20位）" size="large" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
                <el-input v-model="forgotForm.confirmPassword" type="password" placeholder="确认新密码" size="large" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
                <div style="margin-top:12px">
                  <button class="submit-btn" :disabled="forgotLoading" @click="handleForgotReset" style="height:42px;font-size:14px">
                    <span v-if="forgotLoading" class="spinner"></span>
                    <span v-else>重置密码</span>
                  </button>
                </div>
              </template>
            </div>
          </el-dialog>

          <template v-if="mode === 'register'">
            <form @submit.prevent="handleRegister">
            <div class="role-selector">
              <div v-for="r in registerRoleOptions" :key="r.value" class="role-item" :class="{ active: registerForm.role === r.value }" @click="registerForm.role = r.value">
                <el-icon :size="18"><component :is="r.icon" /></el-icon>
                <span>{{ r.label }}</span>
              </div>
            </div>
            <el-input v-model="registerForm.username" placeholder="用户名（仅支持英文/数字/符号）" size="large" class="custom-input" @input="registerForm.username = filterChinese(registerForm.username)"><template #prefix><el-icon><User /></el-icon></template></el-input>
            <el-input v-model="registerForm.phone" placeholder="手机号" size="large" maxlength="11" class="custom-input"><template #prefix><el-icon><Iphone /></el-icon></template></el-input>
            <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" show-password class="custom-input"><template #prefix><el-icon><Lock /></el-icon></template></el-input>
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" show-password class="custom-input"><template #prefix><el-icon><Lock /></el-icon></template></el-input>
            <button class="submit-btn" :disabled="registerLoading">
              <span v-if="registerLoading" class="spinner"></span>
              <span v-else>注 册</span>
            </button>
            </form>
            <p class="tip">已有账号？<a @click="switchMode('login')">立即登录</a></p>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Key, Iphone, UserFilled, Avatar, School } from "@element-plus/icons-vue";
import { loginpwd, loginphone, sendCodeApi, registerApi, forgotPwdCodeApi, forgotPwdVerifyApi, forgotPwdResetApi } from "@/api/login";

const router = useRouter();
const mode = ref("login");
const loginMethod = ref("username");
const loading = ref(false);
const registerLoading = ref(false);
const sending = ref(false);
const codeCountdown = ref(0);
const pupilStyle = ref({});
let countdownTimer = null;

const registerRoleOptions = [
  { label: "求职者", value: 0, icon: "School" },
  { label: "HR", value: 1, icon: "Avatar" },
];

const loginForm = reactive({ username: "", phone: "", password: "", catchCode: "" });
const registerForm = reactive({ username: "", phone: "", password: "", confirmPassword: "", role: null });

// 忘记密码
const showForgotDialog = ref(false);
const forgotStep = ref(0);
const forgotLoading = ref(false);
const forgotSending = ref(false);
const forgotCodeCountdown = ref(0);
let forgotTimer = null;
const forgotForm = reactive({ username: "", phone: "", code: "", newPassword: "", confirmPassword: "" });
const resetToken = ref("");

const openForgotDialog = () => {
  showForgotDialog.value = true;
};

const onForgotClosed = () => {
  forgotStep.value = 0;
  forgotForm.username = "";
  forgotForm.phone = "";
  resetToken.value = "";
  forgotForm.code = "";
  forgotForm.newPassword = "";
  forgotForm.confirmPassword = "";
  forgotCodeCountdown.value = 0;
  forgotLoading.value = false;
  forgotSending.value = false;
  if (forgotTimer) { clearInterval(forgotTimer); forgotTimer = null; }
};

const handleForgotSendCode = async () => {
  if (forgotSending.value || forgotCodeCountdown.value > 0) return;
  if (!forgotForm.username) { ElMessage.warning("请输入用户名"); return; }
  if (hasChinese(forgotForm.username)) { ElMessage.warning("用户名不允许包含中文字符"); return; }
  if (!forgotForm.phone) { ElMessage.warning("请输入手机号"); return; }
  if (!/^1[3-9]\d{9}$/.test(forgotForm.phone)) { ElMessage.warning("请输入正确的手机号"); return; }
  forgotSending.value = true;
  try {
    const res = await forgotPwdCodeApi({ username: forgotForm.username, phone: forgotForm.phone });
    if (res.code === 1) {
      ElMessage.success("验证码已发送");
      forgotStep.value = 1;
      forgotCodeCountdown.value = 60;
      forgotTimer = setInterval(() => { forgotCodeCountdown.value--; if (forgotCodeCountdown.value <= 0) { clearInterval(forgotTimer); forgotTimer = null; } }, 1000);
    } else { ElMessage.error(res.msg || "验证码发送失败"); }
  } catch { ElMessage.error("网络异常"); }
  finally { forgotSending.value = false; }
};

const handleForgotVerifyCode = async () => {
  if (forgotLoading.value) return;
  if (!forgotForm.code) { ElMessage.warning("请输入验证码"); return; }
  forgotLoading.value = true;
  try {
    const res = await forgotPwdVerifyApi({
      username: forgotForm.username,
      phone: forgotForm.phone,
      code: forgotForm.code,
    });
    if (res.code === 1) {
      resetToken.value = res.data;
      ElMessage.success("验证通过，请设置新密码");
      forgotStep.value = 2;
    } else {
      ElMessage.error(res.msg || "验证码错误");
    }
  } catch { ElMessage.error("网络异常"); }
  finally { forgotLoading.value = false; }
};

const handleForgotReset = async () => {
  if (forgotLoading.value) return;
  if (!forgotForm.newPassword || forgotForm.newPassword.length < 6) { ElMessage.warning("新密码至少6位"); return; }
  if (forgotForm.newPassword !== forgotForm.confirmPassword) { ElMessage.warning("两次密码不一致"); return; }
  if (!resetToken.value) { ElMessage.warning("验证已过期，请重新验证"); return; }
  forgotLoading.value = true;
  try {
    const res = await forgotPwdResetApi({
      username: forgotForm.username,
      newPassword: forgotForm.newPassword,
      confirmPassword: forgotForm.confirmPassword,
      resetToken: resetToken.value,
    });
    if (res.code === 1) {
      ElMessage.success("密码重置成功，请登录");
      showForgotDialog.value = false;
    } else { ElMessage.error(res.msg || "重置失败"); }
  } catch { ElMessage.error("网络异常"); }
  finally { forgotLoading.value = false; }
};

const onMouseMove = (e) => {
  const rect = e.currentTarget.getBoundingClientRect();
  const cx = rect.left + rect.width / 2;
  const cy = rect.top + rect.height / 2;
  const dx = (e.clientX - cx) / rect.width * 16;
  const dy = (e.clientY - cy) / rect.height * 16;
  pupilStyle.value = { transform: `translate(${dx}px, ${dy}px)` };
};

const hasChinese = (str) => /[一-龥]/.test(str);

const filterChinese = (val) => val.replace(/[一-龥]/g, '');

const switchMode = (t) => { mode.value = t; if (t === 'login') { loginForm.username = ''; loginForm.phone = ''; loginForm.password = ''; loginForm.catchCode = ''; } };
const switchLoginMethod = (m) => { loginMethod.value = m; };

const sendCode = async () => {
  if (sending.value || codeCountdown.value > 0) return;
  if (!loginForm.phone) { ElMessage.warning("请先输入手机号"); return; }
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) { ElMessage.warning("请输入正确的手机号"); return; }
  sending.value = true;
  try {
    const res = await sendCodeApi({ phone: loginForm.phone });
    if (res.code === 1) {
      ElMessage.success("验证码已发送");
      codeCountdown.value = 60;
      countdownTimer = setInterval(() => { codeCountdown.value--; if (codeCountdown.value <= 0) { clearInterval(countdownTimer); countdownTimer = null; } }, 1000);
    } else { ElMessage.error(res.msg || "发送失败"); }
  } catch { ElMessage.error("验证码发送失败"); }
  finally { sending.value = false; }
};

const handleLogin = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    let res;
    if (loginMethod.value === "username") {
      if (!loginForm.username) { ElMessage.warning("请输入用户名"); return; }
      if (hasChinese(loginForm.username)) { ElMessage.warning("用户名不允许包含中文字符"); return; }
      if (!loginForm.password) { ElMessage.warning("请输入密码"); return; }
      res = await loginpwd({ username: loginForm.username, password: loginForm.password });
    } else {
      if (!loginForm.phone) { ElMessage.warning("请输入手机号"); return; }
      if (!loginForm.catchCode) { ElMessage.warning("请输入验证码"); return; }
      res = await loginphone({ phone: loginForm.phone, code: loginForm.catchCode });
    }
    if (res.code === 1) {
      const role = res.data.role;
      let target = "";
      if (role === 0) {
        target = "/user";
      } else if (role === 1) {
        target = (res.data.companyId && res.data.companyId !== 0) ? "/hr" : "/hr/setup";
      } else if (role === 2) {
        target = "/admin";
      }
      if (!target) { ElMessage.error("未知身份"); return; }
      localStorage.setItem("loginUser", JSON.stringify(res.data));
      ElMessage.success("登录成功");
      router.replace(target);
    } else { ElMessage.error(res.msg || "登录失败"); }
  } catch { ElMessage.error("网络异常"); }
  finally { loading.value = false; }
};

const handleRegister = async () => {
  if (registerLoading.value) return;
  if (!registerForm.username) { ElMessage.warning("请输入用户名"); return; }
  if (hasChinese(registerForm.username)) { ElMessage.warning("用户名不允许包含中文字符"); return; }
  if (!registerForm.phone) { ElMessage.warning("请输入手机号"); return; }
  if (!registerForm.password || registerForm.password.length < 6) { ElMessage.warning("密码至少6位"); return; }
  if (registerForm.password !== registerForm.confirmPassword) { ElMessage.warning("两次密码不一致"); return; }
  if (registerForm.role === null) { ElMessage.warning("请选择身份"); return; }
  registerLoading.value = true;
  try {
    const res = await registerApi({ username: registerForm.username, phone: registerForm.phone, password: registerForm.password, confirmPassword: registerForm.confirmPassword, role: registerForm.role });
    if (res.code === 1) {
      ElMessage.success("注册成功，请登录");
      registerForm.username = ""; registerForm.phone = ""; registerForm.password = ""; registerForm.confirmPassword = ""; registerForm.role = null;
      switchMode("login");
    } else { ElMessage.error(res.msg || "注册失败"); }
  } catch { ElMessage.error("网络异常"); }
  finally { registerLoading.value = false; }
};

onUnmounted(() => { if (countdownTimer) clearInterval(countdownTimer); if (forgotTimer) clearInterval(forgotTimer); });
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #0284c7 0%, #0369a1 20%, #075985 50%, #0c4a6e 80%, #082f49 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.ocean-bg { position: absolute; inset: 0; pointer-events: none; }
.deep-layer { position: absolute; inset: 0; background: radial-gradient(ellipse at 30% 60%, rgba(14,165,233,0.15) 0%, transparent 60%); }
.mid-layer { position: absolute; inset: 0; background: radial-gradient(ellipse at 60% 50%, rgba(56,189,248,0.1) 0%, transparent 50%); }
.light-layer { position: absolute; bottom: 0; left: 0; right: 0; height: 30%; background: linear-gradient(0deg, rgba(125,211,252,0.08) 0%, transparent 100%); }

.bubbles { position: absolute; inset: 0; }
.bubble { position: absolute; bottom: -20px; background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.4), rgba(125,211,252,0.15)); border-radius: 50%; animation: bubbleRise ease-in infinite; }
@keyframes bubbleRise {
  0% { transform: translateY(0) scale(0.5); opacity: 0; }
  20% { opacity: 0.6; }
  100% { transform: translateY(-105vh) scale(1.2); opacity: 0; }
}

.login-layout {
  display: flex;
  width: 100%;
  max-width: 1150px;
  height: 100vh;
  align-items: center;
  gap: 50px;
  padding: 0 30px;
  z-index: 1;
}

.hero-side {
  flex: 1.15;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 36px;
}

.whale-layer { position: relative; }
.depth-shadow {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  width: 280px;
  height: 40px;
  background: radial-gradient(ellipse, rgba(0,0,0,0.25) 0%, transparent 70%);
  border-radius: 50%;
  animation: shadowPulse 5s ease-in-out infinite;
}

@keyframes shadowPulse {
  0%, 100% { transform: translateX(-50%) scale(1); opacity: 0.6; }
  50% { transform: translateX(-50%) scale(1.15); opacity: 0.35; }
}

.whale { position: relative; animation: whaleSwim 7s ease-in-out infinite; }

@keyframes whaleSwim {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  20% { transform: translate(-15px, -18px) rotate(1.5deg); }
  40% { transform: translate(8px, -6px) rotate(-0.8deg); }
  60% { transform: translate(-5px, 12px) rotate(0.5deg); }
  80% { transform: translate(12px, -10px) rotate(-1deg); }
}

.water-splash {
  position: absolute;
  top: -55px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.splash-drop {
  width: 6px;
  height: 14px;
  background: linear-gradient(0deg, rgba(125,211,252,0.8), rgba(255,255,255,0.6));
  border-radius: 50% 50% 20% 20%;
  animation: splash 2s ease-out infinite;
}
.splash-drop:nth-child(1) { animation-delay: 0s; }
.splash-drop:nth-child(2) { animation-delay: 0.18s; width: 5px; height: 12px; }
.splash-drop:nth-child(3) { animation-delay: 0.36s; width: 4px; height: 10px; }
.splash-drop:nth-child(4) { animation-delay: 0.54s; }
.splash-drop:nth-child(5) { animation-delay: 0.72s; width: 5px; }
.splash-drop:nth-child(6) { animation-delay: 0.9s; width: 3px; height: 8px; }
.splash-drop:nth-child(7) { animation-delay: 1.08s; width: 6px; }
.splash-drop:nth-child(8) { animation-delay: 1.26s; width: 4px; height: 9px; }

@keyframes splash {
  0% { transform: translateY(0) scale(1); opacity: 0.9; }
  100% { transform: translateY(-70px) scale(0.2); opacity: 0; }
}

.whale-body {
  width: 300px;
  height: 190px;
  background: linear-gradient(175deg, #7dd3fc 0%, #38bdf8 30%, #0ea5e9 60%, #0284c7 85%, #0369a1 100%);
  border-radius: 55% 55% 50% 50% / 58% 58% 42% 42%;
  position: relative;
  box-shadow:
    0 12px 48px rgba(14,165,233,0.35),
    inset 0 -15px 30px rgba(0,0,0,0.08),
    inset 0 8px 20px rgba(255,255,255,0.15);
  transition: border-radius 0.4s;
}

.whale-body:hover {
  border-radius: 52% 52% 50% 50% / 55% 55% 45% 45%;
}

.body-grad {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(ellipse at 40% 30%, rgba(255,255,255,0.25) 0%, transparent 60%);
}

.body-spots { position: absolute; inset: 0; }
.spot {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.12);
}
.spot:nth-child(1) { width: 18px; height: 12px; top: 25%; left: 55%; }
.spot:nth-child(2) { width: 12px; height: 8px; top: 20%; left: 68%; }
.spot:nth-child(3) { width: 22px; height: 14px; top: 28%; left: 75%; }
.spot:nth-child(4) { width: 10px; height: 7px; top: 32%; left: 82%; }

.whale-belly {
  position: absolute;
  bottom: 10px;
  left: 18%;
  width: 64%;
  height: 48%;
  background: linear-gradient(180deg, rgba(255,255,255,0.55) 0%, rgba(186,230,253,0.7) 40%, rgba(125,211,252,0.3) 100%);
  border-radius: 0 0 45% 45%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 5px;
  padding-bottom: 8px;
}

.belly-ridge {
  width: 80%;
  margin: 0 auto;
  height: 1px;
  background: rgba(255,255,255,0.3);
  border-radius: 1px;
}
.belly-ridge:nth-child(odd) { width: 60%; }

.whale-fin { position: absolute; }
.tail-fin {
  right: -55px;
  top: 40px;
  width: 80px;
  height: 90px;
  background: linear-gradient(170deg, #38bdf8 0%, #0284c7 40%, #0369a1 100%);
  border-radius: 0 55% 70% 0;
  transform: rotate(-8deg);
  box-shadow: 0 6px 20px rgba(14,165,233,0.25);
  animation: tailWag 3s ease-in-out infinite;
}

@keyframes tailWag {
  0%, 100% { transform: rotate(-8deg); }
  50% { transform: rotate(2deg); }
}

.side-fin-left {
  bottom: 18px;
  left: 90px;
  width: 55px;
  height: 30px;
  background: linear-gradient(160deg, #0ea5e9, #0369a1);
  border-radius: 0 45% 45% 0;
  transform: rotate(12deg);
  animation: finFlap 4s ease-in-out infinite;
}

@keyframes finFlap {
  0%, 100% { transform: rotate(12deg); }
  50% { transform: rotate(-5deg); }
}

.eye-group {
  position: absolute;
  top: 42px;
  left: 80px;
  display: flex;
  gap: 38px;
}

.eye {
  width: 28px;
  height: 30px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 8px rgba(0,0,0,0.08), inset 0 -2px 3px rgba(0,0,0,0.06);
}

.pupil {
  width: 14px;
  height: 16px;
  background: radial-gradient(circle at 40% 40%, #1e293b, #0c4a6e);
  border-radius: 50%;
  transition: transform 0.06s ease-out;
  position: relative;
}

.pupil::after {
  content: '';
  position: absolute;
  width: 5px;
  height: 5px;
  background: #fff;
  border-radius: 50%;
  top: 3px;
  left: 2px;
}

.whale-smile {
  position: absolute;
  bottom: 40px;
  left: 92px;
  width: 30px;
  height: 12px;
  border-bottom: 2.5px solid rgba(3,105,161,0.5);
  border-radius: 0 0 14px 14px;
}

.whale-blush {
  position: absolute;
  width: 20px;
  height: 14px;
  background: rgba(251,146,60,0.3);
  border-radius: 50%;
  top: 66px;
}
.left-blush { left: 55px; }
.right-blush { left: 125px; }

.hero-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.title-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.t1 { font-size: 30px; font-weight: 800; color: rgba(255,255,255,0.9); letter-spacing: 3px; text-shadow: 0 2px 12px rgba(0,0,0,0.2); }
.t2 { font-size: 34px; font-weight: 900; color: #7dd3fc; letter-spacing: 4px; text-shadow: 0 4px 20px rgba(125,211,252,0.4); }

.desc-group {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  justify-content: center;
}

.d1, .d2, .d3 {
  padding: 10px 18px;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 24px;
  font-size: 14px;
  color: rgba(255,255,255,0.85);
  font-weight: 500;
  letter-spacing: 1px;
}

.form-side { flex: 1; display: flex; justify-content: center; max-width: 440px; }

.login-panel {
  width: 100%;
  background: linear-gradient(180deg, rgba(224,242,254,0.92) 0%, rgba(186,230,253,0.88) 100%);
  backdrop-filter: blur(24px);
  border-radius: 24px;
  padding: 38px 34px;
  box-shadow:
    0 12px 50px rgba(0,0,0,0.2),
    0 0 0 1px rgba(255,255,255,0.25) inset;
}

.panel-brand { text-align: center; margin-bottom: 24px; }
.brand-icon { font-size: 44px; margin-bottom: 6px; filter: drop-shadow(0 4px 8px rgba(14,165,233,0.3)); }
.brand-name { font-size: 26px; font-weight: 800; color: #0c4a6e; margin: 0 0 4px; letter-spacing: 4px; }
.brand-sub { font-size: 13px; color: #64748b; margin: 0; letter-spacing: 2px; }

.mode-switch {
  display: flex;
  background: rgba(255,255,255,0.5);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 24px;
}
.mode-switch button {
  flex: 1; padding: 10px; border: none; background: transparent;
  color: #64748b; font-size: 14px; font-weight: 600; border-radius: 10px;
  cursor: pointer; transition: all 0.3s;
}
.mode-switch button.active {
  background: #fff; color: #0284c7;
  box-shadow: 0 2px 10px rgba(2,132,199,0.15);
}

.login-method { display: flex; flex-direction: column; gap: 14px; }
.sms-row { display: flex; gap: 10px; }
.sms-input { flex: 1; }
.sms-btn {
  width: 105px; white-space: nowrap;
  border: 1px solid #7dd3fc; background: #fff; color: #0284c7;
  border-radius: 10px; font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all 0.3s;
}
.sms-btn:hover:not(:disabled) { background: #e0f2fe; }
.sms-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.login-switch {
  display: flex; justify-content: center; gap: 12px;
  margin: 16px 0 20px; font-size: 13px; color: #94a3b8;
}
.login-switch a { color: #64748b; cursor: pointer; transition: color 0.3s; }
.login-switch a:hover, .login-switch a.on { color: #0284c7; font-weight: 600; }

.role-selector { display: flex; gap: 12px; margin-bottom: 16px; }
.role-item {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 14px 8px; border: 2px solid #bae6fd; border-radius: 12px;
  cursor: pointer; transition: all 0.3s; color: #64748b; font-size: 13px;
  background: rgba(255,255,255,0.5);
}
.role-item:hover { border-color: #7dd3fc; color: #0284c7; background: rgba(255,255,255,0.8); }
.role-item.active { border-color: #38bdf8; color: #0284c7; background: rgba(224,242,254,0.9); box-shadow: 0 0 16px rgba(56,189,248,0.15); }

.submit-btn {
  width: 100%; height: 48px;
  background: linear-gradient(135deg, #38bdf8 0%, #0284c7 100%);
  border: none; border-radius: 14px; font-size: 16px; font-weight: 700;
  color: #fff; letter-spacing: 4px; cursor: pointer; transition: all 0.3s;
  box-shadow: 0 8px 24px rgba(2,132,199,0.3);
  display: flex; align-items: center; justify-content: center;
}
.submit-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(2,132,199,0.4); }
.submit-btn:disabled { opacity: 0.55; cursor: not-allowed; }

.spinner { width: 20px; height: 20px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.tip { text-align: center; margin-top: 18px; font-size: 13px; color: #64748b; }
.tip a { color: #0284c7; cursor: pointer; font-weight: 600; }
.tip a:hover { text-decoration: underline; }

.forgot-steps {
  display: flex; align-items: center; justify-content: center; gap: 0;
  margin-bottom: 20px;
}
.step-dot {
  width: 28px; height: 28px; border-radius: 50%;
  background: #f1f5f9; border: 2px solid #cbd5e1;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #94a3b8;
  transition: all 0.3s;
}
.step-dot.active { border-color: #38bdf8; color: #0284c7; background: #e0f2fe; }
.step-dot.done { border-color: #38bdf8; color: #fff; background: #38bdf8; }
.step-line {
  width: 36px; height: 2px; background: #cbd5e1;
  transition: background 0.3s;
}
.step-line.done { background: #38bdf8; }

.forgot-body {
  display: flex; flex-direction: column; gap: 12px;
}
.forgot-body :deep(.el-input__wrapper) {
  background: #f8fafc !important;
  border: 1px solid #cbd5e1 !important;
  border-radius: 10px !important; box-shadow: none !important;
}
.forgot-body :deep(.el-input__wrapper:hover) { border-color: #94a3b8 !important; }
.forgot-body :deep(.el-input__wrapper.is-focus) {
  border-color: #38bdf8 !important; background: #fff !important;
  box-shadow: 0 0 0 3px rgba(56,189,248,0.1) !important;
}

:deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.7) !important;
  border: 2px solid #bae6fd !important;
  border-radius: 12px !important; box-shadow: none !important;
  padding: 2px 14px !important; transition: all 0.3s !important;
}
:deep(.el-input__wrapper:hover) { border-color: #7dd3fc !important; }
:deep(.el-input__wrapper.is-focus) {
  border-color: #38bdf8 !important; background: #fff !important;
  box-shadow: 0 0 0 4px rgba(56,189,248,0.1) !important;
}
:deep(.el-input__inner) { color: #0c4a6e !important; }
:deep(.el-input__inner::placeholder) { color: #94a3b8 !important; }
:deep(.el-input__prefix .el-icon) { color: #94a3b8 !important; }
:deep(.el-input__wrapper.is-focus .el-input__prefix .el-icon) { color: #38bdf8 !important; }
:deep(.el-input__suffix .el-icon) { color: #94a3b8 !important; }

@media (max-width: 860px) {
  .login-layout { flex-direction: column; justify-content: center; gap: 20px; padding: 16px; }
  .hero-side { flex: none; gap: 16px; }
  .hero-text { gap: 12px; }
  .t1 { font-size: 22px; }
  .t2 { font-size: 26px; }
  .desc-group { gap: 10px; }
  .d1, .d2, .d3 { font-size: 12px; padding: 8px 14px; }
  .whale-body { width: 200px; height: 128px; }
  .tail-fin { width: 55px; height: 64px; right: -38px; top: 28px; }
  .eye-group { top: 28px; left: 55px; gap: 26px; }
  .eye { width: 20px; height: 22px; }
  .pupil { width: 10px; height: 12px; }
  .whale-smile { bottom: 28px; left: 64px; width: 22px; }
  .left-blush { left: 38px; top: 46px; }
  .right-blush { left: 88px; top: 46px; }
  .water-splash { top: -40px; }
  .form-side { width: 100%; max-width: 100%; }
  .login-panel { padding: 28px 20px; }
}
</style>
