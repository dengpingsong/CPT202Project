<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi, clearAuth } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const router = useRouter()

const loading = ref(true)
const saving = ref(false)
const profileStatus = ref('')
const profileStatusType = ref<'success' | 'error' | ''>('')

// Profile fields
const account = ref('')
const fullName = ref('')
const email = ref('')

// 2FA
const twoFactorEnabled = ref(false)
const twoFactorSetupBox = ref(false)
const twoFactorManualKey = ref('')
const twoFactorEnableCode = ref('')
const twoFactorDisablePassword = ref('')
const twoFactorStatus = ref('')
const twoFactorStatusType = ref<'success' | 'error' | ''>('')
const setupLoading = ref(false)

// Password
const oldPassword = ref('')
const newPassword = ref('')
const confirmNewPassword = ref('')
const passwordStatus = ref('')
const passwordStatusType = ref<'success' | 'error' | ''>('')
const passwordSaving = ref(false)

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

async function fetchProfile() {
  loading.value = true
  try {
    const res = await adminApi.getProfile()
    const p = res.data
    if (p) {
      account.value = p.username || p.account || ''
      fullName.value = p.fullName || ''
      email.value = p.email || ''
      twoFactorEnabled.value = Boolean(p.twoFactorEnabled)
      if (p.fullName) localStorage.setItem('fullName', p.fullName)
    }
    profileStatus.value = 'Profile loaded.'
    profileStatusType.value = 'success'
  } catch (e: any) {
    profileStatus.value = e.message || 'Failed to load profile'
    profileStatusType.value = 'error'
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!fullName.value.trim()) {
    profileStatus.value = 'Full name is required.'
    profileStatusType.value = 'error'
    return
  }
  if (!email.value.trim()) {
    profileStatus.value = 'Email is required.'
    profileStatusType.value = 'error'
    return
  }
  if (!EMAIL_PATTERN.test(email.value.trim())) {
    profileStatus.value = 'Invalid email format.'
    profileStatusType.value = 'error'
    return
  }

  saving.value = true
  profileStatus.value = 'Saving...'
  profileStatusType.value = ''
  try {
    await adminApi.updateProfile({
      fullName: fullName.value.trim(),
      email: email.value.trim(),
    })
    localStorage.setItem('fullName', fullName.value.trim())
    profileStatus.value = 'Profile saved successfully.'
    profileStatusType.value = 'success'
    toast.success('Profile saved')
  } catch (e: any) {
    profileStatus.value = e.message || 'Failed to save profile'
    profileStatusType.value = 'error'
  } finally {
    saving.value = false
  }
}

// 2FA
async function setupTwoFactor() {
  setupLoading.value = true
  twoFactorStatus.value = 'Generating QR code...'
  twoFactorStatusType.value = ''
  try {
    const res = await adminApi.initializeTwoFactorSetup()
    const setup = res.data
    if (setup?.enabled) {
      twoFactorEnabled.value = true
      twoFactorSetupBox.value = false
      twoFactorStatus.value = '2FA is already enabled.'
      twoFactorStatusType.value = 'success'
      return
    }
    twoFactorSetupBox.value = true
    twoFactorManualKey.value = setup?.manualEntryKey || ''
    twoFactorEnableCode.value = ''
    twoFactorStatus.value = 'Scan the QR code, then enter the 6-digit code.'
    twoFactorStatusType.value = 'success'
  } catch (e: any) {
    twoFactorStatus.value = e.message || 'Failed to setup 2FA'
    twoFactorStatusType.value = 'error'
  } finally {
    setupLoading.value = false
  }
}

async function enableTwoFactor() {
  if (!/^\d{6}$/.test(twoFactorEnableCode.value)) {
    twoFactorStatus.value = 'Please enter a valid 6-digit code.'
    twoFactorStatusType.value = 'error'
    return
  }
  setupLoading.value = true
  twoFactorStatus.value = 'Enabling 2FA...'
  twoFactorStatusType.value = ''
  try {
    await adminApi.enableTwoFactor(twoFactorEnableCode.value)
    twoFactorEnabled.value = true
    twoFactorSetupBox.value = false
    twoFactorStatus.value = '2FA enabled successfully.'
    twoFactorStatusType.value = 'success'
  } catch (e: any) {
    twoFactorStatus.value = e.message || 'Failed to enable 2FA'
    twoFactorStatusType.value = 'error'
  } finally {
    setupLoading.value = false
  }
}

async function disableTwoFactor() {
  if (!twoFactorDisablePassword.value.trim()) {
    twoFactorStatus.value = 'Please enter your current password.'
    twoFactorStatusType.value = 'error'
    return
  }
  setupLoading.value = true
  twoFactorStatus.value = 'Disabling 2FA...'
  twoFactorStatusType.value = ''
  try {
    await adminApi.disableTwoFactor(twoFactorDisablePassword.value)
    twoFactorEnabled.value = false
    twoFactorDisablePassword.value = ''
    twoFactorStatus.value = '2FA disabled.'
    twoFactorStatusType.value = 'success'
  } catch (e: any) {
    twoFactorStatus.value = e.message || 'Failed to disable 2FA'
    twoFactorStatusType.value = 'error'
  } finally {
    setupLoading.value = false
  }
}

// Password
async function handleChangePassword() {
  if (!oldPassword.value.trim()) {
    passwordStatus.value = 'Please enter your current password.'
    passwordStatusType.value = 'error'
    return
  }
  if (!newPassword.value.trim()) {
    passwordStatus.value = 'Please enter a new password.'
    passwordStatusType.value = 'error'
    return
  }
  if (newPassword.value !== confirmNewPassword.value) {
    passwordStatus.value = 'Passwords do not match.'
    passwordStatusType.value = 'error'
    return
  }

  passwordSaving.value = true
  passwordStatus.value = 'Changing password...'
  passwordStatusType.value = ''
  try {
    await adminApi.changePassword({ oldPassword: oldPassword.value, newPassword: newPassword.value })
    passwordStatus.value = 'Password changed. Logging out...'
    passwordStatusType.value = 'success'
    toast.success('Password changed')
    setTimeout(() => {
      clearAuth()
      router.push('/login')
    }, 1500)
  } catch (e: any) {
    passwordStatus.value = e.message || 'Failed to change password'
    passwordStatusType.value = 'error'
  } finally {
    passwordSaving.value = false
  }
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}

onMounted(fetchProfile)
</script>

<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>Admin Settings</h1>
    </div>

    <div class="content-panel">
      <form class="settings-form" @submit.prevent="handleSave">
        <!-- Profile -->
        <div class="form-section">
          <div class="section-title">Profile</div>

          <div class="form-row">
            <span class="form-label"><i class="bi bi-person"></i> Account</span>
            <input :value="account" type="text" class="form-control" disabled>
          </div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-person-badge"></i> Full Name</span>
            <input v-model="fullName" type="text" class="form-control" placeholder="Your full name">
          </div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-envelope"></i> Email</span>
            <input v-model="email" type="email" class="form-control" placeholder="Your email">
          </div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-shield"></i> Role</span>
            <div style="color: #6b6b82;">System Administrator</div>
          </div>
        </div>

        <!-- Password -->
        <div class="form-section">
          <div class="section-title">Change Password</div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-lock"></i> Old Password</span>
            <input v-model="oldPassword" type="password" class="form-control" placeholder="Current password">
          </div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-key"></i> New Password</span>
            <input v-model="newPassword" type="password" class="form-control" placeholder="New password">
          </div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-key-fill"></i> Confirm</span>
            <input v-model="confirmNewPassword" type="password" class="form-control" placeholder="Confirm new password">
          </div>
          <div style="margin-top: 8px;">
            <button type="button" class="btn-secondary" :disabled="passwordSaving" @click="handleChangePassword">
              {{ passwordSaving ? 'Changing...' : 'Change Password' }}
            </button>
          </div>
          <div class="form-status" :class="passwordStatusType">{{ passwordStatus }}</div>
        </div>

        <!-- 2FA -->
        <div class="form-section">
          <div class="section-title">Two-Factor Authentication (TOTP)</div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-shield-lock"></i> Status</span>
            <div style="color: #6b6b82;">{{ twoFactorEnabled ? 'Enabled' : 'Disabled' }}</div>
          </div>

          <div v-if="twoFactorSetupBox" class="tfa-box">
            <p style="font-size: 0.9rem; color: #6b6b82; margin: 0 0 8px;">Manual key: <strong>{{ twoFactorManualKey }}</strong></p>
            <input v-model="twoFactorEnableCode" type="text" inputmode="numeric" maxlength="6" class="form-control" placeholder="6-digit code">
            <button type="button" class="btn-primary" style="margin-top: 12px;" :disabled="setupLoading" @click="enableTwoFactor">
              {{ setupLoading ? 'Enabling...' : 'Enable 2FA' }}
            </button>
          </div>

          <div v-if="twoFactorEnabled" class="tfa-box">
            <input v-model="twoFactorDisablePassword" type="password" class="form-control" placeholder="Enter current password to disable 2FA">
            <button type="button" class="btn-secondary" style="margin-top: 12px;" :disabled="setupLoading" @click="disableTwoFactor">
              {{ setupLoading ? 'Disabling...' : 'Disable 2FA' }}
            </button>
          </div>

          <div style="display: flex; gap: 12px; flex-wrap: wrap;">
            <button type="button" class="btn-secondary" :disabled="setupLoading" @click="setupTwoFactor">
              {{ twoFactorEnabled ? 'Regenerate QR Code' : 'Setup 2FA' }}
            </button>
          </div>
          <div class="form-status" :class="twoFactorStatusType">{{ twoFactorStatus }}</div>
        </div>

        <!-- Actions -->
        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="saving">
            {{ saving ? 'Saving...' : 'Save Profile' }}
          </button>
          <button type="button" class="btn-logout" @click="handleLogout">
            Logout
          </button>
          <span style="flex: 1; text-align: right; font-size: 0.8rem; color: #6b6b82;">
            <i class="bi bi-shield-check"></i> Encrypted transmission
          </span>
          <div class="form-status" :class="profileStatusType">{{ profileStatus }}</div>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.settings-page { display: flex; flex-direction: column; gap: 20px; }
.page-header h1 { margin: 0; font-size: 1.9rem; font-weight: 600; color: #1c1b33; }

.content-panel {
  background: #fff;
  border-radius: 28px;
  box-shadow: 0 20px 60px rgba(21, 16, 45, 0.12);
  padding: 32px 36px;
  border: 1px solid rgba(28, 27, 51, 0.08);
  max-width: 700px;
}

.settings-form { display: flex; flex-direction: column; gap: 24px; }
.form-section { display: flex; flex-direction: column; gap: 18px; }

.section-title {
  font-weight: 600;
  font-size: 1.1rem;
  color: #5a2b98;
  margin-bottom: 4px;
  border-left: 4px solid #5a2b98;
  padding-left: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  align-items: center;
  gap: 16px;
}

.form-label { font-weight: 500; color: #1c1b33; font-size: 0.95rem; }
.form-label i { color: #6b6b82; margin-right: 6px; font-size: 1rem; }

.form-control {
  padding: 12px 16px;
  border: 1.5px solid #d9d2e8;
  border-radius: 16px;
  font-size: 0.95rem;
  background: white;
  transition: border 0.2s, box-shadow 0.2s;
  font-family: inherit;
  width: 100%;
  max-width: 420px;
  outline: none;
}

.form-control:focus { border-color: #5a2b98; box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.1); }
.form-control:disabled { background: #f4f3f7; color: #6b6b82; }

.tfa-box {
  border: 1px solid rgba(90, 43, 152, 0.12);
  border-radius: 20px;
  padding: 18px;
  background: #faf8ff;
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(156, 156, 178, 0.25);
  align-items: center;
  flex-wrap: wrap;
}

.form-status { width: 100%; min-height: 22px; font-size: 0.9rem; color: #6b6b82; }
.form-status.success { color: #167d68; }
.form-status.error { color: #b02a37; }

.btn-primary {
  background: #5a2b98;
  border: none;
  color: white;
  padding: 12px 30px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  font-family: inherit;
}
.btn-primary:hover { background: #4a2380; }
.btn-primary:disabled { background: #c4c4e0; cursor: not-allowed; }

.btn-secondary {
  background: transparent;
  border: 1.5px solid #6b6b82;
  color: #1c1b33;
  padding: 12px 28px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
}
.btn-secondary:hover { border-color: #5a2b98; color: #5a2b98; }

.btn-logout {
  background: transparent;
  border: 1.5px solid #dc3545;
  color: #dc3545;
  padding: 12px 28px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  margin-left: auto;
}
.btn-logout:hover { background: rgba(220, 53, 69, 0.05); }

@media (max-width: 960px) {
  .form-row { grid-template-columns: 1fr; gap: 6px; }
  .content-panel { padding: 24px; }
  .btn-logout { margin-left: 0; }
}
</style>
