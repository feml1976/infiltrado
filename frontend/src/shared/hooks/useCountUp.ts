import { useState, useEffect, useRef } from 'react'

const prefersReducedMotion =
  typeof window !== 'undefined' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches

export function useCountUp(target: number, duration = 550): number {
  const [display, setDisplay] = useState(target)
  const prevRef = useRef(target)

  useEffect(() => {
    if (prevRef.current === target) return
    const from = prevRef.current
    prevRef.current = target

    if (prefersReducedMotion) {
      setDisplay(target)
      return
    }

    const startTime = performance.now()
    let raf: number
    function step(now: number) {
      const t = Math.min((now - startTime) / duration, 1)
      const eased = 1 - (1 - t) ** 2
      setDisplay(Math.round(from + (target - from) * eased))
      if (t < 1) raf = requestAnimationFrame(step)
    }
    raf = requestAnimationFrame(step)
    return () => cancelAnimationFrame(raf)
  }, [target, duration])

  return display
}
