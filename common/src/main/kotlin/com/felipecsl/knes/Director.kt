package com.felipecsl.knes

import com.felipecsl.knes.CPU.Companion.FREQUENCY
import kotlin.math.roundToInt

object Director {
  fun startConsole(
      cartridgeData: ByteArray,
      sprite: Sprite,
      mapperCallback: MapperStepCallback? = null,
      cpuCallback: CPUStepCallback? = null,
      ppuCallback: PPUStepCallback? = null
  ) {
    val cartridge = INESFileParser.parseCartridge(ByteArrayInputStream(cartridgeData))
    val console = Console.newConsole(
        cartridge, sprite, ::Bitmap, mapperCallback, cpuCallback, ppuCallback)
    console.reset()
    while (true) {
      var totalCycles = 0L
      val startTime = currentTimeMs()
      while (totalCycles < FREQUENCY) {
        totalCycles += console.step()
      }
      val secondsSpent = (currentTimeMs() - startTime) / 1000
      val clock = totalCycles / secondsSpent
      val speed = (clock / FREQUENCY.toFloat()) * 100
      println("Clock=${clock}Hz (${speed.roundToInt()}% speed)")
    }
  }
}