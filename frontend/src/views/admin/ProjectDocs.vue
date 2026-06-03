<template>
  <div class="pd-root" v-loading="loading">
    <div class="sub-hero">
      <span class="sub-hero-icon">📚</span>
      <div>
        <h2>项目文档</h2>
        <p>系统技术介绍、各端功能说明及数据库设计</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="pd-tabs" @tab-change="onTabChange">
      <el-tab-pane label="系统介绍" name="system-intro" />
      <el-tab-pane label="求职者端" name="user-side" />
      <el-tab-pane label="HR端" name="hr-side" />
      <el-tab-pane label="管理端" name="admin-guide" />
      <el-tab-pane label="数据库设计" name="database-design" />
    </el-tabs>

    <div class="markdown-body" v-html="html"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const activeTab = ref('system-intro')
const loading = ref(false)
const html = ref('')

const docMap = {
  'system-intro': '/help/system-intro.md',
  'user-side': '/help/user-side-doc.md',
  'hr-side': '/help/hr-side-doc.md',
  'admin-guide': '/help/admin-guide.md',
  'database-design': '/help/database-design.md',
}

function renderMd(md) {
  // Step 1: extract fenced code blocks → placeholders
  const blocks = []
  let out = md.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    const i = blocks.length
    blocks.push(code.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'))
    return `\x00CODE${i}\x00`
  })

  // Step 2: inline elements
  out = out
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/^---$/gm, '<hr>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/^> (.+)$/gm, '<blockquote><p>$1</p></blockquote>')

  out = out.replace(/((?:^\|.+\|\n?)+)/gm, (block) => {
    const lines = block.trim().split('\n').filter(l => l.includes('|'))
    if (lines.length < 2) return block
    const isSep = l => /^\|[\s\-:|]+\|$/.test(l)
    const rows = lines.filter(l => !isSep(l))
    if (!rows.length) return block
    const th = rows[0], td = rows.slice(1)
    const toCells = r => r.split('|').filter(c => c.trim()).map(c => `<${r === th ? 'th' : 'td'}>${c.trim()}</${r === th ? 'th' : 'td'}>`).join('')
    return `<table><thead><tr>${toCells(th)}</tr></thead><tbody>${td.map(r => `<tr>${toCells(r)}</tr>`).join('')}</tbody></table>`
  })
  out = out.replace(/((?:^[-*] .+\n?)+)/gm, (m) => {
    const items = m.trim().split('\n').map(l => l.replace(/^[-*] /, '')).filter(Boolean)
    return '<ul>' + items.map(i => `<li>${i}</li>`).join('') + '</ul>'
  })
  out = out.replace(/((?:^\d+\. .+\n?)+)/gm, (m) => {
    const items = m.trim().split('\n').map(l => l.replace(/^\d+\. /, '')).filter(Boolean)
    return '<ol>' + items.map(i => `<li>${i}</li>`).join('') + '</ol>'
  })

  const chunks = out.split('\n')
  const result = []
  let buf = []
  const isBlock = l => /^<(h[1-3]|hr|table|blockquote|ul|ol|li|thead|tbody|tr|th|td|code|strong|pre)/.test(l)
  const flush = () => {
    if (buf.length) { result.push('<p>' + buf.join('<br>') + '</p>'); buf = [] }
  }
  for (const l of chunks) {
    const t = l.trim()
    if (!t) { flush(); continue }
    if (isBlock(t)) { flush(); result.push(t); continue }
    buf.push(t)
  }
  flush()
  return result.join('\n').replace(/\x00CODE(\d+)\x00/g, (_, i) =>
    `<pre><code>${blocks[parseInt(i)]}</code></pre>`
  )
}

const loadDoc = async (key) => {
  loading.value = true
  try {
    const res = await fetch(docMap[key])
    if (res.ok) {
      html.value = renderMd(await res.text())
    } else {
      html.value = '<p style="color:#94a3b8">文档加载失败，请稍后重试。</p>'
    }
  } catch {
    html.value = '<p style="color:#94a3b8">文档加载失败，请检查网络后重试。</p>'
  } finally {
    loading.value = false
  }
}

const onTabChange = (name) => {
  loadDoc(name)
}

onMounted(() => loadDoc(activeTab.value))
</script>

<style scoped>
.pd-root {  }
.sub-hero { display:flex; align-items:center; gap:16px; padding:24px 28px; background:linear-gradient(135deg,#eff6ff,#dbeafe); border-radius:14px; margin-bottom:28px; }
.sub-hero-icon { font-size:40px; }
.sub-hero h2 { font-size:20px; font-weight:700; color:#1e40af; margin:0 0 4px; }
.sub-hero p { font-size:13px; color:#64748b; margin:0; }

.pd-tabs { margin-bottom: 24px; }
.pd-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.pd-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 600; }

.markdown-body { max-width: 960px; font-size: 15px; color: #334155; line-height: 1.85; }

.markdown-body :deep(h1) {
  font-size: 26px; font-weight: 800; color: #0f172a; margin: 0 0 8px;
  padding-bottom: 16px; border-bottom: 2px solid #f1f5f9;
}
.markdown-body :deep(h2) {
  font-size: 19px; font-weight: 700; color: #0f172a; margin: 36px 0 14px;
  padding-bottom: 8px; border-bottom: 1px solid #f8fafc;
}
.markdown-body :deep(h3) {
  font-size: 16px; font-weight: 600; color: #334155; margin: 24px 0 10px;
}
.markdown-body :deep(p) { margin: 0 0 14px; }
.markdown-body :deep(ul), .markdown-body :deep(ol) { margin: 8px 0 18px; padding-left: 22px; }
.markdown-body :deep(li) { margin-bottom: 4px; }
.markdown-body :deep(strong) { color: #0f172a; font-weight: 700; }

.markdown-body :deep(table) {
  width: 100%; border-collapse: collapse; margin: 16px 0 20px;
  font-size: 14px;
}
.markdown-body :deep(th) {
  background: #f8fafc; text-align: left; padding: 10px 14px;
  font-weight: 700; color: #0f172a; border-bottom: 2px solid #e2e8f0;
}
.markdown-body :deep(td) {
  padding: 9px 14px; border-bottom: 1px solid #f1f5f9; color: #475569;
}
.markdown-body :deep(tr:hover td) { background: #fafbfc; }

.markdown-body :deep(blockquote) {
  margin: 16px 0; padding: 12px 18px;
  background: #eff6ff; border-left: 4px solid #3b82f6;
  border-radius: 0 8px 8px 0; color: #1e40af; font-size: 14px;
}
.markdown-body :deep(blockquote p) { margin: 0; }

.markdown-body :deep(code) {
  background: #f1f5f9; padding: 2px 6px; border-radius: 4px;
  font-size: 13px; color: #e11d48;
}
.markdown-body :deep(pre) {
  background: #1e293b; color: #e2e8f0; padding: 16px 20px;
  border-radius: 10px; overflow-x: auto; margin: 16px 0;
  font-size: 13px; line-height: 1.7;
}
.markdown-body :deep(pre code) {
  background: transparent; color: inherit; padding: 0; border-radius: 0;
  font-size: inherit;
}
.markdown-body :deep(hr) {
  border: none; border-top: 1px solid #f1f5f9; margin: 32px 0;
}
</style>
