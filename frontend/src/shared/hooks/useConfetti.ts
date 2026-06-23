import { useEffect } from 'react'

const PALETTE = ['#6366f1','#ec4899','#f59e0b','#10b981','#3b82f6','#f97316','#8b5cf6','#14b8a6','#ef4444']
const DURATION = 3500

type Particle = {
  x: number; y: number
  vx: number; vy: number
  color: string
  w: number; h: number
  r: number; dr: number
}

export function useConfetti(active: boolean) {
  useEffect(() => {
    if (!active) return
    if (typeof window === 'undefined') return
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return

    const COUNT = window.innerWidth < 768 ? 45 : 90

    const canvas = document.createElement('canvas')
    canvas.style.cssText = 'position:fixed;inset:0;width:100%;height:100%;pointer-events:none;z-index:9999;'
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    document.body.appendChild(canvas)

    const ctx = canvas.getContext('2d')
    if (!ctx) { canvas.remove(); return }

    const rnd = () => Math.random()
    const particles: Particle[] = Array.from({ length: COUNT }, () => ({
      x: rnd() * canvas.width,
      y: -20 - rnd() * canvas.height * 0.3,
      vx: (rnd() - 0.5) * 4,
      vy: 2 + rnd() * 4,
      color: PALETTE[Math.floor(rnd() * PALETTE.length)],
      w: 6 + rnd() * 8,
      h: 3 + rnd() * 4,
      r: rnd() * Math.PI * 2,
      dr: (rnd() - 0.5) * 0.15,
    }))

    const start = performance.now()
    let raf: number

    function draw(now: number) {
      const elapsed = now - start
      if (elapsed > DURATION) { canvas.remove(); return }

      ctx!.clearRect(0, 0, canvas.width, canvas.height)
      const fade = Math.max(0, (elapsed - DURATION * 0.55) / (DURATION * 0.45))

      for (const p of particles) {
        p.x += p.vx
        p.y += p.vy
        p.vy += 0.06
        p.r += p.dr
        ctx!.save()
        ctx!.globalAlpha = 1 - fade
        ctx!.translate(p.x, p.y)
        ctx!.rotate(p.r)
        ctx!.fillStyle = p.color
        ctx!.fillRect(-p.w / 2, -p.h / 2, p.w, p.h)
        ctx!.restore()
      }
      raf = requestAnimationFrame(draw)
    }
    raf = requestAnimationFrame(draw)

    return () => {
      cancelAnimationFrame(raf)
      canvas.remove()
    }
  }, [active])
}
