import api from './client'

export interface Job {
  id: string
  status: 'PENDING' | 'RUNNING' | 'DONE' | 'FAILED'
  resultSummary: string | null
  startedAt: string | null
  completedAt: string | null
}

export async function getJob(jobId: string): Promise<Job> {
  const { data } = await api.get<Job>(`/api/jobs/${jobId}`)
  return data
}
