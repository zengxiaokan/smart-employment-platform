import request from '@/utils/request'

export function getMatchJobs() {
  return request({ url: '/hr/match/jobs', method: 'get' })
}

export function getMatchList(jobId) {
  return request({ url: `/hr/match/list/${jobId}`, method: 'get' })
}

export function getMatchDetail(matchId) {
  return request({ url: `/hr/match/detail/${matchId}`, method: 'get' })
}

export function refreshMatchSummary(matchId) {
  return request({ url: `/hr/match/refresh/${matchId}`, method: 'post' })
}
