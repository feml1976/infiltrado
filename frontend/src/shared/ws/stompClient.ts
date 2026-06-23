import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_URL = (() => {
  const base = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  return base ? `${base}/ws` : '/ws'
})()

type BodyCallback = (body: string) => void

let activeClient: Client | null = null
let activeSub:    { unsubscribe: () => void } | null = null

export function connectPartidaSocket(
  token: string,
  partidaId: string,
  onMessage: BodyCallback,
  onReconnect: () => void,
): void {
  disconnectPartidaSocket()
  let firstConnect = true

  activeClient = new Client({
    webSocketFactory: () => new SockJS(WS_URL) as WebSocket,
    connectHeaders:   { Authorization: `Bearer ${token}` },
    heartbeatIncoming: 10_000,
    heartbeatOutgoing: 10_000,
    reconnectDelay:    3_000,
    onConnect: () => {
      activeSub = activeClient!.subscribe(
        `/topic/partida/${partidaId}`,
        (msg) => onMessage(msg.body),
      )
      if (!firstConnect) onReconnect()
      firstConnect = false
    },
  })

  activeClient.activate()
}

export function disconnectPartidaSocket(): void {
  activeSub?.unsubscribe()
  activeSub = null
  if (activeClient?.active) activeClient.deactivate()
  activeClient = null
}
