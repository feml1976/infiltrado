import { useState, useCallback, useRef } from 'react'

const STORAGE_KEY = 'infiltrado_sound'

function tone(ctx: AudioContext, hz: number, t0: number, dur: number, vol = 0.25) {
  const osc = ctx.createOscillator()
  const g = ctx.createGain()
  osc.connect(g)
  g.connect(ctx.destination)
  osc.frequency.value = hz
  osc.type = 'sine'
  g.gain.setValueAtTime(0, t0)
  g.gain.linearRampToValueAtTime(vol, t0 + 0.01)
  g.gain.exponentialRampToValueAtTime(0.001, t0 + dur)
  osc.start(t0)
  osc.stop(t0 + dur + 0.05)
}

export function useSoundToggle() {
  const [soundOn, setSoundOn] = useState<boolean>(() =>
    typeof window !== 'undefined' && localStorage.getItem(STORAGE_KEY) === 'on',
  )
  const ctxRef = useRef<AudioContext | null>(null)
  const soundOnRef = useRef(soundOn)
  soundOnRef.current = soundOn

  const getCtx = useCallback((): AudioContext => {
    if (!ctxRef.current) ctxRef.current = new AudioContext()
    return ctxRef.current
  }, [])

  const toggleSound = useCallback(() => {
    setSoundOn((prev) => {
      const next = !prev
      localStorage.setItem(STORAGE_KEY, next ? 'on' : 'off')
      return next
    })
  }, [])

  /* Breve ding ascendente — revelación de jugador NORMAL */
  const playReveal = useCallback(() => {
    if (!soundOnRef.current) return
    try {
      const ctx = getCtx()
      const t = ctx.currentTime
      tone(ctx, 523, t, 0.12)
      tone(ctx, 659, t + 0.1, 0.18)
    } catch { /* ignorar errores de AudioContext en entornos sin audio */ }
  }, [getCtx])

  /* Tonos descendentes y graves — revelación de INFILTRADO */
  const playInfiltrado = useCallback(() => {
    if (!soundOnRef.current) return
    try {
      const ctx = getCtx()
      const t = ctx.currentTime
      tone(ctx, 392, t, 0.1, 0.3)
      tone(ctx, 349, t + 0.09, 0.1, 0.3)
      tone(ctx, 294, t + 0.18, 0.25, 0.3)
    } catch { /* ignorar errores de AudioContext en entornos sin audio */ }
  }, [getCtx])

  /* Fanfarria ascendente de cuatro notas — fin de partida */
  const playFinale = useCallback(() => {
    if (!soundOnRef.current) return
    try {
      const ctx = getCtx()
      const t = ctx.currentTime
      tone(ctx, 523, t, 0.12)
      tone(ctx, 659, t + 0.1, 0.12)
      tone(ctx, 784, t + 0.2, 0.12)
      tone(ctx, 1047, t + 0.3, 0.4, 0.3)
    } catch { /* ignorar errores de AudioContext en entornos sin audio */ }
  }, [getCtx])

  return { soundOn, toggleSound, playReveal, playInfiltrado, playFinale }
}
