"use client";

import { useRef } from "react";

export function WallEMascot() {
  const videoRef = useRef<HTMLVideoElement>(null);

  return (
    <div
      className="fixed"
      style={{
        position: 'fixed',
        bottom: '1rem',
        right: '1rem',
        zIndex: 9999,
        width: '200px',
        height: '200px',
        pointerEvents: 'none'
      }}
    >
      {/* Glow effect */}
      <div className="absolute -inset-2 bg-gradient-to-r from-cyan-500/30 to-purple-500/30 rounded-2xl blur-lg opacity-60" />

      {/* Video container - Square with rounded corners */}
      <div className="relative w-full h-full rounded-2xl overflow-hidden border-2 border-cyan-500/40 shadow-xl bg-black/20">
        <video
          ref={videoRef}
          className="w-full h-full object-cover"
          muted
          loop
          autoPlay
          playsInline
        >
          <source src="/wall-e.mp4" type="video/mp4" />
        </video>
      </div>
    </div>
  );
}
