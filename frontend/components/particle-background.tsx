"use client"

import { Canvas } from "@react-three/fiber"
import { Particles } from "./gl/particles"

export function ParticleBackground() {
  return (
    <div className="fixed inset-0 -z-10">
      <Canvas
        camera={{
          position: [1.26, 2.66, -1.82],
          fov: 50,
          near: 0.01,
          far: 300,
        }}
      >
        <color attach="background" args={["#000000"]} />
        <Particles
          speed={1.0}
          aperture={1.79}
          focus={3.8}
          size={512}
          noiseScale={0.6}
          noiseIntensity={0.52}
          timeScale={1}
          pointSize={10.0}
          opacity={0.8}
          planeScale={10.0}
          useManualTime={false}
          manualTime={0}
          introspect={false}
        />
      </Canvas>
    </div>
  )
}
