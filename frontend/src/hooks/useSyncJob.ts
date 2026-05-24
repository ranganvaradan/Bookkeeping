import { useCallback, useRef, useState } from 'react'
import { getJob, type Job } from '../api/jobs'
import { syncClient, type SyncRequest } from '../api/clients'

const POLL_INTERVAL_MS = 2000

export type SyncState = 'idle' | 'running' | 'done' | 'failed'

export function useSyncJob(clientId: string) {
  const [state, setState] = useState<SyncState>('idle')
  const [job, setJob] = useState<Job | null>(null)
  const [error, setError] = useState<string | null>(null)
  const pollTimerRef = useRef<number | null>(null)

  const clearPoll = useCallback(() => {
    if (pollTimerRef.current !== null) {
      window.clearInterval(pollTimerRef.current)
      pollTimerRef.current = null
    }
  }, [])

  const pollJob = useCallback(
    (jobId: string) => {
      clearPoll()
      setState('running')

      pollTimerRef.current = window.setInterval(async () => {
        try {
          const current = await getJob(jobId)
          setJob(current)

          if (current.status === 'DONE') {
            clearPoll()
            setState('done')
          } else if (current.status === 'FAILED') {
            clearPoll()
            setState('failed')
          }
        } catch {
          clearPoll()
          setError('Failed to check sync job status.')
          setState('failed')
        }
      }, POLL_INTERVAL_MS)
    },
    [clearPoll],
  )

  const runSync = useCallback(
    async (request: SyncRequest) => {
      setError(null)
      setJob(null)

      try {
        const { jobId } = await syncClient(clientId, request)
        pollJob(jobId)
      } catch {
        setError('Failed to start sync.')
        setState('failed')
      }
    },
    [clientId, pollJob],
  )

  return { state, job, error, runSync, clearPoll }
}
