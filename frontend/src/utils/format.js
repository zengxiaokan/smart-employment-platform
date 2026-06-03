export function parseSkills(skillsStr) {
  if (!skillsStr) return [];
  if (Array.isArray(skillsStr)) return skillsStr;
  if (typeof skillsStr === 'string') {
    return skillsStr.split(',').map(s => s.trim()).filter(s => s !== '');
  }
  return [];
}

export function formatSkills(skillsArr) {
  if (!skillsArr) return '';
  if (typeof skillsArr === 'string') return skillsArr;
  if (Array.isArray(skillsArr)) {
    return skillsArr.join(',');
  }
  return '';
}

const fmtSalary = (v) => {
  if (v == null) return ''
  if (v <= 200) return v + 'K'
  return Math.round(v / 1000) + 'K'
}

export function formatSalary(min, max) {
  if (!min && !max) return '面议';
  const fmtMin = min ? fmtSalary(min) : ''
  const fmtMax = max ? fmtSalary(max) : ''
  if (!min) return fmtMax + '及以下'
  if (!max) return fmtMin + '及以上'
  return fmtMin + '-' + fmtMax
}

export function maxEducationLabel(v) {
  const map = { 0: '初中', 1: '高中', 2: '中专', 3: '大专', 4: '本科', 5: '硕士', 6: '博士' }
  return map[v] ?? '-'
}

export function educationLabel(v) {
  const map = { 0: '初中', 1: '高中', 2: '中专', 3: '大专', 4: '本科', 5: '硕士', 6: '博士' }
  return map[v] ?? '-'
}

export function jobEducationLabel(v) {
  const map = { 0: '不限', 1: '初中', 2: '高中', 3: '中专', 4: '大专', 5: '本科', 6: '硕士', 7: '博士' }
  return map[v] ?? '-'
}