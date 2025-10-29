"use client"

import { useEffect, useState } from "react"
import Image from "next/image"

export function HologramAvatar({
  username,
  role,
  imageSrc,
}: {
  username: string
  role: string
  imageSrc?: string
}) {
  const [glitchActive, setGlitchActive] = useState(false)
  const [mousePos, setMousePos] = useState({ x: 0, y: 0 })
  const [scanlinePos, setScanlinePos] = useState(0)

  // Random glitch effect
  useEffect(() => {
    const glitchInterval = setInterval(() => {
      if (Math.random() > 0.7) {
        setGlitchActive(true)
        setTimeout(() => setGlitchActive(false), 100 + Math.random() * 200)
      }
    }, 2000 + Math.random() * 3000)

    return () => clearInterval(glitchInterval)
  }, [])

  // Animated scanline
  useEffect(() => {
    const interval = setInterval(() => {
      setScanlinePos((prev) => (prev + 1) % 100)
    }, 50)
    return () => clearInterval(interval)
  }, [])

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect()
    setMousePos({
      x: ((e.clientX - rect.left) / rect.width - 0.5) * 15,
      y: ((e.clientY - rect.top) / rect.height - 0.5) * 15,
    })
  }

  const handleMouseLeave = () => {
    setMousePos({ x: 0, y: 0 })
  }

  return (
    <div className="relative group cursor-pointer" onMouseMove={handleMouseMove} onMouseLeave={handleMouseLeave}>
      {/* Hologram container */}
      <div className="relative overflow-hidden rounded-lg bg-black/60 backdrop-blur-xl border border-cyan-500/40 p-4 shadow-[0_0_30px_rgba(34,211,238,0.3)]">
        {/* Animated scanlines overlay */}
        <div className="absolute inset-0 pointer-events-none z-20">
          <div
            className="absolute inset-0 bg-gradient-to-b from-transparent via-cyan-400/20 to-transparent h-32 blur-sm"
            style={{
              transform: `translateY(${scanlinePos}%)`,
              transition: "transform 0.05s linear",
            }}
          />
          <div
            className="absolute inset-0 opacity-20"
            style={{
              backgroundImage:
                "repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(34, 211, 238, 0.1) 2px, rgba(34, 211, 238, 0.1) 4px)",
            }}
          />
        </div>

        {/* Glitch RGB split overlay */}
        {glitchActive && (
          <>
            <div
              className="absolute inset-0 bg-red-500/30 mix-blend-screen z-10"
              style={{
                transform: `translate(${Math.random() * 6 - 3}px, ${Math.random() * 6 - 3}px)`,
                clipPath: `inset(${Math.random() * 50}% 0 ${Math.random() * 50}% 0)`,
              }}
            />
            <div
              className="absolute inset-0 bg-blue-500/30 mix-blend-screen z-10"
              style={{
                transform: `translate(${Math.random() * 6 - 3}px, ${Math.random() * 6 - 3}px)`,
                clipPath: `inset(${Math.random() * 50}% 0 ${Math.random() * 50}% 0)`,
              }}
            />
          </>
        )}

        {/* Content with parallax */}
        <div
          className="relative z-10 transition-transform duration-200 ease-out"
          style={{
            transform: `translate(${mousePos.x}px, ${mousePos.y}px)`,
          }}
        >
          {/* Avatar image with hologram effects */}
          <div className="relative mb-4 mx-auto w-32 h-32">
            {/* Glow background */}
            <div className="absolute inset-0 bg-cyan-500/20 rounded-full blur-xl animate-pulse" />

            {/* Image container */}
            <div className="relative w-full h-full rounded-full overflow-hidden border-2 border-cyan-400/50 shadow-[0_0_20px_rgba(34,211,238,0.5)]">
              {imageSrc ? (
                <Image
                  src={imageSrc}
                  alt={username}
                  fill
                  className="object-cover"
                  style={{
                    filter: "brightness(1.1) contrast(1.2) saturate(0.8)",
                    mixBlendMode: "screen",
                  }}
                />
              ) : (
                <div className="w-full h-full bg-gradient-to-br from-cyan-500/30 to-blue-500/30 flex items-center justify-center">
                  <span className="text-4xl text-cyan-300 font-bold">{username.charAt(0).toUpperCase()}</span>
                </div>
              )}

              {/* Hologram overlay on image */}
              <div
                className="absolute inset-0 pointer-events-none"
                style={{
                  background:
                    "linear-gradient(180deg, transparent 0%, rgba(34, 211, 238, 0.1) 50%, transparent 100%)",
                  animation: "scan 2s linear infinite",
                }}
              />
            </div>

            {/* Rotating ring */}
            <div className="absolute inset-0 rounded-full border border-cyan-400/30 animate-spin-slow" />
            <div
              className="absolute inset-0 rounded-full border border-cyan-400/20 animate-spin-slow"
              style={{ animationDirection: "reverse", animationDuration: "8s" }}
            />
          </div>

          {/* User info with glitch effect */}
          <div className="space-y-1 text-center">
            <p
              className={`text-base font-bold text-cyan-300 tracking-wider ${glitchActive ? "animate-pulse" : ""}`}
              style={{
                textShadow: "0 0 10px rgba(34, 211, 238, 0.8), 0 0 20px rgba(34, 211, 238, 0.4)",
                fontFamily: "monospace",
              }}
            >
              {username}
            </p>
            <p
              className="text-xs text-cyan-500/90 font-mono tracking-widest"
              style={{
                textShadow: "0 0 5px rgba(34, 211, 238, 0.5)",
              }}
            >
              ROLE: <span className="text-cyan-400 font-bold">{role}</span>
            </p>
          </div>
        </div>

        {/* Hologram glow effect on hover */}
        <div
          className="absolute inset-0 pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity duration-300 z-5"
          style={{
            background: "radial-gradient(circle at center, rgba(34, 211, 238, 0.15) 0%, transparent 70%)",
          }}
        />

        {/* Corner decorations */}
        <div className="absolute top-0 left-0 w-4 h-4 border-t-2 border-l-2 border-cyan-400/70" />
        <div className="absolute top-0 right-0 w-4 h-4 border-t-2 border-r-2 border-cyan-400/70" />
        <div className="absolute bottom-0 left-0 w-4 h-4 border-b-2 border-l-2 border-cyan-400/70" />
        <div className="absolute bottom-0 right-0 w-4 h-4 border-b-2 border-r-2 border-cyan-400/70" />

        {/* Data stream effect */}
        <div className="absolute top-2 right-2 text-[0.5rem] text-cyan-400/40 font-mono leading-tight pointer-events-none">
          <div className="animate-pulse">ID:{username.slice(0, 4).toUpperCase()}</div>
          <div className="animate-pulse" style={{ animationDelay: "0.5s" }}>
            STATUS:ACTIVE
          </div>
        </div>
      </div>

      {/* Floating particles */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden">
        <div className="absolute w-1 h-1 bg-cyan-400 rounded-full animate-float-1 opacity-60" />
        <div className="absolute w-1 h-1 bg-cyan-400 rounded-full animate-float-2 opacity-40" />
        <div className="absolute w-1 h-1 bg-cyan-400 rounded-full animate-float-3 opacity-50" />
        <div className="absolute w-0.5 h-0.5 bg-cyan-300 rounded-full animate-float-4 opacity-70" />
      </div>

      <style jsx>{`
        @keyframes scan {
          0% {
            transform: translateY(-100%);
          }
          100% {
            transform: translateY(100%);
          }
        }
        @keyframes spin-slow {
          from {
            transform: rotate(0deg);
          }
          to {
            transform: rotate(360deg);
          }
        }
        @keyframes float-1 {
          0%,
          100% {
            transform: translate(10%, 0%) scale(1);
            opacity: 0.6;
          }
          50% {
            transform: translate(90%, 100%) scale(0.5);
            opacity: 0.2;
          }
        }
        @keyframes float-2 {
          0%,
          100% {
            transform: translate(80%, 0%) scale(0.8);
            opacity: 0.4;
          }
          50% {
            transform: translate(20%, 100%) scale(1.2);
            opacity: 0.1;
          }
        }
        @keyframes float-3 {
          0%,
          100% {
            transform: translate(50%, 0%) scale(0.6);
            opacity: 0.5;
          }
          50% {
            transform: translate(60%, 100%) scale(1);
            opacity: 0.15;
          }
        }
        @keyframes float-4 {
          0%,
          100% {
            transform: translate(30%, 0%) scale(1);
            opacity: 0.7;
          }
          50% {
            transform: translate(70%, 100%) scale(0.8);
            opacity: 0.3;
          }
        }
        .animate-spin-slow {
          animation: spin-slow 6s linear infinite;
        }
        .animate-float-1 {
          animation: float-1 4s ease-in-out infinite;
        }
        .animate-float-2 {
          animation: float-2 5s ease-in-out infinite;
        }
        .animate-float-3 {
          animation: float-3 6s ease-in-out infinite;
        }
        .animate-float-4 {
          animation: float-4 4.5s ease-in-out infinite;
        }
      `}</style>
    </div>
  )
}
