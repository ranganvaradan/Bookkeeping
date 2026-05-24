import { useCallback, useRef, useState } from 'react'
import { connectQbo, getClient } from '../api/clients'

const POLL_INTERVAL_MS = 3000
const MAX_POLL_DURATION_MS = 30000

export type QboConnectState = 'idle' | 'waiting' | 'connected' | 'timeout' | 'error'

export function useQboConnect(clientId: string, onConnected?: () => void) {
  const [state, setState] = useState<QboConnectState>('idle')
  const [error, setError] = useState<string | null>(null)
  const pollTimerRef = useRef<number | null>(null)
  const pollStartRef = useRef<number | null>(null)

  const clearPoll = useCallback(() => {
    if (pollTimerRef.current !== null) {
      window.clearInterval(pollTimerRef.current)
      pollTimerRef.current = null
    }
    pollStartRef.current = null
  }, [])

  const startPolling = useCallback(() => {
    clearPoll()
    setState('waiting')
    pollStartRef.current = Date.now()

    pollTimerRef.current = window.setInterval(async () => {
      if (pollStartRef.current === null) return

      if (Date.now() - pollStartRef.current >= MAX_POLL_DURATION_MS) {
        clearPoll()
        setState('timeout')
        return
      }

      try {
        const client = await getClient(clientId)
        if (client.qboRealmId) {
          clearPoll()
          setState('connected')
          onConnected?.()
        }
      } catch {
        // keep polling until timeout
      }
    }, POLL_INTERVAL_MS)
  }, [clientId, clearPoll, onConnected])

  const connect = useCallback(async () => {
    setError(null)
    setState('idle')

    try {
      const { authUrl } = await connectQbo(clientId)
      const popup = window.open(authUrl, '_blank')

      if (!popup) {
        setError('Popup blocked. Please allow popups and try again.')
        setState('error')
        return
      }

      const closeWatcher = window.setInterval(() => {
        if (popup.closed) {
          window.clearInterval(closeWatcher)
          startPolling()
        }
      }, 500)
    } catch {
      setError('Failed to start QuickBooks connection.')
      setState('error')
    }
  }, [clientId, startPolling])

  return { state, error, connect, clearPoll }
}
