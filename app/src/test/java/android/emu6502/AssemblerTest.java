package android.emu6502;

import android.emu6502.instructions.Symbols;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AssemblerTest {

  private Assembler assembler;

  @Before public void setUp() {
    assembler = new Assembler(new Memory(new Display()), new Symbols());
  }

  @Test public void testSimple() {
    List<String> lines = ImmutableList.of(
        "LDA #$01",
        "STA $0200",
        "LDA #$05",
        "STA $0201",
        "LDA #$08",
        "STA $0202");
    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(), equalTo("0600: A9 01 8D 00 02 A9 05 8D 01 02 A9 08 8D 02 02"));
  }

  @Test public void testWithComments() {
    List<String> lines = ImmutableList.of(
        "LDA #$c0  ;Load the hex value $c0 into the A register",
        "TAX       ;Transfer the value in the A register to X",
        "INX       ;Increment the value in the X register",
        "ADC #$c4  ;Add the hex value $c4 to the A register",
        "BRK       ;Break - we're done");
    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(), equalTo("0600: A9 C0 AA E8 69 C4 00"));
  }

  @Test public void testBranchAndLabel() {
    List<String> lines = ImmutableList.of(
        "LDX #$08",
        "decrement:",
        "DEX",
        "STX $0200",
        "CPX #$03",
        "BNE decrement",
        "STX $0201",
        "BRK");
    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(), equalTo("0600: A2 08 CA 8E 00 02 E0 03 D0 F8 8E 01 02 00"));
  }

  @Test public void testRelative() {
    List<String> lines = ImmutableList.of(
        "LDA #$01",
        "CMP #$02",
        "BNE notequal",
        "STA $22",
        "notequal:",
        "BRK");

    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(), equalTo("0600: A9 01 C9 02 D0 02 85 22 00"));
  }

  @Test public void testIndirect() {
    List<String> lines = ImmutableList.of(
        "LDA #$01",
        "STA $f0",
        "LDA #$cc",
        "STA $f1",
        "JMP ($00f0) ;dereferences to $cc01");

    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(), equalTo("0600: A9 01 85 F0 A9 CC 85 F1 6C F0 00"));
  }

  @Test public void testIndirectX() {
    List<String> lines = ImmutableList.of(
        "LDX #$01",
        "LDA #$05",
        "STA $01",
        "LDA #$06",
        "STA $02",
        "LDY #$0a",
        "STY $0605",
        "LDA ($00,X)");

    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(),
        equalTo("0600: A2 01 A9 05 85 01 A9 06 85 02 A0 0A 8C 05 06 A1 \n0610: 00"));
  }

  @Test public void testIndirectY() {
    List<String> lines = ImmutableList.of(
        "LDY #$01",
        "LDA #$03",
        "STA $01",
        "LDA #$07",
        "STA $02",
        "LDX #$0a",
        "STX $0704",
        "LDA ($01),Y");

    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(),
        equalTo("0600: A0 01 A9 03 85 01 A9 07 85 02 A2 0A 8E 04 07 B1 \n0610: 01"));
  }

  @Test public void testJump() {
    List<String> lines = ImmutableList.of(
        "LDA #$03",
        "JMP there",
        "BRK",
        "BRK",
        "BRK",
        "there:",
        "STA $0200");

    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(), equalTo("0600: A9 03 4C 08 06 00 00 00 8D 00 02"));
  }

  @Test public void testSymbols() {
    List<String> lines = ImmutableList.of(
        "define  sysRandom  $fe ; an adress",
        "define  a_dozen    $0c ; a constant",
        "LDA sysRandom  ; equivalent to \"LDA $fe\"",
        "LDX #a_dozen   ; equivalent to \"LDX #$0c\"");
    assembler.assembleCode(lines);
    assertThat(assembler.hexdump(), equalTo("0600: A5 FE A2 0C"));
  }
}
